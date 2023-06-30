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

import com.example.schedulertodo.databinding.FragmentPendingBinding;
import com.example.schedulertodo.utils.ToDoData;
import com.example.schedulertodo.utils.TodoAdapter;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.WriteBatch;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

public class PendingFragment extends Fragment implements TodoAdapter.ToDoAdapterClicksInterface,
        AddTodoPopupFragment.DialogNextButtonClickListener {

    private FirebaseAuth auth;
    private FirebaseFirestore db;
    private NavController navController;
    private FragmentPendingBinding binding;
    private AddTodoPopupFragment popupFragment;
    private TodoAdapter adapter;
    private List<ToDoData> mList;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        binding = FragmentPendingBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        init(view);
        if (isAdded()) {
            getDataFromFirebase();
        }
    }

    private void init(View view) {
        navController = Navigation.findNavController(view);
        auth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
        binding.recyclerview3.setHasFixedSize(true);
        binding.recyclerview3.setLayoutManager(new LinearLayoutManager(getContext()));
        mList = new ArrayList<>();
        adapter = new TodoAdapter(mList);
        adapter.setListener(this);
        binding.recyclerview3.setAdapter(adapter);
    }

    private void getDataFromFirebase() {
        CollectionReference collectionRef = db.collection("users")
                .document(auth.getCurrentUser().getUid())
                .collection("tasks");

        Date currentDate = new Date();
        SimpleDateFormat dateFormat = new SimpleDateFormat("MMMM dd, yyyy", Locale.getDefault());
        String formattedDate = dateFormat.format(currentDate);
        Date currentParsedDate;
        try {
            currentParsedDate = dateFormat.parse(formattedDate);
        } catch (ParseException e) {
            e.printStackTrace();
            return;
        }

        collectionRef.orderBy("timestamp")
                .addSnapshotListener((snapshot, exception) -> {
                    if (exception != null) {
                        Log.w(AddTodoPopupFragment.TAG, "Listen failed", exception);
                        return;
                    }

                    if (snapshot != null) {
                        List<ToDoData> taskList = new ArrayList<>();
                        for (QueryDocumentSnapshot doc : snapshot) {
                            String task = doc.getString("name");
                            String date = doc.getString("date");
                            String time = doc.getString("time");
                            Boolean completed = doc.getBoolean("completed");
                            String taskId = doc.getId();
                            Date dateparsed;
                            try {
                                dateparsed = dateFormat.parse(date);
                            } catch (ParseException e) {
                                e.printStackTrace();
                                continue;
                            }
                            if (dateparsed != null) {
                                if (task != null && date != null && time != null && dateparsed.before(currentParsedDate)) {
                                    if (completed != null && completed) {
                                        DocumentReference documentRef = db.collection("users")
                                                .document(auth.getCurrentUser().getUid())
                                                .collection("tasks")
                                                .document(taskId);

                                        documentRef.delete()
                                                .addOnSuccessListener(aVoid -> {
                                                    if (isAdded()) {
                                                        Toast.makeText(requireContext(),
                                                                "Deleted completed task",
                                                                Toast.LENGTH_SHORT).show();
                                                    }
                                                })
                                                .addOnFailureListener(e -> {
                                                    if (isAdded()) {
                                                        Toast.makeText(requireContext(),
                                                                "Failed to delete completed task: " + e.getMessage(),
                                                                Toast.LENGTH_SHORT).show();
                                                    }
                                                });
                                    } else {
                                        taskList.add(new ToDoData(taskId, task, date, time));
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
    public void onDeleteTaskBtnClicked(ToDoData toDoData) {
        String taskId = toDoData.getTaskid();
        DocumentReference documentRef = db.collection("users").document(auth.getCurrentUser().getUid()).collection("tasks").document(taskId);
        documentRef.delete()
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
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
        if (popupFragment != null) {
            getChildFragmentManager().beginTransaction().remove(popupFragment).commit();
        }
        popupFragment = AddTodoPopupFragment.newInstance(toDoData.getTaskid(), toDoData.getTask(), toDoData.getDate(), toDoData.getTime(), toDoData.isCompleted());
        popupFragment.setListener(this);
        popupFragment.show(getChildFragmentManager(), AddTodoPopupFragment.TAG);
    }

    @Override
    public void onSaveTask(String todo, TextInputEditText popuptodotaskname, String popupdate, String popuptime) {
// Not yet implemented
    }

    @Override
    public void onUpdateTask(ToDoData toDoData, TextInputEditText popuptodotaskname, String popupdate, String popuptime) {
        String taskId = toDoData.getTaskid();
        String name = popuptodotaskname.getText().toString();
        DocumentReference taskRef = db.collection("users").document(auth.getCurrentUser().getUid()).collection("tasks").document(taskId);

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

}


//public class PendingFragment extends Fragment {
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
//    public PendingFragment() {
//        // Required empty public constructor
//    }
//
//    /**
//     * Use this factory method to create a new instance of
//     * this fragment using the provided parameters.
//     *
//     * @param param1 Parameter 1.
//     * @param param2 Parameter 2.
//     * @return A new instance of fragment PendingFragment.
//     */
//    // TODO: Rename and change types and number of parameters
//    public static PendingFragment newInstance(String param1, String param2) {
//        PendingFragment fragment = new PendingFragment();
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
//        return inflater.inflate(R.layout.fragment_pending, container, false);
//    }
//}