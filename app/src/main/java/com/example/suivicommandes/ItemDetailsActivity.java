package com.example.suivicommandes;

import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.bumptech.glide.Glide;


import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.DocumentReference;

public class ItemDetailsActivity extends AppCompatActivity {

    private TextView itemName, itemPrice, itemDescription;
    private ImageView itemImage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_item_details);

        // Initialize Views
        itemName = findViewById(R.id.itemNameDetail);
        itemPrice = findViewById(R.id.itemPriceDetail);
        itemDescription = findViewById(R.id.itemDescriptionDetail);
        itemImage = findViewById(R.id.itemImageDetail);

        // Get the item details from the Intent
        Item item = (Item) getIntent().getSerializableExtra("item");

        // Set item data to views
        if (item != null) {
            itemName.setText(item.getName());
            itemPrice.setText("Price: $" + item.getPrice());
            itemDescription.setText(item.getDescription());
            Glide.with(this).load(item.getImage()).into(itemImage);
        }

        // Handle Order Button Click
        findViewById(R.id.orderButton).setOnClickListener(v -> placeOrder(item));
    }

    private void placeOrder(Item item) {
        // Get Firebase instance
        FirebaseAuth auth = FirebaseAuth.getInstance();
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        if (auth.getCurrentUser() != null) {
            // Create order object with initial status "pending"
            Order order = new Order(
                    item.getName(),
                    item.getPrice(),
                    item.getDescription(),
                    item.getImage(),
                    auth.getCurrentUser().getUid(),
                    "pending" // Default status
            );

            // Save order to Firestore
            db.collection("orders")
                    .add(order)
                    .addOnSuccessListener(documentReference -> {
                        Toast.makeText(ItemDetailsActivity.this, "Order placed successfully!", Toast.LENGTH_SHORT).show();
                        finish(); // Close the activity after placing the order
                    })
                    .addOnFailureListener(e -> {
                        Toast.makeText(ItemDetailsActivity.this, "Failed to place order: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    });
        }
    }

}

