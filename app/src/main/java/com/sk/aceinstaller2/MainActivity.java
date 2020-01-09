package com.sk.aceinstaller2;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import com.sk.aceinstaller2.util.Utils;

public class MainActivity extends AppCompatActivity {

    private static Button installerButton;
    private static Button contentCopyButton;
    private static Button appListButton;
    private Context mContext;

    private int STORAGE_PERMISSION_CODE=23;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mContext = this;

        if (!Utils.isTablet(this)) {
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        } else {
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE);
        }

        ActivityCompat.requestPermissions(this, new String[] {Manifest.permission.READ_EXTERNAL_STORAGE} , STORAGE_PERMISSION_CODE);

        installerButton = (Button) findViewById(R.id.installerView);
        installerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openInstaller(v);
            }
        });

        appListButton = (Button) findViewById(R.id.appListView);
        appListButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openAppList(v);
            }
        });

        contentCopyButton = (Button) findViewById(R.id.contentDataView);
        contentCopyButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openCopier(v);
            }
        });
    }

    public void openInstaller(View view)
    {
        Intent intent = new Intent(getApplicationContext(), InstallerActivity.class);
        startActivity(intent);
    }

    public void openAppList(View view) {
        Intent intent = new Intent(getApplicationContext(), AppListActivity.class);
        startActivity(intent);
    }

    public void openCopier(View view)
    {
        Intent intent = new Intent(getApplicationContext(), CopyActivity.class);
        startActivity(intent);
    }
}
