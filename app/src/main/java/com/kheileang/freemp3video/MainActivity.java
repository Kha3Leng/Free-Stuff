package com.kheileang.freemp3video;

import android.Manifest;
import android.app.ProgressDialog;
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
import com.google.android.material.snackbar.Snackbar;
import com.yausername.youtubedl_android.DownloadProgressCallback;
import com.yausername.youtubedl_android.YoutubeDL;
import com.yausername.youtubedl_android.YoutubeDLException;
import com.yausername.youtubedl_android.YoutubeDLRequest;
import com.yausername.youtubedl_android.YoutubeDLResponse;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import java.io.File;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    TextView tvCommandOutput, tvDownloadStatus;
    EditText etUrl;
    ProgressBar pgLoading, pgLoop;
    BottomNavigationView navView;
    Button btnDownload;
    ProgressDialog progressDialog;
    Boolean downloading = false;

    final DownloadProgressCallback callback = new DownloadProgressCallback() {
        @Override
        public void onProgressUpdate(float progress, long etaInSeconds) {
            runOnUiThread(() -> {
//                pgLoading.setProgress((int) progress);
                progressDialog.setProgress((int) progress);
                tvDownloadStatus.setText(String.valueOf(progress) + "% ( ETA " + String.valueOf(etaInSeconds) + " seconds )");
            });
        }
    };

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
        pgLoop = findViewById(R.id.pgb_progress4);
        navView = findViewById(R.id.nav_view);
        btnDownload = findViewById(R.id.btn);
        progressDialog = new ProgressDialog(this);
    }

    @Override
    public void onClick(View v) {
        if(v.getId() == R.id.btn){
            // before downloading
            prepareDownloading();
        }
    }

    private void prepareDownloading() {

    }

    private void startDownload() {

        progressDialog.setMessage("Downloading Music");
        progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        progressDialog.setIndeterminate(false);
        progressDialog.setProgress(0);
        progressDialog.setMax(100);
        progressDialog.setCancelable(false);
        progressDialog.show();

        if(downloading){
            Toast.makeText(this, "Downloading in progress", Toast.LENGTH_SHORT).show();
        }

        if(!isStoragePermissionGranted()){
            Toast.makeText(this, "Give storage permission and retry..", Toast.LENGTH_SHORT).show();
            return;
        }

        String url = etUrl.getText().toString();
        if(TextUtils.isEmpty(url)){
            etUrl.setHint("Enter a valid url");
            return;
        }

        YoutubeDLRequest youtubeDL = new YoutubeDLRequest(url);
        File ytdlDir = getDownloadDir();

        youtubeDL.addOption("-x");
        youtubeDL.addOption("--audio-format", "mp3");
        youtubeDL.addOption("--audio-quality", "320K");
        youtubeDL.addOption("--embed-thumbnail");
        youtubeDL.addOption("-o", ytdlDir.getAbsolutePath()+"/%(title)s.%(ext)s");

        showStart();

        downloading=true;

        getMp3(youtubeDL);

    }

    private void getMp3(YoutubeDLRequest youtubeDL) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {

                    YoutubeDLResponse youtubeDLResponse = YoutubeDL.getInstance().execute(youtubeDL, callback);

                    runOnUiThread(()->{
                        endLoading(youtubeDLResponse);
                    });
                } catch (YoutubeDLException e) {
                    e.printStackTrace();
                    switch (getExceptionCode(e.getMessage())){
                        case 1:
                            runOnUiThread(()->{
                                View parentLayout = findViewById(android.R.id.content);
                                Snackbar.make(parentLayout,"Invalid URL", Snackbar.LENGTH_LONG).show();

                                endLoading(e.getMessage());
                            });
                            break;
                        case 2:
                            runOnUiThread(()->{
                                View parentLayout = findViewById(android.R.id.content);
                                Snackbar.make(parentLayout, "No Internet to Download", Snackbar.LENGTH_LONG).show();

                                endLoading(e.getMessage());
                            });
                            break;
                        case 0:
                            runOnUiThread(()->{
                                View parentLayout = findViewById(android.R.id.content);
                                Snackbar.make(parentLayout, "Something wrong..", Snackbar.LENGTH_LONG).show();
                                endLoading(e.getMessage());
                            });
                            break;
                    }
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }).start();

    }



    void endLoading(YoutubeDLResponse youtubeDLResponse){
        tvCommandOutput.setText(youtubeDLResponse.getOut());
        tvDownloadStatus.setText("Download Complete");
        pgLoading.setProgress(100);
        pgLoading.setVisibility(View.GONE);
        pgLoop.setVisibility(View.GONE);
        progressDialog.dismiss();
    }

    void endLoading(String errorMessage){
        tvCommandOutput.setText(errorMessage);
        tvDownloadStatus.setText("Download Failed.");
        pgLoading.setProgress(100);
        pgLoading.setVisibility(View.GONE);
        pgLoop.setVisibility(View.GONE);
        progressDialog.dismiss();
    }

    private int getExceptionCode(String error){
        String code1 = "is not a valid URL";
        String code2 = "Unable to download webpage";
        if (error.contains(code1)){
            return 1;
        }else if ( error.contains(code2)){
            return 2;
        }
        return 0;
    }

    private void showStart() {
        tvDownloadStatus.setText("Start Downloading...");
        pgLoading.setProgress(0);
        pgLoading.setVisibility(View.VISIBLE);
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