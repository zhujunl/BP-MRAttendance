package com.miaxis.attendance.service.process.base;


import com.miaxis.attendance.service.HttpServer;
import com.miaxis.attendance.service.MxResponse;

import org.nanohttpd.NanoHTTPD;

import timber.log.Timber;

/**
 * @author Tank
 * @date 2021/8/23 5:28 下午
 * @des
 * @updateAuthor
 * @updateDes
 */
public abstract class BasePostProcess implements BaseProcess {

    protected final String TAG = "BasePostProcess";

    public abstract MxResponse<?> onProcess(NanoHTTPD.IHTTPSession session) throws Exception;

    public NanoHTTPD.Response process(NanoHTTPD.IHTTPSession session) throws Exception {
        Timber.e( "Method:" + session.getMethod());
        if (NanoHTTPD.Method.POST != session.getMethod()) {
            return NanoHTTPD.newFixedLengthResponse(
                    NanoHTTPD.Response.Status.METHOD_NOT_ALLOWED, NanoHTTPD.MIME_PLAINTEXT, "Error method");
        }
        MxResponse<?> mxResponse = onProcess(session);
        Timber.e( "Response: " + mxResponse);
        return NanoHTTPD.newFixedLengthResponse(
                NanoHTTPD.Response.Status.OK, NanoHTTPD.MIME_JSON, HttpServer.Gson.toJson(mxResponse));
    }

}



