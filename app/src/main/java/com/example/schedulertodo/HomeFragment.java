package com.example.schedulertodo;

import static android.content.ContentValues.TAG;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.content.BroadcastReceiver;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.schedulertodo.databinding.FragmentHomeBinding;
import com.example.schedulertodo.utils.ToDoData;
import com.example.schedulertodo.utils.TodoAdapter;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.firestore.WriteBatch;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.RemoteMessage;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.EventListener;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.TimeZone;

public class HomeFragment extends Fragment implements AddTodoPopupFragment.DialogNextButtonClickListener, TodoAdapter.ToDoAdapterClicksInterface {

    private FirebaseAuth auth;
    private FirebaseFirestore db;
    private NavController navController;
    private FragmentHomeBinding binding;
    private AddTodoPopupFragment popupFragment;
    private TodoAdapter adapter;
    private ArrayList<ToDoData> mList;
    private Timestamp timestamp;
    private static final String TAG = "HomeFragment";

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentHomeBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        init(view);
        getDataFromFirebase();
        registerEvents();
        storeFCMTokenInFirestore();
    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence channelName = "My Channel";
            String channelDescription = "My Channel Description";
            int importance = NotificationManager.IMPORTANCE_DEFAULT;

            NotificationChannel channel = new NotificationChannel("myFirebaseChannel", channelName, importance);
            channel.setDescription(channelDescription);

            NotificationManager notificationManager = requireContext().getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }
    }


    private void init(View view) {
        navController = Navigation.findNavController(view);
        auth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
        binding.recyclerView.setHasFixedSize(true);
        binding.recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        mList = new ArrayList<>();
        adapter = new TodoAdapter(mList);
        adapter.setListener(this);
        binding.recyclerView.setAdapter(adapter);
    }

    private void registerEvents() {
        binding.addtaskbtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (popupFragment != null)
                    getChildFragmentManager().beginTransaction().remove(popupFragment).commit();
                popupFragment = new AddTodoPopupFragment();
                popupFragment.setListener(HomeFragment.this);
                popupFragment.show(getChildFragmentManager(), AddTodoPopupFragment.TAG);
            }
        });
    }
    private void getDataFromFirebase() {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        String uid = currentUser != null ? currentUser.getUid() : "";
        CollectionReference collectionRef = db.collection("users").document(uid).collection("tasks");
        Date currentDate = new Date();
        DateFormat dateFormat = new SimpleDateFormat("MMMM dd, yyyy", Locale.getDefault());
        String formattedDate = dateFormat.format(currentDate);
        Query query = collectionRef.orderBy("timestamp");

        ListenerRegistration listenerRegistration = query.addSnapshotListener(new com.google.firebase.firestore.EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(@Nullable QuerySnapshot snapshot, @Nullable FirebaseFirestoreException exception) {
                if (exception != null) {
                    Log.w(TAG, "Listen failed", exception);
                    return;
                }

                if (snapshot != null) {
                    List<ToDoData> taskList = new ArrayList<>();
                    for (QueryDocumentSnapshot doc : snapshot) {
                        String task = doc.getString("name");
                        String date = doc.getString("date");
                        String time = doc.getString("time");
                        Boolean completed = doc.getBoolean("completed");
                        if (completed == null) {
                            completed = false;
                        }
                        Timestamp timestamp = doc.getTimestamp("timestamp");

                        String taskId = doc.getId();

                        if (task != null && date != null && time != null && date.equals(formattedDate)) {
                            taskList.add(new ToDoData(taskId, task, date, time, completed));
                        }
                    }

                    // Sort the task list so completed tasks are at the bottom
                    //taskList.sort((o1, o2) -> Boolean.compare(o2.isCompleted(), o1.isCompleted()));

                    mList.clear();
                    mList.addAll(taskList);
                    adapter.notifyDataSetChanged();
                } else {
                    Log.d(TAG, "Current data: null");
                }
            }
        });
    }

    private void storeFCMTokenInFirestore() {
        FirebaseMessaging.getInstance().getToken()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful() && task.getResult() != null) {
                        String token = task.getResult();

                        // Store the token in Firestore
                        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
                        String uid = currentUser != null ? currentUser.getUid() : "";
                        DocumentReference userRef = db.collection("users").document(uid);

                        userRef.update("fcmToken", token)
                                .addOnSuccessListener(aVoid -> Log.d(TAG, "FCM token stored in Firestore"))
                                .addOnFailureListener(e -> Log.e(TAG, "Failed to store FCM token in Firestore: " + e.getMessage()));
                    } else {
                        Log.w(TAG, "Failed to retrieve FCM token: " + task.getException());
                    }
                });
    }



    @Override
    public void onSaveTask(String todo, TextInputEditText popuptodotaskname, String popupdate, String popuptime) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        String uid = currentUser != null ? currentUser.getUid() : "";
        CollectionReference collectionRef = db.collection("users").document(uid).collection("tasks");
        DocumentReference newTaskRef = collectionRef.document();
        DateFormat dateFormat = new SimpleDateFormat("MMMM d, yyyy", Locale.ENGLISH);

        // Create the taskData object
        String taskId = newTaskRef.getId(); // Generate the task ID
        ToDoData taskData = new ToDoData(taskId, popuptodotaskname.getText().toString(), popupdate, popuptime, false);

        // Retrieve the FCM token for the user
        FirebaseMessaging.getInstance().getToken()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful() && task.getResult() != null) {
                        String token = task.getResult();

                        // Use the token for sending notifications to this device
                        sendFcmNotification(token, taskData);
                    } else {
                        Log.w(TAG, "Failed to retrieve token: " + task.getException());
                    }
                });

        try {
            Date date = dateFormat.parse(popupdate);
            DateFormat timeFormat = new SimpleDateFormat("h:mm:ss a", Locale.ENGLISH);
            timeFormat.setTimeZone(TimeZone.getDefault());
            Date time = timeFormat.parse(popuptime);
            Calendar dateTime = Calendar.getInstance();
            dateTime.setTimeZone(TimeZone.getDefault());
            dateTime.setTime(time);
            dateTime.set(Calendar.YEAR, date.getYear() + 1900);
            dateTime.set(Calendar.MONTH, date.getMonth());
            dateTime.set(Calendar.DAY_OF_MONTH, date.getDate());
            Timestamp timestamp = new Timestamp(dateTime.getTime());
            Map<String, Object> taskMap = new HashMap<>();
            taskMap.put("name", popuptodotaskname.getText().toString());
            taskMap.put("date", popupdate);
            taskMap.put("time", popuptime);
            taskMap.put("timestamp", timestamp);
            newTaskRef.set(taskMap).addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
//                    String taskId = newTaskRef.getId(); // Get the newly generated task ID
                    scheduleNotification(new ToDoData(taskId, taskMap.get("name").toString(), popupdate, popuptime, false));
                    // Do something else, like displaying a success message
                    Toast.makeText(getContext(), "Added", Toast.LENGTH_SHORT).show();
                } else {
                    // Write operation failed, so display an error message
                    Toast.makeText(getContext(), "Failed to add", Toast.LENGTH_SHORT).show();
                }
                popuptodotaskname.setText(null);
                popupFragment.dismiss();
            });
        } catch (ParseException e) {
            e.printStackTrace();
        }
    }

    private void sendFcmNotification(String token, ToDoData taskData) {
        // Create the FCM message payload
        Map<String, String> data = new HashMap<>();
        data.put("title", taskData.getTask());
        data.put("body", "5 minutes until the task starts");
        // Add any additional data you want to send with the notification

        // Send the FCM message using the FirebaseMessaging API
        FirebaseMessaging.getInstance().send(new RemoteMessage.Builder(token)
                .setData(data)
                .build());
    }

    @Override
    public void onUpdateTask(ToDoData toDoData, TextInputEditText popuptodotaskname, String popupdate, String popuptime) {
        String taskId = toDoData.getTaskid();
        String name = popuptodotaskname.getText().toString();
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        String uid = currentUser != null ? currentUser.getUid() : "";
        DocumentReference taskRef = db.collection("users").document(uid).collection("tasks").document(taskId);
        WriteBatch batch = db.batch();
        batch.update(taskRef, "name", name);
        batch.commit().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                Toast.makeText(getContext(), "Updated", Toast.LENGTH_SHORT).show();
                adapter.notifyDataSetChanged();
            } else {
                Toast.makeText(getContext(), "Failed to update", Toast.LENGTH_SHORT).show();
            }
            popuptodotaskname.setText(null);
            popupFragment.dismiss();
        });
    }

    @Override
    public void onDeleteTaskBtnClicked(ToDoData toDoData) {
        String taskId = toDoData.getTaskid();
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        String uid = currentUser != null ? currentUser.getUid() : "";
        DocumentReference documentRef = db.collection("users").document(uid).collection("tasks").document(taskId);
        documentRef.delete().addOnSuccessListener(aVoid -> {
            Toast.makeText(getContext(), "Deleted", Toast.LENGTH_SHORT).show();
        }).addOnFailureListener(e -> {
            Toast.makeText(getContext(), "Failed to delete: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        });
    }


    public void onEditTaskBtnClicked(ToDoData toDoData) {
        if (popupFragment != null) {
            getChildFragmentManager().beginTransaction().remove(popupFragment).commit();
        }
        popupFragment = AddTodoPopupFragment.newInstance(toDoData.getTaskid(), toDoData.getTask(), toDoData.getDate(), toDoData.getTime(), toDoData.isCompleted());
        popupFragment.setListener(this);
        popupFragment.show(getChildFragmentManager(), AddTodoPopupFragment.TAG);
    }



    private void scheduleNotification(ToDoData taskData) {
        // Get the task time and calculate the notification time
        String taskTime = taskData.getTime(); // Assuming time is in HH:mm format
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(Objects.requireNonNull(parseTime(taskTime))); // Parse the task time
        calendar.add(Calendar.MINUTE, -5); // Subtract 5 minutes to get the notification time

        // Create an Intent to start the notification BroadcastReceiver
        Intent notificationIntent = new Intent(requireContext(), NotificationReceiver.class);
        notificationIntent.putExtra("taskData", taskData); // Pass the task data as an extra

        // Create a PendingIntent for the notification
        PendingIntent pendingIntent = PendingIntent.getBroadcast(
                requireContext(),
                taskData.getTaskid().hashCode(), // Use a unique identifier for each notification
                notificationIntent,
                PendingIntent.FLAG_IMMUTABLE | PendingIntent.FLAG_UPDATE_CURRENT
        );

        // Get the AlarmManager and schedule the PendingIntent
        AlarmManager alarmManager = (AlarmManager) requireContext().getSystemService(Context.ALARM_SERVICE);
        alarmManager.setExact(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), pendingIntent);
    }

//     Helper method to parse the time string and return a Calendar object

    private Date parseTime(String time) {
        SimpleDateFormat format = new SimpleDateFormat("h:mm:ss a", Locale.getDefault());
        try {
            return format.parse(time);
        } catch (ParseException e) {
            e.printStackTrace();
            return null;
        }
    }



}
