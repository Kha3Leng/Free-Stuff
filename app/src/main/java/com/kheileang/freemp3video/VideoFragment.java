package com.kheileang.freemp3video;

import android.content.ContentUris;
import android.content.Context;
import android.database.Cursor;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Handler;
import android.os.Looper;
import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import java.util.ArrayList;

public class VideoFragment extends Fragment {

    private static final String ARG_COLUMN_COUNT = "column-count";
    private int mColumnCount = 2;
    ArrayList<Mp3Mp4> mp3Mp4;
    public static MyVideoRecyclerViewAdapter adapter;

    public VideoFragment() {
    }

    public static mp3Fragment newInstance(int columnCount) {
        mp3Fragment fragment = new mp3Fragment();
        Bundle args = new Bundle();
        args.putInt(ARG_COLUMN_COUNT, columnCount);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (getArguments() != null) {
            mColumnCount = getArguments().getInt(ARG_COLUMN_COUNT);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_mp3_list, container, false);

        mp3Mp4 = new ArrayList<>();

        Uri collection = MediaStore.Video.Media.EXTERNAL_CONTENT_URI;
        String[] projections = {MediaStore.Video.Media._ID,
                MediaStore.Video.Media.DATA,
                MediaStore.Video.Media.DISPLAY_NAME,
                MediaStore.Video.Media.DATE_TAKEN};
        String selections = MediaStore.Video.Media.DATA + " like ?";
        String[] selectionsArgs = new String[]{"%FreeStuff%"};
        String sortOrder = MediaStore.Video.Media.DATE_ADDED + " DESC";

        Cursor videoCursor = getActivity()
                .getApplicationContext()
                .getContentResolver()
                .query(collection, projections, selections, selectionsArgs, sortOrder);

        int idCol = videoCursor.getColumnIndexOrThrow(MediaStore.Video.Media._ID);
        int titleCol = videoCursor.getColumnIndexOrThrow(MediaStore.Video.Media.DISPLAY_NAME);
        int dataCol = videoCursor.getColumnIndexOrThrow(MediaStore.Video.Media.DATA);

        while(videoCursor.moveToNext()){
            long id = videoCursor.getLong(idCol);
            String title = videoCursor.getString(titleCol);
            String data = videoCursor.getString(dataCol);

            String contentUri = ContentUris.withAppendedId(collection, id).toString();

            mp3Mp4.add(new Mp3Mp4(title,false, contentUri, id, data));
        }

        // if no data show warning
        if (mp3Mp4.size()<1){
            view = inflater.inflate(R.layout.fragment_no_data, container, false);
            return view;
        }

        adapter = new MyVideoRecyclerViewAdapter(mp3Mp4);

        // Set the adapter
        if (view instanceof RecyclerView) {
            Context context = view.getContext();
            RecyclerView recyclerView = (RecyclerView) view;
            if (mColumnCount <= 1) {
                recyclerView.setLayoutManager(new LinearLayoutManager(context));
            } else {
                recyclerView.setLayoutManager(new GridLayoutManager(context, mColumnCount));
            }
            recyclerView.setAdapter(adapter);
        }
        return view;
    }
}