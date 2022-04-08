package com.miaxis.attendance.service.transform;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.text.TextUtils;
import android.util.Log;

import com.miaxis.attendance.config.AppConfig;
import com.miaxis.attendance.data.entity.Face;
import com.miaxis.attendance.data.entity.Finger;
import com.miaxis.attendance.data.entity.LocalImage;
import com.miaxis.attendance.data.entity.Staff;
import com.miaxis.attendance.data.model.FaceModel;
import com.miaxis.attendance.data.model.FingerModel;
import com.miaxis.attendance.data.model.LocalImageModel;
import com.miaxis.attendance.data.model.StaffModel;
import com.miaxis.attendance.service.MxResponse;
import com.miaxis.attendance.service.MxResponseCode;
import com.miaxis.attendance.service.bean.DeleteBean;
import com.miaxis.attendance.service.bean.StaffBean;
import com.miaxis.attendance.service.bean.User;
import com.miaxis.attendance.ui.finger.MR990FingerStrategy;
import com.miaxis.common.utils.BitmapUtils;
import com.miaxis.common.utils.FileUtils;
import com.miaxis.common.utils.ListUtils;
import com.miaxis.common.utils.StringUtils;

import org.zz.api.MXFace;
import org.zz.api.MXFaceIdAPI;
import org.zz.api.MXImageToolsAPI;
import org.zz.api.MXResult;
import org.zz.api.MxImage;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;

import timber.log.Timber;

//import com.miaxis.attendance.service.bean.User;

/**
 * @author Tank
 * @date 2021/8/23 7:24 下午
 * @des
 * @updateAuthor
 * @updateDes
 */
public class PersonTransform {

    private static final String TAG = "PersonTransform";

    private static final Random RANDOM = new Random();
    //添加
    public static MxResponse<?> insert(StaffBean staffbean) {
        if (staffbean==null){
            return MxResponse.CreateFail(MxResponseCode.CODE_ILLEGAL_PARAMETER, MxResponseCode.MSG_ILLEGAL_PARAMETER);
        }
        Staff staff=new Staff();
        staff.setPlace(staffbean.getPlace());
        staff.setCode(staffbean.getCode());
//        processFace(staffbean.getCode(),staffbean.getPlace(),staffbean.getFaceFeature());
        staff.setFaceFeature(staffbean.getFaceFeature());
        staff.setFinger0(staffbean.getFinger0());
        staff.setFinger1(staffbean.getFinger1());
        long index=StaffModel.insert(staff);
        if(index<0){
            StaffModel.delete(staff);
            return MxResponse.CreateFail(MxResponseCode.CODE_OPERATION_FAILED, "insert person failed");
        }
        return MxResponse.CreateSuccess();
    }

    private static MxResponse<byte[]> doFaceProcess(String facePath) {
        MXResult<MxImage> imageLoad = MXImageToolsAPI.getInstance().ImageLoad(facePath, 3);
        if (!MXResult.isSuccess(imageLoad)) {
            return MxResponse.CreateFail(imageLoad.getCode(), imageLoad.getMsg());
        }
        MxImage image = imageLoad.getData();
        byte[] decode = image.buffer;
        MXResult<List<MXFace>> mxResult = MXFaceIdAPI.getInstance().mxDetectFace(decode, image.width, image.height);
        if (!MXResult.isSuccess(mxResult)) {
            return MxResponse.CreateFail(mxResult.getCode(), mxResult.getMsg());
        }
        MXFace maxFace = MXFaceIdAPI.getInstance().getMaxFace(mxResult.getData());
        if(maxFace==null){
            return MxResponse.CreateFail(mxResult.getCode(), mxResult.getMsg());
        }
        MXResult<Integer> faceQuality = MXFaceIdAPI.getInstance().mxFaceQuality(decode, image.width, image.height, maxFace);
        if (!MXResult.isSuccess(faceQuality)) {
            return MxResponse.CreateFail(faceQuality.getCode(), faceQuality.getMsg());
        }
        if (faceQuality.getData() < MXFaceIdAPI.getInstance().FaceQuality) {
            return MxResponse.CreateFail(MxResponseCode.Code_Illegal_Image_Face, "质量过低,当前：" + faceQuality.getData()+",低于"+MXFaceIdAPI.getInstance().FaceQuality);
        }
        MXResult<byte[]> featureExtract = MXFaceIdAPI.getInstance().mxFeatureExtract(decode, image.width, image.height, maxFace);
        if (!MXResult.isSuccess(featureExtract)) {
            return MxResponse.CreateFail(featureExtract.getCode(), featureExtract.getMsg());
        }
        return MxResponse.CreateSuccess(featureExtract.getData());
    }

    private static MxResponse<LocalImage> doImageProcess(boolean faceMod, String remoteUrl) {
            String savePath = (faceMod ? (AppConfig.Path_FaceImage + "face_") : (AppConfig.Path_FingerImage + "finger_")) + System.currentTimeMillis() + "_" + Math.abs(RANDOM.nextInt()) + ".jpeg";
            Bitmap bitmap= StringUtils.stringToBitmap(remoteUrl);//这个可以后期删除好像跟下面重复了？
            BitmapUtils.saveBitmap(bitmap,savePath);
            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inJustDecodeBounds = true;
            BitmapFactory.decodeFile(savePath, options);
            if (options.outWidth <= 0 || options.outHeight <= 0) {
                FileUtils.delete(savePath);
                return MxResponse.CreateFail(faceMod ? MxResponseCode.Code_Illegal_Image_Face : MxResponseCode.Code_Illegal_Image_Finger,
                        faceMod ? MxResponseCode.Msg_Illegal_Image_Face : MxResponseCode.Msg_Illegal_Image_Finger);
            }

            LocalImage localImage;
                localImage = new LocalImage();
            localImage.LocalPath = savePath;
            localImage.id = LocalImageModel.insert(localImage);
            if (localImage.id <= 0) {
                FileUtils.delete(savePath);
                return MxResponse.CreateFail(MxResponseCode.CODE_OPERATION_ERROR, "insert image failed");
            }
            return MxResponse.CreateSuccess(localImage);
    }

    private static MxResponse<Face> processFace(String code, String place,String url_face) {
        if (StringUtils.isNullOrEmpty(code)) {
            return MxResponse.CreateFail(MxResponseCode.CODE_OPERATION_ERROR, "user id error");
        }
        if (StringUtils.isNullOrEmpty(url_face)) {
            return MxResponse.CreateFail(MxResponseCode.CODE_OPERATION_ERROR, "image url can not be null or empty");
        }
        MxResponse<LocalImage> doImageProcess = doImageProcess(true, url_face);
        if (!MxResponse.isSuccess(doImageProcess)) {
            return MxResponse.CreateFail(doImageProcess.getCode(), doImageProcess.getMessage());
        }
        LocalImage faceImage = doImageProcess.getData();
        Face temp = FaceModel.findByUserID(code,place);
        Timber.e("processFace  find local Face:%s", temp);
        Face face = temp == null ? new Face() : temp;
        if (face.id <= 0 || face.faceImageId != faceImage.id) {
            MxResponse<byte[]> featureExtract = doFaceProcess(faceImage.LocalPath);
            if (!MxResponse.isSuccess(featureExtract)) {
                return MxResponse.CreateFail(featureExtract);
            }
            face.faceImageId = faceImage.id;
            face.Code = code;
            face.placeId=place;
            face.FaceFeature = featureExtract.getData();
            face.id = FaceModel.insert(face);
            Timber.e("processFace insert or update Face:%s", face);
            if (face.id <= 0) {
                return MxResponse.CreateFail(MxResponseCode.CODE_OPERATION_ERROR, "insert face failed");
            }
        }
        return MxResponse.CreateSuccess(face);
    }

    /**
     * @return 返回指纹ID列表
     */
    private static MxResponse<List<Long>> processFingers(String userId, List<User.Finger> url_fingers) {
        if (StringUtils.isNullOrEmpty(userId)) {
            return MxResponse.CreateFail(MxResponseCode.CODE_OPERATION_ERROR, "user id error");
        }
        List<Long> list = new ArrayList<>();
        List<Finger> fingers = FingerModel.findByUserID(userId);
        Timber.e("processFingers  find local fingers:%s", fingers);
        if (ListUtils.isNullOrEmpty(url_fingers)) {
            for (Finger finger : fingers) {
                FingerModel.delete(finger);
                LocalImageModel.delete(finger.fingerImageId);
            }
            return MxResponse.CreateSuccess(list);
        } else {
            //下载所有指纹图片
            HashMap<Long, LocalImage> downloadMap = new HashMap<>();
            for (User.Finger fingerBean : url_fingers) {
                if (fingerBean.isIllegal()) {
                    return MxResponse.CreateFail(MxResponseCode.CODE_OPERATION_ERROR, "finger data error");
                }
                MxResponse<LocalImage> doImageProcess = doImageProcess(false, fingerBean.url);
                if (!MxResponse.isSuccess(doImageProcess)) {
                    return MxResponse.CreateFail(doImageProcess.getCode(), doImageProcess.getMessage());
                }
                LocalImage data = doImageProcess.getData();
                downloadMap.put(data.id, data);
            }
            //封装指纹数据
            HashMap<Long, Finger> fingerMap = new HashMap<>();
            for (Finger finger : fingers) {
                fingerMap.put(finger.id, finger);
            }
            Timber.e("processFingers  need update fingerMap:%s", fingerMap);
            //从下载的指纹图片中查找已有指纹图片ID,如果本地指纹图片ID不在下载的图片中，则说明该指纹需要被删除
            Iterator<Map.Entry<Long, Finger>> iterator = fingerMap.entrySet().iterator();
            while (iterator.hasNext()) {
                Map.Entry<Long, Finger> next = iterator.next();
                Timber.e("processFingers   next:%s", next);
                Finger finger = next.getValue();
                if (finger != null) {
                    LocalImage localImage = downloadMap.get(finger.fingerImageId);
                    if (localImage == null) {//说明指纹需要被删除
                        Timber.e("processFingers   delete:%s", finger);
                        FingerModel.delete(finger);
                        LocalImageModel.delete(finger.fingerImageId);
                        iterator.remove();
                    }
                }
            }

            //从下载的指纹图片中查找没有提取过指纹特征的图片，然后提取特征
            Set<Map.Entry<Long, LocalImage>> downloadEntries = downloadMap.entrySet();
            for (Map.Entry<Long, LocalImage> entry : downloadEntries) {
                Long key = entry.getKey();
                Finger finger = fingerMap.get(key);
                if (finger == null) {
                    LocalImage localImage = entry.getValue();
                    MXResult<MxImage> imageLoad = MXImageToolsAPI.getInstance().ImageLoad(localImage.LocalPath, 1);
                    if (!MXResult.isSuccess(imageLoad)) {
                        return MxResponse.CreateFail(imageLoad.getCode(), imageLoad.getMsg());
                    }
                    MxImage fingerImage = imageLoad.getData();
                    MXResult<byte[]> extractFeature = MR990FingerStrategy.getInstance().extractFeature(fingerImage.buffer, fingerImage.width, fingerImage.height);
                    if (!MXResult.isSuccess(extractFeature)) {
                        return MxResponse.CreateFail(extractFeature.getCode(), extractFeature.getMsg());
                    }
                    finger = new Finger();
                    finger.UserId = userId;
//                    finger.Position = getPositionFromList(localImage.RemotePath, url_fingers);
                    finger.fingerImageId = localImage.id;
                    finger.FingerFeature = extractFeature.getData();
                    finger.id = FingerModel.insert(finger);
                    if (finger.id <= 0) {
                        return MxResponse.CreateFail(MxResponseCode.CODE_OPERATION_ERROR, "insert finger failed");
                    }
                }
                list.add(finger.id);
            }
        }
        return MxResponse.CreateSuccess(list);
    }

//    public static MxResponse<?> update(User user) {
//        if (user == null || user.isIllegal()) {
//            return MxResponse.CreateFail(MxResponseCode.CODE_ILLEGAL_PARAMETER, MxResponseCode.MSG_ILLEGAL_PARAMETER);
//        }
//        String userId = String.valueOf(user.id);
//        Person person = PersonModel.findByUserID(userId);
//        if (person == null) {
//            return MxResponse.CreateFail(MxResponseCode.CODE_OPERATION_ERROR, "not exists");
//        }
//        MxResponse<Face> faceMxResponse = processFace(userId, user.url_face);
//        if (!MxResponse.isSuccess(faceMxResponse)) {
//            return faceMxResponse;
//        }
//        MxResponse<List<Long>> listMxResponse = processFingers(userId, user.getUrl_fingers());
//        if (!MxResponse.isSuccess(listMxResponse)) {
//            return listMxResponse;
//        }
//        List<Long> list = new ArrayList<>();
//        list.add(faceMxResponse.getData().faceImageId);
//        person.UserId = userId;
//        person.faceIds = list;
//        person.fingerIds = listMxResponse.getData();
//        person.IdCardNumber = user.id_number;
//        person.Number = user.id_number;
//        person.Name = user.name;
//        person.id = PersonModel.insert(person);
//        if (person.id <= 0) {
//            PersonModel.delete(person);
//            FaceModel.delete(faceMxResponse.getData());
//            return MxResponse.CreateFail(MxResponseCode.CODE_OPERATION_FAILED, "insert person failed");
//        }
//        return MxResponse.CreateSuccess();
//    }
//
    //删除
    public static MxResponse<?> delete(DeleteBean user) {
        if (user == null) {
            return MxResponse.CreateFail(MxResponseCode.CODE_ILLEGAL_PARAMETER, MxResponseCode.MSG_ILLEGAL_PARAMETER);
        }
        int index=StaffModel.delete(user);
        Log.e("delete:","user="+user.toString());
        Log.e("delete:","index="+index);
        return MxResponse.CreateSuccess();
    }

    public static MxResponse<?> updateList(List<StaffBean> staffBeans){
        for (StaffBean   staffBean: staffBeans) {
        	Staff staff=StaffModel.findStaffByCode(staffBean.getCode(),staffBean.getPlace());
        	if (staff==null){
        	    insert(staffBean);
            }else {
                staff.setFaceFeature(staffBean.getFaceFeature());
                staff.setFinger0(staffBean.getFinger0());
                staff.setFinger1(staffBean.getFinger1());
                staff.setUpdate_time(System.currentTimeMillis());
                int index=StaffModel.updateStaff(staff);
        	}
        }

        return MxResponse.CreateSuccess();
    }
//
//    public static MxResponse<?> insertOrUpdate(User user) {
//        if (user == null || user.isIllegal()) {
//            return MxResponse.CreateFail(MxResponseCode.CODE_ILLEGAL_PARAMETER, MxResponseCode.MSG_ILLEGAL_PARAMETER);
//        }
//        Timber.e("insertOrUpdate   User:%s", user);
//        String userId = String.valueOf(user.id);
//        Person person = PersonModel.findByUserID(userId);
//        if (person == null) {
//            person = new Person();
//        }
//        person.UserId = userId;
//        person.IdCardNumber = user.id_number;
//        person.Number = user.id_number;
//        person.Name = user.name;
//        MxResponse<Face> faceMxResponse = processFace(userId, user.url_face);
//        if (!MxResponse.isSuccess(faceMxResponse)) {
//            return faceMxResponse;
//        }
//        MxResponse<List<Long>> listMxResponse = processFingers(userId, user.getUrl_fingers());
//        if (!MxResponse.isSuccess(listMxResponse)) {
//            return listMxResponse;
//        }
//        List<Long> list = new ArrayList<>();
//        list.add(faceMxResponse.getData().id);
//        person.faceIds = list;
//        person.fingerIds = listMxResponse.getData();
//        person.id = PersonModel.insert(person);
//        if (person.id <= 0) {
//            PersonModel.delete(person);
//            FaceModel.delete(faceMxResponse.getData());
//            return MxResponse.CreateFail(MxResponseCode.CODE_OPERATION_FAILED, "insert person failed");
//        }
//        return MxResponse.CreateSuccess(person.id);
//    }


    public static MxResponse<?> update(StaffBean staffBean){
        String code=staffBean.getCode();
        String place=staffBean.getPlace();
        Staff staff=StaffModel.findStaffByCode(code,place);
        if (staff==null){
           return insert(staffBean);
        }
        staff.setFaceFeature(staffBean.getFaceFeature());
        staff.setFinger0(staffBean.getFinger0());
        staff.setFinger1(staffBean.getFinger1());
        staff.setUpdate_time(System.currentTimeMillis());
        int index=StaffModel.updateStaff(staff);
        Log.e("Update:%s",""+index);
        return MxResponse.CreateSuccess();
    }

    private static int getPositionFromList(String url, List<User.Finger> url_fingers) {
        if (ListUtils.isNullOrEmpty(url_fingers) || TextUtils.isEmpty(url)) {
            return -1;
        }
        for (User.Finger finger : url_fingers) {
            if (url.equals(finger.url)) {
                return finger.location;
            }
        }
        return -2;
    }
}
