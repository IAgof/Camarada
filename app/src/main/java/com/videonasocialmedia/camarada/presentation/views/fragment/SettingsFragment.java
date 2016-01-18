public class SettingsFragment extends PreferenceFragment implements SharedPreferences.OnSharedPreferenceChangeListener,
        PreferencesView, OnVideonaDialogButtonsListener {


    protected PreferencesPresenter preferencesPresenter;
    protected Context context;
    protected SharedPreferences sharedPreferences;
    protected SharedPreferences.Editor editor;

    /* Dialogs*/
    private VideonaDialogFragment dialogExitApp;
    private final int REQUEST_CODE_DIALOG_EXIT = 1;


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
        setupBetaPreference();

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

    private void setupBetaPreference() {
        Preference joinBetaPref = findPreference("beta");
        joinBetaPref.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {

                    createBetaDialog();

                return true;
            }
        });
    }

    private void createBetaDialog() {

        dialogBeta = new VideonaDialogFragment().newInstance(
                getString(R.string.betaDialogTitle),
                getString(R.string.betaDialogMessage),
                getString(R.string.betaDialogAccept),
                getString(R.string.betaDialogCancel),
                REQUEST_CODE_DIALOG_BETA
        );

        dialogBeta.setTargetFragment(this, REQUEST_CODE_DIALOG_BETA);
        dialogBeta.show(getFragmentManager(), "Settings beta fragment");
    }

    private void createExitAppDialog() {

        dialogExitApp = new VideonaDialogFragment().newInstance(
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
        View v = inflater.inflate(R.layout.list, null);
        ListView listView = (ListView)v.findViewById(android.R.id.list);

        ViewGroup footer = (ViewGroup) inflater.inflate(R.layout.footer, listView, false);
        listView.addFooterView(footer, null, false);

        TextView footerText = (TextView)v.findViewById(R.id.footerText);
        String text = getString(R.string.videona) + " v" + BuildConfig.VERSION_NAME + "\n" +
                getString(R.string.madeIn);
        footerText.setText(text);

        return v;
    }

    @Override
    public void onResume() {
        super.onResume();
        preferencesPresenter.checkAvailablePreferences();
        sharedPreferences.registerOnSharedPreferenceChangeListener(this);
        sharedPreferences.registerOnSharedPreferenceChangeListener(preferencesPresenter);
        Qordoba.updateScreen(getActivity());
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
        for (int i=0; i<size; i++) {
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

        if(id == REQUEST_CODE_DIALOG_EXIT) {
            dialogExitApp.dismiss();
        }

        if(id == REQUEST_CODE_DIALOG_BETA) {
            dialogBeta.dismissDialog();
        }
    }

    @Override
    public void onClickAcceptDialogListener(int id) {

        if(id == REQUEST_CODE_DIALOG_EXIT) {
            exitApp();
        }

        if(id == REQUEST_CODE_DIALOG_BETA){

            String url = "https://play.google.com/apps/testing/com.videonasocialmedia.videona";
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
