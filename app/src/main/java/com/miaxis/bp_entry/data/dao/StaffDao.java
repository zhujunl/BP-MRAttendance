package com.miaxis.bp_entry.data.dao;

import com.miaxis.bp_entry.data.entity.Staff;

import java.util.List;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

/**
 * @author ZJL
 * @date 2022/3/31 9:27
 * @des
 * @updateAuthor
 * @updateDes
 */
@Dao
public interface StaffDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert(Staff staff);

    @Query("select * from Staff where Staff.id=:id")
    Staff QueryStaff(long id);

    @Query("delete  from Staff where Staff.place=:place and Staff.code=:code")
    void DeleteStaff(String place,String code);

    @Query("select * from Staff where Staff.place=:place and Staff.code=:code order by Staff.id desc limit 1")
    Staff QueryStaff(String place,String code);

    @Query("select * from Staff")
    List<Staff> QueryStaffList();
}
