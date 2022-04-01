package com.miaxis.attendance.data.dao;


import com.miaxis.attendance.data.entity.Attendance;

import java.util.List;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

/**
 * @author Admin
 * @version $
 * @des
 * @updateAuthor $
 * @updateDes
 */
@Dao
public interface AttendanceDao {

    @Query("SELECT * FROM Attendance ORDER BY Attendance.id ASC")
    List<Attendance> findAll();

    @Query("SELECT * FROM Attendance WHERE Attendance.Upload!=1 ORDER BY Attendance.id DESC Limit 1")
    List<Attendance> findNoUpload();

    @Query("SELECT * FROM Attendance WHERE Attendance.userID=:userID")
    List<Attendance> findByUserID(String userID);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    long insert(Attendance attendance);

    @Update
    int update(Attendance attendance);

    @Delete
    int delete(Attendance attendance);

    @Query("DELETE FROM Attendance WHERE Attendance.userID=:userId")
    int delete(String userId);

    @Query("DELETE FROM Attendance")
    int deleteAll();

    @Query("SELECT COUNT(*) FROM Attendance")
    int allCounts();

    @Query("SELECT * FROM Attendance ORDER BY Attendance.id ASC LIMIT :pageSize OFFSET :offset")
    List<Attendance> findPage(int pageSize, int offset);

}
