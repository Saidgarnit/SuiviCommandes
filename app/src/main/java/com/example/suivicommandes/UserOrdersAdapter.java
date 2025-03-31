package com.example.suivicommandes;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class UserOrdersAdapter extends RecyclerView.Adapter<UserOrdersAdapter.UserViewHolder> {
    private static final String TAG = "UserOrdersAdapter";
    private Context context;
    private List<UserOrderGroup> userGroups;
    private OrderAdapter.OrderStatusCallback statusCallback;

    // Track which user groups are expanded
    private Set<String> expandedGroups = new HashSet<>();

    public static class UserOrderGroup {
        private String userId;
        private String userEmail;
        private List<Order> orders;

        public UserOrderGroup(String userId, String userEmail) {
            this.userId = userId;
            this.userEmail = userEmail != null ? userEmail : "Unknown User";
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
        Log.d(TAG, "Created adapter with " + userGroups.size() + " user groups");
    }

    private List<UserOrderGroup> groupOrdersByUser(List<Order> allOrders) {
        Map<String, UserOrderGroup> groupMap = new HashMap<>();

        for (Order order : allOrders) {
            String userId = order.getUserId();
            String userEmail = order.getUserEmail();

            if (userId == null) continue;

            UserOrderGroup group = groupMap.get(userId);
            if (group == null) {
                group = new UserOrderGroup(userId, userEmail);
                groupMap.put(userId, group);
                Log.d(TAG, "Created new group for user: " + userEmail);
            }

            group.addOrder(order);
            Log.d(TAG, "Added order to user " + userEmail + ", total orders: " + group.getOrderCount());
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
        String userId = group.getUserId();

        // Set up the user info
        holder.userEmailTextView.setText(group.getUserEmail());
        holder.orderCountTextView.setText(group.getOrderCount() + " orders");

        // Check if this group should be expanded
        boolean isExpanded = expandedGroups.contains(userId);

        // Set initial expanded state
        holder.ordersRecyclerView.setVisibility(isExpanded ? View.VISIBLE : View.GONE);
        holder.expandIndicator.setText(isExpanded ? "▼" : "▶");

        // Set up the nested RecyclerView for this user's orders
        holder.ordersRecyclerView.setLayoutManager(new LinearLayoutManager(context));
        AdminOrderAdapter orderAdapter = new AdminOrderAdapter(group.getOrders(), statusCallback);
        holder.ordersRecyclerView.setAdapter(orderAdapter);

        // Set up the click listener for the entire header section
        holder.itemView.findViewById(R.id.userHeaderLayout).setOnClickListener(v -> {
            // Toggle expansion state
            boolean nowExpanded = holder.ordersRecyclerView.getVisibility() != View.VISIBLE;

            // Update UI
            holder.ordersRecyclerView.setVisibility(nowExpanded ? View.VISIBLE : View.GONE);
            holder.expandIndicator.setText(nowExpanded ? "▼" : "▶");

            // Store the new state
            if (nowExpanded) {
                expandedGroups.add(userId);
                Log.d(TAG, "Expanded group: " + group.getUserEmail());
            } else {
                expandedGroups.remove(userId);
                Log.d(TAG, "Collapsed group: " + group.getUserEmail());
            }
        });
    }

    @Override
    public int getItemCount() {
        return userGroups.size();
    }

    public void updateData(List<Order> newOrders) {
        // Before updating, store which users were expanded
        Set<String> previouslyExpandedUsers = new HashSet<>(expandedGroups);

        // Update the data
        this.userGroups = groupOrdersByUser(newOrders);

        // Keep just the users that still exist
        expandedGroups.clear();
        for (UserOrderGroup group : userGroups) {
            if (previouslyExpandedUsers.contains(group.getUserId())) {
                expandedGroups.add(group.getUserId());
            }
        }

        Log.d(TAG, "Updated adapter with " + userGroups.size() + " user groups, kept " +
                expandedGroups.size() + " groups expanded");

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