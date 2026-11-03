package com.suckhoe.app.ui.dialog;

import android.app.Dialog;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.media.AudioManager;
import android.media.ToneGenerator;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Vibrator;
import android.view.Window;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;

import com.google.android.material.button.MaterialButton;
import com.suckhoe.app.R;

import java.util.Locale;

public class RestTimerDialog extends Dialog {
    private TextView tvCountdown;
    private TextView tvStatus;
    private MaterialButton btnStartPause;
    private MaterialButton btnAdd15;
    private MaterialButton btnReset;
    private Button btnClose;

    private Button btn30, btn60, btn90, btn120;

    private CountDownTimer countDownTimer;
    private long totalTimeMillis = 60000;
    private long timeRemainingMillis = 60000;
    private boolean isRunning = false;

    private ToneGenerator toneGenerator;
    private Vibrator vibrator;

    public RestTimerDialog(@NonNull Context context) {
        super(context);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.dialog_rest_timer);

        if (getWindow() != null) {
            getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        }

        try {
            toneGenerator = new ToneGenerator(AudioManager.STREAM_NOTIFICATION, 100);
            vibrator = (Vibrator) getContext().getSystemService(Context.VIBRATOR_SERVICE);
        } catch (Exception ignored) {}

        initViews();
        setupListeners();
        updateCountdownText();
    }

    private void initViews() {
        tvCountdown = findViewById(R.id.tv_timer_countdown);
        tvStatus = findViewById(R.id.tv_rest_status);
        btnStartPause = findViewById(R.id.btn_timer_start_pause);
        btnAdd15 = findViewById(R.id.btn_timer_add_15);
        btnReset = findViewById(R.id.btn_timer_reset);
        btnClose = findViewById(R.id.btn_timer_close);

        btn30 = findViewById(R.id.btn_preset_30);
        btn60 = findViewById(R.id.btn_preset_60);
        btn90 = findViewById(R.id.btn_preset_90);
        btn120 = findViewById(R.id.btn_preset_120);
    }

    private void setupListeners() {
        btnStartPause.setOnClickListener(v -> {
            if (isRunning) {
                pauseTimer();
            } else {
                startTimer();
            }
        });

        btnAdd15.setOnClickListener(v -> {
            timeRemainingMillis += 15000;
            totalTimeMillis += 15000;
            if (isRunning) {
                pauseTimer();
                startTimer();
            } else {
                updateCountdownText();
            }
        });

        btnReset.setOnClickListener(v -> resetTimer());

        btn30.setOnClickListener(v -> setPresetTime(30000));
        btn60.setOnClickListener(v -> setPresetTime(60000));
        btn90.setOnClickListener(v -> setPresetTime(90000));
        btn120.setOnClickListener(v -> setPresetTime(120000));

        btnClose.setOnClickListener(v -> dismiss());
    }

    private void setPresetTime(long millis) {
        if (isRunning) {
            pauseTimer();
        }
        totalTimeMillis = millis;
        timeRemainingMillis = millis;
        tvStatus.setText("Sẵn sàng đếm nghỉ");
        updateCountdownText();
    }

    private void startTimer() {
        countDownTimer = new CountDownTimer(timeRemainingMillis, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                timeRemainingMillis = millisUntilFinished;
                updateCountdownText();
            }

            @Override
            public void onFinish() {
                isRunning = false;
                timeRemainingMillis = 0;
                updateCountdownText();
                tvStatus.setText(getContext().getString(R.string.rest_done));
                btnStartPause.setText(getContext().getString(R.string.start));

                // Thông báo âm thanh & rung khi hết giờ nghỉ
                try {
                    if (toneGenerator != null) {
                        toneGenerator.startTone(ToneGenerator.TONE_PROP_BEEP2, 1000);
                    }
                    if (vibrator != null) {
                        vibrator.vibrate(500);
                    }
                } catch (Exception ignored) {}
            }
        }.start();

        isRunning = true;
        btnStartPause.setText(getContext().getString(R.string.pause));
        tvStatus.setText("Đang nghỉ ngơi...");
    }

    private void pauseTimer() {
        if (countDownTimer != null) {
            countDownTimer.cancel();
        }
        isRunning = false;
        btnStartPause.setText(getContext().getString(R.string.resume));
        tvStatus.setText("Đã tạm dừng");
    }

    private void resetTimer() {
        if (countDownTimer != null) {
            countDownTimer.cancel();
        }
        isRunning = false;
        timeRemainingMillis = totalTimeMillis;
        btnStartPause.setText(getContext().getString(R.string.start));
        tvStatus.setText("Nghỉ giữa các hiệp tập");
        updateCountdownText();
    }

    private void updateCountdownText() {
        int minutes = (int) (timeRemainingMillis / 1000) / 60;
        int seconds = (int) (timeRemainingMillis / 1000) % 60;
        String formatted = String.format(Locale.getDefault(), "%02d:%02d", minutes, seconds);
        tvCountdown.setText(formatted);
    }

    @Override
    public void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        if (countDownTimer != null) {
            countDownTimer.cancel();
        }
        if (toneGenerator != null) {
            toneGenerator.release();
        }
    }
}
