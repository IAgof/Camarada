package com.videonasocialmedia.camarada.presentation.mvp.presenters;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import com.videonasocialmedia.avrecorder.AVRecorder;
import com.videonasocialmedia.avrecorder.SessionConfig;
import com.videonasocialmedia.avrecorder.event.CameraEncoderResetEvent;
import com.videonasocialmedia.avrecorder.event.CameraOpenedEvent;
import com.videonasocialmedia.avrecorder.event.MuxerFinishedEvent;
import com.videonasocialmedia.avrecorder.view.GLCameraEncoderView;
import com.videonasocialmedia.camarada.domain.ExportUseCase;
import com.videonasocialmedia.camarada.domain.OnExportFinishedListener;
import com.videonasocialmedia.camarada.presentation.mvp.views.RecordView;
import com.videonasocialmedia.camarada.utils.Constants;

import java.io.IOException;

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
     * Get media list from project use case
     */
//    private GetMediaListFromProjectUseCase getMediaListFromProjectUseCase;
//    private RemoveVideosUseCase removeVideosUseCase;

    public RecordPresenter(Context context, RecordView recordView,
                           GLCameraEncoderView cameraPreview, SharedPreferences sharedPreferences) {
        Log.d(LOG_TAG, "constructor presenter");
        this.recordView = recordView;
        this.context = context;
        this.cameraPreview = cameraPreview;
        this.sharedPreferences = sharedPreferences;
        exportUseCase = new ExportUseCase(this);
//        removeVideosUseCase = new RemoveVideosUseCase();
//        getMediaListFromProjectUseCase = new GetMediaListFromProjectUseCase();
        initRecorder(context, cameraPreview, sharedPreferences);
    }

    private void initRecorder(Context context, GLCameraEncoderView cameraPreview,
                              SharedPreferences sharedPreferences) {

        config = getConfigFromPreferences(sharedPreferences);
        try {
            recorder = new AVRecorder(config);
            recorder.setPreviewDisplay(cameraPreview);
            firstTimeRecording = true;
        } catch (IOException ioe) {
            Log.e("ERROR", "ERROR", ioe);
        }
    }

    private SessionConfig getConfigFromPreferences(SharedPreferences sharedPreferences) {
        // TODO comprobar la máxima resolución que puede coger y usarla aquí
        String destinationFolderPath = Constants.PATH_APP_TEMP;
        int width = 1280;
        int height = 720;
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
            initRecorder(context, cameraPreview, sharedPreferences);
        }
    }

    public void onResume() {
        EventBus.getDefault().register(this);
        recorder.onHostActivityResumed();
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

    private void startRecord() {
        recorder.startRecording();
        recordView.showStopButton();
        recordView.disableShareButton();
        firstTimeRecording = false;
    }

    public void startExport() {
        // TODO ver cómo coger los vídeos que acaba de grabar
//        List<String> videoList = getMediaListFromProjectUseCase.getMediaListFromProject();
//        if (videoList.size() > 0) {
//            exportUseCase.export(videoList);
//        } else {
//            recordView.hideProgressDialog();
//            recordView.showMessage(R.string.add_videos_to_project);
//        }
    }

    public void removeMasterVideos() {
//        removeVideosUseCase.removeMediaItemsFromProject();
    }

    public void onEventMainThread(CameraEncoderResetEvent e) {
        startRecord();
    }

    public void onEventMainThread(CameraOpenedEvent e) {
        //Calculate orientation, rotate if needed
        //recordView.unlockScreenRotation();
        if (firstTimeRecording) {
//            recordView.unlockScreenRotation();
        }

    }

    public void onEventMainThread(MuxerFinishedEvent e) {
//        String finalPath = moveVideoToMastersFolder();
//        addVideoToProjectUseCase.addVideoToTrack(finalPath);
    }

//    private String moveVideoToMastersFolder() {
//        File originalFile = new File(config.getOutputPath());
//        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
//        String fileName = "VID_" + timeStamp + ".mp4";
//        File destinationFile = new File(Constants.PATH_APP_MASTERS, fileName);
//        originalFile.renameTo(destinationFile);
//        return destinationFile.getAbsolutePath();
//    }

//    public void onEvent(AddMediaItemToTrackSuccessEvent e) {
//        recordView.showRecordButton();
//        recordView.enableShareButton();
//    }

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

    @Override
    public void onExportError(String error) {
        recordView.hideProgressDialog();
        //TODO modify error message
        recordView.showError("Export error");
    }

    @Override
    public void onExportSuccess(String path) {
        recordView.hideProgressDialog();
        recordView.goToShare(path);
    }
}
