package com.miaxis.attendance.ui.net;

import com.miaxis.common.utils.HardWareUtils;

import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

public class NetViewModel extends ViewModel {

    public MutableLiveData<String> IpAddress = new MutableLiveData<>(HardWareUtils.getHostIP());

    public NetViewModel() {
    }

    public void flushIpAddress(){
        this.IpAddress.postValue(HardWareUtils.getHostIP());
    }

}