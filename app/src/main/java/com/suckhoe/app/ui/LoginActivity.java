package com.suckhoe.app.ui;

import android.content.Intent;
import android.content.res.ColorStateList;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.suckhoe.app.MainActivity;
import com.suckhoe.app.R;
import com.suckhoe.app.data.api.SupabaseClient;
import com.suckhoe.app.data.local.AppDatabaseHelper;
import com.suckhoe.app.data.model.User;
import com.suckhoe.app.util.PreferenceManager;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class LoginActivity extends AppCompatActivity {
    private MaterialButton btnTabLogin;
    private MaterialButton btnTabRegister;
    private LinearLayout layoutLoginForm;
    private LinearLayout layoutRegisterForm;

    // Login inputs
    private TextInputEditText etLoginUsername;
    private TextInputEditText etLoginPassword;
    private MaterialButton btnSubmitLogin;

    // Register inputs
    private TextInputEditText etRegFullName;
    private TextInputEditText etRegUsername;
    private TextInputEditText etRegEmail;
    private TextInputEditText etRegPassword;
    private MaterialButton btnSubmitRegister;

    // Quick user switcher
    private LinearLayout layoutQuickUsers;

    private AppDatabaseHelper dbHelper;
    private PreferenceManager prefManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        dbHelper = AppDatabaseHelper.getInstance(this);
        prefManager = new PreferenceManager(this);

        initViews();
        setupTabs();
        setupActions();
        loadQuickUsers();
    }

    private void initViews() {
        btnTabLogin = findViewById(R.id.btn_tab_login);
        btnTabRegister = findViewById(R.id.btn_tab_register);
        layoutLoginForm = findViewById(R.id.layout_login_form);
        layoutRegisterForm = findViewById(R.id.layout_register_form);

        etLoginUsername = findViewById(R.id.et_login_username);
        etLoginPassword = findViewById(R.id.et_login_password);
        btnSubmitLogin = findViewById(R.id.btn_submit_login);

        etRegFullName = findViewById(R.id.et_reg_fullname);
        etRegUsername = findViewById(R.id.et_reg_username);
        etRegEmail = findViewById(R.id.et_reg_email);
        etRegPassword = findViewById(R.id.et_reg_password);
        btnSubmitRegister = findViewById(R.id.btn_submit_register);

        layoutQuickUsers = findViewById(R.id.layout_quick_users);
    }

    private void setupTabs() {
        btnTabLogin.setOnClickListener(v -> showLoginForm());
        btnTabRegister.setOnClickListener(v -> showRegisterForm());
    }

    private void setTabActive(MaterialButton btn) {
        btn.setBackgroundTintList(ColorStateList.valueOf(ContextCompat.getColor(this, R.color.primary_green)));
        btn.setStrokeColor(ColorStateList.valueOf(ContextCompat.getColor(this, R.color.primary_green)));
        btn.setStrokeWidth((int) (1 * getResources().getDisplayMetrics().density));
        btn.setTextColor(ContextCompat.getColor(this, R.color.white));
    }

    private void setTabInactive(MaterialButton btn) {
        btn.setBackgroundTintList(ColorStateList.valueOf(ContextCompat.getColor(this, R.color.white)));
        btn.setStrokeColor(ColorStateList.valueOf(ContextCompat.getColor(this, R.color.white)));
        btn.setStrokeWidth((int) (1 * getResources().getDisplayMetrics().density));
        btn.setTextColor(ContextCompat.getColor(this, R.color.black));
    }

    private void showLoginForm() {
        layoutLoginForm.setVisibility(View.VISIBLE);
        layoutRegisterForm.setVisibility(View.GONE);

        setTabActive(btnTabLogin);
        setTabInactive(btnTabRegister);
    }

    private void showRegisterForm() {
        layoutLoginForm.setVisibility(View.GONE);
        layoutRegisterForm.setVisibility(View.VISIBLE);

        setTabActive(btnTabRegister);
        setTabInactive(btnTabLogin);
    }

    private void setupActions() {
        btnSubmitLogin.setOnClickListener(v -> {
            String username = etLoginUsername.getText() != null ? etLoginUsername.getText().toString().trim() : "";
            String password = etLoginPassword.getText() != null ? etLoginPassword.getText().toString().trim() : "";

            if (username.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Vui lòng nhập đầy đủ tên đăng nhập và mật khẩu", Toast.LENGTH_SHORT).show();
                return;
            }

            performLogin(username, password);
        });

        btnSubmitRegister.setOnClickListener(v -> {
            String fullName = etRegFullName.getText() != null ? etRegFullName.getText().toString().trim() : "";
            String username = etRegUsername.getText() != null ? etRegUsername.getText().toString().trim() : "";
            String email = etRegEmail.getText() != null ? etRegEmail.getText().toString().trim() : "";
            String password = etRegPassword.getText() != null ? etRegPassword.getText().toString().trim() : "";

            if (fullName.isEmpty() || username.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Vui lòng nhập họ tên, tên đăng nhập và mật khẩu", Toast.LENGTH_SHORT).show();
                return;
            }

            User newUser = new User(username, password, fullName, email);
            long id = dbHelper.registerUser(newUser);
            if (id > 0) {
                newUser.setId(id);
                // Sync user to Supabase
                SupabaseClient.getApi(this).registerUser(newUser).enqueue(new Callback<List<User>>() {
                    @Override
                    public void onResponse(Call<List<User>> call, Response<List<User>> response) {}
                    @Override
                    public void onFailure(Call<List<User>> call, Throwable t) {}
                });

                Toast.makeText(this, "Đăng ký thành công! Chào mừng " + fullName, Toast.LENGTH_SHORT).show();
                loginSuccess(newUser);
            } else {
                Toast.makeText(this, "Tên đăng nhập đã tồn tại, vui lòng chọn tên khác!", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void performLogin(String username, String password) {
        User user = dbHelper.loginUser(username, password);
        if (user != null) {
            loginSuccess(user);
        } else {
            // Kiểm tra đám mây Supabase nếu trên máy chưa có
            SupabaseClient.getApi(this).loginUser("eq." + username, "eq." + password).enqueue(new Callback<List<User>>() {
                @Override
                public void onResponse(Call<List<User>> call, Response<List<User>> response) {
                    if (response.isSuccessful() && response.body() != null && !response.body().isEmpty()) {
                        User remoteUser = response.body().get(0);
                        dbHelper.registerUser(remoteUser);
                        loginSuccess(remoteUser);
                    } else {
                        Toast.makeText(LoginActivity.this, "Tên đăng nhập hoặc mật khẩu không chính xác!", Toast.LENGTH_SHORT).show();
                    }
                }

                @Override
                public void onFailure(Call<List<User>> call, Throwable t) {
                    Toast.makeText(LoginActivity.this, "Tên đăng nhập hoặc mật khẩu không đúng!", Toast.LENGTH_SHORT).show();
                }
            });
        }
    }

    private void loginSuccess(User user) {
        prefManager.saveUserSession(user);
        Toast.makeText(this, "Đang vào tài khoản: " + user.getFullName(), Toast.LENGTH_SHORT).show();

        Intent intent = new Intent(this, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
        finish();
    }

    private void loadQuickUsers() {
        layoutQuickUsers.removeAllViews();
        List<User> users = dbHelper.getAllUsers();
        if (users == null || users.isEmpty()) {
            layoutQuickUsers.setVisibility(View.GONE);
            return;
        }

        for (User u : users) {
            MaterialButton btnUser = new MaterialButton(this);
            btnUser.setText(u.getFullName());
            btnUser.setTextSize(12);
            btnUser.setCornerRadius(20);
            btnUser.setStrokeColor(ColorStateList.valueOf(ContextCompat.getColor(this, R.color.primary_green)));
            btnUser.setStrokeWidth(2);
            btnUser.setBackgroundTintList(ColorStateList.valueOf(ContextCompat.getColor(this, R.color.soft_green_bg)));
            btnUser.setTextColor(ContextCompat.getColor(this, R.color.primary_green_dark));
            btnUser.setAllCaps(false);

            LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
            );
            lp.setMargins(8, 0, 8, 0);
            btnUser.setLayoutParams(lp);

            btnUser.setOnClickListener(v -> {
                etLoginUsername.setText(u.getUsername());
                etLoginPassword.setText("123456");
                performLogin(u.getUsername(), "123456");
            });

            layoutQuickUsers.addView(btnUser);
        }
    }
}

