package com.example.suivicommandes;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class UserOrdersAdapter extends RecyclerView.Adapter<UserOrdersAdapter.UserViewHolder> {

    private Context context;
    private List<UserOrderGroup> userGroups;
    private OrderAdapter.OrderStatusCallback statusCallback;

    public static class UserOrderGroup {
        private String userId;
        private String userEmail;
        private List<Order> orders;

        public UserOrderGroup(String userId, String userEmail) {
            this.userId = userId;
            this.userEmail = userEmail;
            this.orders = new ArrayList<>();
        }

        public void addOrder(Order order) {
            orders.add(order);
        }

        public String getUserId() {
            return userId;
        }

        public String getUserEmail() {
            return userEmail;
        }

        public List<Order> getOrders() {
            return orders;
        }

        public int getOrderCount() {
            return orders.size();
        }
    }

    public UserOrdersAdapter(Context context, List<Order> allOrders, OrderAdapter.OrderStatusCallback callback) {
        this.context = context;
        this.statusCallback = callback;
        this.userGroups = groupOrdersByUser(allOrders);
    }

    private List<UserOrderGroup> groupOrdersByUser(List<Order> allOrders) {
        Map<String, UserOrderGroup> groupMap = new HashMap<>();

        for (Order order : allOrders) {
            String userId = order.getUserId();
            String userEmail = order.getUserEmail();

            if (userId == null) continue;

            UserOrderGroup group = groupMap.get(userId);
            if (group == null) {
                group = new UserOrderGroup(userId, userEmail != null ? userEmail : "Unknown User");
                groupMap.put(userId, group);
            }

            group.addOrder(order);
        }

        return new ArrayList<>(groupMap.values());
    }

    @NonNull
    @Override
    public UserViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_user_orders, parent, false);
        return new UserViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull UserViewHolder holder, int position) {
        UserOrderGroup group = userGroups.get(position);

        // Set up the nested RecyclerView for this user's orders
        holder.ordersRecyclerView.setLayoutManager(new LinearLayoutManager(context));
        AdminOrderAdapter orderAdapter = new AdminOrderAdapter(group.getOrders(), statusCallback);
        holder.ordersRecyclerView.setAdapter(orderAdapter);

        holder.userEmailTextView.setText(group.getUserEmail());
        holder.orderCountTextView.setText(group.getOrderCount() + " orders");

        // Set up the expand/collapse functionality
        holder.itemView.setOnClickListener(v -> {
            boolean isExpanded = holder.ordersRecyclerView.getVisibility() == View.VISIBLE;
            holder.ordersRecyclerView.setVisibility(isExpanded ? View.GONE : View.VISIBLE);
            holder.expandIndicator.setText(isExpanded ? "▶" : "▼");
        });
    }

    @Override
    public int getItemCount() {
        return userGroups.size();
    }

    public void updateData(List<Order> newOrders) {
        this.userGroups = groupOrdersByUser(newOrders);
        notifyDataSetChanged();
    }

    static class UserViewHolder extends RecyclerView.ViewHolder {
        TextView userEmailTextView;
        TextView orderCountTextView;
        TextView expandIndicator;
        RecyclerView ordersRecyclerView;

        public UserViewHolder(@NonNull View itemView) {
            super(itemView);
            userEmailTextView = itemView.findViewById(R.id.userEmailTextView);
            orderCountTextView = itemView.findViewById(R.id.orderCountTextView);
            expandIndicator = itemView.findViewById(R.id.expandIndicator);
            ordersRecyclerView = itemView.findViewById(R.id.userOrdersRecyclerView);
        }
    }
}