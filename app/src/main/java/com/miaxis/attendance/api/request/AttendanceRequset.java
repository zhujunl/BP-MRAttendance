package com.miaxis.attendance.api.request;

/**
 * @author ZJL
 * @date 2022/4/7 8:55
 * @des
 * @updateAuthor
 * @updateDes
 */
public class AttendanceRequset {
    private String WorkNumber;//⼈员⼯号
    private String PositionId;//⻔店ID
    private String Timestamp;//时间，格式：2022-01-01 12:00:00
    private int Status;//识别状态 ，1识别成功 其他识别失败
    private Float  Similarity;//识别相似度，0--1F浮点值，保留四位
    private int Recognition;//识别⽅式，1⼈脸识别 2指纹识别 3其他

    public AttendanceRequset(String workNumber, String positionId, String timestamp, int status, Float similarity, int recognition) {
        WorkNumber = workNumber;
        PositionId = positionId;
        Timestamp = timestamp;
        Status = status;
        Similarity = similarity;
        Recognition = recognition;
    }

    public String getWorkNumber() {
        return WorkNumber;
    }

    public void setWorkNumber(String workNumber) {
        WorkNumber = workNumber;
    }

    public String getPositionId() {
        return PositionId;
    }

    public void setPositionId(String positionId) {
        PositionId = positionId;
    }

    public String getTimestamp() {
        return Timestamp;
    }

    public void setTimestamp(String timestamp) {
        Timestamp = timestamp;
    }

    public int getStatus() {
        return Status;
    }

    public void setStatus(int status) {
        Status = status;
    }

    public Float getSimilarity() {
        return Similarity;
    }

    public void setSimilarity(Float similarity) {
        Similarity = similarity;
    }

    public int getRecognition() {
        return Recognition;
    }

    public void setRecognition(int recognition) {
        Recognition = recognition;
    }

    @Override
    public String toString() {
        return "AttendanceRequset{" +
                "WorkNumber='" + WorkNumber + '\'' +
                ", PositionId='" + PositionId + '\'' +
                ", Timestamp='" + Timestamp + '\'' +
                ", Status=" + Status +
                ", Similarity=" + Similarity +
                ", Recognition=" + Recognition +
                '}';
    }
}
