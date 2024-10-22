package com.videonasocialmedia.kamarada.presentation.mvp.presenters;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;

import com.videonasocialmedia.kamarada.domain.ObtainSocialNetworksToShareUseCase;
import com.videonasocialmedia.kamarada.model.entities.SocialNetwork;
import com.videonasocialmedia.kamarada.presentation.mvp.views.PreviewVideoView;
import com.videonasocialmedia.kamarada.presentation.mvp.views.ShareView;
import com.videonasocialmedia.kamarada.utils.ConfigPreferences;
import com.videonasocialmedia.kamarada.utils.Utils;

import java.util.List;

/**
 * Created by jca on 19/1/16.
 */
public class SharePresenter {

    private ShareView shareView;
    private PreviewVideoView videoPreview;
    private SharedPreferences sharedPreferences;

    public SharePresenter(ShareView shareView, PreviewVideoView videoPreview,
                          SharedPreferences sharedPreferences) {
        this.shareView = shareView;
        this.videoPreview = videoPreview;
        this.sharedPreferences = sharedPreferences;
    }

    public void onResume() {
        obtainSocialNetworksAvailableToShare();
    }

    public void obtainSocialNetworksAvailableToShare() {
        ObtainSocialNetworksToShareUseCase useCase = new ObtainSocialNetworksToShareUseCase();
        List<SocialNetwork> socialNetworks = useCase.getKnownSocialNetworks();
        shareView.showAppsToShareWith(socialNetworks);
    }

    public void onPause() {

    }

    public void shareVideo(String videoPath, SocialNetwork appToShareWith, Context ctx) {

        final ComponentName name = new ComponentName(appToShareWith.getAndroidPackageName(),
                appToShareWith.getAndroidActivityName());

        Uri uri = Utils.obtainUriToShare(ctx, videoPath);

        Intent intent = new Intent(Intent.ACTION_SEND);
        intent.setType("video/*");
        intent.putExtra(Intent.EXTRA_STREAM, uri);
        intent.addCategory(Intent.CATEGORY_DEFAULT);
        //intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED);
        intent.setComponent(name);
        ctx.startActivity(intent);
    }

    public double getVideoLength() {
        return sharedPreferences.getLong(ConfigPreferences.VIDEO_DURATION, 0);
    }

    public double getNumberOfClips() {
        return sharedPreferences.getInt(ConfigPreferences.NUMBER_OF_CLIPS, 1);
    }

    public String getResolution() {
        return sharedPreferences.getString(ConfigPreferences.RESOLUTION, "1280x720");
    }


}
