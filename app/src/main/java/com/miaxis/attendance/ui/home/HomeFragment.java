package com.miaxis.attendance.ui.home;

import android.os.Bundle;

import com.miaxis.attendance.App;
import com.miaxis.attendance.MainViewModel;
import com.miaxis.attendance.R;
import com.miaxis.attendance.databinding.FragmentMainBinding;
import com.miaxis.attendance.ui.bar.BarFragment;
import com.miaxis.attendance.ui.finger.FingerFragment;
import com.miaxis.attendance.ui.preview.PreviewFragment;
import com.miaxis.common.activity.BaseBindingFragment;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.ViewModelProvider;

public class HomeFragment extends BaseBindingFragment<FragmentMainBinding> {

    private MainViewModel mMainViewModel;

    public static HomeFragment newInstance() {
        return new HomeFragment();
    }

    @Override
    protected int initLayout() {
        return R.layout.fragment_main;
    }

    @Override
    protected void initView(@NonNull FragmentMainBinding binding, @Nullable Bundle savedInstanceState) {
        HomeViewModel viewModel = new ViewModelProvider(this).get(HomeViewModel.class);
        //mainActivityViewModel = new ViewModelProvider(getActivity()).get(MainActivityViewModel.class);
        replaceChild(R.id.fl_preview, PreviewFragment.newInstance());
        replaceChild(R.id.fl_content, BarFragment.newInstance());
        replaceChild(R.id.fl_finger, FingerFragment.newInstance());
        App.getInstance().startUploadAttendance();
    }

    @Override
    public void onResume() {
        super.onResume();
    }

}