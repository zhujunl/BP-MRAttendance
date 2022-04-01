package com.miaxis.attendance;

import android.Manifest;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.WindowManager;
import android.widget.Toast;

import com.miaxis.attendance.config.AppConfig;
import com.miaxis.attendance.databinding.ActivityMainBinding;
import com.miaxis.attendance.ui.advertising.AdvertisingFragment;
import com.miaxis.attendance.ui.home.HomeFragment;
import com.miaxis.attendance.ui.permission.PermissionFragment;
import com.miaxis.common.activity.BaseBindingFragmentActivity;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;

public class MainActivity extends BaseBindingFragmentActivity<ActivityMainBinding> {

    private MainViewModel mMainViewModel;
    private final Handler mHandler = new Handler();

    @Override
    protected int initLayout() {
        return R.layout.activity_main;
    }

    @Override
    protected void initView(@NonNull ActivityMainBinding binding, @Nullable Bundle savedInstanceState) {
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        mMainViewModel = new ViewModelProvider(this).get(MainViewModel.class);
        String[] permissions = {
                Manifest.permission.READ_EXTERNAL_STORAGE,
                Manifest.permission.WRITE_EXTERNAL_STORAGE,
                Manifest.permission.CAMERA,
                Manifest.permission.RECORD_AUDIO,
                Manifest.permission.READ_PHONE_STATE,
                Manifest.permission.ACCESS_WIFI_STATE};
        replace(R.id.container, PermissionFragment.newInstance(permissions));
        //TODO: 2021/8/31 待解耦
        //replace(R.id.container, AdvertisingFragment.newInstance());
        mMainViewModel.isIdle.observe(this, new Observer<Boolean>() {
            @Override
            public void onChanged(Boolean aBoolean) {
                boolean isIdleValue = aBoolean != null && aBoolean;
                //mHandler.removeCallbacksAndMessages(null);
                if (isIdleValue) {
//                    mHandler.postDelayed(new Runnable() {
//                        @Override
//                        public void run() {
                            replace(R.id.container, AdvertisingFragment.newInstance());
//                        }
//                    }, AppConfig.IdleTimeOut);
                } else {
                    replace(R.id.container, HomeFragment.newInstance());
                }
            }
        });

        mMainViewModel.isIdleDetectStop.observe(this, new Observer<Boolean>() {
            @Override
            public void onChanged(Boolean aBoolean) {
                if (aBoolean != null && aBoolean) {
                    mHandler.removeCallbacksAndMessages(null);
                }else {
                    replace(R.id.container, HomeFragment.newInstance());
                }
            }
        });

        mMainViewModel.startService.observe(this, aBoolean -> {
            if (aBoolean) {
                mMainViewModel.startHttpServer(AppConfig.Server_Port);
            }
        });
        mMainViewModel.httpServerStatus.observe(this, integer -> {
            switch (integer) {
                case 1:
                    Toast.makeText(MainActivity.this, "服务已启动", Toast.LENGTH_SHORT).show();
                    break;
                case -1:
                    Toast.makeText(MainActivity.this, "服务启动失败", Toast.LENGTH_SHORT).show();
                    break;
                default:
                    Toast.makeText(MainActivity.this, "服务已关闭", Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.e("MainActivity",":onDestroy");
        if (mMainViewModel != null) {
            mMainViewModel.stopHttpServer();
            mMainViewModel.destroy();
            mMainViewModel.StopThread();
        }
        if (mHandler!=null){
            mHandler.removeCallbacksAndMessages(null);
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.e("MainActivity",":onCreate");
        super.onCreate(savedInstanceState);
    }

    @Override
    protected void onPause() {
        Log.e("MainActivity",":onPause");
        super.onPause();
    }

    @Override
    protected void onStart() {
        Log.e("MainActivity",":onStart");
        super.onStart();
    }

    @Override
    protected void onStop() {
        Log.e("MainActivity",":onStop");
        super.onStop();
    }
}