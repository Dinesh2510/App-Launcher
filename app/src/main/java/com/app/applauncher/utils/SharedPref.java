package com.app.applauncher.utils;

import android.content.Context;
import android.content.SharedPreferences;


public class SharedPref {

    private Context context;
    private SharedPreferences sharedPreferences;
    private SharedPreferences.Editor editor;

    public SharedPref(Context context) {
        this.context = context;
        sharedPreferences = context.getSharedPreferences("settings", Context.MODE_PRIVATE);
        editor = sharedPreferences.edit();
    }

    public void saveGif(String path, String gif_name) {
        editor.putString("path", path);
        editor.putString("gif_name", gif_name);
        editor.apply();
    }

    public String getPath() {
        return sharedPreferences.getString("path", "0");
    }

    public String getGifName() {
        return sharedPreferences.getString("gif_name", "0");
    }

    public Integer getDisplayPosition(int default_value) {
        return sharedPreferences.getInt("display_position", default_value);
    }

    public void updateDisplayPosition(int position) {
        editor.putInt("display_position", position);
        editor.apply();
    }

    public Integer getWallpaperColumns() {
        return sharedPreferences.getInt("wallpaper_columns", Config.DEFAULT_WALLPAPER_COLUMN);
    }

    public void updateWallpaperColumns(int columns) {
        editor.putInt("wallpaper_columns", columns);
        editor.apply();
    }

    public void setDefaultSortWallpaper() {
        editor.putInt("sort_act", 0);
        editor.apply();
    }


    public Boolean getIsDarkTheme() {
        return sharedPreferences.getBoolean("theme", false);
    }

    public void setIsDarkTheme(Boolean isDarkTheme) {
        editor.putBoolean("theme", isDarkTheme);
        editor.apply();
    }



}
