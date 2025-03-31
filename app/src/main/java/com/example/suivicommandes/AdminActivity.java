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
import com.google.firebase.firestore.DocumentReference;
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
                        Toast.makeText(AdminActivity.this, "Error: " + error.getMessage(), Toast.LENGTH_SHORT).show();
                        return;
                    }

                    if (value != null) {
                        ordersList.clear();
                        for (QueryDocumentSnapshot document : value) {
                            try {
                                // Manually extract order data for robustness
                                Order order = new Order();
                                order.setOrderId(document.getId());
                                order.setUserId(document.getString("userId"));
                                order.setUserEmail(document.getString("userEmail"));
                                order.setOrderStatus(document.getString("orderStatus"));

                                if (document.contains("itemName")) {
                                    order.setItemName(document.getString("itemName"));
                                }

                                if (document.contains("itemPrice")) {
                                    Double price = document.getDouble("itemPrice");
                                    if (price != null) order.setItemPrice(price);
                                }

                                if (document.contains("totalAmount")) {
                                    Double total = document.getDouble("totalAmount");
                                    if (total != null) order.setTotalAmount(total);
                                }

                                if (document.contains("orderDate")) {
                                    order.setOrderDate(document.getDate("orderDate"));
                                }

                                ordersList.add(order);
                                Log.d(TAG, "Added order: " + order.getOrderId() + " for user: " + order.getUserEmail());
                            } catch (Exception e) {
                                Log.e(TAG, "Error processing order document", e);
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

        DocumentReference orderRef = db.collection("orders").document(orderId);

        // Update the order status
        orderRef.update("orderStatus", newStatus)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(AdminActivity.this, "Order status updated to " + newStatus, Toast.LENGTH_SHORT).show();
                    // Removed notification code
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(AdminActivity.this, "Error updating status: " + e.getMessage(), Toast.LENGTH_SHORT).show();
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