package com.miaxis.attendance.service.process;


import com.miaxis.attendance.data.model.FingerModel;
import com.miaxis.attendance.service.MxResponse;
import com.miaxis.attendance.service.process.base.GetParamProcess;

import java.util.Map;


/**
 * @author Tank
 * @date 2021/8/23 5:28 下午
 * @des
 * @updateAuthor
 * @updateDes
 */
public class FingerProcess {


    public static class QueryAll extends GetParamProcess {
        public QueryAll() {
        }

        @Override
        protected MxResponse<?> onPostParamProcess(Map<String, String> parameter) throws Exception {
            return MxResponse.CreateSuccess(FingerModel.findAll());
        }
    }

}



