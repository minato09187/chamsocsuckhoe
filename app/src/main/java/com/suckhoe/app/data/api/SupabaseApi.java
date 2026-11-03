package com.suckhoe.app.data.api;

import com.suckhoe.app.data.model.NutritionItem;
import com.suckhoe.app.data.model.WaterLog;
import com.suckhoe.app.data.model.WeightRecord;
import com.suckhoe.app.data.model.WorkoutExercise;
import com.suckhoe.app.data.model.WorkoutSchedule;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.DELETE;
import retrofit2.http.GET;
import retrofit2.http.PATCH;
import retrofit2.http.POST;
import retrofit2.http.Query;

public interface SupabaseApi {

    // ==========================================
    // 1. DINH DƯỠNG (nutrition_logs)
    // ==========================================
    @GET("nutrition_logs?select=*&order=created_at.desc")
    Call<List<NutritionItem>> getNutritionLogs();

    @GET("nutrition_logs?select=*")
    Call<List<NutritionItem>> getNutritionLogsByDate(@Query("log_date") String logDateFilter);

    @POST("nutrition_logs")
    Call<List<NutritionItem>> insertNutritionLog(@Body NutritionItem item);

    @DELETE("nutrition_logs")
    Call<Void> deleteNutritionLog(@Query("id") String idFilter); // format: "eq.1"


    // ==========================================
    // 2. CÂN NẶNG (weight_logs)
    // ==========================================
    @GET("weight_logs?select=*&order=logged_date.desc")
    Call<List<WeightRecord>> getWeightLogs();

    @POST("weight_logs")
    Call<List<WeightRecord>> insertWeightLog(@Body WeightRecord record);

    @DELETE("weight_logs")
    Call<Void> deleteWeightLog(@Query("id") String idFilter);


    // ==========================================
    // 3. TẬP LUYỆN (workout_logs)
    // ==========================================
    @GET("workout_logs?select=*&order=created_at.desc")
    Call<List<WorkoutExercise>> getWorkoutLogs();

    @GET("workout_logs?select=*")
    Call<List<WorkoutExercise>> getWorkoutLogsByDate(@Query("log_date") String logDateFilter);

    @POST("workout_logs")
    Call<List<WorkoutExercise>> insertWorkoutLog(@Body WorkoutExercise exercise);

    @DELETE("workout_logs")
    Call<Void> deleteWorkoutLog(@Query("id") String idFilter);


    // ==========================================
    // 4. UỐNG NƯỚC (water_logs)
    // ==========================================
    @GET("water_logs?select=*&order=log_date.desc")
    Call<List<WaterLog>> getWaterLogs();

    @GET("water_logs?select=*")
    Call<List<WaterLog>> getWaterLogByDate(@Query("log_date") String logDateFilter);

    @POST("water_logs")
    Call<List<WaterLog>> insertWaterLog(@Body WaterLog waterLog);


    // ==========================================
    // 5. LỊCH TẬP (workout_schedules)
    // ==========================================
    @GET("workout_schedules?select=*&order=day_of_week.asc")
    Call<List<WorkoutSchedule>> getWorkoutSchedules();

    @POST("workout_schedules")
    Call<List<WorkoutSchedule>> insertWorkoutSchedule(@Body WorkoutSchedule schedule);

    @PATCH("workout_schedules")
    Call<List<WorkoutSchedule>> updateWorkoutSchedule(@Query("id") String idFilter, @Body WorkoutSchedule schedule);

    @DELETE("workout_schedules")
    Call<Void> deleteWorkoutSchedule(@Query("id") String idFilter);
}

