package com.example.pmis.Model;

public class DrugPrescriptionMain {
    public String key;
    public String date;
    public String dateUpdated;

    public DrugPrescriptionMain(String key, String date) {
        this.key = key;
        this.date = date;
    }

    public DrugPrescriptionMain(){

    }
    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

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
}
