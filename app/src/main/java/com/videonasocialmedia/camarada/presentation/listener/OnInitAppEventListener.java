/*
 * Copyright (c) 2015. Videona Socialmedia SL
 * http://www.videona.com
 * info@videona.com
 * All rights reserved
 */

package com.videonasocialmedia.camarada.presentation.listener;

/**
 * This interface is used to control the init config of the app on shared preferences.
 */
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
