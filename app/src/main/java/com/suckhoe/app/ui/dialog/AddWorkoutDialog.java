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
import com.suckhoe.app.data.model.WorkoutExercise;
import com.suckhoe.app.util.DateUtils;

public class AddWorkoutDialog extends Dialog {
    private TextInputEditText etName;
    private Spinner spinnerMuscle;
    private TextInputEditText etSets;
    private TextInputEditText etReps;
    private TextInputEditText etWeight;
    private TextInputEditText etDuration;
    private TextInputEditText etCalories;
    private Button btnCancel;
    private Button btnSave;

    private final OnWorkoutAddedListener listener;

    public interface OnWorkoutAddedListener {
        void onWorkoutAdded(WorkoutExercise exercise);
    }

    public AddWorkoutDialog(@NonNull Context context, OnWorkoutAddedListener listener) {
        super(context);
        this.listener = listener;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.dialog_add_workout);

        if (getWindow() != null) {
            getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        }

        initViews();
    }

    private void initViews() {
        etName = findViewById(R.id.et_exercise_name);
        spinnerMuscle = findViewById(R.id.spinner_muscle_group);
        etSets = findViewById(R.id.et_sets);
        etReps = findViewById(R.id.et_reps);
        etWeight = findViewById(R.id.et_weight_lifted);
        etDuration = findViewById(R.id.et_duration_mins);
        etCalories = findViewById(R.id.et_calories_burned);
        btnCancel = findViewById(R.id.btn_cancel_workout);
        btnSave = findViewById(R.id.btn_save_workout);

        String[] muscles = new String[]{"Ngực", "Lưng xô", "Chân", "Vai", "Tay trước", "Tay sau", "Bụng", "Toàn thân", "Cardio"};
        ArrayAdapter<String> adapter = new ArrayAdapter<>(getContext(), android.R.layout.simple_spinner_dropdown_item, muscles);
        spinnerMuscle.setAdapter(adapter);

        btnCancel.setOnClickListener(v -> dismiss());

        btnSave.setOnClickListener(v -> {
            String name = etName.getText() != null ? etName.getText().toString().trim() : "";
            if (name.isEmpty()) {
                Toast.makeText(getContext(), "Vui lòng nhập tên bài tập", Toast.LENGTH_SHORT).show();
                return;
            }

            int sets = 3;
            int reps = 12;
            double weight = 0;
            int duration = 15;
            int calories = 60;

            try {
                if (etSets.getText() != null) sets = Integer.parseInt(etSets.getText().toString().trim());
                if (etReps.getText() != null) reps = Integer.parseInt(etReps.getText().toString().trim());
                if (etWeight.getText() != null) weight = Double.parseDouble(etWeight.getText().toString().trim());
                if (etDuration.getText() != null) duration = Integer.parseInt(etDuration.getText().toString().trim());
                if (etCalories.getText() != null) calories = Integer.parseInt(etCalories.getText().toString().trim());
            } catch (Exception ignored) {}

            String muscle = spinnerMuscle.getSelectedItem().toString();
            WorkoutExercise exercise = new WorkoutExercise(name, muscle, sets, reps, weight, duration, calories, DateUtils.getTodayIsoDate());

            if (listener != null) {
                listener.onWorkoutAdded(exercise);
            }
            dismiss();
        });
    }
}
