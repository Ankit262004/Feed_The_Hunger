package com.example.feed_the_hunger;

import android.Manifest;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.Priority;
import com.google.android.gms.tasks.CancellationTokenSource;

import org.maplibre.android.MapLibre;
import org.maplibre.android.camera.CameraPosition;
import org.maplibre.android.geometry.LatLng;
import org.maplibre.android.maps.MapLibreMap;
import org.maplibre.android.maps.MapView;
import org.maplibre.android.maps.OnMapReadyCallback;

import java.util.Locale;

public class MapPickerActivity extends AppCompatActivity {

    // =========================================================
    // LOCATION PERMISSION
    // =========================================================

    private static final int LOCATION_PERMISSION_REQUEST = 1001;

    // =========================================================
    // MAPTILER API KEY
    // =========================================================
    //
    // Put your NEW MapTiler key here.
    //
    // Example:
    //
    // private static final String MAPTILER_API_KEY =
    //         "abc123...";
    //
    // Do NOT use the old exposed key.
    // =========================================================

    private static final String MAPTILER_API_KEY =
            "W9ZNL8VDrPlMmK7YvWr4";

    // =========================================================
    // MAP
    // =========================================================

    private MapView mapView;
    private MapLibreMap mapLibreMap;

    // =========================================================
    // UI
    // =========================================================

    private TextView txtSelectedLocation;
    private Button btnConfirmLocation;

    // =========================================================
    // GPS
    // =========================================================

    private FusedLocationProviderClient fusedLocationClient;

    // =========================================================
    // SELECTED LOCATION
    // =========================================================

    private double selectedLatitude = 22.5726;
    private double selectedLongitude = 88.3639;


    // =========================================================
    // ON CREATE
    // =========================================================

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        // =====================================================
        // INITIALIZE MAPLIBRE
        // =====================================================

        MapLibre.getInstance(this);

        // =====================================================
        // LOAD XML
        // =====================================================

        setContentView(
                R.layout.activity_map_picker
        );

        // =====================================================
        // INITIALIZE VIEWS
        // =====================================================

        mapView = findViewById(
                R.id.mapView
        );

        txtSelectedLocation = findViewById(
                R.id.txtSelectedLocation
        );

        btnConfirmLocation = findViewById(
                R.id.btnConfirmLocation
        );

        // =====================================================
        // LOCATION CLIENT
        // =====================================================

        fusedLocationClient =
                LocationServices
                        .getFusedLocationProviderClient(this);

        // =====================================================
        // MAPVIEW LIFECYCLE
        // =====================================================

        mapView.onCreate(savedInstanceState);

        // =====================================================
        // LOAD MAP
        // =====================================================

        mapView.getMapAsync(
                new OnMapReadyCallback() {

                    @Override
                    public void onMapReady(
                            @NonNull MapLibreMap map
                    ) {

                        mapLibreMap = map;

                        // =====================================
                        // MAPTILER STREET STYLE
                        // =====================================

                        String mapStyleUrl =
                                "https://api.maptiler.com/maps/streets-v2/style.json?key="
                                        + MAPTILER_API_KEY;

                        // =====================================
                        // LOAD MAPTILER STYLE
                        // =====================================

                        map.setStyle(
                                mapStyleUrl,
                                style -> {

                                    // =========================
                                    // DEFAULT CAMERA
                                    // =========================

                                    moveMapToLocation(
                                            selectedLatitude,
                                            selectedLongitude
                                    );

                                    // =========================
                                    // CAMERA MOVED
                                    // =========================

                                    map.addOnCameraIdleListener(
                                            () -> {

                                                LatLng center =
                                                        map.getCameraPosition()
                                                                .target;

                                                if (center != null) {

                                                    selectedLatitude =
                                                            center.getLatitude();

                                                    selectedLongitude =
                                                            center.getLongitude();

                                                    updateLocationText();
                                                }
                                            }
                                    );

                                    // =========================
                                    // GET GPS LOCATION
                                    // =========================

                                    requestLocationPermission();
                                }
                        );
                    }
                }
        );

        // =====================================================
        // CONFIRM LOCATION
        // =====================================================

        btnConfirmLocation.setOnClickListener(
                v -> confirmLocation()
        );

        // =====================================================
        // INITIAL TEXT
        // =====================================================

        updateLocationText();
    }


    // =========================================================
    // UPDATE LOCATION TEXT
    // =========================================================

    private void updateLocationText() {

        String locationText =
                String.format(
                        Locale.getDefault(),

                        "Latitude: %.6f\nLongitude: %.6f",

                        selectedLatitude,
                        selectedLongitude
                );

        txtSelectedLocation.setText(
                locationText
        );
    }


    // =========================================================
    // GET CURRENT LOCATION
    // =========================================================

    private void requestLocationPermission() {

        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
        ) != PackageManager.PERMISSION_GRANTED
                &&
                ActivityCompat.checkSelfPermission(
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
    // GET GPS LOCATION
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

        CancellationTokenSource cancellationTokenSource =
                new CancellationTokenSource();

        fusedLocationClient
                .getCurrentLocation(
                        Priority.PRIORITY_HIGH_ACCURACY,
                        cancellationTokenSource.getToken()
                )
                .addOnSuccessListener(
                        location -> {

                            if (location != null) {

                                moveMapToLocation(
                                        location.getLatitude(),
                                        location.getLongitude()
                                );

                            } else {

                                // =========================
                                // DEFAULT KOLKATA LOCATION
                                // =========================

                                moveMapToLocation(
                                        22.5726,
                                        88.3639
                                );

                                Toast.makeText(
                                        this,
                                        "GPS location unavailable",
                                        Toast.LENGTH_SHORT
                                ).show();
                            }
                        }
                )
                .addOnFailureListener(
                        e -> {

                            // =============================
                            // DEFAULT KOLKATA
                            // =============================

                            moveMapToLocation(
                                    22.5726,
                                    88.3639
                            );

                            Toast.makeText(
                                    this,
                                    "Unable to get current location",
                                    Toast.LENGTH_SHORT
                            ).show();
                        }
                );
    }


    // =========================================================
    // MOVE MAP
    // =========================================================

    private void moveMapToLocation(
            double latitude,
            double longitude
    ) {

        if (mapLibreMap == null) {
            return;
        }

        selectedLatitude = latitude;
        selectedLongitude = longitude;

        // =====================================================
        // MAPLIBRE LAT/LNG
        // =====================================================

        LatLng location =
                new LatLng(
                        latitude,
                        longitude
                );

        // =====================================================
        // CAMERA
        // =====================================================

        CameraPosition cameraPosition =
                new CameraPosition.Builder()
                        .target(location)
                        .zoom(15.0)
                        .build();

        mapLibreMap.setCameraPosition(
                cameraPosition
        );

        updateLocationText();
    }


    // =========================================================
    // CONFIRM LOCATION
    // =========================================================

    private void confirmLocation() {

        if (selectedLatitude == 0.0
                && selectedLongitude == 0.0) {

            Toast.makeText(
                    this,
                    "Please select a location first",
                    Toast.LENGTH_SHORT
            ).show();

            return;
        }

        // =====================================================
        // RETURN LATITUDE
        // =====================================================

        getIntent().putExtra(
                "latitude",
                selectedLatitude
        );

        // =====================================================
        // RETURN LONGITUDE
        // =====================================================

        getIntent().putExtra(
                "longitude",
                selectedLongitude
        );

        // =====================================================
        // RETURN RESULT
        // =====================================================

        setResult(
                RESULT_OK,
                getIntent()
        );

        Toast.makeText(
                this,
                "Location selected successfully",
                Toast.LENGTH_SHORT
        ).show();

        finish();
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

            if (grantResults.length > 0
                    &&
                    grantResults[0] ==
                            PackageManager.PERMISSION_GRANTED) {

                getCurrentLocation();

            } else {

                Toast.makeText(
                        this,
                        "Location permission denied",
                        Toast.LENGTH_LONG
                ).show();

                // Keep Kolkata as default location.

                moveMapToLocation(
                        22.5726,
                        88.3639
                );
            }
        }
    }


    // =========================================================
    // MAPVIEW LIFECYCLE
    // =========================================================

    @Override
    protected void onStart() {

        super.onStart();

        if (mapView != null) {
            mapView.onStart();
        }
    }


    @Override
    protected void onResume() {

        super.onResume();

        if (mapView != null) {
            mapView.onResume();
        }
    }


    @Override
    protected void onPause() {

        if (mapView != null) {
            mapView.onPause();
        }

        super.onPause();
    }


    @Override
    protected void onStop() {

        if (mapView != null) {
            mapView.onStop();
        }

        super.onStop();
    }


    @Override
    protected void onDestroy() {

        if (mapView != null) {
            mapView.onDestroy();
        }

        super.onDestroy();
    }


    @Override
    public void onLowMemory() {

        super.onLowMemory();

        if (mapView != null) {
            mapView.onLowMemory();
        }
    }


    @Override
    protected void onSaveInstanceState(
            @NonNull Bundle outState
    ) {

        super.onSaveInstanceState(
                outState
        );

        if (mapView != null) {

            mapView.onSaveInstanceState(
                    outState
            );
        }
    }
}