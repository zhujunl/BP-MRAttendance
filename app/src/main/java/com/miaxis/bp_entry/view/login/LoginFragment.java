package com.miaxis.bp_entry.view.login;

import android.content.Context;
import android.util.Log;


import com.miaxis.bp_entry.R;
import com.miaxis.bp_entry.app.App;
import com.miaxis.bp_entry.data.entity.ConfigManager;
import com.miaxis.bp_entry.databinding.FragmentLoadingBinding;
import com.miaxis.bp_entry.manager.ToastManager;
import com.miaxis.bp_entry.view.home.HomeFragment;
import com.miaxis.bp_entry.view.base.BaseViewModelFragment;
import com.miaxis.bp_entry.view.base.OnFragmentInteractionListener;

import androidx.lifecycle.ViewModelProvider;


public class LoginFragment extends BaseViewModelFragment<FragmentLoadingBinding, LoginViewModel> {

    private static LoginFragment instance;
    private final String TAG="LoginFragment";
    private OnFragmentInteractionListener mListener;

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnFragmentInteractionListener) {
            mListener = (OnFragmentInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString() + " must implement OnFragmentInteractionListener");
        }
    }

    public static LoginFragment getInstance(){
        if(instance==null){
            instance=new LoginFragment();
        }
        return instance;
    }

    @Override
    protected int setContentView() { return R.layout.fragment_loading; }

    @Override
    protected LoginViewModel initViewModel() {
        return new ViewModelProvider(this, getViewModelProviderFactory()).get(LoginViewModel.class);
    }

    @Override
    public int initVariableId() {
        return com.miaxis.bp_entry.BR.viewmodel;
    }

    @Override
    protected void initView() {
        App.getInstance().initApplication();
        binding.loginSure.setOnClickListener(v->{
            viewModel.Login(binding.placeId.getText().toString().trim(),
                    binding.title.getText().toString().trim(),
                    binding.attenIp.getText().toString().trim(),listener);
        });
    }

    ConfigManager.OnConfigSaveListener listener= (result, message) -> {
        if (result) {
            mListener.replaceFragment(HomeFragment.getInstance());
            ToastManager.getToastBody(message, ToastManager.SUCCESS);
        } else {
            ToastManager.getToastBody(message, ToastManager.ERROR);
        }
    };

    @Override
    public void onBackPressed() {
        mListener.backToStack(null);
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.e(TAG,"onDestroy");
        App.getInstance().cpClear();
    }
}
