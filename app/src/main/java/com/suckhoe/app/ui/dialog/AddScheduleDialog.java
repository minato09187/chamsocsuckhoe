package com.suckhoe.app.ui.dialog;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.view.Window;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.google.android.material.textfield.TextInputEditText;
import com.suckhoe.app.R;
import com.suckhoe.app.data.model.WorkoutSchedule;
import com.suckhoe.app.util.PreferenceManager;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

public class AddScheduleDialog extends Dialog {
    private TextView tvTitleDialog;
    private TextView tvSubtitleDialog;
    private Spinner spinnerDay;
    private TextInputEditText etTitle;
    private ChipGroup chipGroupMuscles;
    private TextInputEditText etTime;
    private TextInputEditText etNotes;
    private Button btnDelete;
    private Button btnCancel;
    private Button btnSave;

    private final String[] days = new String[]{"Thứ 2", "Thứ 3", "Thứ 4", "Thứ 5", "Thứ 6", "Thứ 7", "Chủ Nhật"};
    private boolean userEditedTitle = false;
    private WorkoutSchedule scheduleToEdit = null;
    private String preselectedDay = null;
    private final OnScheduleActionListener listener;

    public interface OnScheduleActionListener {
        void onScheduleSaved(WorkoutSchedule schedule, boolean isEditMode);
        default void onScheduleDeleted(WorkoutSchedule schedule) {}
    }

    /**
     * Chế độ thêm mới thông thường
     */
    public AddScheduleDialog(@NonNull Context context, OnScheduleActionListener listener) {
        this(context, (String) null, listener);
    }

    /**
     * Chế độ bổ sung buổi tập cho một thứ cụ thể trong tuần (ví dụ: Thứ 2)
     */
    public AddScheduleDialog(@NonNull Context context, String preselectedDay, OnScheduleActionListener listener) {
        super(context);
        this.preselectedDay = preselectedDay;
        this.scheduleToEdit = null;
        this.listener = listener;
    }

    /**
     * Chế độ tùy chỉnh / sửa lịch tập đã có
     */
    public AddScheduleDialog(@NonNull Context context, WorkoutSchedule scheduleToEdit, OnScheduleActionListener listener) {
        super(context);
        this.scheduleToEdit = scheduleToEdit;
        this.preselectedDay = scheduleToEdit != null ? scheduleToEdit.getDayOfWeek() : null;
        this.listener = listener;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.dialog_add_schedule);

        if (getWindow() != null) {
            getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        }

        initViews();
        populateData();
    }

    private void initViews() {
        tvTitleDialog = findViewById(R.id.tv_dialog_schedule_title);
        tvSubtitleDialog = findViewById(R.id.tv_dialog_schedule_subtitle);
        spinnerDay = findViewById(R.id.spinner_schedule_day);
        etTitle = findViewById(R.id.et_schedule_title);
        chipGroupMuscles = findViewById(R.id.chip_group_schedule_muscles);
        etTime = findViewById(R.id.et_schedule_time);
        etNotes = findViewById(R.id.et_schedule_notes);
        btnDelete = findViewById(R.id.btn_delete_schedule_dialog);
        btnCancel = findViewById(R.id.btn_cancel_schedule);
        btnSave = findViewById(R.id.btn_save_schedule);

        ArrayAdapter<String> adapter = new ArrayAdapter<>(getContext(), android.R.layout.simple_spinner_dropdown_item, days);
        spinnerDay.setAdapter(adapter);

        // Theo dõi nếu người dùng tự gõ tiêu đề bằng tay thì không tự động ghi đè
        etTitle.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (etTitle.hasFocus()) {
                    userEditedTitle = true;
                }
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });

        // Lắng nghe tích chọn nhóm cơ để gợi ý tên buổi tập (nếu người dùng chưa tự đặt tên)
        for (int i = 0; i < chipGroupMuscles.getChildCount(); i++) {
            View child = chipGroupMuscles.getChildAt(i);
            if (child instanceof Chip) {
                Chip chip = (Chip) child;
                chip.setOnCheckedChangeListener((buttonView, isChecked) -> {
                    if (!userEditedTitle) {
                        List<String> selectedMuscles = getSelectedMuscles();
                        if (!selectedMuscles.isEmpty()) {
                            String suggestedTitle = "Tập " + String.join(", ", selectedMuscles);
                            etTitle.setText(suggestedTitle);
                        } else {
                            etTitle.setText("");
                        }
                    }
                });
            }
        }

        btnCancel.setOnClickListener(v -> dismiss());

        btnSave.setOnClickListener(v -> saveSchedule());
    }

    /**
     * Nạp dữ liệu ban đầu cho hộp thoại tùy theo chế độ Sửa (Edit) hay Bổ sung (Add)
     */
    private void populateData() {
        if (scheduleToEdit != null) {
            // 1. Chế độ SỬA / TÙY CHỈNH
            userEditedTitle = true; // Giữ nguyên tên người dùng đã lưu
            if (tvTitleDialog != null) tvTitleDialog.setText("Tùy chỉnh lịch tập");
            if (tvSubtitleDialog != null) {
                tvSubtitleDialog.setText("Chỉnh sửa buổi tập, bài tập và nhóm cơ (" + scheduleToEdit.getDayOfWeek() + ")");
            }
            if (btnSave != null) btnSave.setText("Lưu thay đổi");

            // Chọn thứ
            int dayPos = findDayPosition(scheduleToEdit.getDayOfWeek());
            if (dayPos >= 0) spinnerDay.setSelection(dayPos);

            // Điền tên buổi tập, giờ tập, ghi chú
            etTitle.setText(scheduleToEdit.getTitle() != null ? scheduleToEdit.getTitle() : "");
            etTime.setText(scheduleToEdit.getTimeOfDay() != null ? scheduleToEdit.getTimeOfDay() : "17:30");
            etNotes.setText(scheduleToEdit.getNotes() != null ? scheduleToEdit.getNotes() : "");

            // Tích chọn các nhóm cơ đã chọn trước đó
            String targetMuscles = scheduleToEdit.getTargetMuscle();
            if (targetMuscles != null && !targetMuscles.isEmpty()) {
                String[] parts = targetMuscles.split("[,;/]");
                List<String> muscleList = new ArrayList<>();
                for (String p : parts) muscleList.add(p.trim().toLowerCase(Locale.getDefault()));

                for (int i = 0; i < chipGroupMuscles.getChildCount(); i++) {
                    View child = chipGroupMuscles.getChildAt(i);
                    if (child instanceof Chip) {
                        Chip chip = (Chip) child;
                        String chipText = chip.getText().toString().trim().toLowerCase(Locale.getDefault());
                        for (String m : muscleList) {
                            if (chipText.contains(m) || m.contains(chipText)) {
                                chip.setChecked(true);
                                break;
                            }
                        }
                    }
                }
            }

            // Hiển thị nút xóa trong hộp thoại chỉnh sửa
            if (btnDelete != null) {
                btnDelete.setVisibility(View.VISIBLE);
                btnDelete.setOnClickListener(v -> {
                    new AlertDialog.Builder(getContext())
                            .setTitle("Xác nhận xóa")
                            .setMessage("Bạn có chắc chắn muốn xóa buổi tập này khỏi lịch?")
                            .setPositiveButton("Xóa", (dialog, which) -> {
                                if (listener != null) {
                                    listener.onScheduleDeleted(scheduleToEdit);
                                }
                                dismiss();
                            })
                            .setNegativeButton("Hủy", null)
                            .show();
                });
            }

        } else if (preselectedDay != null && !preselectedDay.isEmpty()) {
            // 2. Chế độ BỔ SUNG BUỔI TẬP cho một thứ cụ thể
            int dayPos = findDayPosition(preselectedDay);
            if (dayPos >= 0) spinnerDay.setSelection(dayPos);

            if (tvTitleDialog != null) tvTitleDialog.setText("Bổ sung buổi tập (" + preselectedDay + ")");
            if (tvSubtitleDialog != null) {
                tvSubtitleDialog.setText("Bổ sung thêm buổi tập, nhóm cơ vào thời gian khác trong ngày");
            }
            if (btnSave != null) btnSave.setText("Thêm vào lịch");
            if (btnDelete != null) btnDelete.setVisibility(View.GONE);

        } else {
            // 3. Chế độ thêm mới bình thường
            if (tvTitleDialog != null) tvTitleDialog.setText("Lập lịch tập luyện");
            if (tvSubtitleDialog != null) tvSubtitleDialog.setText("Lên kế hoạch rèn luyện kỷ luật cho từng ngày");
            if (btnSave != null) btnSave.setText("Lưu lịch tập");
            if (btnDelete != null) btnDelete.setVisibility(View.GONE);
        }
    }

    private void saveSchedule() {
        List<String> selectedMuscles = getSelectedMuscles();
        if (selectedMuscles.isEmpty()) {
            Toast.makeText(getContext(), "Vui lòng tích chọn ít nhất 1 nhóm cơ mục tiêu", Toast.LENGTH_SHORT).show();
            return;
        }

        String title = etTitle.getText() != null ? etTitle.getText().toString().trim() : "";
        if (title.isEmpty()) {
            title = "Tập " + String.join(", ", selectedMuscles);
        }

        String day = spinnerDay.getSelectedItem().toString();
        String muscle = String.join(", ", selectedMuscles);
        String time = (etTime.getText() != null && !etTime.getText().toString().trim().isEmpty())
                ? etTime.getText().toString().trim()
                : "17:30";
        String notes = etNotes.getText() != null ? etNotes.getText().toString().trim() : "";

        long userId = new PreferenceManager(getContext()).getCurrentUserId();

        if (scheduleToEdit != null) {
            // Cập nhật buổi tập hiện có
            scheduleToEdit.setDayOfWeek(day);
            scheduleToEdit.setTitle(title);
            scheduleToEdit.setTargetMuscle(muscle);
            scheduleToEdit.setTimeOfDay(time);
            scheduleToEdit.setNotes(notes);

            if (listener != null) {
                listener.onScheduleSaved(scheduleToEdit, true);
            }
        } else {
            // Tạo buổi tập mới
            WorkoutSchedule schedule = new WorkoutSchedule(userId, day, title, muscle, time, false, notes);
            if (listener != null) {
                listener.onScheduleSaved(schedule, false);
            }
        }
        dismiss();
    }

    private int findDayPosition(String dayName) {
        if (dayName == null) return 0;
        for (int i = 0; i < days.length; i++) {
            if (days[i].equalsIgnoreCase(dayName.trim())) return i;
        }
        return 0;
    }

    /**
     * Lấy danh sách các nhóm cơ được người dùng tích chọn
     */
    private List<String> getSelectedMuscles() {
        List<String> selected = new ArrayList<>();
        if (chipGroupMuscles == null) return selected;

        for (int i = 0; i < chipGroupMuscles.getChildCount(); i++) {
            View child = chipGroupMuscles.getChildAt(i);
            if (child instanceof Chip) {
                Chip chip = (Chip) child;
                if (chip.isChecked()) {
                    selected.add(chip.getText().toString().trim());
                }
            }
        }
        return selected;
    }
}
