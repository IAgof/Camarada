package com.videonasocialmedia.camarada.presentation.views.activity;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;

import com.videonasocialmedia.camarada.R;
import com.videonasocialmedia.camarada.presentation.views.dialog.CamaradaDialogActivity;
import com.videonasocialmedia.camarada.presentation.views.fragment.SettingsFragment;
import com.videonasocialmedia.camarada.presentation.views.listener.OnCamaradaDialogClickListener;



public class SettingsActivity extends CamaradaActivity implements OnCamaradaDialogClickListener {

    private final int REQUEST_CODE_DIALOG_VOTE = 1;
    private CamaradaDialogActivity dialogVote;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(false);
        android.support.v7.app.ActionBar ab = getSupportActionBar();
        ab.setDisplayHomeAsUpEnabled(true);

        getFragmentManager().beginTransaction()
                .replace(R.id.fragment_preferences, new SettingsFragment())
                .commit();

    }

    @Override
    protected void onStart() {
        super.onStart();
        mixpanel.timeEvent("Time in Settings Activity");
    }

    @Override
    protected void onResume() {
        super.onResume();

    }

    @Override
    protected void onPause() {
        super.onPause();
        mixpanel.track("Time in Settings Activity");

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_settings, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_help:
                goToContact();
                return true;
            case R.id.action_vote:
                goToVote();
                return true;
            default:
                // If we got here, the user's action was not recognized.
                // Invoke the superclass to handle it.
                return super.onOptionsItemSelected(item);
        }
    }


    private void goToContact() {
        navigateTo("mailto:info@videona.com");
    }

    private void goToVote() {

        dialogVote = new CamaradaDialogActivity().newInstance(
                getString(R.string.rateUsDialogTitle),
                getString(R.string.rateUsDialogMessage),
                getString(R.string.acceptDialogRateUs),
                getString(R.string.cancelDialogRateUs),
                REQUEST_CODE_DIALOG_VOTE
        );

        dialogVote.show(getFragmentManager(),"DialogVote");

    }

    private void navigateTo(String url) {
        Intent i = new Intent(Intent.ACTION_VIEW);
        i.setData(Uri.parse(url));
        startActivity(i);
    }

    @Override
    public void onClickAcceptDialogListener(int id) {
        if(id == REQUEST_CODE_DIALOG_VOTE) {
            navigateTo("market://details?id=com.videonasocialmedia.videona");
        }
    }

    @Override
    public void onClickCancelDialogListener(int id) {
        if(id == REQUEST_CODE_DIALOG_VOTE) {
            navigateTo("mailto:info@videona.com");
        }
    }
}