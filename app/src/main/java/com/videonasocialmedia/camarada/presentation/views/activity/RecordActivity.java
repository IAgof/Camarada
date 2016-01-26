package com.videonasocialmedia.camarada.presentation.views.activity;

import android.annotation.TargetApi;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.mixpanel.android.mpmetrics.InAppNotification;
import com.videonasocialmedia.avrecorder.view.GLCameraEncoderView;
import com.videonasocialmedia.camarada.R;
import com.videonasocialmedia.camarada.presentation.mvp.presenters.RecordPresenter;
import com.videonasocialmedia.camarada.presentation.mvp.views.RecordView;
import com.videonasocialmedia.camarada.utils.ConfigPreferences;
import com.videonasocialmedia.camarada.utils.Utils;

import org.json.JSONException;
import org.json.JSONObject;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.OnTouch;

/**
 * Created by Veronica Lago Fominaya on 19/01/2016.
 */
public class RecordActivity extends CamaradaActivity implements RecordView {

    @Bind(R.id.recordLayout)
    LinearLayout recordLayout;
    @Bind(R.id.recordButton)
    ImageButton recButton;
    @Bind(R.id.cameraPreview)
    GLCameraEncoderView cameraView;
    @Bind(R.id.toggleCameraButton)
    ImageButton rotateCameraButton;
    @Bind(R.id.flashButton)
    ImageButton flashButton;
    @Bind(R.id.shareButton)
    ImageButton shareButton;
    @Bind(R.id.skinWoodButton)
    ImageButton skinWoodButton;
    @Bind(R.id.skinLeatherButton)
    ImageButton skinLeatherButton;
    @Bind(R.id.filterBlueButton)
    ImageButton filterBlueButton;
    @Bind(R.id.filterBlackAndWhiteButton)
    ImageButton filterBlackAndWhiteButton;
    @Bind(R.id.filterSepiaButton)
    ImageButton filterSepiaButton;
    @Bind(R.id.settingsButton)
    ImageButton settingsButton;

    private RecordPresenter recordPresenter;
    private boolean buttonBackPressed;
    private boolean recording;
    private AlertDialog progressDialog;
    private boolean mUseImmersiveMode = true;

    //TODO sacar esta variable de aquí (hay que guardarlo en disco: shared prefs o algo así)
    private int backgroundResourceId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_record);
        ButterKnife.bind(this);

        changeSkin(R.mipmap.activity_record_background_leather);
        cameraView.setKeepScreenOn(true);

        SharedPreferences sharedPreferences = getSharedPreferences(
                ConfigPreferences.SETTINGS_SHARED_PREFERENCES_FILE_NAME,
                Context.MODE_PRIVATE);
        recordPresenter = new RecordPresenter(this, this, cameraView, sharedPreferences);
        createProgressDialog();
    }

    private void createProgressDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_export_progress, null);
        progressDialog = builder.setCancelable(false)
                .setView(dialogView)
                .create();
    }

    @Override
    protected void onStart() {
        super.onStart();
        checkAndRequestPermissions();
        recordPresenter.onStart();
    }

    @Override
    protected void onResume() {
        super.onResume();
        recordPresenter.onResume();
        recording = false;
        hideSystemUi();
        checkNewNotification();
    }

    private void hideSystemUi() {
        if (!Utils.isKitKat() || !mUseImmersiveMode) {
            getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                    WindowManager.LayoutParams.FLAG_FULLSCREEN);
        } else if (mUseImmersiveMode) {
            setKitKatWindowFlags();
        }
    }

    private void checkNewNotification(){
        InAppNotification notification = mixpanel.getPeople().getNotificationIfAvailable();
        if (notification != null) {
            mixpanel.getPeople().showGivenNotification(notification, this);
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        recordPresenter.onPause();
    }

    @Override
    protected void onStop() {
        super.onStop();
        recordPresenter.onStop();
        finish();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        recordPresenter.onDestroy();
    }

    @TargetApi(Build.VERSION_CODES.KITKAT)
    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        if (Utils.isKitKat() && hasFocus && mUseImmersiveMode) {
            setKitKatWindowFlags();
        }
    }

    @TargetApi(Build.VERSION_CODES.KITKAT)
    private void setKitKatWindowFlags() {
        getWindow().getDecorView().setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                        | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                        | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_FULLSCREEN
                        | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
        );
    }

    @OnTouch(R.id.recordButton)
    boolean onTouch(MotionEvent event) {
        if (event.getAction() == MotionEvent.ACTION_DOWN) {
            if (!recording) {
                recordPresenter.requestRecord();
            } else {
                recordPresenter.stopRecord();
            }
        }
        return true;
    }

    @OnClick(R.id.flashButton)
    public void toggleFlash() {
        recordPresenter.toggleFlash();
    }

    @OnClick(R.id.toggleCameraButton)
    public void changeCamera() {
        recordPresenter.setFlashOff();
        recordPresenter.changeCamera();
    }

    @OnClick(R.id.skinLeatherButton)
    public void changeToLeatherSkin() {
        recordPresenter.changeToLeatherSkin();
    }

    @OnClick(R.id.skinWoodButton)
    public void changeToWoodSkin() {
        recordPresenter.changeToWoodSkin();
    }

    @OnClick(R.id.shareButton)
    public void exportAndShare() {
        if (!recording) {
            showProgressDialog();
            sendMetadataTracking();
            startExportThread();
        }
    }

    @OnClick(R.id.settingsButton)
    public void goToSettings(){
        sendUserInteractedTracking();
        Intent intent = new Intent(this, SettingsActivity.class);
        startActivity(intent);

    }

    private void sendUserInteractedTracking() {
        JSONObject userInteractionsProperties = new JSONObject();
        try {
            userInteractionsProperties.put("acitivity", getClass().getSimpleName());
            userInteractionsProperties.put("recording", recording);
            userInteractionsProperties.put("interaction", "open settings");
            userInteractionsProperties.put("result", null);
            mixpanel.track("User Interacted", userInteractionsProperties);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private void sendMetadataTracking() {
        mixpanel.timeEvent("Time Exporting Video");
        JSONObject videoExportedProperties = new JSONObject();
        try {
            videoExportedProperties.put("videoLength", recordPresenter.getVideoLength());
            videoExportedProperties.put("resolution", recordPresenter.getResolution());
            videoExportedProperties.put("numberOfClips", recordPresenter.getNumberOfClipsRecorded());
            mixpanel.track("Video Exported", videoExportedProperties);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private void startExportThread() {
        final Thread t = new Thread() {
            @Override
            public void run() {
                recordPresenter.startExport();
            }
        };
        t.start();
    }

    @OnClick(R.id.filterBlackAndWhiteButton)
    public void selectBlackAndWhiteFilter() {
        recordPresenter.setBlackAndWitheFilter();
        resetSelections();
        filterBlackAndWhiteButton.setSelected(true);
    }

    @OnClick(R.id.filterSepiaButton)
    public void selectSepiaFilter() {
        recordPresenter.setSepiaFilter();
        resetSelections();
        filterSepiaButton.setSelected(true);
    }

    @OnClick(R.id.filterBlueButton)
    public void selectBlueFilter() {
        recordPresenter.setBlueFilter();
        resetSelections();
        filterBlueButton.setSelected(true);
    }

    private void resetSelections() {
        filterBlackAndWhiteButton.setSelected(false);
        filterBlueButton.setSelected(false);
        filterSepiaButton.setSelected(false);
    }

    @Override
    public void showRecordButton() {
        recButton.setActivated(false);
        recButton.setAlpha(1f);
        recording = false;
    }

    @Override
    public void showStopButton() {
        recButton.setActivated(true);
        recButton.setAlpha(1f);
        recording = true;
    }

    @Override
    public void showFlashOn(boolean on) {
        flashButton.setActivated(on);
    }

    @Override
    public void showFlashSupported(boolean supported) {
        if (supported) {
            flashButton.setImageAlpha(255);
            flashButton.setActivated(false);
            flashButton.setActivated(false);
            flashButton.setEnabled(true);
        } else {
            flashButton.setImageAlpha(65);
            flashButton.setActivated(false);
            flashButton.setEnabled(false);
        }
    }

    @Override
    public void showFrontCameraSelected() {
        rotateCameraButton.setActivated(false);
    }

    @Override
    public void showBackCameraSelected() {
        rotateCameraButton.setActivated(false);
    }

    @Override
    public void showError(String errorMessage) {
        mixpanel.track("Time Exporting Video");
        Toast.makeText(this, errorMessage, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void showError(int stringResourceId) {
        Toast.makeText(this, this.getText(stringResourceId), Toast.LENGTH_SHORT).show();
    }

    @Override
    public void goToShare(String videoToSharePath) {
        mixpanel.track("Time Exporting Video");
        recordPresenter.removeTempVideos();
        Intent intent = new Intent(this, ShareActivity.class);
        intent.putExtra(ShareActivity.INTENT_EXTRA_VIDEO_PATH, videoToSharePath);
        //TODO once the resource id is saved on shared preference the extra will be necessary
        intent.putExtra(ShareActivity.INTENT_EXTRA_BACKGROND, backgroundResourceId);
        startActivity(intent);
    }

    @Override
    public void onBackPressed() {
        if (buttonBackPressed) {
            buttonBackPressed = false;
            Intent intent = new Intent(Intent.ACTION_MAIN);
            intent.addCategory(Intent.CATEGORY_HOME);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);//***Change Here***
            startActivity(intent);
            finish();
            System.exit(0);
        } else {
            buttonBackPressed = true;
            Toast.makeText(getApplicationContext(), getString(R.string.toast_exit),
                    Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void showProgressDialog() {
        progressDialog.show();
    }

    @Override
    public void hideProgressDialog() {
        if (progressDialog != null && progressDialog.isShowing())
            progressDialog.dismiss();
    }

    @Override
    public void showMessage(final int message) {
        this.runOnUiThread(new Runnable() {
            public void run() {
                Toast.makeText(getApplicationContext(), message, Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public void enableShareButton() {
        shareButton.setAlpha(1f);
        shareButton.setClickable(true);
    }

    @Override
    public void disableShareButton() {
        shareButton.setAlpha(0.25f);
        shareButton.setClickable(false);
    }

    @Override
    public void changeSkin(int backgroundId) {
        recordLayout.setBackgroundResource(backgroundId);
        backgroundResourceId = backgroundId;
    }

    @Override
    public void showSkinWoodButton() {
        skinLeatherButton.setVisibility(View.GONE);
        skinWoodButton.setVisibility(View.VISIBLE);
    }

    @Override
    public void showSkinLeatherButton() {
        skinWoodButton.setVisibility(View.GONE);
        skinLeatherButton.setVisibility(View.VISIBLE);
    }

}
