package com.miaxis.bp_entry.view.register;

import android.graphics.Bitmap;
import android.text.TextUtils;

import com.miaxis.bp_entry.app.App;
import com.miaxis.bp_entry.bridge.SingleLiveEvent;
import com.miaxis.bp_entry.data.entity.Staff;
import com.miaxis.bp_entry.data.entity.StaffManager;
import com.miaxis.bp_entry.viewModel.BaseViewModel;

import androidx.databinding.ObservableField;
import androidx.lifecycle.MutableLiveData;
import io.reactivex.Observable;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;

public class RegisterViewModel extends BaseViewModel {

    public final static String FINGER1 = "1";
    public final static String FINGER2 = "2";

    public ObservableField<String> name = new ObservableField<>();
    public ObservableField<String> placeId = new ObservableField<>();
    public ObservableField<String> code = new ObservableField<>();

    public ObservableField<String> faceFeatureHint = new ObservableField<>("点击采集");
    public ObservableField<String> finger1FeatureHint = new ObservableField<>("点击采集");
    public ObservableField<String> finger2FeatureHint = new ObservableField<>("点击采集");

    public MutableLiveData<Boolean> registerFlag = new SingleLiveEvent<>();

    private String featureCache;
    private String maskFeatureCache;
    private Bitmap headerCache;
    private String fingerFeature1;
    private String fingerFeature2;

    public RegisterViewModel() {
    }

    public boolean checkInput() {
        if (TextUtils.isEmpty(placeId.get())
                || TextUtils.isEmpty(code.get())
                || TextUtils.isEmpty(featureCache)
                || TextUtils.isEmpty(fingerFeature1)
                || TextUtils.isEmpty(fingerFeature2)
                || headerCache == null) {
            return false;
        }
        return true;
    }

    public void getCourierByPhone() {
        waitMessage.setValue("注册中，请稍后");
        Disposable subscribe = Observable.create((ObservableOnSubscribe<Boolean>) emitter -> {
//            LoginRepository.getInstance().registerExpressmanSync(name.get(),
//                    number.get(),
//                    phone.get(),
//                    featureCache,
//                    fingerFeature1,
//                    fingerFeature2,
//                    headerCache);
            Staff staff=new Staff.Builder()
                    .place(placeId.get())
                    .code(code.get())
                    .faceFeature(featureCache)
                    .finger0(fingerFeature1)
                    .finger1(fingerFeature2).builder();
            StaffManager.getInstance().save(staff);
            emitter.onNext(Boolean.TRUE);
        })
                .subscribeOn(Schedulers.from(App.getInstance().getThreadExecutor()))
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(courier -> {
                    waitMessage.setValue("");
                    resultMessage.setValue("注册成功");
                    registerFlag.setValue(Boolean.TRUE);
                }, throwable -> {
                    waitMessage.setValue("");
                    resultMessage.setValue(handleError(throwable));
                });
    }

    public void setFeatureCache(String featureCache) {
        this.featureCache = featureCache;
    }

    public void setMaskFeatureCache(String maskFeatureCache) {
        this.maskFeatureCache = maskFeatureCache;
    }

    public void setHeaderCache(Bitmap headerCache) {
        this.headerCache = headerCache;
    }

    public void setFingerFeature1(String fingerFeature1) {
        this.fingerFeature1 = fingerFeature1;
    }

    public void setFingerFeature2(String fingerFeature2) {
        this.fingerFeature2 = fingerFeature2;
    }
}
