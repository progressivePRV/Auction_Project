package com.example.auctionapp;

import android.graphics.Color;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;

import java.util.ArrayList;

public class AdapterAuctionPlaced extends RecyclerView.Adapter<AdapterAuctionPlaced.MyViewHolder> {
    private ArrayList<AuctionItems> mDataset;
    public static InteractWithRecyclerView interact;
    private FirebaseAuth mAuth;

    // Provide a suitable constructor (depends on the kind of dataset)
    public AdapterAuctionPlaced(ArrayList<AuctionItems> myDataset, InteractWithRecyclerView ctx) {
        mDataset = myDataset;
        interact = ctx;
        mAuth = FirebaseAuth.getInstance();
    }

    // Create new views (invoked by the layout manager)
    @Override
    public AdapterAuctionPlaced.MyViewHolder onCreateViewHolder(ViewGroup parent,
                                                                int viewType) {
        // create a new view
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.list_all_auctions_placed, parent, false);
        MyViewHolder vh = new MyViewHolder(v);
        return vh;
    }

    // Replace the contents of a view (invoked by the layout manager)
    @Override
    public void onBindViewHolder(final MyViewHolder holder, final int position) {
        AuctionItems auctionItems = mDataset.get(position);

        holder.allAuctionsItemName.setText(auctionItems.item_name);
        if(auctionItems.current_highest_bid == 0.0){
            holder.allAuctionsCurrentHighestBid.setText("$ -.-");
        }else{
            holder.allAuctionsCurrentHighestBid.setText("$" + auctionItems.current_highest_bid);
        }

        holder.allAuctionsStartBid.setText("$" + auctionItems.start_bid);
        holder.allAuctionsDate.setText(auctionItems.auction_start_date);
        holder.auctions_placed_min_final_bid.setText("$" + auctionItems.min_final_bid);

        if(auctionItems.auction_status.equals("created")){
            holder.dividerAllAuction.setBackgroundColor(Color.parseColor("#ED8911"));
        }else if(auctionItems.auction_status.equals("in_progress")){
            holder.dividerAllAuction.setBackgroundColor(Color.parseColor("#C53CEE"));
        }else if(auctionItems.auction_status.equals("complete")){
            //The user can bid on this item
            holder.dividerAllAuction.setBackgroundColor(Color.parseColor("#0F4FEC"));
        }

        holder.allAuctionsConstraintLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d("Demo", "Selected Position is :" + mDataset.get(position));
                interact.getDetails(mDataset.get(position));
            }
        });
    }

    // Return the size of your dataset (invoked by the layout manager)
    @Override
    public int getItemCount() {
        return mDataset.size();
    }

    // Provide a reference to the views for each data item
    // Complex data items may need more than one view per item, and
    // you provide access to all the views for a data item in a view holder
    public static class MyViewHolder extends RecyclerView.ViewHolder {
        // each data item is just a string in this case

        TextView allAuctionsItemName, allAuctionsDate, allAuctionsStartBid, allAuctionsCurrentHighestBid, auctions_placed_min_final_bid;
        ConstraintLayout allAuctionsConstraintLayout;
        View dividerAllAuction;

        public MyViewHolder(View view) {
            super(view);
            allAuctionsItemName = view.findViewById(R.id.allAuctionsItemName);
            allAuctionsDate = view.findViewById(R.id.allAuctionsDate);
            allAuctionsStartBid = view.findViewById(R.id.allAuctionsStartBid);
            allAuctionsCurrentHighestBid = view.findViewById(R.id.allAuctionsCurrentHighestBid);
            dividerAllAuction = view.findViewById(R.id.dividerAllAuction);
            allAuctionsConstraintLayout = view.findViewById(R.id.allAuctionsConstraintLayout);
            auctions_placed_min_final_bid = view.findViewById(R.id.auctions_placed_min_final_bid);
        }
    }

    public interface InteractWithRecyclerView {
        public void getDetails(AuctionItems auctionItems);
    }
}