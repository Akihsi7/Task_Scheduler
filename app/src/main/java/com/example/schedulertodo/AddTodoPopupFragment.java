package com.example.schedulertodo;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;

import com.example.schedulertodo.databinding.FragmentAddTodoPopupBinding;
import com.example.schedulertodo.utils.ToDoData;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.google.android.material.textfield.TextInputEditText;

import java.text.SimpleDateFormat;
import java.util.*;






public class AddTodoPopupFragment extends BottomSheetDialogFragment implements DatePickerDialog.OnDateSetListener, TimePickerDialog.OnTimeSetListener {
    private FragmentAddTodoPopupBinding binding;
    private DialogNextButtonClickListener listener;
    private ToDoData toDoData;

    public void setListener(@NonNull HomeFragment listener) {
        this.listener = listener;
    }
    public void setListener(@NonNull SchedularFragment listener) {
        this.listener = listener;
    }
    public void setListener(@NonNull PendingFragment listener) {
        this.listener = listener;
    }
    public static final String TAG = "AddTodoPopupFragment";

    public static AddTodoPopupFragment newInstance(String taskId, String task, String date, String time, boolean completed) {
        AddTodoPopupFragment fragment = new AddTodoPopupFragment();
        Bundle args = new Bundle();
        args.putString("taskId", taskId);
        args.putString("task", task);
        args.putString("date", date);
        args.putString("time", time);
        args.putBoolean("Completed", completed);
        fragment.setArguments(args);
        return fragment;
    }
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentAddTodoPopupBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        if (getArguments() != null) {
            Boolean completed = getArguments().getBoolean("Completed");
            toDoData = new ToDoData(
                    getArguments().getString("taskId"),
                    getArguments().getString("task"),
                    getArguments().getString("date"),
                    getArguments().getString("time"),
                    completed
            );
            binding.popuptodotaskname.setText(toDoData.getTask());
            binding.popupdate.setText(toDoData.getDate());
            binding.popuptime.setText(toDoData.getTime());
        }
        registerEvents();
    }
    private void registerEvents() {
        binding.popupsave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String todotask = binding.popuptodotaskname.getText().toString();
                String tododate = binding.popupdate.getText().toString();
                String todotime = binding.popuptime.getText().toString();
                if (!todotask.isEmpty() && !tododate.isEmpty() && !todotime.isEmpty()) {
                    if (toDoData == null) {
                        listener.onSaveTask(todotask, binding.popuptodotaskname, binding.popupdate.getText().toString(), binding.popuptime.getText().toString());
                    } else {
                        toDoData.setTask(todotask);
                        toDoData.setDate(tododate);
                        toDoData.setTime(todotime);
                        listener.onUpdateTask(toDoData, binding.popuptodotaskname, binding.popupdate.getText().toString(), binding.popuptime.getText().toString());
                    }
                } else {
                    Toast.makeText(getContext(), "Please type some task", Toast.LENGTH_SHORT).show();
                }
            }
        });

        binding.popupdate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Calendar calendar = Calendar.getInstance();
                int year = calendar.get(Calendar.YEAR);
                int month = calendar.get(Calendar.MONTH);
                int dayOfMonth = calendar.get(Calendar.DAY_OF_MONTH);
                DatePickerDialog datePickerDialog = new DatePickerDialog(requireContext(), new DatePickerDialog.OnDateSetListener() {
                    @Override
                    public void onDateSet(DatePicker datePicker, int year, int month, int dayOfMonth) {
                        SimpleDateFormat dateFormat = new SimpleDateFormat("MMMM dd, yyyy", Locale.getDefault());
                        Calendar calendar = Calendar.getInstance();
                        calendar.set(Calendar.YEAR, year);
                        calendar.set(Calendar.MONTH, month);
                        calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);
                        String date = dateFormat.format(calendar.getTime());
                        binding.popupdate.setText(date);
                    }
                }, year, month, dayOfMonth);
                datePickerDialog.show();
            }
        });

        binding.popuptime.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Calendar calendar = Calendar.getInstance();
                int hour = calendar.get(Calendar.HOUR_OF_DAY);
                int minute = calendar.get(Calendar.MINUTE);
                TimePickerDialog timePickerDialog = new TimePickerDialog(requireContext(), new TimePickerDialog.OnTimeSetListener() {
                    @Override
                    public void onTimeSet(TimePicker timePicker, int hourOfDay, int minute) {
                        SimpleDateFormat timeFormat = new SimpleDateFormat("hh:mm:ss a", Locale.getDefault());
                        Calendar calendar = Calendar.getInstance();
                        calendar.set(Calendar.HOUR_OF_DAY, hourOfDay);
                        calendar.set(Calendar.MINUTE, minute);
                        String time = timeFormat.format(calendar.getTime());
                        binding.popuptime.setText(time);
                    }
                }, hour, minute, false);
                timePickerDialog.show();
            }
        });
    }
    public interface DialogNextButtonClickListener {
        void onSaveTask(String todo, TextInputEditText popuptodotaskname, String popupdate, String popuptime);
        void onUpdateTask(ToDoData toDoData, TextInputEditText popuptodotaskname, String popupdate, String popuptime);
    }

    @Override
    public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
        // Update the date EditText with the selected date
        SimpleDateFormat dateFormat = new SimpleDateFormat("MMM dd, yyyy", Locale.getDefault());
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.YEAR, year);
        calendar.set(Calendar.MONTH, month);
        calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);
        String date = dateFormat.format(calendar.getTime());
        binding.popupdate.setText(date);
    }

    @Override
    public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
        // Update the time EditText with the selected time
        SimpleDateFormat timeFormat = new SimpleDateFormat("hh:mm:ss a", Locale.getDefault());
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.HOUR_OF_DAY, hourOfDay);
        calendar.set(Calendar.MINUTE, minute);
        String time = timeFormat.format(calendar.getTime());
        binding.popuptime.setText(time);
    }
}

///**
// * A simple {@link Fragment} subclass.
// * Use the {@link AddTodoPopupFragment#newInstance} factory method to
// * create an instance of this fragment.
// */
//public class AddTodoPopupFragment extends Fragment {
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
//    public AddTodoPopupFragment() {
//        // Required empty public constructor
//    }
//
//    /**
//     * Use this factory method to create a new instance of
//     * this fragment using the provided parameters.
//     *
//     * @param param1 Parameter 1.
//     * @param param2 Parameter 2.
//     * @return A new instance of fragment AddTodoPopupFragment.
//     */
//    // TODO: Rename and change types and number of parameters
//    public static AddTodoPopupFragment newInstance(String param1, String param2) {
//        AddTodoPopupFragment fragment = new AddTodoPopupFragment();
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
//        return inflater.inflate(R.layout.fragment_add_todo_popup, container, false);
//    }
//}