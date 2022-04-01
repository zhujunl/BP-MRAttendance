package com.miaxis.attendance.service.process.base;


import com.miaxis.attendance.service.HttpServer;
import com.miaxis.attendance.service.MxResponse;

import org.nanohttpd.NanoHTTPD;

import java.util.Map;

import timber.log.Timber;

/**
 * @author Tank
 * @date 2021/8/23 5:28 下午
 * @des
 * @updateAuthor
 * @updateDes
 */
public abstract class GetParamProcess extends BaseGetProcess {

    @Override
    public MxResponse<?> onProcess(NanoHTTPD.IHTTPSession session) throws Exception {
        Map<String, String> parms = session.getParms();
        Timber.e("parameter: " + HttpServer.Gson.toJson(parms));
        return onPostParamProcess(parms);
    }

    protected abstract MxResponse<?> onPostParamProcess(Map<String, String> parameter) throws Exception;

}



