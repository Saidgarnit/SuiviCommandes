package com.example.suivicommandes;

import com.google.firebase.firestore.DocumentId;
import java.io.Serializable;

public class Item implements Serializable {
    @DocumentId
    private String itemId;
    private String name;
    private Object price;
    private String description;
    private String image; // Image URL

    public Item() {
        // Firestore requires an empty constructor
    }

    public Item(String itemId, String name, Object price, String description, String image) {
        this.itemId = itemId;
        this.name = name;
        this.price = price;
        this.description = description;
        this.image = image;
    }

    public String getItemId() {
        return itemId;
    }

    public void setItemId(String itemId) {
        this.itemId = itemId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public double getPrice() {
        // Handle different types (String or Double)
        if (price instanceof String) {
            try {
                return Double.parseDouble((String) price); // Convert String to double
            } catch (NumberFormatException e) {
                return 0.0;
            }
        } else if (price instanceof Double) {
            return (Double) price; // If it's already a double
        } else if (price instanceof Long) {
            return ((Long) price).doubleValue(); // If it's a long
        } else if (price instanceof Integer) {
            return ((Integer) price).doubleValue(); // If it's an integer
        }
        return 0; // Default value if no valid price is found
    }

    public void setPrice(Object price) {
        this.price = price;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }
}