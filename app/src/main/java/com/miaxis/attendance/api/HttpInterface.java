package com.miaxis.attendance.api;


import com.miaxis.attendance.api.bean.UserBean;

import java.util.List;

import androidx.annotation.IntRange;
import okhttp3.MultipartBody;
import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.Part;

public interface HttpInterface {

    /**
     * 获取用户列表
     */
    @GET("api/app/user/list")
    Call<HttpResponse<List<UserBean>>> getUserList();


    /**
     * 上传图片
     *
     * @return 图片地址
     */
    @Multipart
    @POST("api/file/upload")
    Call<HttpResponse<String>> uploadImage(
            @Part MultipartBody.Part file
    );

    /**
     * 上传考勤记录
     *
     * @param status 识别状态 0-成功 1-失败
     * @param type   通行类型（0-面部识别 1-指纹识别）
     * @param url    图片地址
     */
    @Multipart
    @POST("api/app/attendance/add")
    Call<HttpResponse<Object>> uploadAttendance(@Part("userId") long userId,
                                                @Part("status") int status,
                                                @Part("direction") int direction,
                                                @Part("attendanceDeviceId") int attendanceDeviceId,
                                                @Part("attendanceTime") String attendanceTime,
                                                @Part("address") String address,
                                                @Part("type") int type,
                                                @Part("url") String url
    );

    /**
     * 上传指纹信息
     *
     * @param userId   用户ID
     * @param location 手指位置
     * @param url      图片地址
     */
    @Multipart
    @POST("api/app/user/inputFinger")
    Call<HttpResponse<Object>> uploadFinger(@Part("userId") long userId,
                                            @Part("location") @IntRange(from = 0, to = 9) int location,
                                            @Part("url") String url
    );


    /**
     * 上传错误信息
     *
     * @param userId 用户ID
     * @param reason 错误原因
     */
    @Multipart
    @POST("api/app/user/exceptionReport")
    Call<HttpResponse<Object>> exceptionReport(@Part("userId") long userId,
                                               @Part("reason") String reason
    );
}
