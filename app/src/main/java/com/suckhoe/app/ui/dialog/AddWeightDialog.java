package com.suckhoe.app.ui.dialog;

import android.app.Dialog;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Window;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.google.android.material.textfield.TextInputEditText;
import com.suckhoe.app.R;
import com.suckhoe.app.data.model.WeightRecord;
import com.suckhoe.app.util.DateUtils;
import com.suckhoe.app.util.PreferenceManager;

public class AddWeightDialog extends Dialog {
    private TextInputEditText etWeight;
    private TextInputEditText etHeight;
    private TextInputEditText etNote;
    private TextView tvBmiPreview;
    private Button btnCancel;
    private Button btnSave;

    private final PreferenceManager preferenceManager;
    private final OnWeightAddedListener listener;

    public interface OnWeightAddedListener {
        void onWeightAdded(WeightRecord record);
    }

    public AddWeightDialog(@NonNull Context context, OnWeightAddedListener listener) {
        super(context);
        this.listener = listener;
        this.preferenceManager = new PreferenceManager(context);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.dialog_add_weight);

        if (getWindow() != null) {
            getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        }

        initViews();
    }

    private void initViews() {
        etWeight = findViewById(R.id.et_weight);
        etHeight = findViewById(R.id.et_height);
        etNote = findViewById(R.id.et_weight_note);
        tvBmiPreview = findViewById(R.id.tv_bmi_preview);
        btnCancel = findViewById(R.id.btn_cancel_weight);
        btnSave = findViewById(R.id.btn_save_weight);

        float defaultHeight = preferenceManager.getUserHeight();
        etHeight.setText(String.valueOf(defaultHeight));

        TextWatcher watcher = new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                updateBmiCalculation();
            }
            @Override
            public void afterTextChanged(Editable s) {}
        };

        etWeight.addTextChangedListener(watcher);
        etHeight.addTextChangedListener(watcher);

        btnCancel.setOnClickListener(v -> dismiss());

        btnSave.setOnClickListener(v -> {
            String wStr = etWeight.getText() != null ? etWeight.getText().toString().trim() : "";
            String hStr = etHeight.getText() != null ? etHeight.getText().toString().trim() : "";

            if (wStr.isEmpty()) {
                Toast.makeText(getContext(), "Vui lòng nhập cân nặng (kg)", Toast.LENGTH_SHORT).show();
                return;
            }

            double weight = Double.parseDouble(wStr);
            double height = hStr.isEmpty() ? defaultHeight : Double.parseDouble(hStr);
            preferenceManager.setUserHeight((float) height);

            double bmi = WeightRecord.calculateBmi(weight, height);
            String note = etNote.getText() != null ? etNote.getText().toString().trim() : "";

            WeightRecord record = new WeightRecord(weight, height, bmi, note, DateUtils.getTodayIsoDate());

            if (listener != null) {
                listener.onWeightAdded(record);
            }
            dismiss();
        });
    }

    private void updateBmiCalculation() {
        try {
            String wStr = etWeight.getText() != null ? etWeight.getText().toString().trim() : "";
            String hStr = etHeight.getText() != null ? etHeight.getText().toString().trim() : "";
            if (!wStr.isEmpty() && !hStr.isEmpty()) {
                double w = Double.parseDouble(wStr);
                double h = Double.parseDouble(hStr);
                double bmi = WeightRecord.calculateBmi(w, h);
                WeightRecord temp = new WeightRecord();
                temp.setBmi(bmi);
                tvBmiPreview.setText(bmi + " - " + temp.getBmiCategory());
            } else {
                tvBmiPreview.setText("--");
            }
        } catch (Exception e) {
            tvBmiPreview.setText("--");
        }
    }
}
