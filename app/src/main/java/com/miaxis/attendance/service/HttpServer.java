package com.miaxis.attendance.service;

import android.text.TextUtils;

import com.google.gson.Gson;
import com.miaxis.attendance.service.process.UserProcess;
import com.miaxis.attendance.service.process.base.BaseProcess;

import org.nanohttpd.NanoHTTPD;

import timber.log.Timber;


/**
 * @author Tank
 * @date 2021/8/3 8:37 下午
 * @des
 * @updateAuthor
 * @updateDes
 */
public class HttpServer extends NanoHTTPD {

    private final String TAG = "HttpServer";
    public static final Gson Gson = new Gson();
    //private final ConcurrentHashMap<String, BaseProcess> mProcess = new ConcurrentHashMap<>();

    public HttpServer(int port) {
        super(port);
    }


    @Override
    public Response serve(IHTTPSession session) {
        if (session != null) {
            //Map<String, String> headers = session.getHeaders();
            //Timber.e("headers: %s", HttpServer.Gson.toJson(headers));
            //String token = headers.get("Token");
            //Timber.e("Token: %s", token);
            //String user_agent = headers.get("user_agent");
            BaseProcess process = getProcess(session);
            if (process != null) {
                try {
                    return process.process(session);
                } catch (Exception e) {
                    e.printStackTrace();
                    Timber.e("Exception:%s", e);
                    return NanoHTTPD.newFixedLengthResponse(
                            Response.Status.INTERNAL_ERROR, NanoHTTPD.MIME_PLAINTEXT, "" + e.getMessage());
                }
            }
            return NanoHTTPD.newFixedLengthResponse(
                    Response.Status.NOT_FOUND, NanoHTTPD.MIME_PLAINTEXT, null);
        }
        return NanoHTTPD.newFixedLengthResponse(
                Response.Status.NO_CONTENT, NanoHTTPD.MIME_PLAINTEXT, null);
    }


    private BaseProcess getProcess(IHTTPSession session) {
        if (session == null) {
            return null;
        }
        String uri = session.getUri();
        Timber.e("serve: uri:%s", uri);
        if (TextUtils.isEmpty(uri)) {
            return null;
        }
        switch (uri) {
            case "/api/user/list":
                return new UserProcess.QueryAll();
            case "/api/user/add":
                return new UserProcess.AddUser();
            case "/api/user/update":
                return new UserProcess.UpdateStaff();
            case "/api/user/addlist":
                return new UserProcess.UpdateList();
            case "/api/user/delete":
                return new UserProcess.DeleteUser();
//            case "/api/face/list":
//                return new FaceProcess.QueryAll();
//            case "/api/finger/list":
//                return new FingerProcess.QueryAll();
//            case "/api/attendance/list":
//                return new AttendanceProcess.QueryAll();
//            case "/api/file/list":
//                return new FileProcess.QueryAll();
//            case "/api/file/add":
//                return new FileProcess.AddFile();
            default:
                return null;
        }
    }

}
