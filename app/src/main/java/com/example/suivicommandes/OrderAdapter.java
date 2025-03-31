package com.example.suivicommandes;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class OrderAdapter extends RecyclerView.Adapter<OrderAdapter.OrderViewHolder> {
    private List<Order> orders;

    public OrderAdapter(List<Order> orders) {
        this.orders = orders;
    }

    @Override
    public OrderViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_order, parent, false);
        return new OrderViewHolder(view);
    }

    @Override
    public void onBindViewHolder(OrderViewHolder holder, int position) {
        Order order = orders.get(position);

        // Bind data to the views
        holder.itemNameTextView.setText(order.getItemName());
        holder.itemStatusTextView.setText("Status: " + order.getOrderStatus());
        holder.itemPriceTextView.setText("$" + order.getItemPrice());
    }

    @Override
    public int getItemCount() {
        return orders.size();
    }

    public static class OrderViewHolder extends RecyclerView.ViewHolder {
        TextView itemNameTextView;
        TextView itemStatusTextView;
        TextView itemPriceTextView;

        public OrderViewHolder(View itemView) {
            super(itemView);
            itemNameTextView = itemView.findViewById(R.id.itemNameTextView);
            itemStatusTextView = itemView.findViewById(R.id.itemStatusTextView);
            itemPriceTextView = itemView.findViewById(R.id.itemPriceTextView);
        }
    }
}
