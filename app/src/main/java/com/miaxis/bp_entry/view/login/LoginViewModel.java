package com.miaxis.bp_entry.view.login;

import android.text.TextUtils;

import com.miaxis.bp_entry.data.entity.Config;
import com.miaxis.bp_entry.data.entity.ConfigManager;
import com.miaxis.bp_entry.viewModel.BaseViewModel;


public class LoginViewModel extends BaseViewModel {

    public void Login( String tittle,String placeId,String attenId,ConfigManager.OnConfigSaveListener listener){
        Config config=new Config(placeId,tittle,attenId);
        ConfigManager.getInstance().saveConfig(config,listener);
    }

    public boolean checkInput(String tittle,String placeId,String attenId) {
        if (TextUtils.isEmpty(tittle)
                || TextUtils.isEmpty(placeId)
                || TextUtils.isEmpty(attenId)) {
            return false;
        }
        return true;
    }

}
