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

public class AddMoney extends AppCompatActivity {

    private static final String TAG = "demo";
    private EditText textAddCashAmount;
    private FirebaseFunctions mFunctions;
    private ProgressDialog progressDialog;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_money);

        mFunctions = FirebaseFunctions.getInstance();
        textAddCashAmount = findViewById(R.id.textAddCashAmount);

        findViewById(R.id.buttonAddCash).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(checkValidations(textAddCashAmount)){
                    Map<String ,Object> data = new HashMap<>();
                    data.put("amount",Double.parseDouble(textAddCashAmount.getText().toString()));
                    showProgressBarDialog();
                    mFunctions.getHttpsCallable("addBalance")
                            .call(data)
                            .addOnCompleteListener(new OnCompleteListener<HttpsCallableResult>() {
                                @Override
                                public void onComplete(@NonNull Task<HttpsCallableResult> task) {
                                    if (task.isSuccessful()){
                                        Log.d(TAG, "onComplete: device add cash to the server successful");
                                        Toast.makeText(AddMoney.this, "Money Added Successfully", Toast.LENGTH_SHORT).show();
                                        textAddCashAmount.setText("");
                                        hideProgressBarDialog();
                                    }else{
                                        Log.d(TAG, "onComplete: error while sending add cash to the server"+task.getException().getMessage());
                                        Log.d(TAG, "onComplete: data=>"+data);
                                        Toast.makeText(AddMoney.this, "Some error occurred internally", Toast.LENGTH_SHORT).show();
                                        hideProgressBarDialog();
                                    }
                                }
                            });
                }
            }
        });

        findViewById(R.id.buttonAddCashCancel).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

    }

    public boolean checkValidations(EditText editText){
        if(editText.getText().toString().trim().equals("")){
            editText.setError("Cannot be empty");
            return false;
        }else if(Double.parseDouble(textAddCashAmount.getText().toString()) < 1){
            editText.setError("Should be atleast $1.00");
            return false;
        }else{
            return true;
        }
    }

    public void showProgressBarDialog()
    {
        progressDialog = new ProgressDialog(AddMoney.this);
        progressDialog.setMessage("Loading");
        progressDialog.setCancelable(false);
        progressDialog.show();
    }

    public void hideProgressBarDialog()
    {
        progressDialog.dismiss();
    }
}