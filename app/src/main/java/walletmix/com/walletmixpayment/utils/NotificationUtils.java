package walletmix.com.walletmixpayment.utils;

import android.app.ActivityManager;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.ComponentName;
import android.content.Context;
import android.content.ContextWrapper;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.Build;
import android.provider.Settings;
import android.support.v4.app.NotificationCompat;
import android.support.v4.content.ContextCompat;
import android.text.Html;
import android.text.TextUtils;
import android.util.Patterns;

import java.util.Date;
import java.util.List;

import walletmix.com.walletmixpayment.R;
import walletmix.com.walletmixpayment.file.ImageDownloader;


public class NotificationUtils extends ContextWrapper {

    private NotificationManager mNotificationManager;
    public static final String CHANNEL_ID = "my_channel_Id.";
    public static final String CHANNEL_NAME = "my_channel_Name.";
    private final long[] vibrationScheme = new long[]{1000, 1000, 1000, 1000, 1000};
    private Context context;


    public NotificationUtils(Context context) {
        super(context);
        this.context = context;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID + context.getApplicationContext().getPackageName(),
                    CHANNEL_NAME + context.getApplicationContext().getPackageName(),
                    NotificationManager.IMPORTANCE_DEFAULT);
            // Sets whether notifications posted to this channel should display notification lights
            channel.enableLights(true);
            // Sets whether notification posted to this channel should vibrate.
            channel.enableVibration(true);
            // Sets the notification light color for notifications posted to this channel
            channel.setLightColor(Color.GREEN);
            // Sets whether notifications posted to this channel appear on the lockscreen or not
            channel.setLockscreenVisibility(Notification.VISIBILITY_PRIVATE);

            channel.setVibrationPattern(vibrationScheme);
            // Submit the notification channel object to the notification manager
            getNotificationManager().createNotificationChannel(channel);

        }
    }


    public Notification getNotification(String title, String body) {


        NotificationCompat.BigTextStyle bigText = new NotificationCompat.BigTextStyle();
        bigText.bigText(body);
        bigText.setBigContentTitle(title);
        //  bigText.setSummaryText("Text in detail");
        //  Notification.InboxStyle inboxStyle = new Notification.InboxStyle(); // Need grouping notifications....


        return new NotificationCompat.Builder(getApplicationContext(), CHANNEL_ID)
                .setSmallIcon(R.mipmap.app_icon)
                .setContentTitle(title)
                .setTicker("From Cholbe ROBI")
                .setContentText(body)
                .setSound(Settings.System.DEFAULT_NOTIFICATION_URI)
                .setVibrate(vibrationScheme)
                //  .setColor(ContextCompat.getColor(getApplicationContext(), R.color.colorPrimary))
                .setLights(ContextCompat.getColor(getApplicationContext(), R.color.colorPrimary), 300, 300)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                //       .setContentIntent(pendingIntent)
                .setAutoCancel(true)
                .setStyle(bigText)
                .build();
    }


    public NotificationManager getNotificationManager() {
        if (mNotificationManager == null) {
            mNotificationManager =
                    (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        }
        return mNotificationManager;
    }


    public int getNotificationId() {
        return (int) ((new Date().getTime() / 1000L) % Integer.MAX_VALUE);
    }

    public void clearNotifications(Context context) {
        getNotificationManager().cancelAll();
    }

    public boolean isAppIsInBackground(Context context) {
        boolean isInBackground = true;
        ActivityManager am = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.KITKAT_WATCH) {
            assert am != null;
            List<ActivityManager.RunningAppProcessInfo> runningProcesses = am.getRunningAppProcesses();
            for (ActivityManager.RunningAppProcessInfo processInfo : runningProcesses) {
                if (processInfo.importance == ActivityManager.RunningAppProcessInfo.IMPORTANCE_FOREGROUND) {
                    for (String activeProcess : processInfo.pkgList) {
                        if (activeProcess.equals(context.getPackageName())) {
                            isInBackground = false;
                        }
                    }
                }
            }
        } else {
            assert am != null;
            List<ActivityManager.RunningTaskInfo> taskInfo = am.getRunningTasks(1);
            ComponentName componentInfo = taskInfo.get(0).topActivity;
            if (componentInfo.getPackageName().equals(context.getPackageName())) {
                isInBackground = false;
            }
        }

        return isInBackground;
    }


    public void showPushNotification(final String title, final String message, final PendingIntent pendingIntent, final String imageUrl) {
        // Check for empty push message
        if (TextUtils.isEmpty(message))
            return;
        // notification icon
        final int icon = R.mipmap.app_icon;
        final NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(getApplicationContext(), CHANNEL_ID);


        if (imageUrl != null && imageUrl.length() > 4 && Patterns.WEB_URL.matcher(imageUrl).matches()) {

            ImageDownloader imageDownloader = new ImageDownloader(imageUrl, new ImageDownloader.ImageDownloadListener() {
                @Override
                public void onDownloadedImage(Bitmap bitmap) {
                    if (bitmap != null) {
                        showBigPictureNotification(bitmap, icon, mBuilder, title, message, pendingIntent);
                    } else {
                        showTextNotification(icon, mBuilder, title, message, pendingIntent);
                    }
                }
            });
            imageDownloader.execute();
        } else {
            showTextNotification(icon, mBuilder, title, message, pendingIntent);
        }
    }


    private void showTextNotification(int icon,
                                      NotificationCompat.Builder mBuilder,
                                      String title,
                                      String message,
                                      PendingIntent pendingIntent) {

        NotificationCompat.BigTextStyle bigText = new NotificationCompat.BigTextStyle();
        bigText.bigText(message);
        bigText.setBigContentTitle(title);

        NotificationCompat.Builder builder = mBuilder.setSmallIcon(icon).setTicker(title).setWhen(0)
                .setAutoCancel(true)
                .setContentTitle(title)
                .setStyle(bigText)
                .setSmallIcon(icon)
                .setContentText(message)
                .setSound(Settings.System.DEFAULT_NOTIFICATION_URI)
                .setVibrate(vibrationScheme);
        if (pendingIntent != null)
            builder.setContentIntent(pendingIntent);

        getNotificationManager().notify(getNotificationId(), builder.build());
    }


    private void showBigPictureNotification(Bitmap bitmap,
                                            int icon,
                                            NotificationCompat.Builder mBuilder,
                                            String title,
                                            String message,
                                            PendingIntent pendingIntent) {
        NotificationCompat.BigPictureStyle bigPictureStyle = new NotificationCompat.BigPictureStyle();
        bigPictureStyle.setBigContentTitle(title);
        bigPictureStyle.setSummaryText(Html.fromHtml(message).toString());
        bigPictureStyle.bigPicture(bitmap);
        NotificationCompat.Builder builder = mBuilder.setSmallIcon(icon).setTicker(title).setWhen(0)
                .setAutoCancel(true)
                .setContentTitle(title)
                .setStyle(bigPictureStyle)
                .setSmallIcon(icon)
                .setContentText(message)
                .setSound(Settings.System.DEFAULT_NOTIFICATION_URI)
                .setVibrate(vibrationScheme);
        if (pendingIntent != null)
            builder.setContentIntent(pendingIntent);
        getNotificationManager().notify(getNotificationId(), builder.build());

    }
}