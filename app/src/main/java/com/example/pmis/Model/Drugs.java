package com.example.pmis.Model;

public class Drugs {
    public String drugName , drugBrand, drugDosage, key;
    public int position;

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public Drugs(String drugName, String drugBrand, String drugDosage, String key) {
        this.drugName = drugName;
        this.drugBrand = drugBrand;
        this.drugDosage = drugDosage;
        this.key = key;
    }

    public int getPosition() {
        return position;
    }

    public void setPosition(int position) {
        this.position = position;
    }

    public  Drugs(int position){
        this.position = position;
    }
    public  Drugs(){

    }
    public String getDrugName() {
        return drugName;
    }

    public void setDrugName(String drugName) {
        this.drugName = drugName;
    }

    public String getDrugBrand() {
        return drugBrand;
    }

    public void setDrugBrand(String drugBrand) {
        this.drugBrand = drugBrand;
    }

    public String getDrugDosage() {
        return drugDosage;
    }

    public void setDrugDosage(String drugDosage) {
        this.drugDosage = drugDosage;
    }
}
