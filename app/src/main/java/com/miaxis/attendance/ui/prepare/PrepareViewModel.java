package com.miaxis.attendance.ui.prepare;

import com.miaxis.attendance.api.bean.UserBean;
import com.miaxis.attendance.data.entity.Person;
import com.miaxis.attendance.data.model.FaceModel;
import com.miaxis.attendance.data.model.FingerModel;
import com.miaxis.attendance.data.model.PersonModel;
import com.miaxis.common.utils.ListUtils;

import java.util.List;

import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

public class PrepareViewModel extends ViewModel {

    MutableLiveData<String> progressMsg = new MutableLiveData<>();
    MutableLiveData<Boolean> result = new MutableLiveData<>();
    MutableLiveData<String> msg = new MutableLiveData<>();

    public PrepareViewModel() {
    }

//    public void init() {
//        progressMsg.setValue("正在请求数据中");
//        msg.setValue("");
//        Disposable subscribe = Observable.create((ObservableOnSubscribe<Boolean>) emitter -> {
//            Response<HttpResponse<List<UserBean>>> execute = HttpApi.getUserList().execute();
//            HttpResponse<List<UserBean>> body = execute.body();
//            Timber.e("getUserList:" + body);
//            if (!body.isSuccess()) {
//                throw new IllegalArgumentException("获取用户信息失败，" + body.message);
//            } else {
//                List<UserBean> result = body.result;
//                progressMsg.postValue("处理数据中");
//                if (ListUtils.isNullOrEmpty(result)) {
//                    msg.postValue("数据初始化");
//                    PersonModel.deleteAll();
//                    FingerModel.deleteAll();
//                    FaceModel.deleteAll();
//                    emitter.onNext(true);
//                } else {
//                    int success = 0;
//                    int failed = 0;
//                    int total = result.size();
//                    Gson gson = new Gson();
//                    int index = 0;
//                    for (UserBean userBean : result) {
//                        User user = new User();
//                        user.id = userBean.id;
//                        user.id_number = userBean.idNumber;
//                        user.name = userBean.name;
//                        user.job_no = userBean.jobNo;
//                        user.department_id = "" + userBean.departmentId;
//                        user.url_face = userBean.basePic;
//                        user.url_fingers = gson.toJson(userBean.fingerList);
//                        Timber.e("insertOrUpdate:%s", "-----------------------");
//                        MxResponse<?> mxResponse = PersonTransform.insertOrUpdate(user);
//                        Timber.e("insertOrUpdate:%s", mxResponse);
//                        Timber.e("insertOrUpdate:%s", "+++++++++++++++++++++++");
//                        if (MxResponse.isSuccess(mxResponse)) {
//                            success++;
//                        } else {
//                            failed++;
//                            msg.postValue((msg.getValue() == null ? "" :
//                                    msg.getValue()) + "添加失败，姓名：" + user.name + "，错误：" + mxResponse.getMessage() + "\n");
//                            HttpResponse<?> response = HttpApi.exceptionReport(user.id, mxResponse.getMessage());
//                            Timber.e("exceptionReport:%s", response);
//                            //msg.postValue((msg.getValue() == null ? "" :
//                            //"上传错误信息" + (response.isSuccess() ? "成功" : "," + response.message) + "\n"));
//                        }
//                        progressMsg.postValue("总共：" + total + "   成功：" + success + "   失败：" + failed);
//                        if (index == 1) {
//                            //break;
//                        }
//                        index++;
//                    }
//                    processPersonNeedDelete(result);
//                    emitter.onNext(success >= total);
//                }
//            }
//        }).subscribeOn(Schedulers.from(App.getInstance().threadExecutor))
//                .observeOn(AndroidSchedulers.mainThread())
//                .subscribe(success -> {
//                    result.setValue(success);
//                }, throwable -> {
//                    Timber.e("getUserList  throwable:" + throwable);
//                    msg.setValue("" + throwable.getMessage());
//                    result.setValue(false);
//                });
//    }

    /**
     * 根据远程数据删除本地数据
     *
     * @param result 远程数据列表
     */
    private void processPersonNeedDelete(List<UserBean> result) {
        if (ListUtils.isNullOrEmpty(result)) {
            PersonModel.deleteAll();
            FingerModel.deleteAll();
            FaceModel.deleteAll();
            return;
        }
        List<Person> local = PersonModel.findAll();
//        if (!ListUtils.isNullOrEmpty(local)) {
//            HashMap<String, UserBean> remote = new HashMap<>();
//            for (UserBean userBean : result) {
//                remote.put("" + userBean.id, userBean);
//            }
//            for (Person person : local) {
//                UserBean userBean = remote.get(person.UserId);
//                if (userBean == null) {
//                    Timber.e("processPersonNeedDelete:%s", person);
//                    PersonModel.delete(person);
//                    FingerModel.delete(person.UserId);
//                    FaceModel.delete(person.UserId);
//                }
//            }
//        }
    }

}