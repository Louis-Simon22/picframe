/*
    Copyright (C) 2015 Myra Fuchs, Linda Spindler, Clemens Hlawacek, Ebenezer Bonney Ussher

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

package picframe.at.picframe.settings;

import android.content.Context;
import android.content.SharedPreferences;

import java.io.File;

import picframe.at.picframe.R;
import picframe.at.picframe.helper.local_storage.SD_Card_Helper;

/**
 * Stores App Settings, to get and load easily
 * Created by ClemensH on 04.04.2015.
 */
public class AppData {
    public static final String mySettingsFilename = "PicFrameSettings";

    public static void resetSettings(Context context) {
        SettingsDefaults.resetSettings(context);
    }

    // enums for available source types (like images from SD-Card, OwnCloud or Dropbox)
    public enum sourceTypes {
        ExternalSD, OwnCloud, Dropbox;
        private static final sourceTypes[] allValues = values();

        public static sourceTypes getSourceTypesForInt(int num) {
            try {
                return allValues[num];
            } catch (ArrayIndexOutOfBoundsException e) {
                return ExternalSD;
            }
        }
    }

    /* TODO
    extFolderAppRoot = new SD_Card_Helper().getExteralStoragePath() +
                File.separator + "picframe";
        extFolderDisplayPath = extFolderAppRoot + File.separator + "pictures";
        extFolderCachePath = extFolderAppRoot + File.separator + "cache";
    *
    * */

    // ONLY TO BE MODIFIED BY SETTINGS ACTIVITY
    // flag whether slideshow is selected(on=true) or not(off=false)
    public static boolean getSlideshow(Context context) {
        return getSharedPreferences(context).getBoolean(context.getString(R.string.sett_key_slideshow),
                (Boolean) SettingsDefaults.getDefaultValueForKey(R.string.sett_key_slideshow));
    }

    // holds the time to display each picture in seconds
    public static int getDisplayTime(Context context) {
        return Integer.parseInt(getSharedPreferences(context).getString(context.getString(R.string.sett_key_displaytime),
                (String) SettingsDefaults.getDefaultValueForKey(R.string.sett_key_displaytime)));
    }

    // holds the int of the transitionStyle - @res.values.arrays.transitionTypeValues
    public static int getTransitionStyle(Context context) {
        return Integer.parseInt(
                getSharedPreferences(context).getString(context.getString(R.string.sett_key_transition),
                        (String) SettingsDefaults.getDefaultValueForKey(R.string.sett_key_transition)));
    }

    // flag whether to randomize the order of the displayed images (on=true)
    public static boolean getRandomize(Context context) {
        return getSharedPreferences(context).getBoolean(context.getString(R.string.sett_key_randomize),
                (Boolean) SettingsDefaults.getDefaultValueForKey(R.string.sett_key_randomize));
    }

    // flag whether to scale the displayed image (on=true)
    public static boolean getScaling(Context context) {
        return getSharedPreferences(context).getBoolean(context.getString(R.string.sett_key_scaling),
                (Boolean) SettingsDefaults.getDefaultValueForKey(R.string.sett_key_scaling));
    }

    // holds the type of the selected source as int
    public static int getSrcTypeInt(Context context) {
        return Integer.parseInt(
                getSharedPreferences(context).getString(context.getString(R.string.sett_key_srctype),
                        (String) SettingsDefaults.getDefaultValueForKey(R.string.sett_key_srctype)));
    }

    // holds the type of the selected source
    public static sourceTypes getSourceType(Context context) {
        return sourceTypes.getSourceTypesForInt(Integer.parseInt(
                getSharedPreferences(context).getString(context.getString(R.string.sett_key_srctype),
                        (String) SettingsDefaults.getDefaultValueForKey(R.string.sett_key_srctype))));
    }

    // holds the username to log into the owncloud account
    public static String getUserName(Context context) {
        return getSharedPreferences(context).getString(context.getString(R.string.sett_key_username),
                (String) SettingsDefaults.getDefaultValueForKey(R.string.sett_key_username));
    }

    // holds the password to log into the owncloud account
    public static String getUserPassword(Context context) {
        return getSharedPreferences(context).getString(context.getString(R.string.sett_key_password),
                (String) SettingsDefaults.getDefaultValueForKey(R.string.sett_key_password));
    }

    // Returns selected SD-Card directory, or URL to owncloud or samba server
    // holds the path to the image source (from where to (down)-load them
    public static String getSourcePath(Context context) {
        sourceTypes tmpType = getSourceType(context);
        if (sourceTypes.OwnCloud.equals(tmpType)) {
            return getSharedPreferences(context).getString(context.getString(R.string.sett_key_srcpath_dropbox),
                    (String) SettingsDefaults.getDefaultValueForKey(R.string.sett_key_srcpath_dropbox));
        } else if (sourceTypes.Dropbox.equals(tmpType)) {
            return getSharedPreferences(context).getString(context.getString(R.string.sett_key_srcpath_dropbox), "");
        } else {    // if SD or undefined, get SD path
            return getSharedPreferences(context).getString(context.getString(R.string.sett_key_srcpath_sd),
                    (String) SettingsDefaults.getDefaultValueForKey(R.string.sett_key_srcpath_sd));
        }
    }

    // holds the time-interval to initiate the next download of images in hours
    public static int getUpdateIntervalInHours(Context context) {
        return Integer.parseInt(
                getSharedPreferences(context).getString(context.getString(R.string.sett_key_downloadInterval),
                        (String) SettingsDefaults.getDefaultValueForKey(R.string.sett_key_downloadInterval)));
    }


    // flag whether to include images in subfolders (on=true)
    public static boolean getRecursiveSearch(Context context) {
        return getSharedPreferences(context).getBoolean(context.getString(R.string.sett_key_recursiveSearch),
                (Boolean) SettingsDefaults.getDefaultValueForKey(R.string.sett_key_recursiveSearch));
    }

    // Always returns the path to the img folder of current src type
    // holds the root-path to the displayed images
    public static String getImagePath(Context context) {
        sourceTypes tmpType = getSourceType(context);
        if (sourceTypes.ExternalSD.equals(tmpType)) {
            return getSharedPreferences(context).getString(context.getString(R.string.sett_key_srcpath_sd),
                    (String) SettingsDefaults.getDefaultValueForKey(R.string.sett_key_srcpath_sd));
        } else {
            return getExtFolderDisplayPath();
        }
    }

    // get flag whether the last loginCheck for a server was successful or not
    public static boolean getLoginSuccessful(Context context) {
        return getSharedPreferences(context).getBoolean(context.getString(R.string.sett_key_recursiveSearch), false);
    }

    // set flag whether the last loginCheck for a server is successful or not
    public static void setLoginSuccessful(Context context, boolean loginSuccess) {
        getSharedPreferences(context).edit().putBoolean(context.getString(R.string.sett_key_loginCheckButton), loginSuccess).commit();
    }

    public static void setSdSourcePath(Context context, String path) {
        getSharedPreferences(context).edit().putString(context.getString(R.string.sett_key_srcpath_sd), path).commit();
    }

    // flag whether this is the first app start
    public static boolean getFirstAppStart(Context context) {
        return getSharedPreferences(context).getBoolean(context.getString(R.string.sett_key_firstStart), false);
    }

    public static void setFirstAppStart(Context context, boolean firstAppStart) {
        getSharedPreferences(context).edit().putBoolean(context.getString(R.string.sett_key_loginCheckButton), firstAppStart).commit();
    }

    // flag whether to display the tutorial-dialog (on=true)
    public static boolean getTutorial(Context context) {
        return getSharedPreferences(context).getBoolean(context.getString(R.string.sett_key_tutorial), false);
    }

    public static void setTutorial(Context context, boolean showTutorial) {
        getSharedPreferences(context).edit().putBoolean(context.getString(R.string.sett_key_loginCheckButton), showTutorial).commit();
    }

    public static int getCurrentPage(Context context) {
        return getSharedPreferences(context).getInt(context.getString(R.string.sett_key_currentPage), 1);
    }

    public static void setCurrentPage(Context context, int page) {
        getSharedPreferences(context).edit().putInt(context.getString(R.string.sett_key_currentPage), page).commit();
    }

    // flag for slideshow direction
    public static boolean getDirection(Context context) {
        return getSharedPreferences(context).getBoolean(context.getString(R.string.sett_key_direction), false);
    }

    public static void setDirection(Context context, boolean toggleDirection) {
        getSharedPreferences(context).edit().putBoolean(context.getString(R.string.sett_key_loginCheckButton), toggleDirection).commit();
    }

    // last alarm time in milliseconds
    public static Long getLastAlarmTime(Context context) {
        return getSharedPreferences(context).getLong(context.getString(R.string.sett_key_lastAlarmTime), -1);
    }

    public static void setLastAlarmTime(Context context, Long time) {
        getSharedPreferences(context).edit().putLong(context.getString(R.string.sett_key_lastAlarmTime), time).commit();
    }

    public static Long getNextAlarmTime(Context context) {
        return getSharedPreferences(context).getLong(context.getString(R.string.sett_key_nextAlarmTime), -1);
    }

    public static void setNextAlarmTime(Context context, Long time) {
        getSharedPreferences(context).edit().putLong(context.getString(R.string.sett_key_nextAlarmTime), time).commit();
    }

    // CAN NEVER BE MODIFIED!   (holds local paths, desc at vars)
    // sd-card-dir/picframe
    public static String getExtFolderAppRoot() {
        return new SD_Card_Helper().getExteralStoragePath() + File.separator + "picframe";
    }

    // sd-card-dir/picframe/cache
    public static String getExtFolderCachePath() {
        return (getExtFolderAppRoot() + File.separator + "cache");
    }

    // sd-card-dir/picframe/pictures
    public static String getExtFolderDisplayPath() {
        return (getExtFolderAppRoot() + File.separator + "pictures");
    }

    public static SharedPreferences getSharedPreferences(Context context) {
        return context.getSharedPreferences(mySettingsFilename, Context.MODE_PRIVATE);
    }
}