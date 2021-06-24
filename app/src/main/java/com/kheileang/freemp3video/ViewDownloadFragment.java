package com.kheileang.freemp3video;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;


public class ViewDownloadFragment extends Fragment implements View.OnClickListener {

    private int btnNo;
    private int quality;

    TextView tvDownloadView;
    public static ProgressBar pbLoading;
    public static ImageView ivFinish;
    public static TextView tvPercent;

    public ViewDownloadFragment(int btnNo, int quality) {
        // Required empty public constructor
        this.btnNo = btnNo;
        this.quality = quality;
    }

    // TODO: Rename and change types and number of parameters
    public static ViewDownloadFragment newInstance(int btnNo, int quality) {
        ViewDownloadFragment fragment = new ViewDownloadFragment(btnNo, quality);
        Bundle args = new Bundle();
        args.putInt("btnNo", btnNo);
        args.putInt("quality", quality);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            btnNo = getArguments().getInt("btnNo");
            quality = getArguments().getInt("quality");
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_view_download, container, false);
        tvDownloadView = view.findViewById(R.id.go_download);

        pbLoading = view.findViewById(R.id.loading);
        pbLoading.setProgress(0);

        ivFinish = view.findViewById(R.id.finish);
        tvPercent = view.findViewById(R.id.percent);
        tvPercent.setText("View Downloading...");

        tvDownloadView.setOnClickListener(this);
        return view;
    }

    @Override
    public void onClick(View v) {
        Intent intent = new Intent(getActivity(), DownloadActivity.class);
        Bundle bundle = new Bundle();
        bundle.putInt("btnNo", btnNo);
        bundle.putInt("quality", quality);
        intent.putExtra("btn", bundle);
        startActivity(intent);

        // remove this fragment
        getActivity().getSupportFragmentManager().beginTransaction().remove(this).commit();
    }
}