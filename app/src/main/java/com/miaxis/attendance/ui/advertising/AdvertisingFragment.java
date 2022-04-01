package com.miaxis.attendance.ui.advertising;

import android.os.Bundle;
import android.os.Handler;

import com.miaxis.attendance.R;
import com.miaxis.attendance.databinding.FragmentAdvertisingBinding;
import com.miaxis.attendance.ui.home.HomeFragment;
import com.miaxis.attendance.ui.mic.MicFragment;
import com.miaxis.common.activity.BaseBindingFragment;

import java.util.ArrayList;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.ViewModelProvider;
import timber.log.Timber;

public class AdvertisingFragment extends BaseBindingFragment<FragmentAdvertisingBinding> implements AdvertisingClickListener {

    private AdvertisingViewModel mViewModel;
    private Handler mHandler = new Handler();

    public static AdvertisingFragment newInstance() {
        return new AdvertisingFragment();
    }

    @Override
    protected int initLayout() {
        return R.layout.fragment_advertising;
    }

    @Override
    protected void initView(@NonNull FragmentAdvertisingBinding binding, @Nullable Bundle savedInstanceState) {
        replaceChild(R.id.fl_mic, MicFragment.newInstance());

        mViewModel = new ViewModelProvider(this).get(AdvertisingViewModel.class);
        mViewModel.isIdle.observe(this, aBoolean -> {
            Timber.e("AdvertisingFragment  %s",aBoolean);
            if (aBoolean == null || !aBoolean) {
                onGoHome();
            }
        });
        AdvertisingAdapter advertisingAdapter = new AdvertisingAdapter(this);
        binding.vpAdvertising.setAdapter(advertisingAdapter);

        List<Advertising> objects = new ArrayList<>();
        objects.add(new Advertising(R.drawable.advertising_1, R.drawable.logo_2, new Advertising.MxRect(50, 50, 316, 107)));
        objects.add(new Advertising(R.drawable.advertising_2, R.drawable.logo_2, new Advertising.MxRect(50, 50, 316, 107)));
        objects.add(new Advertising(R.drawable.advertising_3, R.drawable.logo_2, new Advertising.MxRect(50, 50, 316, 107)));
        objects.add(new Advertising(R.drawable.advertising_4, R.drawable.logo_2, new Advertising.MxRect(50, 50, 316, 107)));
        objects.add(new Advertising(R.drawable.advertising_5, R.drawable.logo_2, new Advertising.MxRect(50, 50, 316, 107)));
        objects.add(new Advertising(R.drawable.advertising_6, R.drawable.logo_2, new Advertising.MxRect(50, 50, 316, 107)));
        advertisingAdapter.addAll(objects);
    }

    @Override
    public void onAdvertisingClick(Advertising advertising) {
        mViewModel.isIdle.setValue(false);
    }

    private void onGoHome() {
        replaceParent(R.id.container, HomeFragment.newInstance());
    }

    @Override
    public void onResume() {
        super.onResume();
        startLoop();
    }

    private void startLoop() {
        this.mHandler.postDelayed(() -> {
            binding.vpAdvertising.setCurrentItem(binding.vpAdvertising.getCurrentItem() + 1);
            startLoop();
        }, 1000 * 8);
    }

    private void stopLoop() {
        this.mHandler.removeCallbacksAndMessages(null);
    }

    @Override
    public void onPause() {
        super.onPause();
        stopLoop();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        stopLoop();
    }
}