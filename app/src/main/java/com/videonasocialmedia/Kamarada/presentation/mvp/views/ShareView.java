package com.videonasocialmedia.Kamarada.presentation.mvp.views;

import com.videonasocialmedia.Kamarada.model.entities.SocialNetwork;

import java.util.List;

/**
 * Created by jca on 19/1/16.
 */
public interface ShareView {
    void showAppsToShareWith(List<SocialNetwork> socialNetworks);
}
