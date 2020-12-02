package com.example.auctionapp;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.os.Build;
import android.util.Log;
import android.widget.Switch;

import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.functions.FirebaseFunctions;
import com.google.firebase.functions.HttpsCallableResult;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;
import com.google.gson.JsonObject;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;

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
        String title = remoteMessage.getNotification().getTitle();
        String body = remoteMessage.getNotification().getBody();
        Map<String, String> data = remoteMessage.getData();
        String channelId = remoteMessage.getNotification().getChannelId();
        Log.d(TAG, "onMessageReceived: notification received while app was in foreground, message was=>"+body);
        Log.d(TAG, "onMessageReceived: remoteMessage.getData()=>"+data);
        SendNotification(title,body,data);
//        createNotificationChannel(channelId);

//        Intent i =  new Intent();
//        i.putExtra("msg",body);
//        i.putExtra("Code",data.get("Code"));
//        i.putExtra("item_id",data.get("itemId"));
//        i.setAction("My_custom_action");
//        sendBroadcast(i);

//        Intent i =  new Intent(android.intent.action.custom_to_my_app);
//        i.putExtra("msg",body);
//        startActivity(i);
    }

    void SendNotification(String title,String body,Map<String, String> data){
        Intent notificationIntent = new Intent("this, MainActivity.class");
//        String Code = data.get("Code");
//        notificationIntent.putExtra("Code",Code);
//        notificationIntent.putExtra("item_id",data.get("itemId"));
        PendingIntent notoficationPendingIntend = PendingIntent.getActivity(getBaseContext(),0,notificationIntent,PendingIntent.FLAG_UPDATE_CURRENT);
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, getString(R.string.notification_channel_id))
                .setLargeIcon(BitmapFactory.decodeResource(getResources(),R.drawable.owver_created_auctions))
                .setSmallIcon(R.drawable.notification_icon)
                .setContentTitle(title)
                .setContentText(body)
                .setContentIntent(notoficationPendingIntend)
                .setAutoCancel(true)
                .setDefaults(NotificationCompat.DEFAULT_ALL)
                .setPriority(NotificationCompat.PRIORITY_HIGH);

        NotificationManager mNotifyManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        mNotifyManager.notify(0,builder.build());
//        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(this);
//        Random random =  new Random();
//        int random_int = random.nextInt();
// notificationId is a unique int for each notification that you must define
    }

}
