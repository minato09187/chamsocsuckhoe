package com.suckhoe.app.ui.dialog;

import android.app.Dialog;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.Window;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.google.android.material.textfield.TextInputEditText;
import com.suckhoe.app.R;
import com.suckhoe.app.util.PreferenceManager;

public class SupabaseConfigDialog extends Dialog {
    private TextInputEditText etUrl;
    private TextInputEditText etKey;
    private Button btnCancel;
    private Button btnSave;

    private final PreferenceManager preferenceManager;
    private final OnConfigSavedListener listener;

    public interface OnConfigSavedListener {
        void onConfigSaved();
    }

    public SupabaseConfigDialog(@NonNull Context context, OnConfigSavedListener listener) {
        super(context);
        this.preferenceManager = new PreferenceManager(context);
        this.listener = listener;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.dialog_supabase_config);

        if (getWindow() != null) {
            getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        }

        etUrl = findViewById(R.id.et_supabase_url);
        etKey = findViewById(R.id.et_supabase_key);
        btnCancel = findViewById(R.id.btn_cancel_config);
        btnSave = findViewById(R.id.btn_save_config);

        etUrl.setText(preferenceManager.getSupabaseUrl());
        etKey.setText(preferenceManager.getSupabaseKey());

        btnCancel.setOnClickListener(v -> dismiss());

        btnSave.setOnClickListener(v -> {
            String url = etUrl.getText() != null ? etUrl.getText().toString().trim() : "";
            String key = etKey.getText() != null ? etKey.getText().toString().trim() : "";

            if (url.isEmpty()) {
                Toast.makeText(getContext(), "Vui lòng nhập Supabase URL", Toast.LENGTH_SHORT).show();
                return;
            }

            preferenceManager.setSupabaseUrl(url);
            preferenceManager.setSupabaseKey(key);

            Toast.makeText(getContext(), "Đã lưu cấu hình Supabase!", Toast.LENGTH_SHORT).show();
            if (listener != null) {
                listener.onConfigSaved();
            }
            dismiss();
        });
    }
}

