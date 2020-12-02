package com.example.auctionapp;

import android.content.Context;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.widget.LinearLayoutCompat;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;

import java.util.ArrayList;

public class AdapterAllAuctions extends RecyclerView.Adapter<AdapterAllAuctions.MyViewHolder> {
    private ArrayList<AuctionItems> mDataset;
    public static InteractWithRecyclerView interact;
    private FirebaseAuth mAuth;

    // Provide a suitable constructor (depends on the kind of dataset)
    public AdapterAllAuctions(ArrayList<AuctionItems> myDataset, InteractWithRecyclerView ctx) {
        mDataset = myDataset;
        interact = ctx;
        mAuth = FirebaseAuth.getInstance();
    }

    // Create new views (invoked by the layout manager)
    @Override
    public AdapterAllAuctions.MyViewHolder onCreateViewHolder(ViewGroup parent,
                                                                      int viewType) {
        // create a new view
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.list_all_auctions, parent, false);
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

        if(auctionItems.owner_id.equals(mAuth.getUid())){
            holder.dividerAllAuction.setBackgroundColor(Color.parseColor("#E5E827"));
        }else if(auctionItems.current_highest_bid_user.equals(mAuth.getUid())){
            holder.dividerAllAuction.setBackgroundColor(Color.parseColor("#F91B1B"));
        }else{
            //The user can bid on this item
            holder.dividerAllAuction.setBackgroundColor(Color.parseColor("#FFFFFF"));
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

        TextView allAuctionsItemName, allAuctionsDate, allAuctionsStartBid, allAuctionsCurrentHighestBid;
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
        }
    }

    public interface InteractWithRecyclerView {
        public void getDetails(AuctionItems auctionItems);
    }
}