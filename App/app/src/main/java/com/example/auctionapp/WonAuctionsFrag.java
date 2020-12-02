package com.example.auctionapp;

import android.app.ProgressDialog;
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
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.functions.FirebaseFunctions;
import com.google.firebase.functions.HttpsCallableResult;
import com.google.gson.Gson;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link WonAuctionsFrag#newInstance} factory method to
 * create an instance of this fragment.
 */
public class WonAuctionsFrag extends Fragment {

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";
    private static final String TAG = "okay";
    private FirebaseFunctions mFunctions;
    private RecyclerView recyclerView;
    private RecyclerView.Adapter mAdapter;
    private RecyclerView.LayoutManager layoutManager;
    ArrayList<WonItems> wonItemsArrayList = new ArrayList<>();
    private ProgressDialog progressDialog;
    private FirebaseAuth mAuth;

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    public WonAuctionsFrag() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment WonAuctionsFrag.
     */
    // TODO: Rename and change types and number of parameters
    public static WonAuctionsFrag newInstance(String param1, String param2) {
        WonAuctionsFrag fragment = new WonAuctionsFrag();
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
        View view = inflater.inflate(R.layout.fragment_won_auctions, container, false);
        Log.d(TAG, "onCreateView: wonAuction Frag");
        return view;
    }


    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mAuth = FirebaseAuth.getInstance();
        mFunctions = FirebaseFunctions.getInstance();


        recyclerView = getView().findViewById(R.id.wonAuctionsRecyclerView);
        layoutManager = new LinearLayoutManager(getActivity());
        recyclerView.setLayoutManager(layoutManager);
        mAdapter = new AdapterWonAuctions(wonItemsArrayList);
        recyclerView.setAdapter(mAdapter);
    }

    @Override
    public void onResume() {
        super.onResume();
        Log.d(TAG, "onResume: wonAuction Frag");
        getWonItemsList();
    }

    public void getWonItemsList(){
        showProgressBarDialog();
        mFunctions.getHttpsCallable("getWonItem")
                .call()
                .addOnCompleteListener(new OnCompleteListener<HttpsCallableResult>() {
                    @Override
                    public void onComplete(@NonNull Task<HttpsCallableResult> task) {
                        if (task.isSuccessful()){
                            Log.d(TAG, "onComplete: All Auctions got successfully");
                            Toast.makeText(getActivity(), "Retrieving all the items", Toast.LENGTH_SHORT).show();
                            Gson g = new Gson();
                            String json = g.toJson(task.getResult().getData());
                            Log.d("demo",json);
                            try {
                                JSONObject root = new JSONObject(json);
                                JSONArray jsonArray = root.getJSONArray("result");
                                for(int i=0; i<jsonArray.length(); i++){
                                    JSONObject resultObject = jsonArray.getJSONObject(i);
                                    WonItems wonItems = new WonItems();
                                    wonItems.item_id = resultObject.getString("itemId");
                                    wonItems.item_price = resultObject.getDouble("item_price");
                                    wonItems.item_name = resultObject.getString("item_name");
                                    wonItems.buying_date = resultObject.getString("buying_date");
                                    wonItemsArrayList.add(wonItems);
                                }
                                Log.d("demo", "auctionItemsArrayList is ==> " + wonItemsArrayList.toString());
                            } catch (JSONException e) {
                                hideProgressBarDialog();
                                e.printStackTrace();
                            }
                            if(wonItemsArrayList.size()>0){
                                hideProgressBarDialog();
                                mAdapter.notifyDataSetChanged();
                            }else{
                                hideProgressBarDialog();
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