//package com.miaxis.attendance.api;
//
//
//import android.content.Context;
//import android.text.TextUtils;
//
//import com.miaxis.attendance.BuildConfig;
//import com.miaxis.attendance.api.bean.UserBean;
//import com.miaxis.attendance.config.AppConfig;
//
//import java.io.File;
//import java.util.List;
//
//import androidx.annotation.IntRange;
//import okhttp3.MediaType;
//import okhttp3.MultipartBody;
//import okhttp3.RequestBody;
//import retrofit2.Call;
//import retrofit2.Response;
//
//public class HttpApi {
//
//    //public static final String BaseUrl = "http://192.168.5.139:8090/";//测试环境
//    //public static final String BaseUrl = "http://192.168.5.139:8085/";//正式环境
//    public static final String BaseUrl = BuildConfig.SERVER_URL;
//
//    public static void init(Context context) {
//        BaseAPI.getInstance().init(context);
//    }
//
//    public static Call<HttpResponse<List<UserBean>>> getUserList() {
//        return BaseAPI.getInstance().getHttpInterface(BaseUrl).getUserList();
//    }
//
//    public static Call<HttpResponse<String>> uploadImage(File file) {
//        MultipartBody.Part fileBody = null;
//        if (file != null) {
//            RequestBody requestBody = RequestBody.create(MediaType.parse("multipart/form-data"), file);
//            fileBody = MultipartBody.Part.createFormData("file", file.getName(), requestBody);
//        }
//        return BaseAPI.getInstance().getHttpInterface(BaseUrl).uploadImage(fileBody);
//    }
//
//    public static Call<HttpResponse<Object>> uploadAttendance(int userId, int status, int direction,
//                                                              String attendanceTime, String address,
//                                                              int type, String url) {
//        return BaseAPI.getInstance().getHttpInterface(BaseUrl).uploadAttendance(userId, status, direction,
//                AppConfig.DeviceId, attendanceTime, address, type, url);
//    }
//
//    public static HttpResponse<Object> exceptionReport(long userId, String errorMsg) {
//        try {
//            Response<HttpResponse<Object>> execute = BaseAPI.getInstance().
//                    getHttpInterface(BaseUrl).exceptionReport(userId, errorMsg).execute();
//            return execute.body();
//        } catch (Exception e) {
//            e.printStackTrace();
//            return new HttpResponse<>("-90", "" + e, null);
//        }
//    }
//
//    public static HttpResponse<String> uploadFinger(int userId, @IntRange(from = 0, to = 9) int location, File file) {
//        String remotePath;
//        try {
//            Response<HttpResponse<String>> execute = uploadImage(file).execute();
//            HttpResponse<String> body = execute.body();
//            if (!HttpResponse.isSuccess(body)) {
//                return body;
//            } else {
//                remotePath = body.result;
//            }
//        } catch (Exception e) {
//            e.printStackTrace();
//            return HttpResponse.createFail("-90", "" + e);
//        }
//        if (TextUtils.isEmpty(remotePath)) {
//            return HttpResponse.createFail("-91", "上传图片成功，但未返回URL");
//        }
//
//        try {
//            Response<HttpResponse<Object>> execute = BaseAPI.getInstance().getHttpInterface(BaseUrl).uploadFinger(userId, location, remotePath).execute();
//            HttpResponse<Object> body = execute.body();
//            if (!HttpResponse.isSuccess(body)) {
//                return HttpResponse.createSuccess(remotePath);
//            } else {
//                return HttpResponse.createFail(body.code,body.message);
//            }
//        } catch (Exception e) {
//            e.printStackTrace();
//            return HttpResponse.createFail("-93", "上传指纹失败，" + e);
//        }
//    }
//
//
//}
