package com.miaxis.attendance.ui.advertising;

import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

public class AdvertisingViewModel extends ViewModel {
    public MutableLiveData<Boolean> isIdle = new MutableLiveData<>();

    public AdvertisingViewModel() {
    }
}