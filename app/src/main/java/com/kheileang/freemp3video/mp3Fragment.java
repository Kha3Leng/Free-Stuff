package com.kheileang.freemp3video;

import android.content.ContentUris;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import java.util.ArrayList;

public class mp3Fragment extends Fragment {

    private static final String ARG_COLUMN_COUNT = "column-count";
    private int mColumnCount = 1;
    ArrayList<Mp3Mp4> mp3Mp4;
    public static MympRecyclerViewAdapter adapter;

    public mp3Fragment() {
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

        Uri collection = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
        String[] projections = {MediaStore.Audio.Media._ID,
                MediaStore.Audio.Media.DATA,
                MediaStore.Audio.Media.DISPLAY_NAME,
                MediaStore.Audio.Media.DATE_TAKEN};
        String selections = MediaStore.Audio.Media.DATA + " like ?";
        String[] selectionsArgs = new String[]{"%FreeStuff%"};
        String sortOrder = MediaStore.Audio.Media.DATE_ADDED + " DESC";

        Cursor mp3Cursor = getActivity()
                .getApplicationContext()
                .getContentResolver()
                .query(collection, projections, selections, selectionsArgs, sortOrder);

        int idCol = mp3Cursor.getColumnIndexOrThrow(MediaStore.Audio.Media._ID);
        int titleCol = mp3Cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DISPLAY_NAME);
        int dataCol = mp3Cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DATA);

        while(mp3Cursor.moveToNext()){
            long id = mp3Cursor.getLong(idCol);
            String title = mp3Cursor.getString(titleCol);
            String data = mp3Cursor.getString(dataCol);

            String contentUri = ContentUris.withAppendedId(collection, id).toString();

            mp3Mp4.add(new Mp3Mp4(title,true, contentUri, id, data));
        }

        // if no data show warning
        if (mp3Mp4.size()<1){
            view = inflater.inflate(R.layout.fragment_no_data, container, false);
            return view;
        }

        adapter = new MympRecyclerViewAdapter(mp3Mp4);

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