package com.miaxis.attendance.api;


public class HttpResponse<T> {
    public static final String SuccessCode = "200";
    public String code;
    public String message;
    public T result;

    public HttpResponse() {
    }

    public HttpResponse(String code, String message, T result) {
        this.code = code;
        this.message = message;
        this.result = result;
    }

    public static <T> HttpResponse<T> createSuccess(T data) {
        return new HttpResponse<T>(SuccessCode, "Success", data);
    }

    public static<T> HttpResponse<T> createFail(String code, String message) {
        return new HttpResponse<>(code, message, null);
    }

    public boolean isSuccess() {
        return HttpResponse.SuccessCode.equals(this.code);
    }

    public static <T> boolean isSuccess(HttpResponse<T> httpResponse) {
        return httpResponse != null && httpResponse.isSuccess();
    }

    @Override
    public String toString() {
        return "ResponseEntity{" +
                "code='" + code + '\'' +
                ", message='" + message + '\'' +
                ", result=" + result +
                '}';
    }
}
