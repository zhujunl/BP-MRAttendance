package com.miaxis.attendance.data.model;

import com.miaxis.attendance.data.AppDataBase;
import com.miaxis.attendance.data.entity.Staff;
import com.miaxis.attendance.service.bean.DeleteBean;

import java.util.List;

/**
 * @author ZJL
 * @date 2022/4/1 17:28
 * @des
 * @updateAuthor
 * @updateDes
 */
public class StaffModel {
    public static long insert(Staff staff){ return AppDataBase.getInstance().StaffDao().insert(staff);    }

    public static int delete(Staff staff){ return AppDataBase.getInstance().StaffDao().delete(staff);    }

    public static int delete(DeleteBean staff){ return AppDataBase.getInstance().StaffDao().delete(staff.place,staff.code);    }

    public static List<Staff> findStaff(){return AppDataBase.getInstance().StaffDao().findStaff();}

    public static Staff findStaffByCode(String code,String place){
        return AppDataBase.getInstance().StaffDao().findStaffByCode(code,place);
    }

    public static int updateStaff(Staff staff){
        return AppDataBase.getInstance().StaffDao().updateStaff(staff);
    }

}
