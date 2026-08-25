package com.example.feed_the_hunger.User;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.example.feed_the_hunger.MapPickerActivity;
import com.example.feed_the_hunger.MyIP;
import com.example.feed_the_hunger.R;
import com.example.feed_the_hunger.TokenManager;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.Priority;
import com.google.android.gms.tasks.CancellationTokenSource;
import com.google.firebase.messaging.FirebaseMessaging;

import org.json.JSONObject;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class User_Registration extends AppCompatActivity {

    // =========================================================
    // LOCATION PERMISSION
    // =========================================================

    private static final int LOCATION_PERMISSION_REQUEST = 2001;

    // =========================================================
    // MAP REQUEST
    // =========================================================

    private static final int MAP_LOCATION_REQUEST = 2002;

    // =========================================================
    // VIEWS
    // =========================================================

    private EditText fname;
    private EditText em;
    private EditText pass;
    private EditText cpass;
    private EditText loc;

    private Spinner spin;

    private Button reg;
    private Button btnCurrentLocation;
    private Button btnSelectMap;

    private TextView log;

    private ProgressDialog progressDialog;

    // =========================================================
    // LOCATION CLIENT
    // =========================================================

    private FusedLocationProviderClient fusedLocationClient;

    // =========================================================
    // SELECTED LOCATION
    // =========================================================

    private double selectedLatitude = 0.0;
    private double selectedLongitude = 0.0;

    // =========================================================
    // ON CREATE
    // =========================================================

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        EdgeToEdge.enable(this);

        setContentView(R.layout.activity_user_registration);

        // =====================================================
        // EDGE TO EDGE
        // =====================================================

        ViewCompat.setOnApplyWindowInsetsListener(
                findViewById(R.id.main),
                (v, insets) -> {

                    Insets systemBars =
                            insets.getInsets(
                                    WindowInsetsCompat.Type.systemBars()
                            );

                    v.setPadding(
                            systemBars.left,
                            systemBars.top,
                            systemBars.right,
                            systemBars.bottom
                    );

                    return insets;
                }
        );

        // =====================================================
        // INITIALIZE VIEWS
        // =====================================================

        fname = findViewById(R.id.fname);
        em = findViewById(R.id.em);
        pass = findViewById(R.id.pass);
        cpass = findViewById(R.id.cpass);
        loc = findViewById(R.id.loc);

        spin = findViewById(R.id.spin);

        reg = findViewById(R.id.reg);
        log = findViewById(R.id.log);

        btnCurrentLocation =
                findViewById(R.id.btnCurrentLocation);

        btnSelectMap =
                findViewById(R.id.btnSelectMap);

        // =====================================================
        // LOCATION CLIENT
        // =====================================================

        fusedLocationClient =
                LocationServices
                        .getFusedLocationProviderClient(this);

        // =====================================================
        // PROGRESS DIALOG
        // =====================================================

        progressDialog =
                new ProgressDialog(this);

        progressDialog.setMessage("Registering...");

        progressDialog.setCancelable(false);

        // =====================================================
        // SPINNER
        // =====================================================

        ArrayAdapter<CharSequence> adapter =
                ArrayAdapter.createFromResource(
                        this,
                        R.array.usertype,
                        android.R.layout.simple_spinner_item
                );

        adapter.setDropDownViewResource(
                android.R.layout.simple_spinner_dropdown_item
        );

        spin.setAdapter(adapter);

        // =====================================================
        // CURRENT LOCATION
        // =====================================================

        btnCurrentLocation.setOnClickListener(
                v -> requestLocationPermission()
        );

        // =====================================================
        // SELECT ON MAP
        // =====================================================

        btnSelectMap.setOnClickListener(
                v -> openMapPicker()
        );

        // =====================================================
        // REGISTER
        // =====================================================

        reg.setOnClickListener(v -> {

            String fullName =
                    fname.getText()
                            .toString()
                            .trim();

            String email =
                    em.getText()
                            .toString()
                            .trim()
                            .toLowerCase();

            String password =
                    pass.getText()
                            .toString()
                            .trim();

            String confirmPassword =
                    cpass.getText()
                            .toString()
                            .trim();

            String location =
                    loc.getText()
                            .toString()
                            .trim();

            String userType =
                    spin.getSelectedItem()
                            .toString()
                            .trim()
                            .toLowerCase();

            // =================================================
            // EMPTY FIELDS
            // =================================================

            if (fullName.isEmpty()
                    || email.isEmpty()
                    || password.isEmpty()
                    || confirmPassword.isEmpty()
                    || location.isEmpty()) {

                Toast.makeText(
                        this,
                        "Please fill all fields",
                        Toast.LENGTH_SHORT
                ).show();

                return;
            }

            // =================================================
            // LOCATION CHECK
            // =================================================

            if (selectedLatitude == 0.0
                    && selectedLongitude == 0.0) {

                Toast.makeText(
                        this,
                        "Please select your location",
                        Toast.LENGTH_SHORT
                ).show();

                return;
            }

            // =================================================
            // PASSWORD MATCH
            // =================================================

            if (!password.equals(confirmPassword)) {

                Toast.makeText(
                        this,
                        "Passwords do not match",
                        Toast.LENGTH_SHORT
                ).show();

                return;
            }

            // =================================================
            // USER TYPE
            // =================================================

            if (!userType.equals("donor")
                    && !userType.equals("receiver")) {

                Toast.makeText(
                        this,
                        "Please select donor or receiver",
                        Toast.LENGTH_SHORT
                ).show();

                return;
            }

            // =================================================
            // PASSWORD LENGTH
            // =================================================

            if (password.length() < 6) {

                Toast.makeText(
                        this,
                        "Password must be at least 6 characters",
                        Toast.LENGTH_SHORT
                ).show();

                return;
            }

            // =================================================
            // EMAIL VALIDATION
            // =================================================

            if (!android.util.Patterns.EMAIL_ADDRESS
                    .matcher(email)
                    .matches()) {

                Toast.makeText(
                        this,
                        "Enter valid email",
                        Toast.LENGTH_SHORT
                ).show();

                return;
            }

            // =================================================
            // REGISTER
            // =================================================

            registerUser(
                    fullName,
                    email,
                    password,
                    location,
                    userType,
                    selectedLatitude,
                    selectedLongitude
            );

        });

        // =====================================================
        // LOGIN
        // =====================================================

        log.setOnClickListener(v -> {

            startActivity(
                    new Intent(
                            this,
                            User_Login.class
                    )
            );

            finish();

        });
    }

    // =========================================================
    // LOCATION PERMISSION
    // =========================================================

    private void requestLocationPermission() {

        if (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
        ) != PackageManager.PERMISSION_GRANTED
                &&
                ContextCompat.checkSelfPermission(
                        this,
                        Manifest.permission.ACCESS_COARSE_LOCATION
                ) != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(
                    this,
                    new String[]{
                            Manifest.permission.ACCESS_FINE_LOCATION,
                            Manifest.permission.ACCESS_COARSE_LOCATION
                    },
                    LOCATION_PERMISSION_REQUEST
            );

            return;
        }

        getCurrentLocation();
    }

    // =========================================================
    // GET CURRENT LOCATION
    // =========================================================

    private void getCurrentLocation() {

        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
        ) != PackageManager.PERMISSION_GRANTED
                &&
                ActivityCompat.checkSelfPermission(
                        this,
                        Manifest.permission.ACCESS_COARSE_LOCATION
                ) != PackageManager.PERMISSION_GRANTED) {

            return;
        }

        Toast.makeText(
                this,
                "Getting current location...",
                Toast.LENGTH_SHORT
        ).show();

        CancellationTokenSource cancellationTokenSource =
                new CancellationTokenSource();

        fusedLocationClient
                .getCurrentLocation(
                        Priority.PRIORITY_HIGH_ACCURACY,
                        cancellationTokenSource.getToken()
                )
                .addOnSuccessListener(location -> {

                    if (location != null) {

                        setSelectedLocation(
                                location.getLatitude(),
                                location.getLongitude()
                        );

                    } else {

                        Toast.makeText(
                                this,
                                "Unable to get current location",
                                Toast.LENGTH_LONG
                        ).show();
                    }

                })
                .addOnFailureListener(e -> {

                    Log.e(
                            "CURRENT_LOCATION",
                            "Location error",
                            e
                    );

                    Toast.makeText(
                            this,
                            "Unable to get current location",
                            Toast.LENGTH_LONG
                    ).show();

                });
    }

    // =========================================================
    // OPEN MAP PICKER
    // =========================================================

    private void openMapPicker() {

        Intent intent =
                new Intent(
                        User_Registration.this,
                        MapPickerActivity.class
                );

        // =====================================================
        // SEND PREVIOUS LOCATION
        // =====================================================

        if (selectedLatitude != 0.0
                || selectedLongitude != 0.0) {

            intent.putExtra(
                    "latitude",
                    selectedLatitude
            );

            intent.putExtra(
                    "longitude",
                    selectedLongitude
            );
        }

        startActivityForResult(
                intent,
                MAP_LOCATION_REQUEST
        );
    }

    // =========================================================
    // RECEIVE MAP LOCATION
    // =========================================================

    @Override
    protected void onActivityResult(
            int requestCode,
            int resultCode,
            Intent data
    ) {

        super.onActivityResult(
                requestCode,
                resultCode,
                data
        );

        if (requestCode == MAP_LOCATION_REQUEST
                && resultCode == RESULT_OK
                && data != null) {

            double latitude =
                    data.getDoubleExtra(
                            "latitude",
                            0.0
                    );

            double longitude =
                    data.getDoubleExtra(
                            "longitude",
                            0.0
                    );

            if (latitude != 0.0
                    || longitude != 0.0) {

                setSelectedLocation(
                        latitude,
                        longitude
                );

            }
        }
    }

    // =========================================================
    // SET SELECTED LOCATION
    // =========================================================

    private void setSelectedLocation(
            double latitude,
            double longitude
    ) {

        selectedLatitude = latitude;
        selectedLongitude = longitude;

        Log.d(
                "SELECTED_LOCATION",
                "Latitude: " + latitude
                        + " Longitude: " + longitude
        );

        // =====================================================
        // IMPORTANT:
        // Convert GPS coordinates into real address
        // =====================================================

        getAddressFromCoordinates(
                latitude,
                longitude
        );
    }

    // =========================================================
    // REVERSE GEOCODING
    // GPS -> REAL ADDRESS
    // =========================================================

    private void getAddressFromCoordinates(
            double latitude,
            double longitude
    ) {

        Toast.makeText(
                this,
                "Finding address...",
                Toast.LENGTH_SHORT
        ).show();

        Geocoder geocoder =
                new Geocoder(
                        this,
                        Locale.getDefault()
                );

        // =====================================================
        // ANDROID NEWER GEOCODER API
        // =====================================================

        if (android.os.Build.VERSION.SDK_INT >=
                android.os.Build.VERSION_CODES.TIRAMISU) {

            geocoder.getFromLocation(
                    latitude,
                    longitude,
                    1,
                    new Geocoder.GeocodeListener() {

                        @Override
                        public void onGeocode(
                                List<Address> addresses
                        ) {

                            runOnUiThread(() ->
                                    setAddressText(addresses)
                            );
                        }

                        @Override
                        public void onError(
                                String errorMessage
                        ) {

                            runOnUiThread(() -> {

                                Log.e(
                                        "GEOCODER",
                                        "Error: "
                                                + errorMessage
                                );

                                showCoordinatesAsFallback(
                                        latitude,
                                        longitude
                                );
                            });
                        }
                    }
            );

        } else {

            // =================================================
            // OLDER ANDROID DEVICES
            // =================================================

            new Thread(() -> {

                try {

                    List<Address> addresses =
                            geocoder.getFromLocation(
                                    latitude,
                                    longitude,
                                    1
                            );

                    runOnUiThread(() ->
                            setAddressText(addresses)
                    );

                } catch (IOException e) {

                    Log.e(
                            "GEOCODER",
                            "Geocoder error",
                            e
                    );

                    runOnUiThread(() ->
                            showCoordinatesAsFallback(
                                    latitude,
                                    longitude
                            )
                    );
                }

            }).start();
        }
    }

    // =========================================================
    // SET ADDRESS TEXT
    // =========================================================

    private void setAddressText(
            List<Address> addresses
    ) {

        if (addresses != null
                && !addresses.isEmpty()) {

            Address address =
                    addresses.get(0);

            String addressText =
                    address.getAddressLine(0);

            if (addressText != null
                    && !addressText.trim().isEmpty()) {

                loc.setText(
                        addressText.trim()
                );

                Toast.makeText(
                        this,
                        "Location selected",
                        Toast.LENGTH_SHORT
                ).show();

                Log.d(
                        "ADDRESS",
                        addressText
                );

                return;
            }
        }

        Toast.makeText(
                this,
                "Address not found",
                Toast.LENGTH_LONG
        ).show();

        showCoordinatesAsFallback(
                selectedLatitude,
                selectedLongitude
        );
    }

    // =========================================================
    // FALLBACK
    // =========================================================

    private void showCoordinatesAsFallback(
            double latitude,
            double longitude
    ) {

        /*
         * Normally the address should be found.
         *
         * If Android's Geocoder cannot find an address,
         * keep coordinates internally but don't replace
         * the location field with coordinates.
         */

        loc.setText(
                "Selected location"
        );

        Log.d(
                "LOCATION_FALLBACK",
                "Coordinates: "
                        + latitude
                        + ", "
                        + longitude
        );

        Toast.makeText(
                this,
                "Address could not be detected. Location coordinates saved.",
                Toast.LENGTH_LONG
        ).show();
    }

    // =========================================================
    // PERMISSION RESULT
    // =========================================================

    @Override
    public void onRequestPermissionsResult(
            int requestCode,
            @NonNull String[] permissions,
            @NonNull int[] grantResults
    ) {

        super.onRequestPermissionsResult(
                requestCode,
                permissions,
                grantResults
        );

        if (requestCode ==
                LOCATION_PERMISSION_REQUEST) {

            boolean granted = false;

            for (int result : grantResults) {

                if (result ==
                        PackageManager.PERMISSION_GRANTED) {

                    granted = true;

                    break;
                }
            }

            if (granted) {

                getCurrentLocation();

            } else {

                Toast.makeText(
                        this,
                        "Location permission denied",
                        Toast.LENGTH_LONG
                ).show();
            }
        }
    }

    // =========================================================
    // REGISTER USER
    // =========================================================

    private void registerUser(
            String fullName,
            String email,
            String password,
            String location,
            String userType,
            double latitude,
            double longitude
    ) {

        progressDialog.show();

        String url =
                MyIP.IP_ADDRESS
                        + "user/registeruser";

        Log.d(
                "REGISTER_URL",
                url
        );

        StringRequest request =
                new StringRequest(
                        Request.Method.POST,

                        url,

                        response -> {

                            Log.d(
                                    "REGISTER_SUCCESS",
                                    response
                            );

                            try {

                                JSONObject jsonObject =
                                        new JSONObject(response);

                                boolean success =
                                        jsonObject.optBoolean(
                                                "success",
                                                false
                                        );

                                String message =
                                        jsonObject.optString(
                                                "message",
                                                "Registration completed"
                                        );

                                if (!success) {

                                    progressDialog.dismiss();

                                    Toast.makeText(
                                            this,
                                            message,
                                            Toast.LENGTH_LONG
                                    ).show();

                                    return;
                                }

                                JSONObject userObj =
                                        jsonObject.getJSONObject(
                                                "user"
                                        );

                                // =================================
                                // SAVE USER ID
                                // =================================

                                String userId =
                                        userObj.getString(
                                                "_id"
                                        );

                                SharedPreferences prefs =
                                        getSharedPreferences(
                                                "USER_PREF",
                                                MODE_PRIVATE
                                        );

                                prefs.edit()
                                        .putString(
                                                "userId",
                                                userId
                                        )
                                        .apply();

                                Log.d(
                                        "USER_ID_SAVED",
                                        userId
                                );

                                // =================================
                                // GET FCM TOKEN
                                // =================================

                                FirebaseMessaging
                                        .getInstance()
                                        .getToken()
                                        .addOnCompleteListener(
                                                task -> {

                                                    progressDialog.dismiss();

                                                    if (!task.isSuccessful()) {

                                                        Log.e(
                                                                "FCM_TOKEN",
                                                                "Failed",
                                                                task.getException()
                                                        );

                                                        Toast.makeText(
                                                                this,
                                                                message
                                                                        + " (token not generated yet)",
                                                                Toast.LENGTH_LONG
                                                        ).show();

                                                        startActivity(
                                                                new Intent(
                                                                        this,
                                                                        User_Login.class
                                                                )
                                                        );

                                                        finish();

                                                        return;
                                                    }

                                                    String token =
                                                            task.getResult();

                                                    Log.d(
                                                            "FCM_TOKEN",
                                                            token
                                                    );

                                                    // =================================
                                                    // SAVE FCM TOKEN
                                                    // =================================

                                                    TokenManager
                                                            .sendTokenToServer(
                                                                    getApplicationContext(),
                                                                    userId,
                                                                    token
                                                            );

                                                    Toast.makeText(
                                                            this,
                                                            message,
                                                            Toast.LENGTH_LONG
                                                    ).show();

                                                    // =================================
                                                    // GO TO LOGIN
                                                    // =================================

                                                    new Handler(
                                                            Looper.getMainLooper()
                                                    ).postDelayed(
                                                            () -> {

                                                                startActivity(
                                                                        new Intent(
                                                                                this,
                                                                                User_Login.class
                                                                        )
                                                                );

                                                                finish();

                                                            },
                                                            1000
                                                    );
                                                }
                                        );

                            } catch (Exception e) {

                                progressDialog.dismiss();

                                Log.e(
                                        "REGISTER_PARSE_ERROR",
                                        "Parse error",
                                        e
                                );

                                Toast.makeText(
                                        this,
                                        "Response parsing error: "
                                                + e.getMessage(),
                                        Toast.LENGTH_LONG
                                ).show();
                            }
                        },

                        error -> {

                            progressDialog.dismiss();

                            String message =
                                    "Registration failed";

                            try {

                                if (error.networkResponse != null
                                        &&
                                        error.networkResponse.data != null) {

                                    String responseData =
                                            new String(
                                                    error.networkResponse.data
                                            );

                                    Log.e(
                                            "REGISTER_ERROR",
                                            responseData
                                    );

                                    try {

                                        JSONObject errorObj =
                                                new JSONObject(
                                                        responseData
                                                );

                                        message =
                                                errorObj.optString(
                                                        "message",
                                                        message
                                                );

                                    } catch (Exception jsonError) {

                                        message =
                                                responseData;
                                    }

                                } else if (
                                        error.getCause() != null) {

                                    message =
                                            error.getCause()
                                                    .toString();

                                } else {

                                    message =
                                            error.toString();
                                }

                            } catch (Exception e) {

                                Log.e(
                                        "REGISTER_ERROR",
                                        "Error reading response",
                                        e
                                );
                            }

                            Toast.makeText(
                                    this,
                                    message,
                                    Toast.LENGTH_LONG
                            ).show();
                        }
                ) {

                    @Override
                    protected Map<String, String>
                    getParams() {

                        Map<String, String> params =
                                new HashMap<>();

                        params.put(
                                "fullName",
                                fullName
                        );

                        params.put(
                                "email",
                                email
                        );

                        params.put(
                                "password",
                                password
                        );

                        params.put(
                                "location",
                                location
                        );

                        params.put(
                                "userType",
                                userType
                        );
                        // GPS COORDINATES
                        params.put(
                                "latitude",
                                String.valueOf(
                                        latitude
                                )
                        );

                        params.put(
                                "longitude",
                                String.valueOf(
                                        longitude
                                )
                        );

                        Log.d(
                                "REGISTER_PARAMS",
                                params.toString()
                        );

                        return params;
                    }
                };

        RequestQueue queue =
                Volley.newRequestQueue(this);

        queue.add(request);
    }
}