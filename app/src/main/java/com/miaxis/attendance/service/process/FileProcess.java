package com.miaxis.attendance.service.process;


import android.util.Base64;

import com.miaxis.attendance.config.AppConfig;
import com.miaxis.attendance.data.entity.LocalImage;
import com.miaxis.attendance.data.model.LocalImageModel;
import com.miaxis.attendance.service.MxResponse;
import com.miaxis.attendance.service.MxResponseCode;
import com.miaxis.attendance.service.process.base.GetParamProcess;
import com.miaxis.attendance.service.process.base.PostBodyProcess;
import com.miaxis.common.utils.FileUtils;
import com.miaxis.common.utils.StringUtils;

import java.util.Map;

import timber.log.Timber;

/**
 * @author Tank
 * @date 2021/8/23 5:28 下午
 * @des
 * @updateAuthor
 * @updateDes
 */
public class FileProcess {


    public static class QueryAll extends GetParamProcess {

        public QueryAll() {
        }

        @Override
        protected MxResponse<?> onPostParamProcess(Map<String, String> parameter) throws Exception {
            return MxResponse.CreateSuccess(LocalImageModel.findAll());
        }
    }

    public static class AddFile extends PostBodyProcess {

        public AddFile() {
        }

        @Override
        public MxResponse<?> onPostProcess(Map<String, String> param) throws Exception {
            String file = param.get("file");
            if (StringUtils.isNullOrEmpty(file)) {
                return MxResponse.CreateFail(MxResponseCode.CODE_ILLEGAL_PARAMETER, "param empty");
            }
            byte[] decode = Base64.decode(file, Base64.NO_WRAP);
            String savePath = AppConfig.Path_File + System.currentTimeMillis() + ".jpeg";
            boolean b = FileUtils.writeFile(savePath, decode);
            Timber.e("writeFile: " + b);
            if (!b) {
                return MxResponse.CreateFail(MxResponseCode.CODE_OPERATION_ERROR, "save image failed");
            }
            LocalImage localImage = new LocalImage();
            localImage.LocalPath = savePath;
            localImage.id = LocalImageModel.insert(localImage);
            return MxResponse.CreateSuccess(localImage.id);
        }
    }

}



