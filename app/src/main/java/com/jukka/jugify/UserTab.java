package com.jukka.jugify;

import android.graphics.Color;
import android.support.annotation.ColorInt;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
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
        final GridView gridTopArtists = (GridView) view.findViewById(R.id.gridTopArtists);
        final ListView listTopTracks = (ListView) view.findViewById(R.id.listTopTracks);
        GridView gridPlaylists = (GridView) view.findViewById(R.id.gridPlaylists);
        final Button btnshort = (Button) view.findViewById(R.id.btnShort);
        final Button btnmedium = (Button) view.findViewById(R.id.btnMedium);
        final Button btnlong = (Button) view.findViewById(R.id.btnLong);

        Log.i("topartistsgotten:", Boolean.toString(topartists_gotten));

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



            final Map<String, Object> options = new HashMap<>();

            if(!topartists_gotten){
                options.put("time_range", "medium_term");
                btnmedium.setBackgroundColor(Color.parseColor("#4C40AD"));
                tagadapter = new TopArtistsGridAdapter(getContext().getApplicationContext(), topartistslist);
                TopArtists(options, tagadapter, gridTopArtists);

            } else {
                if(options.containsValue("short_term")){
                    btnshort.setBackgroundColor(Color.parseColor("#4C40AD"));
                } else if(options.containsValue("medium_term")){
                    btnmedium.setBackgroundColor(Color.parseColor("#4C40AD"));
                } else if(options.containsValue("long_term")){
                    btnlong.setBackgroundColor(Color.parseColor("#4C40AD"));
                }
                gridTopArtists.setAdapter(tagadapter);
            }

            if(!toptracks_gotten) {
                trackadapter = new TopTracksListAdapter(getContext().getApplicationContext(), toptrackslist);
                TopTracks(options, trackadapter, listTopTracks);
            } else {
                listTopTracks.setAdapter(trackadapter);
            }

            btnshort.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    btnshort.setBackgroundColor(Color.parseColor("#4C40AD"));
                    btnmedium.setBackgroundColor(Color.parseColor("#427DD1"));
                    btnlong.setBackgroundColor(Color.parseColor("#427DD1"));
                    options.clear();
                    options.put("time_range", "short_term");
                    TopArtists(options, tagadapter, gridTopArtists);
                    TopTracks(options, trackadapter, listTopTracks);
                }
            });
            btnmedium.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    btnshort.setBackgroundColor(Color.parseColor("#427DD1"));
                    btnmedium.setBackgroundColor(Color.parseColor("#4C40AD"));
                    btnlong.setBackgroundColor(Color.parseColor("#427DD1"));
                    options.clear();
                    options.put("time_range", "medium_term");
                    TopArtists(options, tagadapter, gridTopArtists);
                    TopTracks(options, trackadapter, listTopTracks);
                }
            });
            btnlong.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    btnshort.setBackgroundColor(Color.parseColor("#427DD1"));
                    btnmedium.setBackgroundColor(Color.parseColor("#427DD1"));
                    btnlong.setBackgroundColor(Color.parseColor("#4C40AD"));
                    options.clear();
                    options.put("time_range", "long_term");
                    TopArtists(options, tagadapter, gridTopArtists);
                    TopTracks(options, trackadapter, listTopTracks);
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



        }

        if(name_gotten){
            username.setText("Hello, " + displayname);
            username.setTextColor(Color.LTGRAY);
        }

        return view;
    }

    // Helper functions for Spotify Web API queries
    public void TopArtists( Map<String, Object> options, final TopArtistsGridAdapter adapter, final GridView grid) {

        spotify.getTopArtists(options, new Callback<Pager<Artist>>() {
            @Override
            public void success(Pager<Artist> pager, Response response) {

                adapter.clear();
                int counter=0;
                for(Artist a : pager.items){
                    counter++;
                    if(counter<10){
                        adapter.add(a);
                    }
                }

                topartists_gotten = true;
                grid.setAdapter(adapter);
            }

            @Override
            public void failure(RetrofitError error) {
                Log.d("Top artists failure", error.toString());
            }
        });
    }

    public void TopTracks( Map<String, Object> options, final TopTracksListAdapter adapter, final ListView list) {
        spotify.getTopTracks(options, new Callback<Pager<Track>>() {
            @Override
            public void success(Pager<Track> pager, Response response) {

                adapter.clear();
                for(Track t : pager.items){
                    adapter.add(t);
                }

                toptracks_gotten = true;
                list.setAdapter(adapter);
            }

            @Override
            public void failure(RetrofitError error) {
                Log.d("Top tracks failure", error.toString());
            }
        });
    }
}