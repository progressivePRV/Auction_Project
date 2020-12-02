package com.example.auctionapp;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.DialogFragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.fragment.NavHostFragment;
import androidx.navigation.ui.NavigationUI;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.Switch;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.functions.FirebaseFunctions;
import com.google.firebase.functions.FirebaseFunctionsException;
import com.google.firebase.functions.HttpsCallableResult;
import com.google.firebase.messaging.FirebaseMessaging;

import java.util.HashMap;
import java.util.Map;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "okay_MainActivity";
    private static final int GOOGLE_API_REQUEST_RESULT = 1111;
    BroadcastReceiver mbroadcastReceiver;
    BottomNavigationView bottomNav;
    private FirebaseFunctions mFunctions;
    private ProgressDialog progressDialog;


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.toolbar_menu_in_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case R.id.cash_menu:
                Log.d(TAG, "onOptionsItemSelected: open add cash activity");
                Intent intent = new Intent(MainActivity.this, AddMoney.class);
                startActivity(intent);
                return true;
            case R.id.logout:
                showProgressBarDialog();
                CallDeleteDeviceToken();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void CallDeleteDeviceToken() {
        mFunctions.getHttpsCallable("deleteDeviceToken")
                .call()
                .addOnCompleteListener(new OnCompleteListener<HttpsCallableResult>() {
                    @Override
                    public void onComplete(@NonNull Task<HttpsCallableResult> task) {
                        if (task.isSuccessful()){
                            FirebaseAuth.getInstance().signOut();
                            Toast.makeText(MainActivity.this, "Successfully Logged Out", Toast.LENGTH_SHORT).show();
                            Intent i = new Intent(MainActivity.this,LoginActivity.class);
                            startActivity(i);
                            finish();
                        }else{
                            Toast.makeText(MainActivity.this, "Loggout failed", Toast.LENGTH_SHORT).show();
                        }
                        hideProgressBarDialog();
                    }
                });

    }

    @Override
    protected void onResume() {
        super.onResume();
        // check if play service is available or not
        if (isGooglePlayServicesAvailable()){
            Log.d(TAG, "onResume: play service is not active");
        }else{
            Log.d(TAG, "onResume: play service are active");
        }

        // also look for broadcasts
//        IntentFilter intentFilter =  new IntentFilter("My_custom_action");
//        Log.d(TAG, "onResume: seting up the new broadcast receiver");
//        mbroadcastReceiver = new BroadcastReceiver() {
//            @Override
//            public void onReceive(Context context, Intent intent) {
//                Log.d(TAG, "onReceive: msg received in mainactivity");
//                String msg = intent.getStringExtra("msg");
//                AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
//                builder.setMessage(msg)
//                        .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
//                            public void onClick(DialogInterface dialog, int id) {
//                                // FIRE ZE MISSILES!
//                                Log.d(TAG, "onClick: user clicked ok for alert");
//                            }
//                        }).setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
//                    @Override
//                    public void onClick(DialogInterface dialog, int which) {
//                        Log.d(TAG, "onClick: user canceled the alert from notification");
//                    }
//                });
//                builder.create();
//                builder.show();
//            }
//        };
//        Log.d(TAG, "onResume: registering it");
//        this.registerReceiver(mbroadcastReceiver,intentFilter);
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (mbroadcastReceiver!=null){
            this.unregisterReceiver(this.mbroadcastReceiver);
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mFunctions = FirebaseFunctions.getInstance();

//        RetriveFirebaseMessageingToken();

        NavHostFragment navHostFragment = (NavHostFragment) getSupportFragmentManager().findFragmentById(R.id.nav_host_inMain);
        NavController navController = navHostFragment.getNavController();
        bottomNav = findViewById(R.id.bottomNavigationView_in_main);
        NavigationUI.setupWithNavController(bottomNav, navController);

        CheckForIntentData();

    }

    private void CheckForIntentData() {
        Log.d(TAG, "CheckForIntentData: called");
//          101 -> someone bid higher than you
//          102 -> you are the highest bidder
//          103 -> min final bid reached
//          104 -> bid settled
        //  105 -> Auction no longer exists
        Intent i = getIntent();
        Log.d(TAG, "CheckForIntentData: getting code=>"+i.getStringExtra("Code"));
        if(i.hasExtra("Code")){
            switch (i.getStringExtra("Code")){
                case "101":
                case "102":
                    bottomNav.setSelectedItemId(R.id.currentBidFrag);
                    return;
                case "103":
                    bottomNav.setSelectedItemId(R.id.auctionPlacedFrag);
                    return;
                case "104":
                    bottomNav.setSelectedItemId(R.id.wonAuctionsFrag);
                    return;
                default:
                    Log.d(TAG, "CheckForIntentData: hit the default case for intent data with code");
            }
        }
    }


    public boolean isGooglePlayServicesAvailable() {
        GoogleApiAvailability googleApiAvailability = GoogleApiAvailability.getInstance();
        int status = googleApiAvailability.isGooglePlayServicesAvailable(this);
        if(status != ConnectionResult.SUCCESS) {
            if(googleApiAvailability.isUserResolvableError(status)) {
                googleApiAvailability.getErrorDialog(this, status, GOOGLE_API_REQUEST_RESULT).show();
            }
            return false;
        }
        return true;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK && requestCode == GOOGLE_API_REQUEST_RESULT){
            Log.d(TAG, "onActivityResult: google api/play service was enabled");
        }else if(resultCode == RESULT_CANCELED && requestCode == GOOGLE_API_REQUEST_RESULT){
            Log.d(TAG, "onActivityResult: google api/play service was disabled");
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

}