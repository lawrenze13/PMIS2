package com.example.pmis.Model;

public class Schedule {
    public String key;
    public String patientKey;
    public String docName;
    public String date;
    public String startTime;
    public String endTime;
    public String remarks;

    public long getTimeStamp() {
        return timeStamp;
    }

    public void setTimeStamp(long timeStamp) {
        this.timeStamp = timeStamp;
    }

    public long timeStamp;
    public Schedule(){

    }
    public Schedule(String key, String patientKey, String docName, String date, String startTime, String endTime, String remarks) {
        this.key = key;
        this.patientKey = patientKey;
        this.docName = docName;
        this.date = date;
        this.startTime = startTime;
        this.endTime = endTime;
        this.remarks = remarks;
    }
    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public String getPatientKey() {
        return patientKey;
    }

    public void setPatientKey(String patientKey) {
        this.patientKey = patientKey;
    }

    public String getDocName() {
        return docName;
    }

    public void setDocName(String docName) {
        this.docName = docName;
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

    public String getRemarks() {
        return remarks;
    }

    public void setRemarks(String remarks) {
        this.remarks = remarks;
    }




}
