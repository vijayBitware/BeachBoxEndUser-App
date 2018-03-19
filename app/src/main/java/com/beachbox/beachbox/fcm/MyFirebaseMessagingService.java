package com.beachbox.beachbox.fcm;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Handler;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import com.beachbox.beachbox.Config.Config;
import com.beachbox.beachbox.R;
import com.beachbox.beachbox.User.Activities.UDashboardActivityNew;
import com.beachbox.beachbox.User.Fragments.FragmentOrderStatus;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

/**
 * Created by bitware on 27/7/17.
 */

public class MyFirebaseMessagingService extends FirebaseMessagingService {
    SharedPreferences sharedPreferences;

    SharedPreferences.Editor editor;
    private static final String TAG = "MyFirebaseMsgService";
   // SharedPreferences sharedPreferences = getSharedPreferences("MyPref",MODE_PRIVATE);
   // SharedPreferences.Editor editor = sharedPreferences.edit();

    /**
     * Called when message is received.
     *
     * @param remoteMessage Object representing the message received from Firebase Cloud Messaging.
     */
    // [START receive_message]
    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        // [START_EXCLUDE]
        // There are two types of messages data messages and notification messages. Data messages are handled
        // here in onMessageReceived whether the app is in the foreground or background. Data messages are the type
        // traditionally used with GCM. Notification messages are only received here in onMessageReceived when the app
        // is in the foreground. When the app is in the background an automatically generated notification is displayed.
        // When the user taps on the notification they are returned to the app. Messages containing both notification
        // and data payloads are treated as notification messages. The Firebase console always sends notification
        // messages. For more see: https://firebase.google.com/docs/cloud-messaging/concept-options
        // [END_EXCLUDE]

        // TODO(developer): Handle FCM messages here.
        // Not getting messages here? See why this may be: https://goo.gl/39bRNJ

        // Also if you intend on generating your own notifications as a result of a received FCM
        // message, here is where that should be initiated. See sendNotification method below.
       // System.out.println("##  getData() :"+remoteMessage.getData());
       // System.out.println("##  getNotification() :"+remoteMessage.getNotification().getBody());

        System.out.println("In onMessageReceived >>>> ");
        sendNotification(remoteMessage.getData().get("body"));

    }
    // [END receive_message]

    /**
     * Schedule a job using FirebaseJobDispatcher.
     */
   /* private void scheduleJob() {
        // [START dispatch_job]
        FirebaseJobDispatcher dispatcher = new FirebaseJobDispatcher(new GooglePlayDriver(this));
        Job myJob = dispatcher.newJobBuilder()
                .setService(MyJobService.class)
                .setTag("my-job-tag")
                .build();
        dispatcher.schedule(myJob);
        // [END dispatch_job]
    }*/

    /**
     * Handle time allotted to BroadcastReceivers.
     */
    private void handleNow() {
        Log.d(TAG, "Short lived task is done.");
    }

    /**
     * Create and show a simple notification containing the received FCM message.
     *
     * @param messageBody FCM message body received.
     */
    private void sendNotification(String messageBody) {
        Config.notiFlag = "Yes";
        System.out.println("********msg****"+messageBody);

        /*if(messageBody.contains("on the way"))
        {
            System.out.println("*****not*****");
            sharedPreferences = this.getSharedPreferences("MyPref", MODE_PRIVATE);
            editor = sharedPreferences.edit();
            //sharedPreferences.getString("orderStatus", "");
            editor.putString("orderStatus","on the way");
            Intent intent = new Intent(this, FragmentOrderStatus.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            PendingIntent pendingIntent = PendingIntent.getActivity(this, 0 *//* Request code *//*, intent,
                    PendingIntent.FLAG_ONE_SHOT);

            Uri defaultSoundUri= RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
            NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(this)
                    .setSmallIcon(R.drawable.notiiocn)
                    .setContentTitle("BeachBox Enterprise")
                    .setContentText(messageBody)
                    .setAutoCancel(true)
                    .setSound(defaultSoundUri)
                    .setVibrate(new long[] { 300, 300, 300, 300, 300 })
                    .setContentIntent(pendingIntent);

            NotificationManager notificationManager =
                    (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

            notificationManager.notify((int) System.currentTimeMillis(), notificationBuilder.build());
        }
*/
        Intent intent = new Intent(this, UDashboardActivityNew.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0 /* Request code */, intent,
                PendingIntent.FLAG_ONE_SHOT);

        Uri defaultSoundUri= RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(this)
                .setSmallIcon(R.drawable.notiiocn)
                .setContentTitle("BeachBox Enterprise")
                .setContentText(messageBody)
                .setAutoCancel(true)
                 .setSound(defaultSoundUri)
                .setVibrate(new long[] { 300, 300, 300, 300, 300 })
                 .setContentIntent(pendingIntent);

        NotificationManager notificationManager =
                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        notificationManager.notify((int) System.currentTimeMillis(), notificationBuilder.build());
    }
}
