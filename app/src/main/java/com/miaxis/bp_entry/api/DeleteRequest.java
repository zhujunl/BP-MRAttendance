package com.miaxis.bp_entry.api;

/**
 * @author ZJL
 * @date 2022/4/6 18:10
 * @des
 * @updateAuthor
 * @updateDes
 */
public class DeleteRequest {
    public String code;
    public String place;

    public DeleteRequest(String code, String place) {
        this.code = code;
        this.place = place;
    }
}
