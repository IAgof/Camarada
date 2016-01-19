package com.videonasocialmedia.camarada.presentation.views.activity;

import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.TextView;

import com.google.android.gms.analytics.GoogleAnalytics;
import com.google.android.gms.analytics.HitBuilders;
import com.videonasocialmedia.camarada.BuildConfig;
import com.videonasocialmedia.camarada.R;

import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.OnClick;

public class AboutActivity extends CamaradaActivity {

    @InjectView(R.id.videona_version)
    TextView versionName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_about);
        ButterKnife.inject(this);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(false);
        ActionBar ab = getSupportActionBar();
        ab.setDisplayHomeAsUpEnabled(true);

        String version= BuildConfig.VERSION_NAME + "\n";
        versionName.setText(version);
        // Display the fragment as the main content.

    }

    @Override
    protected void onStart() {
        super.onStart();
        mixpanel.timeEvent("Time in About Activity");
    }

    @Override
    protected void onPause() {
        super.onPause();
        mixpanel.track("Time in About Activity");
    }

    /**
     * Tracks when user clicks the link to go to Videona web page
     */
    @OnClick({R.id.videona_web})
    public void clickListener(View view) {
        sendButtonTracked(view.getId());
    }

    /**
     * Sends button clicks to Google Analytics
     *
     * @param id the identifier of the clicked button
     */
    private void sendButtonTracked(int id) {
        String label;
        switch (id) {
            case R.id.videona_web:
                label = "Go to Videona web page from App";
                break;
            default:
                label = "Other";
        }
        tracker.send(new HitBuilders.EventBuilder()
                .setCategory("AboutActivity")
                .setAction("link clicked")
                .setLabel(label)
                .build());
        GoogleAnalytics.getInstance(this.getApplication().getBaseContext()).dispatchLocalHits();
    }

}