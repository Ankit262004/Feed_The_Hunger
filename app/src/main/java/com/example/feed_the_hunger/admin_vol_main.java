package com.example.feed_the_hunger;

import android.os.Bundle;
import android.util.Log;
import android.widget.ListView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;

public class admin_vol_main extends AppCompatActivity {

    ListView listView;
    ArrayList<admin_vol_model> list;
    admin_vol_adapter adapter;
    RequestQueue requestQueue;

    String URL = MyIP.IP_ADDRESS + "user/getallvolunteers";

    private static final String TAG = "admin_vol_main";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_vol_main);

        listView = findViewById(R.id.listView);

        list = new ArrayList<>();
        adapter = new admin_vol_adapter(this, list);
        listView.setAdapter(adapter);

        requestQueue = Volley.newRequestQueue(this);

        loadVolunteers();
    }

    private void loadVolunteers() {

        JsonObjectRequest request = new JsonObjectRequest(
                Request.Method.GET,
                URL,
                null,
                response -> {
                    list.clear();

                    Log.d(TAG, "Response: " + response.toString());

                    try {
                        JSONArray volunteersArray = response.getJSONArray("volunteers");

                        for (int i = 0; i < volunteersArray.length(); i++) {
                            JSONObject obj = volunteersArray.getJSONObject(i);

                            String id = obj.has("_id") ? obj.getString("_id") : "";
                            String name = obj.has("fullName") ? obj.getString("fullName") : "";
                            String email = obj.has("email") ? obj.getString("email") : "";
                            String status = obj.has("userType") ? obj.getString("userType") : "volunteer";

                            list.add(new admin_vol_model(id, name, email, status));
                        }

                        adapter.notifyDataSetChanged();

                        if (list.size() == 0) {
                            Toast.makeText(admin_vol_main.this, "No volunteers found", Toast.LENGTH_SHORT).show();
                        }

                    } catch (Exception e) {
                        Log.e(TAG, "JSON Error: " + e.getMessage());
                        e.printStackTrace();
                        Toast.makeText(admin_vol_main.this, "Parsing error", Toast.LENGTH_SHORT).show();
                    }
                },
                error -> {
                    Log.e(TAG, "Volley Error: " + error.toString());
                    error.printStackTrace();
                    Toast.makeText(admin_vol_main.this, "Failed to load volunteers", Toast.LENGTH_SHORT).show();
                }
        );

        requestQueue.add(request);
    }
}