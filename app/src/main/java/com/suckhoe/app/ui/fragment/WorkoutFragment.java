package com.suckhoe.app.ui.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.cardview.widget.CardView;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.suckhoe.app.R;
import com.suckhoe.app.data.model.WorkoutExercise;
import com.suckhoe.app.data.repository.WorkoutRepository;
import com.suckhoe.app.ui.adapter.WorkoutAdapter;
import com.suckhoe.app.ui.dialog.AddWorkoutDialog;
import com.suckhoe.app.ui.dialog.RestTimerDialog;
import com.suckhoe.app.util.DateUtils;

import java.util.List;
import java.util.Locale;

public class WorkoutFragment extends Fragment {
    private SwipeRefreshLayout swipeRefresh;
    private CardView cardOpenRestTimer;
    private TextView tvTotalExercises;
    private TextView tvTotalDuration;
    private TextView tvTotalCalories;
    private RecyclerView rvWorkout;
    private TextView tvEmpty;
    private FloatingActionButton fabAdd;

    private WorkoutRepository repository;
    private WorkoutAdapter adapter;

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
        cardOpenRestTimer = view.findViewById(R.id.card_open_rest_timer);
        tvTotalExercises = view.findViewById(R.id.tv_workout_total_exercises);
        tvTotalDuration = view.findViewById(R.id.tv_workout_total_duration);
        tvTotalCalories = view.findViewById(R.id.tv_workout_total_calories);
        rvWorkout = view.findViewById(R.id.rv_workout);
        tvEmpty = view.findViewById(R.id.tv_empty_workout);
        fabAdd = view.findViewById(R.id.fab_add_workout);

        if (getContext() != null) {
            repository = new WorkoutRepository(getContext());
        }

        rvWorkout.setLayoutManager(new LinearLayoutManager(getContext()));
        adapter = new WorkoutAdapter(item -> {
            if (repository != null && item.getId() != null) {
                repository.deleteWorkout(item.getId(), new WorkoutRepository.DataCallback<Boolean>() {
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
        if (repository != null) {
            repository.getWorkoutsByDate(today, new WorkoutRepository.DataCallback<List<WorkoutExercise>>() {
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

    @Override
    public void onResume() {
        super.onResume();
        loadWorkouts();
    }
}
