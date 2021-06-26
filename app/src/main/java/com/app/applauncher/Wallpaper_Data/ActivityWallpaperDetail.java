package com.app.applauncher.Wallpaper_Data;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;

import android.Manifest;
import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.WallpaperManager;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.widget.Toolbar;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.viewpager.widget.PagerAdapter;
import androidx.viewpager.widget.ViewPager;

import com.app.applauncher.R;
import com.app.applauncher.utils.Config;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.CustomTarget;
import com.bumptech.glide.request.target.Target;
import com.bumptech.glide.request.transition.Transition;
import com.github.chrisbanes.photoview.PhotoView;
import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.snackbar.Snackbar;

import com.app.applauncher.utils.Constant;
import com.app.applauncher.utils.DBHelper;
import com.app.applauncher.utils.HackyViewPager;
import com.app.applauncher.utils.SharedPref;
import com.app.applauncher.utils.Tools;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;


import static com.app.applauncher.utils.Constant.BASE_IMAGE_URL;


public class ActivityWallpaperDetail extends AppCompatActivity {

    HackyViewPager viewPager;
    ImagePagerAdapter pagerAdapter;
    Wallpaper wallpaper;
    int position;
    List<Wallpaper> items = new ArrayList<>();
    Toolbar toolbar;
    ActionBar actionBar;
    private String single_choice_selected;
    CoordinatorLayout parent_view;
    private BottomSheetDialog mBottomSheetDialog;
    SharedPref sharedPref;
    DBHelper dbHelper;


    boolean flag = true;

    @SuppressWarnings("unchecked")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Tools.getTheme(this);
        sharedPref = new SharedPref(this);

        Tools.transparentStatusBar(this);
        if (sharedPref.getIsDarkTheme()) {
            Tools.darkNavigation(this);
        }
        Tools.transparentStatusBarNavigation(this);

        setContentView(R.layout.activity_wallpaper_detail);

        Tools.getRtlDirection(this);
        parent_view = findViewById(R.id.coordinatorLayout);

        dbHelper = new DBHelper(this);

        position = getIntent().getIntExtra(Constant.POSITION, 0);
        wallpaper = (Wallpaper) getIntent().getSerializableExtra(Constant.EXTRA_OBJC);

        Intent intent = getIntent();
        Bundle bundle = intent.getBundleExtra(Constant.BUNDLE);
        items = (List<Wallpaper>) bundle.getSerializable(Constant.ARRAY_LIST);

        setupToolbar();
        loadView(position);
        setupViewPager();


    }

    public void setupViewPager() {
        pagerAdapter = new ImagePagerAdapter();
        viewPager = findViewById(R.id.view_pager);
        viewPager.setAdapter(pagerAdapter);
        viewPager.setCurrentItem(position);
        viewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {
                loadView(position);
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });
    }

    public void loadView(int position) {
        Wallpaper wallpaper = items.get(position);

        TextView title_toolbar = findViewById(R.id.title_toolbar);
        TextView sub_title_toolbar = findViewById(R.id.sub_title_toolbar);
        if (wallpaper.image_name.equals("")) {
            title_toolbar.setText(wallpaper.category_name);
            sub_title_toolbar.setVisibility(View.GONE);
        } else {
            title_toolbar.setText(wallpaper.image_name);
            sub_title_toolbar.setText(wallpaper.category_name);
            sub_title_toolbar.setVisibility(View.VISIBLE);
        }

        findViewById(R.id.btn_info).setOnClickListener(view -> showBottomSheetDialog(wallpaper));

        findViewById(R.id.btn_save).setOnClickListener(view -> {
            downloadWallpaper(BASE_IMAGE_URL + wallpaper.image_upload);

        });

        findViewById(R.id.btn_share).setOnClickListener(view -> {
            shareWallpaper(BASE_IMAGE_URL + wallpaper.image_upload);

        });

        findViewById(R.id.btn_set_wallpaper).setOnClickListener(view -> {

            if (!verifyPermissions()) {
                return;
            }

            if (wallpaper.image_upload.endsWith(".gif") || wallpaper.image_url.endsWith(".gif")) {
                if (wallpaper.type.equals("upload")) {
                    setGif(BASE_IMAGE_URL + wallpaper.image_upload);
                } else if (wallpaper.type.equals("url")) {
                    setGif(wallpaper.image_url);
                }
            } else {
                if (Build.VERSION.SDK_INT >= 24) {
                    dialogOptionSetWallpaper(BASE_IMAGE_URL + wallpaper.image_upload, wallpaper);

                } else {
                    setWallpaper(BASE_IMAGE_URL + wallpaper.image_upload);

                }
            }
        });

        favToggle(wallpaper);
        findViewById(R.id.btn_favorite).setOnClickListener(view -> {
            if (dbHelper.isFavoritesExist(wallpaper.image_id)) {
                dbHelper.deleteFavorites(wallpaper);
                Snackbar.make(parent_view, getString(R.string.snack_bar_favorite_removed), Snackbar.LENGTH_SHORT).show();
            } else {
                dbHelper.addOneFavorite(wallpaper);
                Snackbar.make(parent_view, getString(R.string.snack_bar_favorite_added), Snackbar.LENGTH_SHORT).show();
            }
            favToggle(wallpaper);
        });

        //updateView(wallpaper.image_id);

    }

    private void favToggle(Wallpaper wallpaper) {
        ImageView img_favorite = findViewById(R.id.img_favorite);
        if (dbHelper.isFavoritesExist(wallpaper.image_id)) {
            img_favorite.setImageResource(R.drawable.ic_action_fav);
        } else {
            img_favorite.setImageResource(R.drawable.ic_action_fav_outline);
        }
    }

    public void setupToolbar() {
        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        actionBar = getSupportActionBar();
        if (actionBar != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setHomeButtonEnabled(true);
        }
    }

    private void showBottomSheetDialog(Wallpaper wallpaper) {
        @SuppressLint("InflateParams") final View view = getLayoutInflater().inflate(R.layout.include_info, null);
        FrameLayout lyt_bottom_sheet = view.findViewById(R.id.bottom_sheet);

        if (sharedPref.getIsDarkTheme()) {
            lyt_bottom_sheet.setBackground(ContextCompat.getDrawable(this, R.drawable.bg_rounded_dark));
        } else {
            lyt_bottom_sheet.setBackground(ContextCompat.getDrawable(this, R.drawable.bg_rounded_default));
        }

        if (wallpaper.image_name.equals("")) {
            ((TextView) view.findViewById(R.id.txt_wallpaper_name)).setText("-");
        } else {
            ((TextView) view.findViewById(R.id.txt_wallpaper_name)).setText(wallpaper.image_name);
        }

        ((TextView) view.findViewById(R.id.txt_category_name)).setText(wallpaper.category_name);

        if (wallpaper.resolution.equals("0")) {
            ((TextView) view.findViewById(R.id.txt_resolution)).setText("-");
        } else {
            ((TextView) view.findViewById(R.id.txt_resolution)).setText(wallpaper.resolution);
        }

        if (wallpaper.size.equals("0")) {
            ((TextView) view.findViewById(R.id.txt_size)).setText("-");
        } else {
            ((TextView) view.findViewById(R.id.txt_size)).setText(wallpaper.size);
        }

        if (wallpaper.mime.equals("")) {
            ((TextView) view.findViewById(R.id.txt_mime_type)).setText("image/jpeg");
        } else {
            ((TextView) view.findViewById(R.id.txt_mime_type)).setText(wallpaper.mime);
        }

        ((TextView) view.findViewById(R.id.txt_view_count)).setText(Tools.withSuffix(wallpaper.views) + "");
        ((TextView) view.findViewById(R.id.txt_download_count)).setText(Tools.withSuffix(wallpaper.downloads) + "");



        if (sharedPref.getIsDarkTheme()) {
            mBottomSheetDialog = new BottomSheetDialog(this, R.style.SheetDialogDark);
        } else {
            mBottomSheetDialog = new BottomSheetDialog(this, R.style.SheetDialogLight);
        }
        mBottomSheetDialog.setContentView(view);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            //noinspection deprecation
            mBottomSheetDialog.getWindow().addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        }

        BottomSheetBehavior bottomSheetBehavior = mBottomSheetDialog.getBehavior();
        bottomSheetBehavior.setState(BottomSheetBehavior.STATE_EXPANDED);
        bottomSheetBehavior.addBottomSheetCallback(new BottomSheetBehavior.BottomSheetCallback() {
            @Override
            public void onStateChanged(@NonNull View bottomSheet, int newState) {
                if (newState == BottomSheetBehavior.STATE_COLLAPSED) {

                }
            }

            @Override
            public void onSlide(@NonNull View bottomSheet, float slideOffset) {

            }
        });

        mBottomSheetDialog.show();
        mBottomSheetDialog.setOnDismissListener(dialog -> {
            mBottomSheetDialog = null;
            //Tools.lightNavigation(ActivityWallpaperDetail.this);
        });
    }

    private class ImagePagerAdapter extends PagerAdapter {

        private final LayoutInflater inflater;

        ImagePagerAdapter() {
            inflater = getLayoutInflater();
        }

        @Override
        public int getCount() {
            return items.size();
        }

        @Override
        public boolean isViewFromObject(@NonNull View view, @NonNull Object object) {
            return view.equals(object);
        }

        @NonNull
        @Override
        public Object instantiateItem(@NonNull ViewGroup container, final int position) {

            View imageLayout = inflater.inflate(R.layout.item_wallpaper_slider, container, false);
            assert imageLayout != null;
            Wallpaper wallpaper = items.get(position);

            final PhotoView imageView = imageLayout.findViewById(R.id.image_view);
            if (Config.ENABLE_CENTER_CROP_IN_DETAIL_WALLPAPER) {
                imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
            }

            imageView.setOnClickListener(v -> {
                if (flag) {
                    fullScreenMode(true);
                    flag = false;
                } else {
                    fullScreenMode(false);
                    flag = true;
                }
            });

            final ProgressBar progressBar = imageLayout.findViewById(R.id.progress_bar);

            Glide.with(ActivityWallpaperDetail.this)
                    .load(BASE_IMAGE_URL + wallpaper.image_upload.replace(" ", "%20"))
                    .placeholder(R.drawable.ic_transparent)
                    .thumbnail(0.3f)
                    //.centerCrop()
                    .addListener(new RequestListener<Drawable>() {
                        @Override
                        public boolean onLoadFailed(@Nullable GlideException e, Object model, Target<Drawable> target, boolean isFirstResource) {
                            progressBar.setVisibility(View.GONE);
                            return false;
                        }

                        @Override
                        public boolean onResourceReady(Drawable resource, Object model, Target<Drawable> target, DataSource dataSource, boolean isFirstResource) {
                            progressBar.setVisibility(View.GONE);
                            return false;
                        }
                    })
                    .diskCacheStrategy(DiskCacheStrategy.ALL)
                    .into(imageView);

            container.addView(imageLayout, 0);
            return imageLayout;

        }

        @Override
        public void destroyItem(@NonNull ViewGroup container, int position, @NonNull Object object) {
            container.removeView((View) object);
        }
    }

    @TargetApi(Build.VERSION_CODES.N)
    public void dialogOptionSetWallpaper(String imageURL, Wallpaper wp) {
        String[] items = getResources().getStringArray(R.array.dialog_set_wallpaper);
        single_choice_selected = items[0];
        int itemSelected = 0;
        new AlertDialog.Builder(ActivityWallpaperDetail.this)
                .setTitle(R.string.dialog_set_title)
                .setSingleChoiceItems(items, itemSelected, (dialogInterface, i) -> single_choice_selected = items[i])
                .setPositiveButton(R.string.dialog_option_ok, (dialogInterface, i) -> {
                    Snackbar.make(parent_view, getString(R.string.snack_bar_applying), Snackbar.LENGTH_SHORT).show();
                    Glide.with(this)
                            .load(imageURL.replace(" ", "%20"))
                            .into(new CustomTarget<Drawable>() {
                                @Override
                                public void onResourceReady(@NonNull Drawable resource, @Nullable Transition<? super Drawable> transition) {
                                    Bitmap bitmap = ((BitmapDrawable) resource).getBitmap();
                                    if (single_choice_selected.equals(getResources().getString(R.string.set_home_screen))) {
                                        try {
                                            WallpaperManager wallpaperManager = WallpaperManager.getInstance(ActivityWallpaperDetail.this);
                                            wallpaperManager.setBitmap(bitmap, null, true, WallpaperManager.FLAG_SYSTEM);
                                            new Handler(Looper.getMainLooper()).postDelayed(() -> {
                                                Snackbar.make(parent_view, getString(R.string.snack_bar_applied), Snackbar.LENGTH_SHORT).show();
                                            }, Constant.DELAY_SET);
                                        } catch (IOException e) {
                                            e.printStackTrace();
                                            Snackbar.make(parent_view, getString(R.string.snack_bar_failed), Snackbar.LENGTH_SHORT).show();
                                        }
                                    } else if (single_choice_selected.equals(getResources().getString(R.string.set_lock_screen))) {
                                        try {
                                            WallpaperManager wallpaperManager = WallpaperManager.getInstance(ActivityWallpaperDetail.this);
                                            wallpaperManager.setBitmap(bitmap, null, true, WallpaperManager.FLAG_LOCK);
                                            new Handler(Looper.getMainLooper()).postDelayed(() -> {
                                                Snackbar.make(parent_view, getString(R.string.snack_bar_applied), Snackbar.LENGTH_SHORT).show();
                                            }, Constant.DELAY_SET);
                                        } catch (IOException e) {
                                            e.printStackTrace();
                                            Snackbar.make(parent_view, getString(R.string.snack_bar_failed), Snackbar.LENGTH_SHORT).show();
                                        }
                                    } else if (single_choice_selected.equals(getResources().getString(R.string.set_both))) {
                                        try {
                                            WallpaperManager wallpaperManager = WallpaperManager.getInstance(ActivityWallpaperDetail.this);
                                            wallpaperManager.setBitmap(bitmap);
                                            new Handler(Looper.getMainLooper()).postDelayed(() -> {
                                                Snackbar.make(parent_view, getString(R.string.snack_bar_applied), Snackbar.LENGTH_SHORT).show();
                                            }, Constant.DELAY_SET);
                                        } catch (IOException e) {
                                            e.printStackTrace();
                                            Snackbar.make(parent_view, getString(R.string.snack_bar_failed), Snackbar.LENGTH_SHORT).show();
                                        }
                                    } else if (single_choice_selected.equals(getResources().getString(R.string.set_crop))) {
                                        Intent intent = new Intent(getApplicationContext(), ActivityCropWallpaper.class);
                                        intent.putExtra("image_url", BASE_IMAGE_URL + wp.image_upload);
                                        startActivity(intent);
                                    } else if (single_choice_selected.equals(getResources().getString(R.string.set_with))) {
                                        setWallpaperFromOtherApp(BASE_IMAGE_URL + wp.image_upload);

                                    }
                                }

                                @Override
                                public void onLoadCleared(@Nullable Drawable placeholder) {

                                }

                                @Override
                                public void onLoadFailed(@Nullable Drawable errorDrawable) {
                                    super.onLoadFailed(errorDrawable);
                                    Snackbar.make(parent_view, getString(R.string.snack_bar_failed), Snackbar.LENGTH_SHORT).show();
                                }
                            });
                })
                .setNegativeButton(R.string.dialog_option_cancel, null)
                .show();
    }

    public void setWallpaper(String imageURL) {
        Snackbar.make(parent_view, getString(R.string.snack_bar_applying), Snackbar.LENGTH_SHORT).show();
        Glide.with(this)
                .load(imageURL.replace(" ", "%20"))
                .into(new CustomTarget<Drawable>() {
                    @Override
                    public void onResourceReady(@NonNull Drawable resource, @Nullable Transition<? super Drawable> transition) {
                        Bitmap bitmap = ((BitmapDrawable) resource).getBitmap();
                        try {
                            WallpaperManager wallpaperManager = WallpaperManager.getInstance(ActivityWallpaperDetail.this);
                            wallpaperManager.setBitmap(bitmap);
                            new Handler(Looper.getMainLooper()).postDelayed(() -> {
                                Snackbar.make(parent_view, getString(R.string.snack_bar_applied), Snackbar.LENGTH_SHORT).show();
                            }, Constant.DELAY_SET);
                        } catch (IOException e) {
                            e.printStackTrace();
                            Snackbar.make(parent_view, getString(R.string.snack_bar_failed), Snackbar.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onLoadCleared(@Nullable Drawable placeholder) {

                    }

                    @Override
                    public void onLoadFailed(@Nullable Drawable errorDrawable) {
                        super.onLoadFailed(errorDrawable);
                        Snackbar.make(parent_view, getString(R.string.snack_bar_error), Snackbar.LENGTH_SHORT).show();
                    }
                });
    }

    public Boolean verifyPermissions() {
        int permissionExternalMemory = ActivityCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE);
        if (permissionExternalMemory != PackageManager.PERMISSION_GRANTED) {
            String[] STORAGE_PERMISSIONS = {Manifest.permission.WRITE_EXTERNAL_STORAGE};
            ActivityCompat.requestPermissions(this, STORAGE_PERMISSIONS, 1);
            return false;
        }
        return true;
    }

    public void setWallpaperFromOtherApp(String imageURL) {

        if (!verifyPermissions()) {
            return;
        }

        Glide.with(this)
                .download(imageURL.replace(" ", "%20"))
                .listener(new RequestListener<File>() {
                    @Override
                    public boolean onLoadFailed(@Nullable GlideException e, Object model, Target<File> target, boolean isFirstResource) {
                        return false;
                    }

                    @Override
                    public boolean onResourceReady(File resource, Object model, Target<File> target, DataSource dataSource, boolean isFirstResource) {
                        try {
                            if (wallpaper.image_upload.endsWith(".gif") || wallpaper.image_url.endsWith(".gif")) {
                                Tools.setWallpaperFromOtherApp(ActivityWallpaperDetail.this, Tools.getBytesFromFile(resource), Tools.createName(imageURL), "image/gif");
                            } else if (wallpaper.image_upload.endsWith(".png") || wallpaper.image_url.endsWith(".png")) {
                                Tools.setWallpaperFromOtherApp(ActivityWallpaperDetail.this, Tools.getBytesFromFile(resource), Tools.createName(imageURL), "image/png");
                            } else {
                                Tools.setWallpaperFromOtherApp(ActivityWallpaperDetail.this, Tools.getBytesFromFile(resource), Tools.createName(imageURL), "image/jpg");
                            }
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                        return true;
                    }
                }).submit();
    }

    public void setGif(String imageURL) {
        if (!verifyPermissions()) {
            return;
        }
        Snackbar.make(parent_view, getString(R.string.snack_bar_preparing), Snackbar.LENGTH_SHORT).show();
        Glide.with(this)
                .download(imageURL.replace(" ", "%20"))
                .listener(new RequestListener<File>() {
                    @Override
                    public boolean onLoadFailed(@Nullable GlideException e, Object model, Target<File> target, boolean isFirstResource) {
                        return false;
                    }

                    @Override
                    public boolean onResourceReady(File resource, Object model, Target<File> target, DataSource dataSource, boolean isFirstResource) {
                        try {
                            if (wallpaper.image_upload.endsWith(".gif") || wallpaper.image_url.endsWith(".gif")) {
                                Tools.setGifWallpaper(ActivityWallpaperDetail.this, Tools.getBytesFromFile(resource), Tools.createName(imageURL), "image/gif");
                            }
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                        return true;
                    }
                }).submit();
    }

    public void downloadWallpaper(String imageURL) {
        if (!verifyPermissions()) {
            return;
        }
        Snackbar.make(parent_view, getString(R.string.snack_bar_saving), Snackbar.LENGTH_SHORT).show();
        Glide.with(this)
                .download(imageURL.replace(" ", "%20"))
                .listener(new RequestListener<File>() {
                    @Override
                    public boolean onLoadFailed(@Nullable GlideException e, Object model, Target<File> target, boolean isFirstResource) {
                        return false;
                    }

                    @Override
                    public boolean onResourceReady(File resource, Object model, Target<File> target, DataSource dataSource, boolean isFirstResource) {
                        try {
                            if (wallpaper.image_upload.endsWith(".gif") || wallpaper.image_url.endsWith(".gif")) {
                                Tools.saveImage(ActivityWallpaperDetail.this, Tools.getBytesFromFile(resource), Tools.createName(imageURL), "image/gif");
                            } else if (wallpaper.image_upload.endsWith(".png") || wallpaper.image_url.endsWith(".png")) {
                                Tools.saveImage(ActivityWallpaperDetail.this, Tools.getBytesFromFile(resource), Tools.createName(imageURL), "image/png");
                            } else {
                                Tools.saveImage(ActivityWallpaperDetail.this, Tools.getBytesFromFile(resource), Tools.createName(imageURL), "image/jpg");
                            }

                            Handler handler = new Handler(Looper.getMainLooper());
                            handler.postDelayed(() -> {
                                Snackbar.make(parent_view, getString(R.string.snack_bar_saved), Snackbar.LENGTH_SHORT).show();
                                // updateDownload(wallpaper.image_id);
                            }, Constant.DELAY_SET);

                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                        return true;
                    }
                }).submit();
    }

    public void shareWallpaper(String imageURL) {
        if (!verifyPermissions()) {
            return;
        }
        Snackbar.make(parent_view, getString(R.string.snack_bar_preparing
        ), Snackbar.LENGTH_SHORT).show();
        Glide.with(this)
                .download(imageURL.replace(" ", "%20"))
                .listener(new RequestListener<File>() {
                    @Override
                    public boolean onLoadFailed(@Nullable GlideException e, Object model, Target<File> target, boolean isFirstResource) {
                        return false;
                    }

                    @Override
                    public boolean onResourceReady(File resource, Object model, Target<File> target, DataSource dataSource, boolean isFirstResource) {
                        try {
                            if (wallpaper.image_upload.endsWith(".gif") || wallpaper.image_url.endsWith(".gif")) {
                                Tools.shareImage(ActivityWallpaperDetail.this, Tools.getBytesFromFile(resource), Tools.createName(imageURL), "image/gif");
                            } else if (wallpaper.image_upload.endsWith(".png") || wallpaper.image_url.endsWith(".png")) {
                                Tools.shareImage(ActivityWallpaperDetail.this, Tools.getBytesFromFile(resource), Tools.createName(imageURL), "image/png");
                            } else {
                                Tools.shareImage(ActivityWallpaperDetail.this, Tools.getBytesFromFile(resource), Tools.createName(imageURL), "image/jpg");
                            }
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                        return true;
                    }
                }).submit();
    }


    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem menuItem) {
        if (menuItem.getItemId() == android.R.id.home) {
            onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(menuItem);
    }

    public void fullScreenMode(boolean on) {
        LinearLayout lyt_bottom = findViewById(R.id.lyt_bottom);
        RelativeLayout bg_shadow = findViewById(R.id.bg_shadow);
        if (on) {
            toolbar.setVisibility(View.GONE);
            toolbar.animate().translationY(-112);
            lyt_bottom.setVisibility(View.GONE);
            lyt_bottom.animate().translationY(lyt_bottom.getHeight());

            bg_shadow.setVisibility(View.GONE);
            bg_shadow.animate().translationY(lyt_bottom.getHeight());

            Tools.transparentStatusBarNavigation(this);

            hideSystemUI();

        } else {
            toolbar.setVisibility(View.VISIBLE);
            toolbar.animate().translationY(0);
            lyt_bottom.setVisibility(View.VISIBLE);
            lyt_bottom.animate().translationY(0);

            bg_shadow.setVisibility(View.VISIBLE);
            bg_shadow.animate().translationY(0);

            Tools.transparentStatusBarNavigation(this);

        }
    }

    private void hideSystemUI() {
        // Enables regular immersive mode.
        // For "lean back" mode, remove SYSTEM_UI_FLAG_IMMERSIVE.
        // Or for "sticky immersive," replace it with SYSTEM_UI_FLAG_IMMERSIVE_STICKY
        View decorView = getWindow().getDecorView();
        //noinspection deprecation
        decorView.setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_IMMERSIVE
                        // Set the content to appear under the system bars so that the
                        // content doesn't resize when the system bars hide and show.
                        | View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                        | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                        // Hide the nav bar and status bar
                        | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_FULLSCREEN);
    }

}
