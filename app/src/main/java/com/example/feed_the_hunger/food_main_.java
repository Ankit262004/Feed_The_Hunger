package com.example.feed_the_hunger;

import android.os.Bundle;
import android.util.Log;
import android.widget.ListView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONObject;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;

public class food_main_ extends AppCompatActivity {

    ListView listView;
    ArrayList<food_model> foodList;
    foodadapter adapter;
    RequestQueue requestQueue;

    String BASE_URL = MyIP.IP_ADDRESS + "food";

    private static final String TAG = "food_main_";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_food_main);

        setupWindowInsets();
        initViews();
        setupList();
        fetchFoodData();
    }

    private void setupWindowInsets() {
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    }

    private void initViews() {
        listView = findViewById(R.id.listView);
        requestQueue = Volley.newRequestQueue(this);
    }

    private void setupList() {
        foodList = new ArrayList<>();
        adapter = new foodadapter(this, foodList);
        listView.setAdapter(adapter);
    }

    private void fetchFoodData() {

        Log.d(TAG, "Fetching: " + BASE_URL);

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

                            String imageUrl = obj.optString("image", "");

                            // 🔥 NEW (IMPORTANT)
                            double latitude = obj.optDouble("latitude", 0.0);
                            double longitude = obj.optDouble("longitude", 0.0);

                            food_model item = new food_model(
                                    obj.optString("_id", ""),
                                    obj.optString("foodName", "N/A"),
                                    obj.optString("description", "No Description"),
                                    obj.optInt("quantity", 0),
                                    obj.optString("expiryDate", "N/A"),
                                    obj.optString("foodType", "N/A"),
                                    imageUrl,
                                    obj.optString("location", "N/A"),
                                    obj.optString("status", "pending"),
                                    latitude,          // 🔥 ADDED
                                    longitude          // 🔥 ADDED
                            );

                            foodList.add(item);
                        }

                        adapter.notifyDataSetChanged();

                        Toast.makeText(
                                food_main_.this,
                                "Loaded " + foodList.size() + " items",
                                Toast.LENGTH_SHORT
                        ).show();

                    } catch (Exception e) {
                        Log.e(TAG, "Parsing Error", e);
                        Toast.makeText(
                                food_main_.this,
                                "Parsing Error: " + e.getMessage(),
                                Toast.LENGTH_LONG
                        ).show();
                    }
                },
                this::handleVolleyError
        );

        requestQueue.add(request);
    }

    private void handleVolleyError(VolleyError error) {
        Log.e(TAG, "API ERROR: ", error);

        String errorMessage = "API Error! Check backend";

        if (error.networkResponse != null) {
            int statusCode = error.networkResponse.statusCode;
            Log.e(TAG, "Status Code: " + statusCode);

            try {
                String responseBody = new String(error.networkResponse.data, StandardCharsets.UTF_8);
                Log.e(TAG, "Error Body: " + responseBody);
                errorMessage = "Error " + statusCode + ": " + responseBody;
            } catch (Exception e) {
                Log.e(TAG, "Error reading response body", e);
                errorMessage = "Error " + statusCode;
            }
        } else if (error.getMessage() != null) {
            errorMessage = error.getMessage();
        }

        Toast.makeText(this, errorMessage, Toast.LENGTH_LONG).show();
    }
}