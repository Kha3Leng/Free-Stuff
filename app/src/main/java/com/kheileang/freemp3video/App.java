package com.kheileang.freemp3video;

import android.app.Application;
import android.util.Log;
import android.widget.Toast;

import com.yausername.ffmpeg.FFmpeg;
import com.yausername.youtubedl_android.YoutubeDL;
import com.yausername.youtubedl_android.YoutubeDLException;

public class App extends Application {

    private static final String TAG = App.class.getSimpleName();

    @Override
    public void onCreate() {
        super.onCreate();

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
