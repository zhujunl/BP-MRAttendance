package com.miaxis.attendance;

import android.annotation.SuppressLint;
import android.os.Handler;
import android.os.SystemClock;
import android.util.Log;

import com.miaxis.attendance.config.AppConfig;
import com.miaxis.attendance.data.bean.AttendanceBean;
import com.miaxis.attendance.data.bean.TempBean;
import com.miaxis.attendance.device.MR990Device;
import com.miaxis.attendance.service.HttpServer;
import com.miaxis.common.response.ZZResponse;
import com.miaxis.common.utils.FileUtils;

import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Date;
import java.util.List;

import timber.log.Timber;

public class MainViewModel extends ViewModel {


    private final Handler mHandler_Door = new Handler();
    private final Handler mHandler_Dog = new Handler();

    @SuppressLint("SimpleDateFormat")
    private final SimpleDateFormat simpletimeFormat= new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");


    @SuppressLint("SimpleDateFormat")
    private final SimpleDateFormat simpleDateFormat= new SimpleDateFormat("yyyy-MM-dd ");
    private Thread thread;

    public MutableLiveData<Integer> httpServerStatus = new MutableLiveData<>(0);
    /**
     * 设备是否空闲，空闲时显示轮播
     */
    public MutableLiveData<Boolean> isIdle = new MutableLiveData<>();
    public MutableLiveData<Boolean> isIdleDetectStop = new MutableLiveData<>();
    public MutableLiveData<Boolean> startService = new MutableLiveData<>(false);
    public MutableLiveData<ZZResponse<AttendanceBean>> mAttendance = new MutableLiveData<>();
    public MutableLiveData<TempBean> mTemp=new MutableLiveData<>();
    //public MutableLiveData<Boolean> EnableNirProcess = new MutableLiveData<>();

    private HttpServer httpServer;

    public MainViewModel() {
    }

    public  void TempList(){
        TempBean re=new TempBean();
        re.setTime(simpletimeFormat.format(new Date()));
        BufferedReader bf=null;
        try{
            Long temp=null;
            String line;

            bf=new BufferedReader(new FileReader("/sys/class/thermal/thermal_zone"+0+"/temp"));
            line= bf.readLine();
            if (line!=null){
                long t=Long.parseLong(line);
                if(t>0){
                    temp=t;
                }
                re.setBattery(temp);
            }
            bf=new BufferedReader(new FileReader("/sys/class/thermal/thermal_zone"+11+"/temp"));
            line= bf.readLine();
            if (line!=null){
                long t=Long.parseLong(line);
                if(t>0) {
                    temp=t;
                }
                re.setCpu(temp);
            }
            bf.close();
        }catch (Exception e){
            e.printStackTrace();
        }finally {
            if(bf!=null){
                try {
                    bf.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        Timber.d(re.toString());
        mTemp.postValue(re);
        FileWriter fw=null;
        PrintWriter pw = null;
        try {
            File file=new File(AppConfig.Temp_File+"电池与cpu温度"+simpleDateFormat.format(new Date())+".txt");
            if (!file.exists()) {
                File parentFile = file.getParentFile();
                if (parentFile != null && !parentFile.exists()) {
                  parentFile.mkdirs();
                }
            }
            fw=new FileWriter(file,true);
            pw=new PrintWriter(fw);
            pw.println(re.toString());
        } catch (IOException e) {
            e.printStackTrace();
        }
        try {
            if(fw!=null){
                fw.flush();
                fw.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        finally {
            if (pw!=null){
                pw.flush();
                pw.close();
            }
        }

    }

    public void StartThread(){
        StopThread();
        thread=new Thread(() -> {
            while (thread!=null&&!thread.isInterrupted()){
                SystemClock.sleep(3000);
                TempList();
            }
        });
        thread.start();
    }

    public void StopThread(){
        if(thread!=null&&!thread.isInterrupted()){
            thread.interrupt();
            thread=null;
        }
    }


    public void deleteFile(){
        File file=new File(AppConfig.Temp_File);
        File[] fs=file.listFiles();
        if (fs==null) return;
        for(File f:fs){
            if (new Date().getTime()-7*24*60*60*1000>f.lastModified())
                FileUtils.delete(f.getAbsolutePath());
        }
    }

    public void startHttpServer(int port) {
        if (this.httpServerStatus.getValue() != null && this.httpServerStatus.getValue() == 1) {
            return;
        }
        stopHttpServer();
        this.httpServer = new HttpServer(port);
        try {
            this.httpServer.start();
            this.httpServerStatus.setValue(1);
        } catch (Exception e) {
            e.printStackTrace();
            this.httpServerStatus.setValue(-1);
        }
    }

    public void stopHttpServer() {
        if (this.httpServer != null) {
            this.httpServer.stop();
            this.httpServer = null;
            this.httpServerStatus.setValue(0);
        }
    }

    public void openDoor() {
        MR990Device.getInstance().DoorPower(true);
        mHandler_Door.removeCallbacksAndMessages(null);
        mHandler_Door.postDelayed(() -> MR990Device.getInstance().DoorPower(false), AppConfig.CloseDoorDelay);
    }

    public void destroy() {
        timeOutCancel();
        mHandler_Door.removeCallbacksAndMessages(null);
    }

    /**
     * 空闲倒计时重置
     */
    public void timeOutReset(boolean isStartTimeOut) {
        timeOutCancel();
        Timber.e("timeOutReset: %s", isStartTimeOut);
        if (isStartTimeOut) {
            this.mHandler_Dog.postDelayed(() -> isIdle.postValue(true), AppConfig.IdleTimeOut);
        }
    }

    /**
     * 取消空闲倒计时
     */
    public void timeOutCancel() {
        Timber.e("timeOutCancel");
        this.mHandler_Dog.removeCallbacksAndMessages(null);
    }

}