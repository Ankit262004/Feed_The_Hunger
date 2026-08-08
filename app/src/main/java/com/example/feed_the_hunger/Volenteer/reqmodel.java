package com.example.feed_the_hunger.Volenteer;

public class reqmodel {

    private String _id; // ✅ ADD THIS
    private String foodName;
    private String description;
    private int quantity;
    private String expiryDate;
    private String foodType;
    private String image;
    private String location;
    private String status;

    // ✅ Constructor
    public reqmodel(String _id, String foodName, String description, int quantity,
                    String expiryDate, String foodType,
                    String image, String location, String status) {

        this._id = _id; // ✅ ADD THIS
        this.foodName = foodName;
        this.description = description;
        this.quantity = quantity;
        this.expiryDate = expiryDate;
        this.foodType = foodType;
        this.image = image;
        this.location = location;
        this.status = status;
    }

    // ✅ IMPORTANT GETTER
    public String getId() {
        return _id;
    }

    // Existing getters
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

    // ✅ Needed for UI update
    public void setStatus(String status) {
        this.status = status;
    }
}