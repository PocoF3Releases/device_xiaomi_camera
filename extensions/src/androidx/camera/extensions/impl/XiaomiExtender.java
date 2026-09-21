// SPDX-License-Identifier: Apache-2.0
package androidx.camera.extensions.impl;

import android.content.Context;
import android.graphics.ImageFormat;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CaptureRequest;
import android.hardware.camera2.params.SessionConfiguration;
import android.hardware.camera2.params.StreamConfigurationMap;
import android.util.Log;
import android.util.Pair;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/** Bridges the alioth CamX v1 capability table to the basic extension contract. */
abstract class XiaomiExtender implements ExtenderStateListener {
    private static final String TAG = "XiaomiCameraExtensions";
    private static final CameraCharacteristics.Key<byte[]> ALGOS = new CameraCharacteristics.Key<>(
            "com.xiaomi.capabilities.algoSupport", byte[].class);
    private static final CameraCharacteristics.Key<byte[]> VERSION = new CameraCharacteristics.Key<>(
            "com.xiaomi.capabilities.algoVersion", byte[].class);
    private static final CameraCharacteristics.Key<Byte> ENABLED = new CameraCharacteristics.Key<>(
            "com.xiaomi.capabilities.algoCameraXEnabled", Byte.class);
    private static final CameraCharacteristics.Key<Integer> ROLE = new CameraCharacteristics.Key<>(
            "com.xiaomi.cameraid.role.cameraId", Integer.class);
    private static final CaptureRequest.Key<Integer> OPERATION = new CaptureRequest.Key<>(
            "com.xiaomi.sessionparams.operation", Integer.class);
    private static final CaptureRequest.Key<Byte> HDR = new CaptureRequest.Key<>(
            "com.xiaomi.algo.hdrMode", Byte.class);
    private static final CaptureRequest.Key<Byte> NIGHT = new CaptureRequest.Key<>(
            "com.xiaomi.algo.nightModeEnable", Byte.class);
    private final int algorithm;
    private int operation = -1;
    private boolean supportsZsl;

    XiaomiExtender(int algorithm) { this.algorithm = algorithm; }

    private int findOperation(CameraCharacteristics c) {
        if (c == null || (algorithm != 1 && algorithm != 8)) return -1;
        try {
            byte[] version = c.get(VERSION);
            byte[] table = c.get(ALGOS);
            Byte enabled = c.get(ENABLED);
            Integer role = c.get(ROLE);
            if (version == null || !record(version, 0, version.length).equals("1.0")
                    || enabled == null || enabled != 1 || role == null
                    || table == null || table.length != 6 * 256) return -1;
            List<CaptureRequest.Key<?>> requests = c.getAvailableCaptureRequestKeys();
            List<CaptureRequest.Key<?>> sessions = c.getAvailableSessionKeys();
            if (requests == null || !requests.contains(OPERATION)
                    || sessions == null || !sessions.contains(OPERATION)) return -1;
            // This v1 HAL defines hdrMode/nightModeEnable in its vendor-tag registry,
            // but omits them from availableRequestKeys. The per-role algorithm bit
            // is the capability contract used by Xiaomi's SDK for those controls.
            StreamConfigurationMap streams = c.get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP);
            if (streams == null || streams.getOutputSizes(ImageFormat.JPEG) == null
                    || streams.getOutputSizes(ImageFormat.PRIVATE) == null) return -1;
            // SDK v58 maps record 0 to NORMAL (65290), record 2 to SUPER_NIGHT (65292).
            // Do not reuse video/HQ records just because they contain the same algorithm bit.
            int index = algorithm == 1 ? 0 : 2;
            for (String entry : record(table, index * 256, 256).split("\\|")) {
                String[] fields = entry.split(":");
                if (fields.length == 2 && fields[0].equals(Integer.toString(role))) {
                    return (Integer.parseInt(fields[1]) & algorithm) != 0 ? 65290 + index : -1;
                }
            }
        } catch (IllegalArgumentException e) {
            // Absent vendor tags or malformed firmware data must report unsupported.
            Log.w(TAG, "Unsupported CamX capabilities", e);
        }
        return -1;
    }

    private static String record(byte[] data, int offset, int length) {
        int end = offset;
        while (end < offset + length && data[end] != 0) end++;
        return new String(data, offset, end - offset, StandardCharsets.US_ASCII).trim();
    }

    public boolean isExtensionAvailable(String cameraId, CameraCharacteristics characteristics) {
        return findOperation(characteristics) >= 0;
    }

    public void init(String cameraId, CameraCharacteristics characteristics) {
        operation = findOperation(characteristics);
        List<CaptureRequest.Key<?>> keys = characteristics.getAvailableCaptureRequestKeys();
        supportsZsl = keys != null && keys.contains(CaptureRequest.CONTROL_ENABLE_ZSL);
    }

    @Override public void onInit(String cameraId, CameraCharacteristics c, Context context) {
        init(cameraId, c);
    }
    @Override public void onDeInit() { operation = -1; supportsZsl = false; }
    @Override public int onSessionType() { return SessionConfiguration.SESSION_REGULAR; }
    @Override public CaptureStageImpl onPresetSession() { return stage(false); }
    @Override public CaptureStageImpl onEnableSession() {
        Log.i(TAG, "Starting HAL extension algorithm=" + algorithm + " operation=" + operation);
        return stage(false);
    }
    @Override public CaptureStageImpl onDisableSession() { return stage(false); }

    final CaptureStageImpl stage(boolean capture) {
        if (operation < 0) throw new IllegalStateException("Extension not initialized/supported");
        List<Pair<CaptureRequest.Key, Object>> parameters = new ArrayList<>();
        parameters.add(new Pair<>(OPERATION, operation));
        parameters.add(new Pair<>(algorithm == 1 ? HDR : NIGHT, (byte) (capture ? 1 : 0)));
        if (capture && supportsZsl) parameters.add(new Pair<>(CaptureRequest.CONTROL_ENABLE_ZSL, false));
        List<Pair<CaptureRequest.Key, Object>> values = Collections.unmodifiableList(parameters);
        return new CaptureStageImpl() {
            @Override public int getId() { return 0; }
            @Override public List<Pair<CaptureRequest.Key, Object>> getParameters() { return values; }
        };
    }
}
