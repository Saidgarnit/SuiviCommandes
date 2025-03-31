package com.example.suivicommandes;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;

public class AdminOrderAdapter extends RecyclerView.Adapter<AdminOrderAdapter.OrderViewHolder> {
    private List<Order> orders;
    private OrderAdapter.OrderStatusCallback statusCallback;

    public AdminOrderAdapter(List<Order> orders, OrderAdapter.OrderStatusCallback callback) {
        this.orders = orders;
        this.statusCallback = callback;
    }

    @NonNull
    @Override
    public OrderViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_admin_order, parent, false);
        return new OrderViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull OrderViewHolder holder, int position) {
        Order order = orders.get(position);
        if (order == null) return;

        // Format currency and date
        NumberFormat currencyFormat = NumberFormat.getCurrencyInstance(Locale.US);
        SimpleDateFormat dateFormat = new SimpleDateFormat("MMM dd, yyyy", Locale.getDefault());
        String orderDate = order.getOrderDate() != null
                ? dateFormat.format(order.getOrderDate())
                : "Date not available";

        // Create a proper order summary
        StringBuilder orderTitle = new StringBuilder();
        if (order.getItems() != null && !order.getItems().isEmpty()) {
            orderTitle.append("Order #")
                    .append(order.getOrderId().substring(0, 5))
                    .append("...");
        } else if (order.getItemName() != null) {
            orderTitle.append(order.getItemName());
        } else {
            orderTitle.append("Order #")
                    .append(order.getOrderId().substring(0, 5))
                    .append("...");
        }
        holder.orderTitleTextView.setText(orderTitle.toString());

        // Show detailed items list
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
            itemDetails.append(order.getItemName())
                    .append(" - ")
                    .append(currencyFormat.format(order.getItemPrice()));
        } else {
            itemDetails.append("No item details available");
        }
        holder.itemDetailsTextView.setText(itemDetails.toString());

        // Show status and date
        holder.statusTextView.setText("Current Status: " + order.getOrderStatus() + " (" + orderDate + ")");

        // Show total price
        double totalPrice = order.getTotalAmount();
        holder.totalTextView.setText("Total: " + currencyFormat.format(totalPrice));

        // Set up the status spinner for admin
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(
                holder.itemView.getContext(),
                R.array.order_status_array,
                android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        holder.statusSpinner.setAdapter(adapter);

        // Set current status
        int position1 = 0;
        for (int i = 0; i < adapter.getCount(); i++) {
            if (adapter.getItem(i).toString().equals(order.getOrderStatus())) {
                position1 = i;
                break;
            }
        }
        holder.statusSpinner.setSelection(position1);

        // Handle status changes
        holder.statusSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            private boolean isInitial = true;

            @Override
            public void onItemSelected(AdapterView<?> parentView, View selectedItemView, int pos, long id) {
                // Skip the initial selection event
                if (isInitial) {
                    isInitial = false;
                    return;
                }

                String newStatus = parentView.getItemAtPosition(pos).toString();
                if (!newStatus.equals(order.getOrderStatus()) && statusCallback != null) {
                    statusCallback.onStatusUpdate(order.getOrderId(), newStatus);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parentView) {
                // Do nothing
            }
        });
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
        Spinner statusSpinner;

        public OrderViewHolder(@NonNull View itemView) {
            super(itemView);
            orderTitleTextView = itemView.findViewById(R.id.orderTitleTextView);
            itemDetailsTextView = itemView.findViewById(R.id.itemDetailsTextView);
            statusTextView = itemView.findViewById(R.id.statusTextView);
            totalTextView = itemView.findViewById(R.id.totalTextView);
            statusSpinner = itemView.findViewById(R.id.statusSpinner);
        }
    }
}