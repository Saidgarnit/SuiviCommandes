package com.example.suivicommandes;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import java.text.NumberFormat;
import java.util.List;
import java.util.Locale;

public class CartAdapter extends RecyclerView.Adapter<CartAdapter.CartViewHolder> {

    private Context context;
    private List<CartItem> cartItems;
    private CartItemListener itemListener;

    public interface CartItemListener {
        void onQuantityChanged(CartItem item, int newQuantity);
    }

    public CartAdapter(Context context, List<CartItem> cartItems, CartItemListener listener) {
        this.context = context;
        this.cartItems = cartItems;
        this.itemListener = listener;
    }

    @NonNull
    @Override
    public CartViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_cart, parent, false);
        return new CartViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CartViewHolder holder, int position) {
        CartItem item = cartItems.get(position);

        // Set item details
        holder.nameTextView.setText(item.getName());

        // Format prices
        NumberFormat currencyFormat = NumberFormat.getCurrencyInstance(Locale.US);
        holder.priceTextView.setText(currencyFormat.format(item.getPrice()));
        holder.totalTextView.setText(currencyFormat.format(item.getTotalPrice()));

        // Set quantity
        holder.quantityTextView.setText(String.valueOf(item.getQuantity()));

        // Load image with Glide
        Glide.with(context)
                .load(item.getImage())
                .placeholder(R.drawable.ic_placeholder)
                .error(R.drawable.ic_error)
                .into(holder.itemImageView);

        // Set click listeners for quantity buttons
        holder.decreaseButton.setOnClickListener(v -> {
            int newQuantity = item.getQuantity() - 1;
            itemListener.onQuantityChanged(item, newQuantity);
        });

        holder.increaseButton.setOnClickListener(v -> {
            int newQuantity = item.getQuantity() + 1;
            itemListener.onQuantityChanged(item, newQuantity);
        });

        // Set click listener for remove button
        holder.removeButton.setOnClickListener(v -> {
            itemListener.onQuantityChanged(item, 0);
        });
    }

    @Override
    public int getItemCount() {
        return cartItems.size();
    }

    static class CartViewHolder extends RecyclerView.ViewHolder {
        ImageView itemImageView;
        TextView nameTextView, priceTextView, quantityTextView, totalTextView;
        ImageButton decreaseButton, increaseButton, removeButton;

        public CartViewHolder(@NonNull View itemView) {
            super(itemView);
            itemImageView = itemView.findViewById(R.id.itemImage);
            nameTextView = itemView.findViewById(R.id.itemName);
            priceTextView = itemView.findViewById(R.id.itemPrice);
            quantityTextView = itemView.findViewById(R.id.quantityText);
            totalTextView = itemView.findViewById(R.id.itemTotal);
            decreaseButton = itemView.findViewById(R.id.decreaseButton);
            increaseButton = itemView.findViewById(R.id.increaseButton);
            removeButton = itemView.findViewById(R.id.removeButton);
        }
    }
}