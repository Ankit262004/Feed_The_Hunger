package com.example.feed_the_hunger.Volenteer;

import android.app.Activity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.example.feed_the_hunger.R;

import java.util.ArrayList;

public class volunteer_delivery_adapter extends BaseAdapter {

    Activity context;
    ArrayList<volunteer_delivery_model> list;

    public volunteer_delivery_adapter(Activity context, ArrayList<volunteer_delivery_model> list) {
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
        ImageView ivImage;
        TextView tvFoodName, tvType, tvQuantity, tvExpiry, tvLocation, tvStatus, tvDescription;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder;
        LayoutInflater inflater = context.getLayoutInflater();

        if (convertView == null) {
            convertView = inflater.inflate(R.layout.activity_volunteer_delivery_row, parent, false);

            holder = new ViewHolder();
            holder.ivImage = convertView.findViewById(R.id.ivImage);
            holder.tvFoodName = convertView.findViewById(R.id.tvFoodName);
            holder.tvType = convertView.findViewById(R.id.tvType);
            holder.tvQuantity = convertView.findViewById(R.id.tvQuantity);
            holder.tvExpiry = convertView.findViewById(R.id.tvExpiry);
            holder.tvLocation = convertView.findViewById(R.id.tvLocation);
            holder.tvStatus = convertView.findViewById(R.id.tvStatus);
            holder.tvDescription = convertView.findViewById(R.id.tvDescription);

            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        volunteer_delivery_model item = list.get(position);

        holder.tvFoodName.setText("Food: " + item.getFoodName());
        holder.tvType.setText("Type: " + item.getFoodType());
        holder.tvQuantity.setText("Quantity: " + item.getQuantity());
        holder.tvExpiry.setText("Expiry: " + item.getExpiryDate());
        holder.tvLocation.setText("Location: " + item.getLocation());
        holder.tvStatus.setText("Status: " + item.getStatus());
        holder.tvDescription.setText("Description: " + item.getDescription());

        Glide.with(context)
                .load(item.getImage())
                .placeholder(R.drawable.ic_launcher_background)
                .into(holder.ivImage);

        return convertView;
    }
}