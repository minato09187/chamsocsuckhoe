package com.suckhoe.app.ui.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.google.android.material.button.MaterialButton;
import com.suckhoe.app.MainActivity;
import com.suckhoe.app.R;
import com.suckhoe.app.data.api.SupabaseClient;
import com.suckhoe.app.data.model.NutritionItem;
import com.suckhoe.app.data.model.WaterLog;
import com.suckhoe.app.data.model.WeightRecord;
import com.suckhoe.app.data.model.WorkoutExercise;
import com.suckhoe.app.data.repository.NutritionRepository;
import com.suckhoe.app.data.repository.WaterRepository;
import com.suckhoe.app.data.repository.WeightRepository;
import com.suckhoe.app.data.repository.WorkoutRepository;
import com.suckhoe.app.ui.dialog.AddNutritionDialog;
import com.suckhoe.app.ui.dialog.RestTimerDialog;
import com.suckhoe.app.ui.dialog.SupabaseConfigDialog;
import com.suckhoe.app.util.DateUtils;
import com.suckhoe.app.util.PreferenceManager;

import java.util.List;
import java.util.Locale;

public class HomeFragment extends Fragment {
    private SwipeRefreshLayout swipeRefresh;
    private TextView tvTodayDate;
    private TextView tvCloudStatus;
    private ImageView btnRestTimerHeader;

    // Calorie Views
    private TextView tvCalorieGoal;
    private TextView tvCaloriesConsumed;
    private TextView tvCaloriesRemaining;
    private ProgressBar pbCalories;

    // Water Views
    private TextView tvWaterIntake;
    private TextView tvWaterTarget;
    private ProgressBar pbWater;
    private Button btnQuickWater;

    // Weight Views
    private TextView tvWeight;
    private TextView tvBmi;

    // Workout Views
    private TextView tvWorkoutCount;
    private TextView tvCaloriesBurned;

    // Quick Action Buttons
    private MaterialButton btnOpenTimer;
    private MaterialButton btnAddMeal;

    private NutritionRepository nutritionRepo;
    private WaterRepository waterRepo;
    private WeightRepository weightRepo;
    private WorkoutRepository workoutRepo;
    private PreferenceManager prefManager;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home, container, false);
        initViews(view);
        initRepos();
        setupListeners();
        loadAllData();
        return view;
    }

    private void initViews(View view) {
        swipeRefresh = view.findViewById(R.id.swipe_refresh_home);
        tvTodayDate = view.findViewById(R.id.tv_today_date);
        tvCloudStatus = view.findViewById(R.id.tv_cloud_status);
        btnRestTimerHeader = view.findViewById(R.id.btn_home_rest_timer);

        tvCalorieGoal = view.findViewById(R.id.tv_home_calorie_goal);
        tvCaloriesConsumed = view.findViewById(R.id.tv_home_calories_consumed);
        tvCaloriesRemaining = view.findViewById(R.id.tv_home_calories_remaining);
        pbCalories = view.findViewById(R.id.pb_home_calories);

        tvWaterIntake = view.findViewById(R.id.tv_home_water_intake);
        tvWaterTarget = view.findViewById(R.id.tv_home_water_target);
        pbWater = view.findViewById(R.id.pb_home_water);
        btnQuickWater = view.findViewById(R.id.btn_home_quick_water);

        tvWeight = view.findViewById(R.id.tv_home_weight);
        tvBmi = view.findViewById(R.id.tv_home_bmi);

        tvWorkoutCount = view.findViewById(R.id.tv_home_workout_count);
        tvCaloriesBurned = view.findViewById(R.id.tv_home_calories_burned);

        btnOpenTimer = view.findViewById(R.id.btn_home_open_timer);
        btnAddMeal = view.findViewById(R.id.btn_home_add_meal);

        String dateText = DateUtils.getDayOfWeekName() + ", " + DateUtils.formatDisplayDate(DateUtils.getTodayIsoDate());
        tvTodayDate.setText(dateText);
    }

    private void initRepos() {
        if (getContext() == null) return;
        nutritionRepo = new NutritionRepository(getContext());
        waterRepo = new WaterRepository(getContext());
        weightRepo = new WeightRepository(getContext());
        workoutRepo = new WorkoutRepository(getContext());
        prefManager = new PreferenceManager(getContext());
    }

    private void setupListeners() {
        swipeRefresh.setColorSchemeResources(R.color.primary_green);
        swipeRefresh.setOnRefreshListener(this::loadAllData);

        View.OnClickListener openTimerListener = v -> {
            if (getContext() != null) {
                new RestTimerDialog(getContext()).show();
            }
        };

        btnRestTimerHeader.setOnClickListener(openTimerListener);
        btnOpenTimer.setOnClickListener(openTimerListener);

        tvCloudStatus.setOnClickListener(v -> {
            if (getContext() != null) {
                new SupabaseConfigDialog(getContext(), this::loadAllData).show();
            }
        });

        btnAddMeal.setOnClickListener(v -> {
            if (getContext() != null) {
                new AddNutritionDialog(getContext(), item -> {
                    nutritionRepo.addNutritionItem(item, new NutritionRepository.DataCallback<Long>() {
                        @Override
                        public void onSuccess(Long data) {
                            Toast.makeText(getContext(), "Đã thêm món ăn!", Toast.LENGTH_SHORT).show();
                            loadNutritionData();
                        }

                        @Override
                        public void onError(String message) {}
                    });
                }).show();
            }
        });

        btnQuickWater.setOnClickListener(v -> {
            String today = DateUtils.getTodayIsoDate();
            int goal = prefManager.getDailyWaterGoal();
            waterRepo.addWater(today, 250, goal, new WaterRepository.DataCallback<WaterLog>() {
                @Override
                public void onSuccess(WaterLog data) {
                    Toast.makeText(getContext(), "+250ml nước đã uống!", Toast.LENGTH_SHORT).show();
                    updateWaterView(data);
                }

                @Override
                public void onError(String message) {}
            });
        });
    }

    public void loadAllData() {
        updateCloudStatus();
        loadNutritionData();
        loadWaterData();
        loadWeightData();
        loadWorkoutData();
        if (swipeRefresh.isRefreshing()) {
            swipeRefresh.setRefreshing(false);
        }
    }

    private void updateCloudStatus() {
        if (getContext() == null) return;
        if (SupabaseClient.hasValidApiKey(getContext())) {
            tvCloudStatus.setText("☁ Supabase Cloud: Đã kết nối");
        } else {
            tvCloudStatus.setText("☁ Supabase Cloud: Chưa cấu hình Anon Key (Chạm để cài đặt)");
        }
    }

    private void loadNutritionData() {
        String today = DateUtils.getTodayIsoDate();
        int goal = prefManager.getDailyCalorieGoal();
        tvCalorieGoal.setText(String.format(Locale.getDefault(), "Mục tiêu: %d kcal", goal));
        pbCalories.setMax(goal);

        nutritionRepo.getNutritionByDate(today, new NutritionRepository.DataCallback<List<NutritionItem>>() {
            @Override
            public void onSuccess(List<NutritionItem> data) {
                int consumed = 0;
                for (NutritionItem item : data) {
                    consumed += item.getCalories();
                }
                tvCaloriesConsumed.setText(String.valueOf(consumed));
                pbCalories.setProgress(consumed);
                int remaining = Math.max(0, goal - consumed);
                tvCaloriesRemaining.setText(String.format(Locale.getDefault(), "Còn %d kcal", remaining));
            }

            @Override
            public void onError(String message) {}
        });
    }

    private void loadWaterData() {
        String today = DateUtils.getTodayIsoDate();
        int goal = prefManager.getDailyWaterGoal();
        tvWaterTarget.setText(String.format(Locale.getDefault(), "Mục tiêu: %d ml", goal));
        pbWater.setMax(goal);

        waterRepo.getWaterLog(today, new WaterRepository.DataCallback<WaterLog>() {
            @Override
            public void onSuccess(WaterLog data) {
                updateWaterView(data);
            }

            @Override
            public void onError(String message) {}
        });
    }

    private void updateWaterView(WaterLog data) {
        int goal = prefManager.getDailyWaterGoal();
        int amount = (data != null) ? data.getAmountMl() : 0;
        tvWaterIntake.setText(String.format(Locale.getDefault(), "%d ml", amount));
        pbWater.setProgress(amount);
    }

    private void loadWeightData() {
        weightRepo.getAllWeights(new WeightRepository.DataCallback<List<WeightRecord>>() {
            @Override
            public void onSuccess(List<WeightRecord> data) {
                if (data != null && !data.isEmpty()) {
                    WeightRecord latest = data.get(0);
                    tvWeight.setText(String.format(Locale.getDefault(), "%.1f kg", latest.getWeight()));
                    tvBmi.setText(String.format(Locale.getDefault(), "BMI: %.1f (%s)", latest.getBmi(), latest.getBmiCategory()));
                }
            }

            @Override
            public void onError(String message) {}
        });
    }

    private void loadWorkoutData() {
        String today = DateUtils.getTodayIsoDate();
        workoutRepo.getWorkoutsByDate(today, new WorkoutRepository.DataCallback<List<WorkoutExercise>>() {
            @Override
            public void onSuccess(List<WorkoutExercise> data) {
                int count = data != null ? data.size() : 0;
                int calories = 0;
                if (data != null) {
                    for (WorkoutExercise item : data) {
                        calories += item.getCaloriesBurned();
                    }
                }
                tvWorkoutCount.setText(String.format(Locale.getDefault(), "%d bài tập đã hoàn thành", count));
                tvCaloriesBurned.setText(String.format(Locale.getDefault(), "Đốt: %d kcal", calories));
            }

            @Override
            public void onError(String message) {}
        });
    }

    @Override
    public void onResume() {
        super.onResume();
        loadAllData();
    }
}

