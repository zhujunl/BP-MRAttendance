package com.miaxis.attendance.data.model;


import com.miaxis.attendance.data.AppDataBase;
import com.miaxis.attendance.data.entity.Attendance;

import java.util.List;

public class AttendanceModel {

    public static long insert(Attendance attendance) {
        long insert = AppDataBase.getInstance().AttendanceDao().insert(attendance);
        attendance.id = insert;
        return insert;
    }

    public static void update(Attendance attendance) {
        AppDataBase.getInstance().AttendanceDao().update(attendance);
    }

    public static void delete(Attendance attendance) {
        AppDataBase.getInstance().AttendanceDao().delete(attendance);
    }

    public static void delete(String userId) {
        AppDataBase.getInstance().AttendanceDao().delete(userId);
    }

    public static void deleteAll() {
        AppDataBase.getInstance().AttendanceDao().deleteAll();
    }

    public static List<Attendance> findAll() {
        return AppDataBase.getInstance().AttendanceDao().findAll();
    }

    public static List<Attendance> findNoUpload() {
        return AppDataBase.getInstance().AttendanceDao().findNoUpload();
    }

    public static int allCounts() {
        return AppDataBase.getInstance().AttendanceDao().allCounts();
    }

    public static List<Attendance> findByUserID(String userID) {
        return AppDataBase.getInstance().AttendanceDao().findByUserID(userID);
    }

    public static List<Attendance> findPage(int pageSize, int offset) {
        return AppDataBase.getInstance().AttendanceDao().findPage(pageSize, offset);
    }

}
