package com.kheileang.freemp3video;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentStatePagerAdapter;

public class TabAdapter extends FragmentStatePagerAdapter {
    Context context;
    int tabLen;

    public TabAdapter(@NonNull FragmentManager fm, int behavior, Context context, int tabLen) {
        super(fm, behavior);
        this.context = context;
        this.tabLen = tabLen;
    }

    @NonNull
    @Override
    public Fragment getItem(int position) {
        switch (position) {
            case 0:
                return new mp3Fragment();
            case 1:
                return new VideoFragment();
        }
        return new mp3Fragment();
    }

    @Override
    public int getCount() {
        return tabLen;
    }
}
