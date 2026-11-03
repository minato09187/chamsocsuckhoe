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
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.google.android.material.textfield.TextInputEditText;
import com.suckhoe.app.R;
import com.suckhoe.app.data.local.AppDatabaseHelper;
import com.suckhoe.app.data.model.WeightRecord;
import com.suckhoe.app.data.model.WorkoutExercise;
import com.suckhoe.app.util.DateUtils;
import com.suckhoe.app.util.PreferenceManager;
import com.suckhoe.app.util.WorkoutCalorieCalculator;

import java.util.List;
import java.util.Locale;

public class AddWorkoutDialog extends Dialog {
    private AutoCompleteTextView etName;
    private Spinner spinnerMuscle;
    private TextInputEditText etSets;
    private TextInputEditText etReps;
    private TextInputEditText etWeight;
    private TextInputEditText etDuration;
    private TextInputEditText etCalories;
    private TextView tvCalcStatus;
    private TextView tvCalcSummary;
    private Button btnCancel;
    private Button btnSave;

    private final String[] muscles = new String[]{"Ngực", "Lưng xô", "Chân", "Vai", "Tay trước", "Tay sau", "Bụng", "Cardio", "Toàn thân"};
    private double userWeightKg = 68.0;
    private boolean isCalculating = false;

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

        loadUserWeight();
        initViews();
    }

    private void loadUserWeight() {
        try {
            long userId = new PreferenceManager(getContext()).getCurrentUserId();
            List<WeightRecord> records = AppDatabaseHelper.getInstance(getContext()).getAllWeights(userId);
            if (records != null && !records.isEmpty()) {
                userWeightKg = records.get(0).getWeight();
            }
        } catch (Exception ignored) {}
    }

    private void initViews() {
        spinnerMuscle = findViewById(R.id.spinner_muscle_group);
        etName = findViewById(R.id.et_exercise_name);
        etSets = findViewById(R.id.et_sets);
        etReps = findViewById(R.id.et_reps);
        etWeight = findViewById(R.id.et_weight_lifted);
        etDuration = findViewById(R.id.et_duration_mins);
        etCalories = findViewById(R.id.et_calories_burned);
        tvCalcStatus = findViewById(R.id.tv_workout_calc_status);
        tvCalcSummary = findViewById(R.id.tv_workout_calc_summary);
        btnCancel = findViewById(R.id.btn_cancel_workout);
        btnSave = findViewById(R.id.btn_save_workout);

        // 1. Gán danh sách nhóm cơ vào Spinner
        ArrayAdapter<String> muscleAdapter = new ArrayAdapter<>(getContext(), android.R.layout.simple_spinner_dropdown_item, muscles);
        spinnerMuscle.setAdapter(muscleAdapter);

        // 2. Lắng nghe chọn nhóm cơ -> Thay đổi danh sách gợi ý bài tập phù hợp nhóm cơ đó
        spinnerMuscle.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String selectedMuscle = muscles[position];
                updateExerciseSuggestions(selectedMuscle);
                recalculateCalories();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });

        // Khởi tạo danh sách bài tập cho nhóm cơ đầu tiên
        updateExerciseSuggestions(muscles[0]);

        // 3. Khi người dùng bấm chọn một bài tập trong dropdown gợi ý
        etName.setOnItemClickListener((parent, view, position, id) -> recalculateCalories());

        // 4. Lắng nghe thay đổi các thông số: Số hiệp, Số lần, Mức tạ, Thời gian -> Tự động tính Calo tiêu hao
        TextWatcher autoCalculateWatcher = new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                recalculateCalories();
            }

            @Override
            public void afterTextChanged(Editable s) {}
        };

        etName.addTextChangedListener(autoCalculateWatcher);
        etSets.addTextChangedListener(autoCalculateWatcher);
        etReps.addTextChangedListener(autoCalculateWatcher);
        etWeight.addTextChangedListener(autoCalculateWatcher);
        etDuration.addTextChangedListener(autoCalculateWatcher);

        // Tính toán ban đầu
        recalculateCalories();

        btnCancel.setOnClickListener(v -> dismiss());

        // 5. Nút Lưu lại: Calo tiêu hao đã tự động tính sẵn
        btnSave.setOnClickListener(v -> {
            String name = etName.getText() != null ? etName.getText().toString().trim() : "";
            if (name.isEmpty()) {
                Toast.makeText(getContext(), "Vui lòng nhập hoặc chọn tên bài tập", Toast.LENGTH_SHORT).show();
                return;
            }

            int sets = 3;
            int reps = 12;
            double weight = 0;
            int duration = 15;
            int calories = 80;

            try {
                if (etSets.getText() != null && !etSets.getText().toString().trim().isEmpty()) {
                    sets = Integer.parseInt(etSets.getText().toString().trim());
                }
                if (etReps.getText() != null && !etReps.getText().toString().trim().isEmpty()) {
                    reps = Integer.parseInt(etReps.getText().toString().trim());
                }
                if (etWeight.getText() != null && !etWeight.getText().toString().trim().isEmpty()) {
                    weight = Double.parseDouble(etWeight.getText().toString().trim());
                }
                if (etDuration.getText() != null && !etDuration.getText().toString().trim().isEmpty()) {
                    duration = Integer.parseInt(etDuration.getText().toString().trim());
                }
                if (etCalories.getText() != null && !etCalories.getText().toString().trim().isEmpty()) {
                    calories = Integer.parseInt(etCalories.getText().toString().trim());
                }
            } catch (Exception ignored) {}

            String muscle = (spinnerMuscle.getSelectedItem() != null)
                    ? spinnerMuscle.getSelectedItem().toString()
                    : "Ngực";

            if (calories <= 0) {
                calories = WorkoutCalorieCalculator.calculateCaloriesBurned(name, muscle, sets, reps, weight, duration, userWeightKg);
            }

            long userId = new PreferenceManager(getContext()).getCurrentUserId();
            WorkoutExercise exercise = new WorkoutExercise(userId, name, muscle, sets, reps, weight, duration, calories, DateUtils.getTodayIsoDate());

            if (listener != null) {
                listener.onWorkoutAdded(exercise);
            }
            dismiss();
        });
    }

    /**
     * Cập nhật danh sách gợi ý bài tập tương ứng với nhóm cơ đã chọn
     */
    private void updateExerciseSuggestions(String muscleGroup) {
        List<String> exercises = WorkoutCalorieCalculator.getExercisesForMuscle(muscleGroup);
        ArrayAdapter<String> exerciseAdapter = new ArrayAdapter<>(
                getContext(),
                android.R.layout.simple_dropdown_item_1line,
                exercises
        );
        etName.setAdapter(exerciseAdapter);

        // Tự động điền bài tập tiêu biểu nếu ô trống
        if (etName.getText() == null || etName.getText().toString().trim().isEmpty()) {
            if (!exercises.isEmpty()) {
                etName.setText(exercises.get(0), false);
            }
        }
    }

    /**
     * Tự động tính toán lượng Calo tiêu hao dựa trên các thông số đã nhập
     */
    private void recalculateCalories() {
        if (isCalculating) return;
        isCalculating = true;

        String name = etName.getText() != null ? etName.getText().toString().trim() : "";
        String muscle = (spinnerMuscle.getSelectedItem() != null)
                ? spinnerMuscle.getSelectedItem().toString()
                : "Ngực";

        int sets = 3;
        int reps = 12;
        double weight = 0;
        int duration = 15;

        try {
            if (etSets != null && etSets.getText() != null && !etSets.getText().toString().trim().isEmpty()) {
                sets = Integer.parseInt(etSets.getText().toString().trim());
            }
            if (etReps != null && etReps.getText() != null && !etReps.getText().toString().trim().isEmpty()) {
                reps = Integer.parseInt(etReps.getText().toString().trim());
            }
            if (etWeight != null && etWeight.getText() != null && !etWeight.getText().toString().trim().isEmpty()) {
                weight = Double.parseDouble(etWeight.getText().toString().trim());
            }
            if (etDuration != null && etDuration.getText() != null && !etDuration.getText().toString().trim().isEmpty()) {
                duration = Integer.parseInt(etDuration.getText().toString().trim());
            }
        } catch (Exception ignored) {}

        int burned = WorkoutCalorieCalculator.calculateCaloriesBurned(name, muscle, sets, reps, weight, duration, userWeightKg);

        if (etCalories != null) {
            etCalories.setText(String.valueOf(burned));
        }

        if (tvCalcStatus != null) {
            tvCalcStatus.setText(String.format(Locale.getDefault(), "🔥 Tự động tính: ~%d kcal tiêu hao", burned));
        }

        if (tvCalcSummary != null) {
            if (muscle.equalsIgnoreCase("Cardio")) {
                tvCalcSummary.setText(String.format(Locale.getDefault(), "Cardio cường độ cao: đốt ~%d kcal trong %d phút", burned, duration));
            } else if (weight > 0) {
                double totalVol = sets * reps * weight;
                tvCalcSummary.setText(String.format(Locale.getDefault(), "Volume: %.0fkg tạ (%d hiệp x %d rep) trong %d phút", totalVol, sets, reps, duration));
            } else {
                tvCalcSummary.setText(String.format(Locale.getDefault(), "Bodyweight (%d hiệp x %d rep) trong %d phút", sets, reps, duration));
            }
        }

        isCalculating = false;
    }
}
