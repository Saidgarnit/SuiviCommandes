package com.example.suivicommandes;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import java.util.ArrayList;
import java.util.List;

public class OrdersActivity extends AppCompatActivity {
    private static final String TAG = "OrdersActivity";
    private RecyclerView ordersRecyclerView;
    private List<Order> orderList = new ArrayList<>();
    private ClientOrderAdapter clientOrderAdapter; // Declare this at the class level
    private TextView noOrdersTextView;
    private FirebaseFirestore db;
    private FirebaseAuth auth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        try {
            setContentView(R.layout.activity_orders);

            // Initialize Firebase
            auth = FirebaseAuth.getInstance();
            db = FirebaseFirestore.getInstance();

            // Set up toolbar
            Toolbar toolbar = findViewById(R.id.toolbar);
            if (toolbar != null) {
                setSupportActionBar(toolbar);
                if (getSupportActionBar() != null) {
                    getSupportActionBar().setTitle("My Orders");
                    getSupportActionBar().setDisplayHomeAsUpEnabled(true);
                }
            }

            // Find views
            ordersRecyclerView = findViewById(R.id.ordersRecyclerView);
            noOrdersTextView = findViewById(R.id.noOrdersTextView);

            // Set up RecyclerView with CLIENT-SPECIFIC adapter
            ordersRecyclerView.setLayoutManager(new LinearLayoutManager(this));

            // Initialize the adapter here - BEFORE use
            clientOrderAdapter = new ClientOrderAdapter(orderList);
            ordersRecyclerView.setAdapter(clientOrderAdapter);

            // Fetch orders
            fetchOrders();
        } catch (Exception e) {
            Log.e(TAG, "Error initializing OrdersActivity", e);
            Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    private void processOrderSnapshot(com.google.firebase.firestore.QuerySnapshot snapshot) {
        try {
            orderList.clear();

            for (QueryDocumentSnapshot doc : snapshot) {
                try {
                    Order order = doc.toObject(Order.class);

                    // Ensure orderId is set
                    if (order.getOrderId() == null) {
                        order.setOrderId(doc.getId());
                    }

                    // Add null checks for safety
                    if (order.getUserId() != null && order.getUserId().equals(auth.getCurrentUser().getUid())) {
                        orderList.add(order);
                        Log.d(TAG, "Added order: " + order.getOrderId());
                    }
                } catch (Exception e) {
                    Log.e(TAG, "Error processing order document", e);
                }
            }

            // Update the CLIENT adapter
            clientOrderAdapter.notifyDataSetChanged();

            // Show/hide the "no orders" message
            if (orderList.isEmpty() && noOrdersTextView != null) {
                noOrdersTextView.setVisibility(View.VISIBLE);
                ordersRecyclerView.setVisibility(View.GONE);
            } else if (noOrdersTextView != null) {
                noOrdersTextView.setVisibility(View.GONE);
                ordersRecyclerView.setVisibility(View.VISIBLE);
            }

            // Log the results
            Log.d(TAG, "Processed " + orderList.size() + " orders for current user");
        } catch (Exception e) {
            Log.e(TAG, "Error processing order snapshot", e);
        }
    }

    private void fetchOrders() {
        try {
            if (auth.getCurrentUser() == null) {
                Log.e(TAG, "No authenticated user found");
                Toast.makeText(this, "You must be logged in to view orders", Toast.LENGTH_SHORT).show();
                finish();
                return;
            }

            String userId = auth.getCurrentUser().getUid();
            Log.d(TAG, "Fetching orders for user ID: " + userId);

            // Use only ONE approach - the snapshot listener for real-time updates
            db.collection("orders")
                    .whereEqualTo("userId", userId)
                    .addSnapshotListener((queryDocumentSnapshots, e) -> {
                        if (e != null) {
                            Log.e(TAG, "Error fetching orders", e);
                            Toast.makeText(OrdersActivity.this, "Error fetching orders", Toast.LENGTH_SHORT).show();
                            return;
                        }

                        if (queryDocumentSnapshots != null) {
                            Log.d(TAG, "Snapshot listener found " + queryDocumentSnapshots.size() + " orders");
                            processOrderSnapshot(queryDocumentSnapshots);
                        }
                    });
        } catch (Exception e) {
            Log.e(TAG, "Error setting up orders query", e);
            Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }
}