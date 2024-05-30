package louissimonmcnicoll.simpleframe.activities;

import android.annotation.SuppressLint;
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
        setFolderPickerTitle(folderPicker);
        folderPicker.setSummary(getString(R.string.sett_srcPath_externalSD));
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

    @SuppressLint("ApplySharedPref")
    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        if (sharedPreferences != null && key != null) {
            // If we change the image path, we also want to reset the current page to start over
            if (key.equals(getString(R.string.sett_key_srcpath_sd))){
                // We use commit() and not apply() because we want the update to be immediate
                sharedPreferences.edit().putInt(getString(R.string.sett_key_currentPage), 1).commit();
            }
            updateAllFieldTitles();
            debug("CHANGED| Key:" + key + " ++ Value: " + sharedPreferences.getAll().get(key));
        }
    }

    public void updateAllFieldTitles() {
        for (String prefKey : editableTitleFields) {
            updateField(prefKey);
        }
    }

    public void updateField(String key) {
        Preference pref = findPreference(key);
        if (pref != null) {
            String prefValue = "";
            if (pref instanceof ListPreference) {
                prefValue = (String) sharedPreferences.getAll().get(key);
                if (!Objects.equals(prefValue, ((ListPreference) pref).getValue())) {
                    ((ListPreference) pref).setValue(prefValue);
                }
                int index = ((ListPreference) pref).findIndexOfValue(prefValue);
                prefValue = (String) ((ListPreference) pref).getEntries()[index];
            } else if (pref instanceof EditTextPreference) {
                prefValue = (String) sharedPreferences.getAll().get(key);
            }
            if (getString(R.string.sett_key_displaytime).equals(key)) {
                String prefTitle = getString(R.string.sett_displayTime);
                pref.setTitle(prefTitle + ": " + prefValue);
            } else if (getString(R.string.sett_key_transition).equals(key)) {
                String prefTitle = getString(R.string.sett_transition);
                pref.setTitle(prefTitle + ": " + prefValue);
            } else if (getString(R.string.sett_key_srcpath_sd).equals(key)) {
                setFolderPickerTitle(pref);
            }
        }
    }

    private void setFolderPickerTitle(Preference folderPickerPref){
        String imagePath = AppData.getImagePath(getApplicationContext());
        // Make the path a bit mo
        imagePath = imagePath.replace("/storage/emulated/0/", "");
        folderPickerPref.setTitle(imagePath);
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