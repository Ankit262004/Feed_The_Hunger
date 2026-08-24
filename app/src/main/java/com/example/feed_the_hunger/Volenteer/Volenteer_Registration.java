package com.example.feed_the_hunger.Volenteer;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.example.feed_the_hunger.MapPickerActivity;
import com.example.feed_the_hunger.MyIP;
import com.example.feed_the_hunger.R;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.Priority;
import com.google.android.gms.tasks.CancellationTokenSource;
import com.google.firebase.messaging.FirebaseMessaging;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class Volenteer_Registration extends AppCompatActivity {

    // =========================================================
    // LOCATION PERMISSION
    // =========================================================

    private static final int LOCATION_PERMISSION_REQUEST = 3001;

    // =========================================================
    // MAP REQUEST
    // =========================================================

    private static final int MAP_LOCATION_REQUEST = 3002;

    // =========================================================
    // VIEWS
    // =========================================================

    EditText fname, em, pass, cpass, loc;

    Button reg;
    Button btnCurrentLocation;
    Button btnSelectMap;

    TextView log;

    // =========================================================
    // LOCATION
    // =========================================================

    private FusedLocationProviderClient fusedLocationClient;

    private double selectedLatitude = 0.0;
    private double selectedLongitude = 0.0;

    // =========================================================
    // API
    // =========================================================

    String baseUrl = MyIP.IP_ADDRESS;

    // =========================================================
    // ON CREATE
    // =========================================================

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        setContentView(
                R.layout.activity_volenteer_registration
        );

        // =====================================================
        // INITIALIZE VIEWS
        // =====================================================

        fname = findViewById(R.id.fname);
        em = findViewById(R.id.em);
        pass = findViewById(R.id.pass);
        cpass = findViewById(R.id.cpass);
        loc = findViewById(R.id.loc);

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
            // EMAIL
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
            // PASSWORD
            // =================================================

            if (!password.equals(confirmPassword)) {

                Toast.makeText(
                        this,
                        "Passwords do not match",
                        Toast.LENGTH_SHORT
                ).show();

                return;
            }

            if (password.length() < 6) {

                Toast.makeText(
                        this,
                        "Password must be at least 6 characters",
                        Toast.LENGTH_SHORT
                ).show();

                return;
            }

            // =================================================
            // REGISTER
            // =================================================

            registerVolunteer(
                    fullName,
                    email,
                    password,
                    location,
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
                            Volenteer_Login.class
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
                            "VOL_LOCATION",
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
                        Volenteer_Registration.this,
                        MapPickerActivity.class
                );

        // Send previous location if available

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
                "VOL_LOCATION",
                "Latitude: "
                        + latitude
                        + " Longitude: "
                        + longitude
        );

        // Convert GPS coordinates to real address

        getAddressFromCoordinates(
                latitude,
                longitude
        );
    }

    // =========================================================
    // REVERSE GEOCODING
    // GPS -> ADDRESS
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
        // ANDROID 13+
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

                            Log.e(
                                    "VOL_GEOCODER",
                                    errorMessage
                            );

                            runOnUiThread(() ->
                                    showCoordinatesFallback(
                                            latitude,
                                            longitude
                                    )
                            );
                        }
                    }
            );

        } else {

            // =================================================
            // OLDER ANDROID
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
                            "VOL_GEOCODER",
                            "Geocoder error",
                            e
                    );

                    runOnUiThread(() ->
                            showCoordinatesFallback(
                                    latitude,
                                    longitude
                            )
                    );
                }

            }).start();
        }
    }

    // =========================================================
    // SET ADDRESS
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
                        "VOL_ADDRESS",
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

        showCoordinatesFallback(
                selectedLatitude,
                selectedLongitude
        );
    }

    // =========================================================
    // FALLBACK
    // =========================================================

    private void showCoordinatesFallback(
            double latitude,
            double longitude
    ) {

        /*
         * Keep the coordinates internally.
         * The location field stays user-friendly.
         */

        loc.setText(
                "Selected location"
        );

        Log.d(
                "VOL_LOCATION_FALLBACK",
                "Coordinates: "
                        + latitude
                        + ", "
                        + longitude
        );

        Toast.makeText(
                this,
                "Address could not be detected. Coordinates will still be saved.",
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
    // REGISTER VOLUNTEER
    // =========================================================

    private void registerVolunteer(
            String fullName,
            String email,
            String password,
            String location,
            double latitude,
            double longitude
    ) {

        // =====================================================
        // GET FCM TOKEN
        // =====================================================

        FirebaseMessaging
                .getInstance()
                .getToken()
                .addOnCompleteListener(task -> {

                    if (!task.isSuccessful()) {

                        Toast.makeText(
                                Volenteer_Registration.this,
                                "Failed to get FCM token",
                                Toast.LENGTH_LONG
                        ).show();

                        return;
                    }

                    String fcmToken =
                            task.getResult();

                    String url =
                            baseUrl
                                    + "user/volunteer/register";

                    Log.d(
                            "VOL_REGISTER_URL",
                            url
                    );

                    StringRequest request =
                            new StringRequest(
                                    Request.Method.POST,
                                    url,

                                    // =================================
                                    // SUCCESS
                                    // =================================

                                    response -> {

                                        Log.d(
                                                "VOL_REGISTER_SUCCESS",
                                                response
                                        );

                                        Toast.makeText(
                                                Volenteer_Registration.this,
                                                "Registration Successful",
                                                Toast.LENGTH_LONG
                                        ).show();

                                        startActivity(
                                                new Intent(
                                                        Volenteer_Registration.this,
                                                        Volenteer_Login.class
                                                )
                                        );

                                        finish();
                                    },

                                    // =================================
                                    // ERROR
                                    // =================================

                                    error -> {

                                        String message;

                                        if (error.networkResponse != null) {

                                            try {

                                                String body =
                                                        new String(
                                                                error.networkResponse.data,
                                                                "utf-8"
                                                        );

                                                message =
                                                        "Code: "
                                                                + error.networkResponse.statusCode
                                                                + "\n"
                                                                + body;

                                            } catch (Exception e) {

                                                message =
                                                        "Code: "
                                                                + error.networkResponse.statusCode;
                                            }

                                        } else {

                                            message =
                                                    error.toString();
                                        }

                                        Log.e(
                                                "VOL_REGISTER_ERROR",
                                                message
                                        );

                                        Toast.makeText(
                                                Volenteer_Registration.this,
                                                message,
                                                Toast.LENGTH_LONG
                                        ).show();
                                    }
                            ) {

                                // =====================================
                                // PARAMETERS
                                // =====================================

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
                                            "volunteer"
                                    );

                                    params.put(
                                            "fcmToken",
                                            fcmToken
                                    );

                                    // =================================
                                    // GPS COORDINATES
                                    // =================================

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
                                            "VOL_REGISTER_PARAMS",
                                            params.toString()
                                    );

                                    return params;
                                }

                                @Override
                                public String getBodyContentType() {

                                    return "application/x-www-form-urlencoded; charset=UTF-8";
                                }
                            };

                    // =============================================
                    // SEND REQUEST
                    // =============================================

                    RequestQueue queue =
                            Volley.newRequestQueue(
                                    Volenteer_Registration.this
                            );

                    queue.add(request);
                });
    }
}