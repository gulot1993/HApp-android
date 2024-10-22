package co.work.fukouka.happ.service;

import android.app.IntentService;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;

public class BadgeIntentService extends IntentService {

    private int notificationId = 0;

    public BadgeIntentService() {
        super("BadgeIntentService");
    }

    private NotificationManager mNotificationManager;

    @Override
    public void onStart(Intent intent, int startId) {
        super.onStart(intent, startId);
        mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
    }

    @Override
    protected void onHandleIntent(Intent intent) {
//        if (intent != null) {
//            int badgeCount = intent.getIntExtra("badgeCount", 0);
//
//            mNotificationManager.cancel(notificationId);
//            notificationId++;
//
//            Notification.Builder builder = new Notification.Builder(getApplicationContext())
//                    .setContentTitle("")
//                    .setContentText("")
//                    .setSmallIcon(R.mipmap.ic_icon);
//            Notification notification = builder.build();
//            ShortcutBadger.applyNotification(getApplicationContext(), notification, badgeCount);
//            mNotificationManager.notify(notificationId, notification);
//        } else {
//            System.out.println("Triggered here");
//        }
    }
}
