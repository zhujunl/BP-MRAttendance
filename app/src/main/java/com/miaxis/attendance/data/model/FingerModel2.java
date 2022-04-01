package com.miaxis.attendance.data.model;


import com.miaxis.attendance.data.entity.Finger;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

public class FingerModel2 {

    //
    //                                      用户ID                      指纹信息
    private static final ConcurrentHashMap<String, CopyOnWriteArrayList<Finger>> FingerMapCache = new ConcurrentHashMap<>();

//    public static void init() {
//        List<Finger> all = AppDataBase.getInstance().FingerDao().findAll();
//        if (ListUtils.isNullOrEmpty(all)) {
//            FingerModel2.FingerMapCache.clear();
//            return;
//        }
//        ConcurrentHashMap<String, LinkedList<Finger>> mapCache = new ConcurrentHashMap<>();
//        for (Finger finger : all) {
//            if (finger != null && !TextUtils.isEmpty(finger.UserId)) {
//                LinkedList<Finger> list = mapCache.computeIfAbsent(finger.UserId, k -> new LinkedList<>());
//                list.addLast(finger);
//            }
//        }
//        mapCache.forEach((key, linkedList) -> {
//            CopyOnWriteArrayList<Finger> list = new CopyOnWriteArrayList<>(linkedList);
//            CopyOnWriteArrayList<Finger> put = FingerModel2.FingerMapCache.put(key, list);
//            if (!ListUtils.isNullOrEmpty(put)) {
//                for (Finger finger : put) {
//                    deleteFromDb(finger);
//                }
//            }
//        });
//    }

//    public static long insert(Finger finger) {
//        if (Finger.isIllegal(finger)) {
//            return -99;
//        }
//        long insert = AppDataBase.getInstance().FingerDao().insert(finger);
//        if (insert > 0) {
//            CopyOnWriteArrayList<Finger> list = FingerModel2.FingerMapCache.get(finger.UserId);
//            if (list == null) {
//                list = new CopyOnWriteArrayList<>();
//            }
//            list.add(finger);
//            FingerModel2.FingerMapCache.put(finger.UserId, list);
//        }
//        finger.id = insert;
//        return insert;
//    }
//
//    public static long update(Finger finger) {
//        if (Finger.isIllegal(finger)) {
//            return -99;
//        }
//        int update = AppDataBase.getInstance().FingerDao().update(finger);
//        if (update > 0) {
//            CopyOnWriteArrayList<Finger> fingers = FingerModel2.FingerMapCache.get(finger.UserId);
//
//            Finger put = FingerModel2.FingerMapCache.put(finger.UserId, finger);
//            if (put != null) {
//                long delete = AppDataBase.getInstance().FingerDao().delete(put);
//            }
//        }
//        return update;
//    }
//
//    private static long deleteCache(String userId) {
//        if (userId == null) {
//            return -99;
//        }
//        Finger put = FingerModel2.FingerMapCache.remove(userId);
//        if (put != null) {
//            long delete = AppDataBase.getInstance().FingerDao().delete(put);
//        }
//        return put == null ? 0 : 1;
//    }
//
//    private static void deleteFromDb(Finger finger) {
//        int delete = AppDataBase.getInstance().FingerDao().delete(finger);
//    }
//
//    public static int deleteFromDb(String userId) {
//        int delete = AppDataBase.getInstance().FingerDao().delete(userId);
//        if (userId != null) {
//            FingerModel2.FingerMapCache.remove(userId);
//        }
//        return delete;
//    }
//
//    public static void deleteFromDb(List<Finger> fingers) {
//        if (!ListUtils.isNullOrEmpty(fingers)) {
//            for (Finger finger : fingers) {
//                deleteFromDb(finger);
//                if (finger.UserId != null) {
//                    FingerModel2.FingerMapCache.remove(finger.UserId);
//                }
//            }
//        }
//    }
//
//    //public static int delete(long id) {
//    //    return AppDataBase.getInstance().FingerDao().delete(id);
//    //}
//
//    public static void deleteAll() {
//        FingerModel2.FingerMapCache.clear();
//        AppDataBase.getInstance().FingerDao().deleteAll();
//    }
//
//    public static HashMap<String, Finger> findAll() {
//        return new HashMap<>(FingerModel2.FingerMapCache);
//    }
//
//    public static int allCounts() {
//        //return AppDataBase.getInstance().FingerDao().allCounts();
//        return FingerModel2.FingerMapCache.size();
//    }
//
//    public static Finger findByUserID(String userId) {
//        //return AppDataBase.getInstance().FingerDao().findByUserID(userId);
//        if (TextUtils.isEmpty(userId)) {
//            return null;
//        }
//        return FingerModel2.FingerMapCache.get(userId);
//    }
//
//    public static List<Finger> findPage(int pageSize, int offset) {
//        return AppDataBase.getInstance().FingerDao().findPage(pageSize, offset);
//    }

}
