package com.suckhoe.app.ui.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.cardview.widget.CardView;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.suckhoe.app.R;
import com.suckhoe.app.data.local.AppDatabaseHelper;
import com.suckhoe.app.data.model.WorkoutExercise;
import com.suckhoe.app.data.model.WorkoutSchedule;
import com.suckhoe.app.data.repository.WorkoutRepository;
import com.suckhoe.app.ui.adapter.WorkoutAdapter;
import com.suckhoe.app.ui.dialog.AddWorkoutDialog;
import com.suckhoe.app.ui.dialog.RestTimerDialog;
import com.suckhoe.app.util.DateUtils;

import java.util.List;
import java.util.Locale;

public class WorkoutFragment extends Fragment {
    private SwipeRefreshLayout swipeRefresh;
    private CardView cardTodaySchedule;
    private TextView tvTodayScheduleTitle;
    private TextView tvTodayScheduleTime;
    private TextView tvTodayScheduleMuscles;
    private MaterialButton btnTodayScheduleAction;

    private CardView cardOpenRestTimer;
    private TextView tvTotalExercises;
    private TextView tvTotalDuration;
    private TextView tvTotalCalories;
    private RecyclerView rvWorkout;
    private TextView tvEmpty;
    private FloatingActionButton fabAdd;

    private WorkoutRepository repository;
    private WorkoutAdapter adapter;
    private com.suckhoe.app.util.PreferenceManager prefManager;
    private String todayRecommendedMuscle = null;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_workout, container, false);
        initViews(view);
        setupListeners();
        loadWorkouts();
        return view;
    }

    private void initViews(View view) {
        swipeRefresh = view.findViewById(R.id.swipe_refresh_workout);
        cardTodaySchedule = view.findViewById(R.id.card_today_schedule);
        tvTodayScheduleTitle = view.findViewById(R.id.tv_today_schedule_title);
        tvTodayScheduleTime = view.findViewById(R.id.tv_today_schedule_time);
        tvTodayScheduleMuscles = view.findViewById(R.id.tv_today_schedule_muscles);
        btnTodayScheduleAction = view.findViewById(R.id.btn_today_schedule_action);

        cardOpenRestTimer = view.findViewById(R.id.card_open_rest_timer);
        tvTotalExercises = view.findViewById(R.id.tv_workout_total_exercises);
        tvTotalDuration = view.findViewById(R.id.tv_workout_total_duration);
        tvTotalCalories = view.findViewById(R.id.tv_workout_total_calories);
        rvWorkout = view.findViewById(R.id.rv_workout);
        tvEmpty = view.findViewById(R.id.tv_empty_workout);
        fabAdd = view.findViewById(R.id.fab_add_workout);

        if (getContext() != null) {
            repository = new WorkoutRepository(getContext());
            prefManager = new com.suckhoe.app.util.PreferenceManager(getContext());
        }

        rvWorkout.setLayoutManager(new LinearLayoutManager(getContext()));
        adapter = new WorkoutAdapter(item -> {
            if (repository != null && item != null) {
                repository.deleteWorkout(item, new WorkoutRepository.DataCallback<Boolean>() {
                    @Override
                    public void onSuccess(Boolean data) {
                        Toast.makeText(getContext(), "Đã xóa bài tập!", Toast.LENGTH_SHORT).show();
                        loadWorkouts();
                    }

                    @Override
                    public void onError(String message) {}
                });
            }
        });
        rvWorkout.setAdapter(adapter);
    }

    private void setupListeners() {
        swipeRefresh.setColorSchemeResources(R.color.primary_green);
        swipeRefresh.setOnRefreshListener(this::loadWorkouts);

        cardOpenRestTimer.setOnClickListener(v -> {
            if (getContext() != null) {
                new RestTimerDialog(getContext()).show();
            }
        });

        // Bắt đầu bài tập theo đề cử của lịch hôm nay
        btnTodayScheduleAction.setOnClickListener(v -> {
            if (getContext() != null) {
                new AddWorkoutDialog(getContext(), todayRecommendedMuscle, exercise -> {
                    repository.addWorkout(exercise, new WorkoutRepository.DataCallback<Long>() {
                        @Override
                        public void onSuccess(Long data) {
                            Toast.makeText(getContext(), "Đã thêm bài tập!", Toast.LENGTH_SHORT).show();
                            loadWorkouts();
                        }

                        @Override
                        public void onError(String message) {}
                    });
                }).show();
            }
        });

        fabAdd.setOnClickListener(v -> {
            if (getContext() != null) {
                new AddWorkoutDialog(getContext(), exercise -> {
                    repository.addWorkout(exercise, new WorkoutRepository.DataCallback<Long>() {
                        @Override
                        public void onSuccess(Long data) {
                            Toast.makeText(getContext(), "Đã thêm bài tập!", Toast.LENGTH_SHORT).show();
                            loadWorkouts();
                        }

                        @Override
                        public void onError(String message) {}
                    });
                }).show();
            }
        });
    }

    private void loadWorkouts() {
        String today = DateUtils.getTodayIsoDate();
        if (repository != null && prefManager != null) {
            long userId = prefManager.getCurrentUserId();

            // Kiểm tra lịch tập cố định hôm nay để hiển thị gợi ý
            checkTodaySchedule(userId);

            repository.getWorkoutsByDate(userId, today, new WorkoutRepository.DataCallback<List<WorkoutExercise>>() {
                @Override
                public void onSuccess(List<WorkoutExercise> data) {
                    if (swipeRefresh.isRefreshing()) swipeRefresh.setRefreshing(false);
                    adapter.setItems(data);

                    if (data == null || data.isEmpty()) {
                        tvEmpty.setVisibility(View.VISIBLE);
                    } else {
                        tvEmpty.setVisibility(View.GONE);
                    }

                    int totalExercises = data != null ? data.size() : 0;
                    int totalDuration = 0;
                    int totalCalories = 0;

                    if (data != null) {
                        for (WorkoutExercise ex : data) {
                            totalDuration += ex.getDurationMinutes();
                            totalCalories += ex.getCaloriesBurned();
                        }
                    }

                    tvTotalExercises.setText(String.valueOf(totalExercises));
                    tvTotalDuration.setText(String.format(Locale.getDefault(), "%d p", totalDuration));
                    tvTotalCalories.setText(String.valueOf(totalCalories));
                }

                @Override
                public void onError(String message) {
                    if (swipeRefresh.isRefreshing()) swipeRefresh.setRefreshing(false);
                }
            });
        }
    }

    /**
     * Kiểm tra lịch tập cố định của ngày hôm nay và hiển thị banner đề xuất
     */
    private void checkTodaySchedule(long userId) {
        if (getContext() == null || cardTodaySchedule == null) return;

        try {
            String todayDay = DateUtils.getDayOfWeekName(); // "Thứ 2", "Thứ 3", ...
            List<WorkoutSchedule> schedules = AppDatabaseHelper.getInstance(getContext()).getAllSchedules(userId);
            WorkoutSchedule todaySchedule = null;

            if (schedules != null) {
                for (WorkoutSchedule s : schedules) {
                    if (s.getDayOfWeek() != null && s.getDayOfWeek().equalsIgnoreCase(todayDay)) {
                        todaySchedule = s;
                        break;
                    }
                }
            }

            if (todaySchedule != null) {
                cardTodaySchedule.setVisibility(View.VISIBLE);
                String title = todaySchedule.getTitle() != null && !todaySchedule.getTitle().isEmpty()
                        ? todaySchedule.getTitle()
                        : "Lịch hôm nay";
                tvTodayScheduleTitle.setText("💡 Lịch hôm nay (" + todayDay + "): " + title);
                tvTodayScheduleTime.setText(todaySchedule.getTimeOfDay() != null ? todaySchedule.getTimeOfDay() : "");
                tvTodayScheduleMuscles.setText("Nhóm cơ mục tiêu: " + (todaySchedule.getTargetMuscle() != null ? todaySchedule.getTargetMuscle() : "Toàn thân"));

                if (todaySchedule.getTargetMuscle() != null && !todaySchedule.getTargetMuscle().isEmpty()) {
                    String[] parts = todaySchedule.getTargetMuscle().split("[,;/]");
                    todayRecommendedMuscle = parts[0].trim();
                } else {
                    todayRecommendedMuscle = null;
                }
            } else {
                cardTodaySchedule.setVisibility(View.GONE);
                todayRecommendedMuscle = null;
            }
        } catch (Exception e) {
            if (cardTodaySchedule != null) cardTodaySchedule.setVisibility(View.GONE);
            todayRecommendedMuscle = null;
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        loadWorkouts();
    }
}
