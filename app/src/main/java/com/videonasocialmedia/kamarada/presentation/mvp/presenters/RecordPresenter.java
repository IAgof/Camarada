package com.videonasocialmedia.kamarada.presentation.mvp.presenters;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Configuration;
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
import com.videonasocialmedia.kamarada.BuildConfig;
import com.videonasocialmedia.kamarada.R;
import com.videonasocialmedia.kamarada.domain.AddVideoToProjectUseCase;
import com.videonasocialmedia.kamarada.domain.ExportUseCase;
import com.videonasocialmedia.kamarada.domain.GetVideosFromTempProjectUseCase;
import com.videonasocialmedia.kamarada.domain.OnExportFinishedListener;
import com.videonasocialmedia.kamarada.domain.RemoveFilesUsedFromTempProjectUseCase;
import com.videonasocialmedia.kamarada.domain.effects.GetEffectListUseCase;
import com.videonasocialmedia.kamarada.model.entities.ShaderEffect;
import com.videonasocialmedia.kamarada.presentation.mvp.views.EffectSelectorView;
import com.videonasocialmedia.kamarada.presentation.mvp.views.RecordView;
import com.videonasocialmedia.kamarada.utils.AnalyticsConstants;
import com.videonasocialmedia.kamarada.utils.ConfigPreferences;
import com.videonasocialmedia.kamarada.utils.Constants;
import com.videonasocialmedia.kamarada.utils.Utils;

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
    private SharedPreferences.Editor preferencesEditor;
    private Context context;
    private GLCameraEncoderView cameraPreview;
    private MixpanelAPI mixpanel;
    private String fileName;
    /**
     * Export project use case
     */
    private ExportUseCase exportUseCase;
    /**
     * Get media list use case
     */
    private GetVideosFromTempProjectUseCase getVideosFromTempProjectUseCase;
    private RemoveFilesUsedFromTempProjectUseCase removeFilesUsedFromTempProjectUseCase;
    private AddVideoToProjectUseCase addVideoToProjectUseCase;
    private List<ShaderEffect> effects;
    private int selectedFilterIndex = 0;
    private String resolution;
    private int videoBitrate;
    private int numRecordedVideos = 0;

    public RecordPresenter(Context context, RecordView recordView, EffectSelectorView effectSelectorView,
                           GLCameraEncoderView cameraPreview, SharedPreferences sharedPreferences) {
        Log.d(LOG_TAG, "constructor presenter");
        this.recordView = recordView;
        this.effectSelectorView = effectSelectorView;
        this.context = context;
        this.cameraPreview = cameraPreview;
        this.sharedPreferences = sharedPreferences;
        preferencesEditor = sharedPreferences.edit();
        exportUseCase = new ExportUseCase(this);
        effects = getShaderEffectList();
        getVideosFromTempProjectUseCase = new GetVideosFromTempProjectUseCase();
        removeFilesUsedFromTempProjectUseCase = new RemoveFilesUsedFromTempProjectUseCase();
        addVideoToProjectUseCase = new AddVideoToProjectUseCase();
        mixpanel = MixpanelAPI.getInstance(context, BuildConfig.MIXPANEL_TOKEN);
        initRecorder(cameraPreview, sharedPreferences);
    }

    private List<ShaderEffect> getShaderEffectList() {
        return GetEffectListUseCase.getShaderEffectsList();
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

    private SessionConfig getConfigFromPreferences(SharedPreferences sharedPreferences) {
        // TODO comprobar la máxima resolución que puede coger y usarla aquí
        String destinationFolderPath = Constants.PATH_APP_TEMP;
        int width = 640;
        int height = 480;
        videoBitrate = 5000000;
        int audioChannels = 1;
        int audioFrequency = 48000;
        int audioBitrate = 192 * 1000;
        resolution = width + "x" + height;

        SessionConfig result = new SessionConfig(destinationFolderPath, width, height, videoBitrate,
                audioChannels, audioFrequency, audioBitrate);
        saveResolutionToDisk();
        return result;
    }

    private void saveResolutionToDisk() {
        preferencesEditor.putString(ConfigPreferences.RESOLUTION, getResolution());
        preferencesEditor.putInt(ConfigPreferences.QUALITY, videoBitrate);
        preferencesEditor.commit();
    }

    private String getResolution() {
        return resolution;
    }

    public void setSepiaFilter() {
        setSelectedFilter(Filters.FILTER_SEPIA);
    }

    private void setSelectedFilter(int filterId) {
        recorder.applyFilter(filterId);
        updateSelectedIndex(filterId);
        showSelectedEffect(filterId);
    }

    private void updateSelectedIndex(int filterID) {
        ShaderEffect effect = getEffectByFilterId(filterID);
        selectedFilterIndex = effects.indexOf(effect);
    }

    private void showSelectedEffect(int filterId) {
        switch (filterId) {
            case Filters.FILTER_SEPIA:
                effectSelectorView.showSepiaSelected();
                break;
            case Filters.FILTER_MONO:
                effectSelectorView.showBlackAndWhiteSelected();
                break;
            case Filters.FILTER_AQUA:
                effectSelectorView.showBlueSelected();
                break;
        }
        effectSelectorView.showFilterSelectedText(effects.get(selectedFilterIndex).getName());
    }

    private ShaderEffect getEffectByFilterId(int filterID) {
        for (int index = 0; index < effects.size(); index++) {
            ShaderEffect current = effects.get(index);
            if (current.getResourceId() == filterID) {
                return current;
            }
        }
        return null;
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

    public void onStart() {
        if (recorder.isReleased()) {
            cameraPreview.releaseCamera();
            initRecorder(cameraPreview, sharedPreferences);
        }
    }

    public void onResume() {
        EventBus.getDefault().register(this);
        recorder.onHostActivityResumed();
        recorder.applyFilter(getSelectedFilterId());
    }

    private int getSelectedFilterId() {
        return effects.get(selectedFilterIndex).getResourceId();
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
    }

    /**
     * Sends user interactions to Mixpanel Analytics
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

    public void onStop() {
        recorder.release();
    }

    public void onDestroy() {
        //recorder.release();
    }

    public int getFilterSelectedId() {
        return selectedFilterIndex;
    }

    public void onEventMainThread(MuxerFinishedEvent e) {
        fileName = renameOutputVideo(config.getOutputPath());
        addVideoToProjectUseCase.addVideoToProfile(Constants.PATH_APP + File.separator + fileName);
        recordView.stopProgressBar();
        recordView.showRecordedVideoThumbIndicator(getLastClipPath(), ++numRecordedVideos);
        updateTotalVideosRecorded();
        trackTotalVideosRecordedSuperProperty();
        sendVideoRecordedTracking();
    }

    private String renameOutputVideo(String originalPath) {
        File originalFile = new File(originalPath);
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String outputPath = "VID_" + timeStamp + ".mp4";
        File destinationFile = new File(Constants.PATH_APP, outputPath);
        originalFile.renameTo(destinationFile);
        return outputPath;
    }

    private String getLastClipPath() {
        return Constants.PATH_APP + File.separator + fileName;
    }

    private void updateTotalVideosRecorded() {
        int numTotalVideosRecorded = sharedPreferences
                .getInt(ConfigPreferences.TOTAL_VIDEOS_RECORDED, 0);
        preferencesEditor.putInt(ConfigPreferences.TOTAL_VIDEOS_RECORDED,
                ++numTotalVideosRecorded);
        preferencesEditor.commit();
    }

    private void trackTotalVideosRecordedSuperProperty() {
        JSONObject totalVideoRecordedSuperProperty = new JSONObject();
        int numPreviousVideosRecorded;
        try {
            numPreviousVideosRecorded =
                    mixpanel.getSuperProperties().getInt(AnalyticsConstants.TOTAL_VIDEOS_RECORDED);
        } catch (JSONException e) {
            numPreviousVideosRecorded = 0;
        }
        try {
            totalVideoRecordedSuperProperty.put(AnalyticsConstants.TOTAL_VIDEOS_RECORDED,
                    ++numPreviousVideosRecorded);
            mixpanel.registerSuperProperties(totalVideoRecordedSuperProperty);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private void sendVideoRecordedTracking() {
        JSONObject videoRecordedProperties = new JSONObject();
        int totalVideosRecorded = sharedPreferences.getInt(ConfigPreferences.TOTAL_VIDEOS_RECORDED, 0);
        try {
            videoRecordedProperties.put(AnalyticsConstants.VIDEO_LENGTH, getClipDuration());
            videoRecordedProperties.put(AnalyticsConstants.RESOLUTION, getResolution());
            videoRecordedProperties.put(AnalyticsConstants.TOTAL_VIDEOS_RECORDED,
                    totalVideosRecorded);
            videoRecordedProperties.put(AnalyticsConstants.DOUBLE_HOUR_AND_MINUTES,
                    Utils.getDoubleHourAndMinutes());
            mixpanel.track(AnalyticsConstants.VIDEO_RECORDED, videoRecordedProperties);
        } catch (JSONException | IOException e) {
            e.printStackTrace();
        }
        updateUserProfileProperties();
    }

    private double getClipDuration() throws IOException {
        return Utils.getFileDuration(getLastClipPath());
    }

    private void updateUserProfileProperties() {
        JSONObject userProfileProperties = new JSONObject();
        try {
            userProfileProperties.put(AnalyticsConstants.RESOLUTION, sharedPreferences.getString(
                    ConfigPreferences.RESOLUTION, getResolution()));
            userProfileProperties.put(AnalyticsConstants.QUALITY,
                    sharedPreferences.getInt(ConfigPreferences.QUALITY, config.getVideoBitrate()));
            mixpanel.getPeople().set(userProfileProperties);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        mixpanel.getPeople().increment(AnalyticsConstants.TOTAL_VIDEOS_RECORDED, 1);
        mixpanel.getPeople().set(AnalyticsConstants.LAST_VIDEO_RECORDED,
                new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZ").format(new Date()));
    }

    public void startExport() {
        recordView.showProgressDialog();
        mixpanel.timeEvent(AnalyticsConstants.VIDEO_EXPORTED);
        final Thread t = new Thread() {
            @Override
            public void run() {
                doStartExport();
            }
        };
        t.start();
    }

    private void doStartExport() {
        List<String> videoList = getVideosFromTempProjectUseCase.getVideosFromTempProject();
        if (videoList.size() > 0) {
            exportUseCase.export(videoList);
        } else {
            recordView.hideProgressDialog();
        }
    }

    @Override
    public void onExportError(String error) {
        mixpanel.track(AnalyticsConstants.VIDEO_EXPORTED);
        recordView.hideProgressDialog();
        recordView.showError(R.string.errorExportingVideo);
    }

    @Override
    public void onExportSuccess(String path) {
        removeTempVideos();
        recordView.hideProgressDialog();
        sendExportedVideoMetadataTracking();
        recordView.goToShare(path);
    }

    private void removeTempVideos() {
        removeFilesUsedFromTempProjectUseCase.removeFilesUsedInExport();
        removeFilesUsedFromTempProjectUseCase.clearVideosFromProject();
    }

    private void sendExportedVideoMetadataTracking() {
        JSONObject videoExportedProperties = new JSONObject();
        try {
            videoExportedProperties.put(AnalyticsConstants.VIDEO_LENGTH,
                    this.getVideoLength());
            videoExportedProperties.put(AnalyticsConstants.RESOLUTION,
                    this.getResolution());
            videoExportedProperties.put(AnalyticsConstants.NUMBER_OF_CLIPS,
                    this.getNumberOfRecordedClips());
            videoExportedProperties.put(AnalyticsConstants.DOUBLE_HOUR_AND_MINUTES,
                    Utils.getDoubleHourAndMinutes());
            mixpanel.track(AnalyticsConstants.VIDEO_EXPORTED, videoExportedProperties);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private double getVideoLength() {
        List<String> videoList = getVideosFromTempProjectUseCase.getVideosFromTempProject();
        double duration = 0.0;
        if (videoList.size() > 0)
            duration = Utils.getFileDuration(videoList);
        preferencesEditor.putLong(ConfigPreferences.VIDEO_DURATION, (long) duration);
        preferencesEditor.commit();
        return duration;
    }

    private int getNumberOfRecordedClips() {
        int numberOfClipsRecorded = getVideosFromTempProjectUseCase.getVideosFromTempProject().size();
        preferencesEditor.putInt(ConfigPreferences.NUMBER_OF_CLIPS, numberOfClipsRecorded);
        preferencesEditor.commit();
        return numberOfClipsRecorded;
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

    private void startRecord() {
        mixpanel.timeEvent(AnalyticsConstants.VIDEO_RECORDED);
        sendUserInteractedTracking(AnalyticsConstants.RECORD, AnalyticsConstants.START);
        recorder.startRecording();
        recordView.showStopButton();
        recordView.disableShareButton();
        recordView.startProgressBar();
        firstTimeRecording = false;
    }

    public void onEventMainThread(CameraEncoderResetEvent e) {
        recorder.applyFilter(getSelectedFilterId());
        startRecord();
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

    public void checkSkin(int previousBackgroundId) {
        switch (previousBackgroundId) {
            case R.mipmap.activity_record_background_leather:
                changeToLeatherSkin();
                break;
            case R.mipmap.activity_record_background_leather_tablet:
                changeToLeatherSkin();
                break;
            case R.mipmap.activity_record_background_wood:
                changeToWoodSkin();
                break;
            case R.mipmap.activity_record_background_wood_tablet:
                changeToWoodSkin();
                break;
            default:
                changeToLeatherSkin();
                break;
        }
    }

    public void changeToWoodSkin() {
        Configuration config = context.getResources().getConfiguration();
        if (config.smallestScreenWidthDp >= 600) {
            recordView.changeSkin(R.mipmap.activity_record_background_wood_tablet);
        } else {
            recordView.changeSkin(R.mipmap.activity_record_background_wood);
        }
        recordView.showSkinLeatherButton();
    }

    public void changeToLeatherSkin() {
        Configuration config = context.getResources().getConfiguration();
        if (config.smallestScreenWidthDp >= 600) {
            recordView.changeSkin(R.mipmap.activity_record_background_leather_tablet);
        } else {
            recordView.changeSkin(R.mipmap.activity_record_background_leather);
        }
        recordView.showSkinWoodButton();
    }

    public void setBlackAndWitheFilter() {
        setSelectedFilter(Filters.FILTER_MONO);
    }

    public void setBlueFilter() {
        setSelectedFilter(Filters.FILTER_AQUA);
    }

    public void setNextFilter() {
        selectedFilterIndex = ( selectedFilterIndex + 1 ) % effects.size();
        setSelectedFilter(getSelectedFilterId());
    }

    public void setPrevFilter() {
        selectedFilterIndex = selectedFilterIndex - 1;
        if (selectedFilterIndex < 0)
            selectedFilterIndex = selectedFilterIndex + effects.size();
        setSelectedFilter(getSelectedFilterId());
    }

    public void checkAvailableVideos() {
        int actualNumberOfRecordedClips = getNumberOfRecordedClips();
        if(actualNumberOfRecordedClips > 0) {
            List<String> videoList = getVideosFromTempProjectUseCase.getVideosFromTempProject();
            String latestClipPath = videoList.get(videoList.size()-1);
            numRecordedVideos = actualNumberOfRecordedClips;
            recordView.showRecordedVideoThumbIndicator(latestClipPath, numRecordedVideos);
            recordView.enableShareButton();
        }
    }

}
