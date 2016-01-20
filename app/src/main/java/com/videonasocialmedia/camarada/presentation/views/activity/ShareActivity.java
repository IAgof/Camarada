package com.videonasocialmedia.camarada.presentation.views.activity;

import android.media.MediaPlayer;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.widget.VideoView;

import com.videonasocialmedia.camarada.R;
import com.videonasocialmedia.camarada.model.SocialNetwork;
import com.videonasocialmedia.camarada.presentation.adapter.SocialNetworkAdapter;
import com.videonasocialmedia.camarada.presentation.listener.OnSocialNetworkClickedListener;
import com.videonasocialmedia.camarada.presentation.mvp.presenters.SharePresenter;
import com.videonasocialmedia.camarada.presentation.mvp.views.PreviewVideoView;
import com.videonasocialmedia.camarada.presentation.mvp.views.ShareView;

import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;

/**
 * Created by jca on 18/1/16.
 */
public class ShareActivity extends AppCompatActivity implements ShareView, PreviewVideoView,
        OnSocialNetworkClickedListener {

    @Bind(R.id.videoPreview)
    VideoView videoPreview;
    @Bind(R.id.socialNetworkRecycler)
    RecyclerView socialNetworkRecycler;
    private SharePresenter presenter;
    private String videoPath;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ButterKnife.bind(this);

        presenter = new SharePresenter(this, this);

        initVideoPreview();
        initSocialNetworkContainer();
    }

    private void initVideoPreview() {
        VideoPreviewEventListener videoPreviewEventListener = new VideoPreviewEventListener();
        videoPreview.setOnCompletionListener(videoPreviewEventListener);
        videoPreview.setOnPreparedListener(videoPreviewEventListener);
    }

    private void initSocialNetworkContainer(){
        RecyclerView.LayoutManager layoutManager=
                new LinearLayoutManager(this,LinearLayoutManager.VERTICAL,false);
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
    }

    class VideoPreviewEventListener implements MediaPlayer.OnCompletionListener, MediaPlayer.OnPreparedListener{
        @Override
        public void onCompletion(MediaPlayer mp) {

        }

        @Override
        public void onPrepared(MediaPlayer mp) {

        }
    }


}
