package com.example.suivicommandes;

import com.google.firebase.firestore.DocumentId;
import java.io.Serializable;

public class Item implements Serializable {
    private static final long serialVersionUID = 1L; // Add serialVersionUID for better serialization control

    @DocumentId
    private String itemId;
    private String name;
    private double price;
    @com.google.firebase.firestore.Exclude // Exclude from Firestore
    private transient Object originalPrice; // Transient field for temporary storage
    private String description;
    private String image; // Image URL

    // Default constructor required for Firestore
    public Item() {
    }

    // Main constructor
    public Item(String itemId, String name, Object price, String description, String image) {
        this.itemId = itemId;
        this.name = name;
        setPrice(price);
        this.description = description;
        this.image = image;
    }

    // Getters and setters
    public String getItemId() {
        return itemId != null ? itemId : "";
    }

    public void setItemId(String itemId) {
        this.itemId = itemId;
    }

    public String getName() {
        return name != null ? name : "";
    }

    public void setName(String name) {
        this.name = name;
    }

    public double getPrice() {
        return price;
    }

    // Improved price setter with better type handling and validation
    public void setPrice(Object priceObj) {
        this.originalPrice = priceObj;

        if (priceObj == null) {
            this.price = 0.0;
            return;
        }

        try {
            if (priceObj instanceof String) {
                String priceStr = ((String) priceObj).trim();
                // Remove currency symbols and spaces if present
                priceStr = priceStr.replaceAll("[^\\d.,\\-]", "")
                        .replace(",", ".");
                this.price = Double.parseDouble(priceStr);
            } else if (priceObj instanceof Double) {
                this.price = (Double) priceObj;
            } else if (priceObj instanceof Long) {
                this.price = ((Long) priceObj).doubleValue();
            } else if (priceObj instanceof Integer) {
                this.price = ((Integer) priceObj).doubleValue();
            } else if (priceObj instanceof Float) {
                this.price = ((Float) priceObj).doubleValue();
            } else {
                this.price = 0.0;
            }
        } catch (NumberFormatException | NullPointerException e) {
            this.price = 0.0;
        }

        // Ensure price is not negative
        if (this.price < 0) {
            this.price = 0.0;
        }
    }

    public String getDescription() {
        return description != null ? description : "";
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getImage() {
        return image != null ? image : "";
    }

    public void setImage(String image) {
        this.image = image;
    }

    // Add method to get formatted price string
    public String getFormattedPrice() {
        return String.format("%.2f", price);
    }

    // Override toString for debugging
    @Override
    public String toString() {
        return "Item{" +
                "itemId='" + itemId + '\'' +
                ", name='" + name + '\'' +
                ", price=" + price +
                ", description='" + description + '\'' +
                ", image='" + image + '\'' +
                '}';
    }
}