package com.example.suivicommandes;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import java.text.NumberFormat;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class CartActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private CartAdapter cartAdapter;
    private TextView emptyCartText, totalPriceText;
    private Button checkoutButton, continueShoppingButton;
    private CartManager cartManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cart);

        // Set up toolbar
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("Mon Panier");
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        // Initialize views
        recyclerView = findViewById(R.id.cartRecyclerView);
        emptyCartText = findViewById(R.id.emptyCartText);
        totalPriceText = findViewById(R.id.totalPriceText);
        checkoutButton = findViewById(R.id.checkoutButton);
        continueShoppingButton = findViewById(R.id.continueShoppingButton);

        // Get cart manager
        cartManager = CartManager.getInstance(this);

        // Set up RecyclerView
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        cartAdapter = new CartAdapter(this, cartManager.getCartItems(), this::updateCartItem);
        recyclerView.setAdapter(cartAdapter);

        // Update UI based on cart contents
        updateCartUI();

        // Button click listeners
        checkoutButton.setOnClickListener(v -> checkout());
        continueShoppingButton.setOnClickListener(v -> finish());  // Go back to previous screen
    }

    private void updateCartUI() {
        if (cartManager.getCartItems().isEmpty()) {
            recyclerView.setVisibility(View.GONE);
            emptyCartText.setVisibility(View.VISIBLE);
            checkoutButton.setEnabled(false);
        } else {
            recyclerView.setVisibility(View.VISIBLE);
            emptyCartText.setVisibility(View.GONE);
            checkoutButton.setEnabled(true);

            // Format the total price
            NumberFormat currencyFormat = NumberFormat.getCurrencyInstance(Locale.US);
            totalPriceText.setText("Total: " + currencyFormat.format(cartManager.getTotalPrice()));
        }
    }

    private void updateCartItem(CartItem item, int newQuantity) {
        if (newQuantity <= 0) {
            cartManager.removeItem(item);
        } else {
            cartManager.updateItemQuantity(item, newQuantity);
        }

        cartAdapter.notifyDataSetChanged();
        updateCartUI();
    }

    // Inside the checkout method:
    private void checkout() {
        FirebaseAuth auth = FirebaseAuth.getInstance();
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        FirebaseUser currentUser = auth.getCurrentUser();

        if (currentUser == null) {
            Toast.makeText(this, "Please sign in to place an order", Toast.LENGTH_SHORT).show();
            return;
        }

        if (cartManager.getCartItems().isEmpty()) {
            Toast.makeText(this, "Your cart is empty", Toast.LENGTH_SHORT).show();
            return;
        }

        // Create the order with all required fields
        Map<String, Object> orderData = new HashMap<>();
        orderData.put("userId", currentUser.getUid()); // CRITICAL - user ID
        orderData.put("userEmail", currentUser.getEmail()); // For display in admin view
        orderData.put("items", cartManager.getCartItems());
        orderData.put("totalAmount", cartManager.getTotalPrice());
        orderData.put("orderStatus", "pending");
        orderData.put("orderDate", new java.util.Date()); // Current timestamp

        // Extract info from first item for easier display
        if (!cartManager.getCartItems().isEmpty()) {
            CartItem firstItem = cartManager.getCartItems().get(0);
            orderData.put("itemName", firstItem.getName());
            orderData.put("itemPrice", firstItem.getPrice());
            orderData.put("itemImage", firstItem.getImage());
        }

        // Save to Firestore
        db.collection("orders")
                .add(orderData)
                .addOnSuccessListener(documentReference -> {
                    Log.d("CartActivity", "Order created with ID: " + documentReference.getId());

                    // Clear the cart
                    cartManager.clearCart();

                    // Show success message
                    Toast.makeText(CartActivity.this, "Order placed successfully!", Toast.LENGTH_SHORT).show();

                    // Navigate to order confirmation screen
                    Intent intent = new Intent(CartActivity.this, OrderConfirmationActivity.class);
                    intent.putExtra("ORDER_ID", documentReference.getId());
                    startActivity(intent);

                    // Close this activity
                    finish();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(CartActivity.this, "Failed to place order: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }
    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }
}