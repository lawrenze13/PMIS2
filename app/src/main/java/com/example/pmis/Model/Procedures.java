package com.example.pmis.Model;

public class Procedures {
    String name, description, key;
    int price;

    public Procedures(String name, String description, String key, int price) {
        this.name = name;
        this.description = description;
        this.key = key;
        this.price = price;
    }
    public Procedures(){

    }
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public int getPrice() {
        return price;
    }

    public void setPrice(int price) {
        this.price = price;
    }
}
