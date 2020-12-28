package com.example.pmis.Model;

public class PatientProcedures {
    String key;
    String date;
    String dateUpdated;
    String procedure;
    String note;

    public String getProcedureKey() {
        return procedureKey;
    }

    public PatientProcedures(String key, String date, String dateUpdated, String procedure, String note, String procedureKey) {
        this.key = key;
        this.date = date;
        this.dateUpdated = dateUpdated;
        this.procedure = procedure;
        this.note = note;
        this.procedureKey = procedureKey;
    }

    public void setProcedureKey(String procedureKey) {
        this.procedureKey = procedureKey;
    }

    String procedureKey;
    public PatientProcedures(){

    }

    public String getNote() {
        return note;
    }

    public void setNote(String note) {
        this.note = note;
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

    public String getProcedure() {
        return procedure;
    }

    public void setProcedure(String procedure) {
        this.procedure = procedure;
    }
}
