package com.miaxis.attendance.ui.manager;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.os.Handler;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.miaxis.attendance.R;
import com.miaxis.attendance.config.AppConfig;
import com.miaxis.attendance.ui.finger.MR990FingerStrategy;
import com.miaxis.common.utils.FileUtils;
import com.mx.finger.common.MxImage;
import com.mx.finger.utils.RawBitmapUtils;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.AppCompatSpinner;
import timber.log.Timber;

//import com.miaxis.attendance.api.HttpApi;

/**
 * @author Tank
 * @date 2021/9/29 5:39 下午
 * @des
 * @updateAuthor
 * @updateDes
 */
public class FingerCaptureDialog extends Dialog {
    private static final String TAG = "FingerCaptureDialog";
    private Handler mHandler = new Handler();

    private MxUser mxUser;

    protected FingerCaptureDialog(@NonNull Context context, MxUser mxUser) {
        super(context);
        this.mxUser = mxUser;
        setContentView(R.layout.dialog_capture_finger);
        mProgressDialog = new ProgressDialog(context);
    }

    private PageNotifyInterface pageNotifyInterface;

    public FingerCaptureDialog bind(PageNotifyInterface pageNotifyInterface) {
        this.pageNotifyInterface = pageNotifyInterface;
        return this;
    }

    private String fingerPath;
    private byte[] fingerFeature;

    private int selected_position = 0;

    @Override
    protected void onStart() {
        super.onStart();
        AppCompatSpinner acs_spinner = findViewById(R.id.acs_spinner);
        acs_spinner.setSelection(0);
        acs_spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                selected_position = position;
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });
        ImageView iv_finger = findViewById(R.id.iv_finger);
//        findViewById(R.id.btn_confirm).setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                Disposable subscribe = Observable.create((ObservableOnSubscribe<MxResponse<?>>) emitter -> {
//                    showProgress("上传指纹中，请稍后");
//                    HttpResponse<String> httpResponse = HttpApi.uploadFinger(Integer.parseInt(mxUser.userId), selected_position, new File(fingerPath));
//                    if (httpResponse != null && httpResponse.isSuccess()) {
//                        Person person = PersonModel.findByUserID(mxUser.userId);
//                        if (person == null) {
//                            emitter.onNext(MxResponse.CreateFail(-70, "指纹入库失败"));
//                            return;
//                        }
//                        LocalImage localImage = new LocalImage();
//                        localImage.LocalPath = fingerPath;
//                        localImage.RemotePath = httpResponse.result;
//                        long insert = LocalImageModel.insert(localImage);
//                        if (insert <= 0) {
//                            emitter.onNext(MxResponse.CreateFail(-71, "图像入库失败"));
//                            return;
//                        }
//                        person.fingerIds = person.fingerIds == null ? new ArrayList<>() : person.fingerIds;
//                        person.fingerIds.add(insert);
//                        long update = PersonModel.update(person);
//                        if (update <= 0) {
//                            emitter.onNext(MxResponse.CreateFail(-72, "更新数据失败"));
//                            return;
//                        }
//                        Finger finger = new Finger();
//                        finger.UserId = mxUser.userId;
//                        finger.fingerImageId = insert;
//                        finger.FingerFeature = fingerFeature;
//                        finger.Position = selected_position;
//                        insert = FingerModel.insert(finger);
//                        if (insert <= 0) {
//                            emitter.onNext(MxResponse.CreateFail(-73, "指纹入库失败"));
//                            return;
//                        }
//                        emitter.onNext(MxResponse.CreateSuccess());
//                        return;
//                    }
//                    emitter.onNext(MxResponse.CreateFail(-60, httpResponse != null ? httpResponse.message : "上传指纹信息失败"));
//                }).subscribeOn(Schedulers.from(App.getInstance().threadExecutor))
//                        .observeOn(AndroidSchedulers.mainThread())
//                        .subscribe(result -> {
//                            showProgress(null);
//                            dismiss();
//                            if (!MxResponse.isSuccess(result)) {
//                                showError(result.getMessage());
//                            } else {
//                                if (pageNotifyInterface != null) {
//                                    pageNotifyInterface.onFlush();
//                                }
//                            }
//                        }, throwable -> {
//                            showProgress(null);
//                            Timber.e("getUserList  throwable:" + throwable);
//                            showError(throwable.getMessage());
//                        });
//
//            }
//        });
        findViewById(R.id.btn_capture).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showProgress("正在采集指纹");
                MR990FingerStrategy.getInstance().readFingerOnly(new MR990FingerStrategy.ReadFingerImageCallBack() {
                    @Override
                    public void onReadFinger(MxImage image, byte[] feature) {
                        showProgress(null);
                        fingerFeature = feature;
                        Log.e(TAG, "onReadFinger: " + image);
                        fingerPath = AppConfig.Path_FingerTemp + "finger_" + mxUser.userId + "_" + System.currentTimeMillis() + ".bmp";
                        FileUtils.initFile(fingerPath);
                        int saveBMP = RawBitmapUtils.saveBMP(fingerPath, image.data, image.width, image.height);
                        Timber.e("saveBMP:%s", saveBMP);
                        mHandler.post(new Runnable() {
                            @Override
                            public void run() {
                                enableConfirm(saveBMP != 0);
                            }
                        });
                        if (saveBMP == 0) {
                            try {
                                mHandler.post(new Runnable() {
                                    @Override
                                    public void run() {
                                        Glide.with(iv_finger).load(fingerPath).into(iv_finger);
                                    }
                                });
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        } else {
                            enableConfirm(false);
                        }
                    }

                    @Override
                    public void onError(Exception e) {
                        Log.e(TAG, "onError: " + e);
                        showProgress(null);
                        enableConfirm(false);
                        showError("" + e.getMessage());
                    }
                });
            }
        });
    }

    private ProgressDialog mProgressDialog;

    private void showProgress(String message) {
        mHandler.post(() -> {
            if (mProgressDialog != null) {
                if (TextUtils.isEmpty(message)) {
                    mProgressDialog.dismiss();
                } else {
                    mProgressDialog.show();
                }
                mProgressDialog.setMessage(String.valueOf(message));
            }
        });
    }

    private void showError(String message) {
        mHandler.post(() -> Toast.makeText(getContext(), "" + message, Toast.LENGTH_SHORT).show());
    }

    @Override
    public void dismiss() {
        super.dismiss();
        MR990FingerStrategy.getInstance().stopRead();
    }

    private void enableConfirm(boolean enable) {
        try {
            findViewById(R.id.btn_confirm).setVisibility(!enable ? View.VISIBLE : View.GONE);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


}
