package com.miaxis.attendance;

import android.app.Activity;
import android.app.Application;
import android.os.Bundle;
import android.util.Log;

//import com.miaxis.attendance.api.HttpApi;
import com.miaxis.attendance.callback.ActivityCallbacks;
import com.miaxis.attendance.config.AppConfig;
import com.miaxis.attendance.data.AppDataBase;
import com.miaxis.attendance.data.model.FaceModel;
import com.miaxis.attendance.data.model.FingerModel;
import com.miaxis.attendance.device.MR990Device;
import com.miaxis.attendance.task.UploadAttendance;
import com.miaxis.attendance.tts.TTSSpeechManager;
import com.miaxis.attendance.ui.finger.MR990FingerStrategy;
import com.miaxis.common.camera.CameraHelper;
import com.miaxis.common.utils.FileUtils;
import com.tencent.mmkv.MMKV;

import org.jetbrains.annotations.NotNull;
import org.zz.api.MXFaceAPI;
import org.zz.api.MXFaceIdAPI;
import org.zz.api.MXResult;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import timber.log.Timber;

/**
 * @author Tank
 * @date 2021/8/19 5:39 下午
 * @des
 * @updateAuthor
 * @updateDes
 */
public class App extends Application {

    private static final String TAG = "App";
    private static App context;
    public final ExecutorService threadExecutor = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());
    private final UploadAttendance mUploadAttendance = new UploadAttendance();
    private static MMKV kv;

    @Override
    public void onCreate() {
        super.onCreate();
        context = this;
        MMKV.initialize(this);
        //        if (BuildConfig.DEBUG) {
        Timber.plant(new Timber.DebugTree() {
            @Override
            protected void log(int priority, String tag, @NotNull String message, Throwable t) {
                super.log(priority, "Mx" + tag, message, t);
            }
        });
        //        }
        registerActivityLifecycleCallbacks(new ActivityCallbacks() {
            @Override
            public void onActivityCreated(@NonNull Activity activity, @Nullable Bundle savedInstanceState) {
                if (activity.getClass() == MainActivity.class) {
                    //MR990Device.getInstance().setUsbDebug(BuildConfig.IS_DEBUG);
                    //MR990Device.getInstance().setWifiDebug(BuildConfig.IS_DEBUG);
                    MR990Device.getInstance().CameraPower(true);
                    MR990Device.getInstance().FingerPower(true);
                    MR990Device.getInstance().EthernetPower(true);
                }
            }

            @Override
            public void onActivityStopped(@NonNull Activity activity) {
                super.onActivityStopped(activity);
                if (activity.getClass() == MainActivity.class) {
                    activity.finish();
                }
            }

            @Override
            public void onActivityDestroyed(@NonNull Activity activity) {
                if (activity.getClass() == MainActivity.class) {
                    //MR990Device.getInstance().setUsbDebug(BuildConfig.IS_DEBUG);
                    //MR990Device.getInstance().setWifiDebug(BuildConfig.IS_DEBUG);
                    MR990FingerStrategy.getInstance().release();
                    MR990Device.getInstance().CameraPower(false);
                    MR990Device.getInstance().FingerPower(false);
                    MR990Device.getInstance().LedPower(false);
                    TTSSpeechManager.getInstance().free();
                    CameraHelper.getInstance().free();
                    System.exit(0);
                }
            }
        });
    }

    public static App getInstance() {
        return context;
    }

    public MXResult<?> init() {
        boolean initFile = FileUtils.initFile(AppConfig.Path_DataBase);
        if (!initFile) {
            return MXResult.CreateFail(-2, "初始化文件错误");
        }
        AppDataBase.getInstance().init(AppConfig.Path_DataBase, this);
        FaceModel.init();
        FingerModel.init();
        kv=MMKV.defaultMMKV();
//        HttpApi.init(this);
        TTSSpeechManager.getInstance().init(this, null);
        MR990FingerStrategy.getInstance().init();
        int ret=MXFaceAPI.getInstance().mxInitAlg(this,null,null);
        Log.e("face_init:",""+ret);
        return MXFaceIdAPI.getInstance().mxInitAlg(this, null, null);
    }

    public void startUploadAttendance() {
        if (!this.mUploadAttendance.isRunning()) {
            this.threadExecutor.execute(this.mUploadAttendance);
        }
    }

    public MMKV getKv(){
        return kv;
    }
}


