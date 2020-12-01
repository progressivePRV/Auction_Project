package com.example.auctionapp;

import android.util.Log;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.functions.FirebaseFunctions;
import com.google.firebase.functions.HttpsCallableResult;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import java.util.HashMap;
import java.util.Map;

public class MyFirebaseMessagingService extends FirebaseMessagingService {

    private static final String TAG = "okay_MyFirebaseMessagingService";
    private FirebaseFunctions mFunctions = FirebaseFunctions.getInstance();

    /**
     * Called if FCM registration token is updated. This may occur if the security of
     * the previous token had been compromised. Note that this is called when the
     * FCM registration token is initially generated so this is where you would retrieve
     * the token.
     */
    @Override
    public void onNewToken(String token) {
        Log.d(TAG, "onNewToken: new Token was generated");
        Log.d(TAG, "Refreshed token: " + token);

        // If you want to send messages to this application instance or
        // manage this apps subscriptions on the server side, send the
        // FCM registration token to your app server.
//        sendRegistrationToServer(token);
//        Map<String ,Object> data = new HashMap<>();
//        data.put("deviceToken",token);
//        mFunctions.getHttpsCallable("updateDeviceToken")
//                .call(data)
//                .addOnCompleteListener(new OnCompleteListener<HttpsCallableResult>() {
//                    @Override
//                    public void onComplete(@NonNull Task<HttpsCallableResult> task) {
//                        if (task.isSuccessful()){
//                            Log.d(TAG, "onComplete: device toke sent to server successful");
//                        }else{
//                            Log.d(TAG, "onComplete: error while sending token to the server"+task.getException().getStackTrace());
//                        }
//                    }
//                });
    }

    @Override
    public void onMessageReceived(@NonNull RemoteMessage remoteMessage) {
//        super.onMessageReceived(remoteMessage);
        Log.d(TAG, "onMessageReceived: notification received while app was in foreground, message was=>"+remoteMessage.getNotification().getBody());
        Log.d(TAG, "onMessageReceived: remoteMessage.getData()=>"+remoteMessage.getData());

    }
}
