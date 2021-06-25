package com.kheileang.freemp3video;

import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.yausername.youtubedl_android.YoutubeDL;
import com.yausername.youtubedl_android.YoutubeDLException;

import java.io.File;
import java.text.DecimalFormat;

public class SettingsActivity extends AppCompatActivity {
    private TextView mRate, mShare, mApps, mPolicy, mHowTo, mDemo, mNotWorking, mFeedback;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.settings_activity);
        initializeCache();
        initializeLocation();
        initializeLibVersion();
    }

    private void initializeLibVersion() {
        new Thread(()->{
            String version = YoutubeDL.getInstance().version(this);
            runOnUiThread(()->{
                ((TextView)findViewById(R.id.libVersion)).setText("v"+version);
            });
        }).start();
    }

    private void initializeLocation() {
        String audio_loc = Environment.DIRECTORY_DOWNLOADS+"/FreeStuff/Free Mp3";
        String video_loc = Environment.DIRECTORY_DOWNLOADS+"/FreeStuff/Free Video";
        ((TextView)findViewById(R.id.audio_location)).setText(audio_loc);
        ((TextView)findViewById(R.id.video_location)).setText(video_loc);
    }

    private void initializeCache() {
        long size = 0;
        size += getDirSize(this.getCacheDir());
        size += getDirSize(this.getExternalCacheDirs());
        ((TextView) findViewById(R.id.cacheSize)).setText(readableFileSize(size));
    }

    public long getDirSize(File[] dirs) {
        long size = 0;
        for (File dir : dirs) {
            for (File file : dir.listFiles()) {
                if (file != null && file.isDirectory()) {
                    size += getDirSize(file);
                } else if (file != null && file.isFile()) {
                    size += file.length();
                }
            }
        }
        return size;
    }

    public long getDirSize(File dir) {
        long size = 0;
        for (File file : dir.listFiles()) {
            if (file != null && file.isDirectory()) {
                size += getDirSize(file);
            } else if (file != null && file.isFile()) {
                size += file.length();
            }
        }
        return size;
    }

    public static String readableFileSize(long size) {
        if (size <= 0) return "0";
        final String[] units = new String[]{"B", "kB", "MB", "GB", "TB"};
        int digitGroups = (int) (Math.log10(size) / Math.log10(1024));
        return new DecimalFormat("#,##0.#").format(size / Math.pow(1024, digitGroups)) + " " + units[digitGroups];
    }

    public void onClick(View view) {
        int id = view.getId();

        switch (id) {
            case R.id.rate_setting:
                rateMyApp();
                break;
            case R.id.share_setting:
                shareMyApp();
                break;
            case R.id.other_app_setting:
                viewOtherApps();
                break;
            case R.id.policy_setting:
                showPolicy();
                break;
            case R.id.howto_setting:
                showHowTo();
                break;
            case R.id.demo_setting:
                showDemo();
                break;
            case R.id.notworking_setting:
                reportDev("App Not Working", "App Not Working");
                break;
            case R.id.feedback_setting:
                reportDev("Feedback", "Your app can be improved..");
                break;
            case R.id.clear_cache:
                clearCache();
                break;
            case R.id.update_ytdl:
                updateYoutubeDl();
                break;
        }
    }

    private void updateYoutubeDl() {
        ProgressDialog progressDialog = new ProgressDialog(this);
        progressDialog.setIndeterminate(true);
        progressDialog.setCancelable(false);
        progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        progressDialog.setMessage("Updating Library..");
        progressDialog.show();

        new Thread(()->{
            try {
                YoutubeDL.UpdateStatus status = YoutubeDL.getInstance().updateYoutubeDL(getApplicationContext());
                switch (status){
                    case DONE:
                        runOnUiThread(()->{
                            Toast.makeText(this, "Update Finished.", Toast.LENGTH_SHORT).show();
                            progressDialog.dismiss();
                        });
                        break;
                    case ALREADY_UP_TO_DATE:
                        runOnUiThread(()->{
                            Toast.makeText(this, "Already Up To Date", Toast.LENGTH_SHORT).show();
                            progressDialog.dismiss();
                        });
                        break;
                    default:
                        runOnUiThread(()->{
                            Toast.makeText(this, status.toString(), Toast.LENGTH_SHORT).show();
                            progressDialog.dismiss();
                        });
                        break;
                }
            } catch (YoutubeDLException e) {
                e.printStackTrace();
                runOnUiThread(()->{progressDialog.dismiss();});
            }
        }).start();
    }

    private void clearCache() {
        File cacheDir = this.getCacheDir();

        File[] files = cacheDir.listFiles();

        if (files != null) {
            for (File file : files)
                file.delete();
        }
        ((TextView) findViewById(R.id.cacheSize)).setText("0");
        Toast.makeText(this, "Cache cleared.", Toast.LENGTH_SHORT).show();
    }

    private void reportDev(String subject, String body) {
        Intent intent = new Intent(Intent.ACTION_SENDTO);
        intent.setData(Uri.parse("mailto:" + "lover.music.sick@gmail.com"));
        intent.putExtra(Intent.EXTRA_SUBJECT, subject);
        intent.putExtra(Intent.EXTRA_TEXT, body);

        startActivity(Intent.createChooser(intent, "Send Email"));
    }


    private void showDemo() {
        Toast.makeText(this, "Video unavailable", Toast.LENGTH_SHORT).show();
    }


    private void showHowTo() {
        AlertDialog.Builder howTo = new AlertDialog.Builder(this);
        howTo.setTitle("How To Use");
        howTo.setMessage(R.string.howto);
        howTo.setCancelable(true);
        howTo.setPositiveButton("OK", null);
        howTo.show();

    }

    private void showPolicy() {
        final String privacyUrl = "https://sites.google.com/view/alpa-privacy-policy";

        startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(privacyUrl)));
    }

    private void viewOtherApps() {
        final String comName = "King";

        startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?pub=" + comName)));
    }

    private void shareMyApp() {
        final String appPackageName = "https://play.google.com/store/apps/details?id=com.king.candycrushsaga";

        Intent intent = new Intent(Intent.ACTION_SEND);
        intent.setType("text/plain");
        intent.putExtra(Intent.EXTRA_SUBJECT, "MY APP");
        intent.putExtra(Intent.EXTRA_TEXT, appPackageName);
        startActivity(Intent.createChooser(intent, "Share Via"));
    }

    private void rateMyApp() {
        final String appPackageName = "com.king.candycrushsaga";
        try {
            startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=" + appPackageName)));
        } catch (android.content.ActivityNotFoundException e) {
            startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("market://detail?id=" + appPackageName)));
        }
    }


}