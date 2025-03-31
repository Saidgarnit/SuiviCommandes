package com.example.suivicommandes;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class UserOrders {
    private String userId;
    private String userEmail;
    private List<Order> orders;

    public UserOrders(String userId, String userEmail) {
        this.userId = userId;
        this.userEmail = userEmail;
        this.orders = new ArrayList<>();
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

    public void addOrder(Order order) {
        if (orders == null) {
            orders = new ArrayList<>();
        }
        orders.add(order);
    }

    /**
     * Sorts orders by date, newest first
     */
    public void sortOrdersByDate() {
        if (orders != null && orders.size() > 1) {
            Collections.sort(orders, (o1, o2) -> {
                // Null safety check
                if (o1.getOrderDate() == null && o2.getOrderDate() == null) return 0;
                if (o1.getOrderDate() == null) return 1; // Null dates at the end
                if (o2.getOrderDate() == null) return -1;

                // Descending order (newest first)
                return o2.getOrderDate().compareTo(o1.getOrderDate());
            });
        }
    }
}