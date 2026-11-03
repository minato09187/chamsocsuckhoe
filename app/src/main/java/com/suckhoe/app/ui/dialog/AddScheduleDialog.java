package com.suckhoe.app.ui.dialog;

import android.app.Dialog;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.Window;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.google.android.material.textfield.TextInputEditText;
import com.suckhoe.app.R;
import com.suckhoe.app.data.model.WorkoutSchedule;

public class AddScheduleDialog extends Dialog {
    private Spinner spinnerDay;
    private TextInputEditText etTitle;
    private TextInputEditText etMuscle;
    private TextInputEditText etTime;
    private TextInputEditText etNotes;
    private Button btnCancel;
    private Button btnSave;

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
        etMuscle = findViewById(R.id.et_schedule_muscle);
        etTime = findViewById(R.id.et_schedule_time);
        etNotes = findViewById(R.id.et_schedule_notes);
        btnCancel = findViewById(R.id.btn_cancel_schedule);
        btnSave = findViewById(R.id.btn_save_schedule);

        String[] days = new String[]{"Thứ 2", "Thứ 3", "Thứ 4", "Thứ 5", "Thứ 6", "Thứ 7", "Chủ Nhật"};
        ArrayAdapter<String> adapter = new ArrayAdapter<>(getContext(), android.R.layout.simple_spinner_dropdown_item, days);
        spinnerDay.setAdapter(adapter);

        btnCancel.setOnClickListener(v -> dismiss());

        btnSave.setOnClickListener(v -> {
            String title = etTitle.getText() != null ? etTitle.getText().toString().trim() : "";
            if (title.isEmpty()) {
                Toast.makeText(getContext(), "Vui lòng nhập tên buổi tập", Toast.LENGTH_SHORT).show();
                return;
            }

            String day = spinnerDay.getSelectedItem().toString();
            String muscle = etMuscle.getText() != null ? etMuscle.getText().toString().trim() : "Toàn thân";
            String time = etTime.getText() != null ? etTime.getText().toString().trim() : "17:30";
            String notes = etNotes.getText() != null ? etNotes.getText().toString().trim() : "";

            WorkoutSchedule schedule = new WorkoutSchedule(day, title, muscle, time, false, notes);

            if (listener != null) {
                listener.onScheduleAdded(schedule);
            }
            dismiss();
        });
    }
}

