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

    public void getWorkoutsByDate(long userId, String date, DataCallback<List<WorkoutExercise>> callback) {
        executor.execute(() -> {
            List<WorkoutExercise> list = dbHelper.getWorkoutsByDate(userId, date);
            mainHandler.post(() -> callback.onSuccess(list));

            SupabaseClient.getApi(context).getWorkoutLogsByDateAndUser("eq." + userId, "eq." + date).enqueue(new Callback<List<WorkoutExercise>>() {
                @Override
                public void onResponse(Call<List<WorkoutExercise>> call, Response<List<WorkoutExercise>> response) {
                    if (response.isSuccessful() && response.body() != null) {
                        executor.execute(() -> {
                            List<WorkoutExercise> remoteList = response.body();
                            List<WorkoutExercise> currentLocal = dbHelper.getWorkoutsByDate(userId, date);
                            boolean changed = false;
                            for (WorkoutExercise remoteItem : remoteList) {
                                boolean exists = false;
                                for (WorkoutExercise localItem : currentLocal) {
                                    if (localItem.getId() != null && localItem.getId().equals(remoteItem.getId())) {
                                        exists = true;
                                        break;
                                    }
                                    if (localItem.getExerciseName() != null && localItem.getExerciseName().equalsIgnoreCase(remoteItem.getExerciseName())
                                            && localItem.getSets() == remoteItem.getSets()
                                            && Math.abs(localItem.getWeight() - remoteItem.getWeight()) < 0.001) {
                                        exists = true;
                                        if (remoteItem.getId() != null && !localItem.getId().equals(remoteItem.getId())) {
                                            dbHelper.updateWorkoutId(localItem.getId(), remoteItem.getId());
                                        }
                                        break;
                                    }
                                }
                                if (!exists) {
                                    remoteItem.setUserId(userId);
                                    remoteItem.setLogDate(date);
                                    dbHelper.insertWorkout(remoteItem);
                                    changed = true;
                                }
                            }
                            if (changed) {
                                List<WorkoutExercise> updated = dbHelper.getWorkoutsByDate(userId, date);
                                mainHandler.post(() -> callback.onSuccess(updated));
                            }
                        });
                    }
                }

                @Override
                public void onFailure(Call<List<WorkoutExercise>> call, Throwable t) {}
            });
        });
    }

    public void addWorkout(WorkoutExercise exercise, DataCallback<Long> callback) {
        executor.execute(() -> {
            long localId = dbHelper.insertWorkout(exercise);
            exercise.setId(localId);
            mainHandler.post(() -> callback.onSuccess(localId));

            // Chèn lên Supabase không gửi kèm ID local để Supabase tự sinh
            WorkoutExercise remoteExercise = new WorkoutExercise(
                    exercise.getUserId(),
                    exercise.getExerciseName(),
                    exercise.getMuscleGroup(),
                    exercise.getSets(),
                    exercise.getReps(),
                    exercise.getWeight(),
                    exercise.getDurationMinutes(),
                    exercise.getCaloriesBurned(),
                    exercise.getLogDate()
            );

            SupabaseClient.getApi(context).insertWorkoutLog(remoteExercise).enqueue(new Callback<List<WorkoutExercise>>() {
                @Override
                public void onResponse(Call<List<WorkoutExercise>> call, Response<List<WorkoutExercise>> response) {
                    if (response.isSuccessful() && response.body() != null && !response.body().isEmpty()) {
                        WorkoutExercise created = response.body().get(0);
                        if (created.getId() != null) {
                            executor.execute(() -> {
                                dbHelper.updateWorkoutId(localId, created.getId());
                                exercise.setId(created.getId());
                            });
                        }
                    }
                }

                @Override
                public void onFailure(Call<List<WorkoutExercise>> call, Throwable t) {}
            });
        });
    }

    public void deleteWorkout(WorkoutExercise exercise, DataCallback<Boolean> callback) {
        if (exercise == null) return;
        executor.execute(() -> {
            if (exercise.getId() != null) {
                dbHelper.deleteWorkout(exercise.getId());
            }
            mainHandler.post(() -> callback.onSuccess(true));

            // 1. Xóa trên Supabase theo ID
            if (exercise.getId() != null) {
                SupabaseClient.getApi(context).deleteWorkoutLog("eq." + exercise.getId()).enqueue(new Callback<Void>() {
                    @Override
                    public void onResponse(Call<Void> call, Response<Void> response) {}
                    @Override
                    public void onFailure(Call<Void> call, Throwable t) {}
                });
            }

            // 2. Xóa đồng thời theo User ID + Tên bài tập + Ngày ghi để dứt điểm hoàn toàn
            if (exercise.getUserId() != null && exercise.getExerciseName() != null && exercise.getLogDate() != null) {
                SupabaseClient.getApi(context).deleteWorkoutLogByDetails(
                        "eq." + exercise.getUserId(),
                        "eq." + exercise.getExerciseName(),
                        "eq." + exercise.getLogDate()
                ).enqueue(new Callback<Void>() {
                    @Override
                    public void onResponse(Call<Void> call, Response<Void> response) {}
                    @Override
                    public void onFailure(Call<Void> call, Throwable t) {}
                });
            }
        });
    }

    public void deleteWorkout(long id, DataCallback<Boolean> callback) {
        executor.execute(() -> {
            dbHelper.deleteWorkout(id);
            mainHandler.post(() -> callback.onSuccess(true));

            SupabaseClient.getApi(context).deleteWorkoutLog("eq." + id).enqueue(new Callback<Void>() {
                @Override
                public void onResponse(Call<Void> call, Response<Void> response) {}
                @Override
                public void onFailure(Call<Void> call, Throwable t) {}
            });
        });
    }
}
