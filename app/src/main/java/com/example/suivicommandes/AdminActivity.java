package com.example.suivicommandes;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
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
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

public class AdminActivity extends AppCompatActivity implements OrderAdapter.OrderStatusCallback {

    private static final String TAG = "AdminActivity";
    private FirebaseAuth auth;
    private FirebaseFirestore db;
    private TextView adminEmailTextView;
    private Button logoutButton;
    private RecyclerView ordersRecyclerView;
    private List<Order> ordersList = new ArrayList<>();
    private UserOrdersAdapter userOrdersAdapter;

    // Notification helper methods and constants (added)
    private static final String CHANNEL_ID = "order_status_channel";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        try {
            setContentView(R.layout.activity_admin);
            Log.d(TAG, "Content view set successfully");

            auth = FirebaseAuth.getInstance();
            db = FirebaseFirestore.getInstance();

            // Set up the toolbar
            Toolbar toolbar = findViewById(R.id.toolbar);
            setSupportActionBar(toolbar);
            if (getSupportActionBar() != null) {
                getSupportActionBar().setTitle("Admin Dashboard");
            }

            // Check if user is authenticated
            FirebaseUser currentUser = auth.getCurrentUser();
            if (currentUser == null) {
                redirectToLogin();
                return;
            }

            // Find views
            adminEmailTextView = findViewById(R.id.adminEmailTextView);
            logoutButton = findViewById(R.id.logoutButton);
            ordersRecyclerView = findViewById(R.id.ordersRecyclerView);

            // Display user email
            adminEmailTextView.setText("Admin: " + currentUser.getEmail());

            // Set up RecyclerView with the new adapter
            ordersRecyclerView.setLayoutManager(new LinearLayoutManager(this));
            userOrdersAdapter = new UserOrdersAdapter(this, ordersList, this);
            ordersRecyclerView.setAdapter(userOrdersAdapter);

            // Fetch orders from Firestore
            fetchOrders();

            // Set up logout button
            logoutButton.setOnClickListener(v -> signOut());

        } catch (Exception e) {
            Log.e(TAG, "Error initializing AdminActivity", e);
            Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    private void fetchOrders() {
        db.collection("orders")
                .addSnapshotListener((value, error) -> {
                    if (error != null) {
                        Log.e(TAG, "Error listening for order updates", error);
                        Toast.makeText(AdminActivity.this, "Error: " + error.getMessage(), Toast.LENGTH_SHORT).show();
                        return;
                    }

                    if (value != null) {
                        ordersList.clear();
                        Log.d(TAG, "Retrieved " + value.size() + " orders total");

                        for (QueryDocumentSnapshot document : value) {
                            try {
                                Order order = document.toObject(Order.class);

                                // Ensure orderId is set correctly
                                if (order.getOrderId() == null || order.getOrderId().isEmpty()) {
                                    order.setOrderId(document.getId());
                                }

                                // Additional data validation
                                if (order.getUserId() == null || order.getUserId().isEmpty()) {
                                    Log.w(TAG, "Order " + document.getId() + " has no userId, skipping");
                                    continue;
                                }

                                ordersList.add(order);
                                Log.d(TAG, "Added order: " + order.getOrderId() + " for user: " + order.getUserEmail());
                            } catch (Exception e) {
                                Log.e(TAG, "Error processing order document " + document.getId(), e);
                            }
                        }

                        // Update the adapter with the new data
                        userOrdersAdapter.updateData(ordersList);
                        Log.d(TAG, "Updated adapter with " + ordersList.size() + " orders");
                    }
                });
    }

    @Override
    public void onStatusUpdate(String orderId, String newStatus) {
        if (orderId == null || orderId.isEmpty()) {
            Toast.makeText(this, "Invalid order ID", Toast.LENGTH_SHORT).show();
            return;
        }

        Log.d(TAG, "Updating order " + orderId + " status to: " + newStatus);

        // Update the order status in Firestore
        db.collection("orders").document(orderId)
                .update("orderStatus", newStatus, "lastUpdated", new java.util.Date())
                .addOnSuccessListener(aVoid -> {
                    Log.d(TAG, "Order status updated successfully");
                    Toast.makeText(AdminActivity.this, "Order status updated to " + newStatus, Toast.LENGTH_SHORT).show();

                    // Send notification to the user (customer)
                    sendOrderStatusNotification(orderId, newStatus);
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error updating order status", e);
                    Toast.makeText(AdminActivity.this, "Error updating status: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    private void sendOrderStatusNotification(String orderId, String newStatus) {
        // Retrieve the user's email from the order data
        db.collection("orders").document(orderId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        String userEmail = documentSnapshot.getString("userEmail");

                        // Send a notification to the user (customer)
                        if (userEmail != null) {
                            Log.d(TAG, "Sending notification to user: " + userEmail);
                            NotificationHelper.sendOrderStatusNotification(this, userEmail, newStatus);
                        }
                    }
                });
    }

    private void signOut() {
        auth.signOut();
        Toast.makeText(this, "Signed out successfully", Toast.LENGTH_SHORT).show();
        redirectToLogin();
    }

    private void redirectToLogin() {
        Intent intent = new Intent(AdminActivity.this, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }
}
