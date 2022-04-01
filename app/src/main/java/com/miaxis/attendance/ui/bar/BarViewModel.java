package com.miaxis.attendance.ui.bar;

import android.os.SystemClock;
import android.text.TextUtils;

import com.miaxis.attendance.data.bean.AttendanceBean;

import java.text.DecimalFormat;
import java.util.concurrent.atomic.AtomicReference;

import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

public class BarViewModel extends ViewModel {

    //public MutableLiveData<Integer> httpServerStatus = new MutableLiveData<>(0);

    public MutableLiveData<Integer> UserCounts = new MutableLiveData<>(0);



    public AtomicReference<String> LastUserId = new AtomicReference<>();
    private final DecimalFormat decimalFormat= new DecimalFormat("##0.0000");
    public BarViewModel() {
    }

    public boolean isNewUser(AttendanceBean attendanceBean) {
        boolean isNew = attendanceBean != null && !
                TextUtils.equals(attendanceBean.UserId, this.LastUserId.get());
        if (isNew) {
            this.LastUserId.set(attendanceBean.UserId);
        }
        return isNew;
    }




    public void setNewUserReset() {
        this.LastUserId.set(null);
    }

    public String format(float f){
        return decimalFormat.format(f);
    }
}