package com.app.applauncher;

import android.content.Intent;
import android.graphics.drawable.Drawable;

public class AppModel {


    public String name,
            packageName,v_name,v_code,v_class;
    public Drawable image;
    public Boolean isAppInDrawer;
    public Intent launchactivity;

/*
    public AppModel(String packageName, String name, Drawable image, Boolean isAppInDrawer) {
        this.name = name;
        this.packageName = packageName;
        this.image = image;
        this.isAppInDrawer = isAppInDrawer;
    }
*/

    public String getPackageName() {
        return packageName;
    }

    public String getName() {
        return name;
    }

    public Drawable getImage() {
        return image;
    }

    public Boolean getIsAppInDrawer() {
        return isAppInDrawer;
    }

    public void setPackageName(String packageName) {
        this.packageName = packageName;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setImage(Drawable image) {
        this.image = image;
    }

    public void setIsAppInDrawer(Boolean image) {
        this.isAppInDrawer = isAppInDrawer;
    }
}