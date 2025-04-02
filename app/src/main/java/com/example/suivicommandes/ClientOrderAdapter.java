package com.example.suivicommandes;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;

public class ClientOrderAdapter extends RecyclerView.Adapter<ClientOrderAdapter.OrderViewHolder> {
    private static final String TAG = "ClientOrderAdapter";
    private List<Order> orders;

    public ClientOrderAdapter(List<Order> orders) {
        this.orders = orders;
    }

    @NonNull
    @Override
    public OrderViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_client_order, parent, false);
        return new OrderViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull OrderViewHolder holder, int position) {
        Order order = orders.get(position);
        if (order == null) {
            holder.itemNameTextView.setText("Error: Invalid Order");
            return;
        }

        // Format currency
        NumberFormat currencyFormat = NumberFormat.getCurrencyInstance(Locale.US);

        // Get order number (first 6 chars)
        String orderNumber = order.getOrderId() != null ?
                order.getOrderId().substring(0, Math.min(6, order.getOrderId().length())) : "";

        // Format date nicely
        SimpleDateFormat displayDateFormat = new SimpleDateFormat("dd MMM yyyy, HH:mm", Locale.getDefault());
        String orderDateFormatted = order.getOrderDate() != null ?
                displayDateFormat.format(order.getOrderDate()) : "";

        // Set title
        holder.itemNameTextView.setText("Order #" + orderNumber + "...\n" +
                "Date: " + orderDateFormatted);

        // Set status text
        String status = order.getOrderStatus() != null ? order.getOrderStatus() : "Unknown";
        holder.itemStatusTextView.setText("Status: " + status);

        // Set order status icon based on status
        if (holder.orderStatusIcon != null) {
            String statusLower = status.toLowerCase();
            if (statusLower.contains("pending")) {
                holder.orderStatusIcon.setImageResource(R.drawable.ic_pending);
            } else if (statusLower.contains("preparing")) {
                holder.orderStatusIcon.setImageResource(R.drawable.ic_preparing);
            } else if (statusLower.contains("shipped")) {
                holder.orderStatusIcon.setImageResource(R.drawable.ic_shipped);
            } else if (statusLower.contains("delivering")) {
                holder.orderStatusIcon.setImageResource(R.drawable.ic_delivering);
            } else if (statusLower.contains("delivered")) {
                holder.orderStatusIcon.setImageResource(R.drawable.ic_delivered);
            }
        }

        // Calculate and set total price
        double totalPrice = order.getTotalAmount();
        if (totalPrice <= 0 && order.getItems() != null) {
            for (CartItem item : order.getItems()) {
                totalPrice += item.getTotalPrice();
            }
        }
        holder.itemPriceTextView.setText("Total: " + currencyFormat.format(totalPrice));
    }

    @Override
    public int getItemCount() {
        return orders != null ? orders.size() : 0;
    }

    static class OrderViewHolder extends RecyclerView.ViewHolder {
        TextView itemNameTextView;
        TextView itemStatusTextView;
        TextView itemPriceTextView;
        ImageView orderStatusIcon;

        public OrderViewHolder(@NonNull View itemView) {
            super(itemView);
            itemNameTextView = itemView.findViewById(R.id.itemNameTextView);
            itemStatusTextView = itemView.findViewById(R.id.itemStatusTextView);
            itemPriceTextView = itemView.findViewById(R.id.itemPriceTextView);
            orderStatusIcon = itemView.findViewById(R.id.orderStatusIcon);
        }
    }
}