package com.example.schedulertodo;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.example.schedulertodo.databinding.FragmentSchedularBinding;
import com.example.schedulertodo.utils.ToDoData;
import com.example.schedulertodo.utils.TodoAdapter;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.WriteBatch;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;


public class SchedularFragment extends Fragment implements AddTodoPopupFragment.DialogNextButtonClickListener,
        TodoAdapter.ToDoAdapterClicksInterface {

    private FragmentSchedularBinding binding;
    private FirebaseAuth auth;
    private FirebaseFirestore db;
    private NavController navController;
    private AddTodoPopupFragment popupFragment = null;
    private TodoAdapter adapter;
    private List<ToDoData> mList;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentSchedularBinding.inflate(inflater, container, false);
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
        binding.recyclerview2.setHasFixedSize(true);
        binding.recyclerview2.setLayoutManager(new LinearLayoutManager(getContext()));
        mList = new ArrayList<>();
        adapter = new TodoAdapter(mList);
        adapter.setListener(this);
        binding.recyclerview2.setAdapter(adapter);
    }

    private void registerEvents() {
        binding.addtaskbtn.setOnClickListener(v -> {
            if (popupFragment != null) {
                getChildFragmentManager().beginTransaction().remove(popupFragment).commit();
            }
            popupFragment = new AddTodoPopupFragment();
            popupFragment.setListener(this);
            popupFragment.show(getChildFragmentManager(), AddTodoPopupFragment.TAG);
        });
    }

    private void getDataFromFirebase() {
        CollectionReference collectionRef = db.collection("users").document(auth.getCurrentUser().getUid()).collection("tasks");

        Date currentDate = new Date();
        DateFormat dateFormat = new SimpleDateFormat("MMMM dd, yyyy", Locale.getDefault());
        String formattedDate = dateFormat.format(currentDate);
        Date currentParsedDate;
        try {
            currentParsedDate = dateFormat.parse(formattedDate);
        } catch (ParseException e) {
            currentParsedDate = null;
            Log.e(AddTodoPopupFragment.TAG, "Error parsing date", e);
        }

        Query query = collectionRef.orderBy("timestamp");

        Date finalCurrentParsedDate = currentParsedDate;
        query.addSnapshotListener((snapshot, exception) -> {
            if (exception != null) {
                Log.w(AddTodoPopupFragment.TAG, "Listen failed", exception);
                return;
            }

            if (snapshot != null) {
                List<ToDoData> taskList = new ArrayList<>();
                for (DocumentSnapshot doc : snapshot.getDocuments()) {
                    String task = doc.getString("name");
                    String date = doc.getString("date");
                    String time = doc.getString("time");
                    Boolean completed = doc.getBoolean("completed");
                    String taskId = doc.getId();
                    Date dateparsed;
                    try {
                        dateparsed = dateFormat.parse(date);
                    } catch (ParseException e) {
                        dateparsed = null;
                        Log.e(AddTodoPopupFragment.TAG, "Error parsing date", e);
                    }
                    if (dateparsed != null) {
                        final Date finalDateparsed = dateparsed;
                        if (task != null && date != null && time != null && finalDateparsed.after(finalCurrentParsedDate)) {
                            if (completed != null) {
                                taskList.add(new ToDoData(taskId, task, date, time, completed.booleanValue()));
                            } else {
                                taskList.add(new ToDoData(taskId, task, date, time, false));
                            }
                        }
                    }
                }
                mList.clear();
                mList.addAll(taskList);
                adapter.notifyDataSetChanged();
            } else {
                Log.d(AddTodoPopupFragment.TAG, "Current data: null");
            }
        });
    }


    @Override
    public void onUpdateTask(ToDoData toDoData, TextInputEditText popuptodotaskname, String popupdate, String popuptime) {
        String taskId = toDoData.getTaskid();
        String name = popuptodotaskname.getText().toString();
        DocumentReference taskRef = db.collection("users").document(auth.getCurrentUser().getUid())
                .collection("tasks").document(taskId);

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
            if (popupFragment != null) {
                popupFragment.dismiss();
            }
        });
    }


    @Override
    public void onDeleteTaskBtnClicked(ToDoData toDoData) {
        String taskId = toDoData.getTaskid();
        DocumentReference documentRef = db.collection("users").document(auth.getCurrentUser().getUid()).collection("tasks").document(taskId);
        documentRef.delete()
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void unused) {
                        Toast.makeText(getContext(), "Deleted", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(getContext(), "Failed to delete: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    @Override
    public void onEditTaskBtnClicked(ToDoData toDoData) {
        if (popupFragment != null)
            getChildFragmentManager().beginTransaction().remove(popupFragment).commit();
        popupFragment = AddTodoPopupFragment.newInstance(toDoData.getTaskid(), toDoData.getTask(), toDoData.getDate(), toDoData.getTime(), toDoData.isCompleted());
        popupFragment.setListener(this);
        popupFragment.show(getChildFragmentManager(), AddTodoPopupFragment.TAG);
    }

    @Override
    public void onSaveTask(String todo, TextInputEditText popuptodotaskname, String popupdate, String popuptime) {
        CollectionReference collectionRef = db.collection("users").document(auth.getCurrentUser().getUid()).collection("tasks");
        DocumentReference newTaskRef = collectionRef.document();

        SimpleDateFormat dateFormat = new SimpleDateFormat("MMMM d, yyyy", Locale.ENGLISH);
        Date date = null;
        try {
            date = dateFormat.parse(popupdate);
        } catch (ParseException e) {
            e.printStackTrace();
        }

        SimpleDateFormat timeFormat = new SimpleDateFormat("h:mm:ss a", Locale.ENGLISH);
        timeFormat.setTimeZone(TimeZone.getDefault());
        Date time = null;
        try {
            time = timeFormat.parse(popuptime);
        } catch (ParseException e) {
            e.printStackTrace();
        }

        Calendar dateTime = Calendar.getInstance();
        dateTime.setTimeZone(TimeZone.getDefault());
        dateTime.setTime(time);
        dateTime.set(Calendar.YEAR, date.getYear() + 1900);
        dateTime.set(Calendar.MONTH, date.getMonth());
        dateTime.set(Calendar.DAY_OF_MONTH, date.getDate());

        Timestamp timestamp = new Timestamp(dateTime.getTime());

        HashMap<String, Object> taskMap = new HashMap<>();
        taskMap.put("name", popuptodotaskname.getText().toString());
        taskMap.put("date", popupdate);
        taskMap.put("time", popuptime);
        taskMap.put("timestamp", timestamp);

        newTaskRef.set(taskMap)
                .addOnCompleteListener(task -> {
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
    }
}


//public class SchedularFragment extends Fragment {
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
//    /**
//     * Use this factory method to create a new instance of
//     * this fragment using the provided parameters.
//     *
//     * @param param1 Parameter 1.
//     * @param param2 Parameter 2.
//     * @return A new instance of fragment SchedularFragment.
//     */
//    // TODO: Rename and change types and number of parameters
//    public static SchedularFragment newInstance(String param1, String param2) {
//        SchedularFragment fragment = new SchedularFragment();
//        Bundle args = new Bundle();
//        args.putString(ARG_PARAM1, param1);
//        args.putString(ARG_PARAM2, param2);
//        fragment.setArguments(args);
//        return fragment;
//    }
//
//    public SchedularFragment() {
//        // Required empty public constructor
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
//        return inflater.inflate(R.layout.fragment_schedular, container, false);
//    }
//}