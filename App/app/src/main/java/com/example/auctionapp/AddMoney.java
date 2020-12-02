package com.example.auctionapp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.functions.FirebaseFunctions;
import com.google.firebase.functions.HttpsCallableResult;
import com.google.gson.Gson;
import com.google.gson.JsonObject;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class AddMoney extends AppCompatActivity {

    private static final String TAG = "demo";
    private EditText textAddCashAmount;
    private FirebaseFunctions mFunctions;
    private ProgressDialog progressDialog;
    private TextView addMoneyBalanceAmount, addMoneyHoldAmount;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_money);

        mFunctions = FirebaseFunctions.getInstance();
        textAddCashAmount = findViewById(R.id.textAddCashAmount);

        addMoneyBalanceAmount = findViewById(R.id.addMoneyBalanceAmount);
        addMoneyHoldAmount = findViewById(R.id.addMoneyHoldAmount);

        getUsersAmount();

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
                                        finish();
                                        hideProgressBarDialog();
                                    }else{
                                        Log.d(TAG, "onComplete: error while sending add cash to the server"+task.getException().getMessage());
                                        Log.d(TAG, "onComplete: data=>"+data);
                                        Toast.makeText(AddMoney.this, task.getException().getMessage(), Toast.LENGTH_SHORT).show();
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

    private void getUsersAmount() {
        showProgressBarDialog();
        mFunctions.getHttpsCallable("getUser")
                .call()
                .addOnCompleteListener(new OnCompleteListener<HttpsCallableResult>() {
                    @Override
                    public void onComplete(@NonNull Task<HttpsCallableResult> task) {
                        if (task.isSuccessful()){
                            Log.d(TAG, "onComplete: device add cash to the server successful");
                            Gson g = new Gson();
                            String json = g.toJson(task.getResult().getData());
                            Log.d("demo",json);
                            try {
                                JSONObject root = new JSONObject(json);
                                JSONObject result = root.getJSONObject("result");
                                Log.d("demo", result.toString());
                                addMoneyBalanceAmount.setText("$ "+result.getDouble("balance"));
                                addMoneyHoldAmount.setText("$ "+result.getDouble("hold"));
                            } catch (JSONException e) {
                                hideProgressBarDialog();
                                e.printStackTrace();
                            }
                            hideProgressBarDialog();
                        }else{
                            Log.d(TAG, "onComplete: error while sending add cash to the server"+task.getException().getMessage());
                            Toast.makeText(AddMoney.this, task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                            hideProgressBarDialog();
                        }
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