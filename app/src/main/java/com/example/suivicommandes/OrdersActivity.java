package com.example.suivicommandes;

import android.os.Bundle;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

public class OrdersActivity extends AppCompatActivity {
    private RecyclerView ordersRecyclerView;
    private List<Order> orderList = new ArrayList<>();
    private OrderAdapter orderAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_orders);

        // Set up RecyclerView
        ordersRecyclerView = findViewById(R.id.ordersRecyclerView);
        ordersRecyclerView.setLayoutManager(new LinearLayoutManager(this));

        orderAdapter = new OrderAdapter(orderList);
        ordersRecyclerView.setAdapter(orderAdapter);

        fetchOrders();  // Fetch orders from Firestore
    }

    private void fetchOrders() {
        FirebaseAuth auth = FirebaseAuth.getInstance();
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        // Get the orders for the current user
        db.collection("orders")
                .whereEqualTo("userId", auth.getCurrentUser().getUid())
                .addSnapshotListener((queryDocumentSnapshots, e) -> {
                    if (e != null) {
                        Toast.makeText(OrdersActivity.this, "Error fetching orders", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    orderList.clear();
                    for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                        Order order = doc.toObject(Order.class);
                        orderList.add(order);
                    }

                    orderAdapter.notifyDataSetChanged();  // Update the RecyclerView
                });
    }

}
