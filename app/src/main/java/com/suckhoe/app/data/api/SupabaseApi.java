package com.suckhoe.app.data.api;

import com.suckhoe.app.data.model.NutritionItem;
import com.suckhoe.app.data.model.User;
import com.suckhoe.app.data.model.WaterLog;
import com.suckhoe.app.data.model.WeightRecord;
import com.suckhoe.app.data.model.WorkoutExercise;
import com.suckhoe.app.data.model.WorkoutSchedule;

import java.util.List;
import java.util.Map;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.DELETE;
import retrofit2.http.GET;
import retrofit2.http.PATCH;
import retrofit2.http.POST;
import retrofit2.http.Query;

public interface SupabaseApi {

    // ==========================================
    // 0. TÀI KHOẢN NGƯỜI DÙNG (app_users)
    // ==========================================
    @GET("app_users?select=*")
    Call<List<User>> loginUser(
            @Query("username") String usernameFilter,
            @Query("password") String passwordFilter
    );

    @POST("app_users")
    Call<List<User>> registerUser(@Body User user);


    // ==========================================
    // 1. DINH DƯỠNG (nutrition_logs)
    // ==========================================
    @GET("nutrition_logs?select=*&order=created_at.desc")
    Call<List<NutritionItem>> getNutritionLogsByUser(@Query("user_id") String userIdFilter);

    @GET("nutrition_logs?select=*")
    Call<List<NutritionItem>> getNutritionLogsByDateAndUser(
            @Query("user_id") String userIdFilter,
            @Query("log_date") String logDateFilter
    );

    @POST("nutrition_logs")
    Call<List<NutritionItem>> insertNutritionLog(@Body NutritionItem item);

    @DELETE("nutrition_logs")
    Call<Void> deleteNutritionLog(@Query("id") String idFilter);

    @DELETE("nutrition_logs")
    Call<Void> deleteNutritionLogByDetails(
            @Query("user_id") String userIdFilter,
            @Query("food_name") String foodNameFilter,
            @Query("log_date") String logDateFilter
    );


    // ==========================================
    // 2. CÂN NẶNG (weight_logs)
    // ==========================================
    @GET("weight_logs?select=*&order=logged_date.desc")
    Call<List<WeightRecord>> getWeightLogsByUser(@Query("user_id") String userIdFilter);

    @POST("weight_logs")
    Call<List<WeightRecord>> insertWeightLog(@Body WeightRecord record);

    @DELETE("weight_logs")
    Call<Void> deleteWeightLog(@Query("id") String idFilter);

    @DELETE("weight_logs")
    Call<Void> deleteWeightLogByDetails(
            @Query("user_id") String userIdFilter,
            @Query("logged_date") String logDateFilter
    );


    // ==========================================
    // 3. TẬP LUYỆN (workout_logs)
    // ==========================================
    @GET("workout_logs?select=*&order=created_at.desc")
    Call<List<WorkoutExercise>> getWorkoutLogsByUser(@Query("user_id") String userIdFilter);

    @GET("workout_logs?select=*")
    Call<List<WorkoutExercise>> getWorkoutLogsByDateAndUser(
            @Query("user_id") String userIdFilter,
            @Query("log_date") String logDateFilter
    );

    @POST("workout_logs")
    Call<List<WorkoutExercise>> insertWorkoutLog(@Body WorkoutExercise exercise);

    @DELETE("workout_logs")
    Call<Void> deleteWorkoutLog(@Query("id") String idFilter);

    @DELETE("workout_logs")
    Call<Void> deleteWorkoutLogByDetails(
            @Query("user_id") String userIdFilter,
            @Query("exercise_name") String exerciseNameFilter,
            @Query("log_date") String logDateFilter
    );


    // ==========================================
    // 4. UỐNG NƯỚC (water_logs)
    // ==========================================
    @GET("water_logs?select=*&order=log_date.desc")
    Call<List<WaterLog>> getWaterLogsByUser(@Query("user_id") String userIdFilter);

    @GET("water_logs?select=*&order=id.desc")
    Call<List<WaterLog>> getWaterLogByDateAndUser(
            @Query("user_id") String userIdFilter,
            @Query("log_date") String logDateFilter
    );

    @PATCH("water_logs")
    Call<List<WaterLog>> updateWaterLog(
            @Query("user_id") String userIdFilter,
            @Query("log_date") String logDateFilter,
            @Body Map<String, Object> fields
    );

    @POST("water_logs")
    Call<List<WaterLog>> insertWaterLog(@Body WaterLog waterLog);


    // ==========================================
    // 5. LỊCH TẬP (workout_schedules)
    // ==========================================
    @GET("workout_schedules?select=*&order=day_of_week.asc")
    Call<List<WorkoutSchedule>> getWorkoutSchedulesByUser(@Query("user_id") String userIdFilter);

    @POST("workout_schedules")
    Call<List<WorkoutSchedule>> insertWorkoutSchedule(@Body WorkoutSchedule schedule);

    @PATCH("workout_schedules")
    Call<List<WorkoutSchedule>> updateWorkoutSchedule(@Query("id") String idFilter, @Body WorkoutSchedule schedule);

    @DELETE("workout_schedules")
    Call<Void> deleteWorkoutSchedule(@Query("id") String idFilter);

    @DELETE("workout_schedules")
    Call<Void> deleteWorkoutScheduleByDetails(
            @Query("user_id") String userIdFilter,
            @Query("day_of_week") String dayOfWeekFilter,
            @Query("title") String titleFilter
    );
}
