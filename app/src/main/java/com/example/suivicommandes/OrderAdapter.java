package com.example.suivicommandes;

import android.util.Log;
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

public class OrderAdapter extends RecyclerView.Adapter<OrderAdapter.OrderViewHolder> {
    private static final String TAG = "OrderAdapter";
    private List<Order> orders;
    private OrderStatusCallback statusCallback;
    private boolean isAdminView;

    // Interface for status update callback
    public interface OrderStatusCallback {
        void onStatusUpdate(String orderId, String newStatus);
    }

    // Constructor with callback (for admin view)
    public OrderAdapter(List<Order> orders, OrderStatusCallback callback) {
        this.orders = orders;
        this.statusCallback = callback;
        this.isAdminView = (callback != null);
    }

    // Constructor without callback (for user view)
    public OrderAdapter(List<Order> orders) {
        this.orders = orders;
        this.statusCallback = null;
        this.isAdminView = false;
    }

    @NonNull
    @Override
    public OrderViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_order, parent, false);
        return new OrderViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull OrderViewHolder holder, int position) {
        try {
            Order order = orders.get(position);

            // Handle null order
            if (order == null) {
                Log.e(TAG, "Null order at position " + position);
                holder.itemNameTextView.setText("Error: Invalid Order");
                return;
            }

            // Format currency
            NumberFormat currencyFormat = NumberFormat.getCurrencyInstance(Locale.US);

            // Format date if available
            SimpleDateFormat dateFormat = new SimpleDateFormat("MMM dd, yyyy", Locale.getDefault());
            String orderDate = order.getOrderDate() != null
                    ? dateFormat.format(order.getOrderDate())
                    : "Date not available";

            // Build the item name text - show multiple items if present
            StringBuilder itemNameBuilder = new StringBuilder();

            // Check if there are items in the items array
            if (order.getItems() != null && !order.getItems().isEmpty()) {
                int itemCount = order.getItems().size();
                if (itemCount == 1) {
                    // Single item
                    itemNameBuilder.append(order.getItems().get(0).getName());
                } else {
                    // Multiple items - list them all
                    itemNameBuilder.append("Order with ").append(itemCount).append(" items:");
                    for (int i = 0; i < Math.min(itemCount, 3); i++) { // Show up to 3 items
                        CartItem item = order.getItems().get(i);
                        itemNameBuilder.append("\n• ").append(item.getName())
                                .append(" (x").append(item.getQuantity()).append(")");
                    }
                    if (itemCount > 3) {
                        itemNameBuilder.append("\n• ...and ").append(itemCount - 3).append(" more");
                    }
                }
            } else if (order.getItemName() != null) {
                // Fallback to itemName field if available
                itemNameBuilder.append(order.getItemName());
            } else {
                itemNameBuilder.append("Unknown Items");
            }

            holder.itemNameTextView.setText(itemNameBuilder.toString());

            // Display status with more details
            String statusText = "Status: " + (order.getOrderStatus() != null ? order.getOrderStatus() : "Unknown");
            statusText += " (" + orderDate + ")";
            holder.itemStatusTextView.setText(statusText);

            // Display the total price of the order, not just the first item
            if (order.getTotalAmount() > 0) {
                holder.itemPriceTextView.setText(currencyFormat.format(order.getTotalAmount()));
            } else if (order.getItemPrice() > 0) {
                holder.itemPriceTextView.setText(currencyFormat.format(order.getItemPrice()));
            } else {
                // Calculate total from items if available
                double total = 0;
                if (order.getItems() != null) {
                    for (CartItem item : order.getItems()) {
                        total += item.getTotalPrice();
                    }
                }
                holder.itemPriceTextView.setText(currencyFormat.format(total));
            }

            // IMPORTANT: Status spinner ONLY visible for admin view
            if (holder.statusSpinner != null) {
                if (isAdminView && statusCallback != null) {
                    holder.statusSpinner.setVisibility(View.VISIBLE);

                    // Set up status spinner
                    try {
                        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(
                                holder.itemView.getContext(),
                                R.array.order_status_array,
                                android.R.layout.simple_spinner_item);
                        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                        holder.statusSpinner.setAdapter(adapter);

                        // Set current status in spinner
                        int spinnerPosition = 0;
                        for (int i = 0; i < adapter.getCount(); i++) {
                            if (adapter.getItem(i).toString().equals(order.getOrderStatus())) {
                                spinnerPosition = i;
                                break;
                            }
                        }
                        holder.statusSpinner.setSelection(spinnerPosition);

                        // Set listener for status change
                        holder.statusSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                            private boolean isInitial = true;

                            @Override
                            public void onItemSelected(AdapterView<?> parentView, View selectedItemView, int pos, long id) {
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
                    } catch (Exception e) {
                        Log.e(TAG, "Error setting up spinner", e);
                    }
                } else {
                    // Hide spinner for regular users
                    holder.statusSpinner.setVisibility(View.GONE);

                    // Hide the "Change Status:" label as well if it exists
                    View changeStatusLabel = holder.itemView.findViewById(R.id.changeStatusLabel);
                    if (changeStatusLabel != null) {
                        changeStatusLabel.setVisibility(View.GONE);
                    }
                }
            }
        } catch (Exception e) {
            Log.e(TAG, "Error binding view holder", e);
            holder.itemNameTextView.setText("Error displaying order");
        }
    }

    @Override
    public int getItemCount() {
        return orders != null ? orders.size() : 0;
    }

    static class OrderViewHolder extends RecyclerView.ViewHolder {
        TextView itemNameTextView;
        TextView itemStatusTextView;
        TextView itemPriceTextView;
        Spinner statusSpinner;

        public OrderViewHolder(@NonNull View itemView) {
            super(itemView);
            itemNameTextView = itemView.findViewById(R.id.itemNameTextView);
            itemStatusTextView = itemView.findViewById(R.id.itemStatusTextView);
            itemPriceTextView = itemView.findViewById(R.id.itemPriceTextView);
            statusSpinner = itemView.findViewById(R.id.statusSpinner);
        }
    }
}