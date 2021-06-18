package com.kheileang.freemp3video;

import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.kheileang.freemp3video.dummy.DummyContent.DummyItem;

import java.util.List;

public class MympRecyclerViewAdapter extends RecyclerView.Adapter<MympRecyclerViewAdapter.ViewHolder> {

    private final List<Mp3Mp4> mValues;

    public MympRecyclerViewAdapter(List<Mp3Mp4> items) {
        mValues = items;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.fragment_mp3, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, int position) {
        holder.mItem = mValues.get(position);
        holder.mIdView.setText(mValues.get(position).getTitle());
        holder.mContentView.setText(String.valueOf(mValues.get(position).getId()));
    }

    @Override
    public int getItemCount() {
        return mValues.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        public final View mView;
        public final TextView mIdView;
        public final TextView mContentView;
        public Mp3Mp4 mItem;

        public ViewHolder(View view) {
            super(view);
            mView = view;
            mIdView = (TextView) view.findViewById(R.id.item_number);
            mContentView = (TextView) view.findViewById(R.id.content);
        }

        @Override
        public String toString() {
            return super.toString() + " '" + mContentView.getText() + "'";
        }
    }
}

class Mp3Mp4{
    String title;
    Boolean mp3;
    String contentUri;
    Long id;

    public Mp3Mp4(String title, Boolean mp3, String contentUri, Long id) {
        this.title = title;
        this.mp3 = mp3;
        this.contentUri = contentUri;
        this.id = id;
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