package com.miaxis.attendance.data.model;


import android.text.TextUtils;

import com.miaxis.attendance.data.AppDataBase;
import com.miaxis.attendance.data.entity.Face;
import com.miaxis.common.utils.ListUtils;

import java.util.HashMap;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

import timber.log.Timber;

public class FaceModel {

    private static final ConcurrentHashMap<String, Face> FaceMapCache = new ConcurrentHashMap<>();
    //private static final CopyOnWriteArrayList<Face> FaceCache = new CopyOnWriteArrayList<>();

    public static void init() {
        List<Face> all = AppDataBase.getInstance().FaceDao().findAll();
        if (ListUtils.isNullOrEmpty(all)) {
            return;
        }
        for (Face face : all) {
            if (face != null && !TextUtils.isEmpty(face.UserId)) {
                Face put = FaceModel.FaceMapCache.put(face.UserId, face);
                if (put != null) {
                    long delete = AppDataBase.getInstance().FaceDao().delete(put);
                }
            }
        }
    }

    public static long insert(Face face) {
        if (Face.isIllegal(face)) {
            return -99;
        }
        long insert = AppDataBase.getInstance().FaceDao().insert(face);
        if (insert > 0) {
            Face put = FaceModel.FaceMapCache.put(face.UserId, face);
            if (put != null) {
                long delete = AppDataBase.getInstance().FaceDao().delete(put);
            }
        }
        face.id = insert;
        Timber.e("FaceModel insert face:%s", face);
        return insert;
    }

    public static long update(Face face) {
        Timber.e("FaceModel update face:%s", face);
        if (Face.isIllegal(face)) {
            return -99;
        }
        long update = AppDataBase.getInstance().FaceDao().update(face);
        if (update > 0) {
            Face put = FaceModel.FaceMapCache.put(face.UserId, face);
            if (put != null) {
                long delete = AppDataBase.getInstance().FaceDao().delete(put);
            }
        }
        return update;
    }

    public static long delete(Face face) {
        Timber.e("FaceModel Delete face:%s", face);
        if (Face.isIllegal(face)) {
            return -99;
        }
        long delete = AppDataBase.getInstance().FaceDao().delete(face);
        if (delete > 0) {
            FaceModel.FaceMapCache.remove(face.UserId);
        }
        return delete;
    }

    //    public static long delete(List<Long> faceIds) {
    //        if (ListUtils.isNullOrEmpty(faceIds)) {
    //            return -99;
    //        }
    //        for (long faceId : faceIds) {
    //            long delete = AppDataBase.getInstance().FaceDao().delete(faceId);
    //            if (delete > 0) {
    //                FaceModel.FaceMapCache.remove(face.UserId);
    //            }
    //        }
    //        return delete;
    //    }

    public static long delete(String userId) {
        Timber.e("FaceModel Delete userId:%s", userId);
        if (TextUtils.isEmpty(userId)) {
            return -99;
        }
        long delete = AppDataBase.getInstance().FaceDao().delete(userId);
        if (delete > 0) {
            FaceModel.FaceMapCache.remove(userId);
        }
        return delete;
    }

    //public static long delete(long id) {
    //  return AppDataBase.getInstance().FaceDao().delete(id);
    //}

    public static void deleteAll() {
        FaceModel.FaceMapCache.clear();
        AppDataBase.getInstance().FaceDao().deleteAll();
    }

    public static HashMap<String, Face> findAll() {
        return new HashMap<>(FaceModel.FaceMapCache);
    }

    public static long allCounts() {
        //return AppDataBase.getInstance().FaceDao().allCounts();
        return FaceModel.FaceMapCache.size();
    }

    public static Face findByUserID(String userId) {
        if (TextUtils.isEmpty(userId)) {
            return null;
        }
        return FaceModel.FaceMapCache.get(userId);
        //return AppDataBase.getInstance().FaceDao().findByUserID(userID);
    }

    public static Face findByID(long id) {
        //        if (id <= 0) {
        //            return null;
        //        }
        //return FaceModel.FaceMapCache.get(userId);
        List<Face> byID = AppDataBase.getInstance().FaceDao().findByID(id);
        if (ListUtils.isNullOrEmpty(byID)) {
            return null;
        } else {
            return byID.get(0);
        }
    }

    public static List<Face> findPage(int pageSize, int offset) {
        return AppDataBase.getInstance().FaceDao().findPage(pageSize, offset);
    }

}
