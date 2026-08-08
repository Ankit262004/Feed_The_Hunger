package com.example.feed_the_hunger;

import android.app.Activity;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;

import java.util.ArrayList;

public class foodadapter extends BaseAdapter {

    Activity context;
    ArrayList<food_model> list;

    public foodadapter(Activity context, ArrayList<food_model> list) {
        this.context = context;
        this.list = list;
    }

    @Override
    public int getCount() {
        return list.size();
    }

    @Override
    public Object getItem(int position) {
        return list.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    static class ViewHolder {
        TextView name, description, category, status;
        ImageView image;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        ViewHolder holder;

        if (convertView == null) {
            LayoutInflater inflater = context.getLayoutInflater();
            convertView = inflater.inflate(R.layout.activity_food_row, parent, false);

            holder = new ViewHolder();
            holder.name = convertView.findViewById(R.id.tvName);
            holder.description = convertView.findViewById(R.id.tvDescription);
            holder.category = convertView.findViewById(R.id.tvCategory);
            holder.status = convertView.findViewById(R.id.tvStatus);
            holder.image = convertView.findViewById(R.id.tvImage); // make sure this is actually an ImageView id

            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        food_model food = list.get(position);

        holder.name.setText(food.getFoodName());
        holder.description.setText(food.getDescription());
        holder.category.setText(food.getFoodType());

        String foodStatus = food.getStatus();

        if (foodStatus != null) {
            holder.status.setText(foodStatus);

            switch (foodStatus.toLowerCase()) {
                case "available":
                    holder.status.setBackgroundColor(Color.parseColor("#4CAF50"));
                    break;
                case "expired":
                    holder.status.setBackgroundColor(Color.parseColor("#F44336"));
                    break;
                case "collected":
                    holder.status.setBackgroundColor(Color.parseColor("#9E9E9E"));
                    break;
                default:
                    holder.status.setBackgroundColor(Color.parseColor("#2196F3"));
                    break;
            }
        } else {
            holder.status.setText("Unknown");
            holder.status.setBackgroundColor(Color.parseColor("#2196F3"));
        }

        String imageUrl = food.getImage();

        if (imageUrl != null && !imageUrl.startsWith("http")) {
            if (imageUrl.startsWith("/uploads/")) {
                imageUrl = MyIP.IP_ADDRESS + imageUrl;
            } else if (imageUrl.startsWith("uploads/")) {
                imageUrl = MyIP.IP_ADDRESS + imageUrl;
            } else {
                imageUrl = MyIP.IP_ADDRESS + "uploads/" + imageUrl;
            }
        }

        Glide.with(context)
                .load(imageUrl)
                .placeholder(R.drawable.ic_launcher_background)
                .error(R.drawable.ic_launcher_background)
                .into(holder.image);

        return convertView;
    }
}