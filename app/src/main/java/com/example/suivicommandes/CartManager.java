package com.example.suivicommandes;

import android.content.Context;
import android.content.SharedPreferences;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

public class CartManager {
    private static final String PREF_NAME = "cart_preferences";
    private static final String CART_ITEMS_KEY = "cart_items";

    private static CartManager instance;
    private List<CartItem> cartItems;
    private Context context;

    private CartManager(Context context) {
        this.context = context.getApplicationContext();
        this.cartItems = loadCartItems();
    }

    public static synchronized CartManager getInstance(Context context) {
        if (instance == null) {
            instance = new CartManager(context);
        }
        return instance;
    }

    public List<CartItem> getCartItems() {
        return cartItems;
    }

    public void addItem(Item item) {
        // Check if item already exists in cart
        for (CartItem cartItem : cartItems) {
            if (cartItem.getName().equals(item.getName())) {
                // Increment quantity
                cartItem.setQuantity(cartItem.getQuantity() + 1);
                saveCartItems();
                return;
            }
        }

        // If not found, add new item
        CartItem cartItem = new CartItem(
                item.getItemId(),
                item.getName(),
                item.getPrice(),
                item.getDescription(),
                item.getImage(),
                1
        );
        cartItems.add(cartItem);
        saveCartItems();
    }

    public void updateItemQuantity(CartItem item, int quantity) {
        for (CartItem cartItem : cartItems) {
            if (cartItem.getName().equals(item.getName())) {
                cartItem.setQuantity(quantity);
                if (quantity <= 0) {
                    cartItems.remove(cartItem);
                }
                saveCartItems();
                return;
            }
        }
    }

    public void removeItem(CartItem item) {
        cartItems.remove(item);
        saveCartItems();
    }

    public void clearCart() {
        cartItems.clear();
        saveCartItems();
    }

    public int getItemCount() {
        int count = 0;
        for (CartItem item : cartItems) {
            count += item.getQuantity();
        }
        return count;
    }

    public double getTotalPrice() {
        double total = 0;
        for (CartItem item : cartItems) {
            total += item.getTotalPrice();
        }
        return total;
    }

    private void saveCartItems() {
        SharedPreferences preferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        Gson gson = new Gson();
        String json = gson.toJson(cartItems);
        editor.putString(CART_ITEMS_KEY, json);
        editor.apply();
    }

    private List<CartItem> loadCartItems() {
        SharedPreferences preferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        Gson gson = new Gson();
        String json = preferences.getString(CART_ITEMS_KEY, null);
        Type type = new TypeToken<ArrayList<CartItem>>() {}.getType();
        List<CartItem> items = gson.fromJson(json, type);
        return items != null ? items : new ArrayList<>();
    }
}