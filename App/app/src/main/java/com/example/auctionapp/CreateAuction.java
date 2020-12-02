package com.example.auctionapp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.functions.FirebaseFunctions;
import com.google.firebase.functions.HttpsCallableResult;

import java.util.HashMap;
import java.util.Map;

public class CreateAuction extends AppCompatActivity {

    private static final String TAG = "demo";
    EditText createAuctionItemName, createAuctionStartBid, createAuctionMinFinalBid;
    private FirebaseFunctions mFunctions;
    private ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_auction);

        mFunctions = FirebaseFunctions.getInstance();
        createAuctionItemName = findViewById(R.id.createAuctionItemName);
        createAuctionStartBid = findViewById(R.id.createAuctionStartBid);
        createAuctionMinFinalBid = findViewById(R.id.createAuctionMinFinalBid);

        findViewById(R.id.createAuctionPostItem).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(checkValidations(createAuctionItemName)){
                    if(checkDoubleValidations(createAuctionStartBid) && checkDoubleValidations(createAuctionMinFinalBid)){
                        showProgressBarDialog();
                        Map<String ,Object> data = new HashMap<>();
                        data.put("itemName",createAuctionItemName.getText().toString());
                        data.put("startBid",Double.parseDouble(createAuctionStartBid.getText().toString()));
                        data.put("minFinalBid",Double.parseDouble(createAuctionMinFinalBid.getText().toString()));

                        mFunctions.getHttpsCallable("postNewItem")
                                .call(data)
                                .addOnCompleteListener(new OnCompleteListener<HttpsCallableResult>() {
                                    @Override
                                    public void onComplete(@NonNull Task<HttpsCallableResult> task) {
                                        if (task.isSuccessful()){
                                            Log.d(TAG, "onComplete: Post New Item to the server succesful");
                                            Toast.makeText(CreateAuction.this, "Posted Item Successfully", Toast.LENGTH_SHORT).show();
                                            createAuctionMinFinalBid.setText("");
                                            createAuctionStartBid.setText("");
                                            createAuctionItemName.setText("");
                                            hideProgressBarDialog();
                                        }else{
                                            Log.d(TAG, "onComplete: error while Post New Item to the serverr"+task.getException().getMessage());
                                            Log.d(TAG, "onComplete: data=>"+data);
                                            Toast.makeText(CreateAuction.this, "Some error occurred internally", Toast.LENGTH_SHORT).show();
                                            hideProgressBarDialog();
                                        }
                                    }
                                });
                    }
                }
            }
        });
    }

    public boolean checkValidations(EditText editText){
        if(editText.getText().toString().trim().equals("")){
            editText.setError("Cannot be empty");
            return false;
        }
        return true;
    }

    public boolean checkDoubleValidations(EditText editText){
        if(editText.getText().toString().trim().equals("")){
            editText.setError("Cannot be empty");
            return false;
        }else if(Double.parseDouble(editText.getText().toString()) <= 0){
            editText.setError("Should be atleast $1.00");
            return false;
        }else{
            return true;
        }
    }

    public void showProgressBarDialog()
    {
        progressDialog = new ProgressDialog(CreateAuction.this);
        progressDialog.setMessage("Loading");
        progressDialog.setCancelable(false);
        progressDialog.show();
    }

    public void hideProgressBarDialog()
    {
        progressDialog.dismiss();
    }
}