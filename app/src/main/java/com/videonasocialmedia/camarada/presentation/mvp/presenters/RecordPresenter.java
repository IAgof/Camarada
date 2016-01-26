package com.videonasocialmedia.camarada.presentation.mvp.presenters;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.drawable.Drawable;
import android.support.annotation.NonNull;
import android.util.Log;

import com.videonasocialmedia.avrecorder.AVRecorder;
import com.videonasocialmedia.avrecorder.Filters;
import com.videonasocialmedia.avrecorder.SessionConfig;
import com.videonasocialmedia.avrecorder.event.CameraEncoderResetEvent;
import com.videonasocialmedia.avrecorder.event.MuxerFinishedEvent;
import com.videonasocialmedia.avrecorder.view.GLCameraEncoderView;
import com.videonasocialmedia.camarada.R;
import com.videonasocialmedia.camarada.domain.ExportUseCase;
import com.videonasocialmedia.camarada.domain.GetVideosFromTempFolderUseCase;
import com.videonasocialmedia.camarada.domain.OnExportFinishedListener;
import com.videonasocialmedia.camarada.domain.RemoveFilesInTempFolderUseCase;
import com.videonasocialmedia.camarada.presentation.mvp.views.RecordView;
import com.videonasocialmedia.camarada.utils.Constants;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import de.greenrobot.event.EventBus;

/**
 * Created by Veronica Lago Fominaya on 19/01/2016.
 */
public class RecordPresenter implements OnExportFinishedListener {

    /**
     * LOG_TAG
     */
    private static final String LOG_TAG = "RecordPresenter";
    private boolean firstTimeRecording;
    private RecordView recordView;
    private SessionConfig config;
    private AVRecorder recorder;
    private SharedPreferences sharedPreferences;
    private Context context;
    private GLCameraEncoderView cameraPreview;
    /**
     * Export project use case
     */
    private ExportUseCase exportUseCase;
    /**
     * Get media list use case
     */
    private GetVideosFromTempFolderUseCase getVideosFromTempFolderUseCase;
    private RemoveFilesInTempFolderUseCase removeFilesInTempFolderUseCase;
    private int selectedFilter;

    public RecordPresenter(Context context, RecordView recordView,
                           GLCameraEncoderView cameraPreview, SharedPreferences sharedPreferences) {
        Log.d(LOG_TAG, "constructor presenter");
        this.recordView = recordView;
        this.context = context;
        this.cameraPreview = cameraPreview;
        this.sharedPreferences = sharedPreferences;
        exportUseCase = new ExportUseCase(this);
        getVideosFromTempFolderUseCase = new GetVideosFromTempFolderUseCase();
        removeFilesInTempFolderUseCase = new RemoveFilesInTempFolderUseCase();
        initRecorder(cameraPreview, sharedPreferences);
    }

    private void initRecorder(GLCameraEncoderView cameraPreview,
                              SharedPreferences sharedPreferences) {

        config = getConfigFromPreferences(sharedPreferences);
        try {
            recorder = new AVRecorder(config);
            recorder.setPreviewDisplay(cameraPreview);
            setBlackAndWitheFilter();
            List<Drawable> animatedOverlayFrames = getAnimatedOverlay();
            recorder.addAnimatedOverlayFilter(animatedOverlayFrames);
            firstTimeRecording = true;
        } catch (IOException ioe) {
            Log.e("ERROR", "ERROR", ioe);
        }
    }

    @NonNull
    private List<Drawable> getAnimatedOverlay() {
        List<Drawable> animatedOverlayFrames = new ArrayList<>();
        animatedOverlayFrames.add(context.getResources().getDrawable(R.mipmap.silent_film_overlay_a));
        animatedOverlayFrames.add(context.getResources().getDrawable(R.mipmap.silent_film_overlay_b));
        animatedOverlayFrames.add(context.getResources().getDrawable(R.mipmap.silent_film_overlay_c));
        animatedOverlayFrames.add(context.getResources().getDrawable(R.mipmap.silent_film_overlay_d));
        animatedOverlayFrames.add(context.getResources().getDrawable(R.mipmap.silent_film_overlay_e));
        animatedOverlayFrames.add(context.getResources().getDrawable(R.mipmap.silent_film_overlay_f));
        animatedOverlayFrames.add(context.getResources().getDrawable(R.mipmap.silent_film_overlay_g));
        animatedOverlayFrames.add(context.getResources().getDrawable(R.mipmap.silent_film_overlay_h));
        animatedOverlayFrames.add(context.getResources().getDrawable(R.mipmap.silent_film_overlay_i));
        animatedOverlayFrames.add(context.getResources().getDrawable(R.mipmap.silent_film_overlay_j));
        return animatedOverlayFrames;
    }

    private SessionConfig getConfigFromPreferences(SharedPreferences sharedPreferences) {
        // TODO comprobar la máxima resolución que puede coger y usarla aquí
        String destinationFolderPath = Constants.PATH_APP_TEMP;
        int width = 640;
        int height = 480;
        int videoBitrate = 5000000;
        int audioChannels = 1;
        int audioFrequency = 48000;
        int audioBitrate = 192 * 1000;

        return new SessionConfig(destinationFolderPath, width, height, videoBitrate,
                audioChannels, audioFrequency, audioBitrate);
    }

    public void onStart() {
        if (recorder.isReleased()) {
            cameraPreview.releaseCamera();
            initRecorder(cameraPreview, sharedPreferences);
        }
    }

    public void onResume() {
        EventBus.getDefault().register(this);
        recorder.onHostActivityResumed();
        recorder.applyFilter(selectedFilter);
    }

    public void onPause() {
        EventBus.getDefault().unregister(this);
        stopRecord();
        recorder.onHostActivityPaused();
    }

    public void stopRecord() {
        if (recorder.isRecording()) {
            recorder.stopRecording();
            recordView.showRecordButton();
            recordView.enableShareButton();
        }
        //TODO show a gif to indicate the process is running til the video is added to the project
    }

    public void onStop() {
        recorder.release();
    }

    public void onDestroy() {
        //recorder.release();
    }

    public void requestRecord() {
        if (!recorder.isRecording()) {
            if (!firstTimeRecording) {
                try {
                    resetRecorder();
                } catch (IOException ioe) {
                    //recordView.showError();
                }
            } else {
                startRecord();
            }
        }
    }

    private void resetRecorder() throws IOException {
        config = getConfigFromPreferences(sharedPreferences);
        recorder.reset(config);
    }

    public void onEventMainThread(CameraEncoderResetEvent e) {
        recorder.applyFilter(selectedFilter);
        startRecord();
    }

    private void startRecord() {
        recorder.startRecording();
        recordView.showStopButton();
        recordView.disableShareButton();
        firstTimeRecording = false;
    }

    public void startExport() {
        List<String> videoList = getVideosFromTempFolderUseCase.getVideosFromTempFolder();
        if (videoList.size() > 0) {
            exportUseCase.export(videoList);
        } else {
            recordView.hideProgressDialog();
            recordView.showMessage(R.string.add_videos_to_project);
        }
    }

    public void removeTempVideos() {
        removeFilesInTempFolderUseCase.removeFilesInTempFolder();
    }


    public void onEventMainThread(MuxerFinishedEvent e) {
        renameOutputVideo(config.getOutputPath());
    }

    private void renameOutputVideo(String path) {
        File originalFile = new File(path);
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String fileName = "VID_" + timeStamp + ".mp4";
        File destinationFile = new File(Constants.PATH_APP_TEMP, fileName);
        originalFile.renameTo(destinationFile);
    }

    public void changeCamera() {
        //TODO controlar el estado del flash
        int camera = recorder.requestOtherCamera();
        if (camera == 0) {
            recordView.showBackCameraSelected();
        } else {
            if (camera == 1) {
                recordView.showFrontCameraSelected();
            }
        }
        checkFlashSupport();
    }

    public void checkFlashSupport() {
        int flashSupport = recorder.checkSupportFlash(); // 0 true, 1 false, 2 ignoring, not prepared

        if (flashSupport == 0)
            recordView.showFlashSupported(true);
        else if (flashSupport == 1)
            recordView.showFlashSupported(false);
    }

    public void setFlashOff() {
        boolean on = recorder.setFlashOff();
        recordView.showFlashOn(on);
    }

    public void toggleFlash() {
        boolean on = recorder.toggleFlash();
        recordView.showFlashOn(on);
    }

    public void changeToWoodSkin() {
        recordView.changeSkin(R.mipmap.activity_record_background_wood);
        recordView.showSkinLeatherButton();
    }

    public void changeToLeatherSkin() {
        recordView.changeSkin(R.mipmap.activity_record_background_leather);
        recordView.showSkinWoodButton();
    }

    @Override
    public void onExportError(String error) {
        recordView.hideProgressDialog();
        recordView.showError(R.string.errorExportingVideo);
    }

    @Override
    public void onExportSuccess(String path) {
        recordView.hideProgressDialog();
        recordView.goToShare(path);
    }

    public void setSepiaFilter() {
        selectedFilter = Filters.FILTER_SEPIA;
        recorder.applyFilter(Filters.FILTER_SEPIA);
    }

    public void setBlackAndWitheFilter() {
        selectedFilter = Filters.FILTER_MONO;
        recorder.applyFilter(Filters.FILTER_MONO);
    }

    public void setBlueFilter() {
        selectedFilter = Filters.FILTER_AQUA;
        recorder.applyFilter(Filters.FILTER_AQUA);
    }
}
