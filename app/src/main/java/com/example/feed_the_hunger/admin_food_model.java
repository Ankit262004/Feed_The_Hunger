package com.example.feed_the_hunger;

public class admin_food_model {

    private String _id;
    private String foodName;
    private String description;
    private int quantity;
    private String expiryDate;
    private String foodType;
    private String image;
    private String location;
    private String status;

    // 🔥 NEW
    private double latitude;
    private double longitude;

    // ✅ Constructor
    public admin_food_model(String _id,
                            String foodName,
                            String description,
                            int quantity,
                            String expiryDate,
                            String foodType,
                            String image,
                            String location,
                            String status,
                            double latitude,
                            double longitude) {

        this._id = _id;
        this.foodName = foodName;
        this.description = description;
        this.quantity = quantity;
        this.expiryDate = expiryDate;
        this.foodType = foodType;
        this.image = image;
        this.location = location;
        this.status = status;
        this.latitude = latitude;
        this.longitude = longitude;
    }

    public String getId() {
        return _id;
    }

    public String getFoodName() {
        return foodName;
    }

    public String getDescription() {
        return description;
    }

    public int getQuantity() {
        return quantity;
    }

    public String getExpiryDate() {
        return expiryDate;
    }

    public String getFoodType() {
        return foodType;
    }

    public String getImage() {
        return image;
    }

    public String getLocation() {
        return location;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    // 🔥 NEW GETTERS
    public double getLatitude() {
        return latitude;
    }

    public double getLongitude() {
        return longitude;
    }
}