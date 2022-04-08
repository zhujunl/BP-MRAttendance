package com.miaxis.bp_entry.data.entity;

import com.miaxis.bp_entry.data.dao.AppDatabase;

import java.util.List;

/**
 * @author ZJL
 * @date 2022/3/31 9:33
 * @des
 * @updateAuthor
 * @updateDes
 */
public class StaffModel {
    public static void save(Staff staff){
        AppDatabase.getInstance().staffDao().insert(staff);
    }

    public static void delete(String palce,String code){
        AppDatabase.getInstance().staffDao().DeleteStaff(palce,code);
    }

    public static List<Staff> queryAll(){
        return AppDatabase.getInstance().staffDao().QueryStaffList();
    }

    public static Staff queryStaff(long id){
        return AppDatabase.getInstance().staffDao().QueryStaff(id);
    }

    public static Staff queryStaff(String place,String code){
        return AppDatabase.getInstance().staffDao().QueryStaff(place,code);
    }
    public static int modifyStaff(Staff staff){
        return AppDatabase.getInstance().staffDao().modifyStaff(staff);
    }
}
