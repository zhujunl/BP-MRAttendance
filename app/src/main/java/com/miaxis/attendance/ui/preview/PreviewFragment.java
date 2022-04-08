package com.miaxis.attendance.ui.preview;

import android.graphics.SurfaceTexture;
import android.os.Bundle;
import android.os.Handler;
import android.view.SurfaceHolder;
import android.view.TextureView;
import android.view.View;

import com.miaxis.attendance.MainViewModel;
import com.miaxis.attendance.R;
import com.miaxis.attendance.config.AppConfig;
import com.miaxis.attendance.databinding.FragmentPreviewBinding;
import com.miaxis.attendance.widget.CameraTextureView;
import com.miaxis.common.activity.BaseBindingFragment;
import com.miaxis.common.camera.CameraConfig;
import com.miaxis.common.camera.CameraHelper;
import com.miaxis.common.camera.MXSurfaceCallback;
import com.miaxis.common.response.ZZResponse;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;

public class PreviewFragment extends BaseBindingFragment<FragmentPreviewBinding> implements TextureView.SurfaceTextureListener {

    private static final String TAG = "PreviewFragment";
    private PreviewViewModel mViewModel;
    private final Handler mHandler = new Handler();

    public static PreviewFragment newInstance() {
        return new PreviewFragment();
    }

    @Override
    protected int initLayout() {
        return R.layout.fragment_preview;
    }

    @Override
    protected void initView(@NonNull FragmentPreviewBinding binding, @Nullable Bundle savedInstanceState) {
        mViewModel = new ViewModelProvider(this).get(PreviewViewModel.class);
        mViewModel.Fill_light(true);
        MainViewModel mainViewModel = new ViewModelProvider(getActivity()).get(MainViewModel.class);
        mViewModel.StartCountdown.observe(this, mainViewModel::timeOutReset);

        mViewModel.AttendanceBean.observe(this, attendanceBean -> mainViewModel.mAttendance.setValue(attendanceBean));

        mainViewModel.mAttendance.observe(this,attendance->{
            if (attendance == null) {
                return;
            }
            if (ZZResponse.isSuccess(attendance)){
                if (mViewModel.isNewUser(attendance.getData())) {
                    binding.staffCode.setText("工号："+String.valueOf(attendance.getData().UserName));
                    if(attendance.getData().tempType==0){
                        binding.staffSimilarity.setText("相似度："+mViewModel.format(attendance.getData().tempFloat));
                    }
                    binding.staffStatus.setVisibility(View.VISIBLE);
                    mHandler.removeCallbacksAndMessages(null);
                    mHandler.postDelayed(() -> {
                        binding.staffCode.setText("");
                        binding.staffSimilarity.setText("");
                        binding.staffStatus.setVisibility(View.GONE);
                        mViewModel.setNewUserReset();
                    }, AppConfig.CloseDoorDelay);
                }
            }
        });

        binding.ttvPreview.setSurfaceTextureListener(this);
        binding.ttvPreview.setRotationY(CameraConfig.Camera_RGB.mirror ? 180 : 0); // 镜面对称
        binding.ttvPreview.setRawPreviewSize(new CameraTextureView.Size(CameraConfig.Camera_RGB.height, CameraConfig.Camera_RGB.width));
        Observer<ZZResponse<?>> observer = response -> {
            if (!ZZResponse.isSuccess(response)) {
                new AlertDialog.Builder(getContext()).setTitle("错误")
                        .setMessage("摄像头打开失败，是否重试？\n" + response.getCode() + "," + response.getMsg())
                        .setPositiveButton("重试", (dialog, which) -> {
                            dialog.dismiss();
                            showCameraPreview(binding.ttvPreview.getSurfaceTexture());
                        }).setNegativeButton("退出", (dialog, which) -> {
                    dialog.dismiss();
                    finish();
                }).create().show();
            }
        };
        mViewModel.IsCameraEnable_Rgb.observe(this, observer);
        mViewModel.IsCameraEnable_Nir.observe(this, observer);

        mViewModel.faceRect.observe(this, rectF -> binding.frvRect.setRect(rectF, false));
        binding.svPreviewNir.getHolder().addCallback(new MXSurfaceCallback() {
            @Override
            public void surfaceCreated(@NonNull SurfaceHolder holder) {
                mViewModel.SurfaceHolder_Nir.set(holder);
            }
        });
    }

    @Override
    public void onSurfaceTextureAvailable(SurfaceTexture surface, int width, int height) {
        showCameraPreview(surface);
    }

    private void showCameraPreview(SurfaceTexture surface) {
        mViewModel.showRgbCameraPreview(surface);
    }

    @Override
    public void onSurfaceTextureSizeChanged(SurfaceTexture surface, int width, int height) {
    }

    @Override
    public boolean onSurfaceTextureDestroyed(SurfaceTexture surface) {
        CameraHelper.getInstance().stop();
        return false;
    }

    @Override
    public void onSurfaceTextureUpdated(SurfaceTexture surface) {
    }

    @Override
    public void onResume() {
        super.onResume();
        //        mViewModel.resume();
    }

    @Override
    public void onPause() {
        super.onPause();
        //        mViewModel.pause();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        mHandler.removeCallbacksAndMessages(null);
        mViewModel.Fill_light(false);
        mViewModel.faceRect.removeObservers(this);
        mViewModel.AttendanceBean.removeObservers(this);
        mViewModel.StartCountdown.removeObservers(this);
        mViewModel.IsCameraEnable_Rgb.removeObservers(this);
        mViewModel.IsCameraEnable_Nir.removeObservers(this);
        mViewModel.destroy();
    }
}

