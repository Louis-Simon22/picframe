package picframe.at.picframe.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import picframe.at.picframe.R;
import picframe.at.picframe.helper.GlobalPhoneFuncs;
import picframe.at.picframe.settings.AppData;

public class StatusActivity extends AppCompatActivity {

    private TextView nbFiles;
    private TextView currentFolder;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_status);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        nbFiles = findViewById(R.id.status_nbFiles);
        currentFolder = findViewById(R.id.status_currentFolder);
        Button aboutButton = findViewById(R.id.status_buttonAbout);
        aboutButton.setOnClickListener(new Button.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getApplicationContext(), AboutActivity.class);
                startActivity(intent);
            }
        });


    }

    protected void onResume() {
        super.onResume();
        setLocalFolderAndFileCount();
    }

    private void setLocalFolderAndFileCount() {
        String localFolder = AppData.getImagePath(getApplicationContext());
        String localFileCount;
        if (localFolder.isEmpty()) {
            localFolder = "No path set";
            localFileCount = "-";
        } else {
            localFileCount = String.valueOf(GlobalPhoneFuncs.getFileList(getApplicationContext(), localFolder).size());
        }
        currentFolder.setText(localFolder);
        nbFiles.setText(localFileCount);
    }

}
