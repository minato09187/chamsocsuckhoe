package com.suckhoe.app.ui.dialog;

import android.app.Dialog;
import android.content.Context;
import android.content.res.ColorStateList;
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
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;

import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.google.android.material.textfield.TextInputEditText;
import com.suckhoe.app.R;
import com.suckhoe.app.data.local.AppDatabaseHelper;
import com.suckhoe.app.data.model.WeightRecord;
import com.suckhoe.app.data.model.WorkoutExercise;
import com.suckhoe.app.data.model.WorkoutSchedule;
import com.suckhoe.app.util.DateUtils;
import com.suckhoe.app.util.PreferenceManager;
import com.suckhoe.app.util.WorkoutCalorieCalculator;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class AddWorkoutDialog extends Dialog {
    private LinearLayout layoutScheduleRec;
    private TextView tvScheduleRecTitle;
    private ChipGroup chipGroupRecMuscles;

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
    private String preferredMuscleGroup = null;

    private final OnWorkoutAddedListener listener;

    public interface OnWorkoutAddedListener {
        void onWorkoutAdded(WorkoutExercise exercise);
    }

    public AddWorkoutDialog(@NonNull Context context, OnWorkoutAddedListener listener) {
        this(context, null, listener);
    }

    public AddWorkoutDialog(@NonNull Context context, String preferredMuscleGroup, OnWorkoutAddedListener listener) {
        super(context);
        this.preferredMuscleGroup = preferredMuscleGroup;
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
        layoutScheduleRec = findViewById(R.id.layout_schedule_recommendation);
        tvScheduleRecTitle = findViewById(R.id.tv_schedule_rec_title);
        chipGroupRecMuscles = findViewById(R.id.chip_group_rec_muscles);

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

        // 1. Gán danh sách nhóm cơ vào Spinner (người dùng tự do chọn bất cứ nhóm cơ nào - không bắt buộc)
        ArrayAdapter<String> muscleAdapter = new ArrayAdapter<>(getContext(), android.R.layout.simple_spinner_dropdown_item, muscles);
        spinnerMuscle.setAdapter(muscleAdapter);

        // 2. Lắng nghe chọn nhóm cơ trên Spinner
        spinnerMuscle.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String selectedMuscle = muscles[position];
                updateExerciseSuggestions(selectedMuscle);
                recalculateCalories();

                // Đồng bộ trạng thái check của Chip đề cử nếu có
                if (chipGroupRecMuscles != null) {
                    for (int i = 0; i < chipGroupRecMuscles.getChildCount(); i++) {
                        View child = chipGroupRecMuscles.getChildAt(i);
                        if (child instanceof Chip) {
                            Chip chip = (Chip) child;
                            Object tag = chip.getTag();
                            if (tag instanceof String && ((String) tag).equalsIgnoreCase(selectedMuscle)) {
                                chip.setChecked(true);
                            } else {
                                chip.setChecked(false);
                            }
                        }
                    }
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });

        // 3. Khởi tạo đề cử theo lịch tập cố định hôm nay
        setupScheduleRecommendations();

        // 4. Khi người dùng bấm chọn một bài tập trong dropdown gợi ý
        etName.setOnItemClickListener((parent, view, position, id) -> recalculateCalories());

        // 5. Lắng nghe thay đổi các thông số: Số hiệp, Số lần, Mức tạ, Thời gian -> Tự động tính Calo tiêu hao
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

        // 6. Nút Lưu lại: Calo tiêu hao đã tự động tính sẵn
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
     * Dựa vào lịch tập cố định hôm nay của người dùng để đề cử nhóm cơ phù hợp
     */
    private void setupScheduleRecommendations() {
        try {
            long userId = new PreferenceManager(getContext()).getCurrentUserId();
            String todayDay = DateUtils.getDayOfWeekName(); // "Thứ 2", "Thứ 3", ...
            List<WorkoutSchedule> allSchedules = AppDatabaseHelper.getInstance(getContext()).getAllSchedules(userId);

            WorkoutSchedule todaySchedule = null;
            if (allSchedules != null) {
                for (WorkoutSchedule s : allSchedules) {
                    if (s.getDayOfWeek() != null && s.getDayOfWeek().equalsIgnoreCase(todayDay)) {
                        todaySchedule = s;
                        break;
                    }
                }
            }

            String initialSelectionMuscle = null;
            if (preferredMuscleGroup != null && !preferredMuscleGroup.isEmpty()) {
                initialSelectionMuscle = preferredMuscleGroup;
            }

            if (todaySchedule != null && todaySchedule.getTargetMuscle() != null && !todaySchedule.getTargetMuscle().isEmpty()) {
                layoutScheduleRec.setVisibility(View.VISIBLE);
                String sessionTitle = todaySchedule.getTitle() != null && !todaySchedule.getTitle().isEmpty()
                        ? todaySchedule.getTitle()
                        : "Lịch hôm nay";
                tvScheduleRecTitle.setText("💡 Đề cử theo lịch " + todayDay + " (" + sessionTitle + "):");

                chipGroupRecMuscles.removeAllViews();

                // Tách danh sách nhóm cơ được lên lịch (ví dụ: "Ngực, Tay sau")
                String[] parts = todaySchedule.getTargetMuscle().split("[,;/]");
                List<String> validRecMuscles = new ArrayList<>();

                for (String part : parts) {
                    String trimmed = part.trim();
                    if (trimmed.isEmpty()) continue;
                    int muscleIdx = findMuscleIndex(trimmed);
                    if (muscleIdx >= 0) {
                        String matchedName = muscles[muscleIdx];
                        if (!validRecMuscles.contains(matchedName)) {
                            validRecMuscles.add(matchedName);
                        }
                    }
                }

                if (!validRecMuscles.isEmpty()) {
                    if (initialSelectionMuscle == null) {
                        initialSelectionMuscle = validRecMuscles.get(0);
                    }

                    ColorStateList bgSelector = ContextCompat.getColorStateList(getContext(), R.color.chip_muscle_bg_selector);
                    ColorStateList textSelector = ContextCompat.getColorStateList(getContext(), R.color.chip_muscle_text_selector);
                    ColorStateList strokeSelector = ContextCompat.getColorStateList(getContext(), R.color.chip_muscle_stroke_selector);

                    for (String recMuscle : validRecMuscles) {
                        Chip chip = new Chip(getContext());
                        chip.setText("🔥 " + recMuscle);
                        chip.setTag(recMuscle);
                        chip.setCheckable(true);
                        chip.setClickable(true);
                        if (bgSelector != null) chip.setChipBackgroundColor(bgSelector);
                        if (textSelector != null) chip.setTextColor(textSelector);
                        if (strokeSelector != null) chip.setChipStrokeColor(strokeSelector);
                        chip.setChipStrokeWidth(2f);
                        chip.setChipCornerRadius(20f);
                        chip.setCheckedIconVisible(true);
                        chip.setCheckedIconTint(ContextCompat.getColorStateList(getContext(), R.color.white));

                        chip.setOnClickListener(v -> {
                            int targetIdx = findMuscleIndex(recMuscle);
                            if (targetIdx >= 0) {
                                spinnerMuscle.setSelection(targetIdx);
                            }
                        });

                        chipGroupRecMuscles.addView(chip);
                    }
                }
            } else {
                layoutScheduleRec.setVisibility(View.GONE);
            }

            // Chọn nhóm cơ ban đầu trên Spinner
            int selectIdx = 0;
            if (initialSelectionMuscle != null) {
                int matched = findMuscleIndex(initialSelectionMuscle);
                if (matched >= 0) selectIdx = matched;
            }
            spinnerMuscle.setSelection(selectIdx);
            updateExerciseSuggestions(muscles[selectIdx]);

        } catch (Exception e) {
            layoutScheduleRec.setVisibility(View.GONE);
            spinnerMuscle.setSelection(0);
            updateExerciseSuggestions(muscles[0]);
        }
    }

    /**
     * Tìm vị trí của nhóm cơ trong danh sách chuẩn
     */
    private int findMuscleIndex(String query) {
        if (query == null) return -1;
        String q = query.trim().toLowerCase(Locale.getDefault());
        for (int i = 0; i < muscles.length; i++) {
            String m = muscles[i].toLowerCase(Locale.getDefault());
            if (m.equals(q) || m.contains(q) || q.contains(m)) {
                return i;
            }
        }
        return -1;
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
