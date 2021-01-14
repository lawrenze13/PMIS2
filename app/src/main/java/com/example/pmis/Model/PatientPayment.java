package com.example.pmis.Model;

public class PatientPayment {
    String patientKey;
    String key;
    String docName;
    String type;
    String method;
    String date;
    String total;
    String remarks;
    String procedureKey;
    String amount;

    public String getAmount() {
        return amount;
    }

    public void setAmount(String amount) {
        this.amount = amount;
    }

    public long getTimeStamp() {
        return timeStamp;
    }

    public void setTimeStamp(long timeStamp) {
        this.timeStamp = timeStamp;
    }

    long timeStamp;

    public String getProcedureKey() {
        return procedureKey;
    }

    public void setProcedureKey(String procedureKey) {
        this.procedureKey = procedureKey;
    }

    double totalPayment;

    public double getTotalPayment() {
        return totalPayment;
    }

    public void setTotalPayment(double totalPayment) {
        this.totalPayment = totalPayment;
    }

    public String getPlanName() {
        return planName;
    }

    public void setPlanName(String planName) {
        this.planName = planName;
    }

    String dateUpdated;
    String planName;
    public String getDateUpdated() {
        return dateUpdated;
    }

    public void setDateUpdated(String dateUpdated) {
        this.dateUpdated = dateUpdated;
    }

    public String getPatientKey() {
        return patientKey;
    }

    public void setPatientKey(String patientKey) {
        this.patientKey = patientKey;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public String getDocName() {
        return docName;
    }

    public void setDocName(String docName) {
        this.docName = docName;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getMethod() {
        return method;
    }

    public void setMethod(String method) {
        this.method = method;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getTotal() {
        return total;
    }

    public void setTotal(String total) {
        this.total = total;
    }

    public String getRemarks() {
        return remarks;
    }

    public void setRemarks(String remarks) {
        this.remarks = remarks;
    }

    public String getInitialPayment() {
        return initialPayment;
    }

    public void setInitialPayment(String initialPayment) {
        this.initialPayment = initialPayment;
    }

    public String getInitialRemarks() {
        return initialRemarks;
    }

    public void setInitialRemarks(String initialRemarks) {
        this.initialRemarks = initialRemarks;
    }

    String initialPayment;
    String initialRemarks;

    public PatientPayment(String patientKey, String key, String docName, String type, String method, String date, String total, String remarks, String initialPayment, String initialRemarks) {
        this.patientKey = patientKey;
        this.key = key;
        this.docName = docName;
        this.type = type;
        this.method = method;
        this.date = date;
        this.total = total;
        this.remarks = remarks;
        this.initialPayment = initialPayment;
        this.initialRemarks = initialRemarks;
    }

    public PatientPayment(){

    }
}
