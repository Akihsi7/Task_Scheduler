package com.example.schedulertodo.utils;

import java.io.Serializable;

public class ToDoData implements Serializable {
    private String taskid;
    private String task;
    private String date;
    private String time;
    private boolean completed=false;

    public ToDoData(String taskid, String task, String date, String time, boolean completed) {
        this.taskid = taskid;
        this.task = task;
        this.date = date;
        this.time = time;
        this.completed = completed;
    }

    public ToDoData(String taskid, String task, String date, String time) {
        this.taskid = taskid;
        this.task = task;
        this.date = date;
        this.time = time;
        completed = false;
    }

    public String getTaskid() {
        return taskid;
    }

    public void setTaskid(String taskid) {
        this.taskid = taskid;
    }

    public String getTask() {
        return task;
    }

    public void setTask(String task) {
        this.task = task;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public boolean isCompleted() {
        return completed;
    }

    public void setCompleted(boolean completed) {
        this.completed = completed;
    }
}
