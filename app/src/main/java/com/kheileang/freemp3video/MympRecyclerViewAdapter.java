package com.kheileang.freemp3video;

import androidx.recyclerview.widget.RecyclerView;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.media.MediaPlayer;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.List;

public class MympRecyclerViewAdapter extends RecyclerView.Adapter<MympRecyclerViewAdapter.ViewHolder> {

    private final List<Mp3Mp4> mValues;
    Context context;

    public MympRecyclerViewAdapter(List<Mp3Mp4> items) {
        mValues = items;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.fragment_mp3, parent, false);
        context = parent.getContext();
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, int position) {
        holder.mItem = mValues.get(position);
        holder.mContentView.setText(mValues.get(position).getTitle());
    }

    @Override
    public int getItemCount() {
        return mValues.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        public final View mView;
        public final TextView mContentView;
        public Mp3Mp4 mItem;
        public ImageView musicPlay, musicDelete, musicShare;
        public MediaPlayer mp;

        public ViewHolder(View view) {
            super(view);
            mView = view;
            mContentView = view.findViewById(R.id.item_title);
            musicPlay = view.findViewById(R.id.musicPlay);
            musicDelete = view.findViewById(R.id.musicDelete);
            musicShare = view.findViewById(R.id.musicShare);

            musicPlay.setOnClickListener(this::onClick);
            musicDelete.setOnClickListener(this::onClick);
            musicShare.setOnClickListener(this::onClick);

            mp = new MediaPlayer();
        }

        @Override
        public String toString() {
            return super.toString() + " '" + mContentView.getText() + "'";
        }

        @Override
        public void onClick(View v) {
            int id = v.getId();
            switch (id) {
                case R.id.musicPlay:
                    Intent intent = new Intent(Intent.ACTION_VIEW);
                    intent.setType("audio/*");
                    intent.putExtra(Intent.EXTRA_STREAM, Uri.parse(mItem.getContentUri()));
                    intent.setData(Uri.parse(mItem.getContentUri()));
                    context.startActivity(intent);
                    break;
                case R.id.musicShare:
                    Intent intent1 = new Intent(Intent.ACTION_SEND);
                    intent1.setType("audio/*");
                    intent1.putExtra(Intent.EXTRA_TITLE, mItem.getTitle());
                    intent1.putExtra(Intent.EXTRA_SUBJECT, mItem.getTitle());
                    intent1.putExtra(Intent.EXTRA_STREAM, Uri.parse(mItem.getContentUri()));
                    context.startActivity(Intent.createChooser(intent1, "Share via"));
                    break;
                case R.id.musicDelete:

                    AlertDialog.Builder alertDialog = new AlertDialog.Builder(context);
                    alertDialog.setTitle("Are you sure you want to delete " + mItem.getTitle() + "?")
                            .setPositiveButton("Delete", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    try {
                                        if (context.getContentResolver().delete(Uri.parse(mItem.getContentUri()), null, null) > 0) {
                                            Animation anim = AnimationUtils.loadAnimation(context, R.anim.slide_out_right);
                                            anim.setDuration(1000);
                                            mView.startAnimation(anim);
                                            Toast.makeText(context, mItem.getTitle() + " is deleted", Toast.LENGTH_SHORT).show();
                                        }
                                    }catch (SecurityException exception){
                                        exception.printStackTrace();
                                        Toast.makeText(context, "You're trying to delete file no longer existing.", Toast.LENGTH_SHORT).show();
                                    }
                                    new Thread(() -> {
                                        mValues.remove(mItem);
                                        MediaScannerConnection.scanFile(context, new String[]{

                                                        mItem.getData()},

                                                null, new MediaScannerConnection.OnScanCompletedListener() {

                                                    public void onScanCompleted(String path, Uri uri) {
                                                        Handler handler = new Handler(Looper.getMainLooper());
                                                        handler.postDelayed(new Runnable() {
                                                            @Override
                                                            public void run() {
                                                                notifyDataSetChanged();
                                                            }
                                                        },1000);
                                                    }

                                                });
                                    }).start();
                                }
                            })
                            .setNegativeButton("Cancel", null)
                            .setCancelable(true)
                            .show();
                    break;
            }
        }
    }
}

class Mp3Mp4 {
    String title;
    Boolean mp3;
    String contentUri;
    String data;
    Long id;

    public Mp3Mp4(String title, Boolean mp3, String contentUri, Long id, String data) {
        this.title = title;
        this.mp3 = mp3;
        this.contentUri = contentUri;
        this.id = id;
        this.data = data;
    }

    public String getData() {
        return data;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public Boolean getMp3() {
        return mp3;
    }

    public void setMp3(Boolean mp3) {
        this.mp3 = mp3;
    }

    public String getContentUri() {
        return contentUri;
    }

    public void setContentUri(String contentUri) {
        this.contentUri = contentUri;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }
}