package co.work.fukouka.happ.utils;

import android.app.Activity;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.media.RingtoneManager;
import android.net.Uri;
import android.support.v4.app.NotificationCompat;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import java.util.Map;

import co.work.fukouka.happ.R;
import co.work.fukouka.happ.activity.ChatRoomActivity;
import co.work.fukouka.happ.activity.DashboardActivity;
import co.work.fukouka.happ.app.HappApp;

public class MessagingService extends FirebaseMessagingService {

    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        super.onMessageReceived(remoteMessage);

        Map<String, String> params = remoteMessage.getData();

        String authorId = params.get("author_id");
        String title = params.get("title");
        String body = params.get("body");
        String chatroomId = params.get("chatroom_id");
        String chatmateId = params.get("chatmate_id");
        String photoUrl = params.get("photo_url");
        String postSkills = params.get("skills");

        //if ChatroomActivity is the currrent activity, don't show push notification
        Activity currentActivity = ((HappApp)getApplicationContext()).getCurrentActivity();

        if (chatroomId != null) {
            if (currentActivity instanceof ChatRoomActivity) {
            //do nothing
            } else {
                sendMessageNotif(title, body, chatroomId, chatmateId, photoUrl);
            }
        } else {
            sendPostNotif(title, body);
        }
    }

    @Override
    public void handleIntent(Intent intent) {
        super.handleIntent(intent);
    }

    private void sendMessageNotif(String notificationTitle, String notificationBody,
                                  String chatroomId, String chatmateId, String photoUrl) {
        Intent intent = new Intent(this, ChatRoomActivity.class);
        intent.putExtra("push_notif", true);
        intent.putExtra("chatroom_id", chatroomId);
        intent.putExtra("chatmate_id", chatmateId);
        intent.putExtra("chatmate_name", notificationTitle);
        intent.putExtra("chatmate_photoUrl", photoUrl);

        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent,
                PendingIntent.FLAG_ONE_SHOT);

        Uri defaultSoundUri= RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(this, "")
                .setAutoCancel(true)//Automatically delete the notification
                .setSmallIcon(R.mipmap.icon_notification)//Notification icon
                .setContentIntent(pendingIntent)
                .setContentTitle(notificationTitle)
                .setContentText(notificationBody)
                .setSound(defaultSoundUri)
                .setTicker(notificationTitle)
                .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setDefaults(NotificationCompat.DEFAULT_VIBRATE);

        NotificationManager notificationManager = (NotificationManager)
                getSystemService(Context.NOTIFICATION_SERVICE);

        notificationManager.notify(0, notificationBuilder.build());

    }

    private void sendPostNotif(String notificationTitle, String notificationBody) {
        Intent intent = new Intent(this, DashboardActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent,
                PendingIntent.FLAG_ONE_SHOT);

        Uri defaultSoundUri= RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(this, "")
                .setAutoCancel(true)//Automatically delete the notification
                .setSmallIcon(R.mipmap.icon_notification)//Notification icon
                .setContentIntent(pendingIntent)
                .setContentTitle(notificationTitle)
                .setContentText(notificationBody)
                .setSound(defaultSoundUri)
                .setDefaults(Notification.DEFAULT_ALL)
                .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
                .setTicker(notificationTitle)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setVibrate(new long[] { 1000, 1000, 1000, 1000, 1000 });

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
            notificationBuilder.setVisibility(Notification.VISIBILITY_PUBLIC);
        }

        NotificationManager notificationManager = (NotificationManager)
                getSystemService(Context.NOTIFICATION_SERVICE);

        notificationManager.notify(0, notificationBuilder.build());

    }

    private String getUfbId() {
        String userId = null;

        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser != null) {
            userId = currentUser.getUid();
        }

        return userId;
    }

}
