package com.plusstudio.medicentalarm;

public class Alarm {
    private int id;
    private String reason;
    private String startTime;
    private int repeatCount;
    private String period;

    public Alarm(int id, String reason, String startTime, int repeatCount, String period) {
        this.id = id;
        this.reason = reason;
        this.startTime = startTime;
        this.repeatCount = repeatCount;
        this.period = period;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getReason() { return reason; }
    public void setReason(String reason) { this.reason = reason; }

    public String getStartTime() { return startTime; }
    public void setStartTime(String startTime) { this.startTime = startTime; }

    public int getRepeatCount() { return repeatCount; }
    public void setRepeatCount(int repeatCount) { this.repeatCount = repeatCount; }

    public String getPeriod() { return period; }
    public void setPeriod(String period) { this.period = period; }
}