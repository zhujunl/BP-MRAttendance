package com.miaxis.attendance.service;


import java.io.Serializable;

/**
 * @author tank
 * @version $
 * @des 请求返回类
 * @updateAuthor $
 * @updateDes
 */
public class MxResponse<T> implements Serializable {

    private final int code;
    private final String message;
    private final T data;

    private MxResponse() {
        this(MxResponseCode.CODE_DEFAULT, null);
    }

    public MxResponse(int code, String message) {
        this(code, message, null);
    }

    public MxResponse(int code, String message, T data) {
        this.code = code;
        this.message = message;
        this.data = data;
    }

    public static <T> MxResponse<T> Create() {
        return new MxResponse<T>();
    }

    public static <T> MxResponse<T> Create(int code, String msg, T data) {
        return new MxResponse<T>(code, msg, data);
    }

    public static <T> MxResponse<T> Create(MxResponse<?> zzResponse, Class<T> clazz) {
        if (zzResponse == null) {
            return Create();
        }
        return Create(zzResponse.getCode(), zzResponse.getMessage(), null);
    }

    public static <T> MxResponse<T> CreateFail() {
        return Create();
    }

    public static <T> MxResponse<T> CreateFail(int code, String msg) {
        return Create(code, msg, null);
    }

    public static <T> MxResponse<T> CreateFail(MxResponse<?> zzResponse) {
        if (zzResponse == null) {
            return Create();
        }
        return CreateFail(zzResponse.getCode(), zzResponse.getMessage());
    }

    public static <T> MxResponse<T> CreateSuccess(T data) {
        return Create(MxResponseCode.CODE_SUCCESS, MxResponseCode.MSG_SUCCESS, data);
    }

    public static <T> MxResponse<T> CreateSuccess() {
        return Create(MxResponseCode.CODE_SUCCESS, MxResponseCode.MSG_SUCCESS, null);
    }

    public static boolean isSuccess(MxResponse<?> zzResponse, int successCode) {
        if (zzResponse == null) {
            return false;
        }
        return zzResponse.getCode() == successCode;
    }

    public static boolean isSuccess(MxResponse<?> zzResponse) {
        return isSuccess(zzResponse, MxResponseCode.CODE_SUCCESS);
    }

    public int getCode() {
        return code;
    }

    public String getMessage() {
        return message;
    }

    public T getData() {
        return data;
    }

    @Override
    public String toString() {
        return "MxResponse{" +
                "code=" + code +
                ", msg='" + message + '\'' +
                (data == null ? "" : (", data=" + data)) +
                '}';
    }

}
