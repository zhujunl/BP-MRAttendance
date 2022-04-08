package com.miaxis.attendance.service.process.base;


import com.miaxis.attendance.service.HttpServer;
import com.miaxis.attendance.service.MxResponse;
import com.miaxis.common.utils.MapUtils;

import org.nanohttpd.NanoHTTPD;

import java.util.HashMap;
import java.util.Map;

import timber.log.Timber;

/**
 * @author Tank
 * @date 2021/8/23 5:28 下午
 * @des
 * @updateAuthor
 * @updateDes
 */
public abstract class PostBodyProcess extends BasePostProcess {

    @Override
    public MxResponse<?> onProcess(NanoHTTPD.IHTTPSession session) throws Exception {
        Map<String, String> param = new HashMap<>();
        session.parseBody(param);
        Map<String, String> parms = session.getParms();
        Timber.e( "parameter: parmsparms" + HttpServer.Gson.toJson(parms));
        Timber.e( "parameter: " + HttpServer.Gson.toJson(param));
        if (MapUtils.isNullOrEmpty(param)) {
            return MxResponse.CreateFail(-1,"Error parameter");
        }
        return onPostProcess(param);
    }

    protected abstract MxResponse<?> onPostProcess(Map<String, String> parameter) throws Exception;

}



