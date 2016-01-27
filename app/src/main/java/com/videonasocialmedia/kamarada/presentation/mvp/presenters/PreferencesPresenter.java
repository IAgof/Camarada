package com.videonasocialmedia.kamarada.presentation.mvp.presenters;

import android.content.Context;
import android.content.SharedPreferences;

import com.videonasocialmedia.kamarada.presentation.mvp.views.PreferencesView;

import java.util.ArrayList;

/**
 * This class is used to show the setting menu.
 */
public class PreferencesPresenter implements SharedPreferences.OnSharedPreferenceChangeListener {

    private Context context;
    private SharedPreferences sharedPreferences;
    private PreferencesView preferencesView;

    /**
     * Constructor
     *
     * @param preferencesView
     * @param context
     * @param sharedPreferences
     */
    public PreferencesPresenter(PreferencesView preferencesView, Context context,
                                SharedPreferences sharedPreferences) {
        this.preferencesView = preferencesView;
        this.context = context;
        this.sharedPreferences = sharedPreferences;
    }


    /**
     * Checks if the actual default value in shared preferences is supported by the device
     *
     * @param key    the key of the shared preference
     * @param values the supported values for this preference
     * @return return true if the default value is not supported by the device, so update it
     */
    private boolean updateDefaultPreference(String key, ArrayList<String> values) {
        boolean result = false;
        String actualDefaultValue = sharedPreferences.getString(key, "");
        if (!values.contains(actualDefaultValue)) {
            result = true;
        }
        return result;
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {

    }

}