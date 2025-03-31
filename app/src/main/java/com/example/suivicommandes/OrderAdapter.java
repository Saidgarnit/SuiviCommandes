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

                // Create a better order summary for admin
                String orderTitle;
                String customerEmail = order.getUserEmail() != null ? order.getUserEmail() : "Unknown customer";
                String orderNumber = order.getOrderId() != null ?
                        order.getOrderId().substring(0, Math.min(6, order.getOrderId().length())) : "";

                // Format date nicely
                SimpleDateFormat displayDateFormat = new SimpleDateFormat("dd MMM yyyy, HH:mm", Locale.getDefault());
                String orderDateFormatted = order.getOrderDate() != null ?
                        displayDateFormat.format(order.getOrderDate()) : "";

                // Better title format for admin
                orderTitle = "Order #" + orderNumber + "... from " + customerEmail + "\n" +
                        "Date: " + orderDateFormatted + " • " +
                        "Items: " + (order.getItems() != null ? order.getItems().size() : 0);

                holder.itemNameTextView.setText(orderTitle);

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
                    // Remove last newline
                    if (itemDetails.length() > 0) {
                        itemDetails.setLength(itemDetails.length() - 1);
                    }
                } else {
                    itemDetails.append("No items available");
                }
                holder.itemStatusTextView.setText(itemDetails.toString());

                // Display total price
                double totalPrice = order.getTotalAmount();
                if (totalPrice <= 0 && order.getItems() != null) {
                    // Calculate total from items if not set
                    for (CartItem item : order.getItems()) {
                        totalPrice += item.getTotalPrice();
                    }
                }
                holder.itemPriceTextView.setText("Total: " + currencyFormat.format(totalPrice));

                // Handle status spinner for admin view
                if (holder.statusSpinner != null) {
                    if (isAdminView && statusCallback != null) {
                        holder.statusSpinner.setVisibility(View.VISIBLE);
                        setupStatusSpinner(holder.statusSpinner, order);
                    } else {
                        holder.statusSpinner.setVisibility(View.GONE);
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

        private void setupStatusSpinner(Spinner spinner, Order order) {
            try {
                ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(
                        spinner.getContext(),
                        R.array.order_status_array,
                        android.R.layout.simple_spinner_item);
                adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                spinner.setAdapter(adapter);

                // Set current status
                int spinnerPosition = 0;
                for (int i = 0; i < adapter.getCount(); i++) {
                    if (adapter.getItem(i).toString().equals(order.getOrderStatus())) {
                        spinnerPosition = i;
                        break;
                    }
                }
                spinner.setSelection(spinnerPosition);

                // Handle status changes
                spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
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