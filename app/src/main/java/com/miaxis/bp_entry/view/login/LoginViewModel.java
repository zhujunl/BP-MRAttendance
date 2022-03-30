package com.miaxis.bp_entry.view.login;

import com.miaxis.bp_entry.data.entity.Config;
import com.miaxis.bp_entry.data.entity.ConfigManager;
import com.miaxis.bp_entry.viewModel.BaseViewModel;


public class LoginViewModel extends BaseViewModel {

    public void Login(String placeid, String tittle, String ip, ConfigManager.OnConfigSaveListener listener){
        Config config=new Config(placeid,tittle,ip);
        ConfigManager.getInstance().saveConfig(config,listener);
    }

}
