package com.example.feed_the_hunger;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ListView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONObject;

import java.util.ArrayList;

public class admin_food_main extends AppCompatActivity {

    private ListView listView;
    private ArrayList<admin_food_model> foodList;
    private admin_food_adapter adapter;
    private RequestQueue requestQueue;

    private static final String TAG = "admin_food_main";
    private static final String BASE_URL = MyIP.IP_ADDRESS + "food";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_admin_food_main);

        setupWindowInsets();
        initViews();
        setupList();
        fetchFoodData();
    }

    private void setupWindowInsets() {
        View mainView = findViewById(R.id.main);
        if (mainView != null) {
            ViewCompat.setOnApplyWindowInsetsListener(mainView, (v, insets) -> {
                Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
                v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
                return insets;
            });
        }
    }

    private void initViews() {
        listView = findViewById(R.id.listView);
        requestQueue = Volley.newRequestQueue(this);
    }

    private void setupList() {
        foodList = new ArrayList<>();
        adapter = new admin_food_adapter(this, foodList);
        listView.setAdapter(adapter);
    }

    private void fetchFoodData() {
        Log.d(TAG, "Fetching food data from: " + BASE_URL);

        JsonArrayRequest request = new JsonArrayRequest(
                Request.Method.GET,
                BASE_URL,
                null,
                response -> {
                    Log.d(TAG, "Response: " + response.toString());

                    foodList.clear();

                    try {
                        for (int i = 0; i < response.length(); i++) {
                            JSONObject obj = response.getJSONObject(i);

                            double latitude = obj.optDouble("latitude", 0.0);
                            double longitude = obj.optDouble("longitude", 0.0);

                            admin_food_model item = new admin_food_model(
                                    obj.optString("_id", ""),
                                    obj.optString("foodName", "N/A"),
                                    obj.optString("description", "No Description"),
                                    obj.optInt("quantity", 0),
                                    obj.optString("expiryDate", "N/A"),
                                    obj.optString("foodType", "N/A"),
                                    obj.optString("image", ""),
                                    obj.optString("location", "N/A"),
                                    obj.optString("status", "Pending"),
                                    latitude,
                                    longitude
                            );

                            foodList.add(item);
                        }

                        adapter.notifyDataSetChanged();

                        if (foodList.isEmpty()) {
                            Toast.makeText(admin_food_main.this, "No food items found", Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(admin_food_main.this, "Loaded " + foodList.size() + " items", Toast.LENGTH_SHORT).show();
                        }

                    } catch (Exception e) {
                        Log.e(TAG, "Parsing error: " + e.getMessage(), e);
                        Toast.makeText(admin_food_main.this, "Parsing Error!", Toast.LENGTH_SHORT).show();
                    }
                },
                error -> {
                    String errorMessage = "API Error! Check server";

                    if (error.networkResponse != null) {
                        errorMessage = "Error Code: " + error.networkResponse.statusCode;
                    }

                    Log.e(TAG, "API Error: " + errorMessage, error);
                    Toast.makeText(admin_food_main.this, errorMessage, Toast.LENGTH_LONG).show();
                }
        );

        request.setTag(TAG);
        requestQueue.add(request);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (requestQueue != null) {
            requestQueue.cancelAll(TAG);
        }
    }
}