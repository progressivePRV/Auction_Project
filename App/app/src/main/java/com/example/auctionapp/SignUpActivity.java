package com.example.auctionapp;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
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

public class SignUpActivity extends AppCompatActivity {

    private static final String TAG = "okay_SignUpActivity";
    private FirebaseAuth mAuth;
    private ProgressDialog progressDialog;
//    MyViewModel myViewModel;
    EditText fname_et,lname_et,email_et,pass_et,cash_et,rePass_et;
    private FirebaseFunctions mFunctions = FirebaseFunctions.getInstance();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);
        setTitle("Sign Up");

//        myViewModel = new ViewModelProvider(this).get(MyViewModel.class);
        mAuth = FirebaseAuth.getInstance();
        findViewById(R.id.buttonSignupFirst).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                 fname_et = findViewById(R.id.editTextFname);
                 lname_et = findViewById(R.id.editTextLname);
                 email_et = findViewById(R.id.editTextEmail);
                 pass_et = findViewById(R.id.editTextChoosePassword);
                 rePass_et = findViewById(R.id.editTextRepeatPassword);

                if(checkValidations(fname_et) && checkValidations(lname_et) &&
                        checkValidations(email_et) && checkEmailValidations(email_et)
                        && checkValidations(pass_et) && checkValidations(rePass_et)) {
                    String password = pass_et.getText().toString().trim();
                    String repeatPassword = rePass_et.getText().toString().trim();
                    if (password.equals(repeatPassword)) {
                        showProgressBarDialog();
                        mAuth.createUserWithEmailAndPassword(email_et.getText().toString().trim(), password)
                                .addOnCompleteListener(SignUpActivity.this, new OnCompleteListener<AuthResult>() {
                                    @Override
                                    public void onComplete(@NonNull Task<AuthResult> task) {
                                        if (task.isSuccessful()) {
                                            // Sign in success, update UI with the signed-in user's information
                                            Log.d("demo", "createUserWithEmail:success");
                                            Toast.makeText(SignUpActivity.this, "User Successfully created!", Toast.LENGTH_LONG).show();
                                            FirebaseUser user = mAuth.getCurrentUser();
                                            hideProgressBarDialog();
                                            Intent intent = new Intent(SignUpActivity.this, MainActivity.class);
                                            startActivity(intent);
                                            CallCreateUser();
                                            finish();
//                                            intent.putExtra("user", user.getUid());
//                                            startActivityForResult(intent, 1000);
                                        } else {
                                            // If sign in fails, display a message to the user.
                                            Log.w("demo", "createUserWithEmail:failure", task.getException());
                                            Toast.makeText(SignUpActivity.this, "Create user failed!" + task.getException(),
                                                    Toast.LENGTH_SHORT).show();
                                            //updateUI(null);
                                            hideProgressBarDialog();
                                        }
                                    }
                                });
                    } else {
                        Toast.makeText(SignUpActivity.this, "Both passwords should match", Toast.LENGTH_SHORT).show();
                    }
                    hideProgressBarDialog();
                }
            }
        });

        findViewById(R.id.buttonCancel).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }

    private void CallCreateUser() {
        Log.d(TAG, "CallCreateUser: called");
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
                        Log.d(TAG, "onComplete: new toke retrived from FirebaseMessageingToken=>"+token);
                        Map<String ,Object> data = new HashMap<>();
                        data.put("firstName",fname_et.getText().toString());
                        data.put("lastName",lname_et.getText().toString());
                        data.put("balance",200);
                        data.put("deviceToken",token);
//                        data.put("uid","a1");
                        mFunctions.getHttpsCallable("createUser")
                                .call(data)
                                .addOnCompleteListener(new OnCompleteListener<HttpsCallableResult>() {
                                    @Override
                                    public void onComplete(@NonNull Task<HttpsCallableResult> task) {
                                        if (task.isSuccessful()){
                                            Log.d(TAG, "onComplete: device toke sent to server successful");
                                        }else{
                                            Log.d(TAG, "onComplete: error while sending token to the server"+task.getException().getMessage());
                                            Log.d(TAG, "onComplete: data=>"+data);
                                        }
                                    }
                                });
                    }
                });

//        Map<String ,Object> data = new HashMap<>();
//        createUser({
//    "firstName":"aditi",
//    "rojatkar":"rojatkar",
//    "balance":200
//})

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == 1000 && resultCode == 2000){
            finish();
        }
    }

    public void showProgressBarDialog()
    {
        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Loading");
        progressDialog.setCancelable(false);
        progressDialog.show();
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
        return true;
    }
    public boolean checkValidations(EditText editText){
        if(editText.getText().toString().trim().equals("")){
            editText.setError("Cannot be empty");
            return false;
        }
        return true;
    }
}