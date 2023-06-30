package com.example.schedulertodo;

import static android.content.ContentValues.TAG;

import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

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



    @Override
    public void onSaveTask(String todo, TextInputEditText popuptodotaskname, String popupdate, String popuptime) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        String uid = currentUser != null ? currentUser.getUid() : "";
        CollectionReference collectionRef = db.collection("users").document(uid).collection("tasks");
        DocumentReference newTaskRef = collectionRef.document();
        DateFormat dateFormat = new SimpleDateFormat("MMMM d, yyyy", Locale.ENGLISH);
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
}



/**
 * A simple {@link Fragment} subclass.
 * Use the {@link HomeFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
//public class HomeFragment extends Fragment {
//
//    // TODO: Rename parameter arguments, choose names that match
//    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
//    private static final String ARG_PARAM1 = "param1";
//    private static final String ARG_PARAM2 = "param2";
//
//    // TODO: Rename and change types of parameters
//    private String mParam1;
//    private String mParam2;
//
//    public HomeFragment() {
//        // Required empty public constructor
//    }
//
//    /**
//     * Use this factory method to create a new instance of
//     * this fragment using the provided parameters.
//     *
//     * @param param1 Parameter 1.
//     * @param param2 Parameter 2.
//     * @return A new instance of fragment HomeFragment.
//     */
//    // TODO: Rename and change types and number of parameters
//    public static HomeFragment newInstance(String param1, String param2) {
//        HomeFragment fragment = new HomeFragment();
//        Bundle args = new Bundle();
//        args.putString(ARG_PARAM1, param1);
//        args.putString(ARG_PARAM2, param2);
//        fragment.setArguments(args);
//        return fragment;
//    }
//
//    @Override
//    public void onCreate(Bundle savedInstanceState) {
//        super.onCreate(savedInstanceState);
//        if (getArguments() != null) {
//            mParam1 = getArguments().getString(ARG_PARAM1);
//            mParam2 = getArguments().getString(ARG_PARAM2);
//        }
//    }
//
//    @Override
//    public View onCreateView(LayoutInflater inflater, ViewGroup container,
//                             Bundle savedInstanceState) {
//        // Inflate the layout for this fragment
//        return inflater.inflate(R.layout.fragment_home, container, false);
//    }
//}