package com.miaxis.attendance.service.bean;

/**
 * @author ZJL
 * @date 2022/4/6 18:10
 * @des
 * @updateAuthor
 * @updateDes
 */
public class DeleteBean {
    public String code;
    public String place;

    public DeleteBean() {
    }

    public DeleteBean(String code, String place) {
        this.code = code;
        this.place = place;
    }

    @Override
    public String toString() {
        return "DeleteRequest{" +
                "code='" + code + '\'' +
                ", place='" + place + '\'' +
                '}';
    }
}
