package com.kheileang.freemp3video;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.text.TextUtils;
import android.transition.Slide;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
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
import com.yausername.youtubedl_android.mapper.VideoInfo;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import java.io.File;

public class MainActivity extends AppCompatActivity implements View.OnClickListener, DownloadOptionsSheetFragment.FragmentListener {

    TextView tvCommandOutput, tvDownloadStatus;
    EditText etUrl;
    ProgressBar pgLoading, pgLoop;
    BottomNavigationView navView;
    Button btnDownload;
    ProgressDialog progressDialog;
    Boolean downloading = false;
    String url;
    InputMethodManager imm;
    int btnNo, quality;
    VideoInfo videoInfo;
    DownloadOptionsSheetFragment bottomSheet;

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
        imm = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
    }

    @Override
    public void onClick(View v) {
        if(v.getId() == R.id.btn){
            // checking editext value
            url = etUrl.getText().toString();
            if(TextUtils.isEmpty(url)){
                etUrl.setHint("Enter a valid url");
                return;
            }
            // before downloading
            prepareDownloading();
        }
    }

    private YoutubeDLRequest buildRequest() {
        YoutubeDLRequest youtubeDL = new YoutubeDLRequest(url);
        File ytdlDir = getDownloadDir(btnNo);

        if(btnNo<6){
            youtubeDL.addOption("-x");
            youtubeDL.addOption("--audio-format", "mp3");
            youtubeDL.addOption("--audio-quality", quality+"K");
            youtubeDL.addOption("--embed-thumbnail");
        }else if(btnNo>6 && btnNo<12){
            youtubeDL.addOption("-f", "bestvideo[height="+quality+"]+bestaudio/best");
        }


        youtubeDL.addOption("-o", ytdlDir.getAbsolutePath()+"/%(title)s.%(ext)s");
        return  youtubeDL;
    }

    private void prepareDownloading() {
        try {
            imm.hideSoftInputFromWindow(etUrl.getWindowToken(), 0);
        }catch (Exception e){
            View rootView = findViewById(android.R.id.content);
            Snackbar.make(rootView, "Something wrong..", Snackbar.LENGTH_LONG).show();
        }
        linkChecking();
    }

    private void showProgressDialog(String info, Boolean indeterminate) {
        progressDialog.setMessage(info);
        progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        progressDialog.setIndeterminate(indeterminate);
        progressDialog.setProgress(0);
        progressDialog.setMax(100);
        progressDialog.setCancelable(false);
        progressDialog.show();
    }

    private void linkChecking()  {
        showProgressDialog("Checking URL", true);

        // because of this YoutubeDL.getInstance(),
        // run it in a thread.
        new Thread(()->{
            try {
                videoInfo = YoutubeDL.getInstance().getInfo(url);
                if (videoInfo != null)
                    runOnUiThread(()->{
                        progressDialog.dismiss();
                        showBottomSheetDownloadOptions(videoInfo);
                    });
            }catch (YoutubeDLException e){
                showException(e);
            }catch (InterruptedException e){
                e.printStackTrace();
                runOnUiThread(()->{
                    View parentView = findViewById(android.R.id.content);
                    Snackbar.make(parentView, "InterruptedException "+e.getMessage(), Snackbar.LENGTH_LONG).show();
                });
            }
        }).start();
    }

    private void showBottomSheetDownloadOptions(VideoInfo videoInfo) {
        bottomSheet = new DownloadOptionsSheetFragment(videoInfo);
        bottomSheet.show(getSupportFragmentManager(), "bottomSheet");
    }


    private void startDownload() {

        if(downloading){
            Toast.makeText(this, "Downloading in progress", Toast.LENGTH_SHORT).show();
        }

        if(!isStoragePermissionGranted()){
            Toast.makeText(this, "Give storage permission and retry..", Toast.LENGTH_SHORT).show();
            return;
        }
        showStart();
        downloading=true;

        showProgressDialog("Downloading..", false);
        getMp3();

    }

    private void getMp3() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    YoutubeDLRequest youtubeDLRequest = buildRequest();
                    YoutubeDLResponse youtubeDLResponse = YoutubeDL.getInstance().execute(youtubeDLRequest, callback);

                    runOnUiThread(()->{
                        endLoading(youtubeDLResponse);
                    });
                } catch (YoutubeDLException e) {
                    e.printStackTrace();
                    showException(e);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }

    void showException(YoutubeDLException e){
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
                    Snackbar.make(parentLayout, "No Internet Connection", Snackbar.LENGTH_LONG).show();

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
            case 3:
                runOnUiThread(()->{
                    View parentLayout = findViewById(android.R.id.content);
                    Snackbar.make(parentLayout, "Failed to fetch video information", Snackbar.LENGTH_LONG).show();
                    endLoading(e.getMessage());
                });
                break;
            case 4:
                runOnUiThread(()->{
                    View parentLayout = findViewById(android.R.id.content);
                    Snackbar.make(parentLayout, "Unable to parse video information", Snackbar.LENGTH_LONG).show();
                    endLoading(e.getMessage());
                });
                break;
            case 5:
                runOnUiThread(()->{
                    View parentLayout = findViewById(android.R.id.content);
                    Snackbar.make(parentLayout, "Unable to download thumbnail.", Snackbar.LENGTH_LONG).show();
                    endLoading(e.getMessage());
                });
                break;
            case 6:
                runOnUiThread(()->{
                    View parentLayout = findViewById(android.R.id.content);
                    Snackbar.make(parentLayout, "Unable to convert audio.", Snackbar.LENGTH_LONG).show();
                    endLoading(e.getMessage());
                });
                break;

        }
    }

    void endLoading(YoutubeDLResponse youtubeDLResponse){
        tvCommandOutput.setText(youtubeDLResponse.getOut());
        tvDownloadStatus.setText("Download Complete");
        pgLoading.setProgress(100);
        pgLoading.setVisibility(View.GONE);
        etUrl.setEnabled(true);
        pgLoop.setVisibility(View.GONE);
        progressDialog.dismiss();
        downloading = false;
    }

    void endLoading(String errorMessage){
        tvCommandOutput.setText(errorMessage);
        tvDownloadStatus.setText("Download Failed.");
        pgLoading.setProgress(100);
        pgLoading.setVisibility(View.GONE);
        pgLoop.setVisibility(View.GONE);
        etUrl.setEnabled(true);
        progressDialog.dismiss();
        downloading = false;
    }

    private int getExceptionCode(String error){
        String code1 = "is not a valid URL";
        String code2 = "Unable to download webpage";
        String code3 = "Failed to fetch video information";
        String code4 = "Unable to parse video information";
        String code5 = "Unable to download thumbnail";
        String code6 = "audio conversion failed";
        if (error.contains(code1)){
            return 1;
        }else if ( error.contains(code2)){
            return 2;
        }else if (error.contains(code3)){
            return 3;
        }else if (error.contains(code4)){
            return 4;
        }else if (error.contains(code5)){
            return 5;
        }else if (error.contains(code6)){
            return 6;
        }
        return 0;
    }

    private void showStart() {
        tvDownloadStatus.setText("Start Downloading...");
        pgLoading.setProgress(0);
        pgLoading.setVisibility(View.GONE);
        pgLoop.setVisibility(View.GONE);
        etUrl.setEnabled(false);

    }

    private File getDownloadDir(int btnNo) {
        File downloadDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
        File youtubeAudioDir = new File(downloadDir, "FreeStuff/Free MP3");
        File youtubeVideoDir = new File(downloadDir, "FreeStuff/Free Video");

        if (btnNo<6) {
            // MP3
            if (!youtubeAudioDir.exists()) {
                youtubeAudioDir.mkdirs();
            }
            return youtubeAudioDir;
        }else{
            // MP4
            if (!youtubeVideoDir.exists()) {
                youtubeVideoDir.mkdirs();
            }
            return youtubeVideoDir;
        }
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

    @Override
    public void onSendData(int btnNo, int quality) {
        this.btnNo = btnNo;
        this.quality = quality;
        // dismiss bottom sheet after user has chosen to download a mp3 or mp4
        bottomSheet.dismiss();
        Toast.makeText(this, "Starting to download "+videoInfo.getTitle(), Toast.LENGTH_LONG).show();
        startDownload();
    }
}