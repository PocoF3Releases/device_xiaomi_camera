/*
 * Native Java entry points for the existing Xiaomi MiSys 3.0 JNI library.
 * Names, static modifiers and descriptors are part of its RegisterNatives ABI.
 */
package vendor.xiaomi.hardware.misys.V3_0;

import android.util.Log;

public class MiSys {
    private static final String TAG = "MiSys@3.0-Java";
    private static final boolean LIBRARY_LOADED = loadLibrary();

    public static native long getFileSize(String path, String fileName);
    public static native int init();
    public static native byte[] readFromFile(String path, String fileName, long size);
    public static native boolean setProp(String name, String value);
    public static native int writeToFile(byte[] data, String path, String fileName, long size);

    /** Library load state only; init() and HAL operation results remain authoritative. */
    public static boolean isNativeLibraryLoaded() { return LIBRARY_LOADED; }

    private static boolean loadLibrary() {
        // This device ships the suffixed library. Keep the stock name as a fallback
        // for other users of the compatibility library, without swallowing native calls.
        // RegisterNatives can throw NoSuchMethodError for an incompatible descriptor,
        // not just UnsatisfiedLinkError. Keep an ABI mismatch from poisoning this class
        // before a compatible fallback can be tried. Native operations still fail normally.
        try {
            System.loadLibrary("misys_jni.xiaomi");
            return true;
        } catch (LinkageError | SecurityException first) {
            try {
                System.loadLibrary("misys_jni");
                return true;
            } catch (LinkageError | SecurityException second) {
                // Keep the shipped library's linkage/ABI error as the primary cause.
                // A missing fallback must not conceal a broken dependency of .xiaomi.
                if (first != second) first.addSuppressed(second);
                Log.w(TAG, "MiSys JNI unavailable; native operations are not supported", first);
                return false;
            }
        }
    }
}
