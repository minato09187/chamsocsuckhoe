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
                    if (response.isSuccessful() && response.body() != null) {
                        executor.execute(() -> {
                            List<WorkoutSchedule> remoteList = response.body();
                            List<WorkoutSchedule> currentLocal = dbHelper.getAllSchedules(userId);
                            boolean changed = false;
                            for (WorkoutSchedule remoteItem : remoteList) {
                                boolean exists = false;
                                for (WorkoutSchedule localItem : currentLocal) {
                                    if (localItem.getId() != null && localItem.getId().equals(remoteItem.getId())) {
                                        exists = true;
                                        break;
                                    }
                                    if (localItem.getDayOfWeek() != null && localItem.getDayOfWeek().equalsIgnoreCase(remoteItem.getDayOfWeek())
                                            && localItem.getTitle() != null && localItem.getTitle().equalsIgnoreCase(remoteItem.getTitle())) {
                                        exists = true;
                                        if (remoteItem.getId() != null && !localItem.getId().equals(remoteItem.getId())) {
                                            dbHelper.updateScheduleId(localItem.getId(), remoteItem.getId());
                                        }
                                        break;
                                    }
                                }
                                if (!exists) {
                                    remoteItem.setUserId(userId);
                                    dbHelper.insertSchedule(remoteItem);
                                    changed = true;
                                }
                            }
                            if (changed) {
                                List<WorkoutSchedule> updated = dbHelper.getAllSchedules(userId);
                                mainHandler.post(() -> callback.onSuccess(updated));
                            }
                        });
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
            schedule.setId(localId);
            mainHandler.post(() -> callback.onSuccess(localId));

            // Chèn lên Supabase mà không ép id local để Supabase tự sinh ID chuẩn
            WorkoutSchedule remoteSchedule = new WorkoutSchedule(
                    schedule.getUserId(),
                    schedule.getDayOfWeek(),
                    schedule.getTitle(),
                    schedule.getTargetMuscle(),
                    schedule.getTimeOfDay(),
                    schedule.isCompleted(),
                    schedule.getNotes()
            );

            SupabaseClient.getApi(context).insertWorkoutSchedule(remoteSchedule).enqueue(new Callback<List<WorkoutSchedule>>() {
                @Override
                public void onResponse(Call<List<WorkoutSchedule>> call, Response<List<WorkoutSchedule>> response) {
                    if (response.isSuccessful() && response.body() != null && !response.body().isEmpty()) {
                        WorkoutSchedule remoteItem = response.body().get(0);
                        if (remoteItem.getId() != null) {
                            executor.execute(() -> {
                                dbHelper.updateScheduleId(localId, remoteItem.getId());
                                schedule.setId(remoteItem.getId());
                            });
                        }
                    }
                }

                @Override
                public void onFailure(Call<List<WorkoutSchedule>> call, Throwable t) {}
            });
        });
    }

    public void updateSchedule(WorkoutSchedule schedule, DataCallback<Boolean> callback) {
        executor.execute(() -> {
            dbHelper.updateSchedule(schedule);
            mainHandler.post(() -> callback.onSuccess(true));

            if (schedule.getId() != null) {
                SupabaseClient.getApi(context).updateWorkoutSchedule("eq." + schedule.getId(), schedule).enqueue(new Callback<List<WorkoutSchedule>>() {
                    @Override
                    public void onResponse(Call<List<WorkoutSchedule>> call, Response<List<WorkoutSchedule>> response) {}
                    @Override
                    public void onFailure(Call<List<WorkoutSchedule>> call, Throwable t) {}
                });
            }
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

    public void deleteSchedule(WorkoutSchedule schedule, DataCallback<Boolean> callback) {
        if (schedule == null) return;
        executor.execute(() -> {
            if (schedule.getId() != null) {
                dbHelper.deleteSchedule(schedule.getId());
            }
            mainHandler.post(() -> callback.onSuccess(true));

            // 1. Xóa ngay trên Supabase theo ID
            if (schedule.getId() != null) {
                SupabaseClient.getApi(context).deleteWorkoutSchedule("eq." + schedule.getId()).enqueue(new Callback<Void>() {
                    @Override
                    public void onResponse(Call<Void> call, Response<Void> response) {}
                    @Override
                    public void onFailure(Call<Void> call, Throwable t) {}
                });
            }

            // 2. Xóa đồng thời theo User ID + Thứ + Tiêu đề để đảm bảo xóa dứt điểm 100%
            if (schedule.getUserId() != null && schedule.getDayOfWeek() != null && schedule.getTitle() != null) {
                SupabaseClient.getApi(context).deleteWorkoutScheduleByDetails(
                        "eq." + schedule.getUserId(),
                        "eq." + schedule.getDayOfWeek(),
                        "eq." + schedule.getTitle()
                ).enqueue(new Callback<Void>() {
                    @Override
                    public void onResponse(Call<Void> call, Response<Void> response) {}
                    @Override
                    public void onFailure(Call<Void> call, Throwable t) {}
                });
            }
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
