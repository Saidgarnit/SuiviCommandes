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
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_order, parent, false);
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

        // 1. Simplified order title - just order ID
        String orderTitle = "Order #" + order.getOrderId().substring(0, Math.min(5, order.getOrderId().length())) + "...";
        holder.itemNameTextView.setText(orderTitle);

        // 2. Show status with date together
        holder.itemStatusTextView.setText("Status: " + order.getOrderStatus() + " (" + orderDate + ")");

        // 3. Show detailed item list in separate element
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
        holder.orderItemsTextView.setText(itemDetails.toString());

        // 4. Show total price
        double totalPrice = order.getTotalAmount();
        if (totalPrice <= 0 && order.getItems() != null) {
            // If total amount isn't set, calculate it from items
            for (CartItem item : order.getItems()) {
                totalPrice += item.getTotalPrice();
            }
        }
        holder.itemPriceTextView.setText("Total: " + currencyFormat.format(totalPrice));

        // 5. Set up the status spinner for admin
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(
                holder.itemView.getContext(),
                R.array.order_status_array,
                android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        holder.statusSpinner.setAdapter(adapter);

        // 6. Set current status
        int spinnerPosition = 0;
        for (int i = 0; i < adapter.getCount(); i++) {
            if (adapter.getItem(i).toString().equals(order.getOrderStatus())) {
                spinnerPosition = i;
                break;
            }
        }
        holder.statusSpinner.setSelection(spinnerPosition);

        // 7. Handle status changes
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
        TextView itemNameTextView;
        TextView itemStatusTextView;
        TextView orderItemsTextView;
        TextView itemPriceTextView;
        Spinner statusSpinner;

        public OrderViewHolder(@NonNull View itemView) {
            super(itemView);
            itemNameTextView = itemView.findViewById(R.id.itemNameTextView);
            itemStatusTextView = itemView.findViewById(R.id.itemStatusTextView);
            orderItemsTextView = itemView.findViewById(R.id.orderItemsTextView);
            itemPriceTextView = itemView.findViewById(R.id.itemPriceTextView);
            statusSpinner = itemView.findViewById(R.id.statusSpinner);
        }
    }
}