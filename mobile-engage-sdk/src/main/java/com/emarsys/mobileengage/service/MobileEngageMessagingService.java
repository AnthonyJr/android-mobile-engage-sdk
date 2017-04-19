package com.emarsys.mobileengage.service;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.NotificationCompat;

import com.emarsys.mobileengage.R;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import java.util.Map;

public class MobileEngageMessagingService extends FirebaseMessagingService {

    public static final int NOTIFICATION_ICON = R.mipmap.ic_launcher;
    public static final String MESSAGE_FILTER = "ems-me";

    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        super.onMessageReceived(remoteMessage);

        if (remoteMessage.getData().size() > 0 && remoteMessage.getData().containsKey(MESSAGE_FILTER)) {

            Intent intent = createIntent(remoteMessage);
            Notification notification = createNotification(remoteMessage, intent);

            ((NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE))
                    .notify(42, notification);
        }
    }

    @NonNull
    private Notification createNotification(RemoteMessage remoteMessage, Intent intent) {
        NotificationCompat.Builder mBuilder =
                new NotificationCompat.Builder(this)
                        .setSmallIcon(NOTIFICATION_ICON)
                        .setContentTitle(remoteMessage.getData().get("title"));

        PendingIntent resultPendingIntent = PendingIntent.getActivity(this, 0, intent, 0);
        mBuilder.setContentIntent(resultPendingIntent);
        return mBuilder.build();
    }

    @NonNull
    private Intent createIntent(RemoteMessage remoteMessage) {
        String packageName = getApplicationContext().getPackageName();
        Intent intent = getPackageManager().getLaunchIntentForPackage(packageName);
        Bundle bundle = new Bundle();
        for (Map.Entry<String, String> entry : remoteMessage.getData().entrySet()) {
            bundle.putString(entry.getKey(), entry.getValue());
        }
        intent.putExtra("payload", bundle);
        return intent;
    }
}
