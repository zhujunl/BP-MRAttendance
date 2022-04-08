package com.miaxis.bp_entry.event;

/**
 * @author ZJL
 * @date 2022/4/7 15:31
 * @des
 * @updateAuthor
 * @updateDes
 */
public class VerifyEvent {
    private String message;

    public VerifyEvent(String message){
        this.message=message;
    }
    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
