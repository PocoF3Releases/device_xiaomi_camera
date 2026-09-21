// SPDX-License-Identifier: Apache-2.0
package androidx.camera.extensions.impl;

/** Basic HAL-side processing; no advanced or app-side image processor. */
public final class ExtensionVersionImpl {
    public String checkApiVersion(String version) {
        return version != null && version.startsWith("1.") ? "1.1.0" : null;
    }
    public boolean isAdvancedExtenderImplemented() { return false; }
}
