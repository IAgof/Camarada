package com.videonasocialmedia.Camarada;

import android.app.Application;
import android.content.Context;
import android.os.Build;


import com.google.android.gms.analytics.GoogleAnalytics;
import com.google.android.gms.analytics.Logger;
import com.google.android.gms.analytics.Tracker;
import com.karumi.dexter.Dexter;


public class KamaradaApplication extends Application {

    private static Context context;

    Tracker appTracker;

    public static Context getAppContext() {
        return KamaradaApplication.context;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        context = getApplicationContext();
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
    }

    /**
     * @return google analytics tracker
     */
    public synchronized Tracker getTracker() {
        return appTracker;
    }

}

