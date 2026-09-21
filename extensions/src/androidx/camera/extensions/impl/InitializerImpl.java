// SPDX-License-Identifier: Apache-2.0
package androidx.camera.extensions.impl;

import android.content.Context;
import java.util.concurrent.Executor;

public final class InitializerImpl {
    public interface OnExtensionsInitializedCallback {
        void onSuccess();
        void onFailure(int error);
    }
    public interface OnExtensionsDeinitializedCallback {
        void onSuccess();
        void onFailure(int error);
    }
    public static void init(String version, Context context,
            OnExtensionsInitializedCallback callback, Executor executor) {
        Runnable done = () -> {
            if (context != null && new ExtensionVersionImpl().checkApiVersion(version) != null) {
                callback.onSuccess();
            } else {
                callback.onFailure(0);
            }
        };
        if (executor != null) executor.execute(done); else done.run();
    }
    public static void deinit(OnExtensionsDeinitializedCallback callback, Executor executor) {
        if (executor != null) executor.execute(callback::onSuccess); else callback.onSuccess();
    }
}
