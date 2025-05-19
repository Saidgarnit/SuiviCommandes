package com.example.suivicommandes;

import android.Manifest;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.StaggeredGridLayoutManager;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class HomeActivity extends AppCompatActivity {

    private static final String TAG = "HomeActivity";
    private static final String NOTIFICATION_CHANNEL_ID = "order_status_channel";
    private static final int NOTIFICATION_PERMISSION_CODE = 123;

    // Firebase instances
    private FirebaseAuth auth;
    private FirebaseFirestore db;

    // UI Components
    private TextView userEmailTextView;
    private RecyclerView recyclerView;
    private ItemAdapter itemAdapter;
    private final List<Item> itemList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        initializeFirebase();
        if (!checkAuthentication()) return;
        initializeViews();
        setupUI();
        setupRecyclerView();
        requestNotificationPermission();
        createNotificationChannel();
        setupOrderStatusListener();
    }

    private void initializeFirebase() {
        auth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
    }

    private boolean checkAuthentication() {
        FirebaseUser currentUser = auth.getCurrentUser();
        if (currentUser == null) {
            redirectToLogin();
            return false;
        }
        return true;
    }

    private void initializeViews() {
        userEmailTextView = findViewById(R.id.userEmailTextView);
        recyclerView = findViewById(R.id.recyclerView);

        // Set up navigation buttons
        setupNavigationButtons();
    }

    private void setupNavigationButtons() {
        ImageButton cartButton = findViewById(R.id.cartButton);
        ImageButton notificationButton = findViewById(R.id.notificationButton);
        ImageButton ordersButton = findViewById(R.id.ordersButton);
        ImageButton logoutButton = findViewById(R.id.logoutButton);

        cartButton.setOnClickListener(v -> startActivity(new Intent(this, CartActivity.class)));
        notificationButton.setOnClickListener(v -> {
            markAllNotificationsAsRead();
            startActivity(new Intent(this, NotificationActivity.class));
        });
        ordersButton.setOnClickListener(v -> startActivity(new Intent(this, OrdersActivity.class)));
        logoutButton.setOnClickListener(v -> signOut());
    }

    private void setupUI() {
        FirebaseUser currentUser = auth.getCurrentUser();
        if (currentUser != null && currentUser.getEmail() != null) {
            userEmailTextView.setText(getString(R.string.signed_in_as, currentUser.getEmail()));
        }
    }

    private void setupRecyclerView() {
        recyclerView.setLayoutManager(new StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL));
        itemAdapter = new ItemAdapter(this, itemList);
        recyclerView.setAdapter(itemAdapter);
        fetchItems();
    }

    private void fetchItems() {
        db.collection("items").get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    itemList.clear();
                    for (DocumentSnapshot document : queryDocumentSnapshots) {
                        Item item = document.toObject(Item.class);
                        if (item != null) {
                            itemList.add(item);
                        }
                    }
                    itemAdapter.notifyDataSetChanged();
                })
                .addOnFailureListener(e -> showError("Error loading items: " + e.getMessage()));
    }
    private void setupOrderStatusListener() {
        FirebaseUser currentUser = auth.getCurrentUser();
        if (currentUser != null) {
            listenForOrderStatusChanges(currentUser.getUid());
        }
    }
//permission needed for android 13+
    private void requestNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS)
                    != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.POST_NOTIFICATIONS},
                        NOTIFICATION_PERMISSION_CODE);
            }
        }
    }
//channels needded for android 8+
    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    NOTIFICATION_CHANNEL_ID,
                    getString(R.string.notification_channel_name),
                    NotificationManager.IMPORTANCE_HIGH
            );
            channel.setDescription(getString(R.string.notification_channel_description));
            channel.enableVibration(true);

            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            if (notificationManager != null) {
                notificationManager.createNotificationChannel(channel);
            }
        }
    }
//checks for order status changes in firestore.
    private void listenForOrderStatusChanges(String userId) {
        Log.d(TAG, "Starting order status listener for user: " + userId);

        db.collection("orders")
                .whereEqualTo("userId", userId)
                .addSnapshotListener((snapshots, error) -> {
                    if (error != null) {
                        Log.e(TAG, "Error listening for order updates", error);
                        return;
                    }

                    if (snapshots != null) {
                        for (DocumentChange dc : snapshots.getDocumentChanges()) {
                            if (dc.getType() == DocumentChange.Type.MODIFIED) {
                                processOrderChange(dc.getDocument());
                            }
                        }
                    }
                });
    }

//fetches orderid  ad orderstatus
    private void processOrderChange(DocumentSnapshot document) {
        String orderStatus = document.getString("orderStatus");
        String orderId = document.getId();

        if (orderStatus != null) {
            Log.d(TAG, "Order " + orderId + " status changed to: " + orderStatus);
            sendOrderStatusNotification(orderId, orderStatus);
        }
    }

//creates intent for notification
//assigns notification to channel
    private void sendOrderStatusNotification(String orderId, String status) {
        String notificationMessage = "Order " + orderId + ": " + getString(R.string.order_status_changed, status);

        // Create intent for notification click
        Intent intent = new Intent(this, NotificationActivity.class)
                .putExtra("highlighted_notification", notificationMessage)
                .addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);

        PendingIntent pendingIntent = PendingIntent.getActivity(
                this,
                0,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, NOTIFICATION_CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_notification)
                .setContentTitle(getString(R.string.order_update_title))
                .setContentText(notificationMessage)
                .setAutoCancel(true)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setContentIntent(pendingIntent);

        NotificationManager notificationManager =
                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        if (notificationManager != null) {
            notificationManager.notify(orderId.hashCode(), builder.build());
        }

        // Save notification with timestamp
        saveNotification(notificationMessage);

        // Update notification count immediately after adding
        updateUnreadNotificationCount();
    }
    private void saveNotification(String message) {
        long timestamp = System.currentTimeMillis();
        String notificationWithTimestamp = timestamp + "::" + message + "::unread";

        SharedPreferences sharedPreferences = getSharedPreferences("notifications_prefs", MODE_PRIVATE);
        Set<String> notifications = sharedPreferences.getStringSet("notifications_key", new HashSet<>());
        Set<String> updatedNotifications = new HashSet<>(notifications);
        updatedNotifications.add(notificationWithTimestamp);
        sharedPreferences.edit().putStringSet("notifications_key", updatedNotifications).apply();

        // Update notification count and UI
        updateUnreadNotificationCount();
    }
//ui update for num of notif
    private void updateUnreadNotificationCount() {
        SharedPreferences sharedPreferences = getSharedPreferences("notifications_prefs", MODE_PRIVATE);
        Set<String> notificationsSet = sharedPreferences.getStringSet("notifications_key", new HashSet<>());
        int unreadCount = 0;
        for (String notification : notificationsSet) {
            if (notification.endsWith("::unread")) {
                unreadCount++;
            }
        }
        updateNotificationIcon(unreadCount);
        mentionUnreadNotifications(unreadCount);
    }
//change notif icon if red or unread
    private void updateNotificationIcon(int count) {
        ImageButton notificationButton = findViewById(R.id.notificationButton);
        if (count > 0) {
            notificationButton.setImageResource(R.drawable.ic_notification_with_badge);
        } else {
            notificationButton.setImageResource(R.drawable.ic_notification);
        }
    }
    private void mentionUnreadNotifications(int count) {
        if (count > 0) {
            userEmailTextView.setText(getString(R.string.signed_in_as_unread_notifications, auth.getCurrentUser().getEmail(), count));
        } else {
            userEmailTextView.setText(getString(R.string.signed_in_as, auth.getCurrentUser().getEmail()));
        }
    }

    private void markAllNotificationsAsRead() {
        SharedPreferences sharedPreferences = getSharedPreferences("notifications_prefs", MODE_PRIVATE);
        Set<String> notificationsSet = sharedPreferences.getStringSet("notifications_key", new HashSet<>());
        Set<String> updatedNotificationsSet = new HashSet<>();
        for (String notification : notificationsSet) {
            if (notification.endsWith("::unread")) {
                updatedNotificationsSet.add(notification.replace("::unread", "::read"));
            } else {
                updatedNotificationsSet.add(notification);
            }
        }
        sharedPreferences.edit().putStringSet("notifications_key", updatedNotificationsSet).apply();
        updateUnreadNotificationCount();
    }

    private void signOut() {
        auth.signOut();
        showSuccess(getString(R.string.signed_out_successfully));
        redirectToLogin();
    }

    private void redirectToLogin() {
        Intent intent = new Intent(this, MainActivity.class)
                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    private void showError(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }

    private void showSuccess(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == NOTIFICATION_PERMISSION_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Log.d(TAG, "Notification permission granted");
            } else {
                Log.w(TAG, "Notification permission denied");
            }
        }
    }
}