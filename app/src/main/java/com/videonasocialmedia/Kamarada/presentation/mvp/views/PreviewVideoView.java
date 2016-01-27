package com.videonasocialmedia.kamarada.presentation.mvp.views;

/**
 * Created by jca on 19/1/16.
 */
public interface PreviewVideoView {
    void play();

    void pause();

    void seekTo(int milliseconds);
}
