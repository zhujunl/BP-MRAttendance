package com.miaxis.bp_entry.app;


import android.app.Application;
import android.util.Log;

import com.miaxis.bp_entry.data.dao.AppDatabase;
import com.miaxis.bp_entry.manager.FaceManager;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import io.reactivex.disposables.CompositeDisposable;

public class App extends Application {
    private ExecutorService threadExecutor = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors() * 2);
    private static App instance;
    private static CompositeDisposable cp;
    private final String TAG="APP";

    @Override
    public void onCreate() {
        super.onCreate();
        Log.e("App:","onCreate");
        instance=this;
    }

    public static App getInstance() {
        return instance;
    }

    public void initApplication(){
        int result = FaceManager.getInstance().initFaceST(getApplicationContext(), "");
        Log.e(TAG,""+result);
        AppDatabase.initDB(this);
    }

    public ExecutorService getThreadExecutor() {
        return threadExecutor;
    }

    public CompositeDisposable getCp(){
        if(cp==null){
            cp=new CompositeDisposable();
        }
        return cp;
    }

    public  void cpClear(){
        if(cp!=null){
            cp.clear();
            cp=null;
        }
    }

}
