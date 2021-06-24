package com.kheileang.freemp3video;

import android.Manifest;
import android.app.Dialog;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.ProgressDialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.IBinder;
import android.os.PowerManager;
import android.text.TextUtils;
import android.transition.Slide;
import android.util.Log;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
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

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import java.io.File;
import java.util.Date;

import static com.kheileang.freemp3video.App.CHANNEL_ID;
import static com.kheileang.freemp3video.App.notificationManager;

public class MainActivity extends AppCompatActivity implements View.OnClickListener, DownloadOptionsSheetFragment.FragmentListener {

    TextView tvCommandOutput;
    EditText etUrl;
    BottomNavigationView navView;
    Button btnDownload;
    ProgressDialog progressDialog;
    Boolean downloading = false;
    String url;
    InputMethodManager imm;
    int btnNo, quality;
    DownloadOptionsSheetFragment bottomSheet;
    ViewDownloadFragment viewDownloadFragment;
    private static final String TAG = MainActivity.class.getSimpleName();
    NotificationCompat.Builder mNotificationBuilder;
    PendingIntent pendingIntentActivity, pendingIntentBroadcast;
    Bitmap licon;
    NotificationCompat.BigTextStyle bigTextStyle;
    FragmentManager fragmentManager;

    @Override
    protected void onResume() {
        super.onResume();
        navView.setSelectedItemId(R.id.navigation_home);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.top_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();
        switch (id) {
            case R.id.download_progress:
                startActivity(new Intent(this, DownloadActivity.class));
                overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
                break;
        }
        return true;
    }

    /*final DownloadProgressCallback callback = new DownloadProgressCallback() {
        @Override
        public void onProgressUpdate(float progress, long etaInSeconds) {
            runOnUiThread(() -> {
                mNotificationBuilder.setProgress(100, (int) progress, false)
                        .setContentText(String.valueOf(progress) + "% ( ETA " + String.valueOf(etaInSeconds) + " seconds )")
                        .setOngoing(true)
                        .setSmallIcon(R.drawable.ic_notifications_black_24dp)
                        .setPriority(NotificationCompat.PRIORITY_HIGH)
                        .setOnlyAlertOnce(true)
                        .setContentIntent(pendingIntentActivity);
                notificationManager.notify(m, mNotificationBuilder.build());
                progressDialog.setProgress((int) progress);
                tvDownloadStatus.setText(String.valueOf(progress) + "% ( ETA " + String.valueOf(etaInSeconds) + " seconds )");

            });
        }
    };*/


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initView();
        initListener();
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        Intent startIntent = intent;
        String action = startIntent.getAction();
        String type = startIntent.getType();
        if (Intent.ACTION_SEND.equals(action) && type != null) {
            etUrl.setText(startIntent.getStringExtra(Intent.EXTRA_TEXT));
        }
    }

    private void initListener() {
        btnDownload.setOnClickListener(this);
    }


    private void initView() {
        tvCommandOutput = findViewById(R.id.commandOutput);
        etUrl = findViewById(R.id.url);
        navView = findViewById(R.id.nav_view);
        btnDownload = findViewById(R.id.btn);
        progressDialog = new ProgressDialog(this);
        imm = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);

        Intent startIntent = getIntent();
        String action = startIntent.getAction();
        String type = startIntent.getType();
        if (Intent.ACTION_SEND.equals(action) && type != null) {
            etUrl.setText(startIntent.getStringExtra(Intent.EXTRA_TEXT));
        }

        navView.setSelectedItemId(R.id.navigation_home);

        // bottom nav view
        navView.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                int id = item.getItemId();
                switch (id) {
                    case R.id.navigation_home:
                        return true;
                    case R.id.navigation_media:
                        startActivity(new Intent(getApplicationContext(), DownloadActivity.class));
                        overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
                        break;
                    case R.id.navigation_setting:
                        startActivity(new Intent(getApplicationContext(), SettingsActivity.class));
                        overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
                        break;
                    default:
                        Toast.makeText(MainActivity.this, "You clicked something wrong.", Toast.LENGTH_SHORT).show();
                }
                return true;
            }
        });

        // intent for getActivity Notifiaction
        Intent intent = new Intent(this, DownloadActivity.class);
        pendingIntentActivity = PendingIntent.getActivity(this, 0, intent, 0);

        // building a notification
        mNotificationBuilder = new NotificationCompat.Builder(this, CHANNEL_ID);


        licon = BitmapFactory.decodeResource(getResources(), R.drawable.ic_notifications_black_24dp);
        bigTextStyle = new NotificationCompat.BigTextStyle();

        // show fragment pointing to download activity
        fragmentManager = getSupportFragmentManager();
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.btn) {
            // checking editext value
            url = etUrl.getText().toString();
            if (TextUtils.isEmpty(url)) {
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

        if (btnNo < 6) {
            youtubeDL.addOption("-x");
            youtubeDL.addOption("--audio-format", "mp3");
            youtubeDL.addOption("--audio-quality", quality + "K");
            youtubeDL.addOption("--embed-thumbnail");
        } else if (btnNo > 6 && btnNo < 12) {
            youtubeDL.addOption("-f", "bestvideo[height=" + quality + "]+bestaudio/best");
        }


        youtubeDL.addOption("-o", ytdlDir.getAbsolutePath() + "/%(title)s.%(ext)s");
        return youtubeDL;
    }

    private void prepareDownloading() {
        try {
            imm.hideSoftInputFromWindow(etUrl.getWindowToken(), 0);
        } catch (Exception e) {
            View rootView = findViewById(android.R.id.content);
            Snackbar.make(rootView, "Something wrong..", Snackbar.LENGTH_LONG).show();
        } finally {
            linkChecking();
        }
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

    private void linkChecking() {
        showProgressDialog("Checking URL", true);
        // because of this YoutubeDL.getInstance(),
        // run it in a thread.
        new Thread(() -> {
            try {

                VideoInfo videoInfo = YoutubeDL.getInstance().getInfo(url);
                if (videoInfo != null)
                    runOnUiThread(() -> {
                        progressDialog.dismiss();
                        /*getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);*/
                        showBottomSheetDownloadOptions(videoInfo);
                    });
            } catch (YoutubeDLException e) {
                showException(e);
            } catch (InterruptedException e) {
                e.printStackTrace();
                runOnUiThread(() -> {
                    View parentView = findViewById(android.R.id.content);
                    Snackbar.make(parentView, "InterruptedException " + e.getMessage(), Snackbar.LENGTH_LONG).show();
                });
            }
        }).start();
    }

    private void showBottomSheetDownloadOptions(VideoInfo videoInfo) {
        try {
            bottomSheet = new DownloadOptionsSheetFragment(videoInfo);
            bottomSheet.show(getSupportFragmentManager(), "bottomSheet");
        } catch (IllegalStateException e) {
            e.printStackTrace();
        }
    }


    private void startDownload(VideoInfo videoInfo) {

        if (downloading) {
            Toast.makeText(this, "Downloading in progress", Toast.LENGTH_SHORT).show();
        }

        if (!isStoragePermissionGranted()) {
            Toast.makeText(this, "Give storage permission and retry..", Toast.LENGTH_SHORT).show();
            return;
        }
        showStart();
        downloading = true;

//        showProgressDialog("Downloading..", false);
        getMp3(videoInfo);

    }

    private void getMp3(VideoInfo videoInfo) {

        new Thread(new Runnable() {
            @Override
            public void run() {
                // get unique number for multiple notifications
                int m = (int) ((new Date().getTime() / 1000L) % Integer.MAX_VALUE);
                // intent for getBroadcast Notification
                Intent intent1 = new Intent(getApplicationContext(), MyNotiReceiver.class);
                intent1.putExtra("thread_id", Thread.currentThread().getId());
                intent1.putExtra("notificaiton_id", m);
                pendingIntentBroadcast = PendingIntent.getBroadcast(getApplicationContext(), m, intent1, PendingIntent.FLAG_ONE_SHOT);

                runOnUiThread(() -> {
                    mNotificationBuilder.setContentText(videoInfo.getTitle())
                            .setProgress(100, 0, false)
                            .setOngoing(true)
                            .setSmallIcon(R.drawable.ic_notifications_black_24dp)
                            .setPriority(NotificationCompat.PRIORITY_HIGH)
                            .setContentTitle("Downloading in progress")
                            .setContentIntent(pendingIntentActivity);
                            /*.addAction(R.drawable.ic__01_music, "Cancel", pendingIntentBroadcast);*/
                    notificationManager.notify(m, mNotificationBuilder.build());
                });


                try {
                    YoutubeDLRequest youtubeDLRequest = buildRequest();
                    YoutubeDLResponse youtubeDLResponse = YoutubeDL.getInstance().execute(youtubeDLRequest,
                            (progress, etaInSeconds) -> runOnUiThread(() -> {
                                mNotificationBuilder.setProgress(100, (int) progress, false)
                                        .setContentText(String.valueOf(progress) + "% ( ETA " + String.valueOf(etaInSeconds) + " seconds )")
                                        .setOngoing(true)
                                        .setSmallIcon(R.drawable.ic_notifications_black_24dp)
                                        .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                                        .setContentIntent(pendingIntentActivity);
                                notificationManager.notify(m, mNotificationBuilder.build());
                                ViewDownloadFragment.pbLoading.setProgress((int) progress);
                                ViewDownloadFragment.tvPercent.setText(progress+"% Saving...");
                            }));

                    runOnUiThread(() -> {
                        // writing output file
                        endLoading(youtubeDLResponse, m, videoInfo);
                    });
                } catch (YoutubeDLException e) {
                    e.printStackTrace();
                    showException(e, m, videoInfo);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                    Thread.currentThread().interrupt();
                }
            }
        }).start();
    }

    void showException(YoutubeDLException e, int m, VideoInfo videoInfo) {
        switch (getExceptionCode(e.getMessage())) {
            case 1:
                runOnUiThread(() -> {
                    View parentLayout = findViewById(android.R.id.content);
                    Snackbar.make(parentLayout, "Invalid URL", Snackbar.LENGTH_LONG).show();

                    endLoading(e.getMessage(), m, videoInfo);
                });
                break;
            case 2:
                runOnUiThread(() -> {
                    View parentLayout = findViewById(android.R.id.content);
                    Snackbar.make(parentLayout, "No Internet Connection", Snackbar.LENGTH_LONG).show();

                    endLoading(e.getMessage(), m, videoInfo);
                });
                break;
            case 0:
                runOnUiThread(() -> {
                    View parentLayout = findViewById(android.R.id.content);
                    Snackbar.make(parentLayout, "Something wrong..", Snackbar.LENGTH_LONG).show();
                    endLoading(e.getMessage(), m, videoInfo);
                });
                break;
            case 3:
                runOnUiThread(() -> {
                    View parentLayout = findViewById(android.R.id.content);
                    Snackbar.make(parentLayout, "Failed to fetch video information", Snackbar.LENGTH_LONG).show();
                    endLoading(e.getMessage(), m, videoInfo);
                });
                break;
            case 4:
                runOnUiThread(() -> {
                    View parentLayout = findViewById(android.R.id.content);
                    Snackbar.make(parentLayout, "Unable to parse video information", Snackbar.LENGTH_LONG).show();
                    endLoading(e.getMessage(), m, videoInfo);
                });
                break;
            case 5:
                runOnUiThread(() -> {
                    View parentLayout = findViewById(android.R.id.content);
                    Snackbar.make(parentLayout, "Unable to download thumbnail.", Snackbar.LENGTH_LONG).show();
                    endLoading(e.getMessage(), m, videoInfo);
                });
                break;
            case 6:
                runOnUiThread(() -> {
                    View parentLayout = findViewById(android.R.id.content);
                    Snackbar.make(parentLayout, "Unable to convert audio.", Snackbar.LENGTH_LONG).show();
                    endLoading(e.getMessage(), m, videoInfo);
                });
                break;

        }
    }

    void showException(YoutubeDLException e) {
        switch (getExceptionCode(e.getMessage())) {
            case 1:
                runOnUiThread(() -> {
                    View parentLayout = findViewById(android.R.id.content);
                    Snackbar.make(parentLayout, "Invalid URL", Snackbar.LENGTH_LONG).show();

                    endLoading(e.getMessage());
                });
                break;
            case 2:
                runOnUiThread(() -> {
                    View parentLayout = findViewById(android.R.id.content);
                    Snackbar.make(parentLayout, "No Internet Connection", Snackbar.LENGTH_LONG).show();

                    endLoading(e.getMessage());
                });
                break;
            case 0:
                runOnUiThread(() -> {
                    View parentLayout = findViewById(android.R.id.content);
                    Snackbar.make(parentLayout, "Something wrong..", Snackbar.LENGTH_LONG).show();
                    endLoading(e.getMessage());
                });
                break;
            case 3:
                runOnUiThread(() -> {
                    View parentLayout = findViewById(android.R.id.content);
                    Snackbar.make(parentLayout, "Failed to fetch video information", Snackbar.LENGTH_LONG).show();
                    endLoading(e.getMessage());
                });
                break;
            case 4:
                runOnUiThread(() -> {
                    View parentLayout = findViewById(android.R.id.content);
                    Snackbar.make(parentLayout, "Unable to parse video information", Snackbar.LENGTH_LONG).show();
                    endLoading(e.getMessage());
                });
                break;
            case 5:
                runOnUiThread(() -> {
                    View parentLayout = findViewById(android.R.id.content);
                    Snackbar.make(parentLayout, "Unable to download thumbnail.", Snackbar.LENGTH_LONG).show();
                    endLoading(e.getMessage());
                });
                break;
            case 6:
                runOnUiThread(() -> {
                    View parentLayout = findViewById(android.R.id.content);
                    Snackbar.make(parentLayout, "Unable to convert audio.", Snackbar.LENGTH_LONG).show();
                    endLoading(e.getMessage());
                });
                break;

        }
    }

    void endLoading(YoutubeDLResponse youtubeDLResponse, int m, VideoInfo videoInfo) {
        tvCommandOutput.setText(youtubeDLResponse.getOut());
        etUrl.setEnabled(true);
        progressDialog.dismiss();
        downloading = false;
        ViewDownloadFragment.pbLoading.setVisibility(View.GONE);
        ViewDownloadFragment.tvPercent.setVisibility(View.GONE);
        ViewDownloadFragment.ivFinish.setVisibility(View.VISIBLE);

        bigTextStyle.bigText(videoInfo.getFulltitle() + " has been downloaded.");

        mNotificationBuilder.setProgress(0, 0, false)
                .setContentText(videoInfo.getFulltitle() + " has been downloaded.")
                .setContentTitle("Download Complete")
                .setLargeIcon(licon)
                .setStyle(bigTextStyle)
                .setSmallIcon(R.drawable.ic_notifications_black_24dp)
                .setOngoing(false);
        notificationManager.notify(m, mNotificationBuilder.build());
        Toast.makeText(this, "Download Finished", Toast.LENGTH_SHORT).show();
    }

    void endLoading(String errorMessage, int m, VideoInfo videoInfo) {
        tvCommandOutput.setText(errorMessage);
        etUrl.setEnabled(true);
        progressDialog.dismiss();
        downloading = false;
        ViewDownloadFragment.pbLoading.setVisibility(View.GONE);
        ViewDownloadFragment.tvPercent.setVisibility(View.GONE);
        ViewDownloadFragment.ivFinish.setVisibility(View.VISIBLE);

        bigTextStyle.bigText(videoInfo.getTitle() + " cannot be downloaded.");

        mNotificationBuilder.setProgress(0, 0, false)
                .setContentTitle("Download Failed")
                .setLargeIcon(licon)
                .setStyle(bigTextStyle)
                .setOngoing(false)
                .setContentText("Failed to download")
                .setSmallIcon(R.drawable.ic_notifications_black_24dp);
        notificationManager.notify(m, mNotificationBuilder.build());
    }

    void endLoading(String errorMessage) {
        tvCommandOutput.setText(errorMessage);
        etUrl.setEnabled(true);
        progressDialog.dismiss();
        downloading = false;
    }

    private int getExceptionCode(String error) {
        String code1 = "is not a valid URL";
        String code2 = "Unable to download webpage";
        String code3 = "Failed to fetch video information";
        String code4 = "Unable to parse video information";
        String code5 = "Unable to download thumbnail";
        String code6 = "audio conversion failed";
        if (error.contains(code1)) {
            return 1;
        } else if (error.contains(code2)) {
            return 2;
        } else if (error.contains(code3)) {
            return 3;
        } else if (error.contains(code4)) {
            return 4;
        } else if (error.contains(code5)) {
            return 5;
        } else if (error.contains(code6)) {
            return 6;
        }
        return 0;
    }

    private void showStart() {
        tvCommandOutput.setText("");
        // etUrl.setEnabled(false);

    }

    private File getDownloadDir(int btnNo) {
        File downloadDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
        File youtubeAudioDir = new File(downloadDir, "FreeStuff/Free MP3");
        File youtubeVideoDir = new File(downloadDir, "FreeStuff/Free Video");

        if (btnNo < 6) {
            // MP3
            if (!youtubeAudioDir.exists()) {
                youtubeAudioDir.mkdirs();
            }
            return youtubeAudioDir;
        } else {
            // MP4
            if (!youtubeVideoDir.exists()) {
                youtubeVideoDir.mkdirs();
            }
            return youtubeVideoDir;
        }
    }

    private boolean isStoragePermissionGranted() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
                return true;
            } else {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1);
                return false;
            }
        } else {
            return true;
        }
    }

    @Override
    public void onSendData(int btnNo, int quality, VideoInfo videoInfo) {
        this.btnNo = btnNo;
        this.quality = quality;
        // dismiss bottom sheet after user has chosen to download a mp3 or mp4
        bottomSheet.dismiss();

        viewDownloadFragment = ViewDownloadFragment.newInstance(btnNo, quality);
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.add(R.id.viewDownload, viewDownloadFragment, "View Download Fragment");
        fragmentTransaction.commit();

        startDownload(videoInfo);
    }

}