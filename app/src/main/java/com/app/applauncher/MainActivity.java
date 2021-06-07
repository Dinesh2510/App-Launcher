package com.app.applauncher;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.core.app.ActivityCompat;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.Manifest;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.util.Pair;
import android.widget.Adapter;
import android.widget.EditText;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    ArrayList<AppModel> installedAppList = new ArrayList<>();
    RecyclerView recycleView;
    AppAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        if (isStoragePermissionGranted()) {


        }
        adapter = new AppAdapter();
        EditText editText_s = findViewById(R.id.editText_s);

        // below line is to call set on query text listener method.
        Log.d("CHECK", "onCreate: " + installedAppList.size());
        recycleView = findViewById(R.id.recycleView);
       // recycleView.setLayoutManager((new GridLayoutManager(getApplicationContext(), 4)));
        recycleView.setLayoutManager(new LinearLayoutManager(getApplicationContext(), LinearLayoutManager.VERTICAL, false));
        Log.d("CHECK", "onCreate: " + installedAppList.size());

        getInstalledAppList();
        editText_s.addTextChangedListener(new TextWatcher() {
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }

            public void afterTextChanged(Editable editable) {
                Log.d("check", "afterTextChanged: "+editable);
                filterQuery(editable.toString());
            }
        });
    }


    private void getInstalledAppList() {

        Intent intent = new Intent(Intent.ACTION_MAIN, null);
        intent.addCategory(Intent.CATEGORY_LAUNCHER);
        List<ResolveInfo> untreatedAppList = getApplicationContext().getPackageManager().queryIntentActivities(intent, 0);

        for (ResolveInfo untreatedApp : untreatedAppList) {
            AppModel app_data = new AppModel();
            PackageInfo pInfo = null;
            String version ="";
            app_data.name = untreatedApp.activityInfo.loadLabel(getPackageManager()).toString();
            app_data.packageName = untreatedApp.activityInfo.packageName;
            app_data.image = untreatedApp.activityInfo.loadIcon(getPackageManager());
            String app_c_data =untreatedApp.activityInfo.packageName;
            try {
                 pInfo = getApplicationContext().getPackageManager().getPackageInfo(app_c_data, 0);
                 version = pInfo.versionName;
                app_data.v_code = String.valueOf(pInfo.versionCode);
                app_data.v_name = version;
            } catch (PackageManager.NameNotFoundException e) {
                e.printStackTrace();
            }
            Intent launchIntent = getPackageManager().getLaunchIntentForPackage(app_c_data);
            app_data.v_class =  launchIntent.getComponent().getClassName();

            installedAppList.add(app_data);
            Log.d("CHECK", "onCreate: " + installedAppList.size());

        }
        adapter =(new AppAdapter(installedAppList, MainActivity.this));
        recycleView.setAdapter(adapter);

    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    public boolean check_permissions() {

        String[] PERMISSIONS = {
                Manifest.permission.READ_EXTERNAL_STORAGE,
                Manifest.permission.WRITE_EXTERNAL_STORAGE,
                Manifest.permission.CAMERA,
                Manifest.permission.RECORD_AUDIO
        };

        if (!hasPermissions(this, PERMISSIONS)) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                requestPermissions(PERMISSIONS, 2);
            }
        } else {

            return true;
        }

        return false;
    }

    public static boolean hasPermissions(Context context, String... permissions) {
        if (context != null && permissions != null) {
            for (String permission : permissions) {
                if (ActivityCompat.checkSelfPermission(context, permission) != PackageManager.PERMISSION_GRANTED) {
                    return false;
                }
            }
        }
        return true;
    }

    public boolean isStoragePermissionGranted() {
        check_permissions();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (checkSelfPermission(android.Manifest.permission.WRITE_EXTERNAL_STORAGE)
                    == PackageManager.PERMISSION_GRANTED) {
                Log.v("TAG", "Permission is granted");
                return true;
            } else {

                Log.v("TAG", "Permission is revoked");
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1);
                return false;
            }
        } else { //permission is automatically granted on sdk<23 upon installation
            Log.v("TAG", "Permission is granted");
            return true;
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            Log.v("TAG", "Permission: " + permissions[0] + "was " + grantResults[0]);
            //resume tasks needing this permission


        }
    }

    public void normalClick(AppModel appModel, int position) {
        String packageName = appModel.getPackageName();
        try {
            Intent launchIntentForPackage = getPackageManager().getLaunchIntentForPackage(packageName)
                    .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(launchIntentForPackage);
        } catch (Exception e) {
            Toast.makeText(
                    MainActivity.this,
                    String.format("Couldn't launch %s", packageName),
                    Toast.LENGTH_LONG
            ).show();
        }
    }

    public void normalClickLong(AppModel appModel, int position) {
        try {
            Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
            intent.setData(Uri.parse("package:" + appModel.getPackageName()));
            startActivity(intent);
        } catch (ActivityNotFoundException e) {
            getInstalledAppList();
        }
    }

    public void filterQuery(String text) {
        ArrayList<AppModel> filterdNames = new ArrayList<>();
        for (AppModel s : this.installedAppList) {
            if (s.name.toLowerCase().contains(text) || s.name.toUpperCase().contains(text)) {
                filterdNames.add(s);
            }
        }
        this.adapter.setFilter(filterdNames);
    }

}