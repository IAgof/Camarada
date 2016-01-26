package com.videonasocialmedia.camarada.presentation.views.activity;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageButton;
import android.widget.SeekBar;
import android.widget.VideoView;

import com.videonasocialmedia.camarada.R;
import com.videonasocialmedia.camarada.model.SocialNetwork;
import com.videonasocialmedia.camarada.presentation.adapter.SocialNetworkAdapter;
import com.videonasocialmedia.camarada.presentation.listener.OnSocialNetworkClickedListener;
import com.videonasocialmedia.camarada.presentation.mvp.presenters.SharePresenter;
import com.videonasocialmedia.camarada.presentation.mvp.views.PreviewVideoView;
import com.videonasocialmedia.camarada.presentation.mvp.views.ShareView;
import com.videonasocialmedia.camarada.utils.ConfigPreferences;
import com.videonasocialmedia.camarada.utils.Utils;

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
public class ShareActivity extends CamaradaActivity implements ShareView, PreviewVideoView,
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
        presenter.onResume();
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
        preferencesEditor.putInt(ConfigPreferences.TOTAL_VIDEOS_SHARED, totalVideosShared++);
        preferencesEditor.commit();
    }


    private void trackVideoShared(SocialNetwork socialNetwork) {
        String socialNetworkName = null;
        if(socialNetwork != null)
            socialNetworkName = socialNetwork.getName();
        JSONObject socialNetworkProperties = new JSONObject();
        try {
            socialNetworkProperties.put("socialNetwork", socialNetworkName);
            socialNetworkProperties.put("videoLength", presenter.getVideoLength());
            socialNetworkProperties.put("resolution", presenter.getResolution());
            socialNetworkProperties.put("numberOfClips", presenter.getNumberOfClips());
            socialNetworkProperties.put("totalSharedVideos",
                    sharedPreferences.getInt(ConfigPreferences.TOTAL_VIDEOS_SHARED, 0));
            mixpanel.track("Video Shared", socialNetworkProperties);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        mixpanel.getPeople().increment("totalSharedVideos", 1);
        mixpanel.getPeople().set("lastVideoShared", new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZ")
                .format(new Date()));
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
            mediaPlayer.pause();
        }
    }


}
