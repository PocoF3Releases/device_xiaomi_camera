/* SPDX-License-Identifier: Apache-2.0 */
package vendor.xiaomi.hardware.misys;

/** Bounds checks for the generated HIDL vector layout, not a file-size quota. */
public final class HidlVector {
    private HidlVector() {}
    public static int byteSize(int count, int stride) {
        if (count < 0 || stride <= 0 || count > Integer.MAX_VALUE / stride) {
            throw new IllegalArgumentException("Invalid MiSys vector size");
        }
        return count * stride;
    }
}
