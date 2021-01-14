package com.example.pmis.Model;

public class Clinic {
    public String clinicName;
    public String address;
    public String contactNo;
    public String photoUrl;
    public String license;

    public String getDegree() {
        return degree;
    }

    public void setDegree(String degree) {
        this.degree = degree;
    }

    public String getLicense() {
        return license;
    }

    public void setLicense(String license) {
        this.license = license;
    }

    public String degree;
    public  Clinic(){

    }

    public String getPhotoUrl() {
        return photoUrl;
    }

    public void setPhotoUrl(String photoUrl) {
        this.photoUrl = photoUrl;
    }

    public Clinic(String clinicName, String address, String contactNo) {
        this.clinicName = clinicName;
        this.address = address;
        this.contactNo = contactNo;
    }

    public String getClinicName() {
        return clinicName;
    }

    public void setClinicName(String clinicName) {
        this.clinicName = clinicName;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getContactNo() {
        return contactNo;
    }

    public void setContactNo(String contactNo) {
        this.contactNo = contactNo;
    }
}
