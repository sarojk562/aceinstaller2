package com.sk.aceinstaller2;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.content.FileProvider;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.sk.aceinstaller2.util.Utils;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import lib.folderpicker.FolderPicker;

public class InstallerActivity extends Activity {

    private File root;
    private Context mContext;
    private ArrayList<File> fileList = new ArrayList<File>();
    private LinearLayout view;
    private RelativeLayout select_all;
    private Button chooseFolder;

    private RecyclerView mRecyclerView;
    private RecyclerView.Adapter mAdapter;
    private RecyclerView.LayoutManager mLayoutManager;
    private List<ApkFile> apkList;
    private Button installSelected;
    public CheckBox selectAll;

    private SharedPreferences appSettings;
    private int FOLDERPICKER_CODE = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_installer);
        mContext = this;

        if (!Utils.isTablet(this)) {
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        } else {
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE);
        }

        view = (LinearLayout) findViewById(R.id.view);
        select_all = (RelativeLayout) findViewById(R.id.select_all);
        chooseFolder = (Button) findViewById(R.id.chooseButton);
        installSelected = (Button) findViewById(R.id.installSelected);
        selectAll = (CheckBox) findViewById(R.id.chkSelected);

        selectAll.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                if(selectAll.isChecked())
                {
                    ((ApkViewDataAdapter) mAdapter).selectAll();
                }
                if(!selectAll.isChecked())
                {
                    ((ApkViewDataAdapter) mAdapter).deselectAll();
                }
            }
        });

        apkList = new ArrayList<ApkFile>();

        this.showApkList();

        appSettings = getSharedPreferences("ACE_INSTALLER", MODE_PRIVATE);
    }

    private void showApkList() {

        root = new File(Utils.getStoragePath().getAbsolutePath().concat("/AceInstaller"));
        fileList = Utils.getApkFiles(root);

        if(fileList.size() < 1) {
            Toast.makeText(mContext, "Couldn't find any apk's in default folder, Please choose a different Folder", Toast.LENGTH_SHORT).show();

            TextView textView = new TextView(InstallerActivity.this);
            textView.setText("No Apk's Found in this Location");
            textView.setPadding(5, 5, 5, 5);
            view.addView(textView);

            chooseFolder.setVisibility(View.VISIBLE);
            chooseFolder.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    openDialog(v);
                }
            });
            select_all.setVisibility(View.GONE);
        } else {
            for (int i = 0; i < fileList.size(); i++) {
                ApkFile apk = new ApkFile(fileList.get(i).getName(), false);

                String filePath = root.toString() + "/" + fileList.get(i).getName();
                PackageInfo packageInfo = mContext.getPackageManager().getPackageArchiveInfo(filePath, PackageManager.GET_ACTIVITIES);
                if(packageInfo != null) {
                    ApplicationInfo appInfo = packageInfo.applicationInfo;
                    if (Build.VERSION.SDK_INT >= 8) {
                        appInfo.sourceDir = filePath;
                        appInfo.publicSourceDir = filePath;
                    }
                    Drawable icon = appInfo.loadIcon(mContext.getPackageManager());
                    apk.setIcon(icon);
                    if(appInstalledOrNot(appInfo.packageName)) {
                        apk.setInstalled(true);
                    } else {
                        apk.setInstalled(false);
                    }
                }

                apkList.add(apk);
            }

            mRecyclerView = (RecyclerView) findViewById(R.id.apk_recycler);
            mRecyclerView.setHasFixedSize(true);
            mRecyclerView.setLayoutManager(new LinearLayoutManager(this));
            mAdapter = new ApkViewDataAdapter(apkList);
            mRecyclerView.setAdapter(mAdapter);
            installSelected.setVisibility(View.VISIBLE);
        }
    }

    private boolean appInstalledOrNot(String uri) {
        PackageManager pm = getPackageManager();
        try {
            pm.getPackageInfo(uri, PackageManager.GET_ACTIVITIES);
            return true;
        } catch (PackageManager.NameNotFoundException e) {
        }

        return false;
    }

    private void openDialog(View view) {
        Intent intent = new Intent(this, FolderPicker.class);
        startActivityForResult(intent, FOLDERPICKER_CODE);
    }

    protected void onActivityResult(int requestCode, int resultCode, Intent intent) {
        if (requestCode == FOLDERPICKER_CODE && resultCode == Activity.RESULT_OK) {
            String folderLocation = intent.getExtras().getString("data");
            showNewApkList(folderLocation);
        }
    }

    private void showNewApkList(String dir) {
        root = new File(dir);
        fileList = Utils.getApkFiles(root);

        if (fileList.size() > 0) {
            select_all.setVisibility(View.VISIBLE);
            view.removeAllViews();
            chooseFolder.setVisibility(View.GONE);
        }

        for (int i = 0; i < fileList.size(); i++) {
            ApkFile apk = new ApkFile(fileList.get(i).getName(), false);

            String filePath = root.toString() + "/" + fileList.get(i).getName();
            PackageInfo packageInfo = mContext.getPackageManager().getPackageArchiveInfo(filePath, PackageManager.GET_ACTIVITIES);
            if(packageInfo != null) {
                ApplicationInfo appInfo = packageInfo.applicationInfo;
                if (Build.VERSION.SDK_INT >= 8) {
                    appInfo.sourceDir = filePath;
                    appInfo.publicSourceDir = filePath;
                }
                Drawable icon = appInfo.loadIcon(mContext.getPackageManager());
                apk.setIcon(icon);
                if(appInstalledOrNot(appInfo.packageName)) {
                    apk.setInstalled(true);
                } else {
                    apk.setInstalled(false);
                }
            }

            apkList.add(apk);
        }

        mRecyclerView = (RecyclerView) findViewById(R.id.apk_recycler);
        mRecyclerView.setHasFixedSize(true);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        mAdapter = new ApkViewDataAdapter(apkList);
        mRecyclerView.setAdapter(mAdapter);
        installSelected.setVisibility(View.VISIBLE);
    }

    public void installSelected(View view)
    {
        installSelected.setEnabled(false);

        List<ApkFile> apkList = ((ApkViewDataAdapter) mAdapter).getSelectedApkList();

        for (int i = 0; i < apkList.size(); i++) {
            File directory = Environment.getExternalStoragePublicDirectory("AceInstaller");
            File file = new File(directory, apkList.get(i).getName());
            Uri fileUri = Uri.fromFile(file);

            if (Build.VERSION.SDK_INT >= 24) {
                fileUri = FileProvider.getUriForFile(this, BuildConfig.APPLICATION_ID + ".provider", file);
            }

            Intent intent = new Intent(Intent.ACTION_INSTALL_PACKAGE);
            intent.putExtra(Intent.EXTRA_NOT_UNKNOWN_SOURCE, true);
            intent.setDataAndType(fileUri, "application/vnd.android.package-archive");
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION); //dont forget add this line
            startActivity(intent);
        }

        // Make sure you only run addShortcut() once, not to create duplicate shortcuts.
        if(!appSettings.getBoolean("apps_shortcut", false)) {
            addAppsShortcut();
        }
    }

    private void addAppsShortcut() {
        //Adding shortcut for App List Activity
        //on Home screen
        Intent shortcutIntent = new Intent(getApplicationContext(), AppListActivity.class);
        shortcutIntent.setAction(Intent.ACTION_MAIN);

        Intent addIntent = new Intent();
        addIntent.putExtra(Intent.EXTRA_SHORTCUT_INTENT, shortcutIntent);
        addIntent.putExtra(Intent.EXTRA_SHORTCUT_NAME, "Education Apps");
        addIntent.putExtra(Intent.EXTRA_SHORTCUT_ICON_RESOURCE, Intent.ShortcutIconResource.fromContext(getApplicationContext(), R.mipmap.ic_shortcut));

        addIntent.setAction("com.android.launcher.action.INSTALL_SHORTCUT");
        addIntent.putExtra("duplicate", false);  //may it's already there so   don't duplicate
        getApplicationContext().sendBroadcast(addIntent);

        SharedPreferences.Editor prefEditor = appSettings.edit();
        prefEditor.putBoolean("apps_shortcut", true);
        prefEditor.commit();
    }
}