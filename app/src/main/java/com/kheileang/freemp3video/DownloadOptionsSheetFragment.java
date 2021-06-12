package com.kheileang.freemp3video;

import android.accounts.NetworkErrorException;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
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
import androidx.fragment.app.Fragment;

import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.yausername.youtubedl_android.mapper.VideoInfo;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;

import static android.provider.ContactsContract.CommonDataKinds.Website.URL;

public class DownloadOptionsSheetFragment extends BottomSheetDialogFragment {
    VideoInfo videoInfo;

    ImageView thumbnail;
    TextView tvDuration, tvModalTitle, tvUploader;

    public DownloadOptionsSheetFragment(VideoInfo videoInfo) {
        this.videoInfo = videoInfo;

        Bundle bundle = new Bundle();
        bundle.putString("title", videoInfo.getFulltitle());
        bundle.putInt("duration", videoInfo.getDuration());
        bundle.putString("thumbnail", videoInfo.getThumbnail());

        videoInfo.getUploader();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.download_options_layout, container, false);

        thumbnail = view.findViewById(R.id.thumbnail);
        tvDuration = view.findViewById(R.id.duration);
        tvModalTitle = view.findViewById(R.id.modal_title);
        tvUploader = view.findViewById(R.id.uploader);

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

        tvDuration.setText(""+(min<10?"0"+min:min)+":"+sec);
        tvModalTitle.setText(videoInfo.getTitle());
        tvUploader.setText(videoInfo.getUploader());




        return view;
    }
}
