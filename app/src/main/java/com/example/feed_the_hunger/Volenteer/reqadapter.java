package com.example.feed_the_hunger.Volenteer;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.net.Uri;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.bumptech.glide.Glide;
import com.example.feed_the_hunger.MyIP;
import com.example.feed_the_hunger.R;
import com.example.feed_the_hunger.food_model;

import java.util.ArrayList;

public class reqadapter extends BaseAdapter {

    private static final String TAG = "reqadapter";

    Activity context;
    ArrayList<food_model> list;

    public reqadapter(Activity context, ArrayList<food_model> list) {
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
        Button btnAccept, btnReject, btnDelete, btnMap;
        LinearLayout buttonLayout;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder;

        if (convertView == null) {
            LayoutInflater inflater = context.getLayoutInflater();
            convertView = inflater.inflate(R.layout.activity_vol_req_row, parent, false);

            holder = new ViewHolder();
            holder.name = convertView.findViewById(R.id.tvName);
            holder.description = convertView.findViewById(R.id.tvDescription);
            holder.category = convertView.findViewById(R.id.tvCategory);
            holder.status = convertView.findViewById(R.id.tvStatus);
            holder.image = convertView.findViewById(R.id.tvImage);

            holder.btnAccept = convertView.findViewById(R.id.btnAccept);
            holder.btnReject = convertView.findViewById(R.id.btnReject);
            holder.btnDelete = convertView.findViewById(R.id.btnDelete);
            holder.btnMap = convertView.findViewById(R.id.btnMap);
            holder.buttonLayout = convertView.findViewById(R.id.buttonLayout);

            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        food_model food = list.get(position);

        holder.name.setText(food.getFoodName());
        holder.description.setText(food.getDescription());
        holder.category.setText(food.getFoodType());

        Glide.with(context)
                .load(food.getImage())
                .placeholder(R.drawable.ic_launcher_background)
                .error(R.drawable.ic_launcher_background)
                .into(holder.image);

        String currentStatus = food.getStatus();

        if (currentStatus == null || currentStatus.trim().isEmpty()) {
            currentStatus = "pending";
        }

        String finalStatus = currentStatus.toLowerCase();

        holder.btnMap.setVisibility(View.VISIBLE);

        if (finalStatus.equals("pending")) {
            holder.status.setText("Status: Pending");
            holder.status.setTextColor(Color.parseColor("#FF9800"));
            holder.buttonLayout.setVisibility(View.VISIBLE);

            holder.btnAccept.setVisibility(View.VISIBLE);
            holder.btnReject.setVisibility(View.VISIBLE);
            holder.btnDelete.setVisibility(View.GONE);

            holder.btnAccept.setEnabled(true);
            holder.btnReject.setEnabled(true);

        } else if (finalStatus.equals("accepted")) {
            holder.status.setText("This item is Accepted");
            holder.status.setTextColor(Color.parseColor("#2E7D32"));
            holder.buttonLayout.setVisibility(View.VISIBLE);

            holder.btnAccept.setVisibility(View.GONE);
            holder.btnReject.setVisibility(View.GONE);
            holder.btnDelete.setVisibility(View.VISIBLE);

        } else if (finalStatus.equals("rejected")) {
            holder.status.setText("This item is Rejected");
            holder.status.setTextColor(Color.parseColor("#C62828"));
            holder.buttonLayout.setVisibility(View.VISIBLE);

            holder.btnAccept.setVisibility(View.GONE);
            holder.btnReject.setVisibility(View.GONE);
            holder.btnDelete.setVisibility(View.VISIBLE);

        } else {
            holder.status.setText("Status: " + currentStatus);
            holder.status.setTextColor(Color.BLACK);
            holder.buttonLayout.setVisibility(View.VISIBLE);

            holder.btnAccept.setVisibility(View.VISIBLE);
            holder.btnReject.setVisibility(View.VISIBLE);
            holder.btnDelete.setVisibility(View.GONE);

            holder.btnAccept.setEnabled(true);
            holder.btnReject.setEnabled(true);
        }

        holder.btnAccept.setOnClickListener(v -> {
            holder.btnAccept.setEnabled(false);
            holder.btnReject.setEnabled(false);
            updateStatus(food, "accepted");
        });

        holder.btnReject.setOnClickListener(v -> {
            holder.btnAccept.setEnabled(false);
            holder.btnReject.setEnabled(false);
            updateStatus(food, "rejected");
        });

        holder.btnDelete.setOnClickListener(v -> deleteFood(food, position));
        holder.btnMap.setOnClickListener(v -> openMap(food));

        return convertView;
    }

    private void openMap(food_model food) {
        double latitude = food.getLatitude();
        double longitude = food.getLongitude();

        Log.d(TAG, "Map clicked. Lat=" + latitude + ", Lng=" + longitude);

        if (latitude == 0.0 && longitude == 0.0) {
            Toast.makeText(context, "Location not available", Toast.LENGTH_SHORT).show();
            return;
        }

        try {
            Uri gmmIntentUri = Uri.parse("google.navigation:q=" + latitude + "," + longitude + "&mode=d");
            Intent mapIntent = new Intent(Intent.ACTION_VIEW, gmmIntentUri);
            mapIntent.setPackage("com.google.android.apps.maps");

            if (mapIntent.resolveActivity(context.getPackageManager()) != null) {
                context.startActivity(mapIntent);
                return;
            }

            Uri webUri = Uri.parse("https://www.google.com/maps/dir/?api=1&destination=" + latitude + "," + longitude);
            Intent webIntent = new Intent(Intent.ACTION_VIEW, webUri);

            if (webIntent.resolveActivity(context.getPackageManager()) != null) {
                context.startActivity(webIntent);
            } else {
                Toast.makeText(context, "No map app found", Toast.LENGTH_SHORT).show();
            }

        } catch (Exception e) {
            Log.e(TAG, "Map open error", e);
            Toast.makeText(context, "Unable to open map", Toast.LENGTH_SHORT).show();
        }
    }

    private void updateStatus(food_model food, String status) {
        SharedPreferences prefs = context.getSharedPreferences("VolunteerSession", Activity.MODE_PRIVATE);
        String volunteerId = prefs.getString("userId", "");

        if (volunteerId == null || volunteerId.trim().isEmpty()) {
            Toast.makeText(context, "Volunteer not logged in", Toast.LENGTH_SHORT).show();
            notifyDataSetChanged();
            return;
        }

        // ✅ FIXED: old wrong URL was user/food/status/
        String url = MyIP.IP_ADDRESS + "food/status/" + food.getId();

        Log.d(TAG, "STATUS URL: " + url);
        Log.d(TAG, "STATUS: " + status);
        Log.d(TAG, "VOLUNTEER ID: " + volunteerId);

        StringRequest request = new StringRequest(
                Request.Method.PATCH,
                url,
                response -> {
                    Log.d(TAG, "STATUS RESPONSE: " + response);
                    Toast.makeText(context, "Status Updated", Toast.LENGTH_SHORT).show();

                    food.setStatus(status);
                    notifyDataSetChanged();
                },
                error -> {
                    String msg = "Error updating status";

                    if (error.networkResponse != null) {
                        msg = "Error Code: " + error.networkResponse.statusCode;
                        Log.e(TAG, "STATUS ERROR CODE: " + error.networkResponse.statusCode);

                        if (error.networkResponse.data != null) {
                            String errorBody = new String(error.networkResponse.data);
                            Log.e(TAG, "STATUS ERROR BODY: " + errorBody);
                        }
                    } else {
                        Log.e(TAG, "STATUS NETWORK ERROR: " + error.toString());
                    }

                    Toast.makeText(context, msg, Toast.LENGTH_LONG).show();
                    notifyDataSetChanged();
                }
        ) {
            @Override
            public byte[] getBody() {
                String body = "{"
                        + "\"status\":\"" + status + "\","
                        + "\"volunteerId\":\"" + volunteerId + "\""
                        + "}";

                Log.d(TAG, "STATUS BODY: " + body);
                return body.getBytes();
            }

            @Override
            public String getBodyContentType() {
                return "application/json; charset=utf-8";
            }
        };

        Volley.newRequestQueue(context).add(request);
    }

    private void deleteFood(food_model food, int position) {
        String url = MyIP.IP_ADDRESS + "food/delete/" + food.getId() + "?deletedBy=volunteer";

        Log.d(TAG, "DELETE URL: " + url);

        StringRequest request = new StringRequest(
                Request.Method.DELETE,
                url,
                response -> {
                    Log.d(TAG, "DELETE RESPONSE: " + response);
                    Toast.makeText(context, "Request Deleted", Toast.LENGTH_SHORT).show();

                    list.remove(position);
                    notifyDataSetChanged();
                },
                error -> {
                    String msg = "Delete failed";

                    if (error.networkResponse != null) {
                        msg = "Error Code: " + error.networkResponse.statusCode;
                        Log.e(TAG, "DELETE ERROR CODE: " + error.networkResponse.statusCode);

                        if (error.networkResponse.data != null) {
                            String errorBody = new String(error.networkResponse.data);
                            Log.e(TAG, "DELETE ERROR BODY: " + errorBody);
                        }
                    } else {
                        Log.e(TAG, "DELETE NETWORK ERROR: " + error.toString());
                    }

                    Toast.makeText(context, msg, Toast.LENGTH_LONG).show();
                }
        );

        Volley.newRequestQueue(context).add(request);
    }
}