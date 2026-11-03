package com.suckhoe.app.ui.dialog;

import android.app.Dialog;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Window;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.google.android.material.textfield.TextInputEditText;
import com.suckhoe.app.R;
import com.suckhoe.app.data.model.NutritionItem;
import com.suckhoe.app.util.DateUtils;
import com.suckhoe.app.util.FoodNutritionPredictor;

import java.util.Locale;

public class AddNutritionDialog extends Dialog {
    private AutoCompleteTextView etFoodName;
    private Spinner spinnerMealType;
    private TextInputEditText etCalories;
    private TextInputEditText etProtein;
    private TextInputEditText etCarbs;
    private TextInputEditText etFat;
    private TextView tvAiStatus;
    private TextView tvAiSummary;
    private Button btnCancel;
    private Button btnSave;

    private final String[] mealTypes = new String[]{"Bữa sáng", "Bữa trưa", "Bữa tối", "Ăn vặt"};
    private boolean isAutoFilling = false;

    private final OnNutritionAddedListener listener;

    public interface OnNutritionAddedListener {
        void onNutritionAdded(NutritionItem item);
    }

    public AddNutritionDialog(@NonNull Context context, OnNutritionAddedListener listener) {
        super(context);
        this.listener = listener;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.dialog_add_nutrition);

        if (getWindow() != null) {
            getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        }

        initViews();
    }

    private void initViews() {
        etFoodName = findViewById(R.id.et_food_name);
        spinnerMealType = findViewById(R.id.spinner_meal_type);
        etCalories = findViewById(R.id.et_calories);
        etProtein = findViewById(R.id.et_protein);
        etCarbs = findViewById(R.id.et_carbs);
        etFat = findViewById(R.id.et_fat);
        tvAiStatus = findViewById(R.id.tv_ai_prediction_status);
        tvAiSummary = findViewById(R.id.tv_ai_prediction_summary);
        btnCancel = findViewById(R.id.btn_cancel_nutrition);
        btnSave = findViewById(R.id.btn_save_nutrition);

        // 1. Gán danh sách lựa chọn bữa ăn vào Spinner
        ArrayAdapter<String> mealAdapter = new ArrayAdapter<>(getContext(), android.R.layout.simple_spinner_dropdown_item, mealTypes);
        spinnerMealType.setAdapter(mealAdapter);

        // 2. Cung cấp danh mục gợi ý món ăn vào AutoCompleteTextView
        ArrayAdapter<String> foodSuggestionAdapter = new ArrayAdapter<>(
                getContext(),
                android.R.layout.simple_dropdown_item_1line,
                FoodNutritionPredictor.getAllFoodSuggestions()
        );
        etFoodName.setAdapter(foodSuggestionAdapter);

        // 3. Khi người dùng chọn một món trong danh sách gợi ý -> Tự động điền tức thì
        etFoodName.setOnItemClickListener((parent, view, position, id) -> {
            String selected = (String) parent.getItemAtPosition(position);
            triggerAiPrediction(selected);
        });

        // 4. Khi người dùng tự gõ phím -> Tự động nhận diện và tính toán Calo & Macro theo thời gian thực
        etFoodName.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (isAutoFilling) return;
                String text = (s != null) ? s.toString().trim() : "";
                if (text.length() >= 2) {
                    triggerAiPrediction(text);
                } else if (text.isEmpty()) {
                    if (tvAiStatus != null) tvAiStatus.setText("✨ AI tự động nhận diện dinh dưỡng");
                    if (tvAiSummary != null) tvAiSummary.setText("Gõ tên món để xem mức Calo & Macro ước tính");
                }
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });

        btnCancel.setOnClickListener(v -> dismiss());

        // 5. Nút Lưu lại: Chỉ cần gõ tên món là đã có đủ dữ liệu để lưu
        btnSave.setOnClickListener(v -> {
            String name = etFoodName.getText() != null ? etFoodName.getText().toString().trim() : "";
            if (name.isEmpty()) {
                Toast.makeText(getContext(), "Vui lòng nhập tên món ăn", Toast.LENGTH_SHORT).show();
                return;
            }

            // Nếu các ô calo/macro đang trống, chạy dự đoán tự động lần cuối
            int calories = 0;
            try {
                if (etCalories.getText() != null && !etCalories.getText().toString().trim().isEmpty()) {
                    calories = Integer.parseInt(etCalories.getText().toString().trim());
                }
            } catch (Exception ignored) {}

            double protein = 0;
            double carbs = 0;
            double fat = 0;
            try {
                if (etProtein.getText() != null && !etProtein.getText().toString().trim().isEmpty()) {
                    protein = Double.parseDouble(etProtein.getText().toString().trim());
                }
                if (etCarbs.getText() != null && !etCarbs.getText().toString().trim().isEmpty()) {
                    carbs = Double.parseDouble(etCarbs.getText().toString().trim());
                }
                if (etFat.getText() != null && !etFat.getText().toString().trim().isEmpty()) {
                    fat = Double.parseDouble(etFat.getText().toString().trim());
                }
            } catch (Exception ignored) {}

            // Nếu người dùng chỉ gõ tên và bấm Lưu ngay lập tức
            if (calories <= 0) {
                FoodNutritionPredictor.Prediction fallbackPred = FoodNutritionPredictor.predict(name);
                calories = fallbackPred.calories;
                if (protein <= 0) protein = fallbackPred.protein;
                if (carbs <= 0) carbs = fallbackPred.carbs;
                if (fat <= 0) fat = fallbackPred.fat;
            }

            String mealType = (spinnerMealType.getSelectedItem() != null)
                    ? spinnerMealType.getSelectedItem().toString()
                    : "Bữa trưa";

            long userId = new com.suckhoe.app.util.PreferenceManager(getContext()).getCurrentUserId();
            NutritionItem item = new NutritionItem(userId, name, calories, protein, carbs, fat, mealType, DateUtils.getTodayIsoDate());

            if (listener != null) {
                listener.onNutritionAdded(item);
            }
            dismiss();
        });
    }

    /**
     * Tự động tính toán và điền toàn bộ Calo, Đạm, Đường, Béo và Bữa ăn theo tên món
     */
    private void triggerAiPrediction(String foodName) {
        FoodNutritionPredictor.Prediction pred = FoodNutritionPredictor.predict(foodName);
        if (pred == null) return;

        isAutoFilling = true;

        if (etCalories != null) {
            etCalories.setText(String.valueOf(pred.calories));
        }
        if (etProtein != null) {
            etProtein.setText(String.format(Locale.getDefault(), "%.1f", pred.protein));
        }
        if (etCarbs != null) {
            etCarbs.setText(String.format(Locale.getDefault(), "%.1f", pred.carbs));
        }
        if (etFat != null) {
            etFat.setText(String.format(Locale.getDefault(), "%.1f", pred.fat));
        }

        // Tự động gán Bữa ăn phù hợp
        if (spinnerMealType != null && pred.mealType != null) {
            for (int i = 0; i < mealTypes.length; i++) {
                if (mealTypes[i].equalsIgnoreCase(pred.mealType)) {
                    spinnerMealType.setSelection(i);
                    break;
                }
            }
        }

        // Cập nhật thẻ hiển thị AI thông minh
        if (tvAiStatus != null) {
            tvAiStatus.setText("✨ " + pred.description);
        }
        if (tvAiSummary != null) {
            tvAiSummary.setText(String.format(Locale.getDefault(), "Ước tính: %d kcal | Đạm %.1fg | Carbs %.1fg | Béo %.1fg",
                    pred.calories, pred.protein, pred.carbs, pred.fat));
        }

        isAutoFilling = false;
    }
}
