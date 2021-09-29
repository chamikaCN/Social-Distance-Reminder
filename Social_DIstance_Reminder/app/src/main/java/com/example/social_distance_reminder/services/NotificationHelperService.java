package com.example.social_distance_reminder.services;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.IBinder;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import com.example.social_distance_reminder.R;
import com.example.social_distance_reminder.exceptions.NotificationManagerException;

import static com.example.social_distance_reminder.helper.RandomIDGenerator.getNotifictionID;

public class NotificationHelperService extends Service {
    private Context context;

    private static NotificationManager notificationManager = null;

    public NotificationHelperService(Context context) {
        this.context = context;
    }

    private static NotificationHelperService notificationHelperService = null;

    private static boolean isNormalNotificationChannelActive = false;
    private static boolean identifiedNotificationChannelActive = false;

    private static int normalNotoficationLockScreenVisiblity = Notification.VISIBILITY_PRIVATE;
    private static int normalNotificationLightColor = Color.BLUE;
    private static String normalNotificationID= "NORMAL_NOTIFICATION_CHANNEL_ID";
    private static CharSequence normalNotificationChannelName = "NORMAL_NOTIFICATION_CHANNEL_NAME";
    private static String normalNotificationChannelDescription = "NORMAL_NOTIFICATION_CHANNEL_DESCRIPTION";
    private static int normalNotificationChannelImportance = NotificationManager.IMPORTANCE_DEFAULT;
    private static Uri normalNotificationChannelSound = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);

    private static int identifiedNotoficationLockScreenVisiblity = Notification.VISIBILITY_PUBLIC;
    private static int identifiedNotificationLightColor = Color.BLUE;
    private static String identifiedNotificationID = "IDENTIFIED_NOTIFICATION_CHANNEL_ID";
    private static CharSequence identifiedNotificationChannelName = "IDENTIFIED_NOTIFICATION_CHANNEL_NAME";
    private static String identifiedNotificationChannelDescription = "IDENTIFIED_NOTIFICATION_CHANNEL_DESCRIPTION";
    private static int identifiedNotificationChannelImportance = NotificationManager.IMPORTANCE_HIGH;
    private static Uri identifiedNotificationChannelSound = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);

    private NotificationManager createNotificationManager() {
        if (notificationManager == null) {
            notificationManager = (NotificationManager) this.context.getSystemService(NotificationManager.class);
        }
        return notificationManager;
    }

    public static NotificationHelperService getInstance(Context context) {
        if (notificationHelperService == null || notificationHelperService.context != context) {
            notificationHelperService = new NotificationHelperService(context);
        }
        return notificationHelperService;
    }

    private void showNormalNotification(String textTitle, String textContent) throws Exception {
        this.createNormalNotificationChannel();
        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(this.context, normalNotificationID)
                .setSmallIcon(R.drawable.common_google_signin_btn_icon_dark)
                .setContentTitle(textTitle)
                .setContentText(textContent);
        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(this.context);
        notificationManager.notify(getNotifictionID(), notificationBuilder.build());
    }

    private void showIdentifiedNotification(String textTitle, String textContent) throws Exception {
        this.createIdentifiedNotificationChannel();
        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(this.context, identifiedNotificationID)
                .setSmallIcon(R.drawable.common_google_signin_btn_icon_dark)
                .setContentTitle(textTitle)
                .setContentText(textContent);
        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(this.context);
        notificationManager.notify(getNotifictionID(), notificationBuilder.build());
    }

    public static void sendNormalNotification(String textTitle, String textContent, Context context) {
        try {
            getInstance(context).showNormalNotification(textTitle,textContent);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void sendIdentifiedNotification(String textTitle, String textContent, Context context) {
        try {
            getInstance(context).showIdentifiedNotification(textTitle,textContent);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void createNormalNotificationChannel() throws Exception {
        // TODO: Check for versions

        if (isNormalNotificationChannelActive) return;

        NotificationChannel notificationChannel = new NotificationChannel(normalNotificationID, normalNotificationChannelName, normalNotificationChannelImportance);
        notificationChannel.setLightColor(normalNotificationLightColor);
        notificationChannel.setLockscreenVisibility(normalNotoficationLockScreenVisiblity);
        notificationChannel.setDescription(normalNotificationChannelDescription);
        notificationChannel.setSound(normalNotificationChannelSound, null);

        NotificationManager manager = this.createNotificationManager();
        if (manager != null) {
            manager.createNotificationChannel(notificationChannel);
            isNormalNotificationChannelActive = true;
        } else {
            throw new NotificationManagerException("Sound system doesn't Responding. Try again!!");
        }
    }

    private void createIdentifiedNotificationChannel() throws Exception {
        // TODO: Check for versions

        if (identifiedNotificationChannelActive) return;

        NotificationChannel notificationChannel = new NotificationChannel(identifiedNotificationID, identifiedNotificationChannelName, identifiedNotificationChannelImportance);
        notificationChannel.setLightColor(identifiedNotificationLightColor);
        notificationChannel.setLockscreenVisibility(identifiedNotoficationLockScreenVisiblity);
        notificationChannel.setDescription(identifiedNotificationChannelDescription);
        notificationChannel.setSound(identifiedNotificationChannelSound, null);

        NotificationManager manager = this.createNotificationManager();
        if (manager != null) {
            manager.createNotificationChannel(notificationChannel);
            identifiedNotificationChannelActive = true;
        } else {
            throw new NotificationManagerException("Sound system doesn't Responding. Try again!!");
        }
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

}
