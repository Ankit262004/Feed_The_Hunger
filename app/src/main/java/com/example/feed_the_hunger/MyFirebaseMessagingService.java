package com.example.feed_the_hunger;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Build;
import android.util.Log;

import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

public class MyFirebaseMessagingService extends FirebaseMessagingService {

    private static final String CHANNEL_ID = "food_upload_channel";
    private static final String TAG = "MyFCMService";

    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        super.onMessageReceived(remoteMessage);

        String title = "New Food Uploaded";
        String body = "A new food item is now available.";

        Log.d(TAG, "Message received");

        if (remoteMessage.getNotification() != null) {
            if (remoteMessage.getNotification().getTitle() != null) {
                title = remoteMessage.getNotification().getTitle();
            }
            if (remoteMessage.getNotification().getBody() != null) {
                body = remoteMessage.getNotification().getBody();
            }
        }

        if (remoteMessage.getData().size() > 0) {
            String dataTitle = remoteMessage.getData().get("title");
            String dataBody = remoteMessage.getData().get("body");

            if (dataTitle != null && !dataTitle.isEmpty()) {
                title = dataTitle;
            }
            if (dataBody != null && !dataBody.isEmpty()) {
                body = dataBody;
            }

            Log.d(TAG, "Data payload: " + remoteMessage.getData().toString());
        }

        showNotification(title, body);
    }

    @Override
    public void onNewToken(String token) {
        super.onNewToken(token);

        Log.d(TAG, "New FCM Token: " + token);

        saveTokenForAnyLoggedInUser(token);
    }

    private void saveTokenForAnyLoggedInUser(String token) {
        // 1) Volunteer session
        SharedPreferences volunteerPrefs = getSharedPreferences("VolunteerSession", MODE_PRIVATE);
        String volunteerId = volunteerPrefs.getString("userId", "");
        boolean volunteerLoggedIn = volunteerPrefs.getBoolean("isLoggedIn", false);

        if (volunteerLoggedIn && volunteerId != null && !volunteerId.isEmpty()) {
            Log.d(TAG, "Sending refreshed token for Volunteer userId: " + volunteerId);
            TokenManager.sendTokenToServer(getApplicationContext(), volunteerId, token);
            return;
        }

        // 2) User session
        SharedPreferences userPrefs = getSharedPreferences("UserSession", MODE_PRIVATE);
        String userId = userPrefs.getString("userId", "");
        boolean userLoggedIn = userPrefs.getBoolean("isLoggedIn", false);

        if (userLoggedIn && userId != null && !userId.isEmpty()) {
            Log.d(TAG, "Sending refreshed token for User userId: " + userId);
            TokenManager.sendTokenToServer(getApplicationContext(), userId, token);
            return;
        }

        // 3) Admin session if you use one
        SharedPreferences adminPrefs = getSharedPreferences("AdminSession", MODE_PRIVATE);
        String adminId = adminPrefs.getString("userId", "");
        boolean adminLoggedIn = adminPrefs.getBoolean("isLoggedIn", false);

        if (adminLoggedIn && adminId != null && !adminId.isEmpty()) {
            Log.d(TAG, "Sending refreshed token for Admin userId: " + adminId);
            TokenManager.sendTokenToServer(getApplicationContext(), adminId, token);
            return;
        }

        Log.d(TAG, "No logged-in user found in SharedPreferences, token not sent");
    }

    private void showNotification(String title, String body) {
        createNotificationChannel();

        Intent intent = new Intent(this, activity_splash.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);

        PendingIntent pendingIntent = PendingIntent.getActivity(
                this,
                0,
                intent,
                PendingIntent.FLAG_ONE_SHOT | PendingIntent.FLAG_IMMUTABLE
        );

        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setSmallIcon(R.mipmap.ic_launcher)
                .setContentTitle(title)
                .setContentText(body)
                .setStyle(new NotificationCompat.BigTextStyle().bigText(body))
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setAutoCancel(true)
                .setContentIntent(pendingIntent);

        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(this);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (checkSelfPermission(android.Manifest.permission.POST_NOTIFICATIONS)
                    != PackageManager.PERMISSION_GRANTED) {
                Log.d(TAG, "POST_NOTIFICATIONS permission not granted");
                return;
            }
        }

        notificationManager.notify((int) System.currentTimeMillis(), builder.build());
    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = "Food Upload Notifications";
            String description = "Notifies users when food is uploaded";
            int importance = NotificationManager.IMPORTANCE_HIGH;

            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, name, importance);
            channel.setDescription(description);

            NotificationManager manager = getSystemService(NotificationManager.class);
            if (manager != null) {
                manager.createNotificationChannel(channel);
            }
        }
    }
}