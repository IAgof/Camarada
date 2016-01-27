package com.videonasocialmedia.Camarada.model.repository;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.drawable.Drawable;
import android.util.Log;

import com.videonasocialmedia.Camarada.KamaradaApplication;
import com.videonasocialmedia.Camarada.model.entities.SocialNetwork;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by jca on 18/1/16.
 */
public class SocialNetworkRepository {

    public List<SocialNetwork> getSocialNetworks() {

        return getSocialAppsInstalled();
    }

    private List<SocialNetwork> getSocialAppsInstalled() {
        List<SocialNetwork> socialNetworkApps = new ArrayList<>();
        Context context = KamaradaApplication.getAppContext();
        Intent shareIntent = new Intent(android.content.Intent.ACTION_SEND);
        shareIntent.setType("video/*");
        PackageManager pm = context.getPackageManager();
        List<ResolveInfo> activityList = pm.queryIntentActivities(shareIntent, 0);
        for (final ResolveInfo app : activityList) {
            Log.d("SocialNetworks", app.activityInfo.name);
            String packageName = app.activityInfo.applicationInfo.packageName;
            String activityName = app.activityInfo.name;
            Drawable icon = app.loadIcon(pm);
            SocialNetwork socialNetworkApp;
            if (packageName.toLowerCase().contains("twitter")
                    || activityName.toLowerCase().contains("twitter")) {
                socialNetworkApp = new SocialNetwork("Twitter", packageName,
                        activityName, icon, "#videona");
            } else if (packageName.toLowerCase().contains("facebook.katana")) {
                socialNetworkApp = new SocialNetwork("Facebook", packageName,
                        activityName, icon, "");
            } else if (packageName.toLowerCase().contains("whatsapp")
                    || activityName.toLowerCase().contains("whatsapp")) {
                socialNetworkApp = new SocialNetwork("Whatsapp", packageName,
                        activityName, icon, "#videona");
            } else if (packageName.toLowerCase().contains("youtube")
                    || activityName.toLowerCase().contains("youtube")) {
                socialNetworkApp = new SocialNetwork("Youtube", packageName,
                        activityName, icon, "");
            } else if (packageName.toLowerCase().contains("plus")
                    && activityName.toLowerCase().contains("com.google.android.libraries.social.gateway.GatewayActivity")) {
                socialNetworkApp = new SocialNetwork("GooglePlus", packageName,
                        activityName, icon, "#videona");
            } else if (packageName.toLowerCase().contains("instagram")
                    || activityName.toLowerCase().contains("instagram")) {
                socialNetworkApp = new SocialNetwork("Instagram", packageName,
                        activityName, icon, "#videona");
            } else {
                socialNetworkApp = new SocialNetwork("Generic", packageName,
                        activityName, icon, "");
            }
            socialNetworkApps.add(socialNetworkApp);
            Log.d(socialNetworkApp.getName(), socialNetworkApp.getAndroidPackageName() + "||" + socialNetworkApp.getAndroidActivityName());
        }
        return socialNetworkApps;
    }
}
