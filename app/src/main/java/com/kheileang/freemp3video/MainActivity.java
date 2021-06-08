package com.kheileang.freemp3video;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.yausername.youtubedl_android.YoutubeDL;
import com.yausername.youtubedl_android.YoutubeDLException;
import com.yausername.youtubedl_android.YoutubeDLRequest;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import java.io.File;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    TextView tvCommandOutput, tvDownloadStatus;
    EditText etUrl;
    ProgressBar pgLoading, pgLoop;
    BottomNavigationView navView;
    Button btnDownload;

    Boolean downloading = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initView();
        initListener();
    }

    private void initListener() {
        btnDownload.setOnClickListener(this);
    }


    private void initView() {
        tvCommandOutput = findViewById(R.id.commandOutput);
        tvDownloadStatus = findViewById(R.id.downloadStatus);
        etUrl = findViewById(R.id.url);
        pgLoading = findViewById(R.id.progressBarLoading);
        pgLoop = findViewById(R.id.pgLoop);
        navView = findViewById(R.id.nav_view);
        btnDownload = findViewById(R.id.btn);
    }

    @Override
    public void onClick(View v) {
        if(v.getId() == R.id.btn){
            startDownload();
        }
    }

    private void startDownload() {
        if(downloading){
            Toast.makeText(this, "Downloading in progress", Toast.LENGTH_SHORT).show();
        }

        if(!isStoragePermissionGranted()){
            Toast.makeText(this, "Give storage permission and retry..", Toast.LENGTH_SHORT).show();
            return;
        }

        String url = etUrl.getText().toString();
        if(TextUtils.isEmpty(url)){
            etUrl.setText("ENter a valid url");
            return;
        }

        YoutubeDLRequest youtubeDL = new YoutubeDLRequest(url);
        File ytdlDir = getDownloadDir();

        youtubeDL.addOption("-x");
        youtubeDL.addOption("--audio-format", "mp3");
        youtubeDL.addOption("--audio-quality", "320K");
        youtubeDL.addOption("-o", ytdlDir.getAbsolutePath()+"/%(title)s – %(artist)s.%(ext)s");

        showStart();

        downloading=true;

        try {
            YoutubeDL.getInstance().execute(youtubeDL, (progress, etaInSeconds) -> {
                System.out.println(String.valueOf(progress) + "% (ETA " + String.valueOf(etaInSeconds) + " seconds)");
            });
        } catch (YoutubeDLException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    private void showStart() {
        tvDownloadStatus.setText("Start Downloading...");
        pgLoading.setProgress(0);
        pgLoop.setVisibility(View.VISIBLE);
    }

    private File getDownloadDir() {
        File downloadDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
        File youtubeDir = new File(downloadDir, "FreeStuff");
        if (!youtubeDir.exists()){
            youtubeDir.mkdirs();
        }
        return youtubeDir;
    }

    private boolean isStoragePermissionGranted() {
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M){
            if(checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED){
                return true;
            }else{
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1);
                return false;
            }
        }else{
            return true;
        }
    }
}