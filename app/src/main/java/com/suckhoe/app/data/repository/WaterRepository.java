package com.suckhoe.app.data.repository;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;

import com.suckhoe.app.data.api.SupabaseClient;
import com.suckhoe.app.data.local.AppDatabaseHelper;
import com.suckhoe.app.data.model.WaterLog;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
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
            // 1. Lấy dữ liệu từ SQLite máy trước
            WaterLog local = dbHelper.getWaterLog(userId, date);
            mainHandler.post(() -> callback.onSuccess(local));

            // 2. Lấy dữ liệu từ Supabase Cloud để đối chiếu
            SupabaseClient.getApi(context).getWaterLogByDateAndUser("eq." + userId, "eq." + date).enqueue(new Callback<List<WaterLog>>() {
                @Override
                public void onResponse(Call<List<WaterLog>> call, Response<List<WaterLog>> response) {
                    if (response.isSuccessful() && response.body() != null && !response.body().isEmpty()) {
                        WaterLog remote = response.body().get(0);
                        int localAmount = (local != null) ? local.getAmountMl() : 0;
                        int remoteAmount = (remote.getAmountMl() != null) ? remote.getAmountMl() : 0;
                        int target = (local != null) ? local.getTargetMl() : (remote.getTargetMl() != null ? remote.getTargetMl() : 2000);

                        if (localAmount > remoteAmount) {
                            // Máy local có tiến trình mới hơn (ví dụ vừa thêm nước thành công) -> Đẩy local lên Supabase
                            syncToSupabase(userId, date, localAmount, target);
                        } else if (remoteAmount > localAmount) {
                            // Supabase có tiến trình lớn hơn -> Cập nhật vào SQLite local và cập nhật giao diện
                            executor.execute(() -> {
                                dbHelper.setWaterLogAmount(userId, date, remoteAmount, target);
                                WaterLog updated = dbHelper.getWaterLog(userId, date);
                                mainHandler.post(() -> callback.onSuccess(updated));
                            });
                        }
                    } else if (local != null) {
                        // Chưa có bản ghi trên Supabase -> đồng bộ bản ghi local lên
                        syncToSupabase(userId, date, local.getAmountMl(), local.getTargetMl());
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
                syncToSupabase(userId, date, updated.getAmountMl(), targetMl);
            }
        });
    }

    private void syncToSupabase(long userId, String date, int currentAmount, int targetMl) {
        Map<String, Object> fields = new HashMap<>();
        fields.put("amount_ml", currentAmount);
        fields.put("target_ml", targetMl);

        // 1. Thử cập nhật bản ghi đã có trên Supabase
        SupabaseClient.getApi(context).updateWaterLog("eq." + userId, "eq." + date, fields).enqueue(new Callback<List<WaterLog>>() {
            @Override
            public void onResponse(Call<List<WaterLog>> call, Response<List<WaterLog>> response) {
                if (!response.isSuccessful() || response.body() == null || response.body().isEmpty()) {
                    // Nếu chưa có trên Supabase -> chèn bản ghi mới (với id = null để Supabase tự sinh)
                    WaterLog newLog = new WaterLog(null, userId, currentAmount, targetMl, date);
                    SupabaseClient.getApi(context).insertWaterLog(newLog).enqueue(new Callback<List<WaterLog>>() {
                        @Override
                        public void onResponse(Call<List<WaterLog>> call, Response<List<WaterLog>> response) {}

                        @Override
                        public void onFailure(Call<List<WaterLog>> call, Throwable t) {}
                    });
                }
            }

            @Override
            public void onFailure(Call<List<WaterLog>> call, Throwable t) {}
        });
    }
}
