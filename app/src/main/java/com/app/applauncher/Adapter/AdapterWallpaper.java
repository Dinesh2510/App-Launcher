package com.app.applauncher.Adapter;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.StaggeredGridLayoutManager;

import com.app.applauncher.Wallpaper_Data.ActivityWallpaperDetail;
import com.app.applauncher.R;
import com.app.applauncher.Wallpaper_Data.Wallpaper;
import com.app.applauncher.utils.Config;
import com.app.applauncher.utils.Constant;
import com.app.applauncher.utils.DBHelper;
import com.app.applauncher.utils.SharedPref;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.RequestOptions;

import java.io.Serializable;
import java.util.List;

import static com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions.withCrossFade;

public class AdapterWallpaper extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private final int VIEW_AD = 2;
    private final int VIEW_ITEM = 1;
    private final int VIEW_PROG = 0;
    private List<Wallpaper> items;
    DBHelper dbHelper;
    private boolean loading;

    private Context context;
    private Wallpaper pos;
    private CharSequence charSequence = null;
    private boolean scrolling = false;



    public AdapterWallpaper(Context context, RecyclerView view, List<Wallpaper> items) {
        this.items = items;
        this.context = context;
    }

    public static class OriginalViewHolder extends RecyclerView.ViewHolder {

        public ImageView wallpaper_image;
        public TextView wallpaper_name;
        public TextView category_name;
        public CardView card_view;
        LinearLayout bg_shadow,ll_name;
        FrameLayout lyt_parent;

        public OriginalViewHolder(View v) {
            super(v);
            wallpaper_image = v.findViewById(R.id.wallpaper_image);
            wallpaper_name = v.findViewById(R.id.wallpaper_name);
            category_name = v.findViewById(R.id.category_name);
            card_view = v.findViewById(R.id.card_view);
            bg_shadow = v.findViewById(R.id.bg_shadow);
            lyt_parent = v.findViewById(R.id.lyt_parent);
            ll_name = v.findViewById(R.id.ll_name);
        }

    }



    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        RecyclerView.ViewHolder vh;
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_wallpaper, parent, false);
        vh = new OriginalViewHolder(v);
        return vh;
    }

    // Replace the contents of a view (invoked by the layout manager)
    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, final int position) {
            final Wallpaper p = items.get(position);
            final OriginalViewHolder vItem = (OriginalViewHolder) holder;

            vItem.wallpaper_name.setText(p.image_name);
            vItem.category_name.setText(p.category_name);

            if (!Config.ENABLE_DISPLAY_WALLPAPER_NAME) {
                vItem.wallpaper_name.setVisibility(View.GONE);
            }

            if (!Config.ENABLE_DISPLAY_WALLPAPER_CATEGORY) {
                vItem.category_name.setVisibility(View.GONE);
            }

            SharedPref sharedPref = new SharedPref(context);
            if (sharedPref.getIsDarkTheme()) {
                vItem.card_view.setCardBackgroundColor(context.getResources().getColor(R.color.colorToolbarDark));
            } else {
                vItem.card_view.setCardBackgroundColor(context.getResources().getColor(R.color.grey_soft));
            }

            if (!Config.ENABLE_DISPLAY_WALLPAPER_NAME && !Config.ENABLE_DISPLAY_WALLPAPER_CATEGORY) {
                vItem.bg_shadow.setBackgroundResource(R.drawable.ic_transparent);
            }

            Glide.with(context)
                    .load(Config.ADMIN_PANEL_URL + "/upload/" + items.get(position).image_upload.replace(" ", "%20"))
                    .transition(withCrossFade())
                    .thumbnail(0.1f)
                    .apply(new RequestOptions().override(Constant.THUMBNAIL_WIDTH, Constant.THUMBNAIL_HEIGHT))
                    .diskCacheStrategy(DiskCacheStrategy.ALL)
                    .placeholder(R.drawable.ic_transparent)
                    .centerCrop()
                    .into(vItem.wallpaper_image);

            vItem.lyt_parent.setOnClickListener(view -> {

                Intent intent = new Intent(context, ActivityWallpaperDetail.class);
                intent.putExtra(com.app.applauncher.Constant.POSITION, position);
                Bundle bundle = new Bundle();
                bundle.putSerializable(com.app.applauncher.Constant.ARRAY_LIST, (Serializable) items);
                intent.putExtra(com.app.applauncher.Constant.BUNDLE, bundle);
                intent.putExtra(com.app.applauncher.Constant.EXTRA_OBJC, p);
                context.startActivity(intent);

            });



        StaggeredGridLayoutManager.LayoutParams layoutParams = (StaggeredGridLayoutManager.LayoutParams) holder.itemView.getLayoutParams();
        layoutParams.setFullSpan(false);

    }

    @Override
    public int getItemCount() {
        return items.size();
    }


    public void insertData(List<Wallpaper> items) {
        setLoaded();
        int positionStart = getItemCount();
        int itemCount = items.size();
        this.items.addAll(items);
        notifyItemRangeInserted(positionStart, itemCount);
    }


    public void setLoaded() {
        loading = false;
        for (int i = 0; i < getItemCount(); i++) {
            if (items.get(i) == null) {
                items.remove(i);
                notifyItemRemoved(i);
            }
        }
    }


    public void setItems(List<Wallpaper> items) {
        this.items = items;
        notifyDataSetChanged();
    }

}