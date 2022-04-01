package com.miaxis.bp_entry.view.home;

import android.util.Log;

import com.miaxis.bp_entry.app.App;
import com.miaxis.bp_entry.data.entity.Staff;
import com.miaxis.bp_entry.data.entity.StaffManager;
import com.miaxis.bp_entry.viewModel.BaseViewModel;

import androidx.lifecycle.MutableLiveData;
import io.reactivex.Observable;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;

/**
 * @author ZJL
 * @date 2022/3/23 19:39
 * @des
 * @updateAuthor
 * @updateDes
 */
public class HomeViewModel extends BaseViewModel {
    public MutableLiveData<Staff> staffRegiter=new MutableLiveData<>();

    public void Register(String place,String code){
        Staff staff=new Staff();
        staff.setPlace(place);
        staff.setCode(code);
        staffRegiter.setValue(staff);
    }

    public void delete(String place,String code){
        Disposable disposable =Observable.create((ObservableOnSubscribe<Boolean>) e->{
            StaffManager.getInstance().deleteStaff(place,code);
            e.onNext(Boolean.TRUE);
        }).subscribeOn(Schedulers.from(App.getInstance().getThreadExecutor()))
        .observeOn(AndroidSchedulers.mainThread())
        .subscribe(flag->{
            Log.e("DeleteStaff:","删除成功:  工号:"+code+"场所ID:"+place);
        },throwable -> {
            Log.e("DeleteStaff:","删除失败:   "+throwable.getMessage());
        });
        App.getInstance().getCp().add(disposable);
    }

}
