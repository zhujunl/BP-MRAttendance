package com.miaxis.attendance.service.process;


import com.miaxis.attendance.data.model.AttendanceModel;
import com.miaxis.attendance.service.MxResponse;
import com.miaxis.attendance.service.process.base.PostBodyProcess;

import java.util.Map;

/**
 * @author Tank
 * @date 2021/8/23 5:28 下午
 * @des
 * @updateAuthor
 * @updateDes
 */
public class AttendanceProcess {

    public static class QueryAll extends PostBodyProcess {

        public QueryAll() {
        }

        @Override
        public MxResponse<?> onPostProcess(Map<String, String> param) throws Exception {
            return MxResponse.CreateSuccess(AttendanceModel.findAll());
        }
    }

}



