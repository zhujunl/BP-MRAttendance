package com.miaxis.attendance.api.response;

/**
 * @author ZJL
 * @date 2022/4/7 8:55
 * @des
 * @updateAuthor
 * @updateDes
 */

public class AttendanceResponse {
    private String GroupName;//⼯号
    private  String WorkNumber;//艺名
    private  String Name;//姓名
    private  String StageName;//艺名
    private  String Fee;//台费
    private  String Tip;//⼩费
    private  String Duties;//职务
    private  String Phone;//⼿机
    private  String Balance;//余额
    private  String Remarks;//备注

    public AttendanceResponse(String groupName, String workNumber, String name, String stageName, String fee, String tip, String duties, String phone, String balance, String remarks) {
        GroupName = groupName;
        WorkNumber = workNumber;
        Name = name;
        StageName = stageName;
        Fee = fee;
        Tip = tip;
        Duties = duties;
        Phone = phone;
        Balance = balance;
        Remarks = remarks;
    }

    public String getGroupName() {
        return GroupName;
    }

    public void setGroupName(String groupName) {
        GroupName = groupName;
    }

    public String getWorkNumber() {
        return WorkNumber;
    }

    public void setWorkNumber(String workNumber) {
        WorkNumber = workNumber;
    }

    public String getName() {
        return Name;
    }

    public void setName(String name) {
        Name = name;
    }

    public String getStageName() {
        return StageName;
    }

    public void setStageName(String stageName) {
        StageName = stageName;
    }

    public String getFee() {
        return Fee;
    }

    public void setFee(String fee) {
        Fee = fee;
    }

    public String getTip() {
        return Tip;
    }

    public void setTip(String tip) {
        Tip = tip;
    }

    public String getDuties() {
        return Duties;
    }

    public void setDuties(String duties) {
        Duties = duties;
    }

    public String getPhone() {
        return Phone;
    }

    public void setPhone(String phone) {
        Phone = phone;
    }

    public String getBalance() {
        return Balance;
    }

    public void setBalance(String balance) {
        Balance = balance;
    }

    public String getRemarks() {
        return Remarks;
    }

    public void setRemarks(String remarks) {
        Remarks = remarks;
    }

    @Override
    public String toString() {
        return "AttendanceResponse{" +
                "GroupName='" + GroupName + '\'' +
                ", WorkNumber='" + WorkNumber + '\'' +
                ", Name='" + Name + '\'' +
                ", StageName='" + StageName + '\'' +
                ", Fee='" + Fee + '\'' +
                ", Tip='" + Tip + '\'' +
                ", Duties='" + Duties + '\'' +
                ", Phone='" + Phone + '\'' +
                ", Balance='" + Balance + '\'' +
                ", Remarks='" + Remarks + '\'' +
                '}';
    }
}
