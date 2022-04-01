package com.miaxis.attendance.ui.net;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.text.TextUtils;

import com.bumptech.glide.Glide;
import com.miaxis.attendance.R;
import com.miaxis.attendance.databinding.FragmentNetBinding;
import com.miaxis.common.activity.BaseBindingFragment;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.ViewModelProvider;

public class NetFragment extends BaseBindingFragment<FragmentNetBinding> {

    private static final String TAG = "NetFragment";
    private BroadcastReceiver networkChangeReceiver;

    public static NetFragment newInstance() {
        return new NetFragment();
    }

    @Override
    protected int initLayout() {
        return R.layout.fragment_net;
    }

    @Override
    protected void initView(@NonNull FragmentNetBinding binding, @Nullable Bundle savedInstanceState) {
        NetViewModel viewModel = new ViewModelProvider(this).get(NetViewModel.class);

        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction("android.net.conn.CONNECTIVITY_CHANGE");
        getActivity().registerReceiver(this.networkChangeReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                viewModel.flushIpAddress();
            }
        }, intentFilter);
        viewModel.IpAddress.observe(this, s -> {
            //if (!TextUtils.isEmpty(s)) {
            //    binding.tvIp.setText("本机IP：" + s);
            //} else {
            //    binding.tvIp.setText("无网络连接");
            //}
            Glide.with(binding.ivIp)
                    .load(TextUtils.isEmpty(s) ? R.drawable.ic_ethernet_unable : R.drawable.ic_ethernet_enable)
                    .into(binding.ivIp);
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (networkChangeReceiver != null) {
            getActivity().unregisterReceiver(networkChangeReceiver);
        }
    }

}