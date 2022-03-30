package com.miaxis.bp_entry.view.register;

import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.FrameLayout;

import com.miaxis.bp_entry.R;
import com.miaxis.bp_entry.databinding.FragmentFaceRegisterBinding;
import com.miaxis.bp_entry.manager.CameraManager;
import com.miaxis.bp_entry.view.base.BaseViewModelFragment;
import com.miaxis.bp_entry.view.custom.RoundBorderView;
import com.miaxis.bp_entry.view.custom.RoundFrameLayout;

import androidx.lifecycle.ViewModelProvider;

public class FaceRegisterFragment extends BaseViewModelFragment<FragmentFaceRegisterBinding, FaceRegisterViewModel> {

    private RoundBorderView roundBorderView;
    private RoundFrameLayout roundFrameLayout;

    public static FaceRegisterFragment newInstance() {
        return new FaceRegisterFragment();
    }

    public FaceRegisterFragment() {
        // Required empty public constructor
    }

    @Override
    protected int setContentView() {
        return R.layout.fragment_face_register;
    }

    @Override
    protected FaceRegisterViewModel initViewModel() {
        return new ViewModelProvider(this, getViewModelProviderFactory()).get(FaceRegisterViewModel.class);
    }

    @Override
    public int initVariableId() {
        return com.miaxis.bp_entry.BR.viewmodel;
    }

    @Override
    protected void initData() {

    }

    @Override
    protected void initView() {
        binding.ivBack.setOnClickListener(v -> onBackPressed());
        binding.rtvCamera.getViewTreeObserver().addOnGlobalLayoutListener(globalListener);
        binding.ivTakePhoto.setOnClickListener(v -> {
            binding.ivTakePhoto.setVisibility(View.INVISIBLE);
            viewModel.takePicture();
        });
        binding.ivRetry.setOnClickListener(v -> {
            binding.ivTakePhoto.setVisibility(View.VISIBLE);
            viewModel.retry();
        });
        binding.ivConfirm.setOnClickListener(v -> viewModel.confirm());
        viewModel.confirmFlag.observe(this, aBoolean -> mListener.backToStack(null));
    }

    @Override
    public void onBackPressed() {
        mListener.backToStack(null);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        CameraManager.getInstance().closeFrontCamera();
    }

    private ViewTreeObserver.OnGlobalLayoutListener globalListener = new ViewTreeObserver.OnGlobalLayoutListener() {
        @Override
        public void onGlobalLayout() {
            binding.rtvCamera.getViewTreeObserver().removeOnGlobalLayoutListener(globalListener);
            ViewGroup.LayoutParams layoutParams = binding.rtvCamera.getLayoutParams();
            layoutParams.width = binding.flCamera.getWidth();
            layoutParams.height = binding.flCamera.getHeight();
            binding.rtvCamera.setLayoutParams(layoutParams);
            binding.rtvCamera.turnRound();
            CameraManager.getInstance().resetRetryTime();
            CameraManager.getInstance().openFrontCamera(binding.rtvCamera, cameraListener);
        }
    };

    private CameraManager.OnCameraOpenListener cameraListener = previewSize -> {
        FrameLayout.LayoutParams textureViewLayoutParams = (FrameLayout.LayoutParams) binding.rtvCamera.getLayoutParams();
        int newHeight = textureViewLayoutParams.width * previewSize.width / previewSize.height;
        int newWidth = textureViewLayoutParams.width;

        roundFrameLayout = new RoundFrameLayout(getContext());
        int sideLength = Math.min(newWidth, newHeight);
        FrameLayout.LayoutParams layoutParams = new FrameLayout.LayoutParams(sideLength, sideLength);
        roundFrameLayout.setLayoutParams(layoutParams);
        FrameLayout parentView = (FrameLayout) binding.rtvCamera.getParent();
        parentView.removeView(binding.rtvCamera);
        parentView.addView(roundFrameLayout);

        roundFrameLayout.addView(binding.rtvCamera);
        FrameLayout.LayoutParams newTextureViewLayoutParams = new FrameLayout.LayoutParams(newWidth, newHeight);
        newTextureViewLayoutParams.topMargin = -(newHeight - newWidth) / 2;
        binding.rtvCamera.setLayoutParams(newTextureViewLayoutParams);

        View siblingView = roundFrameLayout != null ? roundFrameLayout : binding.rtvCamera;
        roundBorderView = new RoundBorderView(getContext());
        ((FrameLayout) siblingView.getParent()).addView(roundBorderView, siblingView.getLayoutParams());

//        new Handler(Looper.getMainLooper()).post(() -> {
//            roundFrameLayout.setRadius(Math.min(roundFrameLayout.getWidth(), roundFrameLayout.getHeight()) / 2);
//            roundFrameLayout.turnRound();
//            roundBorderView.setRadius(Math.min(roundBorderView.getWidth(), roundBorderView.getHeight()) / 2);
//            roundBorderView.turnRound();
//        });

    };

}
