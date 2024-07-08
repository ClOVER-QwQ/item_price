package com.example.item_price;

public class Item {
    private int id;
    private String name;
    private String rice;
    private int num=0;
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

    public String getPrice() {
        return rice;
    }

    public void setPrice(String rice) {
        this.rice = rice;
    }
}
