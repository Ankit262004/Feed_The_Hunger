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
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.Volley;
import com.example.feed_the_hunger.Volenteer.reqadapter;

import org.json.JSONObject;

import java.util.ArrayList;

public class vol_req extends AppCompatActivity {

    ListView listView;
    ArrayList<food_model> foodList;
    reqadapter adapter;
    RequestQueue requestQueue;

    String BASE_URL = MyIP.IP_ADDRESS + "food";

    private static final String TAG = "vol_req";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_vol_req);

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
        adapter = new reqadapter(this, foodList);
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

                            food_model item = new food_model(
                                    obj.optString("_id", ""),
                                    obj.optString("foodName", "N/A"),
                                    obj.optString("description", "No Description"),
                                    obj.optInt("quantity", 0),
                                    obj.optString("expiryDate", "N/A"),
                                    obj.optString("foodType", "N/A"),
                                    obj.optString("image", ""),
                                    obj.optString("location", "N/A"),
                                    obj.optString("status", "N/A"),
                                    latitude,
                                    longitude
                            );

                            foodList.add(item);
                        }

                        adapter.notifyDataSetChanged();

                        Toast.makeText(this,
                                "Loaded " + foodList.size() + " items",
                                Toast.LENGTH_SHORT).show();

                    } catch (Exception e) {
                        Log.e(TAG, "Parsing error", e);
                        Toast.makeText(this, "Parsing Error! " + e.getMessage(), Toast.LENGTH_LONG).show();
                    }
                },
                error -> {
                    Log.e(TAG, "API Error", error);
                    Toast.makeText(this,
                            "API Error! Check server",
                            Toast.LENGTH_LONG).show();
                }
        );

        requestQueue.add(request);
    }
}