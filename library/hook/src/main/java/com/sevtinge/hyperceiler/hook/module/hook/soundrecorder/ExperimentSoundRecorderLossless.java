package com.sevtinge.hyperceiler.hook.module.hook.soundrecorder;

import com.sevtinge.hyperceiler.hook.module.base.BaseHook;

/**
 * [Experiment] Enable lossless FLAC audio recording in Sound Recorder.
 */
public class ExperimentSoundRecorderLossless extends BaseHook {
    @Override
    public void init() {
        findAndHookMethodSilently("com.android.soundrecorder.RecorderParameters", "getAudioOutputFormat",
            new MethodHook() {
                @Override
                protected void after(MethodHookParam param) {
                    // MediaRecorder.OutputFormat.OGG = 11 (FLAC-compatible lossless)
                    param.setResult(11);
                }
            });
        findAndHookMethodSilently("com.android.soundrecorder.RecorderParameters", "isLosslessSupported",
            new MethodHook() {
                @Override
                protected void after(MethodHookParam param) {
                    param.setResult(true);
                }
            });
        findAndHookMethodSilently("com.miui.soundrecorder.RecordingConfig", "isFLACEnabled",
            new MethodHook() {
                @Override
                protected void after(MethodHookParam param) {
                    param.setResult(true);
                }
            });
        findAndHookMethodSilently("com.android.soundrecorder.RecorderParameters", "getMaxSampleRate",
            new MethodHook() {
                @Override
                protected void after(MethodHookParam param) {
                    param.setResult(192000);
                }
            });
    }
}
