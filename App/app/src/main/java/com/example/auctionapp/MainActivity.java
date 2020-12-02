package com.example.auctionapp;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.fragment.NavHostFragment;
import androidx.navigation.ui.NavigationUI;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
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
                FirebaseAuth.getInstance().signOut();
                Toast.makeText(this, "Successfully Logged Out", Toast.LENGTH_SHORT).show();
                Intent i = new Intent(this,LoginActivity.class);
                finish();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
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

    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

//        RetriveFirebaseMessageingToken();
        CheckForIntentData();

        NavHostFragment navHostFragment = (NavHostFragment) getSupportFragmentManager().findFragmentById(R.id.nav_host_inMain);
        NavController navController = navHostFragment.getNavController();
        BottomNavigationView bottomNav = findViewById(R.id.bottomNavigationView_in_main);
        NavigationUI.setupWithNavController(bottomNav, navController);


    }

    private void CheckForIntentData() {
        Intent i = getIntent();
        if(i!=null){
            Log.d(TAG, "CheckForIntentData: intent was not empty in main activity");

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
}