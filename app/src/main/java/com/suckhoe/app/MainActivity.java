package com.suckhoe.app;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.suckhoe.app.ui.LoginActivity;
import com.suckhoe.app.ui.dialog.UserProfileDialog;
import com.suckhoe.app.ui.fragment.HomeFragment;
import com.suckhoe.app.ui.fragment.NutritionFragment;
import com.suckhoe.app.ui.fragment.ScheduleFragment;
import com.suckhoe.app.ui.fragment.WeightWaterFragment;
import com.suckhoe.app.ui.fragment.WorkoutFragment;
import com.suckhoe.app.util.PreferenceManager;

public class MainActivity extends AppCompatActivity {
    private static final int REQ_NOTIFICATION_PERMISSION = 2001;

    private Toolbar toolbar;
    private BottomNavigationView bottomNav;
    private PreferenceManager prefManager;

    private HomeFragment homeFragment;
    private NutritionFragment nutritionFragment;
    private WorkoutFragment workoutFragment;
    private ScheduleFragment scheduleFragment;
    private WeightWaterFragment weightWaterFragment;

    private Fragment activeFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        prefManager = new PreferenceManager(this);
        // Nếu chưa đăng nhập, chuyển sang màn hình Đăng nhập / Chọn tài khoản
        if (!prefManager.isLoggedIn()) {
            Intent intent = new Intent(this, LoginActivity.class);
            startActivity(intent);
            finish();
            return;
        }

        setContentView(R.layout.activity_main);

        initViews();
        setupToolbar();
        setupFragments();
        setupBottomNav();
        checkPermissions();
    }

    private void initViews() {
        toolbar = findViewById(R.id.main_toolbar);
        bottomNav = findViewById(R.id.bottom_navigation);
    }

    private void setupToolbar() {
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setSubtitle("👤 " + prefManager.getCurrentUserFullName());
        }
    }

    private void setupFragments() {
        homeFragment = new HomeFragment();
        nutritionFragment = new NutritionFragment();
        workoutFragment = new WorkoutFragment();
        scheduleFragment = new ScheduleFragment();
        weightWaterFragment = new WeightWaterFragment();

        FragmentManager fm = getSupportFragmentManager();
        FragmentTransaction ft = fm.beginTransaction();
        ft.add(R.id.fragment_container, weightWaterFragment, "5").hide(weightWaterFragment);
        ft.add(R.id.fragment_container, scheduleFragment, "4").hide(scheduleFragment);
        ft.add(R.id.fragment_container, workoutFragment, "3").hide(workoutFragment);
        ft.add(R.id.fragment_container, nutritionFragment, "2").hide(nutritionFragment);
        ft.add(R.id.fragment_container, homeFragment, "1");
        ft.commit();

        activeFragment = homeFragment;
    }

    private void setupBottomNav() {
        bottomNav.setOnItemSelectedListener(item -> {
            int id = item.getItemId();
            if (id == R.id.nav_home) {
                switchFragment(homeFragment, getString(R.string.nav_home));
                return true;
            } else if (id == R.id.nav_nutrition) {
                switchFragment(nutritionFragment, getString(R.string.nav_nutrition));
                return true;
            } else if (id == R.id.nav_workout) {
                switchFragment(workoutFragment, getString(R.string.nav_workout));
                return true;
            } else if (id == R.id.nav_schedule) {
                switchFragment(scheduleFragment, getString(R.string.nav_schedule));
                return true;
            } else if (id == R.id.nav_weight_water) {
                switchFragment(weightWaterFragment, getString(R.string.nav_weight_water));
                return true;
            }
            return false;
        });
    }

    private void switchFragment(Fragment targetFragment, String title) {
        if (activeFragment != targetFragment) {
            getSupportFragmentManager().beginTransaction()
                    .hide(activeFragment)
                    .show(targetFragment)
                    .commit();
            activeFragment = targetFragment;
            if (getSupportActionBar() != null) {
                getSupportActionBar().setTitle(title);
                getSupportActionBar().setSubtitle("👤 " + prefManager.getCurrentUserFullName());
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.top_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_user_profile) {
            new UserProfileDialog(this).show();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void checkPermissions() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS)
                    != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.POST_NOTIFICATIONS},
                        REQ_NOTIFICATION_PERMISSION);
            }
        }
    }
}
