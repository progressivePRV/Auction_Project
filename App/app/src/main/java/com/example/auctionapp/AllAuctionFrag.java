package com.example.auctionapp;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.common.reflect.TypeToken;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.functions.FirebaseFunctions;
import com.google.firebase.functions.HttpsCallableResult;
import com.google.gson.Gson;
import com.google.gson.JsonObject;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link AllAuctionFrag#newInstance} factory method to
 * create an instance of this fragment.
 */
public class AllAuctionFrag extends Fragment implements AdapterAllAuctions.InteractWithRecyclerView{

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";
    private static final String TAG = "okay";
    private FirebaseFunctions mFunctions;
    private RecyclerView recyclerView;
    private RecyclerView.Adapter mAdapter;
    private RecyclerView.LayoutManager layoutManager;
    ArrayList<AuctionItems> auctionItemsArrayList = new ArrayList<>();
    private ProgressDialog progressDialog;
    private SwipeRefreshLayout swipeRefreshLayout;

    private FirebaseAuth mAuth;
    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    public AllAuctionFrag() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment AllAuctionFrag.
     */
    // TODO: Rename and change types and number of parameters
    public static AllAuctionFrag newInstance(String param1, String param2) {
        AllAuctionFrag fragment = new AllAuctionFrag();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_all_auction, container, false);
        Log.d(TAG, "onCreateView: in allAuction Frag");
        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mAuth = FirebaseAuth.getInstance();
        mFunctions = FirebaseFunctions.getInstance();

        recyclerView = getView().findViewById(R.id.allAuctionsRecyclerView);
        layoutManager = new LinearLayoutManager(getActivity());
        recyclerView.setLayoutManager(layoutManager);
        mAdapter = new AdapterAllAuctions(auctionItemsArrayList, this);
        recyclerView.setAdapter(mAdapter);

        swipeRefreshLayout = getView().findViewById(R.id.swipe);

        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                auctionItemsArrayList.clear();
                getAllAuctions();
                swipeRefreshLayout.setRefreshing(false);
            }
        });
    }

    private void getAllAuctions() {
        showProgressBarDialog();
        mFunctions.getHttpsCallable("getAuctionItems")
                .call()
                .addOnCompleteListener(new OnCompleteListener<HttpsCallableResult>() {
                    @Override
                    public void onComplete(@NonNull Task<HttpsCallableResult> task) {
                        if (task.isSuccessful()){
                            Log.d(TAG, "onComplete: All Auctions got successfully");
                            Toast.makeText(getActivity(), "Retrieving all the items", Toast.LENGTH_SHORT).show();
                            Gson g = new Gson();
                            String json = g.toJson(task.getResult().getData());
                            try {
                                JSONObject root = new JSONObject(json);
                                JSONArray jsonArray = root.getJSONArray("result");
                                for(int i=0; i<jsonArray.length(); i++){
                                    JSONObject resultObject = jsonArray.getJSONObject(i);
                                    AuctionItems auctionItems = new AuctionItems();
                                    auctionItems.id = resultObject.getString("id");
                                    JSONObject dataObject = resultObject.getJSONObject("data");
                                    auctionItems.item_name = dataObject.getString("item_name");
                                    auctionItems.owner_id = dataObject.getString("owner_id");
                                    auctionItems.start_bid = dataObject.getDouble("start_bid");
                                    auctionItems.auction_start_date = dataObject.getString("auction_start_date");
                                    auctionItems.auction_status = dataObject.getString("auction_status");
                                    auctionItems.min_final_bid = dataObject.getDouble("min_final_bid");
                                    try{
                                        auctionItems.current_highest_bid = dataObject.getDouble("current_highest_bid");
                                        auctionItems.current_highest_bid_user = dataObject.getString("current_highest_bid_user");
                                    }catch(Exception e){
                                        auctionItems.current_highest_bid = 0.0;
                                        auctionItems.current_highest_bid_user = "";
                                    }

                                    auctionItemsArrayList.add(auctionItems);
                                }
                                Log.d("demo", "auctionItemsArrayList is ==> " + auctionItemsArrayList.toString());
                            } catch (JSONException e) {
                                hideProgressBarDialog();
                                e.printStackTrace();
                            }
                            if(auctionItemsArrayList.size()>0){
                                sortTheArrayList();
                                hideProgressBarDialog();
                                mAdapter.notifyDataSetChanged();
                            }else{
                                hideProgressBarDialog();
                                mAdapter.notifyDataSetChanged();
                                Toast.makeText(getActivity(), "Sorry No Auction available at this moment", Toast.LENGTH_SHORT).show();
                            }
                        }else{
                            hideProgressBarDialog();
                            Log.d(TAG, "onComplete: All Auctions error occurred"+task.getException().getMessage());
                            Toast.makeText(getActivity(), task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }



    @Override
    public void onResume() {
        super.onResume();
        Log.d(TAG, "onResume: in allAuction Frag");
        auctionItemsArrayList.clear();
        getAllAuctions();
    }


    private void sortTheArrayList() {
        String uid = mAuth.getUid();
        Collections.sort(auctionItemsArrayList, new Comparator<AuctionItems>(){
            public int compare(AuctionItems o1, AuctionItems o2){
                int start = 0;
                int end = 0;
                if(!o1.current_highest_bid_user.equals(uid) && !o1.owner_id.equals(uid)){
                    start = 0;
                }else if(o1.current_highest_bid_user.equals(uid) && !o1.owner_id.equals(uid)){
                    start = 1;
                }else if(!o1.current_highest_bid_user.equals(uid) && o1.owner_id.equals(uid)){
                    start = 2;
                }

                if(!o2.current_highest_bid_user.equals(uid) && !o2.owner_id.equals(uid)){
                    end = 0;
                }else if(o2.current_highest_bid_user.equals(uid) && !o2.owner_id.equals(uid)){
                    end = 1;
                }else if(!o2.current_highest_bid_user.equals(uid) && o2.owner_id.equals(uid)){
                    end = 2;
                }

                return start - end;
            }
        });
    }

    public void showProgressBarDialog()
    {
        progressDialog = new ProgressDialog(getActivity());
        progressDialog.setMessage("Loading");
        progressDialog.setCancelable(false);
        progressDialog.show();
    }

    public void hideProgressBarDialog()
    {
        progressDialog.dismiss();
    }

    @Override
    public void getDetails(AuctionItems auctionItems) {
        if(auctionItems.owner_id.equals(mAuth.getUid())){
            Toast.makeText(getActivity(), "As you are the owner, you cannot bid on this item", Toast.LENGTH_SHORT).show();
        }else if(auctionItems.current_highest_bid_user.equals(mAuth.getUid())){
            Toast.makeText(getActivity(), "As you are the current highest bidder, you cannot bid on it again", Toast.LENGTH_SHORT).show();
        }else{
            //The user can bid on this item
            showBidDialog(auctionItems.id);
        }
    }

    private void showBidDialog(String id) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        final View customLayout = getActivity().getLayoutInflater().inflate(R.layout.dialog_bid, null);
        builder.setView(customLayout)
                .setPositiveButton("Ok", new DialogInterface.OnClickListener(){
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        EditText amountBid = customLayout.findViewById(R.id.dialogEnterBidAmount);
                        if(checkDoubleValidations(amountBid)){
                            callBidOnItem(id, Double.parseDouble(amountBid.getText().toString()));
                        }
                    }
                }).setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

            }
        });
        final AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

    private void callBidOnItem(String id, Double amountBid) {
        showProgressBarDialog();
        Map<String ,Object> data = new HashMap<>();
        data.put("itemId",id);
        data.put("bid",amountBid);
        mFunctions.getHttpsCallable("bidOnItem")
                .call(data)
                .addOnCompleteListener(new OnCompleteListener<HttpsCallableResult>() {
                    @Override
                    public void onComplete(@NonNull Task<HttpsCallableResult> task) {
                        if (task.isSuccessful()){
                            Log.d(TAG, "onComplete: Bid On item Successfull");
                            Toast.makeText(getActivity(), "Bidded Successfully", Toast.LENGTH_SHORT).show();
                            hideProgressBarDialog();
                            auctionItemsArrayList.clear();
                            getAllAuctions();
                        }else{
                            Log.d(TAG, "onComplete: error while bidding on item"+task.getException().getMessage());
                            Toast.makeText(getActivity(), task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                            Log.d(TAG, "onComplete: data=>"+data);
                            hideProgressBarDialog();
                        }
                    }
                });
    }

    public boolean checkDoubleValidations(EditText editText){
        if(editText.getText().toString().trim().equals("")){
            editText.setError("Cannot be empty");
            return false;
        }else{
            return true;
        }
    }
}