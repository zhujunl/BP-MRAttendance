package com.miaxis.attendance.ui.manager;

import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import com.miaxis.attendance.MainViewModel;
import com.miaxis.attendance.R;
import com.miaxis.attendance.databinding.FragmentManagerBinding;
import com.miaxis.common.activity.BaseBindingFragment;

import java.util.List;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

public class ManagerFragment extends BaseBindingFragment<FragmentManagerBinding> implements PageNotifyInterface {

    private static final String TAG = "ManagerFragment";
    ManagerViewModel viewModel;

    public static ManagerFragment newInstance() {
        return new ManagerFragment();
    }

    @Override
    protected int initLayout() {
        return R.layout.fragment_manager;
    }

    @Override
    protected void initView(@NonNull FragmentManagerBinding binding, @Nullable Bundle savedInstanceState) {
        viewModel = new ViewModelProvider(this).get(ManagerViewModel.class);
        viewModel.MxUserList.observe(this, new Observer<List<MxUser>>() {
            @Override
            public void onChanged(List<MxUser> mxUsers) {
                RecyclerView.Adapter<?> adapter = binding.rvList.getAdapter();
                if (adapter instanceof UserAdapter) {
                    ((UserAdapter) adapter).setMxUsers(mxUsers);
                }
            }
        });
        viewModel.PagerIndex.observe(this, new Observer<Integer>() {
            @Override
            public void onChanged(Integer integer) {
                Toast.makeText(getActivity(), "第" + integer + "页", Toast.LENGTH_SHORT).show();
            }
        });
        MainViewModel mainViewModel = new ViewModelProvider(getActivity()).get(MainViewModel.class);
        mainViewModel.isIdleDetectStop.setValue(true);
        binding.tvBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mainViewModel.isIdleDetectStop.setValue(false);
            }
        });

        binding.btnPrevious.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                viewModel.previous();
            }
        });
        binding.btnNext.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                viewModel.next();
            }
        });

        viewModel.NextEnable.observe(this, new Observer<Boolean>() {
            @Override
            public void onChanged(Boolean aBoolean) {
                binding.btnNext.setEnabled(aBoolean != null && aBoolean);
            }
        });

        viewModel.PreviousEnable.observe(this, new Observer<Boolean>() {
            @Override
            public void onChanged(Boolean aBoolean) {
                binding.btnPrevious.setEnabled(aBoolean != null && aBoolean);
            }
        });

        binding.rvList.setLayoutManager(new LinearLayoutManager(getActivity(), LinearLayoutManager.VERTICAL, false));
        binding.rvList.setAdapter(new UserAdapter().bind(this));
        viewModel.flush();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
    }

    @Override
    public void onFlush() {
        viewModel.flush();
    }
}