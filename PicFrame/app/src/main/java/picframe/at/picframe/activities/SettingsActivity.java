/*
    Copyright (C) 2015 Myra Fuchs, Linda Spindler, Clemens Hlawacek, Ebenezer Bonney Ussher,
    Martin Bayerl, Christoph Krasa

    This file is part of PicFrame.

    PicFrame is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    PicFrame is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with PicFrame.  If not, see <http://www.gnu.org/licenses/>.
*/

package picframe.at.picframe.activities;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.os.Bundle;
import android.preference.EditTextPreference;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceCategory;
import android.preference.PreferenceManager;
import android.preference.PreferenceScreen;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.appcompat.widget.Toolbar;

import java.util.ArrayList;
import java.util.Map;
import java.util.Objects;

import picframe.at.picframe.R;
import picframe.at.picframe.settings.AppData;
import picframe.at.picframe.settings.MySwitchPref;
import picframe.at.picframe.settings.detailsPrefScreen.DetailsPreferenceScreen;
import picframe.at.picframe.settings.detailsPrefScreen.ExtSdPrefs;

@SuppressWarnings("deprecation")
public class SettingsActivity extends PreferenceActivity implements SharedPreferences.OnSharedPreferenceChangeListener {

    private static final String TAG = SettingsActivity.class.getSimpleName();
    private PreferenceCategory preferenceCategory;
    private SharedPreferences sharedPreferences;
    private final ArrayList<String> editableTitleFields = new ArrayList<>();
    private final ArrayList<String> fieldsToRemove = new ArrayList<>();
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
        preferenceCategory = (PreferenceCategory) findPreference(getString(R.string.sett_key_cat2));

        populateEditableFieldsList();
        populateFieldsToRemove();
        createCat2Fields();
        updateAllFieldTitles();

        // set all missing fields

//        setCorrectSrcPathField();
//        updateTitlePrefsWithValues(mPrefs, "all");
        //System.out.println(AppData.toString());
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
        editableTitleFields.add(getString(R.string.sett_key_username));
        editableTitleFields.add(getString(R.string.sett_key_password));
    }

    private void populateFieldsToRemove() {
        fieldsToRemove.add(getString(R.string.sett_key_recursiveSearch));
        fieldsToRemove.add(getString(R.string.sett_key_deleteData));
        fieldsToRemove.add(getString(R.string.sett_key_srcpath_sd));
        fieldsToRemove.add(getString(R.string.sett_key_restoreDefaults));
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        if (sharedPreferences != null && key != null) {
            debug("CHANGED| Key:" + key + " ++ Value: " + sharedPreferences.getAll().get(key));
        }
        if (editableTitleFields.contains(key)) {
            //update display/transition title
            updateFieldTitle(key);
            if (getString(R.string.sett_key_srctype).equals(key)) {
                createCat2Fields();
            }
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
//                mPrefValue = ((ListPreference)mPref).getEntry() == null ? "" :  ((ListPreference)mPref).getEntry().toString();
                mPrefValue = (String) sharedPreferences.getAll().get(key);
                if (!Objects.equals(mPrefValue, ((ListPreference) mPref).getValue())) {
                    ((ListPreference) mPref).setValue(mPrefValue);
                }
                int index = ((ListPreference) mPref).findIndexOfValue(mPrefValue);
                mPrefValue = (String) ((ListPreference) mPref).getEntries()[index];
            } else if (mPref instanceof EditTextPreference) {
                mPrefValue = (String) sharedPreferences.getAll().get(key);
//                mPrefValue = ((EditTextPreference)mPref).getText();
                if (getString(R.string.sett_key_password).equals(key) && mPrefValue != null && !mPrefValue.isEmpty()) {
                    //noinspection ReplaceAllDot
                    mPrefValue = mPrefValue.replaceAll(".", "*");
                }
            } else if (getString(R.string.sett_key_srcpath_sd).equals(mPref.getKey()) &&
                    AppData.sourceTypes.ExternalSD.equals(AppData.getSourceType(getApplicationContext()))) {
                mPrefValue = AppData.getSourcePath(getApplicationContext());
            }
            if (getString(R.string.sett_key_displaytime).equals(key)) {
                mPrefTitle = getString(R.string.sett_displayTime);
            } else if (getString(R.string.sett_key_transition).equals(key)) {
                mPrefTitle = getString(R.string.sett_transition);
            } else if (getString(R.string.sett_key_srctype).equals(key)) {
                mPrefTitle = getString(R.string.sett_srcType);
            } else if (getString(R.string.sett_key_srcpath_sd).equals(key)) {
                mPrefTitle = getString(R.string.sett_srcPath_externalSD);
            } else if (getString(R.string.sett_key_username).equals(key)) {
                mPrefTitle = getString(R.string.sett_username);
            } else if (getString(R.string.sett_key_password).equals(key)) {
                mPrefTitle = getString(R.string.sett_password);
            } else if (getString(R.string.sett_key_srcpath_owncloud).equals(key)) {
                mPrefTitle = getString(R.string.sett_srcPath_OwnCloud);
            } else if (getString(R.string.sett_key_loginCheckButton).equals(key)) {
                mPrefTitle = getString(R.string.sett_loginCheck);
            }
            mPref.setTitle(mPrefTitle + ": " + mPrefValue);
        }
    }

    public void createCat2Fields() {
        removeCat2Fields();
        // add PreferenceScreens depending on which sourceType
        setDetailsPrefScreen();
        setIncludeSubdirsSwitchPref();
        setResetToDefaultButton();
    }

    private void removeCat2Fields() {
        // remove the preference screen, before adding it again
        PreferenceScreen removeScreen = (PreferenceScreen) findPreference(getString(R.string.sett_key_prefScreenDetails));
        if (removeScreen != null) {
            preferenceCategory.removePreference(removeScreen);
            debug("removed old Pref screen");
        }
        // remove the fields, before adding them again
        Preference removePref;
        for (String path : fieldsToRemove) {
            removePref = findPreference(path);
            if (removePref != null) {
                preferenceCategory.removePreference(removePref);
                debug("removed:" + removePref.getTitle().toString());
            }
        }
    }

    public void setDetailsPrefScreen() {
        if (AppData.sourceTypes.OwnCloud == AppData.getSourceType(getApplicationContext())) {
            DetailsPreferenceScreen detailsPrefScreenToAdd = new DetailsPreferenceScreen(
                    AppData.getSrcTypeInt(getApplicationContext()),
                    getPreferenceManager().createPreferenceScreen(this),
                    SettingsActivity.this);
            if (preferenceCategory != null && detailsPrefScreenToAdd.getPreferenceScreen() != null) {
                preferenceCategory.addPreference(detailsPrefScreenToAdd.getPreferenceScreen());
            }
        } else if (AppData.sourceTypes.ExternalSD.equals(AppData.getSourceType(getApplicationContext()))) {
            Preference pref = new ExtSdPrefs(this).getFolderPicker();
            if (preferenceCategory != null && pref != null) {
                preferenceCategory.addPreference(pref);
            }
        }

    }

    private void setIncludeSubdirsSwitchPref() {
        MySwitchPref myRecCheckbox = new MySwitchPref(this);
        myRecCheckbox.setSummaryOff(R.string.sett_recursiveSearchSummOff);
        myRecCheckbox.setSummaryOn(R.string.sett_recursiveSearchSummOn);
        myRecCheckbox.setTitle(R.string.sett_recursiveSearch);
        myRecCheckbox.setSummary(R.string.sett_recursiveSearchSumm);
        myRecCheckbox.setDefaultValue(true);
        myRecCheckbox.setKey(getString(R.string.sett_key_recursiveSearch));
        if (preferenceCategory != null) {
            preferenceCategory.addPreference(myRecCheckbox);
        }
    }

    public void setResetToDefaultButton() {
        Preference myResetButton = new Preference(this);
        myResetButton.setTitle(R.string.sett_resetTitle);
        myResetButton.setSummary(R.string.sett_resetSummary);
        myResetButton.setKey(getString(R.string.sett_key_restoreDefaults));
        myResetButton.setOnPreferenceClickListener(preference -> {
            AlertDialog.Builder ensureDialogB = new AlertDialog.Builder(SettingsActivity.this);
            ensureDialogB
                    .setCancelable(false)
                    .setMessage(R.string.sett_confirmationDialogMessage)
                    .setNegativeButton(R.string.sett_no, null)
                    .setPositiveButton(R.string.sett_yes, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            debug("in click on yes!");
                            resetSettingsToDefault();
                            Toast.makeText(SettingsActivity.this, R.string.sett_toast_reset, Toast.LENGTH_SHORT).show();
                            updateAllFieldTitles();
                        }
                    });
            ensureDialogB.show();
            return true;
        });
        if (preferenceCategory != null) {
            preferenceCategory.addPreference(myResetButton);
        }
    }

    public void resetSettingsToDefault() {
        AppData.resetSettings(getApplicationContext());
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
    }

    @Override
    public boolean onPreferenceTreeClick(PreferenceScreen preferenceScreen, Preference preference) {
        super.onPreferenceTreeClick(preferenceScreen, preference);
        // If the user has clicked on a preference screen, set up the screen
        if (preference instanceof PreferenceScreen) {
            setUpNestedScreen((PreferenceScreen) preference);
        }
        updateAllFieldTitles();
        return false;
    }

    /************************************************************************************
     *   needed because else the nested preference screen don't have a actionbar/toolbar *
     *   see the fix and the given problem here: http://stackoverflow.com/a/27455363     *
     ************************************************************************************/
    public void setUpNestedScreen(PreferenceScreen preferenceScreen) {
        final Dialog dialog = preferenceScreen.getDialog();
        if (dialog == null)
            return;
        //ViewGroup list;
        Toolbar bar = null;
        //list = (ViewGroup) dialog.findViewById(android.R.id.list);
        View tmp = dialog.findViewById(android.R.id.list);
        LinearLayout root = null;
        if (tmp instanceof LinearLayout) {
            LinearLayout listView = (LinearLayout) tmp;
            tmp = (View) listView.getParent();
            if (tmp instanceof LinearLayout)
                root = (LinearLayout) tmp;
        }
        if (root != null) {
            bar = (Toolbar) LayoutInflater.from(this).inflate(R.layout.settings_toolbar, root, false);
            root.addView(bar, 0); // insert at top
        }
        if (bar == null)
            return;
        bar.setTitle(preferenceScreen.getTitle());
    }

    private void debug(String msg) {
        if (DEBUG) {
            Log.d(TAG, msg);
        }
    }
}