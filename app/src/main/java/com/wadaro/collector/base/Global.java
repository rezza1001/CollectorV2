package com.wadaro.collector.base;

import android.content.Context;

import com.wadaro.collector.model.UserInformation;

/**
 * Created by pho0890910 on 2/20/2019.
 */
public class Global {

    public static UserInformation userInformation = null;

    public static void startAppIniData( Context p_context)
    {
        Shared.initialize(p_context);
    }

    public static void clearGlobalData(){
        userInformation = null;
    }

    public static String PREF_OFFLINE_MODE = "offline mode";

}
