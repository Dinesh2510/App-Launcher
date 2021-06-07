package com.app.applauncher;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

import static android.content.Context.MODE_PRIVATE;


public class AppAdapter extends RecyclerView.Adapter<AppAdapter.MyViewHolder> {
    ArrayList<AppModel> conappArrayList;
    Context context;

    String str_userid, str_name, str_fname, str_lname;
    private AppAdapter.OnItemClickListener listener;

    public interface OnItemClickListener {
        void onItemClick(int positon, AppModel item, View view);
    }
    public void setFilter(ArrayList<AppModel> filterllist) {
        // below line is to add our filtered
        // list in our course array list.
        conappArrayList = filterllist;
        // below line is to notify our adapter
        // as change in recycler view data.
        notifyDataSetChanged();
    }

    public AppAdapter() {
    }

    public AppAdapter(ArrayList<AppModel> conappArrayList, AppCompatActivity context) {
        this.conappArrayList = conappArrayList;
        this.context = context;
    }

    @NonNull
    @Override
    public AppAdapter.MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = (View) LayoutInflater.from(parent.getContext()).inflate(R.layout.item_app, parent, false);
        return new MyViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull AppAdapter.MyViewHolder holder, final int position) {
        final AppModel classModel = conappArrayList.get(position);


        holder.mImage.setImageDrawable(conappArrayList.get(position).getImage());
        holder.mLabel.setText("App Name: "+conappArrayList.get(position).getName());
        holder.label_pkg.setText("Package: " +conappArrayList.get(position).getPackageName());
        holder.label_v_code.setText("Version Code: "+ conappArrayList.get(position).v_code);
        holder.label_v_name.setText("Version Name: "+conappArrayList.get(position).v_name);
        holder.label_class.setText("MainClass: "+conappArrayList.get(position).v_class);

        holder.mLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ((MainActivity) context).normalClick(conappArrayList.get(position), position);
            }
        });
        holder.mLayout.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                ((MainActivity) context).normalClickLong(conappArrayList.get(position), position);
                return true;
            }
        });

//        holder.itemView.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//
//                Intent intent = new Intent(context, data.class);
//                intent.putExtra("name", conVideoArrayList.get(position).getName());
//                intent.putExtra("id", conVideoArrayList.get(position).getId());
//                intent.putExtra("image", Constant.Appointment_img + classModel.getImage());
//                context.startActivity(intent);
//            }
//        });


    }

    @Override
    public int getItemCount() {
        return conappArrayList.size();
    }

    public static class MyViewHolder extends RecyclerView.ViewHolder {
        TextView name, description, date, location, id_tx_inter;
        ImageView share, image;
        LinearLayout mLayout;
        ImageView mImage;
        TextView mLabel, label_v_name, label_v_code, label_pkg,label_class;

        public MyViewHolder(@NonNull View v) {
            super(v);
            mLayout = v.findViewById(R.id.appItemLayout);
            mImage = v.findViewById(R.id.icon);
            mLabel = v.findViewById(R.id.label);
            label_pkg = v.findViewById(R.id.label_pkg);
            label_v_code = v.findViewById(R.id.label_v_code);
            label_v_name = v.findViewById(R.id.label_v_name);
            label_class = v.findViewById(R.id.label_class);


        }


    }


}