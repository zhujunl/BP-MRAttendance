package com.miaxis.bp_entry.api;

import com.miaxis.bp_entry.api.response.StaffResponse;
import com.miaxis.bp_entry.data.entity.Staff;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.POST;

/**
 * @author ZJL
 * @date 2022/4/1 11:26
 * @des
 * @updateAuthor
 * @updateDes
 */
public interface StaffNet {
    @POST("/api/user/add")
    Call<StaffResponse<String>> addStaff(@Body Staff staff);

    @POST("/api/user/delete")
    Call<StaffResponse<String>> delete(@Body DeleteRequest deleteRequest);


    @POST("/api/user/addlist")
    Call<StaffResponse<String>> addlist(@Body List<Staff> list);

    @POST("/api/user/update")
    Call<StaffResponse<String>> updateStaff(@Body Staff staff);

}
