package com.videonasocialmedia.kamarada.presentation.views.activity;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.hardware.Camera;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.provider.Settings;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.Window;
import android.view.WindowManager;
import android.widget.TextView;

import com.mixpanel.android.mpmetrics.InAppNotification;
import com.videonasocialmedia.kamarada.BuildConfig;
import com.videonasocialmedia.kamarada.R;
import com.videonasocialmedia.kamarada.domain.RemoveFilesInTempFolderUseCase;
import com.videonasocialmedia.kamarada.presentation.listener.OnInitAppEventListener;
import com.videonasocialmedia.kamarada.presentation.mvp.views.InitAppView;
import com.videonasocialmedia.kamarada.utils.AnalyticsConstants;
import com.videonasocialmedia.kamarada.utils.AppStart;
import com.videonasocialmedia.kamarada.utils.ConfigPreferences;
import com.videonasocialmedia.kamarada.utils.Constants;
import com.videonasocialmedia.kamarada.utils.Utils;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import butterknife.Bind;
import butterknife.ButterKnife;

/**
 * InitAppActivity.
 * <p/>
 * According to clean code and model, use InitAppView, InitAppPresenter for future use.
 * <p/>
 * Main Activity of the app, launch from manifest.
 * <p/>
 * First activity when the user open the app.
 * <p/>
 * Show a dummy splash screen and initialize all data needed to start
 */

public class InitAppActivity extends KamaradaActivity implements InitAppView, OnInitAppEventListener {


    /**
     * LOG_TAG
     */
    private final String LOG_TAG = this.getClass().getSimpleName();
    protected Handler handler = new Handler();
    @Bind(R.id.videona_version)
    TextView versionName;
    private long MINIMUN_WAIT_TIME;
    private SharedPreferences sharedPreferences;
    private SharedPreferences.Editor preferencesEditor;
    private Camera camera;
    private int numSupportedCameras;
    private long startTime;
    private String androidId = null;
    private RemoveFilesInTempFolderUseCase removeFilesInTempFolderUseCase;
    private String initState;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        super.onCreate(savedInstanceState);

        //remove title, mode fullscreen
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);

        setContentView(R.layout.activity_init_app);
        ButterKnife.bind(this);
        removeFilesInTempFolderUseCase = new RemoveFilesInTempFolderUseCase();
        setVersionCode();
        if (BuildConfig.DEBUG) {
            MINIMUN_WAIT_TIME = 2000;
        } else {
            MINIMUN_WAIT_TIME = 2000;
        }
    }

    private void setVersionCode() {
        String version = "v " + BuildConfig.VERSION_NAME;
        versionName.setText(version);
    }

    @Override
    protected void onStart() {
        super.onStart();
        startTime = System.currentTimeMillis();
        checkAndRequestPermissions();
        sharedPreferences = getSharedPreferences(ConfigPreferences.SETTINGS_SHARED_PREFERENCES_FILE_NAME,
                Context.MODE_PRIVATE);
        preferencesEditor = sharedPreferences.edit();
        PreferenceManager.setDefaultValues(this, R.xml.preferences, false);
        SplashScreenTask splashScreenTask = new SplashScreenTask(this);
        splashScreenTask.execute();
    }

    @Override
    protected void onResume() {
        super.onResume();

    }

    @Override
    protected void onPause() {
        super.onPause();
        releaseCamera();
    }

    @Override
    protected void onStop() {
        super.onStop();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        handler.removeCallbacksAndMessages(null);
    }

    /**
     * Releases the camera object
     */
    private void releaseCamera() {
        if (camera != null) {

            camera.release();
            camera = null;
        }
    }

    private void sendStartupAppTracking() {
        JSONObject initAppProperties = new JSONObject();
        try {
            initAppProperties.put(AnalyticsConstants.TYPE, AnalyticsConstants.TYPE_ORGANIC);
            initAppProperties.put(AnalyticsConstants.INIT_STATE, initState);
            mixpanel.track(AnalyticsConstants.APP_STARTED, initAppProperties);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private void setup() {
        androidId = Settings.Secure.getString(getContentResolver(), Settings.Secure.ANDROID_ID);
        setupPathsApp(this);
        setupStartApp();
    }

    private void setupStartApp() {
        AppStart appStart = new AppStart();
        switch (appStart.checkAppStart(this, sharedPreferences)) {
            case NORMAL:
                Log.d(LOG_TAG, " AppStart State NORMAL");
                initState = "returning";
                initSettings();
                break;
            case FIRST_TIME_VERSION:
                Log.d(LOG_TAG, " AppStart State FIRST_TIME_VERSION");
                initState = "upgrade";
                // example: show what's new
                // could be appear a mix panel popup with improvements.

                // Repeat this method for security, if user delete app data miss this configs.
                setupCameraSettings();
                createUserProfile();
                initSettings();
                break;
            case FIRST_TIME:
                Log.d(LOG_TAG, " AppStart State FIRST_TIME");
                initState = "firstTime";
                // example: show a tutorial
                setupCameraSettings();
                createUserProfile();
                initSettings();
                break;
            default:
                break;
        }
    }

    private void createUserProfile() {
        mixpanel.identify(androidId);
        mixpanel.getPeople().identify(androidId);
        JSONObject userProfileProperties = new JSONObject();
        try {
            userProfileProperties.put(AnalyticsConstants.TYPE, AnalyticsConstants.TYPE_PAID);
            userProfileProperties.put(AnalyticsConstants.CREATED,
                    new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZ").format(new Date()));
            userProfileProperties.put(AnalyticsConstants.LOCALE,
                    Locale.getDefault().toString());
            userProfileProperties.put(AnalyticsConstants.LANG, Locale.getDefault().getISO3Language());
            mixpanel.getPeople().setOnce(userProfileProperties);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    /**
     * Initializes the camera id parameter in shared preferences to back camera
     */
    private void initSettings() {
        preferencesEditor.putInt(ConfigPreferences.CAMERA_ID, ConfigPreferences.BACK_CAMERA).commit();
    }

    /**
     * Checks the available cameras on the device (back/front), supported flash mode and the
     * supported resolutions
     */
    private void setupCameraSettings() {
        checkAvailableCameras();
        checkFlashMode();
    }

    /**
     * Checks the available cameras on the device (back/front)
     */
    private void checkAvailableCameras() {
        if (camera != null) {
            releaseCamera();
        }
        camera = getCameraInstance(sharedPreferences.getInt(ConfigPreferences.CAMERA_ID,
                ConfigPreferences.BACK_CAMERA));
        preferencesEditor.putBoolean(ConfigPreferences.BACK_CAMERA_SUPPORTED, true).commit();
        numSupportedCameras = Camera.getNumberOfCameras();
        if (numSupportedCameras > 1) {
            preferencesEditor.putBoolean(ConfigPreferences.FRONT_CAMERA_SUPPORTED, true).commit();
        }
        releaseCamera();
    }

    /**
     * Checks if the device supports the flash mode
     */
    private void checkFlashMode() {
        if (camera != null) {
            releaseCamera();
        }
        if (numSupportedCameras > 1) {
            camera = getCameraInstance(ConfigPreferences.FRONT_CAMERA);
            if (camera.getParameters().getSupportedFlashModes() != null) {
                preferencesEditor.putBoolean(ConfigPreferences.FRONT_CAMERA_FLASH_SUPPORTED, true).commit();
            } else {
                preferencesEditor.putBoolean(ConfigPreferences.FRONT_CAMERA_FLASH_SUPPORTED, false).commit();
            }
            releaseCamera();
        }
        camera = getCameraInstance(ConfigPreferences.BACK_CAMERA);
        if (camera.getParameters().getSupportedFlashModes() != null) {
            preferencesEditor.putBoolean(ConfigPreferences.BACK_CAMERA_FLASH_SUPPORTED, true).commit();
        } else {
            preferencesEditor.putBoolean(ConfigPreferences.BACK_CAMERA_FLASH_SUPPORTED, false).commit();
        }
        releaseCamera();
    }


    /**
     * Gets an instance of the camera object
     *
     * @param cameraId
     * @return
     */
    public Camera getCameraInstance(int cameraId) {
        Camera c = null;
        try {
            c = Camera.open(cameraId);
        } catch (Exception e) {
            Log.e("DEBUG", "Camera did not open", e);
        }
        return c;
    }

    /**
     * Checks the paths of the app
     *
     * @param listener
     */
    private void setupPathsApp(OnInitAppEventListener listener) {
        try {
            cleanTempPath();
            initPaths();
            listener.onCheckPathsAppSuccess();
        } catch (IOException e) {
            Log.e("CHECK PATH", "error", e);
        }
    }

    /**
     * Check Camarada app paths, PATH_APP, pathVideoTrim, pathVideoMusic, ...
     *
     * @throws IOException
     */
    private void initPaths() throws IOException {
        checkRootPathMovies();
        checkAndInitPath(Constants.PATH_APP);
        checkAndInitPath(Constants.PATH_APP_TEMP);
        checkAndInitPath(Constants.VIDEO_MUSIC_FOLDER);
        copyMusicFromResources();
        File privateDataFolderModel = getDir(Constants.FOLDER_VIDEONA_PRIVATE_MODEL, Context.MODE_PRIVATE);
        String privatePath = privateDataFolderModel.getAbsolutePath();
        preferencesEditor.putString(ConfigPreferences.PRIVATE_PATH, privatePath).commit();
    }

    private void cleanTempPath() {
        removeFilesInTempFolderUseCase.removeFilesInTempFolder();
    }

    private void copyMusicFromResources() {
        try {
            Utils.copyMusicResourceToTemp(this, R.raw.audio);
        } catch (IOException e) {
            Log.e("Error", "IOException", e);
        }
    }

    private void checkRootPathMovies() {
        File fMovies = Environment.getExternalStoragePublicDirectory(
                Environment.DIRECTORY_MOVIES);
        if (!fMovies.exists()) {
            fMovies.mkdir();
        }
    }

    private void checkAndInitPath(String pathApp) {
        File fEdited = new File(pathApp);
        if (!fEdited.exists()) {
            fEdited.mkdir();
        }
    }


    @Override
    public void onCheckPathsAppSuccess() {

    }

    @Override
    public void onCheckPathsAppError() {

    }


    @Override
    public void navigate(Class cls) {
        startActivity(new Intent(getApplicationContext(), cls));
    }

    /**
     * Shows the splash screen
     */
    class SplashScreenTask extends AsyncTask<Void, Void, Boolean> {

        private Activity parentActivity;

        public SplashScreenTask(Activity activity) {
            this.parentActivity = activity;
        }

        @Override
        protected Boolean doInBackground(Void... voids) {
            try {
                waitForCriticalPermissions();
                setup();
                sendStartupAppTracking();
            } catch (Exception e) {
                Log.e("SETUP", "setup failed", e);
            }
            return true;
        }

        @Override
        protected void onPostExecute(Boolean loggedIn) {
            long currentTimeEnd = System.currentTimeMillis();
            long timePassed = currentTimeEnd - startTime;
            if (timePassed < MINIMUN_WAIT_TIME) {
                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        exitSplashScreen();
                    }
                }, MINIMUN_WAIT_TIME - timePassed);
            } else {
                exitSplashScreen();
            }
        }

        private void exitSplashScreen() {
            if (sharedPreferences.getBoolean(ConfigPreferences.FIRST_TIME, true)) {
                navigate(IntroAppActivity.class);
            } else {
                InAppNotification notification = mixpanel.getPeople().getNotificationIfAvailable();
                if (notification != null) {
                    Log.d("INAPP", "in-app notification received");
                    mixpanel.getPeople().showGivenNotification(notification, parentActivity);
                } else {
                    navigate(RecordActivity.class);
                }
            }
        }

        private void waitForCriticalPermissions() {
            while (!areCriticalPermissionsGranted()) {
                //just wait
                //TODO reimplement using handlers and semaphores
            }
        }

        private boolean areCriticalPermissionsGranted() {
            boolean granted = ContextCompat.checkSelfPermission(InitAppActivity.this,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED &&
                    ContextCompat.checkSelfPermission(InitAppActivity.this,
                            Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED &&
                    ContextCompat.checkSelfPermission(InitAppActivity.this,
                            Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED;
            return granted;
        }
    }
}
