package com.miaxis.attendance.service.transform;

import android.graphics.BitmapFactory;
import android.text.TextUtils;

import com.miaxis.attendance.config.AppConfig;
import com.miaxis.attendance.data.entity.Face;
import com.miaxis.attendance.data.entity.Finger;
import com.miaxis.attendance.data.entity.LocalImage;
import com.miaxis.attendance.data.entity.Person;
import com.miaxis.attendance.data.model.FaceModel;
import com.miaxis.attendance.data.model.FingerModel;
import com.miaxis.attendance.data.model.LocalImageModel;
import com.miaxis.attendance.data.model.PersonModel;
import com.miaxis.attendance.service.MxResponse;
import com.miaxis.attendance.service.MxResponseCode;
import com.miaxis.attendance.service.bean.User;
import com.miaxis.attendance.ui.finger.MR990FingerStrategy;
import com.miaxis.common.response.ZZResponse;
import com.miaxis.common.utils.DownloadClient;
import com.miaxis.common.utils.FileUtils;
import com.miaxis.common.utils.ListUtils;
import com.miaxis.common.utils.StringUtils;

import org.zz.api.MXFace;
import org.zz.api.MXFaceIdAPI;
import org.zz.api.MXImageToolsAPI;
import org.zz.api.MXResult;
import org.zz.api.MxImage;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;

import timber.log.Timber;

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

    public static MxResponse<?> insert(User user) {
        if (user == null || user.isIllegal()) {
            return MxResponse.CreateFail(MxResponseCode.CODE_ILLEGAL_PARAMETER, MxResponseCode.MSG_ILLEGAL_PARAMETER);
        }
        String userId = String.valueOf(user.id);
        Person person = PersonModel.findByUserID(userId);
        if (person != null) {
            return MxResponse.CreateFail(MxResponseCode.CODE_OPERATION_ERROR, "already exists");
        }
        person = new Person();
        person.UserId = userId;

        person.IdCardNumber = user.id_number;
        person.Number = user.id_number;
        person.Name = user.name;
        person.id = PersonModel.insert(person);
        if (person.id <= 0) {
            PersonModel.delete(person);
            return MxResponse.CreateFail(MxResponseCode.CODE_OPERATION_FAILED, "insert person failed");
        }
        MxResponse<Face> faceMxResponse = processFace(userId, user.url_face);
        if (!MxResponse.isSuccess(faceMxResponse)) {
            return faceMxResponse;
        }
        MxResponse<List<Long>> listMxResponse = processFingers(userId, user.getUrl_fingers());
        if (!MxResponse.isSuccess(listMxResponse)) {
            return listMxResponse;
        }
        List<Long> list = new ArrayList<>();
        list.add(faceMxResponse.getData().faceImageId);
        person.faceIds = list;
        person.fingerIds = listMxResponse.getData();
        long update = PersonModel.update(person);
        if (update <= 0) {
            PersonModel.delete(person);
            FaceModel.delete(faceMxResponse.getData());
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
        List<LocalImage> byRemotePath = LocalImageModel.findByRemotePath(remoteUrl);
        boolean needDownload = ListUtils.isNullOrEmpty(byRemotePath) ||
                StringUtils.isNullOrEmpty(byRemotePath.get(0).LocalPath) ||
                !new File(byRemotePath.get(0).LocalPath).exists();
        if (needDownload) {
            String savePath = (faceMod ? (AppConfig.Path_FaceImage + "face_") : (AppConfig.Path_FingerImage + "finger_")) + System.currentTimeMillis() + "_" + Math.abs(RANDOM.nextInt()) + ".jpeg";
            ZZResponse<?> download = new DownloadClient()
                    .bindDownloadInfo(remoteUrl, savePath)
                    .bindDownloadTimeOut(5 * 1000, 5 * 1000)
                    .download();
            if (!ZZResponse.isSuccess(download)) {
                return MxResponse.CreateFail(download.getCode(), download.getMsg());
            }
            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inJustDecodeBounds = true;
            BitmapFactory.decodeFile(savePath, options);
            if (options.outWidth <= 0 || options.outHeight <= 0) {
                FileUtils.delete(savePath);
                return MxResponse.CreateFail(faceMod ? MxResponseCode.Code_Illegal_Image_Face : MxResponseCode.Code_Illegal_Image_Finger,
                        faceMod ? MxResponseCode.Msg_Illegal_Image_Face : MxResponseCode.Msg_Illegal_Image_Finger);
            }

            LocalImage localImage;
            if (ListUtils.isNullOrEmpty(byRemotePath)) {
                localImage = new LocalImage();
            } else {
                localImage = byRemotePath.get(0);
            }
            localImage.RemotePath = remoteUrl;
            localImage.LocalPath = savePath;
            localImage.id = LocalImageModel.insert(localImage);
            if (localImage.id <= 0) {
                FileUtils.delete(savePath);
                return MxResponse.CreateFail(MxResponseCode.CODE_OPERATION_ERROR, "insert image failed");
            }
            return MxResponse.CreateSuccess(localImage);
        } else {
            return MxResponse.CreateSuccess(byRemotePath.get(0));
        }
    }

    private static MxResponse<Face> processFace(String userId, String url_face) {
        if (StringUtils.isNullOrEmpty(userId)) {
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
        Face temp = FaceModel.findByUserID(userId);
        Timber.e("processFace  find local Face:%s", temp);
        Face face = temp == null ? new Face() : temp;
        if (face.id <= 0 || face.faceImageId != faceImage.id) {
            MxResponse<byte[]> featureExtract = doFaceProcess(faceImage.LocalPath);
            if (!MxResponse.isSuccess(featureExtract)) {
                return MxResponse.CreateFail(featureExtract);
            }
            face.faceImageId = faceImage.id;
            face.UserId = userId;
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
                    finger.Position = getPositionFromList(localImage.RemotePath, url_fingers);
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

    public static MxResponse<?> update(User user) {
        if (user == null || user.isIllegal()) {
            return MxResponse.CreateFail(MxResponseCode.CODE_ILLEGAL_PARAMETER, MxResponseCode.MSG_ILLEGAL_PARAMETER);
        }
        String userId = String.valueOf(user.id);
        Person person = PersonModel.findByUserID(userId);
        if (person == null) {
            return MxResponse.CreateFail(MxResponseCode.CODE_OPERATION_ERROR, "not exists");
        }
        MxResponse<Face> faceMxResponse = processFace(userId, user.url_face);
        if (!MxResponse.isSuccess(faceMxResponse)) {
            return faceMxResponse;
        }
        MxResponse<List<Long>> listMxResponse = processFingers(userId, user.getUrl_fingers());
        if (!MxResponse.isSuccess(listMxResponse)) {
            return listMxResponse;
        }
        List<Long> list = new ArrayList<>();
        list.add(faceMxResponse.getData().faceImageId);
        person.UserId = userId;
        person.faceIds = list;
        person.fingerIds = listMxResponse.getData();
        person.IdCardNumber = user.id_number;
        person.Number = user.id_number;
        person.Name = user.name;
        person.id = PersonModel.insert(person);
        if (person.id <= 0) {
            PersonModel.delete(person);
            FaceModel.delete(faceMxResponse.getData());
            return MxResponse.CreateFail(MxResponseCode.CODE_OPERATION_FAILED, "insert person failed");
        }
        return MxResponse.CreateSuccess();
    }

    public static MxResponse<?> delete(User user) {
        if (user == null || user.id <= 0) {
            return MxResponse.CreateFail(MxResponseCode.CODE_ILLEGAL_PARAMETER, MxResponseCode.MSG_ILLEGAL_PARAMETER);
        }
        Person person = PersonModel.findByUserID(String.valueOf(user.id));
        if (person == null) {
            return MxResponse.CreateFail(MxResponseCode.CODE_OPERATION_ERROR, "not exists");
        }
        FaceModel.delete(person.UserId);
        FingerModel.delete(person.UserId);
        PersonModel.delete(person);

        List<Long> faceIds = person.faceIds;
        for (long faceId : faceIds) {
            Face face = FaceModel.findByID(faceId);
            if (face != null) {
                LocalImageModel.delete(face.faceImageId);
            }
        }
        List<Long> fingerIds = person.fingerIds;
        for (long fingerId : fingerIds) {
            Finger finger = FingerModel.findByID(fingerId);
            if (finger != null) {
                LocalImageModel.delete(finger.fingerImageId);
            }
        }
        return MxResponse.CreateSuccess();
    }

    public static MxResponse<?> insertOrUpdate(User user) {
        if (user == null || user.isIllegal()) {
            return MxResponse.CreateFail(MxResponseCode.CODE_ILLEGAL_PARAMETER, MxResponseCode.MSG_ILLEGAL_PARAMETER);
        }
        Timber.e("insertOrUpdate   User:%s", user);
        String userId = String.valueOf(user.id);
        Person person = PersonModel.findByUserID(userId);
        if (person == null) {
            person = new Person();
        }
        person.UserId = userId;
        person.IdCardNumber = user.id_number;
        person.Number = user.id_number;
        person.Name = user.name;
        MxResponse<Face> faceMxResponse = processFace(userId, user.url_face);
        if (!MxResponse.isSuccess(faceMxResponse)) {
            return faceMxResponse;
        }
        MxResponse<List<Long>> listMxResponse = processFingers(userId, user.getUrl_fingers());
        if (!MxResponse.isSuccess(listMxResponse)) {
            return listMxResponse;
        }
        List<Long> list = new ArrayList<>();
        list.add(faceMxResponse.getData().id);
        person.faceIds = list;
        person.fingerIds = listMxResponse.getData();
        person.id = PersonModel.insert(person);
        if (person.id <= 0) {
            PersonModel.delete(person);
            FaceModel.delete(faceMxResponse.getData());
            return MxResponse.CreateFail(MxResponseCode.CODE_OPERATION_FAILED, "insert person failed");
        }
        return MxResponse.CreateSuccess(person.id);
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
