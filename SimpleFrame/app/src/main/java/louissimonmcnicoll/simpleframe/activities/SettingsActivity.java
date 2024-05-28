package louissimonmcnicoll.simpleframe.activities;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.os.Bundle;
import android.preference.EditTextPreference;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceCategory;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.Toolbar;

import java.util.ArrayList;
import java.util.Map;
import java.util.Objects;

import louissimonmcnicoll.simpleframe.R;
import louissimonmcnicoll.simpleframe.settings.AppData;
import louissimonmcnicoll.simpleframe.settings.SimpleFileDialog;

@SuppressWarnings("deprecation")
public class SettingsActivity extends PreferenceActivity implements SharedPreferences.OnSharedPreferenceChangeListener {

    private static final String TAG = SettingsActivity.class.getSimpleName();
    private PreferenceCategory sourceSettingsPreferenceCategory;
    private SharedPreferences sharedPreferences;
    private final ArrayList<String> editableTitleFields = new ArrayList<>();
    private final static boolean DEBUG = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        PreferenceManager prefMgr = getPreferenceManager();
        prefMgr.setSharedPreferencesName(AppData.mySettingsFilename);
        prefMgr.setSharedPreferencesMode(MODE_PRIVATE);
        sharedPreferences = AppData.getSharedPreferences(getApplicationContext());
        sharedPreferences.registerOnSharedPreferenceChangeListener(this);

        if (DEBUG) printAllPreferences();
        addPreferencesFromResource(R.xml.settings);
        sourceSettingsPreferenceCategory = (PreferenceCategory) findPreference("sett_key_cat1");

        populateEditableFieldsList();
        updateAllFieldTitles();
        setupFolderPicker();
    }

    private void printAllPreferences() {
        Map<String, ?> keyMap = sharedPreferences.getAll();
        for (String e : keyMap.keySet()) {
            debug("DUMP| Key: " + e + " ++ Value: " + keyMap.get(e));
        }
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);

        LinearLayout root = (LinearLayout) findViewById(android.R.id.list).getParent().getParent().getParent();
        Toolbar bar = (Toolbar) LayoutInflater.from(this).inflate(R.layout.settings_toolbar, root, false);
        root.addView(bar, 0); // insert at top
        bar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }

    private void populateEditableFieldsList() {
        editableTitleFields.add(getString(R.string.sett_key_displaytime));
        editableTitleFields.add(getString(R.string.sett_key_transition));
        editableTitleFields.add(getString(R.string.sett_key_srcpath_sd));
    }

    private void setupFolderPicker() {
        Preference folderPicker;
        folderPicker = new Preference(this);
        folderPicker.setTitle(getString(R.string.sett_srcPath_externalSD));
        folderPicker.setSummary(AppData.getImagePath(getApplicationContext()));
        folderPicker.setDefaultValue("");
        folderPicker.setKey(getString(R.string.sett_key_srcpath_sd));
        Context self = this;
        folderPicker.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            String _chosenDir = AppData.getSourcePath(self);
            @Override
            public boolean onPreferenceClick(Preference preference) {
                // The code in this function will be executed when the dialog OK button is pushed
                SimpleFileDialog FolderChooseDialog =
                        new SimpleFileDialog(
                                self,
                                chosenDir -> {
                                    _chosenDir = chosenDir;
                                    AppData.setSdSourcePath(self, _chosenDir);
                                });
                FolderChooseDialog.chooseFile_or_Dir(_chosenDir);
                return true;
            }
        });
        sourceSettingsPreferenceCategory.addPreference(folderPicker);
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        if (sharedPreferences != null && key != null) {
            debug("CHANGED| Key:" + key + " ++ Value: " + sharedPreferences.getAll().get(key));
        }
    }

    public void updateAllFieldTitles() {
        for (String prefKey : editableTitleFields) {
            updateFieldTitle(prefKey);
        }
    }

    public void updateFieldTitle(String key) {
        Preference mPref = findPreference(key);
        String mPrefTitle = "";
        String mPrefValue = "";

        if (mPref != null) {
            if (mPref instanceof ListPreference) {
                mPrefValue = (String) sharedPreferences.getAll().get(key);
                if (!Objects.equals(mPrefValue, ((ListPreference) mPref).getValue())) {
                    ((ListPreference) mPref).setValue(mPrefValue);
                }
                int index = ((ListPreference) mPref).findIndexOfValue(mPrefValue);
                mPrefValue = (String) ((ListPreference) mPref).getEntries()[index];
            } else if (mPref instanceof EditTextPreference) {
                mPrefValue = (String) sharedPreferences.getAll().get(key);
            } else if (getString(R.string.sett_key_srcpath_sd).equals(mPref.getKey())) {
                mPrefValue = AppData.getSourcePath(getApplicationContext());
            }
            if (getString(R.string.sett_key_displaytime).equals(key)) {
                mPrefTitle = getString(R.string.sett_displayTime);
            } else if (getString(R.string.sett_key_transition).equals(key)) {
                mPrefTitle = getString(R.string.sett_transition);
            } else if (getString(R.string.sett_key_srcpath_sd).equals(key)) {
                mPrefTitle = getString(R.string.sett_srcPath_externalSD);
            }
            mPref.setTitle(mPrefTitle + ": " + mPrefValue);
        }
    }

    @Override
    public void onConfigurationChanged(@NonNull Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
    }

    private void debug(String msg) {
        if (DEBUG) {
            Log.d(TAG, msg);
        }
    }
}