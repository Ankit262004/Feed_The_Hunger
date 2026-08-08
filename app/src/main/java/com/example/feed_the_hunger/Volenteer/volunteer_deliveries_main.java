package com.example.feed_the_hunger.Volenteer;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.widget.ListView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.Volley;
import com.example.feed_the_hunger.MyIP;
import com.example.feed_the_hunger.R;

import org.json.JSONObject;

import java.util.ArrayList;

public class volunteer_deliveries_main extends AppCompatActivity {

    ListView listView;
    ArrayList<volunteer_delivery_model> list;
    volunteer_delivery_adapter adapter;
    RequestQueue requestQueue;

    private static final String TAG = "volunteer_deliveries";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_volunteer_deliveries_main);

        listView = findViewById(R.id.listViewDeliveries);

        list = new ArrayList<>();
        adapter = new volunteer_delivery_adapter(this, list);
        listView.setAdapter(adapter);

        requestQueue = Volley.newRequestQueue(this);

        loadAcceptedDeliveries();
    }

    private void loadAcceptedDeliveries() {
        SharedPreferences prefs = getSharedPreferences("VolunteerSession", MODE_PRIVATE);

        String volunteerId = prefs.getString("userId", "");
        String fullName = prefs.getString("fullName", "");

        Log.d(TAG, "Volunteer ID from session: " + volunteerId);
        Log.d(TAG, "Volunteer Name from session: " + fullName);

        if (volunteerId == null || volunteerId.trim().isEmpty()) {
            Toast.makeText(this, "Volunteer session not found", Toast.LENGTH_SHORT).show();
            Log.e(TAG, "Volunteer ID missing in SharedPreferences");
            return;
        }

        String url = MyIP.IP_ADDRESS + "food/accepted/" + volunteerId;
        Log.d(TAG, "Request URL: " + url);

        JsonArrayRequest request = new JsonArrayRequest(
                Request.Method.GET,
                url,
                null,
                response -> {
                    list.clear();
                    Log.d(TAG, "Response received: " + response.toString());

                    try {
                        for (int i = 0; i < response.length(); i++) {
                            JSONObject obj = response.getJSONObject(i);

                            String id = obj.optString("_id");
                            String foodName = obj.optString("foodName");
                            String description = obj.optString("description");
                            String quantity = obj.optString("quantity");
                            String expiryDate = obj.optString("expiryDate");
                            String foodType = obj.optString("foodType");
                            String image = obj.optString("image");
                            String location = obj.optString("location");
                            String status = obj.optString("status");

                            list.add(new volunteer_delivery_model(
                                    id,
                                    foodName,
                                    description,
                                    quantity,
                                    expiryDate,
                                    foodType,
                                    image,
                                    location,
                                    status
                            ));
                        }

                        adapter.notifyDataSetChanged();

                        if (list.isEmpty()) {
                            Toast.makeText(this, "No accepted deliveries found", Toast.LENGTH_SHORT).show();
                            Log.d(TAG, "No accepted deliveries returned from backend");
                        } else {
                            Log.d(TAG, "Accepted deliveries count: " + list.size());
                        }

                    } catch (Exception e) {
                        Log.e(TAG, "Parse Error: " + e.getMessage(), e);
                        Toast.makeText(this, "Data parsing error", Toast.LENGTH_SHORT).show();
                    }
                },
                error -> {
                    if (error.networkResponse != null) {
                        Log.e(TAG, "Status Code: " + error.networkResponse.statusCode);

                        try {
                            String errorData = new String(error.networkResponse.data);
                            Log.e(TAG, "Error Response: " + errorData);
                        } catch (Exception e) {
                            Log.e(TAG, "Error reading error response", e);
                        }
                    } else {
                        Log.e(TAG, "Volley Error: " + error.toString(), error);
                    }

                    Toast.makeText(this, "Failed to load accepted deliveries", Toast.LENGTH_SHORT).show();
                }
        );

        requestQueue.add(request);
    }
}