package com.kheileang.freemp3video;

import android.app.Application;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.os.Build;
import android.util.Log;
import android.widget.Toast;

import com.yausername.ffmpeg.FFmpeg;
import com.yausername.youtubedl_android.YoutubeDL;
import com.yausername.youtubedl_android.YoutubeDLException;

public class App extends Application {

    private static final String TAG = App.class.getSimpleName();
    public static final String CHANNEL_ID = "Downloading.Channel.Notification";
    public static NotificationChannel channel_download;
    public static NotificationManager notificationManager;

    @Override
    public void onCreate() {
        super.onCreate();

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            channel_download = new NotificationChannel(CHANNEL_ID, "Downloading Channel", NotificationManager.IMPORTANCE_DEFAULT);
            notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
            notificationManager.createNotificationChannel(channel_download);
        }

        initLibs();
    }

    private void initLibs() {
        // yt-dl init & ffmpeg init
        try {
            YoutubeDL.getInstance().init(this);
            FFmpeg.getInstance().init(this);
        } catch (YoutubeDLException e) {
            Log.e(TAG, "failed to initialize youtubedl-android", e);
            Toast.makeText(this, "failed to initialize youtubedl-android"+e.getMessage(), Toast.LENGTH_LONG).show();
        }

    }
}
