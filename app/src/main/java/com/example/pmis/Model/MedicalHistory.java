package com.example.pmis.Model;

public class MedicalHistory {
    public String key, imageUrl, date, caption;

    public MedicalHistory(String key, String imageUrl, String date, String caption) {
        this.key = key;
        this.imageUrl = imageUrl;
        this.date = date;
        this.caption = caption;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getCaption() {
        return caption;
    }

    public void setCaption(String caption) {
        this.caption = caption;
    }

    public  MedicalHistory(){

    }
}
