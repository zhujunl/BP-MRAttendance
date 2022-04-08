package com.miaxis.attendance.service.process;

import com.alibaba.fastjson.JSON;
import com.miaxis.attendance.data.model.PersonModel;
import com.miaxis.attendance.service.HttpServer;
import com.miaxis.attendance.service.MxResponse;
import com.miaxis.attendance.service.MxResponseCode;
import com.miaxis.attendance.service.bean.DeleteBean;
import com.miaxis.attendance.service.bean.StaffBean;
import com.miaxis.attendance.service.process.base.GetParamProcess;
import com.miaxis.attendance.service.process.base.PostBodyProcess;
import com.miaxis.attendance.service.transform.PersonTransform;
import com.miaxis.common.utils.StringUtils;

import java.util.List;
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
            String s=param.get("Data");
            String toJson = HttpServer.Gson.toJson(s);
            Timber.e("AddUser_tojosn:%s", toJson);
            String json=StringUtils.unicodetoString(toJson);
            Timber.e("AddUser_json:%s", json);
            StaffBean staff= JSON.parseObject(s, StaffBean.class);
            Timber.e("AddUser_staff:%s", staff);
            if (staff==null){
                return MxResponse.CreateFail(MxResponseCode.CODE_ILLEGAL_PARAMETER, "param error");
            }
            MxResponse<?> transform = PersonTransform.insert(staff);

            return MxResponse.Create(transform.getCode(), transform.getMessage(), transform.getData());
        }
    }

    public static class DeleteUser extends PostBodyProcess {

        public DeleteUser() {
        }

        @Override
        protected MxResponse<?> onPostProcess(Map<String, String> param) throws Exception {
            Timber.e("delete:deleteparam:%s",param.toString());
            String s=param.get("Data");
            DeleteBean deleteRequest= JSON.parseObject(s, DeleteBean.class);
            Timber.e("delete:deleteparam:%s",deleteRequest.toString());
            MxResponse<?> transform = PersonTransform.delete(deleteRequest);
            return MxResponse.Create(transform.getCode(), transform.getMessage(), transform.getData());
        }
    }

    public static class UpdateList extends PostBodyProcess{
        public UpdateList() {

        }

        @Override
        protected MxResponse<?> onPostProcess(Map<String, String> param) throws Exception {
            String s=param.get("Data");
            List<StaffBean> staffList= JSON.parseArray(s,StaffBean.class);
            Timber.e("UpdateList_staff:%s", staffList);
            if (staffList==null){
                return MxResponse.CreateFail(MxResponseCode.CODE_ILLEGAL_PARAMETER, "param error");
            }
            MxResponse<?> transform = PersonTransform.updateList(staffList);
            return MxResponse.Create(transform.getCode(), transform.getMessage(), transform.getData());
        }
    }

    public static class UpdateStaff extends PostBodyProcess{
        public UpdateStaff() {
        }

        @Override
        protected MxResponse<?> onPostProcess(Map<String, String> param) throws Exception {
            String s=param.get("Data");
            StaffBean staff= JSON.parseObject(s,StaffBean.class);
            Timber.e("UpdateList_staff:%s", staff);
            if (staff==null){
                return MxResponse.CreateFail(MxResponseCode.CODE_ILLEGAL_PARAMETER, "param error");
            }
            MxResponse<?> transform = PersonTransform.update(staff);
            return MxResponse.Create(transform.getCode(), transform.getMessage(), transform.getData());
        }
    }
}



