package com.example.feed_the_hunger.Volenteer;

public class volunteer_delivery_model {

    private String _id;
    private String foodName;
    private String description;
    private String quantity;
    private String expiryDate;
    private String foodType;
    private String image;
    private String location;
    private String status;

    public volunteer_delivery_model(String _id, String foodName, String description,
                                    String quantity, String expiryDate, String foodType,
                                    String image, String location, String status) {
        this._id = _id;
        this.foodName = foodName;
        this.description = description;
        this.quantity = quantity;
        this.expiryDate = expiryDate;
        this.foodType = foodType;
        this.image = image;
        this.location = location;
        this.status = status;
    }

    public String get_id() {
        return _id;
    }

    public String getFoodName() {
        return foodName;
    }

    public String getDescription() {
        return description;
    }

    public String getQuantity() {
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
}