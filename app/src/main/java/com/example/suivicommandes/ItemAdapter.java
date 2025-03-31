package com.example.suivicommandes;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import java.util.List;

public class ItemAdapter extends RecyclerView.Adapter<ItemAdapter.ItemViewHolder> {

    private List<Item> itemList;
    private Context context;

    public ItemAdapter(Context context, List<Item> itemList) {
        this.context = context;
        this.itemList = itemList;
    }

    @NonNull
    @Override
    public ItemViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_layout, parent, false);
        return new ItemViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ItemViewHolder holder, int position) {
        Item item = itemList.get(position);
        holder.nameTextView.setText(item.getName());
        holder.priceTextView.setText("Price: $" + item.getPrice());
        holder.descriptionTextView.setText(item.getDescription());

        // Load image using Glide
        Glide.with(context).load(item.getImage()).into(holder.itemImageView);

        // Set item click listener
        holder.itemView.setOnClickListener(v -> {
            // Open ItemDetailsActivity with the selected item
            Intent intent = new Intent(context, ItemDetailsActivity.class);
            intent.putExtra("item", item); // Passing the item details
            context.startActivity(intent);
        });
    }
    @Override
    public int getItemCount() {
        return itemList.size();
    }

    static class ItemViewHolder extends RecyclerView.ViewHolder {
        TextView nameTextView, priceTextView, descriptionTextView;
        ImageView itemImageView;

        public ItemViewHolder(@NonNull View itemView) {
            super(itemView);
            nameTextView = itemView.findViewById(R.id.itemName);
            priceTextView = itemView.findViewById(R.id.itemPrice);
            descriptionTextView = itemView.findViewById(R.id.itemDescription);
            itemImageView = itemView.findViewById(R.id.itemImage);
        }
    }
}

