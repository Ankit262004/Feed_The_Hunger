package com.example.feed_the_hunger;

public class food_model {

    private String _id;
    private String foodName;
    private String description;
    private int quantity;
    private String expiryDate;
    private String foodType;
    private String image;
    private String location;
    private String status;

    // NEW: map coordinates
    private double latitude;
    private double longitude;

    public food_model(String _id,
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

    public void setId(String _id) {
        this._id = _id;
    }

    public String getFoodName() {
        return foodName;
    }

    public void setFoodName(String foodName) {
        this.foodName = foodName;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    public String getExpiryDate() {
        return expiryDate;
    }

    public void setExpiryDate(String expiryDate) {
        this.expiryDate = expiryDate;
    }

    public String getFoodType() {
        return foodType;
    }

    public void setFoodType(String foodType) {
        this.foodType = foodType;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public double getLatitude() {
        return latitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }
}