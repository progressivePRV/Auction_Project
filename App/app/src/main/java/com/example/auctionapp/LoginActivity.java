package com.example.auctionapp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.functions.FirebaseFunctions;
import com.google.firebase.functions.HttpsCallableResult;
import com.google.firebase.messaging.FirebaseMessaging;

import java.util.HashMap;
import java.util.Map;

public class LoginActivity extends AppCompatActivity {

    private FirebaseAuth mAuth;
    private FirebaseFunctions mFunctions;
    private ProgressDialog progressDialog;
    private static final String TAG = "okay_LoginActivity";

    @Override
    protected void onStart() {
        super.onStart();
        showProgressBarDialog();
        mAuth = FirebaseAuth.getInstance();
        FirebaseUser currentUser = mAuth.getCurrentUser();
        // updateUI(currentUser);
        if(currentUser!=null){
            Log.d("demo","CurrentUser" + currentUser.getDisplayName());
            hideProgressBarDialog();
            Intent intent = new Intent(LoginActivity.this, MainActivity.class);
            intent.putExtra("user",currentUser.getUid());
            startActivity(intent);
            finish();
        } else{
            Log.d("demo","Please login to go see your contacts");
            hideProgressBarDialog();
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        setTitle("Login");
        CheckForIntentData();
        mAuth = FirebaseAuth.getInstance();
        mFunctions = FirebaseFunctions.getInstance();

        findViewById(R.id.buttonLogin).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                EditText emailTextView = findViewById(R.id.loginText);
                EditText passwordTextView = findViewById(R.id.passwordText);
                if(checkValidations(emailTextView) && checkValidations(passwordTextView) && checkEmailValidations(emailTextView)){
                    showProgressBarDialog();
                    mAuth.signInWithEmailAndPassword(emailTextView.getText().toString().trim(), passwordTextView.getText().toString().trim())
                            .addOnCompleteListener(LoginActivity.this, new OnCompleteListener<AuthResult>() {
                                @Override
                                public void onComplete(@NonNull Task<AuthResult> task) {
                                    if (task.isSuccessful()) {
                                        // Sign in success, update UI with the signed-in user's information
                                        Log.d("demo", "signInWithEmail:success");
                                        FirebaseUser user = mAuth.getCurrentUser();
                                        //updateUI(user);
                                        hideProgressBarDialog();
                                        Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                                        intent.putExtra("user",user.getUid());
                                        startActivity(intent);
                                        SendRegistrationToServer();
                                        finish();
                                    } else {
                                        // If sign in fails, display a message to the user.
                                        Log.w("demo", "signInWithEmail:failure", task.getException());
                                        Toast.makeText(LoginActivity.this, "Authentication failed.",
                                                Toast.LENGTH_SHORT).show();
                                        //updateUI(null);
                                        hideProgressBarDialog();
                                        // ...
                                    }
                                }
                            });
                }
            }
        });

        findViewById(R.id.buttonSignUp).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(LoginActivity.this, SignUpActivity.class);
                startActivity(intent);
            }
        });
    }

    private void SendRegistrationToServer() {

        FirebaseMessaging.getInstance().getToken()
                .addOnCompleteListener(new OnCompleteListener<String>() {
                    @Override
                    public void onComplete(@NonNull Task<String> task) {
                        if (!task.isSuccessful()) {
                            Log.d(TAG, "onComplete: error while generating firebase messaging token");
                            //Log.w(TAG, "Fetching FCM registration token failed", task.getException());
                            return;
                        }
                        // Get new FCM registration token
                        String token = task.getResult();
                        Map<String, Object> data = new HashMap<>();
                        data.put("deviceToken", token);
                        mFunctions.getHttpsCallable("updateDeviceToken")
                                .call(data)
                                .continueWith(new Continuation<HttpsCallableResult, Object>() {
                                    @Override
                                    public Object then(@NonNull Task<HttpsCallableResult> task) throws Exception {
                                        String result = (String) task.getResult().getData();
                                        Log.d(TAG, "then: result in continue with");
                                        return null;
                                    }
                                });
//                                .addOnCompleteListener(new OnCompleteListener<HttpsCallableResult>() {
//                                    @Override
//                                    public void onComplete(@NonNull Task<HttpsCallableResult> task) {
//                                        if (task.isSuccessful()) {
//                                            Log.d(TAG, "onComplete: device toke sent to server successful");
//                                        } else {
//                                            Log.d(TAG, "onComplete: error while sending token to the server" + task.getException().getStackTrace());
//                                        }
//                                    }
//                                });
                    }
                });

    }

    public void showProgressBarDialog()
    {
        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Loading");
        progressDialog.setCancelable(false);
        progressDialog.show();
    }

    private void CheckForIntentData() {
        Intent i = getIntent();
        if(i!=null){
            Log.d(TAG, "CheckForIntentData: intent was not empty in login Activity");
            Log.d(TAG, "CheckForIntentData: intent action=>"+i.getAction());
            Log.d(TAG, "CheckForIntentData: intent data one=>"+i.getStringExtra("one"));
        }
    }

    public void hideProgressBarDialog()
    {
        progressDialog.dismiss();
    }

    public boolean checkEmailValidations(EditText editText)
    {
        String emailPattern = "[a-zA-Z0-9._-]+@[a-z]+\\.+[a-z]+";
        if(!editText.getText().toString().trim().matches(emailPattern))
        {
            editText.setError("Invalid Email");
            return false;
        }
        else
        {
            return true;
        }
    }
    public boolean checkValidations(EditText editText){
        if(editText.getText().toString().trim().equals("")){
            editText.setError("Cannot be empty");
            return false;
        }else{
            return true;
        }
    }
}