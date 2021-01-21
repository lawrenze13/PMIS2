package com.example.pmis.Model;

public class AppointmentStatus {
    String scheduleKey, status;

    public AppointmentStatus(String scheduleKey, String status) {
        this.scheduleKey = scheduleKey;
        this.status = status;
    }

    public String getScheduleKey() {
        return scheduleKey;
    }

    public void setScheduleKey(String scheduleKey) {
        this.scheduleKey = scheduleKey;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public AppointmentStatus(){

    }
}
