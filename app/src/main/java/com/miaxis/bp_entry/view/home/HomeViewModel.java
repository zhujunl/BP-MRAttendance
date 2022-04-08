package com.miaxis.bp_entry.view.home;

import android.util.Log;

import com.miaxis.bp_entry.api.DeleteRequest;
import com.miaxis.bp_entry.api.StaffList;
import com.miaxis.bp_entry.api.response.StaffApi;
import com.miaxis.bp_entry.api.response.StaffResponse;
import com.miaxis.bp_entry.app.App;
import com.miaxis.bp_entry.bridge.SingleLiveEvent;
import com.miaxis.bp_entry.data.entity.Staff;
import com.miaxis.bp_entry.data.entity.StaffManager;
import com.miaxis.bp_entry.manager.ToastManager;
import com.miaxis.bp_entry.viewModel.BaseViewModel;

import java.util.List;

import androidx.lifecycle.MutableLiveData;
import io.reactivex.Observable;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;
import retrofit2.Response;

/**
 * @author ZJL
 * @date 2022/3/23 19:39
 * @des
 * @updateAuthor
 * @updateDes
 */
public class HomeViewModel extends BaseViewModel {
    public MutableLiveData<Staff> staffRegiter=new SingleLiveEvent<>();
    public MutableLiveData<Boolean> btn=new SingleLiveEvent<>();

    public void Register(String place,String code){
        Staff staff=new Staff();
        staff.setPlace(place);
        staff.setCode(code);
        staffRegiter.setValue(staff);
    }

    public void delete(String place,String code){
        Disposable disposable =Observable.create((ObservableOnSubscribe<Boolean>) e->{
            DeleteRequest deleteRequest=new DeleteRequest(code,place);
            Response<StaffResponse<String>> execute = StaffApi.delStaff(deleteRequest).execute();
            StaffResponse<String> body = execute.body();
            if (body!=null&&body.Success()){
                StaffManager.getInstance().deleteStaff(place,code);
                e.onNext(Boolean.TRUE);
            }else {
                e.onNext(Boolean.FALSE);
            }
        }).subscribeOn(Schedulers.from(App.getInstance().getThreadExecutor()))
        .observeOn(AndroidSchedulers.mainThread())
        .subscribe(flag->{
            if (flag)
                Log.e("DeleteStaff:","删除成功:  工号:"+code+"场所ID:"+place);
        },throwable -> {
            Log.e("DeleteStaff:","删除失败:   "+throwable.getMessage());
        });
        App.getInstance().getCp().add(disposable);
    }

    public void updateList(){
        Disposable subscribe = Observable.create((ObservableOnSubscribe<Boolean>) e -> {
            List<Staff> list = StaffManager.getInstance().StaffList();
            if (list.size() > 0) {
                Log.e("updateList", "list.size()=" +list.size() );
                Log.e("updateList", "list" +list.toString() );
                StaffList staffList=new StaffList(list);
                Response<StaffResponse<String>> execute = StaffApi.updateList(list).execute();
                StaffResponse<String> body = execute.body();
                if (body != null && body.Success()) {
                    e.onNext(Boolean.TRUE);
                } else {
                    e.onNext(Boolean.FALSE);
                }
            } else {
                e.onNext(Boolean.FALSE);
            }
        }).subscribeOn(Schedulers.from(App.getInstance().getThreadExecutor()))
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(flag -> {
                    if (flag) {
                        ToastManager.toast("添加完毕",ToastManager.SUCCESS);
                        Log.e("updateList:", "添加完毕");
                    }
                }, throwable -> {
                    ToastManager.toast("添加失败"+throwable.getMessage(),ToastManager.ERROR);
                    Log.e("updateList:","添加失败"+throwable.getMessage());
                });
    }

    public void  updateStaff(){

    }

}
