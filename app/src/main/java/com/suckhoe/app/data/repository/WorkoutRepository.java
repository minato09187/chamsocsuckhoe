package com.suckhoe.app.data.repository;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;

import com.suckhoe.app.data.api.SupabaseClient;
import com.suckhoe.app.data.local.AppDatabaseHelper;
import com.suckhoe.app.data.model.WorkoutExercise;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class WorkoutRepository {
    private final AppDatabaseHelper dbHelper;
    private final Context context;
    private final ExecutorService executor = Executors.newSingleThreadExecutor();
    private final Handler mainHandler = new Handler(Looper.getMainLooper());

    public interface DataCallback<T> {
        void onSuccess(T data);
        void onError(String message);
    }

    public WorkoutRepository(Context context) {
        this.context = context.getApplicationContext();
        this.dbHelper = AppDatabaseHelper.getInstance(this.context);
    }

    public void getWorkoutsByDate(String date, DataCallback<List<WorkoutExercise>> callback) {
        executor.execute(() -> {
            List<WorkoutExercise> list = dbHelper.getWorkoutsByDate(date);
            mainHandler.post(() -> callback.onSuccess(list));

            if (SupabaseClient.hasValidApiKey(context)) {
                SupabaseClient.getApi(context).getWorkoutLogsByDate("eq." + date).enqueue(new Callback<List<WorkoutExercise>>() {
                    @Override
                    public void onResponse(Call<List<WorkoutExercise>> call, Response<List<WorkoutExercise>> response) {
                        if (response.isSuccessful() && response.body() != null && !response.body().isEmpty()) {
                            mainHandler.post(() -> callback.onSuccess(response.body()));
                        }
                    }

                    @Override
                    public void onFailure(Call<List<WorkoutExercise>> call, Throwable t) {}
                });
            }
        });
    }

    public void addWorkout(WorkoutExercise exercise, DataCallback<Long> callback) {
        executor.execute(() -> {
            long localId = dbHelper.insertWorkout(exercise);
            mainHandler.post(() -> callback.onSuccess(localId));

            if (SupabaseClient.hasValidApiKey(context)) {
                SupabaseClient.getApi(context).insertWorkoutLog(exercise).enqueue(new Callback<List<WorkoutExercise>>() {
                    @Override
                    public void onResponse(Call<List<WorkoutExercise>> call, Response<List<WorkoutExercise>> response) {}
                    @Override
                    public void onFailure(Call<List<WorkoutExercise>> call, Throwable t) {}
                });
            }
        });
    }

    public void deleteWorkout(long id, DataCallback<Boolean> callback) {
        executor.execute(() -> {
            dbHelper.deleteWorkout(id);
            mainHandler.post(() -> callback.onSuccess(true));

            if (SupabaseClient.hasValidApiKey(context)) {
                SupabaseClient.getApi(context).deleteWorkoutLog("eq." + id).enqueue(new Callback<Void>() {
                    @Override
                    public void onResponse(Call<Void> call, Response<Void> response) {}
                    @Override
                    public void onFailure(Call<Void> call, Throwable t) {}
                });
            }
        });
    }
}

