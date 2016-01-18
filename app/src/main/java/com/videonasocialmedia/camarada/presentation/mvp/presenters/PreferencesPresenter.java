/**
 * This class is used to show the setting menu.
 */
public class PreferencesPresenter implements SharedPreferences.OnSharedPreferenceChangeListener{

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
     * Checks user preferences data
     */
    private void checkUserAccountData() {
        checkUserAccountPreference(ConfigPreferences.NAME);
        checkUserAccountPreference(ConfigPreferences.USERNAME);
        checkUserAccountPreference(ConfigPreferences.EMAIL);
    }

    private void checkUserAccountPreference(String key) {
        String data = sharedPreferences.getString(key, null);
        if (data != null && !data.isEmpty())
            preferencesView.setSummary(key, data);
    }


    /**
     * Checks if the actual default value in shared preferences is supported by the device
     *
     * @param key the key of the shared preference
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
        if (key.compareTo(ConfigPreferences.KEY_LIST_PREFERENCES_QUALITY) == 0 ||
                key.compareTo(ConfigPreferences.KEY_LIST_PREFERENCES_RESOLUTION) == 0) {
            if (BuildConfig.FLAVOR.compareTo("stable") == 0) {
                RemoveVideosUseCase videoRemover = new RemoveVideosUseCase();
                videoRemover.removeMediaItemsFromProject();
            }
        }
    }

}