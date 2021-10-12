package com.gallery.photosgallery.videogallery.bestgallery.Util;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.content.ContextWrapper;
import android.os.Build;

import androidx.annotation.RequiresApi;


public class NotificationUtils extends ContextWrapper{
 
    private NotificationManager mManager;
    public static final String ANDROID_CHANNEL_ID = "com.filemanager.globalFileexplorer.ANDROID";
    public static final String ANDROID_CHANNEL_NAME = "Delete_Channel";

    @RequiresApi(api = Build.VERSION_CODES.O)
    public NotificationUtils(Context base) {
        super(base);
        createChannels();
    }
 
    @RequiresApi(api = Build.VERSION_CODES.O)
    public void createChannels() {
 
        // create android channel
        NotificationChannel androidChannel = new NotificationChannel(ANDROID_CHANNEL_ID, ANDROID_CHANNEL_NAME, NotificationManager.IMPORTANCE_HIGH);
        // Sets whether notifications posted to this channel should display notification lights
//        androidChannel.enableLights(true);
//        // Sets whether notification posted to this channel should vibrate.
//        androidChannel.enableVibration(true);
//        // Sets the notification light color for notifications posted to this channel
//        androidChannel.setLightColor(Color.GREEN);
//        // Sets whether notifications posted to this channel appear on the lockscreen or not
        androidChannel.setLockscreenVisibility(Notification.VISIBILITY_PUBLIC);

        getManager().createNotificationChannel(androidChannel);
    }
 
    private NotificationManager getManager() {
        if (mManager == null) {
            mManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        }
        return mManager;
    }
}
