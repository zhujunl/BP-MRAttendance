package com.miaxis.bp_entry.view.register;

import android.graphics.Bitmap;
import android.text.TextUtils;
import android.util.Log;

import com.miaxis.bp_entry.api.response.StaffApi;
import com.miaxis.bp_entry.api.response.StaffResponse;
import com.miaxis.bp_entry.app.App;
import com.miaxis.bp_entry.bridge.SingleLiveEvent;
import com.miaxis.bp_entry.data.entity.Staff;
import com.miaxis.bp_entry.data.entity.StaffManager;
import com.miaxis.bp_entry.data.entity.StaffModel;
import com.miaxis.bp_entry.viewModel.BaseViewModel;

import androidx.databinding.ObservableField;
import androidx.lifecycle.MutableLiveData;
import io.reactivex.Observable;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;
import retrofit2.Response;

public class RegisterViewModel extends BaseViewModel {

    public final static String FINGER1 = "1";
    public final static String FINGER2 = "2";

    public ObservableField<String> name = new ObservableField<>();
    public ObservableField<String> placeId = new ObservableField<>();
    public ObservableField<String> code = new ObservableField<>();

    public ObservableField<String> faceFeatureHint = new ObservableField<>("点击采集");
    public ObservableField<String> faceVerifyHint = new ObservableField<>("点击核验");
    public ObservableField<String> finger1FeatureHint = new ObservableField<>("点击采集");
    public ObservableField<String> finger2FeatureHint = new ObservableField<>("点击采集");

    public MutableLiveData<Boolean> registerFlag = new SingleLiveEvent<>();
    public MutableLiveData<Boolean> modify=new MutableLiveData<>(Boolean.FALSE);

    private byte[] featureCache;
    private String maskFeatureCache;
    private Bitmap headerCache;
    private String fingerFeature1;
    private String fingerFeature2;
    private byte[] template1;
    private byte[] template2;

    public RegisterViewModel() {
    }

    public boolean checkInput() {
        if (TextUtils.isEmpty(placeId.get())
                || TextUtils.isEmpty(code.get())
                || featureCache==null
                || TextUtils.isEmpty(fingerFeature1)
                || TextUtils.isEmpty(fingerFeature2)
                || headerCache == null) {
            return false;
        }
        return true;
    }

    public void mode(){
        if(modify.getValue()){
            updateStaff();
        }else {
            getCourierByPhone();
        }
    }

    public void getCourierByPhone() {
        waitMessage.setValue("注册中，请稍后");
        Disposable subscribe = Observable.create((ObservableOnSubscribe<Boolean>) emitter -> {
            Staff staff=new Staff.Builder()
                    .place(placeId.get())
                    .code(code.get())
                    .faceFeature(featureCache)
                    .finger0(template1)
                    .finger1(template2).builder();
            Response<StaffResponse<String>> execute= StaffApi.addStaff(staff).execute();
            StaffResponse<String> body = execute.body();
            if(body!=null&&body.Success()){
                StaffManager.getInstance().save(staff);
                emitter.onNext(Boolean.TRUE);
            }else {
                emitter.onNext(Boolean.FALSE);
            }
        })
                .subscribeOn(Schedulers.from(App.getInstance().getThreadExecutor()))
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(flag -> {
                  if (flag){
                      waitMessage.setValue("");
                      resultMessage.setValue("注册成功");
                      registerFlag.setValue(Boolean.TRUE);
                  }else {
                      resultMessage.setValue("注册失败  ");
                      registerFlag.setValue(Boolean.FALSE);
                  }

                }, throwable -> {
                    waitMessage.setValue("");
                    resultMessage.setValue(handleError(throwable));
                    registerFlag.setValue(Boolean.FALSE);
                });
    }

    public void updateStaff(){
        waitMessage.setValue("上传中，请稍后");
        Disposable subscribe = Observable.create((ObservableOnSubscribe<Boolean>) emitter -> {
            String c=code.get();
            String p =placeId.get();
            Staff staff= StaffModel.queryStaff(p,c);
            if (staff!=null){
                staff.setFaceFeature(featureCache);
                staff.setFinger0(template1);
                staff.setFinger1(template2);
                Response<StaffResponse<String>> execute= StaffApi.updateStaff(staff).execute();
                StaffResponse<String> body = execute.body();
                if (body!=null&&body.Success()){
                    int index=StaffManager.getInstance().modifyStaff(staff);
                    Log.e("modify","index:"+index);
                    emitter.onNext(Boolean.TRUE);
                }else {
                    emitter.onNext(Boolean.FALSE);
                }
            }else {
                emitter.onNext(Boolean.FALSE);
            }
        })
                .subscribeOn(Schedulers.from(App.getInstance().getThreadExecutor()))
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(flag -> {
                    waitMessage.setValue("");
                    if (flag){
                        resultMessage.setValue("修改成功");
                        registerFlag.setValue(Boolean.TRUE);
                    }else {
                        resultMessage.setValue("修改失败");
                        registerFlag.setValue(Boolean.FALSE);
                    }
                }, throwable -> {
                    waitMessage.setValue("");
                    resultMessage.setValue(handleError(throwable));
                    registerFlag.setValue(Boolean.FALSE);
                });
    }

    public void setFeatureCache(byte[] featureCache) {
        this.featureCache = featureCache;
    }

    public byte[] getFeatureCache() {
        return featureCache;
    }

    public void setMaskFeatureCache(String maskFeatureCache) {
        this.maskFeatureCache = maskFeatureCache;
    }

    public void setHeaderCache(Bitmap headerCache) {
        this.headerCache = headerCache;
    }

    public void setFingerFeature1(String  fingerFeature1) {
        this.fingerFeature1 = fingerFeature1;
    }

    public void setFingerFeature2(String fingerFeature2) {
        this.fingerFeature2 = fingerFeature2;
    }

    public void setTemplate1(byte[] template1) {
        this.template1 = template1;
    }

    public void setTemplate2(byte[] template2) {
        this.template2 = template2;
    }
}
