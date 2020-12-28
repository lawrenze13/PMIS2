package com.example.pmis.Model;

public class DrugPrescription {
    public String drugInfo;
    public String quantity;
    public String frequency;
    public String duration;
    public String key;
    public String parentKey;
    public String getParentKey() {
        return parentKey;
    }
    public void setParentKey(String parentKey) {
        this.parentKey = parentKey;
    }
    public  DrugPrescription(){

    }

    public DrugPrescription(String drugInfo, String quantity, String frequency, String duration, String key) {
        this.drugInfo = drugInfo;
        this.quantity = quantity;
        this.frequency = frequency;
        this.duration = duration;
        this.key = key;
    }

    public String getDrugInfo() {
        return drugInfo;
    }

    public void setDrugInfo(String drugInfo) {
        this.drugInfo = drugInfo;
    }

    public String getQuantity() {
        return quantity;
    }

    public void setQuantity(String quantity) {
        this.quantity = quantity;
    }

    public String getFrequency() {
        return frequency;
    }

    public void setFrequency(String frequency) {
        this.frequency = frequency;
    }

    public String getDuration() {
        return duration;
    }

    public void setDuration(String duration) {
        this.duration = duration;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }
}
