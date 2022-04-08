package com.miaxis.attendance.ui.prepare;

import android.os.Bundle;

import com.miaxis.attendance.App;
import com.miaxis.attendance.R;
import com.miaxis.attendance.databinding.FragmentPrepareBinding;
import com.miaxis.attendance.ui.home.HomeFragment;
import com.miaxis.common.activity.BaseBindingFragment;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.ViewModelProvider;

public class PrepareFragment extends BaseBindingFragment<FragmentPrepareBinding> {

    private static final String TAG = "PrepareFragment";

    public static PrepareFragment newInstance() {
        return new PrepareFragment();
    }

    private PrepareFragment() {
    }

    @Override
    protected int initLayout() {
        return R.layout.fragment_prepare;
    }

    @Override
    protected void initView(@NonNull FragmentPrepareBinding binding, @Nullable Bundle savedInstanceState) {
        PrepareViewModel viewModel = new ViewModelProvider(this).get(PrepareViewModel.class);
//        viewModel.progressMsg.observe(this, s -> {
//            binding.btnSkip.setVisibility(View.GONE);
//            binding.btnTry.setVisibility(View.GONE);
//            binding.tvProgress.setText("" + s);
//        });
//        replaceParent(R.id.container, HomeFragment.newInstance());
//        viewModel.result.observe(this, aBoolean -> {
//            if (aBoolean) {
//                replaceParent(R.id.container, HomeFragment.newInstance());
//            } else {
//                binding.btnSkip.setVisibility(View.VISIBLE);
//                binding.btnTry.setVisibility(View.VISIBLE);
//            }
//        });
//        binding.btnTry.setOnClickListener(v -> viewModel.init());
        binding.btnSkip.setOnClickListener(v -> {
            replaceParent(R.id.container, HomeFragment.newInstance());
        });

        binding.btnSure.setOnClickListener(v -> {
            App.getInstance().getKv().encode("title",binding.titleEdit.getText().toString().trim());
            App.getInstance().getKv().encode("place",binding.placeEdit.getText().toString().trim());
            App.getInstance().getKv().encode("IP",binding.ipEdit.getText().toString().trim());
            replaceParent(R.id.container, HomeFragment.newInstance());
        });
//        binding.tvMsg.setMovementMethod(ScrollingMovementMethod.getInstance());
//        viewModel.msg.observe(this, binding.tvMsg::setText);
//        viewModel.init();
    }

}