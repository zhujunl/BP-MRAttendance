package com.miaxis.attendance.tts;

import android.speech.tts.UtteranceProgressListener;

/**
 * @author Tank
 * @date 2020/9/11 13:45
 * @des
 * @updateAuthor
 * @updateDes
 */
public abstract class TTSSpeechCallback extends UtteranceProgressListener {

    private String mUtteranceId;

    public TTSSpeechCallback(String speechId) {
        this.mUtteranceId = speechId;
    }

    @Override
    public void onStart(String utteranceId) {
        this.onTTSSpeechStart(utteranceId);
    }

    @Override
    public void onDone(String utteranceId) {
        this.onTTSSpeechDone(utteranceId);
    }

    @Override
    public void onError(String utteranceId) {
        this.onTTSSpeechError(utteranceId);
    }

    public abstract void onTTSSpeechStart(String utteranceId);

    public abstract void onTTSSpeechDone(String utteranceId);

    public abstract void onTTSSpeechError(String utteranceId);

    public String getUtteranceId() {
        return mUtteranceId;
    }

}
