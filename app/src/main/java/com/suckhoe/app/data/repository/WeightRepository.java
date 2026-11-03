package com.suckhoe.app.data.repository;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;

import com.suckhoe.app.data.api.SupabaseClient;
import com.suckhoe.app.data.local.AppDatabaseHelper;
import com.suckhoe.app.data.model.WeightRecord;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class WeightRepository {
    private final AppDatabaseHelper dbHelper;
    private final Context context;
    private final ExecutorService executor = Executors.newSingleThreadExecutor();
    private final Handler mainHandler = new Handler(Looper.getMainLooper());

    public interface DataCallback<T> {
        void onSuccess(T data);
        void onError(String message);
    }

    public WeightRepository(Context context) {
        this.context = context.getApplicationContext();
        this.dbHelper = AppDatabaseHelper.getInstance(this.context);
    }

    public void getAllWeights(long userId, DataCallback<List<WeightRecord>> callback) {
        executor.execute(() -> {
            List<WeightRecord> list = dbHelper.getAllWeights(userId);
            mainHandler.post(() -> callback.onSuccess(list));

            SupabaseClient.getApi(context).getWeightLogsByUser("eq." + userId).enqueue(new Callback<List<WeightRecord>>() {
                @Override
                public void onResponse(Call<List<WeightRecord>> call, Response<List<WeightRecord>> response) {
                    if (response.isSuccessful() && response.body() != null && !response.body().isEmpty()) {
                        mainHandler.post(() -> callback.onSuccess(response.body()));
                    }
                }

                @Override
                public void onFailure(Call<List<WeightRecord>> call, Throwable t) {}
            });
        });
    }

    public void addWeightRecord(WeightRecord record, DataCallback<Long> callback) {
        executor.execute(() -> {
            long localId = dbHelper.insertWeight(record);
            mainHandler.post(() -> callback.onSuccess(localId));

            SupabaseClient.getApi(context).insertWeightLog(record).enqueue(new Callback<List<WeightRecord>>() {
                @Override
                public void onResponse(Call<List<WeightRecord>> call, Response<List<WeightRecord>> response) {}
                @Override
                public void onFailure(Call<List<WeightRecord>> call, Throwable t) {}
            });
        });
    }

    public void deleteWeightRecord(long id, DataCallback<Boolean> callback) {
        executor.execute(() -> {
            dbHelper.deleteWeight(id);
            mainHandler.post(() -> callback.onSuccess(true));

            SupabaseClient.getApi(context).deleteWeightLog("eq." + id).enqueue(new Callback<Void>() {
                @Override
                public void onResponse(Call<Void> call, Response<Void> response) {}
                @Override
                public void onFailure(Call<Void> call, Throwable t) {}
            });
        });
    }
}
