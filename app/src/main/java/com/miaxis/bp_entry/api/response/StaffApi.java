package com.miaxis.bp_entry.api.response;

import com.miaxis.bp_entry.api.BaseAPI;
import com.miaxis.bp_entry.api.DeleteRequest;
import com.miaxis.bp_entry.data.entity.Staff;

import java.util.List;

import retrofit2.Call;

/**
 * @author ZJL
 * @date 2022/4/1 13:37
 * @des
 * @updateAuthor
 * @updateDes
 */
public class StaffApi extends BaseAPI {
    public static Call<StaffResponse<String>> addStaff(Staff staff){
        return getStaffNet().addStaff(staff);
    }

    public static Call<StaffResponse<String>> delStaff(DeleteRequest deleteRequest){
        return getStaffNet().delete(deleteRequest);
    }

    public static Call<StaffResponse<String>> updateList(List<Staff> list){
        return getStaffNet().addlist(list);
    }

    public static Call<StaffResponse<String>> updateStaff(Staff staff){
        return getStaffNet().updateStaff(staff);
    }
}
