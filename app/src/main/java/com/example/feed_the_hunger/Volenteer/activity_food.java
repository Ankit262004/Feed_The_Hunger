package com.example.feed_the_hunger.Volenteer;

import android.Manifest;
import android.app.DatePickerDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.net.Uri;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.example.feed_the_hunger.R;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class activity_food extends AppCompatActivity {

    private static final int LOCATION_PERMISSION_REQUEST_CODE = 101;

    EditText foodName, quantity, expiryDate, description, location;
    Spinner foodTypeSpinner;
    Button uploadImageButton, submitFoodButton;
    ImageView foodImagePreview;

    private Uri imageUri;

    private FusedLocationProviderClient fusedLocationClient;
    private double latitude = 0.0;
    private double longitude = 0.0;

    private final ActivityResultLauncher<Intent> imagePickerLauncher =
            registerForActivityResult(
                    new ActivityResultContracts.StartActivityForResult(),
                    result -> {
                        if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                            imageUri = result.getData().getData();
                            foodImagePreview.setVisibility(ImageView.VISIBLE);
                            foodImagePreview.setImageURI(imageUri);
                        }
                    });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_food);

        foodName = findViewById(R.id.foodName);
        quantity = findViewById(R.id.quantity);
        expiryDate = findViewById(R.id.expiryDate);
        description = findViewById(R.id.description);
        location = findViewById(R.id.location);
        foodTypeSpinner = findViewById(R.id.foodTypeSpinner);
        uploadImageButton = findViewById(R.id.uploadImageButton);
        submitFoodButton = findViewById(R.id.submitFoodButton);
        foodImagePreview = findViewById(R.id.foodImagePreview);

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        expiryDate.setOnClickListener(v -> {
            Calendar calendar = Calendar.getInstance();

            new DatePickerDialog(
                    this,
                    (view, year, month, day) -> {
                        String date = String.format(Locale.getDefault(), "%04d-%02d-%02d", year, month + 1, day);
                        expiryDate.setText(date);
                    },
                    calendar.get(Calendar.YEAR),
                    calendar.get(Calendar.MONTH),
                    calendar.get(Calendar.DAY_OF_MONTH)
            ).show();
        });

        uploadImageButton.setOnClickListener(v -> openGallery());

        getCurrentLocation();

        submitFoodButton.setOnClickListener(v -> {
            if (foodName.getText().toString().trim().isEmpty()) {
                foodName.setError("Enter food name");
                return;
            }

            if (quantity.getText().toString().trim().isEmpty()) {
                quantity.setError("Enter quantity");
                return;
            }

            if (expiryDate.getText().toString().trim().isEmpty()) {
                expiryDate.setError("Select expiry date");
                return;
            }

            if (location.getText().toString().trim().isEmpty()) {
                location.setError("Enter location");
                return;
            }

            if (imageUri == null) {
                Toast.makeText(this, "Please select image", Toast.LENGTH_SHORT).show();
                return;
            }

            if (latitude == 0.0 && longitude == 0.0) {
                Toast.makeText(this, "Location not available yet. Please wait and try again.", Toast.LENGTH_SHORT).show();
                getCurrentLocation();
                return;
            }

            uploadFoodItem();
        });
    }

    private void openGallery() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("image/*");
        imagePickerLauncher.launch(intent);
    }

    private void getCurrentLocation() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(
                    this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    LOCATION_PERMISSION_REQUEST_CODE
            );
            return;
        }

        fusedLocationClient.getLastLocation()
                .addOnSuccessListener(locationObj -> {
                    if (locationObj != null) {
                        latitude = locationObj.getLatitude();
                        longitude = locationObj.getLongitude();

                        try {
                            Geocoder geocoder = new Geocoder(this, Locale.getDefault());
                            List<Address> addresses = geocoder.getFromLocation(latitude, longitude, 1);

                            if (addresses != null && !addresses.isEmpty()) {
                                Address address = addresses.get(0);
                                String fullAddress = address.getAddressLine(0);

                                if (fullAddress != null && !fullAddress.trim().isEmpty()) {
                                    location.setText(fullAddress);
                                } else {
                                    location.setText(latitude + ", " + longitude);
                                }
                            } else {
                                location.setText(latitude + ", " + longitude);
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                            location.setText(latitude + ", " + longitude);
                        }

                    } else {
                        Toast.makeText(this, "Unable to fetch current location", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(e ->
                        Toast.makeText(this, "Location error: " + e.getMessage(), Toast.LENGTH_SHORT).show()
                );
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                getCurrentLocation();
            } else {
                Toast.makeText(this, "Location permission denied", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private File createFileFromUri(Uri uri) throws IOException {
        InputStream inputStream = getContentResolver().openInputStream(uri);

        if (inputStream == null) {
            throw new IOException("Unable to open image");
        }

        File file = new File(getCacheDir(), "food_" + System.currentTimeMillis() + ".jpg");
        FileOutputStream output = new FileOutputStream(file);

        byte[] buffer = new byte[4096];
        int read;

        while ((read = inputStream.read(buffer)) != -1) {
            output.write(buffer, 0, read);
        }

        output.flush();
        output.close();
        inputStream.close();

        return file;
    }

    private RequestBody createPart(String value) {
        return RequestBody.create(value, MediaType.parse("text/plain"));
    }

    private String getFormattedFoodType() {
        return foodTypeSpinner.getSelectedItem().toString()
                .trim()
                .toLowerCase()
                .replace("-", "")
                .replace(" ", "");
    }

    private void uploadFoodItem() {
        try {
            // ✅ FIXED: use UserSession, not UserPrefs
            SharedPreferences prefs = getSharedPreferences("UserSession", MODE_PRIVATE);
            String userId = prefs.getString("userId", "");

            if (userId == null || userId.trim().isEmpty()) {
                Toast.makeText(this, "User not logged in", Toast.LENGTH_LONG).show();
                return;
            }

            File file = createFileFromUri(imageUri);

            RequestBody requestFile =
                    RequestBody.create(file, MediaType.parse("image/*"));

            MultipartBody.Part imagePart =
                    MultipartBody.Part.createFormData("image", file.getName(), requestFile);

            ApiServices api = Apiclient.getClient().create(ApiServices.class);

            Call<ResponseBody> call = api.uploadFood(
                    createPart(userId),
                    createPart(foodName.getText().toString().trim()),
                    createPart(quantity.getText().toString().trim()),
                    createPart(expiryDate.getText().toString().trim()),
                    createPart(description.getText().toString().trim()),
                    createPart(location.getText().toString().trim()),
                    createPart(getFormattedFoodType()),
                    createPart(String.valueOf(latitude)),
                    createPart(String.valueOf(longitude)),
                    imagePart
            );

            call.enqueue(new Callback<ResponseBody>() {
                @Override
                public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                    if (response.isSuccessful()) {
                        Toast.makeText(activity_food.this,
                                "✅ Upload Successful",
                                Toast.LENGTH_LONG).show();

                        foodName.setText("");
                        quantity.setText("");
                        expiryDate.setText("");
                        description.setText("");
                        location.setText("");
                        foodTypeSpinner.setSelection(0);
                        foodImagePreview.setImageDrawable(null);
                        foodImagePreview.setVisibility(ImageView.GONE);
                        imageUri = null;

                        latitude = 0.0;
                        longitude = 0.0;

                        getCurrentLocation();

                    } else {
                        String errorMessage = "❌ Server Error: " + response.code();

                        try {
                            if (response.errorBody() != null) {
                                errorMessage += "\n" + response.errorBody().string();
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                        }

                        Toast.makeText(activity_food.this,
                                errorMessage,
                                Toast.LENGTH_LONG).show();
                    }
                }

                @Override
                public void onFailure(Call<ResponseBody> call, Throwable t) {
                    t.printStackTrace();

                    Toast.makeText(activity_food.this,
                            "❌ ERROR: " + t.getMessage(),
                            Toast.LENGTH_LONG).show();
                }
            });

        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this, "File Error: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }
}