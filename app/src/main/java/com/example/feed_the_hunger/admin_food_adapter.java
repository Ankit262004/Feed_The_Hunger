package com.example.feed_the_hunger;

import android.app.Activity;
import android.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.bumptech.glide.Glide;

import java.util.ArrayList;

public class admin_food_adapter extends BaseAdapter {

    private final Activity context;
    private final ArrayList<admin_food_model> list;
    private final RequestQueue requestQueue;

    public admin_food_adapter(Activity context, ArrayList<admin_food_model> list) {
        this.context = context;
        this.list = list;
        this.requestQueue = Volley.newRequestQueue(context);
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
        Button btnDelete;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder;

        if (convertView == null) {
            LayoutInflater inflater = context.getLayoutInflater();
            convertView = inflater.inflate(R.layout.activity_admin_food_row, parent, false);

            holder = new ViewHolder();
            holder.name = convertView.findViewById(R.id.tvName);
            holder.description = convertView.findViewById(R.id.tvDescription);
            holder.category = convertView.findViewById(R.id.tvCategory);
            holder.status = convertView.findViewById(R.id.tvStatus);
            holder.image = convertView.findViewById(R.id.ivImage);
            holder.btnDelete = convertView.findViewById(R.id.btnDelete);

            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        admin_food_model food = list.get(position);

        String foodName = food.getFoodName();
        String description = food.getDescription();
        String category = food.getFoodType();
        String status = food.getStatus();
        String imageUrl = food.getImage();

        if (foodName == null || foodName.trim().isEmpty()) {
            foodName = "N/A";
        }

        if (description == null || description.trim().isEmpty()) {
            description = "No Description";
        }

        if (category == null || category.trim().isEmpty()) {
            category = "N/A";
        }

        if (status == null || status.trim().isEmpty()) {
            status = "Pending";
        }

        holder.name.setText(foodName);
        holder.description.setText(description);
        holder.category.setText(category);
        holder.status.setText("Status: " + status);

        Glide.with(context)
                .load(imageUrl)
                .placeholder(R.drawable.ic_launcher_background)
                .error(R.drawable.ic_launcher_background)
                .into(holder.image);

        holder.btnDelete.setOnClickListener(v -> showDeleteDialog(food));

        return convertView;
    }

    private void showDeleteDialog(admin_food_model food) {
        new AlertDialog.Builder(context)
                .setTitle("Delete Food")
                .setMessage("Are you sure you want to delete this food item?")
                .setPositiveButton("Yes", (dialog, which) -> deleteFood(food))
                .setNegativeButton("No", null)
                .show();
    }

    private void deleteFood(admin_food_model food) {
        String url = MyIP.IP_ADDRESS + "food/delete/" + food.getId() + "?deletedBy=admin";

        StringRequest request = new StringRequest(
                Request.Method.DELETE,
                url,
                response -> {
                    list.remove(food);
                    notifyDataSetChanged();
                    Toast.makeText(context, "Food Deleted Successfully", Toast.LENGTH_SHORT).show();
                },
                error -> {
                    String msg = "Delete failed";

                    if (error.networkResponse != null) {
                        msg = "Error Code: " + error.networkResponse.statusCode;
                    }

                    Toast.makeText(context, msg, Toast.LENGTH_LONG).show();
                }
        ) {
            @Override
            public byte[] getBody() {
                String body = "{ \"deletedBy\": \"admin\" }";
                return body.getBytes();
            }

            @Override
            public String getBodyContentType() {
                return "application/json; charset=utf-8";
            }
        };

        requestQueue.add(request);
    }
}