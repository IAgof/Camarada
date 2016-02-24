package com.videonasocialmedia.kamarada.presentation.views.activity;

import android.annotation.TargetApi;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.PorterDuff;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.videonasocialmedia.avrecorder.Filters;
import com.videonasocialmedia.avrecorder.view.GLCameraEncoderView;
import com.videonasocialmedia.kamarada.R;
import com.videonasocialmedia.kamarada.presentation.helper.HorizontalGestureDetectorHelper;
import com.videonasocialmedia.kamarada.presentation.listener.OnSwipeListener;
import com.videonasocialmedia.kamarada.presentation.mvp.presenters.RecordPresenter;
import com.videonasocialmedia.kamarada.presentation.mvp.views.EffectSelectorView;
import com.videonasocialmedia.kamarada.presentation.mvp.views.RecordView;
import com.videonasocialmedia.kamarada.utils.AnalyticsConstants;
import com.videonasocialmedia.kamarada.utils.ConfigPreferences;
import com.videonasocialmedia.kamarada.utils.Utils;

import org.json.JSONException;
import org.json.JSONObject;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.OnTouch;

/**
 * Created by Veronica Lago Fominaya on 19/01/2016.
 */

public class RecordActivity extends KamaradaActivity implements RecordView, OnSwipeListener,
        EffectSelectorView {

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
    @Bind(R.id.textFilterSelected)
    TextView textFilterSelected;
    @Bind(R.id.settingsButton)
    ImageButton settingsButton;
    @Bind(R.id.progressBar)
    ProgressBar progressBar;

    private RecordPresenter recordPresenter;
    private boolean buttonBackPressed;
    private boolean recording;
    private AlertDialog progressDialog;
    private boolean mUseImmersiveMode = true;
    private HorizontalGestureDetectorHelper gestureDetecorHelper;
    private int progressTime = 0;
    private int startTime = 0;


    //TODO sacar esta variable de aquí (hay que guardarlo en disco: shared prefs o algo así)
    private int backgroundResourceId;

    Handler timerHandler = new Handler();
    Runnable timerRunnable = new Runnable() {
        long delay = 100;
        @Override
        public void run() {
            progressTime  += delay;
            if(progressTime >= progressBar.getMax())
                progressBar.setMax(progressBar.getMax()*4);
            progressBar.setProgress(Math.round(progressTime));
            timerHandler.postDelayed(this, delay);
        }
    };


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_record);
        ButterKnife.bind(this);

        changeSkin(R.mipmap.activity_record_background_leather);

        cameraView.setKeepScreenOn(true);
        gestureDetecorHelper = new HorizontalGestureDetectorHelper(this, this);
        cameraView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                //gesture detector to detect swipe.
                return gestureDetecorHelper.getGestureDetector().onTouchEvent(event);
            }
        });

        shareButton.setClickable(false);

        SharedPreferences sharedPreferences = getSharedPreferences(
                ConfigPreferences.SETTINGS_SHARED_PREFERENCES_FILE_NAME,
                Context.MODE_PRIVATE);
        recordPresenter = new RecordPresenter(this, this, this, cameraView, sharedPreferences);
        createProgressDialog();
        initProgressBar();
    }

    private void createProgressDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_export_progress, null);
        progressDialog = builder.setCancelable(false)
                .setView(dialogView)
                .create();
    }

    private void initProgressBar() {
        int currentapiVersion = android.os.Build.VERSION.SDK_INT;
        if (currentapiVersion < android.os.Build.VERSION_CODES.LOLLIPOP)
            progressBar.getBackground().setColorFilter(getResources().getColor(R.color.colorAccent),
                    PorterDuff.Mode.SRC_ATOP);
        progressBar.setScaleY(3f);
        progressBar.setMax(10000);
    }

    @Override
    public void startProgressBar() {
        timerHandler.postDelayed(timerRunnable, 0);
    }

    @Override
    public void stopProgressBar() {
        timerHandler.removeCallbacks(timerRunnable);
    }

    @Override
    protected void onStart() {
        super.onStart();
        checkAndRequestPermissions();
        recordPresenter.onStart();
    }

    @Override
    public void onPause() {
        super.onPause();
        recordPresenter.onPause();
        timerHandler.removeCallbacks(timerRunnable);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        recordPresenter.onDestroy();
    }

    @Override
    protected void onResume() {
        super.onResume();
        recordPresenter.onResume();
        recording = false;
        hideSystemUi();
    }

    private void hideSystemUi() {
        if (!Utils.isKitKatOrHigher() || !mUseImmersiveMode) {
            getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                    WindowManager.LayoutParams.FLAG_FULLSCREEN);
        } else if (mUseImmersiveMode) {
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

    @TargetApi(Build.VERSION_CODES.KITKAT)
    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        if (Utils.isKitKatOrHigher() && hasFocus && mUseImmersiveMode) {
            setKitKatWindowFlags();
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        recordPresenter.onStop();
        finish();
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
    public void share() {
        goToShare(recordPresenter.getRecordedVideoPath());
    }


    private void startExportThread() {

    }

    @OnClick(R.id.settingsButton)
    public void goToSettings() {
        sendUserInteractedTracking(AnalyticsConstants.INTERACTION_OPEN_SETTINGS, null);
        Intent intent = new Intent(this, SettingsActivity.class);
        startActivity(intent);
    }

    private void sendUserInteractedTracking(String interaction, String result) {
        JSONObject userInteractionsProperties = new JSONObject();
        try {
            userInteractionsProperties.put(AnalyticsConstants.ACTIVITY, getClass().getSimpleName());
            userInteractionsProperties.put(AnalyticsConstants.RECORDING, recording);
            userInteractionsProperties.put(AnalyticsConstants.INTERACTION, interaction);
            userInteractionsProperties.put(AnalyticsConstants.RESULT, result);
            mixpanel.track(AnalyticsConstants.USER_INTERACTED, userInteractionsProperties);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    @OnClick(R.id.filterBlackAndWhiteButton)
    public void selectBlackAndWhiteFilter() {
        recordPresenter.setBlackAndWitheFilter();
    }

    @OnClick(R.id.filterSepiaButton)
    public void selectSepiaFilter() {
        recordPresenter.setSepiaFilter();
    }

    @OnClick(R.id.filterBlueButton)
    public void selectBlueFilter() {
        recordPresenter.setBlueFilter();
    }

    @Override
    public void showSepiaSelected() {
        resetSelections();
        sendFilterSelectedTracking(AnalyticsConstants.FILTER_NAME_SEPIA,
                AnalyticsConstants.FILTER_CODE_SEPIA);
        filterSepiaButton.setSelected(true);
    }

    @Override
    public void showBlackAndWhiteSelected() {
        resetSelections();
        sendFilterSelectedTracking(AnalyticsConstants.FILTER_NAME_MONO,
                AnalyticsConstants.FILTER_CODE_MONO);
        filterBlackAndWhiteButton.setSelected(true);
    }

    @Override
    public void showBlueSelected() {
        resetSelections();
        sendFilterSelectedTracking(AnalyticsConstants.FILTER_NAME_AQUA,
                AnalyticsConstants.FILTER_CODE_AQUA);
        filterBlueButton.setSelected(true);
    }

    @Override
    public void showFilterSelectedText(String text) {
        textFilterSelected.setText(text);

        // make TextView visible here
        textFilterSelected.setVisibility(View.VISIBLE);
        //use postDelayed to hide TextView
        textFilterSelected.postDelayed(new Runnable() {
            public void run() {
                textFilterSelected.setVisibility(View.INVISIBLE);
            }
        }, 2000);
    }

    private void resetSelections() {
        filterBlackAndWhiteButton.setSelected(false);
        filterBlueButton.setSelected(false);
        filterSepiaButton.setSelected(false);
    }

    private void sendFilterSelectedTracking(String name, String code) {
        JSONObject userInteractionsProperties = new JSONObject();
        try {
            userInteractionsProperties.put(AnalyticsConstants.TYPE, AnalyticsConstants.TYPE_COLOR);
            userInteractionsProperties.put(AnalyticsConstants.NAME, name);
            userInteractionsProperties.put(AnalyticsConstants.CODE, code);
            userInteractionsProperties.put(AnalyticsConstants.RECORDING, recording);
            mixpanel.track(AnalyticsConstants.FILTER_SELECTED, userInteractionsProperties);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void showRecordButton() {
        recButton.setActivated(false);
        shareButton.setClickable(true);
        enableSettingsButton();
        recording = false;
    }

    private void enableSettingsButton() {
        settingsButton.setClickable(true);
        settingsButton.setAlpha(1f);
    }

    @Override
    public void showStopButton() {
        recButton.setActivated(true);
        disableSettingsButton();
        recording = true;
        trackSelectedFilterOnStartRecording();
    }

    private void disableSettingsButton() {
        settingsButton.setClickable(false);
        settingsButton.setAlpha(0.25f);
    }

    private void trackSelectedFilterOnStartRecording() {
        int filterId = recordPresenter.getFilterSelectedId();
        switch (filterId) {
            case Filters.FILTER_SEPIA:
                sendFilterSelectedTracking(AnalyticsConstants.FILTER_NAME_SEPIA,
                        AnalyticsConstants.FILTER_CODE_SEPIA);
                break;
            case Filters.FILTER_MONO:
                sendFilterSelectedTracking(AnalyticsConstants.FILTER_NAME_MONO,
                        AnalyticsConstants.FILTER_CODE_MONO);
                break;
            case Filters.FILTER_AQUA:
                sendFilterSelectedTracking(AnalyticsConstants.FILTER_NAME_AQUA,
                        AnalyticsConstants.FILTER_CODE_AQUA);
                break;
        }
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
        Toast.makeText(this, errorMessage, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void showError(int stringResourceId) {
        Toast.makeText(this, this.getText(stringResourceId), Toast.LENGTH_SHORT).show();
    }

    @Override
    public void goToShare(String videoToSharePath) {
        Intent intent = new Intent(this, ShareActivity.class);
        intent.putExtra(ShareActivity.INTENT_EXTRA_VIDEO_PATH, videoToSharePath);
        //TODO once the resource id is saved on shared preference the extra will be necessary
        intent.putExtra(ShareActivity.INTENT_EXTRA_BACKGROND, backgroundResourceId);
        startActivity(intent);
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
    public void onSwipeLeft() {
        sendUserInteractedTracking(AnalyticsConstants.SWIPE, AnalyticsConstants.LEFT);
        recordPresenter.setPrevFilter();
    }

    @Override
    public void onSwipeRight() {
        sendUserInteractedTracking(AnalyticsConstants.SWIPE, AnalyticsConstants.RIGHT);
        recordPresenter.setNextFilter();
    }

}
