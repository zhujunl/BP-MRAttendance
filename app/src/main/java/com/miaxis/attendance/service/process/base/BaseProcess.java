package com.miaxis.attendance.service.process.base;


import org.nanohttpd.NanoHTTPD;

/**
 * @author Tank
 * @date 2021/8/23 5:28 下午
 * @des
 * @updateAuthor
 * @updateDes
 */
public interface BaseProcess {

    NanoHTTPD.Response process(NanoHTTPD.IHTTPSession session) throws Exception;

}



