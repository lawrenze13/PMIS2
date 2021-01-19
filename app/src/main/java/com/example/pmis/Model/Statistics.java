package com.example.pmis.Model;

public class Statistics {
    public Statistics(){

    }
    public String getBarangay() {
        return Barangay;
    }

    public void setBarangay(String barangay) {
        Barangay = barangay;
    }

    public int getCount() {
        return count;
    }

    public void setCount(int count) {
        this.count = count;
    }

    public Statistics(String barangay, int count) {
        Barangay = barangay;
        this.count = count;
    }

    String Barangay;
    int count;
}
