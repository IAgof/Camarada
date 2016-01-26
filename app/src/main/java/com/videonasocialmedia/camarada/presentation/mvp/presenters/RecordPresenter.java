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
import com.videonasocialmedia.camarada.domain.effects.GetEffectListUseCase;
import com.videonasocialmedia.camarada.model.entities.editor.effects.Effect;
import com.videonasocialmedia.camarada.model.entities.editor.effects.ShaderEffect;
import com.videonasocialmedia.camarada.presentation.mvp.views.EffectSelectorView;
import com.videonasocialmedia.camarada.presentation.mvp.views.RecordView;
import com.videonasocialmedia.camarada.utils.AnalyticsConstants;
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
    private EffectSelectorView effectSelectorView;
    private SessionConfig config;
    private AVRecorder recorder;
    private SharedPreferences sharedPreferences;
    private SharedPreferences.Editor editor;
    private Context context;
    private GLCameraEncoderView cameraPreview;
    private int width;
    private int height;
    private int videoBitrate;
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
    private int selectedFilter;
    private Effect selectedShaderEffect;

    public RecordPresenter(Context context, RecordView recordView, EffectSelectorView effectSelectorView,
                           GLCameraEncoderView cameraPreview, SharedPreferences sharedPreferences) {
        Log.d(LOG_TAG, "constructor presenter");
        this.recordView = recordView;
        this.effectSelectorView = effectSelectorView;
        this.context = context;
        this.cameraPreview = cameraPreview;
        this.sharedPreferences = sharedPreferences;
        editor = sharedPreferences.edit();
        exportUseCase = new ExportUseCase(this);
        getVideosFromTempFolderUseCase = new GetVideosFromTempFolderUseCase();
        removeFilesInTempFolderUseCase = new RemoveFilesInTempFolderUseCase();
        mixpanel = MixpanelAPI.getInstance(context, BuildConfig.MIXPANEL_TOKEN);
        initRecorder(cameraPreview, sharedPreferences);
    }

    private void initRecorder(GLCameraEncoderView cameraPreview,
                              SharedPreferences sharedPreferences) {

        config = getConfigFromPreferences(sharedPreferences);
        try {
            recorder = new AVRecorder(config);
            recorder.setPreviewDisplay(cameraPreview);
            setSepiaFilter();
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

    public String getResolution() {
        String resolution = width+"x"+height;
        editor.putString(ConfigPreferences.RESOLUTION, resolution);
        editor.putInt(ConfigPreferences.QUALITY, videoBitrate);
        editor.commit();
        JSONObject userProfileProperties = new JSONObject();
        try {
            userProfileProperties.put(AnalyticsConstants.RESOLUTION, sharedPreferences.getString(
                    ConfigPreferences.RESOLUTION, resolution));
            userProfileProperties.put(AnalyticsConstants.QUALITY,
                    sharedPreferences.getInt(ConfigPreferences.QUALITY, videoBitrate));
            mixpanel.getPeople().set(userProfileProperties);
        } catch (JSONException e) {
            e.printStackTrace();
        }
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
        recorder.applyFilter(selectedFilter);
    }

    public void onPause() {
        EventBus.getDefault().unregister(this);
        stopRecord();
        recorder.onHostActivityPaused();
    }

    public void stopRecord() {
        if (recorder.isRecording()) {
            sendUserInteractedTracking(AnalyticsConstants.RECORD, AnalyticsConstants.STOP);
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
                mixpanel.timeEvent(AnalyticsConstants.VIDEO_RECORDED);
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
        sendUserInteractedTracking(AnalyticsConstants.RECORD, AnalyticsConstants.START);
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
        fileName = "VID_" + timeStamp + ".mp4";
        File destinationFile = new File(Constants.PATH_APP_TEMP, fileName);
        originalFile.renameTo(destinationFile);
        int numTotalVideosRecorded = sharedPreferences
                .getInt(ConfigPreferences.TOTAL_VIDEOS_RECORDED, 0);
        editor.putInt(ConfigPreferences.TOTAL_VIDEOS_RECORDED,
                ++numTotalVideosRecorded);
        editor.commit();
        sendVideoRecordedTracking();
    }

    private void sendVideoRecordedTracking() {
        JSONObject videoRecordedProperties = new JSONObject();
        int totalVideosRecorded = sharedPreferences.getInt(ConfigPreferences.TOTAL_VIDEOS_RECORDED, 0);
        try {
            videoRecordedProperties.put(AnalyticsConstants.VIDEO_LENGTH, getClipDuration());
            videoRecordedProperties.put(AnalyticsConstants.RESOLUTION, getResolution());
            videoRecordedProperties.put(AnalyticsConstants.TOTAL_RECORDED_VIDEOS,
                    totalVideosRecorded);
            mixpanel.track(AnalyticsConstants.VIDEO_RECORDED, videoRecordedProperties);
        } catch (JSONException | IOException e) {
            e.printStackTrace();
        }
        mixpanel.getPeople().increment(AnalyticsConstants.TOTAL_RECORDED_VIDEOS, 1);
        mixpanel.getPeople().set(AnalyticsConstants.LAST_VIDEO_RECORDED,
                new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZ").format(new Date()));
    }

    public void changeCamera() {
        //TODO controlar el estado del flash
        int camera = recorder.requestOtherCamera();
        if (camera == 0) {
            sendUserInteractedTracking(AnalyticsConstants.CHANGE_CAMERA, "back");
            recordView.showBackCameraSelected();
        } else {
            if (camera == 1) {
                sendUserInteractedTracking(AnalyticsConstants.CHANGE_CAMERA, "front");
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
        sendUserInteractedTracking(AnalyticsConstants.CHANGE_FLASH, String.valueOf(on));
        recordView.showFlashOn(on);
    }

    public void changeToWoodSkin() {
        sendUserInteractedTracking(AnalyticsConstants.CHANGE_SKIN, AnalyticsConstants.SKIN_WOOD);
        recordView.changeSkin(R.mipmap.activity_record_background_wood);
        recordView.showSkinLeatherButton();
    }

    public void changeToLeatherSkin() {
        sendUserInteractedTracking(AnalyticsConstants.CHANGE_SKIN, AnalyticsConstants.SKIN_LEATHER);
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

    public void applyEffect(Effect effect){

                int shaderId = ((ShaderEffect) effect).getResourceId();
                recorder.applyFilter(shaderId);
                selectedShaderEffect = effect;

    }

    public void removeEffect(Effect effect) {

        recorder.applyFilter(Filters.FILTER_NONE);
        selectedShaderEffect = null;
    }

    public void setSepiaFilter() {
        sendFilterSelectedTracking(AnalyticsConstants.FILTER_NAME_SEPIA,
                AnalyticsConstants.FILTER_CODE_SEPIA);
        selectedFilter = Filters.FILTER_SEPIA;
        recorder.applyFilter(Filters.FILTER_SEPIA);
        effectSelectorView.showSepiaSelected();
    }

    public void setBlackAndWitheFilter() {
        sendFilterSelectedTracking(AnalyticsConstants.FILTER_NAME_MONO,
                AnalyticsConstants.FILTER_CODE_MONO);
        selectedFilter = Filters.FILTER_MONO;
        recorder.applyFilter(Filters.FILTER_MONO);
        effectSelectorView.showBlackAndWhiteSelected();
    }

    public void setBlueFilter() {
        sendFilterSelectedTracking(AnalyticsConstants.FILTER_NAME_AQUA,
                AnalyticsConstants.FILTER_CODE_AQUA);
        selectedFilter = Filters.FILTER_AQUA;
        recorder.applyFilter(Filters.FILTER_AQUA);
        effectSelectorView.showBlueSelected();
    }

    public List<Effect> getShaderEffectList() {
        return GetEffectListUseCase.getShaderEffectsList();
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
            userInteractionsProperties.put(AnalyticsConstants.ACTIVITY, context.getClass().getSimpleName());
            userInteractionsProperties.put(AnalyticsConstants.RECORDING, recorder.isRecording());
            userInteractionsProperties.put(AnalyticsConstants.INTERACTION, interaction);
            userInteractionsProperties.put(AnalyticsConstants.RESULT, result);
            mixpanel.track(AnalyticsConstants.USER_INTERACTED, userInteractionsProperties);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private void sendFilterSelectedTracking(String name, String code) {
        JSONObject userInteractionsProperties = new JSONObject();
        try {
            userInteractionsProperties.put(AnalyticsConstants.TYPE, AnalyticsConstants.TYPE_COLOR);
            userInteractionsProperties.put(AnalyticsConstants.NAME, name);
            userInteractionsProperties.put(AnalyticsConstants.CODE, code);
            userInteractionsProperties.put(AnalyticsConstants.RECORDING, recorder.isRecording());
            mixpanel.track(AnalyticsConstants.FILTER_SELECTED, userInteractionsProperties);
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
        editor.putLong(ConfigPreferences.VIDEO_DURATION, (long) duration);
        editor.commit();
        return duration;
    }

    public int getNumberOfClipsRecorded() {
        int numberOfClipsRecorded = getVideosFromTempFolderUseCase.getVideosFromTempFolder().size();
        editor.putInt(ConfigPreferences.NUMBER_OF_CLIPS, numberOfClipsRecorded);
        editor.commit();
        return numberOfClipsRecorded;
    }

}
