package com.jukka.jugify;

import android.graphics.Color;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.GridView;
import android.widget.ListView;
import android.widget.TextView;

import com.spotify.sdk.android.player.Spotify;

import java.sql.Array;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import kaaes.spotify.webapi.android.SpotifyApi;
import kaaes.spotify.webapi.android.SpotifyService;
import kaaes.spotify.webapi.android.models.Album;
import kaaes.spotify.webapi.android.models.Artist;
import kaaes.spotify.webapi.android.models.Pager;
import kaaes.spotify.webapi.android.models.PlaylistSimple;
import kaaes.spotify.webapi.android.models.Track;
import kaaes.spotify.webapi.android.models.Tracks;
import kaaes.spotify.webapi.android.models.UserPrivate;
import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;
import retrofit.http.QueryMap;

import static com.jukka.jugify.MainActivity.atoken;
import static com.jukka.jugify.MainActivity.spotify;
import static com.jukka.jugify.MainActivity.userAuthd;

public class UserTab extends Fragment {

    public static String displayname;
    public static boolean name_gotten = false;
    public static boolean topartists_gotten = false;
    public static boolean toptracks_gotten = false;
    public ArrayList<Artist> topartistslist = new ArrayList<Artist>();
    public ArrayList<Track> toptrackslist = new ArrayList<>();
    public static TopArtistsGridAdapter tagadapter;
    public static TopTracksListAdapter trackadapter;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.activity_user_tab, container, false);
        TextView username = (TextView) view.findViewById(R.id.txtUsername);
        if(userAuthd) {

            spotify.getMe(new Callback<UserPrivate>() {
                @Override
                public void success(UserPrivate user, Response response) {
                    name_gotten = true;
                    displayname = user.id;

                }
                @Override
                public void failure(RetrofitError error) {
                    Log.d("User failure", error.toString());
                }
            });

            Map<String, Object> options = new HashMap<>();
            options.put("time_range", "long_term");
            spotify.getTopArtists(options, new Callback<Pager<Artist>>() {
                @Override
                public void success(Pager<Artist> pager, Response response) {
                    tagadapter = new TopArtistsGridAdapter(getContext().getApplicationContext(), topartistslist);
                    int counter=0;
                    for(Artist a : pager.items){
                        counter++;
                        if(counter<10){
                            tagadapter.add(a);
                        }
                    }


                    topartists_gotten = true;
                }

                @Override
                public void failure(RetrofitError error) {
                    Log.d("Top artists failure", error.toString());
                }
            });


            spotify.getTopTracks(options, new Callback<Pager<Track>>() {
                @Override
                public void success(Pager<Track> pager, Response response) {

                    trackadapter = new TopTracksListAdapter(getContext().getApplicationContext(), toptrackslist);

                    for(Track t : pager.items){
                        trackadapter.add(t);
                    }

                    toptracks_gotten = true;
                }

                @Override
                public void failure(RetrofitError error) {
                    Log.d("Top tracks failure", error.toString());
                }
            });

            spotify.getMyPlaylists(new Callback<Pager<PlaylistSimple>>() {
                @Override
                public void success(Pager<PlaylistSimple> pager, Response response) {

                    for(PlaylistSimple p : pager.items){
                        Log.i("playlist2:", p.name);
                    }
                }

                @Override
                public void failure(RetrofitError error) {
                    Log.d("My playlists failure", error.toString());
                }
            });


            GridView gridTopArtists = (GridView) view.findViewById(R.id.gridTopArtists);
            if(topartists_gotten){
                gridTopArtists.setAdapter(tagadapter);
            }
            ListView listTopTracks = (ListView) view.findViewById(R.id.listTopTracks);
            if(toptracks_gotten){
                listTopTracks.setAdapter((trackadapter));
            }
            GridView gridPlaylists = (GridView) view.findViewById(R.id.gridPlaylists);


        }

        if(name_gotten){
            username.setText("Hello, " + displayname);
            username.setTextColor(Color.LTGRAY);
        }

        return view;
    }
}