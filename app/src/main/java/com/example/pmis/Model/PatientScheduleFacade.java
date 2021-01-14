package com.example.pmis.Model;

public class PatientScheduleFacade {
    String patientName, date, startTime, endTime, note, contactNo, patientKey, scheduleKey;
    public PatientScheduleFacade(){

    }

    public String getPatientName() {
        return patientName;
    }

    public void setPatientName(String patientName) {
        this.patientName = patientName;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getStartTime() {
        return startTime;
    }

    public void setStartTime(String startTime) {
        this.startTime = startTime;
    }

    public String getEndTime() {
        return endTime;
    }

    public void setEndTime(String endTime) {
        this.endTime = endTime;
    }

    public String getNote() {
        return note;
    }

    public void setNote(String note) {
        this.note = note;
    }

    public String getContactNo() {
        return contactNo;
    }

    public void setContactNo(String contactNo) {
        this.contactNo = contactNo;
    }

    public String getPatientKey() {
        return patientKey;
    }

    public void setPatientKey(String patientKey) {
        this.patientKey = patientKey;
    }

    public String getScheduleKey() {
        return scheduleKey;
    }

    public void setScheduleKey(String scheduleKey) {
        this.scheduleKey = scheduleKey;
    }

    public PatientScheduleFacade(String patientName, String date, String startTime, String endTime, String note, String contactNo, String patientKey, String scheduleKey) {
        this.patientName = patientName;
        this.date = date;
        this.startTime = startTime;
        this.endTime = endTime;
        this.note = note;
        this.contactNo = contactNo;
        this.patientKey = patientKey;
        this.scheduleKey = scheduleKey;
    }
}
