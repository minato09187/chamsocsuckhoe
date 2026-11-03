package com.suckhoe.app.data.repository;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;

import com.suckhoe.app.data.api.SupabaseClient;
import com.suckhoe.app.data.local.AppDatabaseHelper;
import com.suckhoe.app.data.model.NutritionItem;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class NutritionRepository {
    private final AppDatabaseHelper dbHelper;
    private final Context context;
    private final ExecutorService executor = Executors.newSingleThreadExecutor();
    private final Handler mainHandler = new Handler(Looper.getMainLooper());

    public interface DataCallback<T> {
        void onSuccess(T data);
        void onError(String message);
    }

    public NutritionRepository(Context context) {
        this.context = context.getApplicationContext();
        this.dbHelper = AppDatabaseHelper.getInstance(this.context);
    }

    public void getNutritionByDate(long userId, String date, DataCallback<List<NutritionItem>> callback) {
        executor.execute(() -> {
            List<NutritionItem> localList = dbHelper.getNutritionByDate(userId, date);
            mainHandler.post(() -> callback.onSuccess(localList));

            SupabaseClient.getApi(context).getNutritionLogsByDateAndUser("eq." + userId, "eq." + date).enqueue(new Callback<List<NutritionItem>>() {
                @Override
                public void onResponse(Call<List<NutritionItem>> call, Response<List<NutritionItem>> response) {
                    if (response.isSuccessful() && response.body() != null && !response.body().isEmpty()) {
                        executor.execute(() -> {
                            List<NutritionItem> remoteList = response.body();
                            List<NutritionItem> currentLocal = dbHelper.getNutritionByDate(userId, date);
                            boolean changed = false;
                            for (NutritionItem remoteItem : remoteList) {
                                boolean exists = false;
                                for (NutritionItem localItem : currentLocal) {
                                    if (localItem.getFoodName() != null && localItem.getFoodName().equalsIgnoreCase(remoteItem.getFoodName())
                                            && localItem.getCalories() == remoteItem.getCalories()) {
                                        exists = true;
                                        break;
                                    }
                                }
                                if (!exists) {
                                    remoteItem.setUserId(userId);
                                    remoteItem.setLogDate(date);
                                    dbHelper.insertNutrition(remoteItem);
                                    changed = true;
                                }
                            }
                            if (changed) {
                                List<NutritionItem> updated = dbHelper.getNutritionByDate(userId, date);
                                mainHandler.post(() -> callback.onSuccess(updated));
                            }
                        });
                    }
                }

                @Override
                public void onFailure(Call<List<NutritionItem>> call, Throwable t) {}
            });
        });
    }

    public void addNutritionItem(NutritionItem item, DataCallback<Long> callback) {
        executor.execute(() -> {
            long localId = dbHelper.insertNutrition(item);
            mainHandler.post(() -> callback.onSuccess(localId));

            SupabaseClient.getApi(context).insertNutritionLog(item).enqueue(new Callback<List<NutritionItem>>() {
                @Override
                public void onResponse(Call<List<NutritionItem>> call, Response<List<NutritionItem>> response) {}
                @Override
                public void onFailure(Call<List<NutritionItem>> call, Throwable t) {}
            });
        });
    }

    public void deleteNutritionItem(long id, DataCallback<Boolean> callback) {
        executor.execute(() -> {
            dbHelper.deleteNutrition(id);
            mainHandler.post(() -> callback.onSuccess(true));

            SupabaseClient.getApi(context).deleteNutritionLog("eq." + id).enqueue(new Callback<Void>() {
                @Override
                public void onResponse(Call<Void> call, Response<Void> response) {}
                @Override
                public void onFailure(Call<Void> call, Throwable t) {}
            });
        });
    }
}
