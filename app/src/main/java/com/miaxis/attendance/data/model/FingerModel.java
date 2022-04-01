package com.miaxis.attendance.data.model;


import com.miaxis.attendance.data.AppDataBase;
import com.miaxis.attendance.data.entity.Finger;
import com.miaxis.common.utils.ListUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class FingerModel {

    private static final ConcurrentHashMap<Long, Finger> FingerMapCache = new ConcurrentHashMap<>();
    //    private static final ConcurrentHashMap<String,Finger> FingerMapCache = new ConcurrentHashMap<>();

    public static void init() {
        List<Finger> all = AppDataBase.getInstance().FingerDao().findAll();
        if (ListUtils.isNullOrEmpty(all)) {
            return;
        }
        for (Finger finger : all) {
            if (finger != null && !finger.isIllegal()) {
                Finger old = FingerModel.FingerMapCache.put(finger.id, finger);
                //                if (old != null) {
                //                    long delete = AppDataBase.getInstance().FingerDao().delete(old);
                //                }
            }
        }
    }

    public static long insert(Finger finger) {
        if (Finger.isIllegal(finger)) {
            return -99;
        }
        //        Finger old = findByUserIDAndPosition(finger.UserId, finger.Position);
        //        if (old != null) {//该用户已经存在相同位置指纹数据
        //            //判断指纹数据是否需要更新
        //            //需要更新指纹数据
        //            if (old.fingerImageId != finger.fingerImageId) {
        //                delete(old);//删除原有数据
        //            } else {//不需要更新
        //                return old.id;
        //            }
        //        }
        //        long insert = AppDataBase.getInstance().FingerDao().insert(finger);
        //        if (insert > 0) {
        //            finger.id = insert;
        //            FingerModel.FingerMapCache.put(finger.id, finger);
        //        }
        //        return insert;


        Finger old = findByUserIDAndPosition(finger.UserId, finger.Position);
        if (old != null) {
            delete(old);
        }
        long insert = AppDataBase.getInstance().FingerDao().insert(finger);
        if (insert > 0) {
            finger.id = insert;
            FingerModel.FingerMapCache.put(finger.id, finger);
            //            Finger old = FingerModel.FingerMapCache.put(finger.id, finger);
            //            if (old != null) {
            //                long delete = AppDataBase.getInstance().FingerDao().delete(old);
            //            }
        }
        return insert;
    }

    //    private static long update(Finger finger) {
    //        if (Finger.isIllegal(finger)) {
    //            return -99;
    //        }
    //        int update = AppDataBase.getInstance().FingerDao().update(finger);
    //        if (update > 0) {
    //            Finger old = FingerModel.FingerMapCache.put(finger.id, finger);
    //        }
    //        return update;
    //    }

    public static void delete(Finger finger) {
        if (finger != null) {
            delete(finger.id);
        }
    }

    public static int delete(String userId) {
        int delete = AppDataBase.getInstance().FingerDao().delete(userId);
        if (userId != null) {
            List<Finger> byUserID = findByUserID(userId);
            for (Finger finger : byUserID) {
                delete(finger);
            }
        }
        return delete;
    }

    public static void delete(List<Finger> fingers) {
        if (!ListUtils.isNullOrEmpty(fingers)) {
            for (Finger finger : fingers) {
                if (finger != null) {
                    delete(finger);
                }
            }
        }
    }

    private static int delete(long id) {
        AppDataBase.getInstance().FingerDao().delete(id);
        FingerModel.FingerMapCache.remove(id);
        return 1;
    }

    public static void deleteAll() {
        FingerModel.FingerMapCache.clear();
        AppDataBase.getInstance().FingerDao().deleteAll();
    }

    public static HashMap<Long, Finger> findAll() {
        return new HashMap<>(FingerModel.FingerMapCache);
    }

    public static int allCounts() {
        //return AppDataBase.getInstance().FingerDao().allCounts();
        return FingerModel.FingerMapCache.size();
    }

    public static List<Finger> findByUserID(String userId) {
        //return AppDataBase.getInstance().FingerDao().findByUserID(userId);
        List<Finger> list = new ArrayList<>();
        Set<Map.Entry<Long, Finger>> entries = FingerModel.FingerMapCache.entrySet();
        for (Map.Entry<Long, Finger> entry : entries) {
            if (entry.getValue() != null
                    && entry.getValue().UserId != null
                    && entry.getValue().UserId.equals(userId)) {
                list.add(entry.getValue());
            }
        }
        return list;
    }

    //    public static Finger findByUser(String userId) {
    //        List<Finger> userID = AppDataBase.getInstance().FingerDao().findByUserID(userId);
    //        if (ListUtils.isNullOrEmpty(userID)) {
    //
    //        }
    //
    //    }

    public static Finger findByID(long id) {
        return FingerModel.FingerMapCache.get(id);
    }

    public static List<Finger> findPage(int pageSize, int offset) {
        return AppDataBase.getInstance().FingerDao().findPage(pageSize, offset);
    }


    public static Finger findByUserIDAndPosition(String userId, int position) {
        List<Finger> byUserIDAndPosition = AppDataBase.getInstance().FingerDao().findByUserIDAndPosition(userId, position);
        if (ListUtils.isNullOrEmpty(byUserIDAndPosition)) {
            return null;
        }
        return byUserIDAndPosition.get(0);
    }

}
