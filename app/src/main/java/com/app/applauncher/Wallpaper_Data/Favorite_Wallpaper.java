package com.app.applauncher.Wallpaper_Data;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.StaggeredGridLayoutManager;

import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;

import com.app.applauncher.Adapter.AdapterWallpaper;
import com.app.applauncher.R;
import com.app.applauncher.utils.DBHelper;
import com.app.applauncher.utils.ItemOffsetDecoration;
import com.app.applauncher.utils.SharedPref;

import java.util.ArrayList;
import java.util.List;

public class Favorite_Wallpaper extends AppCompatActivity {
    private RecyclerView recyclerView;
    private AdapterWallpaper adapterWallpaper;
    SharedPref sharedPref;
    DBHelper dbHelper;
    View lyt_no_favorite;
    List<Wallpaper> items = new ArrayList<>();
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_favorite_wallpaper);
        ImageView img_back = findViewById(R.id.img_back);
        img_back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        lyt_no_favorite = findViewById(R.id.lyt_not_found);
        dbHelper = new DBHelper(Favorite_Wallpaper.this);
        sharedPref = new SharedPref(Favorite_Wallpaper.this);

        recyclerView = findViewById(R.id.recyclerView);
        ItemOffsetDecoration itemDecoration = new ItemOffsetDecoration(Favorite_Wallpaper.this, R.dimen.grid_space_wallpaper);
        recyclerView.addItemDecoration(itemDecoration);
        recyclerView.setLayoutManager(new StaggeredGridLayoutManager(sharedPref.getWallpaperColumns(), StaggeredGridLayoutManager.VERTICAL));
        recyclerView.setHasFixedSize(true);

        adapterWallpaper = new AdapterWallpaper(Favorite_Wallpaper.this, recyclerView, items);
        recyclerView.setAdapter(adapterWallpaper);

        displayData();

    }
    private void displayData() {
        List<Wallpaper> list = dbHelper.getAllFavorite(DBHelper.TABLE_FAVORITE);
        adapterWallpaper.setItems(list);
        if (list.size() == 0) {
            lyt_no_favorite.setVisibility(View.VISIBLE);
        } else {
            lyt_no_favorite.setVisibility(View.GONE);
        }

    }

    @Override
    public void onResume() {
        super.onResume();
        displayData();
    }
}

