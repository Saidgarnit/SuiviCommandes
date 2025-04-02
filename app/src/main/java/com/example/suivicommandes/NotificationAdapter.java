package com.example.suivicommandes;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class NotificationAdapter extends RecyclerView.Adapter<NotificationAdapter.NotificationViewHolder> {

    private List<String> notifications;
    private String highlightedNotification;
    private OnNotificationClickListener listener;
    private SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault());

    public interface OnNotificationClickListener {
        void onNotificationClick(String notification);
    }

    public NotificationAdapter(List<String> notifications, OnNotificationClickListener listener) {
        this.notifications = notifications;
        this.listener = listener;
    }

    @NonNull
    @Override
    public NotificationViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_notification, parent, false);
        return new NotificationViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull NotificationViewHolder holder, int position) {
        String notification = notifications.get(position);

        // Extract timestamp and message
        String displayText = notification;
        long timestamp = 0;

        String[] parts = notification.split("::");
        if (parts.length >= 2) {
            try {
                timestamp = Long.parseLong(parts[0]);
                displayText = parts[1];
            } catch (NumberFormatException e) {
                // If parsing fails, just use the original text
                displayText = notification;
            }
        }

        // Remove ::read or ::unread suffixes for display if present
        if (displayText.endsWith("::read") || displayText.endsWith("::unread")) {
            displayText = displayText.substring(0, displayText.lastIndexOf("::"));
        }

        holder.notificationTextView.setText(displayText);

        // Set the timestamp if available
        if (timestamp > 0) {
            holder.notificationTimeTextView.setText(dateFormat.format(new Date(timestamp)));
            holder.notificationTimeTextView.setVisibility(View.VISIBLE);
        } else {
            holder.notificationTimeTextView.setVisibility(View.GONE);
        }

        // Highlight the notification if it matches the one from the intent
        if (notification.equals(highlightedNotification) ||
                (highlightedNotification != null && notification.contains(highlightedNotification))) {
            holder.itemView.setBackgroundResource(R.drawable.notification_highlighted_background);
        } else {
            holder.itemView.setBackgroundResource(R.drawable.notification_background);
        }

        // Set status icon based on the notification message
        if (notification.contains("pending")) {
            holder.statusIcon.setImageResource(R.drawable.ic_pending);
        } else if (notification.contains("preparing")) {
            holder.statusIcon.setImageResource(R.drawable.ic_preparing);
        } else if (notification.contains("shipped")) {
            holder.statusIcon.setImageResource(R.drawable.ic_shipped);
        } else if (notification.contains("delivering")) {
            holder.statusIcon.setImageResource(R.drawable.ic_delivering);
        } else if (notification.contains("delivered")) {
            holder.statusIcon.setImageResource(R.drawable.ic_delivered);
        }

        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onNotificationClick(notification);
            }
        });
    }

    @Override
    public int getItemCount() {
        return notifications.size();
    }

    public void setHighlightedNotification(String notification) {
        this.highlightedNotification = notification;
        notifyDataSetChanged();
    }

    static class NotificationViewHolder extends RecyclerView.ViewHolder {
        TextView notificationTextView;
        TextView notificationTimeTextView;
        ImageView statusIcon;

        public NotificationViewHolder(@NonNull View itemView) {
            super(itemView);
            notificationTextView = itemView.findViewById(R.id.notificationTextView);
            notificationTimeTextView = itemView.findViewById(R.id.notificationTimeTextView);
            statusIcon = itemView.findViewById(R.id.statusIcon);
        }
    }
}