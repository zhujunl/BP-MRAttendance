package com.miaxis.attendance.data.dao;

import com.miaxis.attendance.data.entity.Staff;

import java.util.List;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

/**
 * @author ZJL
 * @date 2022/4/1 17:13
 * @des
 * @updateAuthor
 * @updateDes
 */
@Dao
public interface StaffDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    long insert(Staff staff);

    @Query("select * from Staff ")
    List<Staff> findStaff();

    @Delete
    int delete(Staff staff);

    @Query("delete from Staff where Staff.place=:place and Staff.code=:code")
    int delete(String place,String code);

    @Query("select * from Staff where Staff.code=:code and Staff.place=:place limit 1")
    Staff findStaffByCode(String code,String place);

    @Update
    int updateStaff(Staff staff);

}
