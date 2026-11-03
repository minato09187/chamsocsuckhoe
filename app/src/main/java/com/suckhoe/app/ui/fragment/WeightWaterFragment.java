package com.suckhoe.app.ui.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.switchmaterial.SwitchMaterial;
import com.suckhoe.app.R;
import com.suckhoe.app.data.model.WaterLog;
import com.suckhoe.app.data.model.WeightRecord;
import com.suckhoe.app.data.repository.WaterRepository;
import com.suckhoe.app.data.repository.WeightRepository;
import com.suckhoe.app.ui.adapter.WeightAdapter;
import com.suckhoe.app.ui.dialog.AddWeightDialog;
import com.suckhoe.app.util.DateUtils;
import com.suckhoe.app.util.NotificationHelper;
import com.suckhoe.app.util.PreferenceManager;

import java.util.List;
import java.util.Locale;

public class WeightWaterFragment extends Fragment {
    private SwipeRefreshLayout swipeRefresh;

    // Water Views
    private TextView tvWaterPercent;
    private TextView tvWaterCurrentAmount;
    private TextView tvWaterTargetLabel;
    private ProgressBar pbWater;
    private Button btnWater150, btnWater250, btnWater500;
    private SwitchMaterial switchWaterReminder;

    // Weight Views
    private TextView tvCurrentWeight;
    private TextView tvCurrentBmi;
    private TextView tvCurrentBmiCategory;
    private MaterialButton btnOpenAddWeight;
    private RecyclerView rvWeightHistory;

    private WaterRepository waterRepo;
    private WeightRepository weightRepo;
    private WeightAdapter weightAdapter;
    private PreferenceManager prefManager;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_weight_water, container, false);
        initViews(view);
        setupListeners();
        loadAll();
        return view;
    }

    private void initViews(View view) {
        swipeRefresh = view.findViewById(R.id.swipe_refresh_weight_water);

        tvWaterPercent = view.findViewById(R.id.tv_water_percent);
        tvWaterCurrentAmount = view.findViewById(R.id.tv_water_current_amount);
        tvWaterTargetLabel = view.findViewById(R.id.tv_water_target_label);
        pbWater = view.findViewById(R.id.pb_water_progress);
        btnWater150 = view.findViewById(R.id.btn_water_add_150);
        btnWater250 = view.findViewById(R.id.btn_water_add_250);
        btnWater500 = view.findViewById(R.id.btn_water_add_500);
        switchWaterReminder = view.findViewById(R.id.switch_water_reminder);

        tvCurrentWeight = view.findViewById(R.id.tv_current_weight_val);
        tvCurrentBmi = view.findViewById(R.id.tv_current_bmi_val);
        tvCurrentBmiCategory = view.findViewById(R.id.tv_current_bmi_category);
        btnOpenAddWeight = view.findViewById(R.id.btn_open_add_weight);
        rvWeightHistory = view.findViewById(R.id.rv_weight_history);

        if (getContext() != null) {
            waterRepo = new WaterRepository(getContext());
            weightRepo = new WeightRepository(getContext());
            prefManager = new PreferenceManager(getContext());
        }

        rvWeightHistory.setLayoutManager(new LinearLayoutManager(getContext()));
        weightAdapter = new WeightAdapter(record -> {
            if (weightRepo != null && record.getId() != null) {
                weightRepo.deleteWeightRecord(record.getId(), new WeightRepository.DataCallback<Boolean>() {
                    @Override
                    public void onSuccess(Boolean data) {
                        Toast.makeText(getContext(), "Đã xóa bản ghi cân nặng!", Toast.LENGTH_SHORT).show();
                        loadWeightData();
                    }

                    @Override
                    public void onError(String message) {}
                });
            }
        });
        rvWeightHistory.setAdapter(weightAdapter);

        if (prefManager != null) {
            switchWaterReminder.setChecked(prefManager.isWaterReminderEnabled());
        }
    }

    private void setupListeners() {
        swipeRefresh.setColorSchemeResources(R.color.primary_green);
        swipeRefresh.setOnRefreshListener(this::loadAll);

        btnWater150.setOnClickListener(v -> addWaterIntake(150));
        btnWater250.setOnClickListener(v -> addWaterIntake(250));
        btnWater500.setOnClickListener(v -> addWaterIntake(500));

        switchWaterReminder.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (prefManager != null && getContext() != null) {
                prefManager.setWaterReminderEnabled(isChecked);
                if (isChecked) {
                    NotificationHelper.scheduleWaterReminder(getContext());
                    Toast.makeText(getContext(), "Đã bật nhắc nhở uống nước!", Toast.LENGTH_SHORT).show();
                } else {
                    NotificationHelper.cancelWaterReminder(getContext());
                    Toast.makeText(getContext(), "Đã tắt nhắc nhở uống nước", Toast.LENGTH_SHORT).show();
                }
            }
        });

        btnOpenAddWeight.setOnClickListener(v -> {
            if (getContext() != null) {
                new AddWeightDialog(getContext(), record -> {
                    weightRepo.addWeightRecord(record, new WeightRepository.DataCallback<Long>() {
                        @Override
                        public void onSuccess(Long data) {
                            Toast.makeText(getContext(), "Đã ghi nhận cân nặng mới!", Toast.LENGTH_SHORT).show();
                            loadWeightData();
                        }

                        @Override
                        public void onError(String message) {}
                    });
                }).show();
            }
        });
    }

    private void addWaterIntake(int amountMl) {
        String today = DateUtils.getTodayIsoDate();
        long userId = prefManager != null ? prefManager.getCurrentUserId() : 1L;
        int goal = prefManager != null ? prefManager.getDailyWaterGoal() : 2000;
        waterRepo.addWater(userId, today, amountMl, goal, new WaterRepository.DataCallback<WaterLog>() {
            @Override
            public void onSuccess(WaterLog data) {
                Toast.makeText(getContext(), "+" + amountMl + "ml nước đã được ghi nhận!", Toast.LENGTH_SHORT).show();
                updateWaterDisplay(data);
            }

            @Override
            public void onError(String message) {}
        });
    }

    private void loadAll() {
        loadWaterData();
        loadWeightData();
        if (swipeRefresh.isRefreshing()) {
            swipeRefresh.setRefreshing(false);
        }
    }

    private void loadWaterData() {
        String today = DateUtils.getTodayIsoDate();
        long userId = prefManager != null ? prefManager.getCurrentUserId() : 1L;
        int goal = prefManager != null ? prefManager.getDailyWaterGoal() : 2000;
        tvWaterTargetLabel.setText(String.format(Locale.getDefault(), "Mục tiêu: %d ml", goal));
        pbWater.setMax(goal);

        if (waterRepo != null) {
            waterRepo.getWaterLog(userId, today, new WaterRepository.DataCallback<WaterLog>() {
                @Override
                public void onSuccess(WaterLog data) {
                    updateWaterDisplay(data);
                }

                @Override
                public void onError(String message) {}
            });
        }
    }

    private void updateWaterDisplay(WaterLog log) {
        int goal = prefManager != null ? prefManager.getDailyWaterGoal() : 2000;
        int current = (log != null) ? log.getAmountMl() : 0;
        tvWaterCurrentAmount.setText(String.valueOf(current));
        pbWater.setProgress(current);

        int percent = (int) (((double) current / goal) * 100);
        tvWaterPercent.setText(percent + "%");
    }

    private void loadWeightData() {
        if (weightRepo != null && prefManager != null) {
            long userId = prefManager.getCurrentUserId();
            weightRepo.getAllWeights(userId, new WeightRepository.DataCallback<List<WeightRecord>>() {
                @Override
                public void onSuccess(List<WeightRecord> data) {
                    weightAdapter.setItems(data);
                    if (data != null && !data.isEmpty()) {
                        WeightRecord latest = data.get(0);
                        tvCurrentWeight.setText(String.format(Locale.getDefault(), "%.1f kg", latest.getWeight()));
                        tvCurrentBmi.setText(String.format(Locale.getDefault(), "%.1f", latest.getBmi()));
                        tvCurrentBmiCategory.setText(latest.getBmiCategory());
                    }
                }

                @Override
                public void onError(String message) {}
            });
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        loadAll();
    }
}

