package com.miaxis.attendance.ui.permission;

import android.os.Bundle;

import com.miaxis.attendance.App;
import com.miaxis.attendance.BuildConfig;
import com.miaxis.attendance.MainViewModel;
import com.miaxis.attendance.R;
import com.miaxis.attendance.databinding.FragmentPermissionBinding;
import com.miaxis.attendance.ui.home.HomeFragment;
import com.miaxis.attendance.ui.prepare.PrepareFragment;
import com.miaxis.common.activity.BaseBindingFragment;
import com.tbruyelle.rxpermissions3.RxPermissions;

import org.zz.api.MXResult;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.lifecycle.ViewModelProvider;

import io.reactivex.Observable;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;
import timber.log.Timber;

public class PermissionFragment extends BaseBindingFragment<FragmentPermissionBinding> {

    private static final String TAG = "PermissionFragment";

    public static PermissionFragment newInstance(String[] permissions) {
        return new PermissionFragment(permissions);
    }

    private final String[] permissions;

    private PermissionFragment(String[] permissions) {
        this.permissions = permissions;
        if (this.permissions == null || this.permissions.length == 0) {
            throw new IllegalArgumentException("permissions can not be null or empty");
        }
    }

    @Override
    protected int initLayout() {
        return R.layout.fragment_permission;
    }

    @Override
    protected void initView(@NonNull FragmentPermissionBinding binding, @Nullable Bundle savedInstanceState) {
        Disposable subscribe = new RxPermissions(this)
                .request(this.permissions)
                .subscribe(aBoolean -> {
                    if (aBoolean) {
                        MainViewModel mainViewModel = new ViewModelProvider(getActivity()).get(MainViewModel.class);
                        mainViewModel.deleteFile();
                        mainViewModel.StartThread();
                        io.reactivex.disposables.Disposable disposable = Observable.create((ObservableOnSubscribe<MXResult<?>>) emitter -> {
                            MXResult<?> initAlg = App.getInstance().init();
                            Timber.e("initAlg:" + initAlg);
                            emitter.onNext(initAlg);
                        }).subscribeOn(Schedulers.from(App.getInstance().threadExecutor))
                                .observeOn(AndroidSchedulers.mainThread())
                                .subscribe(result -> {
                                    if (MXResult.isSuccess(result)) {
                                        if (BuildConfig.SKIP_PREPARE) {
                                            replaceParent(R.id.container, HomeFragment.newInstance());
                                        } else {
                                            replaceParent(R.id.container, PrepareFragment.newInstance());
                                        }
                                    } else {
                                        new AlertDialog.Builder(getContext()).setMessage("初始化失败。").setPositiveButton("退出", (dialog, which) -> {
                                            dialog.dismiss();
                                            finish();
                                        }).create().show();
                                    }
                                }, throwable -> {
                                    new AlertDialog.Builder(getContext()).setMessage("初始化失败:" + throwable).setPositiveButton("退出", (dialog, which) -> {
                                        dialog.dismiss();
                                        finish();
                                    }).create().show();
                                });
                    } else {
                        new AlertDialog.Builder(getContext()).setMessage("请授权后使用。").setPositiveButton("退出", (dialog, which) -> {
                            dialog.dismiss();
                            finish();
                        }).create().show();
                    }
                });
    }

}