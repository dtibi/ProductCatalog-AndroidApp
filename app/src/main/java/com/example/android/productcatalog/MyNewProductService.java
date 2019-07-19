package com.example.android.productcatalog;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;

import androidx.core.app.NotificationCompat;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;


 // REQ #9 service for showing notifications when new products are created!
public class MyNewProductService extends FirebaseMessagingService {
    private static String TAG = "service_tibi";
    public static String CHANNEL_ID = "main_channel";

    public MyNewProductService() {
    }

    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {

        Log.d(TAG, "From: " + remoteMessage.getFrom());

        // Check if message contains a data payload.
        if (remoteMessage.getData().size() > 0) {
            Log.d(TAG, "Message data payload: " + remoteMessage.getData());
        }
        String body = remoteMessage.getData().get("body");
        String title = remoteMessage.getData().get("title");
        Log.d(TAG, "Message Notification Title: " + body);
        Log.d(TAG, "Message Notification Body: " + title);
        NotificationCompat.Builder mBuilder =   new NotificationCompat.Builder(this,CHANNEL_ID)
                .setSmallIcon(R.drawable.common_google_signin_btn_icon_dark_normal_background) // notification icon
                .setContentTitle(title) // title for notification
                .setContentText(body) // message for notification
                .setAutoCancel(true); // clear notification after click
        Intent intent = new Intent(this, MainActivity.class);
        PendingIntent pi = PendingIntent.getActivity(this,0,intent,PendingIntent.FLAG_UPDATE_CURRENT);
        mBuilder.setContentIntent(pi);
        NotificationManager mNotificationManager =
                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        if (mNotificationManager != null) {
            mNotificationManager.notify(0, mBuilder.build());
        }
    }
}
