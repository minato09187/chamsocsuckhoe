package com.suckhoe.app.data.model;

import com.google.gson.annotations.SerializedName;

public class NutritionItem {
    @SerializedName("id")
    private Long id;

    @SerializedName("food_name")
    private String foodName;

    @SerializedName("calories")
    private int calories;

    @SerializedName("protein")
    private double protein;

    @SerializedName("carbs")
    private double carbs;

    @SerializedName("fat")
    private double fat;

    @SerializedName("meal_type")
    private String mealType; // Bữa sáng, Bữa trưa, Bữa tối, Ăn vặt

    @SerializedName("log_date")
    private String logDate;

    @SerializedName("created_at")
    private String createdAt;

    public NutritionItem() {}

    public NutritionItem(String foodName, int calories, double protein, double carbs, double fat, String mealType, String logDate) {
        this.foodName = foodName;
        this.calories = calories;
        this.protein = protein;
        this.carbs = carbs;
        this.fat = fat;
        this.mealType = mealType;
        this.logDate = logDate;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getFoodName() { return foodName; }
    public void setFoodName(String foodName) { this.foodName = foodName; }

    public int getCalories() { return calories; }
    public void setCalories(int calories) { this.calories = calories; }

    public double getProtein() { return protein; }
    public void setProtein(double protein) { this.protein = protein; }

    public double getCarbs() { return carbs; }
    public void setCarbs(double carbs) { this.carbs = carbs; }

    public double getFat() { return fat; }
    public void setFat(double fat) { this.fat = fat; }

    public String getMealType() { return mealType; }
    public void setMealType(String mealType) { this.mealType = mealType; }

    public String getLogDate() { return logDate; }
    public void setLogDate(String logDate) { this.logDate = logDate; }

    public String getCreatedAt() { return createdAt; }
    public void setCreatedAt(String createdAt) { this.createdAt = createdAt; }
}
