package com.miaxis.attendance.tts;

import android.content.Context;
import android.os.Build;
import android.speech.tts.TextToSpeech;
import android.text.TextUtils;

import java.util.Locale;

/**
 * @author Tank
 * @date 2020/9/8 16:13
 * @des
 * @updateAuthor
 * @updateDes
 */
public class TTSSpeechManager implements TextToSpeech.OnInitListener {

    private TextToSpeech mTextToSpeech;

    private TTSSpeechManager() {
    }

    protected static class TTSSpeechManagerHolder {
        static TTSSpeechManager ttsSpeechManager = new TTSSpeechManager();
    }

    public static TTSSpeechManager getInstance() {
        return TTSSpeechManagerHolder.ttsSpeechManager;
    }

    public int init(Context context, TTSSpeechInitCallback ttsSpeechInitCallback) {
        this.mTTSSpeechInitCallback = ttsSpeechInitCallback;
        Context applicationContext = context.getApplicationContext();
        free();
        this.mTextToSpeech = new TextToSpeech(applicationContext, this);
        return 0;
    }

    private TTSSpeechInitCallback mTTSSpeechInitCallback;

    public int setPitch(float value) {
        if (this.mTextToSpeech == null) {
            return -1;
        }
        //设置音调，值越大声音越尖（女生），值越小则变成男声,1.0是常规
        this.mTextToSpeech.setPitch(value);
        return 0;
    }

    public int setSpeechRate(float value) {
        if (this.mTextToSpeech == null) {
            return -1;
        }
        // 设置语速
        this.mTextToSpeech.setSpeechRate(value);
        return 0;
    }

    public int speak(String words, TTSSpeechCallback ttsSpeechCallback) {
        if (TextUtils.isEmpty(words) || ttsSpeechCallback == null) {
            return -1;
        }
        if (this.mTextToSpeech == null) {
            return -1;
        }
        //stop();
        //播放语音
        this.mTextToSpeech.setOnUtteranceProgressListener(null);
        this.mTextToSpeech.setOnUtteranceProgressListener(ttsSpeechCallback);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            this.mTextToSpeech.speak(words, TextToSpeech.QUEUE_ADD, null, ttsSpeechCallback.getUtteranceId());
        }
        return 0;
    }

    public int speak(String words) {
        if (TextUtils.isEmpty(words)) {
            return -1;
        }
        if (this.mTextToSpeech == null) {
            return -1;
        }
        //stop();
        //播放语音
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            try {
                this.mTextToSpeech.speak(words, TextToSpeech.QUEUE_ADD, null, null);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return 0;
    }

    public int stop() {
        if (this.mTextToSpeech == null) {
            return -1;
        }
        this.mTextToSpeech.setOnUtteranceProgressListener(null);
        //this.mTextToSpeech.stop();
        return 0;
    }

    public int free() {
        if (this.mTextToSpeech == null) {
            return -1;
        }
        // 设置语速
        this.mTextToSpeech.stop();
        this.mTextToSpeech.shutdown();
        this.mTextToSpeech = null;
        return 0;
    }

    @Override
    public void onInit(int status) {
        //判断tts回调是否成功
        if (this.mTTSSpeechInitCallback != null) {
            this.mTTSSpeechInitCallback.onTTSSpeechInit(status);
        }
        if (this.mTextToSpeech == null) {
            return;
        }
        if (status == TextToSpeech.SUCCESS) {
            int result1 = mTextToSpeech.setLanguage(Locale.US);
            if (result1 == TextToSpeech.LANG_MISSING_DATA || result1 == TextToSpeech.LANG_NOT_SUPPORTED) {
                if (this.mTTSSpeechInitCallback != null) {
                    this.mTTSSpeechInitCallback.onTTSSpeechConfig(result1);
                }
                return;
            }

            int result2 = this.mTextToSpeech.setLanguage(Locale.CHINESE);
            if (result2 == TextToSpeech.LANG_MISSING_DATA || result2 == TextToSpeech.LANG_NOT_SUPPORTED) {
                if (this.mTTSSpeechInitCallback != null) {
                    this.mTTSSpeechInitCallback.onTTSSpeechConfig(result2);
                }
                return;
            }
            if ( this.mTTSSpeechInitCallback!=null){
                this.mTTSSpeechInitCallback.onTTSSpeechConfig(TextToSpeech.SUCCESS);
            }
        }
    }
}
