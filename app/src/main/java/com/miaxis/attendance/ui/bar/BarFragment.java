package com.miaxis.attendance.ui.bar;

import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.miaxis.attendance.BuildConfig;
import com.miaxis.attendance.MainViewModel;
import com.miaxis.attendance.R;
import com.miaxis.attendance.config.AppConfig;
import com.miaxis.attendance.data.bean.AttendanceBean;
import com.miaxis.attendance.databinding.FragmentBarBinding;
import com.miaxis.attendance.tts.TTSSpeechManager;
import com.miaxis.attendance.ui.bar.widget.ClickableLayout;
import com.miaxis.attendance.ui.manager.ManagerFragment;
import com.miaxis.attendance.ui.net.NetFragment;
import com.miaxis.common.activity.BaseBindingFragment;
import com.miaxis.common.response.ZZResponse;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.ViewModelProvider;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileFilter;
import java.io.FileReader;
import java.io.IOException;
import java.sql.Time;
import java.util.ArrayList;
import java.util.List;
import java.util.Observable;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.regex.Pattern;

import io.reactivex.disposables.Disposable;

public class BarFragment extends BaseBindingFragment<FragmentBarBinding> {

    private static final String TAG = "BarFragment";
    private MainViewModel mMainViewModel;
    private final Handler mHandler = new Handler();

    public static BarFragment newInstance() {
        return new BarFragment();
    }

    @Override
    protected int initLayout() {
        return R.layout.fragment_bar;
    }

    @SuppressLint("SetTextI18n")
    @Override
    protected void initView(@NonNull FragmentBarBinding binding, @Nullable Bundle savedInstanceState) {

        BarViewModel viewModel = new ViewModelProvider(this).get(BarViewModel.class);
        replaceChild(R.id.fl_ip, NetFragment.newInstance());
        //viewModel.UserCounts.observe(this, integer ->
        // binding.tvUserCounts.setText("人数：" + (integer == null ? "0 " : ("" + integer))));
        //viewModel.UserCounts.setValue(PersonModel.allCounts());
        mMainViewModel = new ViewModelProvider(getActivity()).get(MainViewModel.class);
        mMainViewModel.startService.setValue(true);
        mMainViewModel.httpServerStatus.observe(this, integer -> {
            switch (integer) {
                case 1:
                    binding.ivCloud.setImageResource(R.drawable.ic_baseline_cloud_done);
                    break;
                case -1:
                    binding.ivCloud.setImageResource(R.drawable.ic_baseline_cloud_error);
                    break;
                default:
                    binding.ivCloud.setImageResource(R.drawable.ic_baseline_cloud_off);
            }
        });


        //binding.ivCloud.setImageResource(R.drawable.ic_baseline_cloud_done);
        binding.ivCloud2.setImageResource(R.drawable.ic_power_enable);
        //binding.ivCloud3.setImageResource(R.drawable.ic_baseline_cloud_done);

        mMainViewModel.mAttendance.observe(this, attendance -> {
            if (attendance == null) {
                return;
            }
            if (ZZResponse.isSuccess(attendance)) {
                mMainViewModel.openDoor();
                AttendanceBean attendanceData = attendance.getData();
                if (viewModel.isNewUser(attendance.getData())) {
                    if (attendanceData.Mode==1){
                        Glide.with(binding.ivImage).load(attendanceData.CutImage).into(binding.ivImage);
                    }else {
                        Glide.with(binding.ivImage).load(R.drawable.ic_fingerprint).into(binding.ivImage);
                    }
                    binding.tvName.setText(String.valueOf(attendanceData.UserName));
                    if(attendanceData.tempType==0){
                        binding.tvTempFloat.setText("相似度："+viewModel.format(attendanceData.tempFloat));
                    }
                    TTSSpeechManager.getInstance().speak(AppConfig.WelcomeWords);
                    binding.tvStatus.setVisibility(View.VISIBLE);
                    //getView().setBackgroundColor(0xFF32CD32);
                    mHandler.removeCallbacksAndMessages(null);
                    mHandler.postDelayed(() -> {
                        Glide.with(binding.ivImage).load(R.drawable.logo).centerCrop().into(binding.ivImage);
                        binding.tvName.setText("");
                        binding.tvTempFloat.setText("");
                        binding.tvStatus.setVisibility(View.GONE);
                        //getView().setBackgroundColor(getResources().getColor(R.color.blue));
                        //getView().setBackgroundResource(R.drawable.bg_bar);
                        viewModel.setNewUserReset();
                    }, AppConfig.CloseDoorDelay);
                }
            } else {
                if (attendance.getCode() == -81 || attendance.getCode() == -82 || attendance.getCode() == -203){
                    Toast.makeText(getContext(), "" + attendance.getMsg(), Toast.LENGTH_SHORT).show();
                }
            }
        });
        if (BuildConfig.IS_DEBUG){
            binding.clClick.setOnClickListener(new ClickableLayout.OnComboClickListener() {
                @Override
                protected void onComboClick(View v) {
                    replaceParent(R.id.container, ManagerFragment.newInstance());
                }
            });
        }

        mMainViewModel.mTemp.observe(this,tempBean -> {
            binding.tvCpu.setText("CPU温度："+tempBean.getCpu()/10+"℃");
            binding.tvBattery.setText("电池温度："+tempBean.getBattery()/1000+"℃");
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        mHandler.removeCallbacksAndMessages(null);
        mMainViewModel.httpServerStatus.removeObservers(this);
        mMainViewModel.mAttendance.removeObservers(this);
        mMainViewModel.mAttendance.setValue(null);
    }

    //    static class NetworkChangeReceiver extends BroadcastReceiver {
    //        @Override
    //        public void onReceive(Context context, Intent intent) {
    //            //Toast.makeText(context, "网络状态改变", Toast.LENGTH_SHORT).show();
    //        }
    //    }
}