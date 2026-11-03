package com.suckhoe.app.data.repository;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;

import com.suckhoe.app.data.api.SupabaseClient;
import com.suckhoe.app.data.local.AppDatabaseHelper;
import com.suckhoe.app.data.model.WorkoutSchedule;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ScheduleRepository {
    private final AppDatabaseHelper dbHelper;
    private final Context context;
    private final ExecutorService executor = Executors.newSingleThreadExecutor();
    private final Handler mainHandler = new Handler(Looper.getMainLooper());

    public interface DataCallback<T> {
        void onSuccess(T data);
        void onError(String message);
    }

    public ScheduleRepository(Context context) {
        this.context = context.getApplicationContext();
        this.dbHelper = AppDatabaseHelper.getInstance(this.context);
    }

    public void getAllSchedules(long userId, DataCallback<List<WorkoutSchedule>> callback) {
        executor.execute(() -> {
            List<WorkoutSchedule> list = dbHelper.getAllSchedules(userId);
            mainHandler.post(() -> callback.onSuccess(list));

            SupabaseClient.getApi(context).getWorkoutSchedulesByUser("eq." + userId).enqueue(new Callback<List<WorkoutSchedule>>() {
                @Override
                public void onResponse(Call<List<WorkoutSchedule>> call, Response<List<WorkoutSchedule>> response) {
                    if (response.isSuccessful() && response.body() != null && !response.body().isEmpty()) {
                        mainHandler.post(() -> callback.onSuccess(response.body()));
                    }
                }

                @Override
                public void onFailure(Call<List<WorkoutSchedule>> call, Throwable t) {}
            });
        });
    }

    public void addSchedule(WorkoutSchedule schedule, DataCallback<Long> callback) {
        executor.execute(() -> {
            long localId = dbHelper.insertSchedule(schedule);
            mainHandler.post(() -> callback.onSuccess(localId));

            SupabaseClient.getApi(context).insertWorkoutSchedule(schedule).enqueue(new Callback<List<WorkoutSchedule>>() {
                @Override
                public void onResponse(Call<List<WorkoutSchedule>> call, Response<List<WorkoutSchedule>> response) {}
                @Override
                public void onFailure(Call<List<WorkoutSchedule>> call, Throwable t) {}
            });
        });
    }

    public void toggleScheduleCompleted(long id, boolean isCompleted, DataCallback<Boolean> callback) {
        executor.execute(() -> {
            dbHelper.updateScheduleCompleted(id, isCompleted);
            mainHandler.post(() -> callback.onSuccess(true));

            WorkoutSchedule updateObj = new WorkoutSchedule();
            updateObj.setCompleted(isCompleted);
            SupabaseClient.getApi(context).updateWorkoutSchedule("eq." + id, updateObj).enqueue(new Callback<List<WorkoutSchedule>>() {
                @Override
                public void onResponse(Call<List<WorkoutSchedule>> call, Response<List<WorkoutSchedule>> response) {}
                @Override
                public void onFailure(Call<List<WorkoutSchedule>> call, Throwable t) {}
            });
        });
    }

    public void deleteSchedule(long id, DataCallback<Boolean> callback) {
        executor.execute(() -> {
            dbHelper.deleteSchedule(id);
            mainHandler.post(() -> callback.onSuccess(true));

            SupabaseClient.getApi(context).deleteWorkoutSchedule("eq." + id).enqueue(new Callback<Void>() {
                @Override
                public void onResponse(Call<Void> call, Response<Void> response) {}
                @Override
                public void onFailure(Call<Void> call, Throwable t) {}
            });
        });
    }
}
