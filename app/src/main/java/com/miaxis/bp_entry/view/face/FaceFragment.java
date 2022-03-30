package com.miaxis.bp_entry.view.face;

import android.app.AlertDialog;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;

import com.miaxis.bp_entry.R;
import com.miaxis.bp_entry.databinding.FragmentFaceBinding;
import com.miaxis.bp_entry.manager.CameraManager;
import com.miaxis.bp_entry.manager.FaceManager;
import com.miaxis.bp_entry.view.base.BaseViewModelFragment;
import com.miaxis.bp_entry.view.base.OnFragmentInteractionListener;
import com.miaxis.bp_entry.view.custom.RoundBorderView;
import com.miaxis.bp_entry.view.custom.RoundFrameLayout;

import androidx.lifecycle.ViewModelProvider;

/**
 * @author ZJL
 * @date 2022/3/24 9:30
 * @des
 * @updateAuthor
 * @updateDes
 */
public class FaceFragment extends BaseViewModelFragment<FragmentFaceBinding,FaceViewModel> {

    private static FaceFragment instance;
    private OnFragmentInteractionListener mListener;
    private final String TAG="FaceFragment";
    private RoundFrameLayout roundFrameLayout;
    private RoundBorderView roundBorderView;
    private Bitmap mBitmap=null;

    public static FaceFragment getInstance(){
        if (instance==null){
            instance=new FaceFragment();
        }
        return instance;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnFragmentInteractionListener){
            mListener=(OnFragmentInteractionListener) context;
        }else {
            throw new RuntimeException(context.toString() + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    protected int setContentView() {
        return R.layout.fragment_face;
    }

    @Override
    protected FaceViewModel initViewModel() {
        return new ViewModelProvider(this,getViewModelProviderFactory()).get(FaceViewModel.class);
    }

    @Override
    public int initVariableId() {
        return com.miaxis.bp_entry.BR.viewmodel;
    }

    @Override
    protected void initView() {
        viewModel.imgres.observe(this,bitmap -> binding.ivHeader.setImageBitmap(bitmap));
        binding.ivBack.setOnClickListener(v-> onBackPressed());
        binding.tvSwitch.setOnClickListener(v-> {
//            viewModel.CheckFace();
//            mListener.replaceFragment(FingerFragmnet.getInstance(mBitmap))
        });
        binding.rtvCamera.getViewTreeObserver().addOnGlobalLayoutListener(globalListener);
        binding.getPicture.setOnClickListener(v -> {
            try {
                CameraManager.getInstance().takeBackPicture((data, camera) -> {
                    CameraManager.getInstance().stopPreview();
                    ShowPic(data);
                });
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }

    @Override
    public void onBackPressed() {
        mListener.backToStack(null);
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
            CameraManager.getInstance().openBackCamera(binding.rtvCamera, cameraListener);
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

        new Handler(Looper.getMainLooper()).post(() -> {
            roundFrameLayout.setRadius(Math.min(roundFrameLayout.getWidth(), roundFrameLayout.getHeight()) / 2);
            roundFrameLayout.turnRound();
            roundBorderView.setRadius(Math.min(roundBorderView.getWidth(), roundBorderView.getHeight()) / 2);
            roundBorderView.turnRound();
        });
    };

    private void ShowPic(byte[] data){
        Bitmap facePicture;
        AlertDialog.Builder builder=new AlertDialog.Builder(getActivity());
        View view= LayoutInflater.from(getActivity()).inflate(R.layout.dialog_picture,null);
        ImageView img=view.findViewById(R.id.dialog_pic);
        Button cancel=view.findViewById(R.id.pic_cancel);
        Button sure=view.findViewById(R.id.pic_sure);
        facePicture= FaceManager.getInstance().adjustPhotoRotation(BitmapFactory.decodeByteArray(data,0,data.length),90);
        img.setImageBitmap(facePicture);
        builder.setView(view);
        builder.setCancelable(false);
        AlertDialog alertDialog=builder.create();
        cancel.setOnClickListener(v -> {
            CameraManager.getInstance().startBackPreview();
            alertDialog.dismiss();
        });
        sure.setOnClickListener(v -> {
            mBitmap=facePicture;
            viewModel.imgres.setValue(facePicture);
            CameraManager.getInstance().startBackPreview();
            alertDialog.dismiss();});
        alertDialog.show();
    }


}
