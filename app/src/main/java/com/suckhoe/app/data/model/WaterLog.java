package com.suckhoe.app.data.model;

import com.google.gson.annotations.SerializedName;

public class WaterLog {
    @SerializedName("id")
    private Long id;

    @SerializedName("amount_ml")
    private int amountMl;

    @SerializedName("target_ml")
    private int targetMl;

    @SerializedName("log_date")
    private String logDate;

    @SerializedName("created_at")
    private String createdAt;

    public WaterLog() {}

    public WaterLog(int amountMl, int targetMl, String logDate) {
        this.amountMl = amountMl;
        this.targetMl = targetMl;
        this.logDate = logDate;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public int getAmountMl() { return amountMl; }
    public void setAmountMl(int amountMl) { this.amountMl = amountMl; }

    public int getTargetMl() { return targetMl; }
    public void setTargetMl(int targetMl) { this.targetMl = targetMl; }

    public String getLogDate() { return logDate; }
    public void setLogDate(String logDate) { this.logDate = logDate; }

    public String getCreatedAt() { return createdAt; }
    public void setCreatedAt(String createdAt) { this.createdAt = createdAt; }
}

