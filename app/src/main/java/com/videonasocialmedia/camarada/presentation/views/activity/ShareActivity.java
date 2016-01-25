package com.videonasocialmedia.camarada.presentation.views.activity;

import android.media.MediaPlayer;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.widget.VideoView;

import com.google.android.gms.analytics.GoogleAnalytics;
import com.google.android.gms.analytics.HitBuilders;
import com.videonasocialmedia.camarada.R;
import com.videonasocialmedia.camarada.model.SocialNetwork;
import com.videonasocialmedia.camarada.presentation.adapter.SocialNetworkAdapter;
import com.videonasocialmedia.camarada.presentation.listener.OnSocialNetworkClickedListener;
import com.videonasocialmedia.camarada.presentation.mvp.presenters.SharePresenter;
import com.videonasocialmedia.camarada.presentation.mvp.views.PreviewVideoView;
import com.videonasocialmedia.camarada.presentation.mvp.views.ShareView;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;

/**
 * Created by jca on 18/1/16.
 */
public class ShareActivity extends CamaradaActivity implements ShareView, PreviewVideoView,
        OnSocialNetworkClickedListener {

    public static final String INTENT_EXTRA_VIDEO_PATH = "VIDEO_EDITED";

    @Bind(R.id.videoPreview)
    VideoView videoPreview;
    @Bind(R.id.socialNetworkRecycler)
    RecyclerView socialNetworkRecycler;

    private SharePresenter presenter;
    private String videoPath;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.share_activity);
        ButterKnife.bind(this);

        presenter = new SharePresenter(this, this);

        initVideoPreview();
        initSocialNetworkContainer();
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
        SocialNetworkAdapter socialNetworkAdapter = new SocialNetworkAdapter(this);
        socialNetworkRecycler.setAdapter(socialNetworkAdapter);
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

    }

    @Override
    public void play() {
        videoPreview.start();
    }

    @Override
    public void pause() {
        videoPreview.pause();
    }

    @Override
    public void seekTo(int milliseconds) {
        videoPreview.seekTo(milliseconds);
    }

    @Override
    public void onSocialNetworkClicked(SocialNetwork socialNetwork) {
        presenter.shareVideo(videoPath, socialNetwork, this);
        trackVideoShared(socialNetwork);
    }


    private void trackVideoShared(SocialNetwork socialNetwork) {
        tracker.send(new HitBuilders.EventBuilder()
                .setCategory("ShareVideoActivity")
                .setAction("video shared")
                .setLabel(socialNetwork.getName())
                .build());
        GoogleAnalytics.getInstance(this.getApplication().getBaseContext()).dispatchLocalHits();
        JSONObject socialNetworkProperties = new JSONObject();
        try {
            socialNetworkProperties.put("Social Network", socialNetwork.getName());
            mixpanel.track("More social networks button clicked", socialNetworkProperties);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    class VideoPreviewEventListener implements MediaPlayer.OnCompletionListener, MediaPlayer.OnPreparedListener {
        @Override
        public void onCompletion(MediaPlayer mediaPlayer) {
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
            //initSeekBar(videoPreview.getCurrentPosition(), videoPreview.getDuration());
            mediaPlayer.pause();
        }
    }


}
