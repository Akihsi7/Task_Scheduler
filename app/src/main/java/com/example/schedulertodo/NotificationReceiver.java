package com.example.schedulertodo;

import static android.content.ContentValues.TAG;

import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.util.Log;
import android.widget.Toast;

import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.core.content.ContextCompat;

import com.example.schedulertodo.utils.ToDoData;

public class NotificationReceiver extends BroadcastReceiver {


    @Override
    public void onReceive(Context context, Intent intent) {
        Log.d(TAG, "onReceive called");
        // Retrieve the task data from the intent's extras
        ToDoData taskData = (ToDoData) intent.getSerializableExtra("taskData");

        // Display the notification using NotificationCompat.Builder
        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, "myFirebaseChannel")
                .setSmallIcon(R.drawable.baseline_notifications_active_24)
                .setContentTitle(taskData.getTask())
                .setContentText("5 minutes left! Procastination makes easy things hard, hard things harder!")
                .setAutoCancel(true);

        try {
            NotificationManagerCompat manager = NotificationManagerCompat.from(context);
            int notificationId = taskData.getTaskid().hashCode();
            manager.notify(notificationId, builder.build());
        } catch (SecurityException e) {
            Log.e(TAG, "Failed to show notification: " + e.getMessage());
        }
    }
}


