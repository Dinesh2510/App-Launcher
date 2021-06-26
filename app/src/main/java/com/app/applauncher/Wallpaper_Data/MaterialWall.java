package com.app.applauncher.Wallpaper_Data;

import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;

import com.android.volley.AuthFailureError;
import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.RetryPolicy;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.app.applauncher.Adapter.AdapterWallpaper;
import com.app.applauncher.Constant;
import com.app.applauncher.R;
import com.app.applauncher.utils.DBHelper;
import com.app.applauncher.utils.ItemOffsetDecoration;
import com.app.applauncher.utils.SharedPref;
import com.facebook.shimmer.ShimmerFrameLayout;

import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.StaggeredGridLayoutManager;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;


import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class MaterialWall extends AppCompatActivity {
    private static final String BASE_IMAGE_URL = "";
    private String single_choice_selected;
    Wallpaper wallpaper;
    List<Wallpaper> items = new ArrayList<>();
    String all_url = "https://solodroid.id/codecanyon/demo/material_wallpaper/api/v1/api.php?get_wallpapers&page=1&count=100&filter=g.image_extension != 'all'&order=ORDER BY g.id DESC";
    private static final String ARG_ORDER = "order";
    private static final String ARG_FILTER = "filter";
    private RecyclerView recyclerView;
    private AdapterWallpaper adapterWallpaper;
    private SwipeRefreshLayout swipeRefreshLayout;
    private ShimmerFrameLayout lyt_shimmer;
    private int post_total = 0;
    private int failed_page = 0;
    private SharedPref sharedPref;

    String order, filter;
    DBHelper dbHelper;
    int counter = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_material_wall);

        //dialogOptionSetWallpaper("http://pixeldev.in/webservices/digital_reader/admin/post_images/pixel_img.jpg", wallpaper);
        dbHelper = new DBHelper(MaterialWall.this);
        sharedPref = new SharedPref(MaterialWall.this);

        swipeRefreshLayout = findViewById(R.id.swipe_refresh_layout);
        swipeRefreshLayout.setColorSchemeResources(R.color.colorPrimary);
        lyt_shimmer = findViewById(R.id.shimmer_view_container);
        initShimmerLayout();


        recyclerView = findViewById(R.id.recyclerView);
        ItemOffsetDecoration itemDecoration = new ItemOffsetDecoration(MaterialWall.this, R.dimen.grid_space_wallpaper);
        recyclerView.addItemDecoration(itemDecoration);
        recyclerView.setLayoutManager(new StaggeredGridLayoutManager(sharedPref.getWallpaperColumns(), StaggeredGridLayoutManager.VERTICAL));
        recyclerView.setHasFixedSize(true);

        //set data and list adapter
        adapterWallpaper = new AdapterWallpaper(MaterialWall.this, recyclerView, items);
        recyclerView.setAdapter(adapterWallpaper);
        ImageView img_back = findViewById(R.id.img_back);
        img_back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        ImageView wall_fav = findViewById(R.id.wall_fav);
        wall_fav.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(),Favorite_Wallpaper.class);
                startActivity(intent);
            }
        });

        // on item list clicked


        // detect when scroll reach bottom

        // on swipe list
/*
        swipeRefreshLayout.setOnRefreshListener(() -> {
            swipeRefreshLayout.setRefreshing(true);
            lyt_shimmer.setVisibility(View.VISIBLE);
            lyt_shimmer.startShimmer();
            requestAction(1);
        });
*/

        requestAction(1);

    }




    public void requestListPostApi() {

        final ProgressDialog progressDialog = new ProgressDialog(MaterialWall.this);
        progressDialog.setCancelable(false);
        progressDialog.setMessage("Loading..");
        progressDialog.show();
        StringRequest stringRequest = new StringRequest(Request.Method.GET, all_url, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                Log.d("list_cat", "onResponse: " + response);
                try {
                    JSONObject jsonObject = new JSONObject(response);
                    String message = jsonObject.getString("status");
                    String code = jsonObject.getString("status");
                    if (code.equals("ok")) {
                        progressDialog.dismiss();
                        recyclerView.setVisibility(View.VISIBLE);

                        swipeRefreshLayout.setRefreshing(false);
                        showFailedView(false, "");
                        showNoItemView(false);
                        lyt_shimmer.setVisibility(View.GONE);
                        lyt_shimmer.stopShimmer();
                        JSONArray jsonArrayvideo = jsonObject.getJSONArray("posts");

                        if (jsonArrayvideo != null) {
                            for (int i = 0; i < jsonArrayvideo.length(); i++) {
                                Wallpaper wallpaper1 = new Wallpaper();
                                JSONObject BMIReport = jsonArrayvideo.getJSONObject(i);

                                wallpaper1.image_id = BMIReport.getString("image_id");
                                wallpaper1.image_name = BMIReport.getString("image_name");
                                wallpaper1.image_upload = BMIReport.getString("image_upload");
                                wallpaper1.resolution = BMIReport.getString("resolution");
                                wallpaper1.image_url = BMIReport.getString("image_url");
                                wallpaper1.size = BMIReport.getString("size");
                                wallpaper1.mime = BMIReport.getString("mime");
                                wallpaper1.views = BMIReport.getInt("views");
                                wallpaper1.downloads = BMIReport.getInt("downloads");
                                wallpaper1.featured = BMIReport.getString("featured");
                                wallpaper1.category_name = BMIReport.getString("category_name");
                                wallpaper1.last_update = BMIReport.getString("last_update");

                                items.add(wallpaper1);

                            }
                            dbHelper.truncateTableWallpaper(DBHelper.TABLE_RECENT);
                            dbHelper.addListWallpaper(items, DBHelper.TABLE_RECENT);
                            recyclerView.setAdapter(new AdapterWallpaper(MaterialWall.this, recyclerView, items));


                        } else {
                            Log.d("null", "onResponse: ");
                        }


                    } else {
                        initShimmerLayout();
                        swipeRefreshLayout.setRefreshing(false);
                        showFailedView(false, "");
                        showNoItemView(false);
                        progressDialog.dismiss();
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                    initShimmerLayout();
                    swipeRefreshLayout.setRefreshing(false);
                    progressDialog.dismiss();
                }

                progressDialog.dismiss();

            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                error.printStackTrace();
                progressDialog.dismiss();
                loadDataFromDatabase();

            }
        }) {

            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                HashMap<String, String> params = new HashMap();
                Log.d("params", "getParams: " + params);
                return params;
            }

            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                HashMap<String, String> header = new HashMap<>();
                return header;
            }

        };
        RequestQueue requestQueue = Volley.newRequestQueue(getApplicationContext());
        RetryPolicy retryPolicy = new DefaultRetryPolicy(3000,
                DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT);
        stringRequest.setRetryPolicy(retryPolicy);
        requestQueue.add(stringRequest);

    }


    private void loadDataFromDatabase() {
        List<Wallpaper> posts = dbHelper.getAllWallpaper(DBHelper.TABLE_RECENT);
        adapterWallpaper.insertData(posts);

    }


    public void requestAction(final int page_no) {
        showFailedView(false, "");
        showNoItemView(false);
        new Handler(Looper.getMainLooper()).postDelayed(this::requestListPostApi, Constant.DELAY_TIME);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        swipeRefreshLayout.setRefreshing(true);
        lyt_shimmer.setVisibility(View.GONE);
        lyt_shimmer.stopShimmer();
        lyt_shimmer.stopShimmer();
    }

    private void showFailedView(boolean show, String message) {
        View lyt_failed = findViewById(R.id.lyt_failed);
        ((TextView) findViewById(R.id.failed_message)).setText(message);
        if (show) {
            recyclerView.setVisibility(View.GONE);
            lyt_failed.setVisibility(View.VISIBLE);
        } else {
            recyclerView.setVisibility(View.VISIBLE);
            lyt_failed.setVisibility(View.GONE);
        }
        findViewById(R.id.failed_retry).setOnClickListener(view -> requestAction(failed_page));
    }

    private void showNoItemView(boolean show) {
        View lyt_no_item = findViewById(R.id.lyt_no_item);
        ((TextView) findViewById(R.id.no_item_message)).setText(R.string.msg_no_item);
        if (show) {
            recyclerView.setVisibility(View.GONE);
            lyt_no_item.setVisibility(View.VISIBLE);
        } else {
            recyclerView.setVisibility(View.VISIBLE);
            lyt_no_item.setVisibility(View.GONE);
        }
    }


    public void initShimmerLayout() {
        View view_shimmer_2_columns = findViewById(R.id.view_shimmer_2_columns);
        View view_shimmer_3_columns = findViewById(R.id.view_shimmer_3_columns);
        if (sharedPref.getWallpaperColumns() == 3) {
            view_shimmer_2_columns.setVisibility(View.GONE);
            view_shimmer_3_columns.setVisibility(View.VISIBLE);
        } else {
            view_shimmer_2_columns.setVisibility(View.VISIBLE);
            view_shimmer_3_columns.setVisibility(View.GONE);
        }
    }


    @Override
    public void onResume() {
        super.onResume();
    }
}