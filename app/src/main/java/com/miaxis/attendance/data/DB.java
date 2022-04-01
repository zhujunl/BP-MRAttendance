package com.miaxis.attendance.data;

import com.miaxis.attendance.data.converter.LongListConverter;
import com.miaxis.attendance.data.dao.AttendanceDao;
import com.miaxis.attendance.data.dao.FaceDao;
import com.miaxis.attendance.data.dao.FingerDao;
import com.miaxis.attendance.data.dao.LocalImageDao;
import com.miaxis.attendance.data.dao.PersonDao;
import com.miaxis.attendance.data.entity.Attendance;
import com.miaxis.attendance.data.entity.Face;
import com.miaxis.attendance.data.entity.Finger;
import com.miaxis.attendance.data.entity.LocalImage;
import com.miaxis.attendance.data.entity.Person;

import androidx.room.Database;
import androidx.room.RoomDatabase;
import androidx.room.TypeConverters;

@Database(entities = {Attendance.class, Face.class, Finger.class, LocalImage.class, Person.class}, version = 2)
@TypeConverters({LongListConverter.class})
public abstract class DB extends RoomDatabase {

    public abstract FaceDao FaceDao();

    public abstract AttendanceDao AttendanceDao();

    public abstract LocalImageDao LocalImageDao();

    public abstract PersonDao PersonDao();

    public abstract FingerDao FingerDao();

}
