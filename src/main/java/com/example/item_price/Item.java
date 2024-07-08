package com.example.item_price;

public class Item {
    private int id;
    private String name;
    private double rice;
    private int num;
    public int getNum() {
        return num;
    }

    public void setNum(int num) {
        this.num = num;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public double getPrice() {
        return rice;
    }

    public void setPrice(double rice) {
        this.rice = rice;
    }
}
