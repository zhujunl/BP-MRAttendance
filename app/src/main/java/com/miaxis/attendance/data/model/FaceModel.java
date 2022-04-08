package com.miaxis.attendance.data.model;


import android.text.TextUtils;

import com.miaxis.attendance.data.AppDataBase;
import com.miaxis.attendance.data.entity.Face;
import com.miaxis.common.utils.ListUtils;

import java.util.ArrayList;
import java.util.List;

import timber.log.Timber;

public class FaceModel {

//    private static final ConcurrentHashMap<String, Face> FaceMapCache = new ConcurrentHashMap<>();
    private static List<Face> faceList=new ArrayList<>();
    //private static final CopyOnWriteArrayList<Face> FaceCache = new CopyOnWriteArrayList<>();

    public static void init() {
        List<Face> all = AppDataBase.getInstance().FaceDao().findAll();
        if (ListUtils.isNullOrEmpty(all)) {
            return;
        }
        faceList=all;
//        for (Face face : all) {
//            if (face != null && !TextUtils.isEmpty(face.Code)) {
//                Face put = FaceModel.FaceMapCache.put(face.Code, face);
//                if (put != null) {
//                    long delete = AppDataBase.getInstance().FaceDao().delete(put);
//                }
//            }
//        }
    }

    public static long insert(Face face) {
        if (Face.isIllegal(face)) {
            return -99;
        }
        long insert = AppDataBase.getInstance().FaceDao().insert(face);
        faceList.add(face);
//        if (insert > 0) {
//            Face put = FaceModel.FaceMapCache.put(face.Code, face);
//            if (put != null) {
//                long delete = AppDataBase.getInstance().FaceDao().delete(put);
//            }
//        }
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
        for (Face f  : faceList) {
        	if (f.Code.equals(face.Code)&&f.placeId.equals(face.placeId)){
        	    f=face;
            }
        }
//        if (update > 0) {
//            Face put = FaceModel.FaceMapCache.put(face.Code, face);
//            if (put != null) {
//                long delete = AppDataBase.getInstance().FaceDao().delete(put);
//            }
//        }
        return update;
    }

    public static long delete(Face face) {
        Timber.e("FaceModel Delete face:%s", face);
        if (Face.isIllegal(face)) {
            return -99;
        }
        long delete = AppDataBase.getInstance().FaceDao().delete(face);
        if (delete > 0) {
            faceList.remove(face);
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

    public static long delete(String userId,String place) {
        Timber.e("FaceModel Delete userId:%s", userId);
        if (TextUtils.isEmpty(userId)) {
            return -99;
        }
        long delete = AppDataBase.getInstance().FaceDao().delete(userId,place);
        if (delete > 0) {
            for (Face face  : faceList) {
            	if(face.Code.equals(userId)&&face.placeId.equals(place)){
            	    faceList.remove(face);
            	    break;
                }
            }
        }
        return delete;
    }

    //public static long delete(long id) {
    //  return AppDataBase.getInstance().FaceDao().delete(id);
    //}

    public static void deleteAll() {
        faceList.clear();
        AppDataBase.getInstance().FaceDao().deleteAll();
    }

    public static List<Face> findAll() {
        return faceList;
    }

    public static long allCounts() {
        //return AppDataBase.getInstance().FaceDao().allCounts();
        return faceList.size();
    }

    public static Face findByUserID(String userId,String place) {
        if (TextUtils.isEmpty(userId)) {
            return null;
        }
        return AppDataBase.getInstance().FaceDao().findByUserID(userId,place);
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
