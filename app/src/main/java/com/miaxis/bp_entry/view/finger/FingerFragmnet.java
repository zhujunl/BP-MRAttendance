package com.miaxis.bp_entry.view.finger;

import android.content.Context;
import android.graphics.Bitmap;


import com.miaxis.bp_entry.R;
import com.miaxis.bp_entry.databinding.FragmentFingerBinding;
import com.miaxis.bp_entry.view.base.BaseViewModelFragment;
import com.miaxis.bp_entry.view.base.OnFragmentInteractionListener;

import androidx.lifecycle.ViewModelProvider;

/**
 * @author ZJL
 * @date 2022/3/24 9:30
 * @des
 * @updateAuthor
 * @updateDes
 */
public class FingerFragmnet extends BaseViewModelFragment<FragmentFingerBinding,FingerViewModel> {

    private static FingerFragmnet instance;
    private OnFragmentInteractionListener mListener;
    private final String TAG="FingerFragment";
    private Bitmap mBitmap;

    public static FingerFragmnet getInstance(Bitmap mBitmap){
        if(instance==null){
            instance=new FingerFragmnet();
        }
        instance.setBitmap(mBitmap);
        return instance;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if(context instanceof OnFragmentInteractionListener){
            mListener=(OnFragmentInteractionListener) context;
        }else {
            throw new RuntimeException(context.toString() + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    protected int setContentView() {
        return R.layout.fragment_finger;
    }

    @Override
    protected FingerViewModel initViewModel() {
        return new ViewModelProvider(this,getViewModelProviderFactory()).get(FingerViewModel.class);
    }

    @Override
    public int initVariableId() {
        return com.miaxis.bp_entry.BR.viewmodel;
    }

    @Override
    protected void initView() {
        binding.ivHeader.setImageBitmap(mBitmap);
        binding.ivBack.setOnClickListener(v-> onBackPressed());
        binding.tvOver.setOnClickListener(v -> {});
    }

    @Override
    public void onBackPressed() {
        mListener.backToStack(null);
    }

    public void setBitmap(Bitmap bitmap) {
        mBitmap = bitmap;
    }
}
