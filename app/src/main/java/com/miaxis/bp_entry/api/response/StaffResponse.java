package com.miaxis.bp_entry.api.response;

import java.io.Serializable;

/**
 * @author ZJL
 * @date 2022/4/1 13:30
 * @des
 * @updateAuthor
 * @updateDes
 */
public class StaffResponse<T> implements Serializable {
    private final int code;
    private final String message;
    private final T data;

    public StaffResponse(int code, String message, T data) {
        this.code = code;
        this.message = message;
        this.data = data;
    }

    public boolean Success(){
        return this.code==0;
    }

    @Override
    public String toString() {
        return "StaffResponse{" +
                "code=" + code +
                ", message='" + message + '\'' +
                ", data=" + data +
                '}';
    }
}
