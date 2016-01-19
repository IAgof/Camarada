package com.videonasocialmedia.camarada.presentation.mvp.presenters;

import com.videonasocialmedia.camarada.domain.ObtainSocialNetworksToShareUseCase;
import com.videonasocialmedia.camarada.model.SocialNetwork;
import com.videonasocialmedia.camarada.presentation.mvp.views.PreviewVideoView;
import com.videonasocialmedia.camarada.presentation.mvp.views.ShareView;

import java.util.List;

/**
 * Created by jca on 19/1/16.
 */
public class SharePresenter {

    private ShareView shareView;
    private PreviewVideoView videoPreview;

    public SharePresenter(ShareView shareView, PreviewVideoView videoPreview) {
        this.shareView = shareView;
        this.videoPreview = videoPreview;
    }

    public void onResume(){
        obtainSocialNetworksAvailableToShare();
    }

    public void onPause(){

    }

    public void obtainSocialNetworksAvailableToShare(){
        ObtainSocialNetworksToShareUseCase useCase= new ObtainSocialNetworksToShareUseCase();
        List<SocialNetwork> socialNetworks=useCase.getSocialNetworks();
        shareView.showAppsToShareWith(socialNetworks);
    }

}
