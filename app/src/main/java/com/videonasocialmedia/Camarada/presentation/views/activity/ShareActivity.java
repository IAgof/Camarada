package com.videonasocialmedia.Camarada.presentation.views.activity;

import android.content.Context;
import android.annotation.TargetApi;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageButton;
import android.widget.SeekBar;
import android.widget.VideoView;

import com.videonasocialmedia.Camarada.R;
import com.videonasocialmedia.Camarada.model.entities.SocialNetwork;
import com.videonasocialmedia.Camarada.presentation.adapter.SocialNetworkAdapter;
import com.videonasocialmedia.Camarada.presentation.listener.OnSocialNetworkClickedListener;
import com.videonasocialmedia.Camarada.presentation.mvp.presenters.SharePresenter;
import com.videonasocialmedia.Camarada.presentation.mvp.views.PreviewVideoView;
import com.videonasocialmedia.Camarada.presentation.mvp.views.ShareView;
import com.videonasocialmedia.Camarada.utils.AnalyticsConstants;
import com.videonasocialmedia.Camarada.utils.ConfigPreferences;
import com.videonasocialmedia.Camarada.utils.Utils;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.OnTouch;

/**
 * Created by jca on 18/1/16.
 */
public class ShareActivity extends KamaradaActivity implements ShareView, PreviewVideoView,
        OnSocialNetworkClickedListener, SeekBar.OnSeekBarChangeListener {

    public static final String INTENT_EXTRA_VIDEO_PATH = "VIDEO_EDITED";
    public static final String INTENT_EXTRA_BACKGROND = "ACTIVITY_BACKGROUND";

    @Bind(R.id.videoPreview)
    VideoView videoPreview;
    @Bind(R.id.socialNetworkRecycler)
    RecyclerView socialNetworkRecycler;
    @Bind(R.id.seekbar)
    SeekBar seekBar;
    @Bind(R.id.playButton)
    ImageButton playButton;
    @Bind(R.id.background)
    View background;

    private SharePresenter presenter;
    private String videoPath;
    private SocialNetworkAdapter socialNetworkAdapter;
    private boolean draggingSeekBar;
    private Handler updateSeekBarTaskHandler = new Handler();
    private Runnable updateSeekBarTask = new Runnable() {
        @Override
        public void run() {
            updateSeekbar();
        }
    };
    private SharedPreferences sharedPreferences;
    private SharedPreferences.Editor preferencesEditor;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_share);
        ButterKnife.bind(this);

        sharedPreferences =
                getSharedPreferences(ConfigPreferences.SETTINGS_SHARED_PREFERENCES_FILE_NAME,
                        Context.MODE_PRIVATE);
        preferencesEditor = sharedPreferences.edit();
        presenter = new SharePresenter(this, this, sharedPreferences);
        initBackgorund();
        initVideoPreview();
        initSocialNetworkContainer();
    }

    private void initBackgorund() {
        int backgroundId = getIntent().getIntExtra(INTENT_EXTRA_BACKGROND, -1);
        if (backgroundId != -1)
            background.setBackgroundResource(backgroundId);
    }

    private void initVideoPreview() {
        videoPath = getIntent().getStringExtra(INTENT_EXTRA_VIDEO_PATH);
        if (videoPath != null) {
            videoPreview.setVideoPath(videoPath);
        }
        VideoPreviewEventListener videoPreviewEventListener = new VideoPreviewEventListener();
        videoPreview.setOnCompletionListener(videoPreviewEventListener);
        videoPreview.setOnPreparedListener(videoPreviewEventListener);
    }

    private void initSocialNetworkContainer() {
        RecyclerView.LayoutManager layoutManager =
                new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false);
        socialNetworkRecycler.setLayoutManager(layoutManager);
        socialNetworkAdapter = new SocialNetworkAdapter(this);
        socialNetworkRecycler.setAdapter(socialNetworkAdapter);
    }

    private void initSeekBar(int progress, int max) {
        seekBar.setMax(max);
        seekBar.setProgress(progress);
        seekBar.setOnSeekBarChangeListener(this);
        updateSeekbar();
    }

    @Override
    protected void onResume() {
        super.onResume();
        hideSystemUi();
        presenter.onResume();
    }

    private void hideSystemUi() {
        if (!Utils.isKitKatOrHigher()) {
            getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                    WindowManager.LayoutParams.FLAG_FULLSCREEN);
        } else {
            hideSystemUiPreKitKat();
        }
    }

    @TargetApi(Build.VERSION_CODES.KITKAT)
    private void hideSystemUiPreKitKat() {
        getWindow().getDecorView().setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                        | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                        | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_FULLSCREEN
                        | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
        );
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    public void showAppsToShareWith(List<SocialNetwork> socialNetworks) {
        socialNetworkAdapter.setSocialNetworks(socialNetworks);
    }


    @OnTouch(R.id.videoPreview)
    public boolean togglePlayPause(MotionEvent event) {
        boolean result = false;
        if (event.getAction() == MotionEvent.ACTION_DOWN) {
            if (videoPreview.isPlaying()) {
                pause();
                result = true;
            } else {
                play();
                result = false;
            }
        }
        return result;
    }

    @OnClick(R.id.playButton)
    @Override
    public void play() {
        videoPreview.start();
        playButton.setVisibility(View.GONE);
    }

    @Override
    public void pause() {
        videoPreview.pause();
        playButton.setVisibility(View.VISIBLE);
    }

    @Override
    public void seekTo(int milliseconds) {
        videoPreview.seekTo(milliseconds);
    }

    private void updateSeekbar() {
        if (!draggingSeekBar)
            seekBar.setProgress(videoPreview.getCurrentPosition());
        updateSeekBarTaskHandler.postDelayed(updateSeekBarTask, 20);
    }

    @Override
    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
        if (fromUser)
            seekTo(progress);
    }

    @Override
    public void onStartTrackingTouch(SeekBar seekBar) {
        draggingSeekBar = true;
    }

    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {
        draggingSeekBar = false;
    }

    @OnClick(R.id.settingsButton)
    public void goToSettings() {
        Intent intent = new Intent(this, SettingsActivity.class);
        startActivity(intent);
    }

    @OnClick(R.id.backButton)
    public void goBack() {
        Intent intent = new Intent(this, RecordActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        goBack();
    }

    @OnClick(R.id.moreSharingOptionsButton)
    public void showMoreNetworks() {
        updateNumTotalVideosShared();
        trackVideoShared(null);
        Intent intent = new Intent(Intent.ACTION_SEND);
        intent.setType("video/*");
        Uri uri = Utils.obtainUriToShare(this, videoPath);
        intent.putExtra(Intent.EXTRA_STREAM, uri);
        startActivity(Intent.createChooser(intent, getString(R.string.share_using)));
    }

    @Override
    public void onSocialNetworkClicked(SocialNetwork socialNetwork) {
        presenter.shareVideo(videoPath, socialNetwork, this);
        updateNumTotalVideosShared();
        trackVideoShared(socialNetwork);
    }

    private void updateNumTotalVideosShared() {
        int totalVideosShared = sharedPreferences.getInt(ConfigPreferences.TOTAL_VIDEOS_SHARED, 0);
        preferencesEditor.putInt(ConfigPreferences.TOTAL_VIDEOS_SHARED, ++totalVideosShared);
        preferencesEditor.commit();
    }

    private void trackVideoShared(SocialNetwork socialNetwork) {
        String socialNetworkName = null;
        if (socialNetwork != null)
            socialNetworkName = socialNetwork.getName();
        JSONObject socialNetworkProperties = new JSONObject();
        try {
            socialNetworkProperties.put(AnalyticsConstants.SOCIAL_NETWORK, socialNetworkName);
            socialNetworkProperties.put(AnalyticsConstants.VIDEO_LENGTH, presenter.getVideoLength());
            socialNetworkProperties.put(AnalyticsConstants.RESOLUTION, presenter.getResolution());
            socialNetworkProperties.put(AnalyticsConstants.NUMBER_OF_CLIPS, presenter.getNumberOfClips());
            socialNetworkProperties.put(AnalyticsConstants.TOTAL_SHARED_VIDEOS,
                    sharedPreferences.getInt(ConfigPreferences.TOTAL_VIDEOS_SHARED, 0));
            mixpanel.track(AnalyticsConstants.VIDEO_SHARED, socialNetworkProperties);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        mixpanel.getPeople().increment(AnalyticsConstants.TOTAL_SHARED_VIDEOS, 1);
        mixpanel.getPeople().set(AnalyticsConstants.LAST_VIDEO_SHARED,
                new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZ").format(new Date()));
    }

    class VideoPreviewEventListener implements MediaPlayer.OnCompletionListener, MediaPlayer.OnPreparedListener {
        @Override
        public void onCompletion(MediaPlayer mediaPlayer) {
            playButton.setVisibility(View.VISIBLE);
            seekBar.setProgress(0);
        }

        @Override
        public void onPrepared(MediaPlayer mediaPlayer) {
            mediaPlayer.start();
            try {
                seekTo(100);
                Thread.sleep(10);
            } catch (InterruptedException e) {
                Log.d("Share", "error while preparing preview");
            }
            initSeekBar(videoPreview.getCurrentPosition(), videoPreview.getDuration());
            pause();
        }
    }


}
