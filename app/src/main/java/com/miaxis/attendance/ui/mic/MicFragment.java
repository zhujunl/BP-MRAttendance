package com.miaxis.attendance.ui.mic;

import android.os.Bundle;

import com.miaxis.attendance.MainViewModel;
import com.miaxis.attendance.R;
import com.miaxis.attendance.databinding.FragmentMicBinding;
import com.miaxis.common.activity.BaseBindingFragment;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import timber.log.Timber;

public class MicFragment extends BaseBindingFragment<FragmentMicBinding> {

    private MicViewModel mViewModel;

    public static MicFragment newInstance() {
        return new MicFragment();
    }

    @Override
    protected int initLayout() {
        return R.layout.fragment_mic;
    }

    @Override
    protected void initView(@NonNull FragmentMicBinding binding, @Nullable Bundle savedInstanceState) {
        this.mViewModel = new ViewModelProvider(this).get(MicViewModel.class);
        MainViewModel mainViewModel = new ViewModelProvider(getActivity()).get(MainViewModel.class);
        this.mViewModel.isIdle.observe(this, new Observer<Boolean>() {
            @Override
            public void onChanged(Boolean aBoolean) {
                Timber.e("MicFragment  %s",aBoolean);
                mainViewModel.isIdle.setValue(aBoolean);
                Timber.e("MicFragment-----");
            }
        });
        this.mViewModel.start();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        this.mViewModel.stop();
    }
}