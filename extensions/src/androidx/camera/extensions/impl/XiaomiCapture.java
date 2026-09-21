// SPDX-License-Identifier: Apache-2.0
package androidx.camera.extensions.impl;

import android.hardware.camera2.CaptureRequest;
import android.hardware.camera2.CaptureResult;
import android.util.Pair;
import android.util.Range;
import android.util.Size;
import java.util.Collections;
import java.util.List;

abstract class XiaomiCapture extends XiaomiExtender implements ImageCaptureExtenderImpl {
    XiaomiCapture(int algorithm) { super(algorithm); }
    // One JPEG request; CamX owns multi-frame processing, not an app-side YUV processor.
    @Override public CaptureProcessorImpl getCaptureProcessor() { return null; }
    @Override public List<CaptureStageImpl> getCaptureStages() { return Collections.singletonList(stage(true)); }
    @Override public int getMaxCaptureStage() { return 1; }
    @Override public List<Pair<Integer, Size[]>> getSupportedResolutions() { return null; }
    @Override public List<Pair<Integer, Size[]>> getSupportedPostviewResolutions(Size size) { return Collections.emptyList(); }
    @Override public Range<Long> getEstimatedCaptureLatencyRange(Size size) { return null; }
    @Override public List<CaptureRequest.Key> getAvailableCaptureRequestKeys() { return Collections.emptyList(); }
    @Override public List<CaptureResult.Key> getAvailableCaptureResultKeys() { return Collections.emptyList(); }
    @Override public boolean isCaptureProcessProgressAvailable() { return false; }
    @Override public Pair<Long, Long> getRealtimeCaptureLatency() { return null; }
    @Override public boolean isPostviewAvailable() { return false; }
}
