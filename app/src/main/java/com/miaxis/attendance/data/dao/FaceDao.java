package com.miaxis.attendance.data.dao;


import com.miaxis.attendance.data.entity.Face;

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
public interface FaceDao {

    @Query("SELECT * FROM Face ORDER BY Face.id ASC")
    List<Face> findAll();

    @Query("SELECT * FROM Face WHERE Face.userID=:userID LIMIT 1")
    List<Face> findByUserID(String userID);

    @Query("SELECT * FROM Face WHERE Face.id=:id LIMIT 1")
    List<Face> findByID(long id);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    long insert(Face face);

    @Update
    int update(Face face);

    @Delete
    int delete(Face face);

    @Query("DELETE FROM Face WHERE Face.userID=:userId")
    int delete(String userId);

    @Query("DELETE FROM Face WHERE Face.id=:id")
    int delete(long id);

    @Query("DELETE FROM Face")
    int deleteAll();

    @Query("SELECT COUNT(*) FROM Face")
    int allCounts();

    @Query("SELECT * FROM Face ORDER BY Face.id ASC LIMIT :pageSize OFFSET :offset")
    List<Face> findPage(int pageSize, int offset);

}
