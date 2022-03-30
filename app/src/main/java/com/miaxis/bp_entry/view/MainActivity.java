package com.miaxis.bp_entry.view;

import com.afollestad.materialdialogs.MaterialDialog;
import com.miaxis.bp_entry.R;
import com.miaxis.bp_entry.databinding.ActivityMainBinding;
import com.miaxis.bp_entry.view.base.BaseActivity;
import com.miaxis.bp_entry.view.base.BaseViewModelFragment;
import com.miaxis.bp_entry.view.base.OnFragmentInteractionListener;
import com.miaxis.bp_entry.view.login.LoginFragment;

import androidx.fragment.app.Fragment;

public class MainActivity extends BaseActivity<ActivityMainBinding>  implements OnFragmentInteractionListener {

    private MaterialDialog waitDialog;
    private MaterialDialog resultDialog;
    private MaterialDialog quitDialog;
    private String root;


    @Override
    protected int setContentView() {
        return R.layout.activity_main;
    }

    @Override
    protected void initData() {
        replaceFragment(LoginFragment.getInstance());
    }

    @Override
    protected void initView() {
        initDialog();
    }

    @Override
    public void onBackPressed() {
        Fragment visibleFragment = getVisibleFragment();
        if (visibleFragment != null) {
            BaseViewModelFragment fragment = (BaseViewModelFragment) visibleFragment;
            fragment.onBackPressed();
        }
    }

    @Override
    public void setRoot(Fragment fragment) {
        root = fragment.getClass().getName();
    }

    @Override
    public void backToRoot() {
        getSupportFragmentManager().popBackStack(root, 1);
    }

    @Override
    public void replaceFragment(Fragment fragment) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                hideInputMethod();
                getSupportFragmentManager().beginTransaction()
                        .replace(R.id.fg, fragment)
                        .addToBackStack(fragment.getClass().getName())
                        .commit();
            }
        });
    }

    @Override
    public void backToStack(Class<? extends Fragment> fragment) {
        int backStackEntryCount = getSupportFragmentManager().getBackStackEntryCount();
        if (backStackEntryCount > 2) {
            if (fragment != null) {
                getSupportFragmentManager()
                        .popBackStackImmediate(fragment.getName(), 0);
            } else {
                getSupportFragmentManager().popBackStackImmediate(null, 0);
            }
        } else {
            exitApp();
        }
    }


    @Override
    public void showWaitDialog(String message) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                waitDialog.getContentView().setText(message);
                waitDialog.show();
            }
        });
    }

    @Override
    public void dismissWaitDialog() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (waitDialog.isShowing()) {
                    waitDialog.dismiss();
                }
            }
        });
    }

    @Override
    public void showResultDialog(String message) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                resultDialog.getContentView().setText(message);
                resultDialog.show();
            }
        });
    }

    @Override
    public void dismissResultDialog() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (resultDialog.isShowing()) {
                    resultDialog.dismiss();
                }
            }
        });
    }

    @Override
    public void exitApp() { quitDialog.show(); }

    private void initDialog() {
        waitDialog = new MaterialDialog.Builder(this)
                .progress(true, 100)
                .content("请稍后")
                .cancelable(false)
                .autoDismiss(false)
                .build();
        quitDialog = new MaterialDialog.Builder(this)
                .title("确认退出?")
                .positiveText("确认")
                .onPositive((dialog, which) -> {
                    finish();
                    System.exit(0);
                })
                .negativeText("取消")
                .build();
        resultDialog = new MaterialDialog.Builder(this)
                .content("")
                .positiveText("确认")
                .build();
    }
}