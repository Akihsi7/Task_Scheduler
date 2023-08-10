//package com.example.schedulertodo;
//
//import android.app.Service;
//
//import androidx.annotation.NonNull;
//import androidx.core.app.NotificationCompat;
//import androidx.core.app.NotificationManagerCompat;
//
//import android.Manifest;
//import android.content.pm.PackageManager;
//import android.os.Build;
//
//import com.example.schedulertodo.utils.ToDoData;
//import com.google.firebase.messaging.FirebaseMessagingService;
//import com.google.firebase.messaging.RemoteMessage;
//
//import android.Manifest;
//import android.app.NotificationChannel;
//import android.app.NotificationManager;
//import android.util.Log;
//
//
//public class MyFirebaseMessagingService extends FirebaseMessagingService {
//
//    private static final String TAG = "MyFirebaseMsgService";
//
//    @Override
//    public void onCreate() {
//        super.onCreate();
//        createNotificationChannel();
//    }
//
//    @Override
//    public void onMessageReceived(@NonNull RemoteMessage remoteMessage) {
//        super.onMessageReceived(remoteMessage);
//
//        if (remoteMessage.getNotification() != null) {
//            String title = remoteMessage.getNotification().getTitle();
//            String message = remoteMessage.getNotification().getBody();
//            sendNotification(title, message);
//        }
//    }
//
//
//    private void sendNotification(String title, String message) {
//        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, "myFirebaseChannel")
//                .setSmallIcon(R.drawable.baseline_notifications_active_24)
//                .setContentTitle(title)
//                .setContentText(message)
//                .setAutoCancel(true);
//
//        try {
//            NotificationManagerCompat manager = NotificationManagerCompat.from(this);
//            manager.notify(101, builder.build());
//        } catch (SecurityException e) {
//            Log.e(TAG, "Failed to show notification: " + e.getMessage());
//        }
//    }
//
//    private void createNotificationChannel() {
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
//            String channelId = "myFirebaseChannel";
//            String channelName = "Firebase Channel";
//            NotificationChannel channel = new NotificationChannel(channelId, channelName, NotificationManager.IMPORTANCE_DEFAULT);
//            NotificationManager notificationManager = getSystemService(NotificationManager.class);
//            notificationManager.createNotificationChannel(channel);
//        }
//    }
//
//
//}
//
//
//
//
//
//
//
