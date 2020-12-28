package com.example.pmis.Model;

public class Clinic {
    public String clinicName, address, contactNo;
    public  Clinic(){

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
