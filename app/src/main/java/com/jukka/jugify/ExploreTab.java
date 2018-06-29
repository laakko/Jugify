package com.jukka.jugify;

import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import static com.jukka.jugify.MainActivity.mPlayer;
import static com.jukka.jugify.UserTab.displayname;
import static com.jukka.jugify.UserTab.name_gotten;

public class ExploreTab extends Fragment {

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.activity_explore_tab, container, false);

        // mPlayer.playUri(null, "spotify:track:2TpxZ7JUBn3uw46aR7qd6V", 0, 0);
        Log.d("bool", Boolean.toString(name_gotten));
        return view;
    }
}

