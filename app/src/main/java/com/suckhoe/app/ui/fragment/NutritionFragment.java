package com.suckhoe.app.ui.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.suckhoe.app.R;
import com.suckhoe.app.data.model.NutritionItem;
import com.suckhoe.app.data.repository.NutritionRepository;
import com.suckhoe.app.ui.adapter.NutritionAdapter;
import com.suckhoe.app.ui.dialog.AddNutritionDialog;
import com.suckhoe.app.util.DateUtils;
import com.suckhoe.app.util.PreferenceManager;

import java.util.List;
import java.util.Locale;

public class NutritionFragment extends Fragment {
    private SwipeRefreshLayout swipeRefresh;
    private TextView tvTotalCal, tvGoalCal, tvRemCal;
    private ProgressBar pbCalories;
    private TextView tvMacroProtein, tvMacroCarbs, tvMacroFat;
    private RecyclerView rvNutrition;
    private TextView tvEmpty;
    private FloatingActionButton fabAdd;

    private NutritionRepository repository;
    private NutritionAdapter adapter;
    private PreferenceManager prefManager;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_nutrition, container, false);
        initViews(view);
        setupListeners();
        loadNutrition();
        return view;
    }

    private void initViews(View view) {
        swipeRefresh = view.findViewById(R.id.swipe_refresh_nutrition);
        tvTotalCal = view.findViewById(R.id.tv_nutrition_total_cal);
        tvGoalCal = view.findViewById(R.id.tv_nutrition_goal_cal);
        tvRemCal = view.findViewById(R.id.tv_nutrition_rem_cal);
        pbCalories = view.findViewById(R.id.pb_nutrition_calories);
        tvMacroProtein = view.findViewById(R.id.tv_macro_protein);
        tvMacroCarbs = view.findViewById(R.id.tv_macro_carbs);
        tvMacroFat = view.findViewById(R.id.tv_macro_fat);
        rvNutrition = view.findViewById(R.id.rv_nutrition);
        tvEmpty = view.findViewById(R.id.tv_empty_nutrition);
        fabAdd = view.findViewById(R.id.fab_add_nutrition);

        if (getContext() != null) {
            repository = new NutritionRepository(getContext());
            prefManager = new PreferenceManager(getContext());
        }

        rvNutrition.setLayoutManager(new LinearLayoutManager(getContext()));
        adapter = new NutritionAdapter(item -> {
            if (repository != null && item.getId() != null) {
                repository.deleteNutritionItem(item.getId(), new NutritionRepository.DataCallback<Boolean>() {
                    @Override
                    public void onSuccess(Boolean data) {
                        Toast.makeText(getContext(), "Đã xóa món ăn!", Toast.LENGTH_SHORT).show();
                        loadNutrition();
                    }

                    @Override
                    public void onError(String message) {}
                });
            }
        });
        rvNutrition.setAdapter(adapter);
    }

    private void setupListeners() {
        swipeRefresh.setColorSchemeResources(R.color.primary_green);
        swipeRefresh.setOnRefreshListener(this::loadNutrition);

        fabAdd.setOnClickListener(v -> {
            if (getContext() != null) {
                new AddNutritionDialog(getContext(), item -> {
                    repository.addNutritionItem(item, new NutritionRepository.DataCallback<Long>() {
                        @Override
                        public void onSuccess(Long data) {
                            Toast.makeText(getContext(), "Đã thêm món ăn!", Toast.LENGTH_SHORT).show();
                            loadNutrition();
                        }

                        @Override
                        public void onError(String message) {}
                    });
                }).show();
            }
        });
    }

    private void loadNutrition() {
        String today = DateUtils.getTodayIsoDate();
        int goal = prefManager != null ? prefManager.getDailyCalorieGoal() : 2000;
        tvGoalCal.setText(String.format(Locale.getDefault(), "Mục tiêu: %d kcal", goal));
        pbCalories.setMax(goal);

        if (repository != null && prefManager != null) {
            long userId = prefManager.getCurrentUserId();
            repository.getNutritionByDate(userId, today, new NutritionRepository.DataCallback<List<NutritionItem>>() {
                @Override
                public void onSuccess(List<NutritionItem> data) {
                    if (swipeRefresh.isRefreshing()) swipeRefresh.setRefreshing(false);
                    adapter.setItems(data);

                    if (data == null || data.isEmpty()) {
                        tvEmpty.setVisibility(View.VISIBLE);
                    } else {
                        tvEmpty.setVisibility(View.GONE);
                    }

                    int totalCal = 0;
                    double totalProtein = 0;
                    double totalCarbs = 0;
                    double totalFat = 0;

                    if (data != null) {
                        for (NutritionItem item : data) {
                            totalCal += item.getCalories();
                            totalProtein += item.getProtein();
                            totalCarbs += item.getCarbs();
                            totalFat += item.getFat();
                        }
                    }

                    tvTotalCal.setText(String.format(Locale.getDefault(), "%d kcal", totalCal));
                    pbCalories.setProgress(totalCal);

                    int remaining = Math.max(0, goal - totalCal);
                    tvRemCal.setText(String.format(Locale.getDefault(), "Còn lại %d kcal", remaining));

                    tvMacroProtein.setText(String.format(Locale.getDefault(), "%.1fg", totalProtein));
                    tvMacroCarbs.setText(String.format(Locale.getDefault(), "%.1fg", totalCarbs));
                    tvMacroFat.setText(String.format(Locale.getDefault(), "%.1fg", totalFat));
                }

                @Override
                public void onError(String message) {
                    if (swipeRefresh.isRefreshing()) swipeRefresh.setRefreshing(false);
                }
            });
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        loadNutrition();
    }
}

