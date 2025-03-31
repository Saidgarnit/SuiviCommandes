package com.example.suivicommandes;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
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
    private OrderAdapter orderAdapter;
    private TextView noOrdersTextView;
    private FirebaseFirestore db;
    private FirebaseAuth auth;
// Inside OrdersActivity.java - update the onCreate method:

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

            // Use the new ClientOrderAdapter explicitly
            clientOrderAdapter = new ClientOrderAdapter(orderList);
            ordersRecyclerView.setAdapter(clientOrderAdapter);

            // Fetch orders
            fetchOrders();
        } catch (Exception e) {
            Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    // Update the processOrderSnapshot method to work with the new adapter:
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

                    orderList.add(order);
                } catch (Exception e) {
                    // Log error but continue processing other orders
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
        } catch (Exception e) {
            // Log error handling
        }
    }

    // Add this field to the class
    private ClientOrderAdapter clientOrderAdapter;

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

            // IMPORTANT: Debug all orders to see what's in the database
            db.collection("orders").get().addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    Log.d(TAG, "All orders in database: " + task.getResult().size());
                    for (QueryDocumentSnapshot document : task.getResult()) {
                        Log.d(TAG, "Order ID: " + document.getId() +
                                ", User ID: " + document.getString("userId"));
                    }
                }
            });

            // Try both direct get and snapshot listener approaches
            // Approach 1: Direct get
            db.collection("orders")
                    .whereEqualTo("userId", userId)
                    .get()
                    .addOnSuccessListener(queryDocumentSnapshots -> {
                        Log.d(TAG, "Direct query found " + queryDocumentSnapshots.size() + " orders");
                        processOrderSnapshot(queryDocumentSnapshots);
                    })
                    .addOnFailureListener(e -> Log.e(TAG, "Error with direct query", e));

            // Approach 2: Snapshot listener (will update in real-time)
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