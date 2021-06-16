package com.kheileang.freemp3video;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.snackbar.Snackbar;
import com.yausername.youtubedl_android.mapper.VideoFormat;
import com.yausername.youtubedl_android.mapper.VideoInfo;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.DecimalFormat;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class DownloadOptionsSheetFragment extends BottomSheetDialogFragment implements View.OnClickListener {
    VideoInfo videoInfo;

    ImageView thumbnail;
    TextView tvDuration, tvModalTitle, tvUploader;
    TextView tv240, tv480, tv720, tv1080, tv360;
    TextView tv70, tv128, tv160, tv256, tv320;
    MaterialCardView one, two, three, four, five, seven, eight, nine, ten, eleven;
    FragmentListener fragmentListener;
    long mp3Length;

    public DownloadOptionsSheetFragment(VideoInfo videoInfo) {
        this.videoInfo = videoInfo;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.download_options_layout, container, false);

        thumbnail = view.findViewById(R.id.thumbnail);
        tvDuration = view.findViewById(R.id.duration);
        tvModalTitle = view.findViewById(R.id.modal_title);
        tvUploader = view.findViewById(R.id.uploader);

        tv240 = view.findViewById(R.id.mp4240Label);
        tv360 = view.findViewById(R.id.mp4360Label);
        tv480 = view.findViewById(R.id.mp4480Label);
        tv720 = view.findViewById(R.id.mp4720Label);
        tv1080 = view.findViewById(R.id.mp41080Label);

        tv70 = view.findViewById(R.id.mp370Label);
        tv128 = view.findViewById(R.id.mp3128Label);
        tv160 = view.findViewById(R.id.mp3160Label);
        tv256 = view.findViewById(R.id.mp3256Label);
        tv320 = view.findViewById(R.id.mp3320Label);

        new Thread(()->{
            try {
                URL imgUrl = new URL(videoInfo.getThumbnail());
                Bitmap bmp = BitmapFactory.decodeStream(imgUrl.openConnection().getInputStream());
                Handler handler = new Handler(Looper.getMainLooper());
                if(bmp != null){
                    handler.post(()->{
                       thumbnail.setImageBitmap(bmp);
                    });
                }else{
                    handler.post(()->{
                        thumbnail.setImageDrawable(getResources().getDrawable(R.drawable.no_thumbnail));
                    });
                }
            } catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }).start();
        int secs = videoInfo.getDuration();
        int min = secs/60;
        int sec = secs%60;
        int hr = min>=60?min/60:0;
        min = (min % 60);
        String minutes = min<10?(min==0?"00":"0"+min):""+min;
        String hour = hr<10?(hr==0?"00":"0"+hr):""+hr;
        String seconds = sec<10?(sec==0?"00":"0"+sec):""+sec;

        tvDuration.setText(hour+":"+minutes+":"+seconds);
        tvModalTitle.setText(videoInfo.getTitle());
        tvUploader.setText(videoInfo.getUploader());

        one = view.findViewById(R.id.one);
        two = view.findViewById(R.id.two);
        three = view.findViewById(R.id.three);
        four = view.findViewById(R.id.four);
        five = view.findViewById(R.id.five);
        seven = view.findViewById(R.id.seven);
        eight = view.findViewById(R.id.eight);
        nine = view.findViewById(R.id.nine);
        ten = view.findViewById(R.id.ten);
        eleven = view.findViewById(R.id.eleven);

        one.setOnClickListener(this::onClick);
        two.setOnClickListener(this::onClick);
        three.setOnClickListener(this::onClick);
        four.setOnClickListener(this::onClick);
        five.setOnClickListener(this::onClick);
        seven.setOnClickListener(this::onClick);
        eight.setOnClickListener(this::onClick);
        nine.setOnClickListener(this::onClick);
        ten.setOnClickListener(this::onClick);
        eleven.setOnClickListener(this::onClick);

        String[] sizes = {"....",
                "....",
                "....",
                "....",
                "...."};

        new Thread(()->{
            // get video file size with different resolutions
            List<VideoFormat> videoFormats = videoInfo.getFormats();
            Map<Integer, DownloadFormat> downloadFormatHashMap = new HashMap<>();


            for (VideoFormat videoFormat: videoFormats) {
                downloadFormatHashMap.put(videoFormat.getHeight(), new DownloadFormat(videoFormat.getExt(), videoFormat.getFilesize()));
            }

            DownloadFormat downloadFormat = downloadFormatHashMap.get(240);
            if (downloadFormat != null){
                sizes[0] = sizeInMB(downloadFormat.getFileSize()).toString() + "MB";
            }else{
                sizes[0] = "false";
            }

            // get base mp3 file size
            mp3Length = downloadFormatHashMap.get(0).getFileSize()*100/10;

            tv70.setText((double)Math.round(sizeInMB(mp3Length))/100 + "MB");
            tv128.setText((double)Math.round(sizeInMB(mp3Length)*1.2)/100 + "MB");
            tv160.setText((double)Math.round(sizeInMB(mp3Length)*1.25)/100 + "MB");
            tv256.setText((double)Math.round(sizeInMB(mp3Length)*1.29)/100 + "MB");
            tv320.setText((double)Math.round(sizeInMB(mp3Length)*2)/100 +"MB");

            downloadFormat = downloadFormatHashMap.get(360);
            if (downloadFormat != null) {
                sizes[1] = sizeInMB(downloadFormat.getFileSize()).toString() + "MB";
            }else{
                sizes[1] = "false";
            }

            downloadFormat = downloadFormatHashMap.get(480);
            if (downloadFormat != null) {
                sizes[2] = sizeInMB(downloadFormat.getFileSize()).toString() + "MB";
            }else{
                sizes[2] = "false";
            }

            downloadFormat = downloadFormatHashMap.get(720);
            if (downloadFormat != null) {
                sizes[3] = sizeInMB(downloadFormat.getFileSize()).toString() + "MB";
            }else{
                sizes[3] = "false";
            }

            downloadFormat = downloadFormatHashMap.get(1080);
            if (downloadFormat != null) {
                sizes[4] = sizeInMB(downloadFormat.getFileSize()).toString() + "MB";
            }else{
                sizes[4] = "false";
            }

            if (sizes.length != 0){
                getActivity().runOnUiThread(()->{
                    if (sizes[0].contains("false")) {
                        tv240.setEnabled(false);
                        seven.setOnClickListener(null);
                    } else {
                        tv240.setText(sizes[0]);
                    }

                    if (sizes[1].contains("false")) {
                        tv360.setEnabled(false);
                        eight.setOnClickListener(null);
                    } else {
                        tv360.setText(sizes[1]);
                    }

                    if (sizes[2].contains("false")) {
                        tv480.setEnabled(false);
                        nine.setOnClickListener(null);
                    } else {
                        tv480.setText(sizes[2]);
                    }

                    if (sizes[3].contains("false")) {
                        tv720.setEnabled(false);
                        ten.setOnClickListener(null);
                    } else {
                        tv720.setText(sizes[3]);
                    }

                    if (sizes[4].contains("false")) {
                        tv1080.setEnabled(false);
                        eleven.setOnClickListener(null);
                    } else {
                        tv1080.setText(sizes[4]);
                    }

                });
            }

        }).start();
        tv240.setText(sizes[0]);
        tv360.setText(sizes[1]);
        tv480.setText(sizes[2]);
        tv720.setText(sizes[3]);
        tv1080.setText(sizes[4]);

        return view;
    }


    Float sizeInMB(Long filesize){
        Float size;
        DecimalFormat df = new DecimalFormat("#.##");
        long mb = filesize*10/(1024*1024);
        float kb = (filesize%1024)/1000f;
        size = mb+kb;
        return Float.valueOf(df.format(size));
    }

    interface FragmentListener{
        void onSendData(int btnNo, int quality);
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);

        if(context instanceof FragmentListener){
            try{
                fragmentListener = (FragmentListener)context;
            }catch (ClassCastException e){
                e.printStackTrace();
                View parentView = this.getView().findViewById(android.R.id.content);
                Snackbar.make(parentView, "You need to implement fragment listener first", Snackbar.LENGTH_LONG).show();
            }

        }
    }

    @Override
    public void onClick(View v) {
        int btnId = v.getId();

        switch (btnId){
            case R.id.one:
                fragmentListener.onSendData(1, 70);
                break;
            case R.id.two:
                fragmentListener.onSendData(2, 128);
                break;
            case R.id.three:
                fragmentListener.onSendData(3, 160);
                break;
            case R.id.four:
                fragmentListener.onSendData(4, 256);
                break;
            case R.id.five:
                fragmentListener.onSendData(5, 320);
                break;
            case R.id.seven:
                fragmentListener.onSendData(7, 240);
                break;
            case R.id.eight:
                fragmentListener.onSendData(8, 360);
                break;
            case R.id.nine:
                fragmentListener.onSendData(9, 480);
                break;
            case R.id.ten:
                fragmentListener.onSendData(10,720);
                break;
            case R.id.eleven:
                fragmentListener.onSendData(11, 1080);
                break;
        }
    }
}

class DownloadFormat{
    private String ext;
    private long fileSize;

    public DownloadFormat(String ext, long fileSize) {
        this.ext = ext;
        this.fileSize = fileSize;
    }

    public String getExt() {
        return ext;
    }

    public long getFileSize() {
        return fileSize;
    }
}
