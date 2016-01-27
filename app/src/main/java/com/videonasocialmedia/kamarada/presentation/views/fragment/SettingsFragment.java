package com.videonasocialmedia.kamarada.presentation.views.fragment;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.TextView;

import com.videonasocialmedia.kamarada.BuildConfig;
import com.videonasocialmedia.kamarada.R;
import com.videonasocialmedia.kamarada.presentation.listener.OnKamaradaDialogClickListener;
import com.videonasocialmedia.kamarada.presentation.mvp.presenters.PreferencesPresenter;
import com.videonasocialmedia.kamarada.presentation.mvp.presenters.PreferencesView;
import com.videonasocialmedia.kamarada.presentation.views.dialog.KamaradaDialogFragment;
import com.videonasocialmedia.kamarada.utils.ConfigPreferences;

import java.util.ArrayList;

public class SettingsFragment extends PreferenceFragment implements SharedPreferences.OnSharedPreferenceChangeListener,
        PreferencesView, OnKamaradaDialogClickListener {


    private final int REQUEST_CODE_DIALOG_EXIT = 1;
    private final int REQUEST_CODE_DOWNLOAD_BETA = 2;
    protected PreferencesPresenter preferencesPresenter;
    protected Context context;
    protected SharedPreferences sharedPreferences;
    protected SharedPreferences.Editor editor;
    /* Dialogs*/
    private KamaradaDialogFragment dialogExitApp;
    private KamaradaDialogFragment dialogDownloadVideona;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        context = getActivity().getApplicationContext();
        initPreferences();
        preferencesPresenter = new PreferencesPresenter(this, context,
                sharedPreferences);

    }

    private void initPreferences() {
        addPreferencesFromResource(R.xml.preferences);

        getPreferenceManager().setSharedPreferencesName(ConfigPreferences.SETTINGS_SHARED_PREFERENCES_FILE_NAME);
        sharedPreferences = getActivity().getSharedPreferences(
                ConfigPreferences.SETTINGS_SHARED_PREFERENCES_FILE_NAME,
                Context.MODE_PRIVATE);
        editor = sharedPreferences.edit();

        setupExitPreference();
        downloadVideonaPreference();

    }

    private void setupExitPreference() {
        Preference exitPref = findPreference("exit");

        exitPref.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                createExitAppDialog();
                return true;
            }
        });

    }

    private void downloadVideonaPreference() {
        Preference joinBetaPref = findPreference("beta");
        joinBetaPref.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {

                createJoinVideonaDialog();

                return true;
            }
        });
    }

    private void createJoinVideonaDialog() {

        dialogDownloadVideona = new KamaradaDialogFragment().newInstance(
                getString(R.string.downloadVideonaDialogTitle),
                getString(R.string.downloadVideonaDialogMessage),
                getString(R.string.downloadVideonaDialogAccept),
                getString(R.string.downloadVideonaDialogCancel),
                REQUEST_CODE_DOWNLOAD_BETA
        );

        dialogDownloadVideona.setTargetFragment(this, REQUEST_CODE_DOWNLOAD_BETA);
        dialogDownloadVideona.show(getFragmentManager(), "Settings beta fragment");
    }

    private void createExitAppDialog() {

        dialogExitApp = new KamaradaDialogFragment().newInstance(
                getString(R.string.exitAppTitle),
                getString(R.string.exitAppMessage),
                getString(R.string.exitAppAccept),
                getString(R.string.exitAppCancel),
                REQUEST_CODE_DIALOG_EXIT
        );

        dialogExitApp.setTargetFragment(this, REQUEST_CODE_DIALOG_EXIT);
        dialogExitApp.show(getFragmentManager(), "Settins fragment");

    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.settings_list, null);
        ListView listView = (ListView) v.findViewById(android.R.id.list);

        ViewGroup footer = (ViewGroup) inflater.inflate(R.layout.settings_footer, listView, false);
        listView.addFooterView(footer, null, false);

        TextView footerText = (TextView) v.findViewById(R.id.footerText);
        String text = getString(R.string.kamarada) + " v" + BuildConfig.VERSION_NAME + "\n" +
                getString(R.string.madeIn);
        footerText.setText(text);

        return v;
    }

    @Override
    public void onResume() {
        super.onResume();

        sharedPreferences.registerOnSharedPreferenceChangeListener(this);
        sharedPreferences.registerOnSharedPreferenceChangeListener(preferencesPresenter);

    }

    @Override
    public void onPause() {
        super.onPause();
        sharedPreferences.unregisterOnSharedPreferenceChangeListener(this);
        sharedPreferences.unregisterOnSharedPreferenceChangeListener(preferencesPresenter);
    }

    @Override
    public void setAvailablePreferences(ListPreference preference, ArrayList<String> listNames,
                                        ArrayList<String> listValues) {
        int size = listNames.size();
        CharSequence entries[] = new String[size];
        CharSequence entryValues[] = new String[size];
        for (int i = 0; i < size; i++) {
            entries[i] = listNames.get(i);
            entryValues[i] = listValues.get(i);
        }
        preference.setEntries(entries);
        preference.setEntryValues(entryValues);
    }

    @Override
    public void setDefaultPreference(ListPreference preference, String name, String key) {
        preference.setValue(name);
        preference.setSummary(name);
        editor.putString(key, name);
        editor.commit();
    }

    @Override
    public void setPreference(ListPreference preference, String name) {
        preference.setValue(name);
        preference.setSummary(name);
    }

    @Override
    public void setSummary(String key, String value) {
        Preference preference = findPreference(key);
        preference.setSummary(value);
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences,
                                          String key) {
        Preference connectionPref = findPreference(key);
        connectionPref.setSummary(sharedPreferences.getString(key, ""));
    }

    @Override
    public void onClickCancelDialogListener(int id) {

        if (id == REQUEST_CODE_DIALOG_EXIT) {
            dialogExitApp.dismiss();
        }

        if (id == REQUEST_CODE_DOWNLOAD_BETA) {
            dialogDownloadVideona.dismissDialog();
        }
    }

    @Override
    public void onClickAcceptDialogListener(int id) {

        if (id == REQUEST_CODE_DIALOG_EXIT) {
            exitApp();
        }

        if (id == REQUEST_CODE_DOWNLOAD_BETA) {

            //String url = getString(R.string.videonaGooglePlayLink);
            String url = "https://play.google.com/store/apps/details?id=com.videonasocialmedia.videona&referrer=utm_source%3DKamarada%26utm_medium%3DKamaradaApp%26utm_term%3DGetVideona%26utm_content%3DGetVideonaFromKamarada%26utm_campaign%3DDownloadVideona";
            Intent i = new Intent(Intent.ACTION_VIEW);
            i.setData(Uri.parse(url));
            startActivity(i);
        }
    }


    private void exitApp() {
        Intent intent = new Intent(Intent.ACTION_MAIN);
        intent.addCategory(Intent.CATEGORY_HOME);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
        getActivity().finish();
        System.exit(0);
    }

}
