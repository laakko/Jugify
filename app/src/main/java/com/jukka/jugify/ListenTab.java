package com.jukka.jugify;

import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.spotify.protocol.client.Subscription;
import com.spotify.protocol.types.PlayerState;
import com.spotify.protocol.types.Track;

import kaaes.spotify.webapi.android.SpotifyApi;
import kaaes.spotify.webapi.android.SpotifyService;

import static com.jukka.jugify.MainActivity.mSpotifyAppRemote;
import static com.jukka.jugify.MainActivity.spotify;
import static com.jukka.jugify.MainActivity.trackName;
import static com.jukka.jugify.MainActivity.userAuthd;

public class ListenTab extends Fragment {

    TextView txtNowPlaying;
    Button playpause;
    Button skip;
    Button prev;
    Boolean isplaying = false;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.activity_listen_tab, container, false);

        txtNowPlaying = (TextView) view.findViewById(R.id.txtNowPlaying);
        playpause = (Button) view.findViewById(R.id.btnPlay);
        skip = (Button) view.findViewById(R.id.btnNext);
        prev = (Button) view.findViewById(R.id.btnPrev);

        if(userAuthd){

            txtNowPlaying.setText(trackName);
            mSpotifyAppRemote.getPlayerApi()
                    .subscribeToPlayerState().setEventCallback(new Subscription.EventCallback<PlayerState>() {
                public void onEvent(PlayerState playerState) {
                    final Track track = playerState.track;
                    if(playerState.isPaused){
                        isplaying = false;
                    } else{
                        isplaying = true;
                    }
                    if (track != null) {
                        trackName = track.name + " by " + track.artist.name;
                        txtNowPlaying.setText(trackName);
                    }
                }
            });

            // Play/Pause, Skip and Prev buttons
            playpause.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if(isplaying){
                        mSpotifyAppRemote.getPlayerApi().pause();
                    } else {
                        mSpotifyAppRemote.getPlayerApi().resume();
                    }
                }
            });

            skip.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    mSpotifyAppRemote.getPlayerApi().skipNext();
                }
            });

            prev.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    mSpotifyAppRemote.getPlayerApi().skipPrevious();
                }
            });
        }
        return view;
    }


    public void getSongFeatures(Track track) {
    }

}