package com.miaxis.bp_entry.view.register;

import android.text.TextUtils;

import com.miaxis.bp_entry.R;
import com.miaxis.bp_entry.auxiliary.OnLimitClickHelper;
import com.miaxis.bp_entry.data.entity.Staff;
import com.miaxis.bp_entry.databinding.FragmentRegisterBinding;
import com.miaxis.bp_entry.event.FaceRegisterEvent;
import com.miaxis.bp_entry.event.FingerRegisterEvent;
import com.miaxis.bp_entry.manager.ToastManager;
import com.miaxis.bp_entry.view.base.BaseViewModelFragment;
import com.miaxis.bp_entry.view.face.FaceFragment;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import androidx.lifecycle.ViewModelProvider;

public class RegisterFragment extends BaseViewModelFragment<FragmentRegisterBinding, RegisterViewModel> {

    private Staff staff;
    private boolean modify=false;

    public static RegisterFragment newInstance(Staff staff,boolean modify) {
        RegisterFragment registerFragment=new RegisterFragment();
        registerFragment.setStaff(staff);
        registerFragment.setModify(modify);
        return registerFragment;
    }

    public RegisterFragment() {
        // Required empty public constructor
    }

    @Override
    protected int setContentView() {
        return R.layout.fragment_register;
    }

    @Override
    protected RegisterViewModel initViewModel() {
        return new ViewModelProvider(this, getViewModelProviderFactory()).get(RegisterViewModel.class);
    }

    @Override
    public int initVariableId() {
        return com.miaxis.bp_entry.BR.viewmodel;
    }

    @Override
    protected void initData() {
        viewModel.placeId.set(staff.getPlace());
        if (staff.getCode()!=null)viewModel.code.set(staff.getCode());
        viewModel.modify.setValue(modify);
    }

    @Override
    protected void initView() {
        binding.title.setText(modify?"信息修改":"信息注册");
        viewModel.registerFlag.observe(this, flag -> {
            if (flag)
                onBackPressed();
        });
        binding.ivBack.setOnClickListener(v -> onBackPressed());
        binding.tvHeader.setOnClickListener(new OnLimitClickHelper(view -> mListener.replaceFragment(FaceRegisterFragment.newInstance())));
        binding.tvFace.setOnClickListener(new OnLimitClickHelper(view -> {
            if (viewModel.getFeatureCache()!=null){
                mListener.replaceFragment(FaceFragment.getInstance(viewModel.getFeatureCache()));
            }else {
                ToastManager.toast("未采集人脸",ToastManager.ERROR);
            }
        }));
        binding.tvFinger1.setOnClickListener(new OnLimitClickHelper(view -> mListener.replaceFragment(FingerRegisterFragment.newInstance(RegisterViewModel.FINGER1))));
        binding.tvFinger2.setOnClickListener(new OnLimitClickHelper(view -> mListener.replaceFragment(FingerRegisterFragment.newInstance(RegisterViewModel.FINGER2))));
        binding.btnRegister.setText(modify?"修改":"注册");
        binding.btnRegister.setOnClickListener(v -> {
            if (viewModel.checkInput()) {
                viewModel.mode();
            } else {
                ToastManager.toast("请输入全部信息", ToastManager.INFO);
            }
        });
        EventBus.getDefault().register(this);
    }

    @Override
    public void onBackPressed() {
        mListener.backToStack(null);
    }

    @Subscribe(threadMode = ThreadMode.MAIN, sticky = true)
    public void onFaceRegisterEvent(FaceRegisterEvent event) {
        viewModel.faceFeatureHint.set("已采集");
        binding.tvHeader.setOnClickListener(null);
        viewModel.setFeatureCache(event.getFeature());
        viewModel.setMaskFeatureCache(event.getMaskFeature());
        viewModel.setHeaderCache(event.getBitmap());
        EventBus.getDefault().removeStickyEvent(event);
    }

    @Subscribe(threadMode = ThreadMode.MAIN, sticky = true)
    public void onFingerRegisterEvent(FingerRegisterEvent event) {
        String feature = event.getFeature();
        if (TextUtils.isEmpty(feature)) return;
        if (TextUtils.equals(RegisterViewModel.FINGER1, event.getMark())) {
            viewModel.finger1FeatureHint.set("已采集");
            binding.tvFinger1.setOnClickListener(null);
            viewModel.setFingerFeature1(feature);
            viewModel.setTemplate1(event.getTemplate());
        } else {
            viewModel.finger2FeatureHint.set("已采集");
            binding.tvFinger2.setOnClickListener(null);
            viewModel.setFingerFeature2(feature);
            viewModel.setTemplate2(event.getTemplate());
        }
        EventBus.getDefault().removeStickyEvent(event);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        EventBus.getDefault().unregister(this);
    }

    public void setStaff(Staff staff) {
        this.staff = staff;
    }

    public void setModify(boolean modify) {
        this.modify = modify;
    }
}
