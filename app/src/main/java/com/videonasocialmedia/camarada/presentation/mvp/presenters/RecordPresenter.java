package com.videonasocialmedia.camarada.presentation.mvp.presenters;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.drawable.Drawable;
import android.support.annotation.NonNull;
import android.util.Log;

import com.mixpanel.android.mpmetrics.MixpanelAPI;
import com.videonasocialmedia.avrecorder.AVRecorder;
import com.videonasocialmedia.avrecorder.Filters;
import com.videonasocialmedia.avrecorder.SessionConfig;
import com.videonasocialmedia.avrecorder.event.CameraEncoderResetEvent;
import com.videonasocialmedia.avrecorder.event.MuxerFinishedEvent;
import com.videonasocialmedia.avrecorder.view.GLCameraEncoderView;
import com.videonasocialmedia.camarada.BuildConfig;
import com.videonasocialmedia.camarada.R;
import com.videonasocialmedia.camarada.domain.ExportUseCase;
import com.videonasocialmedia.camarada.domain.GetVideosFromTempFolderUseCase;
import com.videonasocialmedia.camarada.domain.OnExportFinishedListener;
import com.videonasocialmedia.camarada.domain.RemoveFilesInTempFolderUseCase;
import com.videonasocialmedia.camarada.presentation.mvp.views.RecordView;
import com.videonasocialmedia.camarada.utils.ConfigPreferences;
import com.videonasocialmedia.camarada.utils.Constants;
import com.videonasocialmedia.camarada.utils.Utils;

import org.json.JSONException;
import org.json.JSONObject;

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
    private int width;
    private int height;
    private MixpanelAPI mixpanel;
    private String fileName;
    /**
     * Export project use case
     */
    private ExportUseCase exportUseCase;
    /**
     * Get media list use case
     */
    private GetVideosFromTempFolderUseCase getVideosFromTempFolderUseCase;
    private RemoveFilesInTempFolderUseCase removeFilesInTempFolderUseCase;

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
        mixpanel = MixpanelAPI.getInstance(context, BuildConfig.MIXPANEL_TOKEN);
    }

    private void initRecorder(GLCameraEncoderView cameraPreview,
                              SharedPreferences sharedPreferences) {

        config = getConfigFromPreferences(sharedPreferences);
        try {
            recorder = new AVRecorder(config);
            recorder.setPreviewDisplay(cameraPreview);
            recorder.applyFilter(Filters.FILTER_MONO);
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
        animatedOverlayFrames.add(context.getResources().getDrawable(R.mipmap.noise_1));
        animatedOverlayFrames.add(context.getResources().getDrawable(R.mipmap.noise_2));
        animatedOverlayFrames.add(context.getResources().getDrawable(R.mipmap.noise_3));
        animatedOverlayFrames.add(context.getResources().getDrawable(R.mipmap.noise_4));
        animatedOverlayFrames.add(context.getResources().getDrawable(R.mipmap.noise_5));
        return animatedOverlayFrames;
    }

    private SessionConfig getConfigFromPreferences(SharedPreferences sharedPreferences) {
        // TODO comprobar la máxima resolución que puede coger y usarla aquí
        String destinationFolderPath = Constants.PATH_APP_TEMP;
        width = 1280;
        height = 720;
        int videoBitrate = 5000000;
        int audioChannels = 1;
        int audioFrequency = 48000;
        int audioBitrate = 192 * 1000;

        return new SessionConfig(destinationFolderPath, width, height, videoBitrate,
                audioChannels, audioFrequency, audioBitrate);
    }

    public String getResolution() {
        String resolution = width+"x"+height;
        sharedPreferences.edit().putString(ConfigPreferences.RESOLUTION, resolution);
        return resolution;
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
        setBlackAndWitheFilter();
    }

    public void onPause() {
        EventBus.getDefault().unregister(this);
        stopRecord();
        recorder.onHostActivityPaused();
    }

    public void stopRecord() {
        if (recorder.isRecording()) {
            sendUserInteractedTracking("record", "stop");
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
                mixpanel.timeEvent("Video Recorded");
                startRecord();
            }
        }
    }

    private void resetRecorder() throws IOException {
        config = getConfigFromPreferences(sharedPreferences);
        recorder.reset(config);
    }

    private void startRecord() {
        sendUserInteractedTracking("record", "start");
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

    public void onEventMainThread(CameraEncoderResetEvent e) {
        startRecord();
    }

    public void onEventMainThread(MuxerFinishedEvent e) {
        renameOutputVideo(config.getOutputPath());
    }

    private void renameOutputVideo(String path) {
        File originalFile = new File(path);
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        fileName = "VID_" + timeStamp + ".mp4";
        File destinationFile = new File(Constants.PATH_APP_TEMP, fileName);
        originalFile.renameTo(destinationFile);
        sendVideoRecordedTracking();
    }

    private void sendVideoRecordedTracking() {
        JSONObject videoRecordedProperties = new JSONObject();
        try {
            videoRecordedProperties.put("videoLength", getClipDuration());
            videoRecordedProperties.put("resolution", getResolution());
            // TODO videos last month and total
            mixpanel.track("Video Recorded", videoRecordedProperties);
        } catch (JSONException | IOException e) {
            e.printStackTrace();
        }
    }

    public void changeCamera() {
        //TODO controlar el estado del flash
        int camera = recorder.requestOtherCamera();
        if (camera == 0) {
            sendUserInteractedTracking("change camera", "back");
            recordView.showBackCameraSelected();
        } else {
            if (camera == 1) {
                sendUserInteractedTracking("change camera", "front");
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
        sendUserInteractedTracking("change flash", String.valueOf(on));
        recordView.showFlashOn(on);
    }

    public void changeToWoodSkin() {
        sendUserInteractedTracking("change skin", "wood");
        recordView.changeSkin(R.mipmap.activity_record_background_wood);
        recordView.showSkinLeatherButton();
    }

    public void changeToLeatherSkin() {
        sendUserInteractedTracking("change skin", "leather");
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
        sendFilterSelectedTracking("sepia", "ad8");
        recorder.applyFilter(Filters.FILTER_SEPIA);
    }

    public void setBlackAndWitheFilter() {
        sendFilterSelectedTracking("mono", "ad4");
        recorder.applyFilter(Filters.FILTER_MONO);
    }

    public void setBlueFilter() {
        sendFilterSelectedTracking("aqua", "ad1");
        recorder.applyFilter(Filters.FILTER_AQUA);
    }

    /**
     * Sends button clicks to Mixpanel Analytics
     *
     * @param interaction
     * @param result
     */
    private void sendUserInteractedTracking(String interaction, String result) {
        JSONObject userInteractionsProperties = new JSONObject();
        try {
            userInteractionsProperties.put("acitivity", context.getClass().getSimpleName());
            userInteractionsProperties.put("recording", recorder.isRecording());
            userInteractionsProperties.put("interaction", interaction);
            userInteractionsProperties.put("result", result);
            mixpanel.track("User Interacted", userInteractionsProperties);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private void sendFilterSelectedTracking(String name, String code) {
        JSONObject userInteractionsProperties = new JSONObject();
        try {
            userInteractionsProperties.put("type", "color");
            userInteractionsProperties.put("name", name);
            userInteractionsProperties.put("code", code);
            userInteractionsProperties.put("recording", recorder.isRecording());
            mixpanel.track("Filter Selected", userInteractionsProperties);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private double getClipDuration() throws IOException{
        return Utils.getFileDuration(Constants.PATH_APP_TEMP + File.separator + fileName);
    }

    public double getVideoLength() {
        List<String> videoList = getVideosFromTempFolderUseCase.getVideosFromTempFolder();
        double duration = 0.0;
        if (videoList.size() > 0)
            duration = Utils.getFileDuration(videoList);
        sharedPreferences.edit().putLong(ConfigPreferences.VIDEO_DURATION, (long) duration);
        return duration;
    }

    public int getNumberOfClipsRecorded() {
        int numberOfClipsRecorded = getVideosFromTempFolderUseCase.getVideosFromTempFolder().size();
        sharedPreferences.edit().putInt(ConfigPreferences.NUMBER_OF_CLIPS, numberOfClipsRecorded);
        return numberOfClipsRecorded;
    }

}
