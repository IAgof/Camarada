package com.videonasocialmedia.Kamarada.presentation.views.activity;

import android.Manifest;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;

import com.google.android.gms.analytics.Tracker;
import com.karumi.dexter.Dexter;
import com.karumi.dexter.MultiplePermissionsReport;
import com.karumi.dexter.PermissionToken;
import com.karumi.dexter.listener.multi.EmptyMultiplePermissionsListener;
import com.karumi.dexter.listener.multi.MultiplePermissionsListener;
import com.mixpanel.android.mpmetrics.MixpanelAPI;
import com.videonasocialmedia.Kamarada.BuildConfig;
import com.videonasocialmedia.Kamarada.CamaradaApplication;
import com.videonasocialmedia.Kamarada.presentation.listener.OnCamaradaDialogClickListener;
import com.videonasocialmedia.Kamarada.presentation.views.dialog.CamaradaDialogActivity;
import com.videonasocialmedia.Kamarada.utils.AnalyticsConstants;
import com.videonasocialmedia.Kamarada.utils.PermissionConstants;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;

public class CamaradaActivity extends AppCompatActivity {

    protected static final String ANDROID_PUSH_SENDER_ID = "783686583047";
    protected MixpanelAPI mixpanel;
    protected Tracker tracker;
    protected boolean criticalPermissionDenied = false;
    protected MultiplePermissionsListener dialogMultiplePermissionsListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getIntent().getBooleanExtra("EXIT", false)) {
            finish();
        }
        mixpanel = MixpanelAPI.getInstance(this, BuildConfig.MIXPANEL_TOKEN);
        mixpanel.getPeople().identify(mixpanel.getPeople().getDistinctId());
        mixpanel.getPeople().initPushHandling(ANDROID_PUSH_SENDER_ID);
        configPermissions();
        CamaradaApplication app = (CamaradaApplication) getApplication();
        tracker = app.getTracker();
    }

    private void configPermissions() {
        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            dialogMultiplePermissionsListener =
                    new CustomPermissionListener(this, "titulo", "mensaje", "ok", null);
            Dexter.continuePendingRequestsIfPossible(dialogMultiplePermissionsListener);
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        mixpanel.timeEvent(AnalyticsConstants.TIME_IN_ACTIVITY);
    }

    @Override
    protected void onPause() {
        super.onPause();
        JSONObject activityProperties = new JSONObject();
        try {
            activityProperties.put(AnalyticsConstants.ACTIVITY, getClass().getSimpleName());
            mixpanel.track(AnalyticsConstants.TIME_IN_ACTIVITY, activityProperties);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void onDestroy() {
        mixpanel.flush();
        super.onDestroy();
    }

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(newBase);
    }

    @Override
    protected void onResume() {
        super.onResume();

    }

    protected final void closeApp() {
        Intent intent = new Intent(getApplicationContext(), InitAppActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        intent.putExtra("EXIT", true);
        startActivity(intent);
    }

    protected void checkAndRequestPermissions() {
        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            checkContacts();
            Dexter.checkPermissions(dialogMultiplePermissionsListener, Manifest.permission.CAMERA,
                    Manifest.permission.RECORD_AUDIO,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE);
        }
    }

    private void checkContacts() {
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.GET_ACCOUNTS) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, PermissionConstants.PERMISSIONS_CONTACTS,
                    PermissionConstants.REQUEST_CONTACTS);
        }
    }


    protected boolean isLandscapeOriented() {
        return getOrientation() == Configuration.ORIENTATION_LANDSCAPE;
    }

    protected boolean isPortraitOriented() {
        return getOrientation() == Configuration.ORIENTATION_PORTRAIT;
    }

    private int getOrientation() {
        return getResources().getConfiguration().orientation;
    }

    class CustomPermissionListener extends EmptyMultiplePermissionsListener
            implements OnCamaradaDialogClickListener {

        private final Context context;
        private final String title;
        private final String message;
        private final String positiveButtonText;
        private final Drawable icon;
        private final int REQUEST_CODE_DIALOG_PERMISSION = 1;
        private AlertDialog dialog;
        private CamaradaDialogActivity dialogPermission;

        private CustomPermissionListener(Context context, String title,
                                         String message, String positiveButtonText, Drawable icon) {
            this.context = context;
            this.title = title;
            this.message = message;
            this.positiveButtonText = positiveButtonText;
            this.icon = icon;
        }

        @Override
        public void onPermissionsChecked(MultiplePermissionsReport report) {
            super.onPermissionsChecked(report);

            if (!report.areAllPermissionsGranted()) {
                createPermissionDialog();
            } else {
                if (dialogPermission != null) {
                    dialogPermission.dismiss();
                }
            }
        }

        @Override
        public void onPermissionRationaleShouldBeShown(List<com.karumi.dexter.listener.PermissionRequest> permissions,
                                                       PermissionToken token) {
            super.onPermissionRationaleShouldBeShown(permissions, token);
            token.continuePermissionRequest();
        }

        private void createPermissionDialog() {


            dialogPermission = new CamaradaDialogActivity().newInstance(
                    title,
                    message,
                    positiveButtonText,
                    "",
                    REQUEST_CODE_DIALOG_PERMISSION
            );

            dialogPermission.hideCancelDialog();

            dialogPermission.show(getFragmentManager(), "dialogPermission");

        }

        @Override
        public void onClickAcceptDialogListener(int id) {

            if( id == REQUEST_CODE_DIALOG_PERMISSION){
                dialogPermission.dismiss();
                Intent myAppSettings = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
                        Uri.parse("package:" + context.getPackageName()));
                myAppSettings.addCategory(Intent.CATEGORY_DEFAULT);
                myAppSettings.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                context.startActivity(myAppSettings);
            }

        }

        @Override
        public void onClickCancelDialogListener(int id) {

        }
    }
}
