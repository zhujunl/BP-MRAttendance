package com.miaxis.bp_entry.viewModel;

import android.util.Log;

import com.miaxis.bp_entry.app.App;
import com.miaxis.bp_entry.bridge.SingleLiveEvent;
import com.miaxis.bp_entry.exception.MyException;
import com.miaxis.bp_entry.exception.NetResultFailedException;
import com.miaxis.bp_entry.manager.ToastManager;
import com.miaxis.bp_entry.util.ValueUtil;

import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

public class BaseViewModel extends ViewModel {

    public MutableLiveData<String> waitMessage = new SingleLiveEvent<>();
    public MutableLiveData<String> resultMessage = new SingleLiveEvent<>();
    public MutableLiveData<ToastManager.ToastBody> toast = new SingleLiveEvent<>();

    protected String handleError(Throwable throwable) {
        throwable.printStackTrace();
        Log.e("asd", "" + throwable.getMessage());
        if (ValueUtil.isNetException(throwable)) {
            return "联网错误";
        } else if (throwable instanceof NetResultFailedException) {
            return throwable.getMessage();
        } else if (throwable instanceof MyException) {
            return throwable.getMessage();
        } else {
            return "出现错误";
        }
    }

    protected String getString(int resourceId) {
        return App.getInstance().getResources().getString(resourceId);
    }

}
