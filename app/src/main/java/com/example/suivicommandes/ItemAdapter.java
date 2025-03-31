package com.example.suivicommandes;

import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.RequestOptions;
import java.text.NumberFormat;
import java.util.List;
import java.util.Locale;

public class ItemAdapter extends RecyclerView.Adapter<ItemAdapter.ItemViewHolder> {
    private static final String TAG = "ItemAdapter";
    private final List<Item> itemList;
    private final Context context;
    private final NumberFormat currencyFormat;
    private final RequestOptions imageOptions;

    public ItemAdapter(Context context, List<Item> itemList) {
        this.context = context;
        this.itemList = itemList;
        this.currencyFormat = NumberFormat.getCurrencyInstance(Locale.US);
        this.imageOptions = new RequestOptions()
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .placeholder(R.drawable.placeholder_image)
                .error(R.drawable.error_image)
                .centerCrop();

        Log.d(TAG, "Adapter initialized with " + itemList.size() + " items");
    }

    @NonNull
    @Override
    public ItemViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        try {
            View view = LayoutInflater.from(context).inflate(R.layout.item_layout, parent, false);
            return new ItemViewHolder(view);
        } catch (Exception e) {
            Log.e(TAG, "Error creating view holder", e);
            // Fallback to empty view in case of inflation error
            View fallbackView = new View(context);
            fallbackView.setLayoutParams(new ViewGroup.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT));
            return new ItemViewHolder(fallbackView);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull ItemViewHolder holder, int position) {
        try {
            Item item = itemList.get(position);
            if (item == null) {
                Log.w(TAG, "Null item at position: " + position);
                displayFallbackItem(holder);
                return;
            }

            bindItemData(holder, item);
            setupItemClickListener(holder, item);

        } catch (Exception e) {
            Log.e(TAG, "Error binding view holder at position: " + position, e);
            displayFallbackItem(holder);
        }
    }

    private void bindItemData(@NonNull ItemViewHolder holder, Item item) {
        // Set item name with fallback
        holder.nameTextView.setText(getSafeString(item.getName(), "Unnamed Product"));

        // Format and set price
        holder.priceTextView.setText(formatPrice(item.getPrice()));

        // Set description with fallback
        holder.descriptionTextView.setText(getSafeString(item.getDescription(), "No description available"));

        // Load image with Glide
        loadItemImage(holder.itemImageView, item.getImage());
    }

    private void setupItemClickListener(@NonNull ItemViewHolder holder, Item item) {
        holder.itemView.setOnClickListener(v -> {
            try {
                Log.d(TAG, "Item clicked: " + item.getName());
                launchDetailsActivity(createSafeItem(item));
            } catch (Exception e) {
                Log.e(TAG, "Error handling item click", e);
                showError("Unable to view product details");
            }
        });
    }

    private void launchDetailsActivity(Item safeItem) {
        Intent intent = new Intent(context, ItemDetailsActivity.class);

        // Add all necessary extras with safe values
        intent.putExtra("item_id", safeItem.getItemId());
        intent.putExtra("item_name", safeItem.getName());
        intent.putExtra("item_price", safeItem.getPrice());
        intent.putExtra("item_description", safeItem.getDescription());
        intent.putExtra("item_image", safeItem.getImage());

        // Add timestamp for tracking
        intent.putExtra("opened_at", System.currentTimeMillis());

        try {
            context.startActivity(intent);
            Log.d(TAG, "Launched details activity for item: " + safeItem.getItemId());
        } catch (Exception e) {
            Log.e(TAG, "Error launching details activity", e);
            showError("Unable to open product details");
        }
    }

    private Item createSafeItem(Item originalItem) {
        Item safeItem = new Item();

        // Generate a temporary ID if none exists
        safeItem.setItemId(getSafeString(originalItem.getItemId(),
                "temp_" + System.currentTimeMillis()));

        // Set other properties with safe defaults
        safeItem.setName(getSafeString(originalItem.getName(), "Unknown Product"));
        safeItem.setDescription(getSafeString(originalItem.getDescription(), ""));
        safeItem.setImage(getSafeString(originalItem.getImage(), ""));
        safeItem.setPrice(Math.max(0.0, originalItem.getPrice())); // Ensure non-negative price

        return safeItem;
    }

    private String getSafeString(String value, String defaultValue) {
        return value != null && !value.trim().isEmpty() ? value : defaultValue;
    }

    private String formatPrice(double price) {
        try {
            return currencyFormat.format(Math.max(0.0, price));
        } catch (Exception e) {
            Log.e(TAG, "Error formatting price", e);
            return "$0.00";
        }
    }

    private void loadItemImage(ImageView imageView, String imageUrl) {
        try {
            Glide.with(context)
                    .load(getSafeString(imageUrl, ""))
                    .apply(imageOptions)
                    .into(imageView);
        } catch (Exception e) {
            Log.e(TAG, "Error loading image", e);
            imageView.setImageResource(R.drawable.error_image);
        }
    }

    private void displayFallbackItem(@NonNull ItemViewHolder holder) {
        holder.nameTextView.setText("Product Unavailable");
        holder.priceTextView.setText("$0.00");
        holder.descriptionTextView.setText("Information temporarily unavailable");
        holder.itemImageView.setImageResource(R.drawable.error_image);
        holder.itemView.setOnClickListener(null);
    }

    private void showError(String message) {
        Toast.makeText(context, message, Toast.LENGTH_SHORT).show();
    }

    @Override
    public int getItemCount() {
        return itemList != null ? itemList.size() : 0;
    }

    public void updateItems(List<Item> newItems) {
        this.itemList.clear();
        if (newItems != null) {
            this.itemList.addAll(newItems);
        }
        notifyDataSetChanged();
        Log.d(TAG, "Updated items list. New size: " + itemList.size());
    }

    static class ItemViewHolder extends RecyclerView.ViewHolder {
        final TextView nameTextView;
        final TextView priceTextView;
        final TextView descriptionTextView;
        final ImageView itemImageView;

        public ItemViewHolder(@NonNull View itemView) {
            super(itemView);
            nameTextView = itemView.findViewById(R.id.itemName);
            priceTextView = itemView.findViewById(R.id.itemPrice);
            descriptionTextView = itemView.findViewById(R.id.itemDescription);
            itemImageView = itemView.findViewById(R.id.itemImage);
        }
    }
}