package com.kheileang.freemp3video;

import android.content.Context;
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
    TextView tv144, tv240, tv480, tv720, tv1080, tv360;
    MaterialCardView one, two, three, four, five, six, seven, eight, nine, ten, eleven;
    FragmentListener fragmentListener;

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

        tv144 = view.findViewById(R.id.mp4144Label);
        tv240 = view.findViewById(R.id.mp4240Label);
        tv360 = view.findViewById(R.id.mp4360Label);
        tv480 = view.findViewById(R.id.mp4480Label);
        tv720 = view.findViewById(R.id.mp4720Label);
        tv1080 = view.findViewById(R.id.mp41080Label);

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
        String minutes = min<10?"0"+min:""+min;

        tvDuration.setText(""+minutes+":"+sec);
        tvModalTitle.setText(videoInfo.getTitle());
        tvUploader.setText(videoInfo.getUploader());

        one = view.findViewById(R.id.one);
        two = view.findViewById(R.id.two);
        three = view.findViewById(R.id.three);
        four = view.findViewById(R.id.four);
        five = view.findViewById(R.id.five);
        six = view.findViewById(R.id.six);
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
        six.setOnClickListener(this::onClick);
        seven.setOnClickListener(this::onClick);
        eight.setOnClickListener(this::onClick);
        nine.setOnClickListener(this::onClick);
        ten.setOnClickListener(this::onClick);
        eleven.setOnClickListener(this::onClick);

        String[] sizes = {"....",
                "....",
                "....",
                "....",
                "....",
                "...."};

        new Thread(()->{
            // get video file size with different resolutions
            List<VideoFormat> videoFormats = videoInfo.getFormats();
            Map<Integer, DownloadFormat> downloadFormatHashMap = new HashMap<>();

            // filter out formats other than mp4
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                for(int i=0; i<videoFormats.size(); i++){
                    if(!videoFormats.get(i).getExt().equalsIgnoreCase("mp4")){
                        videoFormats.remove(i);
                    }
                }
            }

            for (VideoFormat videoFormat: videoFormats) {
                downloadFormatHashMap.put(videoFormat.getHeight(), new DownloadFormat(videoFormat.getExt(), videoFormat.getFilesize()));
            }

            DownloadFormat downloadFormat = downloadFormatHashMap.get(144);
            sizes[0] = sizeInMB(downloadFormat.getFileSize()).toString() + "MB";

            downloadFormat = downloadFormatHashMap.get(240);
            sizes[1] = sizeInMB(downloadFormat.getFileSize()).toString() + "MB";

            downloadFormat = downloadFormatHashMap.get(360);
            sizes[2] = sizeInMB(downloadFormat.getFileSize()).toString() + "MB";

            downloadFormat = downloadFormatHashMap.get(480);
            sizes[3] = sizeInMB(downloadFormat.getFileSize()).toString() + "MB";

            downloadFormat = downloadFormatHashMap.get(720);
            sizes[4] = sizeInMB(downloadFormat.getFileSize()).toString() + "MB";

            downloadFormat = downloadFormatHashMap.get(1080);
            sizes[5] = sizeInMB(downloadFormat.getFileSize()).toString() + "MB";

            if (sizes.length != 0){
                getActivity().runOnUiThread(()->{
                    tv144.setText(sizes[0]);
                    tv240.setText(sizes[1]);
                    tv360.setText(sizes[2]);
                    tv480.setText(sizes[3]);
                    tv720.setText(sizes[4]);
                    tv1080.setText(sizes[5]);
                });
            }


        }).start();
        tv144.setText(sizes[0]);
        tv240.setText(sizes[1]);
        tv360.setText(sizes[2]);
        tv480.setText(sizes[3]);
        tv720.setText(sizes[4]);
        tv1080.setText(sizes[5]);

        return view;
    }


    Float sizeInMB(Long filesize){
        Float size;
        DecimalFormat df = new DecimalFormat("#.##");
        long mb = filesize/(1024*1024);
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
            case R.id.six:
                fragmentListener.onSendData(6, 144);
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
