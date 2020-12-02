package com.example.auctionapp;

import java.util.ArrayList;

public class AuctionItems {
//    "result": [
//    {
//        "id": "bjTaKwTz53zxbDzoncFS",
//            "data": {
//        "owner_id": "a1",
//                "item_name": "Tennis Racket",
//                "auction_start_date": "2020 Dec 1 19:30:57",
//                "min_final_bid": 100,
//                "start_bid": 20,
//                "bidders": {
//            "a2": 20.4
//        },
//        "current_highest_bid": 20.4,
//                "auction_status": "in_progress",
//                "auction_update_date": "2020 Dec 1 19:33:04",
//                "current_highest_bid_user": "a2"
//    }
//    }
//    ]

    String    id
            , owner_id
            , item_name
            , auction_start_date
            , auction_status
            , current_highest_bid_user;

    Double  current_highest_bid
            , start_bid
            , min_final_bid;

//    ArrayList<Biders> bidders = new ArrayList<>();


    @Override
    public String toString() {
        return "AuctionItems{" +
                "id='" + id + '\'' +
                ", owner_id='" + owner_id + '\'' +
                ", item_name='" + item_name + '\'' +
                ", auction_start_date='" + auction_start_date + '\'' +
                ", auction_status='" + auction_status + '\'' +
                ", current_highest_bid_user='" + current_highest_bid_user + '\'' +
                ", current_highest_bid=" + current_highest_bid +
                ", start_bid=" + start_bid +
                ", min_final_bid=" + min_final_bid +
                '}';
    }
}
