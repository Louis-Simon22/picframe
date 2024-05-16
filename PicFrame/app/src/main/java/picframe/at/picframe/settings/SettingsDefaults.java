package picframe.at.picframe.settings;

import android.content.Context;
import android.content.SharedPreferences;

import java.util.HashMap;
import java.util.Map;

import picframe.at.picframe.R;

public class SettingsDefaults {

    private static final Map<Integer, Object> defValues = new HashMap<>();

    static {
        defValues.put(R.string.sett_key_scaling, false);
        defValues.put(R.string.sett_key_randomize, true);
        defValues.put(R.string.sett_key_displaytime, "4");
        defValues.put(R.string.sett_key_srcpath_sd, "");
        defValues.put(R.string.sett_key_recursiveSearch, true);
        defValues.put(R.string.sett_key_transition, "10");
    }
    /*  <string name="sett_key_firstStart" translatable="false">FirstStart</string>     */

    public static Object getDefaultValueForKey(int key) {
        return defValues.get(key);
    }

    public static void resetSettings(Context context) {
        SharedPreferences.Editor prefEditor = AppData.getSharedPreferences(context).edit();
        for (Map.Entry<Integer, Object> prefSet : defValues.entrySet()) {
            if (prefSet.getValue() instanceof String) {
                prefEditor.putString(context.getString(prefSet.getKey()), (String) prefSet.getValue()).apply();
            } else if (prefSet.getValue() instanceof Boolean) {
                prefEditor.putBoolean(context.getString(prefSet.getKey()), (Boolean) prefSet.getValue()).apply();
            }
        }
    }
}
