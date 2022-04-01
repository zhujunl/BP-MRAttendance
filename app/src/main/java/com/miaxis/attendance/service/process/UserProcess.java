package com.miaxis.attendance.service.process;

import com.miaxis.attendance.data.model.PersonModel;
import com.miaxis.attendance.service.HttpServer;
import com.miaxis.attendance.service.MxResponse;
import com.miaxis.attendance.service.MxResponseCode;
import com.miaxis.attendance.service.bean.User;
import com.miaxis.attendance.service.process.base.GetParamProcess;
import com.miaxis.attendance.service.process.base.PostBodyProcess;
import com.miaxis.attendance.service.transform.PersonTransform;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import timber.log.Timber;


/**
 * @author Tank
 * @date 2021/8/23 5:28 下午
 * @des
 * @updateAuthor
 * @updateDes
 */
public class UserProcess {

    private final ConcurrentHashMap<String, Integer> mAddUserProcess = new ConcurrentHashMap<>();


    public static class QueryAll extends GetParamProcess {
        public QueryAll() {
        }

        @Override
        protected MxResponse<?> onPostParamProcess(Map<String, String> parameter) throws Exception {
            return MxResponse.CreateSuccess(PersonModel.findAll());
        }
    }

    public static class AddUser extends PostBodyProcess {
        public AddUser() {
        }

        @Override
        public MxResponse<?> onPostProcess(Map<String, String> param) throws Exception {
            String toJson = HttpServer.Gson.toJson(param);
            Timber.e("AddUser:%s", toJson);
            User user = HttpServer.Gson.fromJson(toJson, User.class);
            Timber.e("AddUser:%s", user);
            if (user == null || user.isIllegal()) {
                return MxResponse.CreateFail(MxResponseCode.CODE_ILLEGAL_PARAMETER, "param error");
            }
            MxResponse<?> transform = PersonTransform.insert(user);
            return MxResponse.Create(transform.getCode(), transform.getMessage(), transform.getData());
        }
    }

    public static class UpdateUser extends PostBodyProcess {

        public UpdateUser() {
        }

        @Override
        protected MxResponse<?> onPostProcess(Map<String, String> param) throws Exception {
            User user = HttpServer.Gson.fromJson(HttpServer.Gson.toJson(param), User.class);
            Timber.e("UpdateUser:%s", user);
            if (user == null || user.isIllegal()) {
                return MxResponse.CreateFail(MxResponseCode.CODE_ILLEGAL_PARAMETER, "param error");
            }
            MxResponse<?> transform = PersonTransform.update(user);
            return MxResponse.Create(transform.getCode(), transform.getMessage(), transform.getData());
        }
    }

    public static class DeleteUser extends PostBodyProcess {

        public DeleteUser() {
        }

        @Override
        protected MxResponse<?> onPostProcess(Map<String, String> param) throws Exception {
            User user = HttpServer.Gson.fromJson(HttpServer.Gson.toJson(param), User.class);
            MxResponse<?> transform = PersonTransform.delete(user);
            return MxResponse.Create(transform.getCode(), transform.getMessage(), transform.getData());
        }
    }
}



