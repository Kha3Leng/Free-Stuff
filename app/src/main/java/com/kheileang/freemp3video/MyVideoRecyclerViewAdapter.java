package com.kheileang.freemp3video;

import androidx.recyclerview.widget.RecyclerView;

import android.app.Activity;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.media.MediaPlayer;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.Handler;
import android.os.Looper;
import android.util.Size;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;
import java.util.List;

public class MyVideoRecyclerViewAdapter extends RecyclerView.Adapter<MyVideoRecyclerViewAdapter.ViewHolder> {

    private final List<Mp3Mp4> mValues;
    Context context;

    public MyVideoRecyclerViewAdapter(List<Mp3Mp4> items) {
        mValues = items;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.fragment_video, parent, false);
        context = parent.getContext();
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, int position) {
        holder.mItem = mValues.get(position);
        Bitmap thumbnail = null;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.Q) {
            try {
                thumbnail = context.getContentResolver()
                        .loadThumbnail(Uri.parse(holder.mItem.getContentUri()),
                                new Size(100, 100),
                                null);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        holder.vidThumbnail.setImageBitmap(thumbnail);
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
        public ImageView musicPlay, musicDelete, musicShare, vidThumbnail;
        public MediaPlayer mp;

        public ViewHolder(View view) {
            super(view);
            mView = view;
            mContentView = view.findViewById(R.id.video_title);
            musicPlay = view.findViewById(R.id.musicPlay);
            musicDelete = view.findViewById(R.id.musicDelete);
            musicShare = view.findViewById(R.id.musicShare);
            vidThumbnail = view.findViewById(R.id.vidthumbnail);

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
                    intent.setType("video/*");
                    intent.setData(Uri.parse(mItem.getContentUri()));
                    context.startActivity(intent);
                    break;
                case R.id.musicShare:
                    Intent intent1 = new Intent(Intent.ACTION_SEND);
                    intent1.setData(Uri.parse(mItem.getContentUri()));
                    intent1.setType("video/*");
                    intent1.putExtra(Intent.EXTRA_TITLE, mItem.getTitle());
                    intent1.putExtra(Intent.EXTRA_SUBJECT, mItem.getTitle());
                    intent1.putExtra(Intent.EXTRA_STREAM, Uri.parse(mItem.getContentUri()));
                    context.startActivity(Intent.createChooser(intent1, "Share via"));
                    break;
                case R.id.musicDelete:
                    if (context.getContentResolver().delete(Uri.parse(mItem.getContentUri()), null, null)>0)
                        Toast.makeText(context, mItem.getTitle() + " is deleted", Toast.LENGTH_SHORT).show();
                    mValues.remove(mItem);
                    new Thread(()->{
                        MediaScannerConnection.scanFile(context, new String[]{

                                        mItem.getData()},

                                null, new MediaScannerConnection.OnScanCompletedListener() {

                                    public void onScanCompleted(String path, Uri uri) {
                                        Handler handler = new Handler(Looper.getMainLooper());
                                        handler.post(new Runnable() {
                                            @Override
                                            public void run() {
                                                notifyDataSetChanged();
                                                /*if (mValues.size()<1){
                                                    FragmentManager fragmentManager = ((Activity)context).getFragmentManager();
                                                    FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
                                                    fragmentTransaction.replace(R.id.vidlist, new NoDataFragment());
                                                }*/
                                            }
                                        });
                                    }

                                });
                    }).start();
                    break;
            }
        }
    }
}