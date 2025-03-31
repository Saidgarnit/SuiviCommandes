package com.example.suivicommandes;

import com.google.firebase.firestore.DocumentId;
import com.google.firebase.firestore.Exclude;
import com.google.firebase.firestore.ServerTimestamp;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class Order implements Serializable {
    private static final long serialVersionUID = 1L;

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

    // Legacy support fields
    private String itemName;
    private double itemPrice;
    private String itemDescription;
    private String itemImage;

    // Default constructor required for Firestore
    public Order() {
        this.items = new ArrayList<>();
        this.orderStatus = "pending";
        this.orderDate = new Date();
        this.lastUpdated = new Date();
    }

    // Constructor for cart orders
    public Order(String userId, String userEmail, List<CartItem> items, double totalAmount) {
        this();
        this.userId = userId;
        this.userEmail = userEmail;
        this.items = items != null ? items : new ArrayList<>();
        this.totalAmount = totalAmount;
    }

    // Constructor for legacy single-item orders
    public Order(String itemName, double itemPrice, String itemDescription,
                 String itemImage, String userId, String orderStatus) {
        this();
        this.itemName = itemName;
        this.itemPrice = itemPrice;
        this.itemDescription = itemDescription;
        this.itemImage = itemImage;
        this.userId = userId;
        this.orderStatus = orderStatus != null ? orderStatus : "pending";
    }

    // Getters and setters with null safety
    public String getOrderId() {
        return orderId != null ? orderId : "";
    }

    public void setOrderId(String orderId) {
        this.orderId = orderId;
    }

    public String getUserId() {
        return userId != null ? userId : "";
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getUserEmail() {
        return userEmail != null ? userEmail : "";
    }

    public void setUserEmail(String userEmail) {
        this.userEmail = userEmail;
    }

    public List<CartItem> getItems() {
        return items != null ? items : new ArrayList<>();
    }

    public void setItems(List<CartItem> items) {
        this.items = items != null ? items : new ArrayList<>();
    }

    public double getTotalAmount() {
        if (totalAmount <= 0) {
            // Calculate total from items if available
            if (items != null && !items.isEmpty()) {
                return items.stream()
                        .mapToDouble(item -> item.getPrice() * item.getQuantity())
                        .sum();
            }
            // Fall back to legacy single item price
            return itemPrice;
        }
        return totalAmount;
    }

    public void setTotalAmount(double totalAmount) {
        this.totalAmount = totalAmount;
    }

    public String getOrderStatus() {
        return orderStatus != null ? orderStatus : "pending";
    }

    public void setOrderStatus(String orderStatus) {
        this.orderStatus = orderStatus != null ? orderStatus : "pending";
        this.lastUpdated = new Date();
    }

    public Date getOrderDate() {
        return orderDate != null ? orderDate : new Date();
    }

    public void setOrderDate(Date orderDate) {
        this.orderDate = orderDate != null ? orderDate : new Date();
    }

    public Date getLastUpdated() {
        return lastUpdated != null ? lastUpdated : new Date();
    }

    public void setLastUpdated(Date lastUpdated) {
        this.lastUpdated = lastUpdated != null ? lastUpdated : new Date();
    }

    // Legacy support getters and setters
    public String getItemName() {
        return itemName != null ? itemName : "";
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
        return itemDescription != null ? itemDescription : "";
    }

    public void setItemDescription(String itemDescription) {
        this.itemDescription = itemDescription;
    }

    public String getItemImage() {
        return itemImage != null ? itemImage : "";
    }

    public void setItemImage(String itemImage) {
        this.itemImage = itemImage;
    }

    // Utility methods
    @Exclude
    public int getItemCount() {
        if (items != null && !items.isEmpty()) {
            return items.stream()
                    .mapToInt(CartItem::getQuantity)
                    .sum();
        }
        return 1; // Default to 1 for legacy single-item orders
    }

    @Exclude
    public String getFormattedStatus() {
        switch (orderStatus != null ? orderStatus : "pending") {
            case "pending": return "En attente";
            case "preparing": return "En préparation";
            case "shipped": return "Expédié";
            case "delivering": return "En livraison";
            case "delivered": return "Livré";
            default: return orderStatus;
        }
    }

    // Optional: Add method to convert legacy order to new format
    @Exclude
    public void convertLegacyOrder() {
        if (itemName != null && items.isEmpty()) {
            CartItem legacyItem = new CartItem();
            legacyItem.setName(itemName);
            legacyItem.setPrice(itemPrice);
            legacyItem.setQuantity(1);
            items.add(legacyItem);

            // Clear legacy fields
            itemName = null;
            itemPrice = 0;
            itemDescription = null;
            itemImage = null;
        }
    }
}