package com.miaxis.bp_entry.view.home;

import android.content.Context;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.miaxis.bp_entry.R;
import com.miaxis.bp_entry.app.App;
import com.miaxis.bp_entry.data.entity.Config;
import com.miaxis.bp_entry.data.entity.ConfigManager;
import com.miaxis.bp_entry.data.entity.Staff;
import com.miaxis.bp_entry.databinding.FragmentHomeBinding;
import com.miaxis.bp_entry.server.UdpServer;
import com.miaxis.bp_entry.util.HardWareUtils;
import com.miaxis.bp_entry.util.StringUtil;
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
public class HomeFragment extends BaseViewModelFragment<FragmentHomeBinding,HomeViewModel> implements UdpServer.UdpListener {

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
    protected void initData() {
        super.initData();
        UdpServer udpServer=new UdpServer(this);
        new Thread(udpServer).start();
        config= ConfigManager.getInstance().getConfig();
    }

    @Override
    protected void initView() {
        viewModel.staffRegiter.observe(this,staff -> {
            Toast.makeText(getActivity(), "有新员工"+staff.toString(), Toast.LENGTH_SHORT).show();
            binding.entryBegin.setVisibility(View.VISIBLE);
        });
        if (config!=null){
            binding.tittleTxt.setText(config.tittle);
            binding.placeTxt.setText("场所地址："+config.place);
        }
        binding.IPTxt.setText("本机IP："+HardWareUtils.getHostIP());
        binding.entryBegin.setOnClickListener(v->{
            mListener.replaceFragment(RegisterFragment.newInstance(viewModel.staffRegiter.getValue(),false));
            binding.entryBegin.setVisibility(View.INVISIBLE);
        });
        binding.tittleTxt.setOnClickListener(v->{
            Log.e("标题：","点击");
            if (config.place!=null){
                Staff staff=new Staff();
                staff.setPlace(config.place);
                mListener.replaceFragment(RegisterFragment.newInstance(staff,true));
            }
        });
        binding.tittleTxt.setOnLongClickListener(v -> {
            Log.e("标题：","长按");
            viewModel.updateList();
            return true;
        });
    }

    @Override
    public void onBackPressed() {
        mListener.backToStack(null);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        App.getInstance().cpClear();
    }

    @Override
    public void recevice(byte[] bytes) {
        getActivity().runOnUiThread(()->{
            if(bytes[3]==0x5A&&bytes[4]==0x43){
                Toast.makeText(getActivity(), "注册", Toast.LENGTH_SHORT).show();
                viewModel.Register(StringUtil.getPlace(bytes), StringUtil.getCode(bytes));
            }else if(bytes[3]==0x53&&bytes[4]==0x43){
                Toast.makeText(getActivity(), "删除", Toast.LENGTH_SHORT).show();
                viewModel.delete(StringUtil.getPlace(bytes), StringUtil.getCode(bytes));
            }
        });
        Log.e("TAG", "dat a:" + new String(bytes));
        Log.e("TAG", "门店ID:" + StringUtil.getPlace(bytes));
        Log.e("TAG", "工  号:" + StringUtil.getCode(bytes));

    }
}
