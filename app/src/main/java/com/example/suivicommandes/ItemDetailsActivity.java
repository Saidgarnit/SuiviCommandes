package com.example.suivicommandes;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import com.bumptech.glide.Glide;

public class ItemDetailsActivity extends AppCompatActivity {

    private TextView itemName, itemPrice, itemDescription;
    private ImageView itemImage;
    private Button orderButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_item_details);

        // Set up toolbar
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("Product Details");
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        // Initialize Views
        itemName = findViewById(R.id.itemNameDetail);
        itemPrice = findViewById(R.id.itemPriceDetail);
        itemDescription = findViewById(R.id.itemDescriptionDetail);
        itemImage = findViewById(R.id.itemImageDetail);
        orderButton = findViewById(R.id.orderButton);

        // Get the item details from the Intent
        Item item = (Item) getIntent().getSerializableExtra("item");

        // Set item data to views
        if (item != null) {
            itemName.setText(item.getName());
            itemPrice.setText("Price: $" + item.getPrice());
            itemDescription.setText(item.getDescription());

            if (item.getImage() != null && !item.getImage().isEmpty()) {
                Glide.with(this).load(item.getImage()).into(itemImage);
            }
        }

        // Handle Order Button Click - Change to Add to Cart functionality
        orderButton.setText("Add to Cart");
        orderButton.setOnClickListener(v -> {
            if (item != null) {
                addToCart(item);
            }
        });
    }

    private void addToCart(Item item) {
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
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }
}