package com.suckhoe.app.data.model;

import com.google.gson.annotations.SerializedName;

public class WorkoutSchedule {
    @SerializedName("id")
    private Long id;

    @SerializedName("day_of_week")
    private String dayOfWeek; // Thứ 2, Thứ 3, ..., Chủ Nhật

    @SerializedName("title")
    private String title;

    @SerializedName("target_muscle")
    private String targetMuscle;

    @SerializedName("time_of_day")
    private String timeOfDay; // HH:mm

    @SerializedName("is_completed")
    private boolean isCompleted;

    @SerializedName("notes")
    private String notes;

    @SerializedName("created_at")
    private String createdAt;

    public WorkoutSchedule() {}

    public WorkoutSchedule(String dayOfWeek, String title, String targetMuscle, String timeOfDay, boolean isCompleted, String notes) {
        this.dayOfWeek = dayOfWeek;
        this.title = title;
        this.targetMuscle = targetMuscle;
        this.timeOfDay = timeOfDay;
        this.isCompleted = isCompleted;
        this.notes = notes;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getDayOfWeek() { return dayOfWeek; }
    public void setDayOfWeek(String dayOfWeek) { this.dayOfWeek = dayOfWeek; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getTargetMuscle() { return targetMuscle; }
    public void setTargetMuscle(String targetMuscle) { this.targetMuscle = targetMuscle; }

    public String getTimeOfDay() { return timeOfDay; }
    public void setTimeOfDay(String timeOfDay) { this.timeOfDay = timeOfDay; }

    public boolean isCompleted() { return isCompleted; }
    public void setCompleted(boolean completed) { isCompleted = completed; }

    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }

    public String getCreatedAt() { return createdAt; }
    public void setCreatedAt(String createdAt) { this.createdAt = createdAt; }
}

