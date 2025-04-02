package com.example.suivicommandes;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class NotificationActivity extends AppCompatActivity implements NotificationAdapter.OnNotificationClickListener {

    private static final String SHARED_PREFS_NAME = "notifications_prefs";
    private static final String NOTIFICATIONS_KEY = "notifications_key";

    private RecyclerView notificationsRecyclerView;
    private NotificationAdapter notificationAdapter;
    private List<String> notificationsList = new ArrayList<>();
    private TextView emptyNotificationsText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notification);

        notificationsRecyclerView = findViewById(R.id.notificationsRecyclerView);
        emptyNotificationsText = findViewById(R.id.emptyNotificationsText);
        notificationsRecyclerView.setLayoutManager(new LinearLayoutManager(this));

        notificationAdapter = new NotificationAdapter(notificationsList, this);
        notificationsRecyclerView.setAdapter(notificationAdapter);

        loadNotifications();

        // Check if there's a highlighted notification
        if (getIntent().hasExtra("highlighted_notification")) {
            String highlightedNotification = getIntent().getStringExtra("highlighted_notification");
            notificationAdapter.setHighlightedNotification(highlightedNotification);

            // Find and scroll to the highlighted notification
            for (int i = 0; i < notificationsList.size(); i++) {
                if (notificationsList.get(i).contains(highlightedNotification)) {
                    notificationsRecyclerView.scrollToPosition(i);
                    break;
                }
            }
        }
    }

    private void loadNotifications() {
        SharedPreferences sharedPreferences = getSharedPreferences(SHARED_PREFS_NAME, MODE_PRIVATE);
        Set<String> notificationsSet = sharedPreferences.getStringSet(NOTIFICATIONS_KEY, new HashSet<>());
        notificationsList.clear();

        // Convert from set to list to sort
        List<NotificationItem> tempList = new ArrayList<>();

        for (String notification : notificationsSet) {
            String[] parts = notification.split("::");
            if (parts.length >= 2) {
                try {
                    long timestamp = Long.parseLong(parts[0]);
                    String message = parts[1];
                    boolean isUnread = parts.length > 2 && "unread".equals(parts[2]);
                    tempList.add(new NotificationItem(timestamp, message, isUnread));
                } catch (NumberFormatException e) {
                    // Handle old format notifications without timestamp
                    tempList.add(new NotificationItem(0, notification, false));
                }
            } else {
                // Simple notification without timestamp
                tempList.add(new NotificationItem(0, notification, false));
            }
        }

        // Sort by timestamp (newest first)
        Collections.sort(tempList, (item1, item2) ->
                Long.compare(item2.getTimestamp(), item1.getTimestamp()));

        // Convert back to strings for the adapter
        for (NotificationItem item : tempList) {
            String statusSuffix = item.isUnread() ? "::unread" : "::read";
            notificationsList.add(item.getTimestamp() + "::" + item.getMessage() + statusSuffix);
        }

        notificationAdapter.notifyDataSetChanged();

        // Show/hide empty state
        if (notificationsList.isEmpty() && emptyNotificationsText != null) {
            emptyNotificationsText.setVisibility(View.VISIBLE);
            notificationsRecyclerView.setVisibility(View.GONE);
        } else if (emptyNotificationsText != null) {
            emptyNotificationsText.setVisibility(View.GONE);
            notificationsRecyclerView.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void onNotificationClick(String notification) {
        // Mark notification as read
        SharedPreferences sharedPreferences = getSharedPreferences(SHARED_PREFS_NAME, MODE_PRIVATE);
        Set<String> notificationsSet = sharedPreferences.getStringSet(NOTIFICATIONS_KEY, new HashSet<>());
        Set<String> updatedNotificationsSet = new HashSet<>();

        for (String notif : notificationsSet) {
            if (notif.equals(notification) && notif.endsWith("::unread")) {
                updatedNotificationsSet.add(notif.replace("::unread", "::read"));
            } else {
                updatedNotificationsSet.add(notif);
            }
        }

        sharedPreferences.edit().putStringSet(NOTIFICATIONS_KEY, updatedNotificationsSet).apply();
        loadNotifications();

        // Redirect to OrdersActivity
        Intent intent = new Intent(this, OrdersActivity.class);
        startActivity(intent);
    }

    // Helper class to represent a notification item with timestamp
    public static class NotificationItem {
        private long timestamp;
        private String message;
        private boolean isUnread;

        public NotificationItem(long timestamp, String message, boolean isUnread) {
            this.timestamp = timestamp;
            this.message = message;
            this.isUnread = isUnread;
        }

        public long getTimestamp() {
            return timestamp;
        }

        public String getMessage() {
            return message;
        }

        public boolean isUnread() {
            return isUnread;
        }
    }
}