package com.example.feed_the_hunger;

import android.content.Context;
import android.util.Log;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONObject;

public class TokenManager {

    private static final String TAG = "TokenManager";

    public static void sendTokenToServer(Context context, String userId, String token) {
        String url = MyIP.IP_ADDRESS + "user/save-token";

        try {
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("userId", userId);
            jsonObject.put("fcmToken", token);

            Log.d(TAG, "TOKEN_URL: " + url);
            Log.d(TAG, "TOKEN_BODY: " + jsonObject.toString());

            JsonObjectRequest request = new JsonObjectRequest(
                    Request.Method.POST,
                    url,
                    jsonObject,
                    response -> Log.d(TAG, "SUCCESS: " + response.toString()),
                    error -> {
                        String message = "Unknown error";

                        if (error.networkResponse != null) {
                            message = "Code: " + error.networkResponse.statusCode;

                            if (error.networkResponse.data != null) {
                                message += " | " + new String(error.networkResponse.data);
                            }
                        } else if (error.getCause() != null) {
                            message = error.getCause().toString();
                        }

                        Log.e(TAG, "FAILED: " + message, error);
                    }
            );

            RequestQueue queue = Volley.newRequestQueue(context.getApplicationContext());
            queue.add(request);

        } catch (Exception e) {
            Log.e(TAG, "JSON ERROR", e);
        }
    }
}