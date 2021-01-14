package com.example.pmis.Model;

public class Installment {
    String method;
    String amount;
    String remarks;
    String date;
    String dateUpdated;
    long timeStamp;

    public long getTimeStamp() {
        return timeStamp;
    }

    public void setTimeStamp(long timeStamp) {
        this.timeStamp = timeStamp;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    String key;

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getDateUpdated() {
        return dateUpdated;
    }

    public void setDateUpdated(String dateUpdated) {
        this.dateUpdated = dateUpdated;
    }

    public Installment(){

    }
    public String getMethod() {
        return method;
    }

    public void setMethod(String method) {
        this.method = method;
    }

    public String getAmount() {
        return amount;
    }

    public void setAmount(String amount) {
        this.amount = amount;
    }

    public String getRemarks() {
        return remarks;
    }

    public void setRemarks(String remarks) {
        this.remarks = remarks;
    }
}
