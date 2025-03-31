package com.example.suivicommandes;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
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
        if (order == null) return;

        // Format currency
        NumberFormat currencyFormat = NumberFormat.getCurrencyInstance(Locale.US);

        // Format date
        SimpleDateFormat dateFormat = new SimpleDateFormat("MMM dd, yyyy", Locale.getDefault());
        String orderDate = order.getOrderDate() != null
                ? dateFormat.format(order.getOrderDate())
                : "Date not available";

        // 1. Show list of items or order title
        StringBuilder orderTitle = new StringBuilder();
        if (order.getItems() != null && !order.getItems().isEmpty()) {
            // Count total items
            int totalQuantity = 0;
            for (CartItem item : order.getItems()) {
                totalQuantity += item.getQuantity();
            }

            orderTitle.append("Order with ")
                    .append(totalQuantity)
                    .append(" item(s)");
        } else {
            orderTitle.append("Order #")
                    .append(order.getOrderId().substring(0, 5))
                    .append("...");
        }
        holder.orderTitleTextView.setText(orderTitle.toString());

        // 2. Show detailed item list
        StringBuilder itemDetails = new StringBuilder();
        if (order.getItems() != null && !order.getItems().isEmpty()) {
            for (CartItem item : order.getItems()) {
                itemDetails.append("• ")
                        .append(item.getName())
                        .append(" (")
                        .append(item.getQuantity())
                        .append(" × ")
                        .append(currencyFormat.format(item.getPrice()))
                        .append(")\n");
            }
            // Remove the last newline
            if (itemDetails.length() > 0) {
                itemDetails.setLength(itemDetails.length() - 1);
            }
        } else if (order.getItemName() != null) {
            itemDetails.append(order.getItemName());
        } else {
            itemDetails.append("No items available");
        }
        holder.itemDetailsTextView.setText(itemDetails.toString());

        // 3. Show status and date
        String statusText = "Status: " + order.getOrderStatus() + " (" + orderDate + ")";
        holder.statusTextView.setText(statusText);

        // 4. Show the TOTAL amount, not just first item
        double totalPrice = order.getTotalAmount();
        if (totalPrice <= 0 && order.getItems() != null) {
            // If total amount isn't set, calculate it from items
            for (CartItem item : order.getItems()) {
                totalPrice += item.getTotalPrice();
            }
        }
        holder.totalTextView.setText("Total: " + currencyFormat.format(totalPrice));
    }

    @Override
    public int getItemCount() {
        return orders != null ? orders.size() : 0;
    }

    static class OrderViewHolder extends RecyclerView.ViewHolder {
        TextView orderTitleTextView;
        TextView itemDetailsTextView;
        TextView statusTextView;
        TextView totalTextView;

        public OrderViewHolder(@NonNull View itemView) {
            super(itemView);
            orderTitleTextView = itemView.findViewById(R.id.orderTitleTextView);
            itemDetailsTextView = itemView.findViewById(R.id.itemDetailsTextView);
            statusTextView = itemView.findViewById(R.id.statusTextView);
            totalTextView = itemView.findViewById(R.id.totalTextView);
        }
    }
}