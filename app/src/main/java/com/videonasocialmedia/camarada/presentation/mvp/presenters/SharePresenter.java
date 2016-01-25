package com.videonasocialmedia.camarada.presentation.mvp.presenters;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;

import com.videonasocialmedia.camarada.domain.ObtainSocialNetworksToShareUseCase;
import com.videonasocialmedia.camarada.model.SocialNetwork;
import com.videonasocialmedia.camarada.presentation.mvp.views.PreviewVideoView;
import com.videonasocialmedia.camarada.presentation.mvp.views.ShareView;
import com.videonasocialmedia.camarada.utils.Utils;

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

    public void onResume() {
        obtainSocialNetworksAvailableToShare();
    }

    public void onPause() {

    }

    public void obtainSocialNetworksAvailableToShare() {
        ObtainSocialNetworksToShareUseCase useCase = new ObtainSocialNetworksToShareUseCase();
        List<SocialNetwork> socialNetworks = useCase.getKnownSocialNetworks();
        shareView.showAppsToShareWith(socialNetworks);
    }

    public void shareVideo(String videoPath, SocialNetwork appToShareWith, Context ctx) {

        final ComponentName name = new ComponentName(appToShareWith.getAndroidPackageName(),
                appToShareWith.getAndroidActivityName());

        Uri uri = Utils.obtainUriToShare(ctx, videoPath);

        Intent intent = new Intent(Intent.ACTION_SEND);
        intent.setType("video/*");
        intent.putExtra(Intent.EXTRA_STREAM, uri);
        intent.addCategory(Intent.CATEGORY_LAUNCHER);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED);
        intent.setComponent(name);
        ctx.startActivity(intent);
    }

}
