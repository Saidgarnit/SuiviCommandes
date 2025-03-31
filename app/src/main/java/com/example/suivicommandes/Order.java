package com.example.suivicommandes;

public class Order {
    private String itemName;
    private double itemPrice;
    private String itemDescription;
    private String itemImage;
    private String userId; // Store user ID who placed the order
    private String orderStatus; // Status of the order (e.g., "pending", "shipped", "delivered")

    // Firestore requires an empty constructor
    public Order() {}

    public Order(String itemName, double itemPrice, String itemDescription, String itemImage, String userId, String orderStatus) {
        this.itemName = itemName;
        this.itemPrice = itemPrice;
        this.itemDescription = itemDescription;
        this.itemImage = itemImage;
        this.userId = userId;
        this.orderStatus = orderStatus;
    }

    // Getters and setters for all fields
    public String getItemName() {
        return itemName;
    }

    public double getItemPrice() {
        return itemPrice;
    }

    public String getItemDescription() {
        return itemDescription;
    }

    public String getItemImage() {
        return itemImage;
    }

    public String getUserId() {
        return userId;
    }

    public String getOrderStatus() {
        return orderStatus;
    }
}

