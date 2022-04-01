package com.miaxis.bp_entry.data.entity;

import java.util.List;

/**
 * @author ZJL
 * @date 2022/3/31 9:33
 * @des
 * @updateAuthor
 * @updateDes
 */
public class StaffManager {
    private static StaffManager instance;
    private Staff staff;

    public static StaffManager getInstance(){
        if (instance==null){
            instance=new StaffManager();
        }
        return instance;
    }

    public void save(Staff staff){
        StaffModel.save(staff);
    }

    public Staff getStaff(long id) {
        return StaffModel.queryStaff(id);
    }

    public Staff getStaff(String place,String code) {
        return StaffModel.queryStaff(place,code);
    }
    public List<Staff> StaffList(){
        return StaffModel.queryAll();
    }

    public void deleteStaff(String palce,String code){
        StaffModel.delete(palce,code);
    }

}
