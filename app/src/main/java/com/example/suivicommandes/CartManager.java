package com.example.suivicommandes;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

public class CartManager {
    private static final String TAG = "CartManager";
    private static final String PREF_NAME = "cart_preferences";
    private static final String CART_ITEMS_KEY = "cart_items";
    private static final int MAX_QUANTITY_PER_ITEM = 99;

    private static final AtomicReference<CartManager> instance = new AtomicReference<>();
    private final List<CartItem> cartItems;
    private final Context context;
    private final Gson gson;

    private CartManager(Context context) {
        this.context = context.getApplicationContext();
        this.gson = new Gson();
        this.cartItems = loadCartItems();
        Log.d(TAG, "CartManager initialized with " + cartItems.size() + " items");
    }

    public static CartManager getInstance(Context context) {
        if (context == null) {
            throw new IllegalArgumentException("Context cannot be null");
        }

        CartManager result = instance.get();
        if (result == null) {
            synchronized (CartManager.class) {
                result = instance.get();
                if (result == null) {
                    result = new CartManager(context);
                    instance.set(result);
                }
            }
        }
        return result;
    }

    public List<CartItem> getCartItems() {
        synchronized (cartItems) {
            return new ArrayList<>(cartItems);
        }
    }

    public void addItem(Item item) {
        if (item == null) {
            Log.e(TAG, "Cannot add null item to cart");
            return;
        }

        synchronized (cartItems) {
            try {
                // Check if item already exists in cart
                CartItem existingItem = findExistingCartItem(item);

                if (existingItem != null) {
                    updateExistingItem(existingItem);
                } else {
                    addNewItem(item);
                }

                saveCartItems();
                Log.d(TAG, "Item added/updated successfully: " + item.getName());

            } catch (Exception e) {
                Log.e(TAG, "Error adding item to cart: " + e.getMessage(), e);
            }
        }
    }

    private CartItem findExistingCartItem(Item item) {
        for (CartItem cartItem : cartItems) {
            if (cartItem.getName().equals(item.getName())) {
                return cartItem;
            }
        }
        return null;
    }

    private void updateExistingItem(CartItem cartItem) {
        int newQuantity = cartItem.getQuantity() + 1;
        if (newQuantity <= MAX_QUANTITY_PER_ITEM) {
            cartItem.setQuantity(newQuantity);
            Log.d(TAG, "Updated quantity for " + cartItem.getName() + " to " + newQuantity);
        } else {
            Log.w(TAG, "Cannot add more items. Maximum quantity reached for " + cartItem.getName());
        }
    }

    private void addNewItem(Item item) {
        CartItem newItem = new CartItem(
                item.getItemId(),    // String id
                item.getName(),      // String name
                item.getPrice(),     // double price
                1,                   // int quantity (starting with 1)
                item.getImage()      // String image
        );
        cartItems.add(newItem);
        Log.d(TAG, "New item added to cart: " + item.getName());
    }

    public void updateItemQuantity(CartItem item, int quantity) {
        if (item == null) {
            Log.e(TAG, "Cannot update null item");
            return;
        }

        synchronized (cartItems) {
            try {
                CartItem cartItem = findExistingCartItem(item);
                if (cartItem != null) {
                    if (quantity <= 0) {
                        cartItems.remove(cartItem);
                        Log.d(TAG, "Removed item from cart: " + item.getName());
                    } else if (quantity <= MAX_QUANTITY_PER_ITEM) {
                        cartItem.setQuantity(quantity);
                        Log.d(TAG, "Updated quantity for " + item.getName() + " to " + quantity);
                    } else {
                        Log.w(TAG, "Cannot set quantity above maximum limit for " + item.getName());
                        return;
                    }
                    saveCartItems();
                }
            } catch (Exception e) {
                Log.e(TAG, "Error updating item quantity: " + e.getMessage(), e);
            }
        }
    }

    public void removeItem(CartItem item) {
        if (item == null) return;

        synchronized (cartItems) {
            boolean removed = cartItems.remove(item);
            if (removed) {
                saveCartItems();
                Log.d(TAG, "Item removed from cart: " + item.getName());
            }
        }
    }

    public void clearCart() {
        synchronized (cartItems) {
            cartItems.clear();
            saveCartItems();
            Log.d(TAG, "Cart cleared");
        }
    }

    public int getItemCount() {
        synchronized (cartItems) {
            return cartItems.stream()
                    .mapToInt(CartItem::getQuantity)
                    .sum();
        }
    }

    public double getTotalPrice() {
        synchronized (cartItems) {
            return cartItems.stream()
                    .mapToDouble(CartItem::getTotalPrice)
                    .sum();
        }
    }

    private void saveCartItems() {
        try {
            SharedPreferences preferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
            String json = gson.toJson(cartItems);
            preferences.edit()
                    .putString(CART_ITEMS_KEY, json)
                    .apply();
            Log.d(TAG, "Cart saved successfully with " + cartItems.size() + " items");
        } catch (Exception e) {
            Log.e(TAG, "Error saving cart items: " + e.getMessage(), e);
        }
    }

    private List<CartItem> loadCartItems() {
        try {
            SharedPreferences preferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
            String json = preferences.getString(CART_ITEMS_KEY, null);
            if (json == null) {
                return new ArrayList<>();
            }

            Type type = new TypeToken<ArrayList<CartItem>>() {}.getType();
            List<CartItem> items = gson.fromJson(json, type);
            return items != null ? items : new ArrayList<>();

        } catch (Exception e) {
            Log.e(TAG, "Error loading cart items: " + e.getMessage(), e);
            return new ArrayList<>();
        }
    }

    private CartItem findExistingCartItem(CartItem item) {
        for (CartItem cartItem : cartItems) {
            if (cartItem.getName().equals(item.getName())) {
                return cartItem;
            }
        }
        return null;
    }
}