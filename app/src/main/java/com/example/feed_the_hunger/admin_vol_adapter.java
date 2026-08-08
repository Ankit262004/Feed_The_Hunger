package com.example.feed_the_hunger;

import android.app.Activity;
import android.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import java.util.ArrayList;

public class admin_vol_adapter extends BaseAdapter {

    Activity activity;
    ArrayList<admin_vol_model> list;

    public admin_vol_adapter(Activity activity, ArrayList<admin_vol_model> list) {
        this.activity = activity;
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

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        if (convertView == null) {
            LayoutInflater inflater = activity.getLayoutInflater();
            convertView = inflater.inflate(R.layout.activity_admin_vol_row, parent, false);
        }

        TextView name = convertView.findViewById(R.id.tvName);
        TextView email = convertView.findViewById(R.id.tvEmail);
        TextView status = convertView.findViewById(R.id.tvStatus);
        Button deleteBtn = convertView.findViewById(R.id.btnDelete);

        admin_vol_model model = list.get(position);

        name.setText(model.getName());
        email.setText(model.getEmail());
        status.setText(model.getStatus());

        deleteBtn.setOnClickListener(v -> {
            new AlertDialog.Builder(activity)
                    .setTitle("Delete Volunteer")
                    .setMessage("Are you sure you want to delete this volunteer?")
                    .setPositiveButton("Yes", (dialog, which) -> deleteVolunteer(model, position))
                    .setNegativeButton("No", null)
                    .show();
        });

        return convertView;
    }

    private void deleteVolunteer(admin_vol_model model, int position) {
        String url = MyIP.IP_ADDRESS + "user/deletevolunteer/" + model.getId();

        RequestQueue queue = Volley.newRequestQueue(activity);

        StringRequest request = new StringRequest(
                Request.Method.DELETE,
                url,
                response -> {
                    list.remove(position);
                    notifyDataSetChanged();
                    Toast.makeText(activity, "Volunteer deleted successfully", Toast.LENGTH_SHORT).show();
                },
                error -> {
                    error.printStackTrace();
                    Toast.makeText(activity, "Delete failed", Toast.LENGTH_SHORT).show();
                }
        );

        queue.add(request);
    }
}