package com.example.suivicommandes;

import com.google.firebase.firestore.DocumentId;
import com.google.firebase.firestore.ServerTimestamp;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class Order {
    @DocumentId
    private String orderId;
    private String userId;
    private String userEmail;
    private List<CartItem> items;
    private double totalAmount;
    private String orderStatus;
    @ServerTimestamp
    private Date orderDate;
    private Date lastUpdated;

    private String itemName; // Add this field
    private double itemPrice; // Add this field
    private String itemDescription; // Add this field
    private String itemImage; // Add this field

    // Firestore requires an empty constructor
    public Order() {
        this.items = new ArrayList<>();
        this.lastUpdated = new Date();
    }

    public Order(String userId, String userEmail, List<CartItem> items, double totalAmount) {
        this.userId = userId;
        this.userEmail = userEmail;
        this.items = items;
        this.totalAmount = totalAmount;
        this.orderStatus = "pending";
        this.lastUpdated = new Date();
    }

    // New constructor matching the parameters used in ItemDetailsActivity
    public Order(String itemName, double itemPrice, String itemDescription, String itemImage, String userId, String orderStatus) {
        this.itemName = itemName;
        this.itemPrice = itemPrice;
        this.itemDescription = itemDescription;
        this.itemImage = itemImage;
        this.userId = userId;
        this.orderStatus = orderStatus;
        this.lastUpdated = new Date();
    }

    // Getters and setters
    public String getOrderId() {
        return orderId;
    }

    public void setOrderId(String orderId) {
        this.orderId = orderId;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getUserEmail() {
        return userEmail;
    }

    public void setUserEmail(String userEmail) {
        this.userEmail = userEmail;
    }

    public List<CartItem> getItems() {
        return items;
    }

    public void setItems(List<CartItem> items) {
        this.items = items;
    }

    public double getTotalAmount() {
        return totalAmount;
    }

    public void setTotalAmount(double totalAmount) {
        this.totalAmount = totalAmount;
    }

    public String getOrderStatus() {
        return orderStatus;
    }

    public void setOrderStatus(String orderStatus) {
        this.orderStatus = orderStatus;
        this.lastUpdated = new Date();
    }

    public Date getOrderDate() {
        return orderDate;
    }

    public void setOrderDate(Date orderDate) {
        this.orderDate = orderDate;
    }

    public Date getLastUpdated() {
        return lastUpdated;
    }

    public void setLastUpdated(Date lastUpdated) {
        this.lastUpdated = lastUpdated;
    }

    // Utility methods
    public int getItemCount() {
        int count = 0;
        for (CartItem item : items) {
            count += item.getQuantity();
        }
        return count;
    }

    public String getFormattedStatus() {
        switch (orderStatus) {
            case "pending": return "En attente";
            case "preparing": return "En préparation";
            case "shipped": return "Expédié";
            case "delivering": return "En livraison";
            case "delivered": return "Livré";
            default: return orderStatus;
        }
    }

    // Add these methods - they're missing in your class
    public String getItemName() {
        return itemName;
    }

    public void setItemName(String itemName) {
        this.itemName = itemName;
    }

    public double getItemPrice() {
        return itemPrice;
    }

    public void setItemPrice(double itemPrice) {
        this.itemPrice = itemPrice;
    }

    public String getItemDescription() {
        return itemDescription;
    }

    public void setItemDescription(String itemDescription) {
        this.itemDescription = itemDescription;
    }

    public String getItemImage() {
        return itemImage;
    }

    public void setItemImage(String itemImage) {
        this.itemImage = itemImage;
    }
}