package com.kheileang.freemp3video;

import androidx.recyclerview.widget.RecyclerView;

import android.content.Context;
import android.content.Intent;
import android.media.MediaPlayer;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.Environment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.kheileang.freemp3video.dummy.DummyContent.DummyItem;

import java.io.IOException;
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
                    intent.setData(Uri.parse(mItem.getContentUri()));
                    context.startActivity(intent);
                    break;
                case R.id.musicShare:

                    Toast.makeText(context, "Music is shared", Toast.LENGTH_SHORT).show();
                    break;
                case R.id.musicDelete:
                    if (context.getContentResolver().delete(Uri.parse(mItem.getContentUri()), null, null)>0)
                        Toast.makeText(context, mItem.getTitle() + " is deleted", Toast.LENGTH_SHORT).show();

                    MediaScannerConnection.scanFile(context, new String[]{

                                    mItem.getData()},

                            null, new MediaScannerConnection.OnScanCompletedListener() {

                                public void onScanCompleted(String path, Uri uri) {
                                    mValues.remove(mItem);
                                    notifyDataSetChanged();
                                }

                            });
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