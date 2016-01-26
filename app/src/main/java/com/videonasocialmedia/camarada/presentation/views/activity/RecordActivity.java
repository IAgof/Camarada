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
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.analytics.GoogleAnalytics;
import com.google.android.gms.analytics.HitBuilders;
import com.videonasocialmedia.avrecorder.view.GLCameraEncoderView;
import com.videonasocialmedia.camarada.R;
import com.videonasocialmedia.camarada.model.entities.editor.effects.Effect;
import com.videonasocialmedia.camarada.presentation.adapter.EffectAdapter;
import com.videonasocialmedia.camarada.presentation.listener.OnEffectSelectedListener;
import com.videonasocialmedia.camarada.presentation.listener.OnSwipeListener;
import com.videonasocialmedia.camarada.presentation.listener.OnSwipeTouchListener;
import com.videonasocialmedia.camarada.presentation.mvp.presenters.RecordPresenter;
import com.videonasocialmedia.camarada.presentation.mvp.views.RecordView;
import com.videonasocialmedia.camarada.utils.ConfigPreferences;
import com.videonasocialmedia.camarada.utils.Utils;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.OnTouch;

/**
 * Created by Veronica Lago Fominaya on 19/01/2016.
 */
public class RecordActivity extends CamaradaActivity implements RecordView, OnEffectSelectedListener,
        OnSwipeListener {

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
    //@Bind(R.id.manualPreview)
    //View swipeFiltersView;
    OnSwipeTouchListener swipeListener;
    @Bind(R.id.textFilterSelected)
    TextView textFilterSelected;

    private RecordPresenter recordPresenter;
    private boolean buttonBackPressed;
    private boolean recording;
    private AlertDialog progressDialog;
    private boolean mUseImmersiveMode = true;
    private EffectAdapter cameraShaderEffectsAdapter;
    private enum SWIPE_TYPE { LEFT, RIGHT  }


    //TODO sacar esta variable de aquí (hay que guardarlo en disco: shared prefs o algo así)
    private int backgroundResourceId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_record);
        ButterKnife.bind(this);

        changeSkin(R.mipmap.activity_record_background_leather);
        cameraView.setKeepScreenOn(true);

        swipeListener = new OnSwipeTouchListener(this, this);

        cameraView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                //gesture detector to detect swipe.
                return swipeListener.getGestureDetector().onTouchEvent(event);
            }
        });

        SharedPreferences sharedPreferences = getSharedPreferences(
                ConfigPreferences.SETTINGS_SHARED_PREFERENCES_FILE_NAME,
                Context.MODE_PRIVATE);
        recordPresenter = new RecordPresenter(this, this, cameraView, sharedPreferences);
        createProgressDialog();

        cameraShaderEffectsAdapter = new EffectAdapter(recordPresenter.getShaderEffectList(), this);
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
        mixpanel.timeEvent("Time in Record Activity");
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

    @Override
    public void onPause() {
        super.onPause();
        recordPresenter.onPause();
        mixpanel.track("Time in Record Activity");
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
        if (Utils.isKitKatOrHigher() && hasFocus && mUseImmersiveMode) {
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


    /*
    public boolean dispatchTouchEvent(MotionEvent ev){
        swipeListener.getGestureDetector().onTouchEvent(ev);
        return super.dispatchTouchEvent(ev);
    } */

    @OnTouch(R.id.recordButton)
    boolean onTouch(MotionEvent event) {
        if (event.getAction() == MotionEvent.ACTION_DOWN) {
            if (!recording) {
                recordPresenter.requestRecord();
                mixpanel.timeEvent("Time recording one video");
                mixpanel.track("Start recording");
            } else {
                recordPresenter.stopRecord();
                mixpanel.track("Time recording one video");
                mixpanel.track("Stop recording");
            }
        }
        return true;
    }

    @OnClick(R.id.flashButton)
    public void toggleFlash() {
        recordPresenter.toggleFlash();
        mixpanel.track("Toggle flash Button clicked", null);
    }

    @OnClick(R.id.toggleCameraButton)
    public void changeCamera() {
        recordPresenter.setFlashOff();
        recordPresenter.changeCamera();
        if (recording)
            mixpanel.track("Change camera Button clicked while recording", null);
        else
            mixpanel.track("Change camera Button clicked on preview", null);
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

        Intent intent = new Intent(this, SettingsActivity.class);
        startActivity(intent);

    }

    private void sendMetadataTracking() {
//        try {
//            int projectDuration = recordPresenter.getProjectDuration();
//            int numVideosOnProject = recordPresenter.getNumVideosOnProject();
//            JSONObject props = new JSONObject();
//            props.put("Number of videos", numVideosOnProject);
//            props.put("Duration of the exported video in msec", projectDuration);
//            mixpanel.track("Exported video", props);
//        } catch (JSONException e) {
//            Log.e("TRACK_FAILED", String.valueOf(e));
//        }
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

        setFilerButtonSelected(0);
    }

    @OnClick(R.id.filterSepiaButton)
    public void selectSepiaFilter() {
        recordPresenter.setSepiaFilter();

        setFilerButtonSelected(1);
    }

    @OnClick(R.id.filterBlueButton)
    public void selectBlueFilter() {
        recordPresenter.setBlueFilter();

        setFilerButtonSelected(2);
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
        mixpanel.track("Front camera selected", null);
    }

    @Override
    public void showBackCameraSelected() {
        rotateCameraButton.setActivated(false);
        mixpanel.track("Back camera selected", null);
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

    @OnClick({R.id.recordButton, R.id.flashButton, R.id.toggleCameraButton})
    public void clickListener(View view) {
        sendButtonTracked(view.getId());
    }

    private void sendButtonTracked(String label) {
        tracker.send(new HitBuilders.EventBuilder()
                .setCategory("RecordActivity")
                .setAction("button clicked")
                .setLabel(label)
                .build());
        GoogleAnalytics.getInstance(this.getApplication().getBaseContext()).dispatchLocalHits();
    }

    /**
     * Sends button clicks to Google Analytics
     *
     * @param id identifier of the clicked view
     */
    private void sendButtonTracked(int id) {
        String label;
        switch (id) {
            case R.id.recordButton:
                label = "Capture";
                break;
            case R.id.toggleCameraButton:
                label = "Change camera";
                break;
            case R.id.flashButton:
                label = "Flash camera";
                break;
            default:
                label = "Other";
        }
        sendButtonTracked(label);
    }

    @Override
    public void onEffectSelected(Effect effect) {
        recordPresenter.applyEffect(effect);
        sendButtonTracked(effect.getIconId());
    }

    @Override
    public void onEffectSelectionCancel(Effect effect) {
        recordPresenter.removeEffect(effect);

    }

    @Override
    public void onSwipeLeft() {
        effectChangedSwipe(SWIPE_TYPE.LEFT);
    }

    @Override
    public void onSwipeRight() {
        effectChangedSwipe(SWIPE_TYPE.RIGHT);
    }


    private void effectChangedSwipe(SWIPE_TYPE swipe) {

        int position = cameraShaderEffectsAdapter.getSelectionPosition();

        int sizeEffectList = cameraShaderEffectsAdapter.getElementList().size() - 1;

        switch(swipe) {
            case LEFT:
                if (position == 0) {
                    position = sizeEffectList;
                } else {
                    position--;
                }
                break;
            case RIGHT:
                if(position == sizeEffectList){
                    position = 0;
                } else {
                    position++;
                }
                break;
        }

        Effect effect = cameraShaderEffectsAdapter.getEffect(position);

        onEffectSelected(effect);

        cameraShaderEffectsAdapter.setNextEffectPosition(position);

        setFilerButtonSelected(position);
    }

    private void setFilerButtonSelected(int position) {

        resetSelections();
        if(position == 0) {
            filterBlackAndWhiteButton.setSelected(true);
        } else {
            if(position == 1){
                filterSepiaButton.setSelected(true);
            } else {
                if(position == 2) {
                    filterBlueButton.setSelected(true);
                }
            }
        }


        textFilterSelected.setText(cameraShaderEffectsAdapter.getEffect(position).getName());

        // make TextView visible here
        textFilterSelected.setVisibility(View.VISIBLE);
        //use postDelayed to hide TextView
        textFilterSelected.postDelayed(new Runnable() {
            public void run() {
                textFilterSelected.setVisibility(View.INVISIBLE);
            }
        }, 2000);


    }

}
