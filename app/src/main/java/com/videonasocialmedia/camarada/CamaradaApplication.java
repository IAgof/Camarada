package com.videonasocialmedia.camarada;

import android.app.Application;
import android.content.Context;

/**
 * Created by jca on 18/1/16.
 */
public class CamaradaApplication extends Application{

    private static Context context;

    public static Context getAppContext() {
        return CamaradaApplication.context;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        context=getApplicationContext();
    }
}
