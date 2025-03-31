package com.example.suivicommandes;

import java.io.Serializable;
import java.util.Objects;

public class CartItem implements Serializable {
    private static final long serialVersionUID = 1L;

    private String id;
    private String name;
    private double price;
    private int quantity;
    private String image;

    // Default constructor
    public CartItem() {
        this.quantity = 1;
    }

    // Constructor for Item conversion
    public CartItem(Item item) {
        this.id = item.getItemId();
        this.name = item.getName();
        this.price = item.getPrice();
        this.quantity = 1;
        this.image = item.getImage();
    }

    // Full constructor
    public CartItem(String id, String name, double price, int quantity, String image) {
        this.id = id;
        this.name = name;
        this.price = price;
        this.quantity = quantity;
        this.image = image;
    }

    // Getters and setters
    public String getId() {
        return id != null ? id : "";
    }

    public void setId(String id) {
        this.id = id;
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

    public void setPrice(double price) {
        this.price = price >= 0 ? price : 0;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity > 0 ? quantity : 1;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    // Helper method to calculate total price
    public double getTotalPrice() {
        return price * quantity;
    }

    // For comparison in ArrayLists
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        CartItem cartItem = (CartItem) o;
        return Objects.equals(id, cartItem.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}