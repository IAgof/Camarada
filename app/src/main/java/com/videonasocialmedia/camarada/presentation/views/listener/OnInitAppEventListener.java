package com.videonasocialmedia.camarada.presentation.views.listener;

public interface OnInitAppEventListener {

    /**
     *  Fires when the paths of the app has been created
     */
    void onCheckPathsAppSuccess();

    /**
     *  Fires when failed creating the paths of the app
     */
    void onCheckPathsAppError();
}
