package com.miaxis.attendance.ui.mic;

import android.media.MediaRecorder;
import android.os.SystemClock;

import com.miaxis.attendance.App;
import com.miaxis.attendance.config.AppConfig;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
import timber.log.Timber;

public class MicViewModel extends ViewModel {

    private MediaRecorder mRecorder;
    private boolean isInterrupted = false;
    MutableLiveData<Boolean> isIdle = new MutableLiveData<>();

    public MicViewModel() {
    }

    public void start() {
        this.isInterrupted = false;
        App.getInstance().threadExecutor.execute(() -> {
            if (this.mRecorder != null) {
                this.mRecorder.release();
                this.mRecorder = null;
            }
            mRecorder = new MediaRecorder();
            mRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
            mRecorder.setOutputFormat(MediaRecorder.OutputFormat.DEFAULT);
            mRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.DEFAULT);
            File temp = new File(App.getInstance().getCacheDir(), "MediaUtil#micAvailTestFile");
            mRecorder.setOutputFile(temp.getAbsolutePath());
            try {
                mRecorder.prepare();
                mRecorder.start();
                while (!isInterrupted) {
                    SystemClock.sleep(100);
                    if (!isInterrupted) {
                        int maxAmplitude = mRecorder.getMaxAmplitude();
                        if (!isInterrupted) {
                            if (maxAmplitude >= AppConfig.MaxAmplitude) {
                                Timber.e("maxAmplitude:   postValue");
                                this.isInterrupted = true;
                                isIdle.postValue(false);
                                return;
                            }
                        }
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            stop();
        });
    }

    public void stop() {
        this.isInterrupted = true;
        if (this.mRecorder != null) {
            this.mRecorder.release();
            this.mRecorder = null;
        }
    }

    public List<Double> sub(List<Double> x) {
        if (x == null || x.isEmpty()) {
            return new ArrayList<>();
        }
        x.sort((o1, o2) -> (int) (o1 - o2));
        if (x.size() >= 3) {//去除最大值与最小值
            x = x.subList(1, x.size() - 2);
        }
        return x;
    }

    public double average(List<Double> x) {
        if (x == null || x.isEmpty()) {
            return 0;
        }
        int m = x.size();
        double sum = 0;
        for (double v : x) {//求和
            //onProcess("值：" + v);
            sum += v;
        }
        return sum / m;//求平均值
    }


}