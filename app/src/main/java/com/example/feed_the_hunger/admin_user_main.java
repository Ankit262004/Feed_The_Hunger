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

public class admin_user_main extends AppCompatActivity {

    ListView listView;
    ArrayList<admin_user_model> list;
    admin_user_adapter adapter;
    RequestQueue requestQueue;

    String URL = MyIP.IP_ADDRESS + "user/getallusers";

    private static final String TAG = "admin_user_main";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_user_main);

        listView = findViewById(R.id.listView);

        list = new ArrayList<>();
        adapter = new admin_user_adapter(this, list);
        listView.setAdapter(adapter);

        requestQueue = Volley.newRequestQueue(this);

        loadUsers();
    }

    private void loadUsers() {

        JsonObjectRequest request = new JsonObjectRequest(
                com.android.volley.Request.Method.GET,
                URL,
                null,
                response -> {

                    list.clear();

                    try {
                        Log.d(TAG, "Response: " + response.toString());

                        JSONArray usersArray = response.getJSONArray("users");

                        for (int i = 0; i < usersArray.length(); i++) {

                            JSONObject obj = usersArray.getJSONObject(i);

                            String id = obj.has("_id") ? obj.getString("_id") : "";
                            String name = obj.has("fullName") ? obj.getString("fullName") : "";
                            String email = obj.has("email") ? obj.getString("email") : "";
                            String userType = obj.has("userType") ? obj.getString("userType") : "";

                            // 🚫 Skip volunteers
                            if (userType.equalsIgnoreCase("volunteer")) {
                                continue;
                            }

                            // 🚫 Skip admin
                            if (userType.equalsIgnoreCase("admin")) {
                                continue;
                            }

                            list.add(new admin_user_model(id, name, email, userType));
                        }

                        adapter.notifyDataSetChanged();

                        if (list.size() == 0) {
                            Toast.makeText(admin_user_main.this, "No users found", Toast.LENGTH_SHORT).show();
                        }

                    } catch (Exception e) {
                        Log.e(TAG, "JSON Error: " + e.getMessage());
                        Toast.makeText(admin_user_main.this, "Parsing error", Toast.LENGTH_SHORT).show();
                    }

                },
                error -> {
                    Log.e(TAG, "Volley Error: " + error.toString());
                    Toast.makeText(admin_user_main.this, "Failed to load users", Toast.LENGTH_SHORT).show();
                }
        );

        requestQueue.add(request);
    }
}