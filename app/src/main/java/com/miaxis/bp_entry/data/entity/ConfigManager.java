package com.miaxis.bp_entry.data.entity;

import com.miaxis.bp_entry.app.App;

import androidx.annotation.NonNull;
import io.reactivex.Observable;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;

/**
 * @author ZJL
 * @date 2022/3/23 16:14
 * @des
 * @updateAuthor
 * @updateDes
 */
public class ConfigManager {

    public ConfigManager() {
    }

    public static ConfigManager getInstance(){
        return SingletonHolder.instance;
    }

    private static class SingletonHolder {
        private static final ConfigManager instance = new ConfigManager();
    }

    private Config config;

    public Config getConfig() {
        return config;
    }

    public void setConfig(Config config) {
        this.config = config;
    }


    public void saveConfig(@NonNull Config config, @NonNull OnConfigSaveListener listener) {
        Disposable subscribe = Observable.create((ObservableOnSubscribe<Boolean>) emitter -> {
            ConfigModel.saveConfig(config);
            this.config = config;
            emitter.onNext(Boolean.TRUE);
        }).subscribeOn(Schedulers.from(App.getInstance().getThreadExecutor()))
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(aBoolean -> listener.onConfigSave(true, "配置保存成功")
                        , throwable -> listener.onConfigSave(false, "保存失败，" + throwable.getMessage()));
        App.getInstance().getCp().add(subscribe);
    }

    public interface OnConfigSaveListener {
        void onConfigSave(boolean result, String message);
    }

}
