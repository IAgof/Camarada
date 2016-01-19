package com.videonasocialmedia.camarada.presentation.views.activity;

import android.app.AlertDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.ImageButton;

import com.videonasocialmedia.avrecorder.view.GLCameraEncoderView;
import com.videonasocialmedia.camarada.R;
import com.videonasocialmedia.camarada.presentation.mvp.presenters.RecordPresenter;
import com.videonasocialmedia.camarada.presentation.mvp.views.RecordView;
import com.videonasocialmedia.camarada.utils.ConfigPreferences;

import butterknife.Bind;
import butterknife.ButterKnife;

/**
 * Created by Veronica Lago Fominaya on 19/01/2016.
 */
public class RecordActivity extends CamaradaActivity implements RecordView {

    @Bind(R.id.recordButton)
    ImageButton recButton;
    @Bind(R.id.cameraPreview)
    GLCameraEncoderView cameraView;
    @Bind(R.id.toggleCameraButton)
    ImageButton rotateCameraButton;
    @Bind(R.id.settingsButton)
    ImageButton settingsButton;
    @Bind(R.id.flashButton)
    ImageButton flashButton;
    @Bind(R.id.shareButton)
    ImageButton shareButton;

    private final String LOG_TAG = getClass().getSimpleName();
    private RecordPresenter recordPresenter;
    private boolean buttonBackPressed;
    private boolean recording;
    private AlertDialog progressDialog;
    private boolean mUseImmersiveMode = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_record);
        ButterKnife.bind(this);

        cameraView.setKeepScreenOn(true);

        SharedPreferences sharedPreferences = getSharedPreferences(
                ConfigPreferences.SETTINGS_SHARED_PREFERENCES_FILE_NAME,
                Context.MODE_PRIVATE);
        recordPresenter = new RecordPresenter(this, this, cameraView, sharedPreferences);
        createProgressDialog();
    }

    private void createProgressDialog() {
//        AlertDialog.Builder builder = new AlertDialog.Builder(this);
//        View dialogView = getLayoutInflater().inflate(R.layout.dialog_export_progress, null);
//        progressDialog = builder.setCancelable(false)
//                .setView(dialogView)
//                .create();
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

    @Override
    public void showRecordButton() {

    }

    @Override
    public void showStopButton() {

    }

    @Override
    public void showSettings() {

    }

    @Override
    public void hideSettings() {

    }

    @Override
    public void lockScreenRotation() {

    }

    @Override
    public void unlockScreenRotation() {

    }

    @Override
    public void reStartScreenRotation() {

    }

    @Override
    public void showFlashOn(boolean on) {

    }

    @Override
    public void showFlashSupported(boolean state) {

    }

    @Override
    public void showFrontCameraSelected() {

    }

    @Override
    public void showBackCameraSelected() {

    }

    @Override
    public void showError(String errorMessage) {

    }

    @Override
    public void showError(int stringResourceId) {

    }

    @Override
    public void goToShare(String videoToSharePath) {

    }

    @Override
    public void showProgressDialog() {

    }

    @Override
    public void hideProgressDialog() {

    }

    @Override
    public void showMessage(int stringToast) {

    }

    @Override
    public void enableShareButton() {

    }

    @Override
    public void disableShareButton() {

    }
}
