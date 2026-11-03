package com.suckhoe.app.data.repository;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;

import com.suckhoe.app.data.api.SupabaseClient;
import com.suckhoe.app.data.local.AppDatabaseHelper;
import com.suckhoe.app.data.model.WaterLog;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class WaterRepository {
    private final AppDatabaseHelper dbHelper;
    private final Context context;
    private final ExecutorService executor = Executors.newSingleThreadExecutor();
    private final Handler mainHandler = new Handler(Looper.getMainLooper());

    public interface DataCallback<T> {
        void onSuccess(T data);
        void onError(String message);
    }

    public WaterRepository(Context context) {
        this.context = context.getApplicationContext();
        this.dbHelper = AppDatabaseHelper.getInstance(this.context);
    }

    public void getWaterLog(long userId, String date, DataCallback<WaterLog> callback) {
        executor.execute(() -> {
            WaterLog log = dbHelper.getWaterLog(userId, date);
            mainHandler.post(() -> callback.onSuccess(log));

            SupabaseClient.getApi(context).getWaterLogByDateAndUser("eq." + userId, "eq." + date).enqueue(new Callback<List<WaterLog>>() {
                @Override
                public void onResponse(Call<List<WaterLog>> call, Response<List<WaterLog>> response) {
                    if (response.isSuccessful() && response.body() != null && !response.body().isEmpty()) {
                        mainHandler.post(() -> callback.onSuccess(response.body().get(0)));
                    }
                }

                @Override
                public void onFailure(Call<List<WaterLog>> call, Throwable t) {}
            });
        });
    }

    public void addWater(long userId, String date, int amountMl, int targetMl, DataCallback<WaterLog> callback) {
        executor.execute(() -> {
            dbHelper.updateWaterLog(userId, date, amountMl, targetMl);
            WaterLog updated = dbHelper.getWaterLog(userId, date);
            mainHandler.post(() -> callback.onSuccess(updated));

            if (updated != null) {
                SupabaseClient.getApi(context).insertWaterLog(updated).enqueue(new Callback<List<WaterLog>>() {
                    @Override
                    public void onResponse(Call<List<WaterLog>> call, Response<List<WaterLog>> response) {}
                    @Override
                    public void onFailure(Call<List<WaterLog>> call, Throwable t) {}
                });
            }
        });
    }
}
