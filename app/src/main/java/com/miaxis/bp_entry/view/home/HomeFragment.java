package com.miaxis.bp_entry.view.home;

import android.content.Context;

import com.miaxis.bp_entry.R;
import com.miaxis.bp_entry.data.entity.Config;
import com.miaxis.bp_entry.data.entity.ConfigManager;
import com.miaxis.bp_entry.databinding.FragmentHomeBinding;
import com.miaxis.bp_entry.util.HardWareUtils;
import com.miaxis.bp_entry.view.base.BaseViewModelFragment;
import com.miaxis.bp_entry.view.base.OnFragmentInteractionListener;
import com.miaxis.bp_entry.view.register.RegisterFragment;

import androidx.lifecycle.ViewModelProvider;

/**
 * @author ZJL
 * @date 2022/3/23 19:37
 * @des
 * @updateAuthor
 * @updateDes
 */
public class HomeFragment extends BaseViewModelFragment<FragmentHomeBinding,HomeViewModel> {

    private static HomeFragment instance;
    private Config config;
    private final String TAG="HomeFragment";
    private OnFragmentInteractionListener mListener;

    public static HomeFragment getInstance(){
        if(instance==null){
            instance=new HomeFragment();
        }
        return instance;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnFragmentInteractionListener) {
            mListener = (OnFragmentInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString() + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    protected int setContentView() {
        return R.layout.fragment_home;
    }

    @Override
    protected HomeViewModel initViewModel() {
        return new ViewModelProvider(this,getViewModelProviderFactory()).get(HomeViewModel.class);
    }

    @Override
    public int initVariableId() {
        return com.miaxis.bp_entry.BR.viewmodel;
    }

    @Override
    protected void initView() {
        config= ConfigManager.getInstance().getConfig();
//        binding.tittleTxt.setText(config.tittle);
//        binding.placeTxt.setText(config.place);
        binding.IPTxt.setText(HardWareUtils.getHostIP());
        binding.entryBegin.setOnClickListener(v->{
            mListener.replaceFragment(RegisterFragment.newInstance());
        });
    }

    @Override
    public void onBackPressed() {
        mListener.backToStack(null);
    }
}
