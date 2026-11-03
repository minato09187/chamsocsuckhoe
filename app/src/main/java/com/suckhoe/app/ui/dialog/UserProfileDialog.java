package com.suckhoe.app.ui.dialog;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;

import com.google.android.material.button.MaterialButton;
import com.suckhoe.app.R;
import com.suckhoe.app.ui.LoginActivity;
import com.suckhoe.app.util.PreferenceManager;

public class UserProfileDialog extends Dialog {
    private final PreferenceManager prefManager;

    public UserProfileDialog(@NonNull Context context) {
        super(context);
        this.prefManager = new PreferenceManager(context);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.dialog_user_profile);

        if (getWindow() != null) {
            getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        }

        TextView tvFullName = findViewById(R.id.tv_dialog_profile_fullname);
        TextView tvUsername = findViewById(R.id.tv_dialog_profile_username);
        TextView tvEmail = findViewById(R.id.tv_dialog_profile_email);
        MaterialButton btnSwitch = findViewById(R.id.btn_dialog_switch_user);
        MaterialButton btnLogout = findViewById(R.id.btn_dialog_logout);
        Button btnClose = findViewById(R.id.btn_dialog_profile_close);

        tvFullName.setText(prefManager.getCurrentUserFullName());
        tvUsername.setText("@" + prefManager.getCurrentUsername());
        String email = prefManager.getCurrentUserEmail();
        tvEmail.setText(email.isEmpty() ? "Chưa liên kết email" : email);

        btnSwitch.setOnClickListener(v -> {
            dismiss();
            prefManager.logout();
            Intent intent = new Intent(getContext(), LoginActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            getContext().startActivity(intent);
        });

        btnLogout.setOnClickListener(v -> {
            dismiss();
            prefManager.logout();
            Intent intent = new Intent(getContext(), LoginActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            getContext().startActivity(intent);
        });

        btnClose.setOnClickListener(v -> dismiss());
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (getWindow() != null) {
            DisplayMetrics metrics = getContext().getResources().getDisplayMetrics();
            int screenWidth = metrics.widthPixels;
            int desiredWidth = (int) (screenWidth * 0.85f);
            int minWidth = (int) (320 * metrics.density);
            int maxWidth = (int) (380 * metrics.density);
            int targetWidth = Math.max(minWidth, Math.min(desiredWidth, maxWidth));
            int finalWidth = Math.min(screenWidth - (int) (32 * metrics.density), targetWidth);
            getWindow().setLayout(finalWidth, ViewGroup.LayoutParams.WRAP_CONTENT);
        }
    }
}

