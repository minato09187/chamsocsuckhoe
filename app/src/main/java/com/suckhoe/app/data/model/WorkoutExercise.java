package com.suckhoe.app.data.model;

import com.google.gson.annotations.SerializedName;

public class WorkoutExercise {
    @SerializedName("id")
    private Long id;

    @SerializedName("exercise_name")
    private String exerciseName;

    @SerializedName("muscle_group")
    private String muscleGroup;

    @SerializedName("sets")
    private int sets;

    @SerializedName("reps")
    private int reps;

    @SerializedName("weight")
    private double weight; // kg

    @SerializedName("duration_minutes")
    private int durationMinutes;

    @SerializedName("calories_burned")
    private int caloriesBurned;

    @SerializedName("log_date")
    private String logDate;

    @SerializedName("created_at")
    private String createdAt;

    public WorkoutExercise() {}

    public WorkoutExercise(String exerciseName, String muscleGroup, int sets, int reps, double weight, int durationMinutes, int caloriesBurned, String logDate) {
        this.exerciseName = exerciseName;
        this.muscleGroup = muscleGroup;
        this.sets = sets;
        this.reps = reps;
        this.weight = weight;
        this.durationMinutes = durationMinutes;
        this.caloriesBurned = caloriesBurned;
        this.logDate = logDate;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getExerciseName() { return exerciseName; }
    public void setExerciseName(String exerciseName) { this.exerciseName = exerciseName; }

    public String getMuscleGroup() { return muscleGroup; }
    public void setMuscleGroup(String muscleGroup) { this.muscleGroup = muscleGroup; }

    public int getSets() { return sets; }
    public void setSets(int sets) { this.sets = sets; }

    public int getReps() { return reps; }
    public void setReps(int reps) { this.reps = reps; }

    public double getWeight() { return weight; }
    public void setWeight(double weight) { this.weight = weight; }

    public int getDurationMinutes() { return durationMinutes; }
    public void setDurationMinutes(int durationMinutes) { this.durationMinutes = durationMinutes; }

    public int getCaloriesBurned() { return caloriesBurned; }
    public void setCaloriesBurned(int caloriesBurned) { this.caloriesBurned = caloriesBurned; }

    public String getLogDate() { return logDate; }
    public void setLogDate(String logDate) { this.logDate = logDate; }

    public String getCreatedAt() { return createdAt; }
    public void setCreatedAt(String createdAt) { this.createdAt = createdAt; }
}
