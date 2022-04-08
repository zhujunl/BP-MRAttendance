package com.miaxis.attendance.ui.bar;

import android.annotation.SuppressLint;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.miaxis.attendance.App;
import com.miaxis.attendance.BuildConfig;
import com.miaxis.attendance.MainViewModel;
import com.miaxis.attendance.R;
import com.miaxis.attendance.config.AppConfig;
import com.miaxis.attendance.data.bean.AttendanceBean;
import com.miaxis.attendance.databinding.FragmentBarBinding;
import com.miaxis.attendance.ui.bar.widget.ClickableLayout;
import com.miaxis.attendance.ui.manager.ManagerFragment;
import com.miaxis.attendance.ui.net.NetFragment;
import com.miaxis.common.activity.BaseBindingFragment;
import com.miaxis.common.response.ZZResponse;
import com.miaxis.common.utils.HardWareUtils;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.ViewModelProvider;

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
        binding.title.setText(App.getInstance().getKv().decodeString("title"));
        binding.place.setText("场所ID:"+App.getInstance().getKv().decodeString("place"));
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

        binding.IPTxt.setText("本机IP:"+ HardWareUtils.getHostIP());
        //binding.ivCloud.setImageResource(R.drawable.ic_baseline_cloud_done);
        binding.ivCloud2.setImageResource(R.drawable.ic_power_enable);
        //binding.ivCloud3.setImageResource(R.drawable.ic_baseline_cloud_done);

        mMainViewModel.mAttendance.observe(this, attendance -> {
            if (attendance == null) {
                return;
            }
            if (ZZResponse.isSuccess(attendance)) {
                mMainViewModel.openDoor();//开门
                AttendanceBean attendanceData = attendance.getData();
                if (viewModel.isNewUser(attendance.getData())) {
                    if (attendanceData.Mode==1){
                        Glide.with(binding.ivImage).load(attendanceData.CutImage).into(binding.ivImage);
                    }else {
                        Glide.with(binding.ivImage).load(R.drawable.ic_fingerprint).into(binding.ivImage);
                    }

//                    TTSSpeechManager.getInstance().speak(AppConfig.WelcomeWords);//语音
                    mHandler.removeCallbacksAndMessages(null);
                    mHandler.postDelayed(() -> {
                        Glide.with(binding.ivImage).load(Color.alpha(0xFFFFFFFF)).centerCrop().into(binding.ivImage);
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