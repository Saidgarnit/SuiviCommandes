package com.example.suivicommandes;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import com.bumptech.glide.Glide;

public class ItemDetailsActivity extends AppCompatActivity {

    private static final String TAG = "ItemDetailsActivity";
    private TextView itemName, itemPrice, itemDescription;
    private ImageView itemImage;
    private Button orderButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_item_details);

        Log.d(TAG, "onCreate started");

        // Set up toolbar with proper error handling
        try {
            Toolbar toolbar = findViewById(R.id.toolbar);
            if (toolbar != null) {
                setSupportActionBar(toolbar);
                if (getSupportActionBar() != null) {
                    getSupportActionBar().setTitle("Product Details");
                    getSupportActionBar().setDisplayHomeAsUpEnabled(true);
                }
            } else {
                Log.e(TAG, "Toolbar not found in layout");
            }
        } catch (Exception e) {
            Log.e(TAG, "Error setting up toolbar", e);
        }

        // Initialize Views
        itemName = findViewById(R.id.itemNameDetail);
        itemPrice = findViewById(R.id.itemPriceDetail);
        itemDescription = findViewById(R.id.itemDescriptionDetail);
        itemImage = findViewById(R.id.itemImageDetail);
        orderButton = findViewById(R.id.orderButton);

        // Try to get item details from intent extras
        try {
            // First check for individual extras (which is what your adapter is actually sending)
            if (getIntent().hasExtra("item_name")) {
                // Get the individual extras
                String name = getIntent().getStringExtra("item_name");
                double price = getIntent().getDoubleExtra("item_price", 0.0);
                String description = getIntent().getStringExtra("item_description");
                String image = getIntent().getStringExtra("item_image");
                String itemId = getIntent().getStringExtra("item_id");

                Log.d(TAG, "Received individual extras - Name: " + name);

                // Create an Item object from the extras
                Item item = new Item();
                item.setItemId(itemId);
                item.setName(name);
                item.setPrice(price);
                item.setDescription(description);
                item.setImage(image);

                // Display the item details
                displayItem(item);
            }
            // Fallback to serialized item (though this isn't being used in your current code)
            else {
                Item item = (Item) getIntent().getSerializableExtra("item");
                if (item != null) {
                    Log.d(TAG, "Received serialized item");
                    displayItem(item);
                } else {
                    throw new Exception("No item data found in intent");
                }
            }
        } catch (Exception e) {
            Log.e(TAG, "Error retrieving item details", e);
            Toast.makeText(this, "Product details not available: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            finish();
        }
    }

    private void displayItem(Item item) {
        // Set item data to views
        itemName.setText(item.getName());
        itemPrice.setText(String.format("Price: $%.2f", item.getPrice()));
        itemDescription.setText(item.getDescription());

        if (item.getImage() != null && !item.getImage().isEmpty()) {
            Glide.with(this).load(item.getImage()).into(itemImage);
        }

        // Handle Order Button Click
        orderButton.setText("Add to Cart");
        orderButton.setOnClickListener(v -> {
            try {
                addToCart(item);
            } catch (Exception e) {
                Log.e(TAG, "Error adding item to cart", e);
                Toast.makeText(ItemDetailsActivity.this,
                        "Failed to add to cart: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void addToCart(Item item) {
        // Validate item
        if (item == null) {
            Toast.makeText(this, "Invalid item", Toast.LENGTH_SHORT).show();
            return;
        }

        if (item.getItemId() == null || item.getItemId().isEmpty()) {
            Log.w(TAG, "Item ID is null or empty");
            // Generate a temporary ID to prevent crashes
            item.setItemId("temp_" + System.currentTimeMillis());
        }

        try {
            CartManager cartManager = CartManager.getInstance(this);
            cartManager.addItem(item);

            // Show feedback
            Toast.makeText(ItemDetailsActivity.this, "Added to cart", Toast.LENGTH_SHORT).show();

            // Option to go to cart
            androidx.appcompat.app.AlertDialog.Builder builder = new androidx.appcompat.app.AlertDialog.Builder(this);
            builder.setTitle("Item Added to Cart")
                    .setMessage("Do you want to view your cart or continue shopping?")
                    .setPositiveButton("View Cart", (dialog, which) -> {
                        Intent intent = new Intent(ItemDetailsActivity.this, CartActivity.class);
                        startActivity(intent);
                    })
                    .setNegativeButton("Continue Shopping", (dialog, which) -> dialog.dismiss())
                    .show();
        } catch (Exception e) {
            Log.e(TAG, "Exception in addToCart", e);
            Toast.makeText(this, "Error adding to cart: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }
}