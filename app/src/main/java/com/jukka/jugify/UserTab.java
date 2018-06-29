package com.jukka.jugify;

import android.graphics.Color;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.spotify.sdk.android.player.Spotify;

import java.sql.Array;
import java.util.ArrayList;

import kaaes.spotify.webapi.android.SpotifyApi;
import kaaes.spotify.webapi.android.SpotifyService;
import kaaes.spotify.webapi.android.models.Album;
import kaaes.spotify.webapi.android.models.Artist;
import kaaes.spotify.webapi.android.models.Pager;
import kaaes.spotify.webapi.android.models.Track;
import kaaes.spotify.webapi.android.models.UserPrivate;
import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;

import static com.jukka.jugify.MainActivity.atoken;
import static com.jukka.jugify.MainActivity.spotify;
import static com.jukka.jugify.MainActivity.userAuthd;

public class UserTab extends Fragment {

    public static String displayname;
    public static boolean name_gotten = false;
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.activity_user_tab, container, false);
        TextView username = (TextView) view.findViewById(R.id.txtUsername);
        if(userAuthd) {

            spotify.getMe(new Callback<UserPrivate>() {
                @Override
                public void success(UserPrivate user, Response response) {
                    Log.d("Name ", "" + user.id);
                    name_gotten = true;
                    displayname = user.id;
                    Log.d("names", displayname);
                }

                @Override
                public void failure(RetrofitError error) {
                    Log.d("User failure", error.toString());
                }
            });

            spotify.getTopArtists(new Callback<Pager<Artist>>() {
                @Override
                public void success(Pager<Artist> pager, Response response) {
                   ArrayList<Artist> topartistslist = new ArrayList<Artist>();

                    for(Artist a : pager.items){
                        topartistslist.add(a);
                        Log.i("Artist:", a.name);
                    }
                }

                @Override
                public void failure(RetrofitError error) {
                    Log.d("Top artists failure", error.toString());
                }
            });

            spotify.getTopTracks(new Callback<Pager<Track>>() {
                @Override
                public void success(Pager<Track> pager, Response response) {
                    ArrayList<Track> toptrackslist = new ArrayList<Track>();

                    for(Track t : pager.items){
                        toptrackslist.add(t);
                        Log.i("Track:", t.name);
                    }
                }

                @Override
                public void failure(RetrofitError error) {
                    Log.d("Top tracks failure", error.toString());
                }
            });

        }

        if(name_gotten){
            username.setText("Welcome, " + displayname);
            username.setTextColor(Color.LTGRAY);
        }

        return view;
    }
}