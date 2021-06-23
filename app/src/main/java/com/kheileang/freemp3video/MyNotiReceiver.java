package com.kheileang.freemp3video;

import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.widget.Toast;

import androidx.core.app.NotificationCompat;

import com.yausername.youtubedl_android.YoutubeDLResponse;

import java.util.Set;

import static android.content.Context.NOTIFICATION_SERVICE;
import static com.kheileang.freemp3video.App.CHANNEL_ID;
import static com.kheileang.freemp3video.App.notificationManager;

public class MyNotiReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        int thread_id = intent.getIntExtra("thread_id", 0);
        int noti_id = intent.getIntExtra("notification_id", 0);
        Set<Thread> threadSet = Thread.getAllStackTraces().keySet();
        for (Thread thread : threadSet) {
            if (thread.getId() == thread_id && thread.isAlive()) {
                thread.interrupt();
                NotificationManager notificationManager = (NotificationManager) context.getSystemService(NOTIFICATION_SERVICE);
                notificationManager.cancel(noti_id);
                Toast.makeText(context, "You cancelled this thread.", Toast.LENGTH_SHORT).show();
            }
        }
    }
}
