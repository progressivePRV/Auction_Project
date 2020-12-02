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

public class AdapterWonAuctions extends RecyclerView.Adapter<AdapterWonAuctions.MyViewHolder> {
    private ArrayList<WonItems> mDataset;
//    public static InteractWithRecyclerView interact;
    private FirebaseAuth mAuth;

    // Provide a suitable constructor (depends on the kind of dataset)
    public AdapterWonAuctions(ArrayList<WonItems> myDataset) {
        mDataset = myDataset;
        mAuth = FirebaseAuth.getInstance();
    }

    // Create new views (invoked by the layout manager)
    @Override
    public AdapterWonAuctions.MyViewHolder onCreateViewHolder(ViewGroup parent,
                                                              int viewType) {
        // create a new view
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.list_won_auctions, parent, false);
        MyViewHolder vh = new MyViewHolder(v);
        return vh;
    }

    // Replace the contents of a view (invoked by the layout manager)
    @Override
    public void onBindViewHolder(final MyViewHolder holder, final int position) {
        WonItems wonItems = mDataset.get(position);

        holder.listWonItemName.setText(wonItems.item_name);
        holder.listWonCompletedDate.setText(wonItems.buying_date);
        holder.listWonBidAmount.setText("$" + wonItems.item_price);
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
        TextView listWonItemName, listWonCompletedDate, listWonBidAmount;
        public MyViewHolder(View view) {
            super(view);
            listWonItemName = view.findViewById(R.id.listWonItemName);
            listWonCompletedDate = view.findViewById(R.id.listWonCompletedDate);
            listWonBidAmount = view.findViewById(R.id.listWonBidAmount);
        }
    }

}