package com.suckhoe.app.ui.fragment;

import android.app.AlertDialog;
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

import java.util.ArrayList;
import java.util.List;

public class ScheduleFragment extends Fragment {
    private SwipeRefreshLayout swipeRefresh;
    private RecyclerView rvSchedule;
    private TextView tvEmpty;
    private FloatingActionButton fabEdit;

    private ScheduleRepository repository;
    private ScheduleAdapter adapter;
    private com.suckhoe.app.util.PreferenceManager prefManager;
    private List<WorkoutSchedule> currentSchedules = new ArrayList<>();

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
        fabEdit = view.findViewById(R.id.fab_edit_schedule);

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
            public void onEdit(WorkoutSchedule schedule) {
                openEditScheduleDialog(schedule);
            }

            @Override
            public void onAddMore(WorkoutSchedule schedule) {
                openAddMoreForDayDialog(schedule.getDayOfWeek());
            }
        });
        rvSchedule.setAdapter(adapter);
    }

    private void setupListeners() {
        swipeRefresh.setColorSchemeResources(R.color.primary_green);
        swipeRefresh.setOnRefreshListener(this::loadSchedules);

        // Nút Sửa / Tùy chỉnh lịch tập trong tuần (thay cho nút thêm)
        fabEdit.setOnClickListener(v -> {
            if (getContext() == null) return;

            if (currentSchedules.isEmpty()) {
                openAddMoreForDayDialog(null);
                return;
            }

            // Hiển thị danh sách các buổi tập trong tuần để người dùng chọn tùy chỉnh
            String[] sessionOptions = new String[currentSchedules.size() + 1];
            for (int i = 0; i < currentSchedules.size(); i++) {
                WorkoutSchedule s = currentSchedules.get(i);
                sessionOptions[i] = "✏️ " + s.getDayOfWeek() + ": " + s.getTitle() + " (" + s.getTimeOfDay() + ")";
            }
            sessionOptions[currentSchedules.size()] = "➕ Thêm buổi tập cho ngày mới...";

            new AlertDialog.Builder(getContext())
                    .setTitle("Tùy chỉnh các bài tập trong tuần")
                    .setItems(sessionOptions, (dialog, which) -> {
                        if (which < currentSchedules.size()) {
                            openEditScheduleDialog(currentSchedules.get(which));
                        } else {
                            openAddMoreForDayDialog(null);
                        }
                    })
                    .setNegativeButton("Đóng", null)
                    .show();
        });
    }

    /**
     * Mở hộp thoại Sửa / Tùy chỉnh buổi tập hiện có
     */
    private void openEditScheduleDialog(WorkoutSchedule schedule) {
        if (getContext() == null || schedule == null) return;

        new AddScheduleDialog(getContext(), schedule, new AddScheduleDialog.OnScheduleActionListener() {
            @Override
            public void onScheduleSaved(WorkoutSchedule updatedSchedule, boolean isEditMode) {
                repository.updateSchedule(updatedSchedule, new ScheduleRepository.DataCallback<Boolean>() {
                    @Override
                    public void onSuccess(Boolean data) {
                        Toast.makeText(getContext(), "Đã cập nhật lịch tập!", Toast.LENGTH_SHORT).show();
                        loadSchedules();
                    }

                    @Override
                    public void onError(String message) {}
                });
            }

            @Override
            public void onScheduleDeleted(WorkoutSchedule deletedSchedule) {
                if (deletedSchedule.getId() != null) {
                    repository.deleteSchedule(deletedSchedule.getId(), new ScheduleRepository.DataCallback<Boolean>() {
                        @Override
                        public void onSuccess(Boolean data) {
                            Toast.makeText(getContext(), "Đã xóa buổi tập khỏi lịch!", Toast.LENGTH_SHORT).show();
                            loadSchedules();
                        }

                        @Override
                        public void onError(String message) {}
                    });
                }
            }
        }).show();
    }

    /**
     * Mở hộp thoại Bổ sung thêm buổi tập / bài tập / nhóm cơ (thời gian khác trong ngày)
     */
    private void openAddMoreForDayDialog(String preselectedDay) {
        if (getContext() == null) return;

        new AddScheduleDialog(getContext(), preselectedDay, new AddScheduleDialog.OnScheduleActionListener() {
            @Override
            public void onScheduleSaved(WorkoutSchedule newSchedule, boolean isEditMode) {
                repository.addSchedule(newSchedule, new ScheduleRepository.DataCallback<Long>() {
                    @Override
                    public void onSuccess(Long data) {
                        String msg = preselectedDay != null
                                ? "Đã bổ sung thêm buổi tập cho " + preselectedDay + "!"
                                : "Đã thêm lịch tập!";
                        Toast.makeText(getContext(), msg, Toast.LENGTH_SHORT).show();
                        loadSchedules();
                    }

                    @Override
                    public void onError(String message) {}
                });
            }
        }).show();
    }

    private void loadSchedules() {
        if (repository != null && prefManager != null) {
            long userId = prefManager.getCurrentUserId();
            repository.getAllSchedules(userId, new ScheduleRepository.DataCallback<List<WorkoutSchedule>>() {
                @Override
                public void onSuccess(List<WorkoutSchedule> data) {
                    if (swipeRefresh.isRefreshing()) swipeRefresh.setRefreshing(false);
                    currentSchedules = data != null ? data : new ArrayList<>();
                    adapter.setItems(currentSchedules);

                    if (currentSchedules.isEmpty()) {
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
