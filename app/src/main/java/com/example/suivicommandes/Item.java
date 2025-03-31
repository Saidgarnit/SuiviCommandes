package com.example.suivicommandes;

import java.io.Serializable;

public class Item implements Serializable {
    private String name;
    private Object price;
    private String description;
    private String image; // Image URL

    public Item() {
        // Firestore requires an empty constructor
    }

    public Item(String name, Object price, String description, String image) {
        this.name = name;
        this.price = price;
        this.description = description;
        this.image = image;
    }

    public String getName() {
        return name;
    }

    public double getPrice() {
        // Handle different types (String or Double)
        if (price instanceof String) {
            return Double.parseDouble((String) price); // Convert String to double
        } else if (price instanceof Double) {
            return (Double) price; // If it's already a double
        }
        return 0; // Default value if no valid price is found
    }

    public String getDescription() {
        return description;
    }

    public String getImage() {
        return image;
    }
}

