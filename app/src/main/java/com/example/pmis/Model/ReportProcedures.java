package com.example.pmis.Model;

import java.util.List;

public class ReportProcedures {
    String patientName;
    String date;
    String price;
    String procedureName;

    public String getProcedureKey() {
        return procedureKey;
    }

    public void setProcedureKey(String procedureKey) {
        this.procedureKey = procedureKey;
    }

    String procedureKey;
    List<PatientProcedures> patientProceduresList;

    public List<PatientProcedures> getPatientProceduresList() {
        return patientProceduresList;
    }

    public void setPatientProceduresList(List<PatientProcedures> patientProceduresList) {
        this.patientProceduresList = patientProceduresList;
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

    public String getPrice() {
        return price;
    }

    public void setPrice(String price) {
        this.price = price;
    }

    public String getProcedureName() {
        return procedureName;
    }

    public void setProcedureName(String procedureName) {
        this.procedureName = procedureName;
    }
}
