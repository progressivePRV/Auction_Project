package com.example.auctionapp;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.common.reflect.TypeToken;
import com.google.firebase.functions.FirebaseFunctions;
import com.google.firebase.functions.HttpsCallableResult;
import com.google.gson.Gson;
import com.google.gson.JsonObject;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link AllAuctionFrag#newInstance} factory method to
 * create an instance of this fragment.
 */
public class AllAuctionFrag extends Fragment {

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

        mFunctions = FirebaseFunctions.getInstance();

        recyclerView = getView().findViewById(R.id.allAuctionsRecyclerView);
        layoutManager = new LinearLayoutManager(getActivity());
        recyclerView.setLayoutManager(layoutManager);
        mAdapter = new AdapterAllAuctions(auctionItemsArrayList, getActivity());
        recyclerView.setAdapter(mAdapter);

        getView().findViewById(R.id.allAuctionsPostNewItemButton).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getActivity(), CreateAuction.class);
                startActivity(intent);
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
                                    if(auctionItems.auction_status.equals("created")){
                                        auctionItems.current_highest_bid = 0.0;
                                    }else{
                                        auctionItems.current_highest_bid = dataObject.getDouble("current_highest_bid");
                                    }
                                    auctionItemsArrayList.add(auctionItems);
                                }
                                Log.d("demo", "auctionItemsArrayList is ==> " + auctionItemsArrayList.toString());
                            } catch (JSONException e) {
                                hideProgressBarDialog();
                                e.printStackTrace();
                            }
                            if(auctionItemsArrayList.size()>0){
                                hideProgressBarDialog();
                                mAdapter.notifyDataSetChanged();
                            }else{
                                hideProgressBarDialog();
                                Toast.makeText(getActivity(), "Sorry No Auction available at this moment", Toast.LENGTH_SHORT).show();
                            }
                        }else{
                            hideProgressBarDialog();
                            Log.d(TAG, "onComplete: All Auctions error occurred"+task.getException().getMessage());
                            Toast.makeText(getActivity(), "Some error occurred internally", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }

    @Override
    public void onResume() {
        super.onResume();
        Log.d(TAG, "onResume: in allAuction Frag");
        getAllAuctions();
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
}