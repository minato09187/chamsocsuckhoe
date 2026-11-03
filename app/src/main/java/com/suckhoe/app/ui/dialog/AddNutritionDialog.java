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
import com.suckhoe.app.data.model.NutritionItem;
import com.suckhoe.app.util.DateUtils;

public class AddNutritionDialog extends Dialog {
    private TextInputEditText etFoodName;
    private Spinner spinnerMealType;
    private TextInputEditText etCalories;
    private TextInputEditText etProtein;
    private TextInputEditText etCarbs;
    private TextInputEditText etFat;
    private Button btnCancel;
    private Button btnSave;

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
        btnCancel = findViewById(R.id.btn_cancel_nutrition);
        btnSave = findViewById(R.id.btn_save_nutrition);

        String[] mealTypes = new String[]{"Bữa sáng", "Bữa trưa", "Bữa tối", "Ăn vặt"};
        ArrayAdapter<String> adapter = new ArrayAdapter<>(getContext(), android.R.layout.simple_spinner_dropdown_item, mealTypes);
        spinnerMealType.setAdapter(adapter);

        btnCancel.setOnClickListener(v -> dismiss());

        btnSave.setOnClickListener(v -> {
            String name = etFoodName.getText() != null ? etFoodName.getText().toString().trim() : "";
            if (name.isEmpty()) {
                Toast.makeText(getContext(), "Vui lòng nhập tên món ăn", Toast.LENGTH_SHORT).show();
                return;
            }

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

            String mealType = spinnerMealType.getSelectedItem().toString();
            long userId = new com.suckhoe.app.util.PreferenceManager(getContext()).getCurrentUserId();
            NutritionItem item = new NutritionItem(userId, name, calories, protein, carbs, fat, mealType, DateUtils.getTodayIsoDate());

            if (listener != null) {
                listener.onNutritionAdded(item);
            }
            dismiss();
        });
    }
}

