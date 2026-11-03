package com.suckhoe.app.ui.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
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
import com.suckhoe.app.data.model.WorkoutSchedule;
import com.suckhoe.app.data.repository.ScheduleRepository;
import com.suckhoe.app.ui.adapter.ScheduleAdapter;
import com.suckhoe.app.ui.dialog.AddScheduleDialog;

import java.util.List;

public class ScheduleFragment extends Fragment {
    private SwipeRefreshLayout swipeRefresh;
    private RecyclerView rvSchedule;
    private TextView tvEmpty;
    private FloatingActionButton fabAdd;

    private ScheduleRepository repository;
    private ScheduleAdapter adapter;
    private com.suckhoe.app.util.PreferenceManager prefManager;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_schedule, container, false);
        initViews(view);
        setupListeners();
        loadSchedules();
        return view;
    }

    private void initViews(View view) {
        swipeRefresh = view.findViewById(R.id.swipe_refresh_schedule);
        rvSchedule = view.findViewById(R.id.rv_schedule);
        tvEmpty = view.findViewById(R.id.tv_empty_schedule);
        fabAdd = view.findViewById(R.id.fab_add_schedule);

        if (getContext() != null) {
            repository = new ScheduleRepository(getContext());
            prefManager = new com.suckhoe.app.util.PreferenceManager(getContext());
        }

        rvSchedule.setLayoutManager(new LinearLayoutManager(getContext()));
        adapter = new ScheduleAdapter(new ScheduleAdapter.OnScheduleActionListener() {
            @Override
            public void onToggleCompleted(WorkoutSchedule schedule, boolean isCompleted) {
                if (repository != null && schedule.getId() != null) {
                    repository.toggleScheduleCompleted(schedule.getId(), isCompleted, new ScheduleRepository.DataCallback<Boolean>() {
                        @Override
                        public void onSuccess(Boolean data) {}
                        @Override
                        public void onError(String message) {}
                    });
                }
            }

            @Override
            public void onDelete(WorkoutSchedule schedule) {
                if (repository != null && schedule.getId() != null) {
                    repository.deleteSchedule(schedule.getId(), new ScheduleRepository.DataCallback<Boolean>() {
                        @Override
                        public void onSuccess(Boolean data) {
                            Toast.makeText(getContext(), "Đã xóa lịch tập!", Toast.LENGTH_SHORT).show();
                            loadSchedules();
                        }

                        @Override
                        public void onError(String message) {}
                    });
                }
            }
        });
        rvSchedule.setAdapter(adapter);
    }

    private void setupListeners() {
        swipeRefresh.setColorSchemeResources(R.color.primary_green);
        swipeRefresh.setOnRefreshListener(this::loadSchedules);

        fabAdd.setOnClickListener(v -> {
            if (getContext() != null) {
                new AddScheduleDialog(getContext(), schedule -> {
                    repository.addSchedule(schedule, new ScheduleRepository.DataCallback<Long>() {
                        @Override
                        public void onSuccess(Long data) {
                            Toast.makeText(getContext(), "Đã thêm lịch tập!", Toast.LENGTH_SHORT).show();
                            loadSchedules();
                        }

                        @Override
                        public void onError(String message) {}
                    });
                }).show();
            }
        });
    }

    private void loadSchedules() {
        if (repository != null && prefManager != null) {
            long userId = prefManager.getCurrentUserId();
            repository.getAllSchedules(userId, new ScheduleRepository.DataCallback<List<WorkoutSchedule>>() {
                @Override
                public void onSuccess(List<WorkoutSchedule> data) {
                    if (swipeRefresh.isRefreshing()) swipeRefresh.setRefreshing(false);
                    adapter.setItems(data);

                    if (data == null || data.isEmpty()) {
                        tvEmpty.setVisibility(View.VISIBLE);
                    } else {
                        tvEmpty.setVisibility(View.GONE);
                    }
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
        loadSchedules();
    }
}

