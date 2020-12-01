package com.example.auctionapp;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class SignUpActivity extends AppCompatActivity {

    private FirebaseAuth mAuth;
    private ProgressDialog progressDialog;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);
        setTitle("Sign Up");

        mAuth = FirebaseAuth.getInstance();
        findViewById(R.id.buttonSignupFirst).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                EditText fname = findViewById(R.id.editTextFname);
                EditText lname = findViewById(R.id.editTextLname);
                EditText email = findViewById(R.id.editTextEmail);
                EditText pass = findViewById(R.id.editTextChoosePassword);
                EditText rePass = findViewById(R.id.editTextRepeatPassword);
                if(checkValidations(fname) && checkValidations(lname) &&
                        checkValidations(email) && checkEmailValidations(email)
                        && checkValidations(pass) && checkValidations(rePass)) {
                    String password = pass.getText().toString().trim();
                    String repeatPassword = rePass.getText().toString().trim();
                    if (password.equals(repeatPassword)) {
                        showProgressBarDialog();
                        mAuth.createUserWithEmailAndPassword(email.getText().toString().trim(), password)
                                .addOnCompleteListener(SignUpActivity.this, new OnCompleteListener<AuthResult>() {
                                    @Override
                                    public void onComplete(@NonNull Task<AuthResult> task) {
                                        if (task.isSuccessful()) {
                                            // Sign in success, update UI with the signed-in user's information
                                            Log.d("demo", "createUserWithEmail:success");
                                            Toast.makeText(SignUpActivity.this, "User Successfully created!", Toast.LENGTH_LONG).show();
                                            FirebaseUser user = mAuth.getCurrentUser();
                                            hideProgressBarDialog();
                                            Intent intent = new Intent(SignUpActivity.this, CreateAuction.class);
                                            intent.putExtra("user", user.getUid());
                                            startActivityForResult(intent, 1000);
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