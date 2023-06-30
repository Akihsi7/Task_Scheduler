package com.example.schedulertodo.utils;

import static android.content.ContentValues.TAG;

import android.graphics.Color;
import android.graphics.Paint;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;


import com.example.schedulertodo.databinding.EachTodoItemBinding;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TodoAdapter extends RecyclerView.Adapter<TodoAdapter.TodoViewHolder> {
    private List<ToDoData> list;
    private ToDoAdapterClicksInterface listener;
    private boolean checked = false;
    private FirebaseAuth auth;
    private FirebaseFirestore db;
    private Map<Integer, Boolean> checkboxStates = new HashMap<>();

    public TodoAdapter(List<ToDoData> list) {
        this.list = list;
        auth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
    }

    public void setListener(ToDoAdapterClicksInterface listener) {
        this.listener = listener;
    }


    public static class TodoViewHolder extends RecyclerView.ViewHolder {
        EachTodoItemBinding binding;

        public TodoViewHolder(EachTodoItemBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }

    @NonNull
    @Override
    public TodoViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        EachTodoItemBinding binding = EachTodoItemBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false);
        return new TodoViewHolder(binding);

    }

    @Override
    public void onBindViewHolder(TodoViewHolder holder, int position) {
        ToDoData toDoData = list.get(position);
        holder.binding.todoTask.setText(toDoData.getTask());
        holder.binding.todoTime.setText(toDoData.getTime());
        holder.binding.todoDate.setText(toDoData.getDate());
        holder.binding.checkbox.setChecked(toDoData.isCompleted());

        holder.binding.deleteTask.setOnClickListener(v -> {
            if (listener != null) {
                listener.onDeleteTaskBtnClicked(toDoData);
            }
        });

        holder.binding.editTask.setOnClickListener(v -> {
            if (listener != null) {
                listener.onEditTaskBtnClicked(toDoData);
            }
        });

        String uid = auth.getCurrentUser() != null ? auth.getCurrentUser().getUid() : "";
        db.collection("users").document(uid).collection("tasks").document(toDoData.getTaskid())
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot != null) {
                        boolean completed = documentSnapshot.getBoolean("completed") != null ? documentSnapshot.getBoolean("completed") : false;
                        if (completed) {
                            holder.binding.todoTask.setTextColor(Color.GRAY);
                            holder.binding.todoDate.setTextColor(Color.GRAY);
                            holder.binding.todoTime.setTextColor(Color.GRAY);
                            holder.binding.todoTask.setPaintFlags(holder.binding.todoTask.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
                            holder.binding.checkbox.setChecked(true);
                        } else {
                            holder.binding.todoTask.setTextColor(Color.BLACK);
                            holder.binding.todoDate.setTextColor(Color.BLACK);
                            holder.binding.todoTime.setTextColor(Color.BLACK);
                            holder.binding.todoTask.setPaintFlags(holder.binding.todoTask.getPaintFlags() & (~Paint.STRIKE_THRU_TEXT_FLAG));
                        }
                    } else {
                        // handle document not found case
                    }
                })
                .addOnFailureListener(e -> {
                    // handle exception
                });

        holder.binding.checkbox.setOnClickListener(v -> {
            boolean isChecked = holder.binding.checkbox.isChecked();
            onCheckBoxClicked(position, isChecked);
        });
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    public void onCheckBoxClicked(int position, boolean isChecked) {
        ToDoData todo = list.get(position);
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        String uid = auth.getCurrentUser() != null ? auth.getCurrentUser().getUid() : "";
        DocumentReference taskRef = db.collection("users").document(uid)
                .collection("tasks").document(todo.getTaskid());

        Map<String, Object> updates = new HashMap<>();
        updates.put("completed", isChecked);

        taskRef.update(updates)
                .addOnSuccessListener(aVoid -> {
                    todo.setCompleted(isChecked);
                    checkboxStates.put(position, isChecked);
                    notifyDataSetChanged();
                })
                .addOnFailureListener(e -> {
                    Log.w(TAG, "Error updating document", e);
                });
    }

    public interface ToDoAdapterClicksInterface {
        void onDeleteTaskBtnClicked(ToDoData toDoData);

        void onEditTaskBtnClicked(ToDoData toDoData);
    }
}
