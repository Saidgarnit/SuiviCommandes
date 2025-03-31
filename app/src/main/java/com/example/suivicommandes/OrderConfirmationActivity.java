package com.example.suivicommandes;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import java.text.DateFormat;
import java.text.NumberFormat;
import java.util.Locale;

public class OrderConfirmationActivity extends AppCompatActivity {

    private TextView orderIdText, dateText, statusText, totalText, itemCountText;
    private Button viewOrdersButton, continueShopping;
    private FirebaseFirestore db;
    private String orderId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_order_confirmation);

        // Set up toolbar
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("Commande Confirmée");
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        // Initialize views
        orderIdText = findViewById(R.id.orderIdText);
        dateText = findViewById(R.id.dateText);
        statusText = findViewById(R.id.statusText);
        totalText = findViewById(R.id.totalText);
        itemCountText = findViewById(R.id.itemCountText);
        viewOrdersButton = findViewById(R.id.viewOrdersButton);
        continueShopping = findViewById(R.id.continueShoppingButton);

        // Initialize Firestore
        db = FirebaseFirestore.getInstance();

        // Get order ID from intent
        orderId = getIntent().getStringExtra("ORDER_ID");
        if (orderId != null) {
            fetchOrderDetails(orderId);
        }

        // Button click listeners
        viewOrdersButton.setOnClickListener(v -> {
            Intent intent = new Intent(OrderConfirmationActivity.this, OrdersActivity.class);
            startActivity(intent);
            finish();
        });

        continueShopping.setOnClickListener(v -> {
            Intent intent = new Intent(OrderConfirmationActivity.this, HomeActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(intent);
            finish();
        });
    }

    private void fetchOrderDetails(String orderId) {
        db.collection("orders").document(orderId)
                .get()
                .addOnSuccessListener(this::displayOrderDetails)
                .addOnFailureListener(e -> {
                    // Handle errors
                });
    }

    private void displayOrderDetails(DocumentSnapshot document) {
        if (document.exists()) {
            Order order = document.toObject(Order.class);
            if (order != null) {
                // Format the order ID
                orderIdText.setText("Commande #" + orderId);

                // Format the date
                DateFormat dateFormat = DateFormat.getDateTimeInstance(DateFormat.MEDIUM, DateFormat.SHORT, Locale.getDefault());
                if (order.getOrderDate() != null) {
                    dateText.setText(dateFormat.format(order.getOrderDate()));
                }

                // Show status
                statusText.setText(order.getFormattedStatus());

                // Format the total
                NumberFormat currencyFormat = NumberFormat.getCurrencyInstance(Locale.US);
                totalText.setText(currencyFormat.format(order.getTotalAmount()));

                // Show item count
                int itemCount = order.getItemCount();
                itemCountText.setText(itemCount + " article" + (itemCount > 1 ? "s" : ""));
            }
        }
    }

    @Override
    public boolean onSupportNavigateUp() {
        Intent intent = new Intent(this, HomeActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
        finish();
        return true;
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        Intent intent = new Intent(this, HomeActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
        finish();
    }
}