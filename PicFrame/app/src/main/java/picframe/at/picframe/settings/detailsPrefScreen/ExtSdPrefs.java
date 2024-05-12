package picframe.at.picframe.settings.detailsPrefScreen;

import android.content.Context;
import android.preference.Preference;
import android.view.ViewGroup;

import java.util.ArrayList;

import picframe.at.picframe.R;
import picframe.at.picframe.settings.AppData;
import picframe.at.picframe.settings.SimpleFileDialog;

public class ExtSdPrefs implements IDetailsPreferenceScreen {
    private final ArrayList<Preference> allPrefs = new ArrayList<>();
    private final Context context;
    private Preference folderPicker;

    public ExtSdPrefs(Context context) {
        this.context = context;

        //createStatusView();
        createSourcePref();
    }

    private void createSourcePref() {
        folderPicker = new Preference(context);
        folderPicker.setTitle(context.getString(R.string.sett_srcPath_externalSD));
        folderPicker.setSummary(R.string.sett_srcPath_externalSDSumm);
        folderPicker.setDefaultValue("");
        folderPicker.setKey(context.getString(R.string.sett_key_srcpath_sd));
        folderPicker.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            String _chosenDir = AppData.getSourcePath(context);

            @Override
            public boolean onPreferenceClick(Preference preference) {
                SimpleFileDialog FolderChooseDialog =
                        new SimpleFileDialog(
                                context,
                                new SimpleFileDialog.SimpleFileDialogListener() {
                                    @Override
                                    // The code in this function will be executed when the dialog OK button is pushed
                                    public void onChosenDir(String chosenDir) {
                                        _chosenDir = chosenDir;
                                        AppData.setSdSourcePath(context, _chosenDir);
                                    }
                                });
                FolderChooseDialog.chooseFile_or_Dir(_chosenDir);
                return true;
            }
        });
        allPrefs.add(folderPicker);
    }

    public Preference getFolderPicker() {
        return folderPicker;
    }


    @Override
    public ArrayList<Preference> getAllDetailPreferenceFields() {
        return allPrefs;
    }

    @Override
    public ViewGroup getStatusViewGroup() {
        return null;
    }
}
