package com.example.suivicommandes;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

public class NotificationHelper {

    private static final String CHANNEL_ID = "order_status_channel";
    private static final int NOTIFICATION_ID = 1;

    //sends notification to user
    public static void sendOrderStatusNotification(Context context, String userEmail, String orderStatus) {
        // Create the Intent to open when the notification is clicked
        Intent intent = new Intent(context, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);

        PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, intent, PendingIntent.FLAG_IMMUTABLE);

        createNotificationChannel(context);

        // Build the notification
        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_notification)
                .setContentTitle("Order Status Update")
                .setContentText("The order status has been updated to: " + orderStatus)
                .setAutoCancel(true)
                .setContentIntent(pendingIntent);

        // Get the NotificationManager system service
        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(context);

        // Show the notification
        notificationManager.notify(NOTIFICATION_ID, notificationBuilder.build());
    }

    // Create a notification channel for Android O and higher
    private static void createNotificationChannel(Context context) {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            CharSequence name = "Order Status Channel";
            String description = "Channel for order status updates";
            int importance = NotificationManager.IMPORTANCE_DEFAULT;

            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, name, importance);
            channel.setDescription(description);

            // Register the channel with the system
            NotificationManager notificationManager = context.getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }
    }
}
