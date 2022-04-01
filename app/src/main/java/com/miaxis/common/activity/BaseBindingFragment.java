package com.miaxis.common.activity;

import android.app.Activity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.IdRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.databinding.DataBindingUtil;
import androidx.databinding.ViewDataBinding;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;

/**
 * @author Tank
 * @date 2021/4/25 3:59 PM
 * @des
 * @updateAuthor
 * @updateDes
 */
public abstract class BaseBindingFragment<V extends ViewDataBinding> extends Fragment {

    protected V binding;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = DataBindingUtil.inflate(inflater, initLayout(), container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        binding.setLifecycleOwner(this);
        initView(binding, savedInstanceState);
        initData(binding, savedInstanceState);
    }

    protected abstract int initLayout();

    protected abstract void initView(@NonNull V binding, @Nullable Bundle savedInstanceState);

    protected void initData(@NonNull V binding, @Nullable Bundle savedInstanceState) {

    }

    public void onBackPressed() {
        FragmentActivity activity = getActivity();
        if (activity!=null){
            activity.onBackPressed();
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (binding != null) {
            binding.unbind();
        }
        Activity activity = getActivity();
        if (activity instanceof BaseBindingActivity) {
            BaseBindingActivity<?> baseBindingActivity = (BaseBindingActivity<?>) activity;
            baseBindingActivity.hideInputMethod();
        }

    }

    protected void finish() {
        if (getActivity() != null) {
            getActivity().finish();
        }
    }

    protected void showLoading() {
        FragmentActivity activity = getActivity();
        if (activity instanceof BaseBindingActivity) {
            ((BaseBindingActivity<?>) activity).showLoading();
        }
    }

    protected void showLoading(String title, String message) {
        FragmentActivity activity = getActivity();
        if (activity instanceof BaseBindingActivity) {
            ((BaseBindingActivity<?>) activity).showLoading(title, message);
        }
    }

    protected void dismissLoading() {
        FragmentActivity activity = getActivity();
        if (activity instanceof BaseBindingActivity) {
            ((BaseBindingActivity<?>) activity).dismissLoading();
        }
    }

    protected void replaceParent(@IdRes int containerViewId, Fragment fragment) {
        FragmentActivity activity = getActivity();
        if (activity instanceof BaseBindingFragmentActivity) {
            ((BaseBindingFragmentActivity<?>) activity).replace(containerViewId, fragment);
        }
    }

//    protected void showParent(@IdRes int containerViewId, Fragment fragment) {
//        FragmentActivity activity = getActivity();
//        if (activity instanceof BaseBindingFragmentActivity) {
//            ((BaseBindingFragmentActivity<?>) activity).show(containerViewId, fragment);
//        }
//    }

    protected void replaceChild(@IdRes int containerViewId, Fragment fragment) {
        getChildFragmentManager().beginTransaction().replace(containerViewId, fragment).commit();
    }

}