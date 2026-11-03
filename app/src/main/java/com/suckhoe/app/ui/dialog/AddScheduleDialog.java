package com.suckhoe.app.ui.dialog;

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
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.google.android.material.textfield.TextInputEditText;
import com.suckhoe.app.R;
import com.suckhoe.app.data.model.WorkoutSchedule;
import com.suckhoe.app.util.PreferenceManager;

import java.util.ArrayList;
import java.util.List;

public class AddScheduleDialog extends Dialog {
    private Spinner spinnerDay;
    private TextInputEditText etTitle;
    private ChipGroup chipGroupMuscles;
    private TextInputEditText etTime;
    private TextInputEditText etNotes;
    private Button btnCancel;
    private Button btnSave;

    private boolean userEditedTitle = false;
    private final OnScheduleAddedListener listener;

    public interface OnScheduleAddedListener {
        void onScheduleAdded(WorkoutSchedule schedule);
    }

    public AddScheduleDialog(@NonNull Context context, OnScheduleAddedListener listener) {
        super(context);
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
    }

    private void initViews() {
        spinnerDay = findViewById(R.id.spinner_schedule_day);
        etTitle = findViewById(R.id.et_schedule_title);
        chipGroupMuscles = findViewById(R.id.chip_group_schedule_muscles);
        etTime = findViewById(R.id.et_schedule_time);
        etNotes = findViewById(R.id.et_schedule_notes);
        btnCancel = findViewById(R.id.btn_cancel_schedule);
        btnSave = findViewById(R.id.btn_save_schedule);

        String[] days = new String[]{"Thứ 2", "Thứ 3", "Thứ 4", "Thứ 5", "Thứ 6", "Thứ 7", "Chủ Nhật"};
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

        btnSave.setOnClickListener(v -> {
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
            WorkoutSchedule schedule = new WorkoutSchedule(userId, day, title, muscle, time, false, notes);

            if (listener != null) {
                listener.onScheduleAdded(schedule);
            }
            dismiss();
        });
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
