package com.example.auctionapp;

public class WonItems {
    Double item_price;
    String item_name, buying_date, item_id;

    @Override
    public String toString() {
        return "WonItems{" +
                "item_price=" + item_price +
                ", item_name='" + item_name + '\'' +
                ", buying_date='" + buying_date + '\'' +
                ", item_id='" + item_id + '\'' +
                '}';
    }
}
