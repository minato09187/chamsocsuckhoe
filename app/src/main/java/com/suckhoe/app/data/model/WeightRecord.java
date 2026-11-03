package com.suckhoe.app.data.model;

import com.google.gson.annotations.SerializedName;

public class WeightRecord {
    @SerializedName("id")
    private Long id;

    @SerializedName("user_id")
    private Long userId;

    @SerializedName("weight")
    private double weight; // kg

    @SerializedName("height")
    private double height; // cm

    @SerializedName("bmi")
    private double bmi;

    @SerializedName("note")
    private String note;

    @SerializedName("logged_date")
    private String loggedDate;

    @SerializedName("created_at")
    private String createdAt;

    public WeightRecord() {}

    public WeightRecord(Long userId, double weight, double height, double bmi, String note, String loggedDate) {
        this.userId = userId;
        this.weight = weight;
        this.height = height;
        this.bmi = bmi;
        this.note = note;
        this.loggedDate = loggedDate;
    }

    public static double calculateBmi(double weightKg, double heightCm) {
        if (heightCm <= 0) return 0;
        double heightM = heightCm / 100.0;
        return Math.round((weightKg / (heightM * heightM)) * 10.0) / 10.0;
    }

    public String getBmiCategory() {
        if (bmi < 18.5) return "Thiếu cân";
        if (bmi < 23.0) return "Bình thường";
        if (bmi < 25.0) return "Thừa cân";
        if (bmi < 30.0) return "Béo phì độ 1";
        return "Béo phì độ 2";
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }

    public double getWeight() { return weight; }
    public void setWeight(double weight) { this.weight = weight; }

    public double getHeight() { return height; }
    public void setHeight(double height) { this.height = height; }

    public double getBmi() { return bmi; }
    public void setBmi(double bmi) { this.bmi = bmi; }

    public String getNote() { return note; }
    public void setNote(String note) { this.note = note; }

    public String getLoggedDate() { return loggedDate; }
    public void setLoggedDate(String loggedDate) { this.loggedDate = loggedDate; }

    public String getCreatedAt() { return createdAt; }
    public void setCreatedAt(String createdAt) { this.createdAt = createdAt; }
}
