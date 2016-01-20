package com.videonasocialmedia.camarada;

import android.app.Application;
import android.content.Context;
import android.os.Build;
import android.support.multidex.MultiDex;

import com.google.android.gms.analytics.GoogleAnalytics;
import com.google.android.gms.analytics.Logger;
import com.google.android.gms.analytics.Tracker;
import com.karumi.dexter.Dexter;


public class CamaradaApplication extends Application{

    private static Context context;

    Tracker appTracker;

    public static Context getAppContext() {
        return CamaradaApplication.context;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        context=getApplicationContext();
        setupGoogleAnalytics();
        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)
            Dexter.initialize(this);
    }


    private void setupGoogleAnalytics() {
        GoogleAnalytics analytics = GoogleAnalytics.getInstance(this);
        if (BuildConfig.DEBUG)
            analytics.setDryRun(true);
        analytics.getLogger().setLogLevel(Logger.LogLevel.VERBOSE);
        appTracker = analytics.newTracker(R.xml.app_tracker);
        appTracker.enableAdvertisingIdCollection(true);
    }

    protected void attachBaseContext(Context base) {
        super.attachBaseContext(base);
        MultiDex.install(this);
    }

    /**
     * @return google analytics tracker
     */
    public synchronized Tracker getTracker() {
        return appTracker;
    }

}

