// SPDX-License-Identifier: Apache-2.0
package androidx.camera.extensions.impl;

import android.util.Pair;
import android.util.Size;
import java.util.List;

abstract class XiaomiPreview extends XiaomiExtender implements PreviewExtenderImpl {
    XiaomiPreview(int algorithm) { super(algorithm); }
    @Override public CaptureStageImpl getCaptureStage() { return stage(false); }
    @Override public ProcessorType getProcessorType() { return ProcessorType.PROCESSOR_TYPE_NONE; }
    @Override public ProcessorImpl getProcessor() { return null; }
    @Override public List<Pair<Integer, Size[]>> getSupportedResolutions() { return null; }
}
