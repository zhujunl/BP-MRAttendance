package com.miaxis.common.activity;

import com.miaxis.common.utils.ListUtils;

import org.jetbrains.annotations.NotNull;

import java.util.List;

import androidx.annotation.IdRes;
import androidx.databinding.ViewDataBinding;
import androidx.fragment.app.Fragment;

/**
 * @author Tank
 * @date 2021/4/25 3:59 PM
 * @des
 * @updateAuthor
 * @updateDes
 */
public abstract class BaseBindingFragmentActivity<V extends ViewDataBinding> extends BaseBindingActivity<V> {


    private static final String TAG = "BaseBindingFragmentActivity";

    protected void replace(@IdRes int containerViewId, @NotNull Fragment fragment) {
        List<Fragment> fragments = getSupportFragmentManager().getFragments();
        if (!ListUtils.isNullOrEmpty(fragments)) {
            for (Fragment fra : fragments) {
                if (fra.getClass() == fragment.getClass()) {
                    return;
                }
            }
        }
        getSupportFragmentManager().beginTransaction()
                .replace(containerViewId, fragment)
                .commit();
    }

    //    protected void show(@IdRes int containerViewId, Fragment fragment) {
    //        List<Fragment> fragments = getSupportFragmentManager().getFragments();
    //        if (ListUtils.isNullOrEmpty(fragments)) {
    //            return;
    //        }
    //        getSupportFragmentManager()
    //                .beginTransaction()
    //                .hide(fragments.get(0))
    //                .add(containerViewId, fragment)
    //                .show(fragment)
    //                .commit();
    //    }

}
