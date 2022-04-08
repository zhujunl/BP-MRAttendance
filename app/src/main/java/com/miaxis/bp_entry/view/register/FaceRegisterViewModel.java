package com.miaxis.bp_entry.view.register;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.hardware.Camera;
import android.text.TextUtils;
import android.util.Base64;
import android.util.Log;

import com.miaxis.bp_entry.app.App;
import com.miaxis.bp_entry.been.PhotoFaceFeature;
import com.miaxis.bp_entry.bridge.SingleLiveEvent;
import com.miaxis.bp_entry.bridge.Status;
import com.miaxis.bp_entry.event.FaceRegisterEvent;
import com.miaxis.bp_entry.exception.MyException;
import com.miaxis.bp_entry.manager.CameraManager;
import com.miaxis.bp_entry.manager.FaceManager;
import com.miaxis.bp_entry.manager.ToastManager;
import com.miaxis.bp_entry.viewModel.BaseViewModel;

import org.greenrobot.eventbus.EventBus;

import androidx.databinding.ObservableField;
import androidx.lifecycle.MutableLiveData;
import io.reactivex.Observable;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;

public class FaceRegisterViewModel extends BaseViewModel {

    public MutableLiveData<Status> shootFlag = new MutableLiveData<>(Status.FAILED);
    public ObservableField<String> hint = new ObservableField<>("请自拍一张大头照");
    public MutableLiveData<Boolean> confirmFlag = new SingleLiveEvent<>();

    private byte[] featureCache;
    private String maskFeatureCache;
    private Bitmap headerCache;

    public FaceRegisterViewModel() {
    }

    public void takePicture() {
        shootFlag.setValue(Status.LOADING);
        hint.set("处理中");
        Camera frontCamera = CameraManager.getInstance().getFrontCamera();
        if (frontCamera != null) {
            frontCamera.takePicture(null, null, this::handlePhoto);
        } else {
            toast.setValue(ToastManager.getToastBody("摄像头未正常启动，请退出后重试", ToastManager.ERROR));
        }
    }

    public void retry() {
        featureCache = null;
        headerCache = null;
        shootFlag.setValue(Status.FAILED);
        hint.set("请自拍一张大头照");
        Camera frontCamera = CameraManager.getInstance().getFrontCamera();
        if (frontCamera != null) {
            frontCamera.startPreview();
        } else {
            toast.setValue(ToastManager.getToastBody("摄像头未正常启动，请退出后重试", ToastManager.ERROR));
        }
    }

    public void confirm() {
        if (!TextUtils.isEmpty(featureCache.toString()) && !TextUtils.isEmpty(maskFeatureCache) && headerCache != null) {
            EventBus.getDefault().postSticky(new FaceRegisterEvent(featureCache, maskFeatureCache, headerCache));
            confirmFlag.setValue(Boolean.TRUE);
        }
    }

    private void handlePhoto(byte[] data, Camera camera) {
        Disposable subscribe = Observable.create((ObservableOnSubscribe<Bitmap>) emitter -> {
            Bitmap bitmap = BitmapFactory.decodeByteArray(data, 0, data.length);
            Matrix matrix = new Matrix();
            matrix.postRotate(CameraManager.getInstance().getPictureOrientation());
            bitmap = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);
            emitter.onNext(bitmap);
        })
                .subscribeOn(Schedulers.from(App.getInstance().getThreadExecutor()))
                .observeOn(Schedulers.from(App.getInstance().getThreadExecutor()))
                .map(bitmap -> {
                    PhotoFaceFeature photoFaceFeature = FaceManager.getInstance().getPhotoFaceFeatureByBitmapForRegisterPosting(bitmap);
                    if (photoFaceFeature.getFaceFeature() != null && photoFaceFeature.getMaskFaceFeature() != null) {
                        headerCache = bitmap;
                        featureCache = photoFaceFeature.getFaceFeature();
                        maskFeatureCache = Base64.encodeToString(photoFaceFeature.getMaskFaceFeature(), Base64.NO_WRAP);
                        return true;
                    }
                    throw new MyException(photoFaceFeature.getMessage());
                })
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(result -> {
                    shootFlag.setValue(Status.SUCCESS);
                    hint.set("人脸特征提取成功");
                }, throwable -> {
                    shootFlag.setValue(Status.FAILED);
                    camera.startPreview();
                    if (throwable instanceof MyException) {
                        hint.set(throwable.getMessage() + "，请重新拍摄");
                    } else {
                        throwable.printStackTrace();
                        Log.e("asd", "" + throwable.getMessage());
                        hint.set("出现错误，请重新拍摄");
                    }
                });
    }

}
