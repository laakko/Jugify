package com.jukka.jugify;

import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.GridView;
import android.widget.SearchView;

import com.gigamole.navigationtabstrip.NavigationTabStrip;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import kaaes.spotify.webapi.android.models.AudioFeaturesTrack;
import kaaes.spotify.webapi.android.models.Recommendations;
import kaaes.spotify.webapi.android.models.Track;
import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;
import static com.jukka.jugify.MainActivity.spotify;


public class ExploreTab extends Fragment {

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.activity_explore_tab, container, false);

        SearchView sview = (SearchView) view.findViewById(R.id.searchView);
        final NavigationTabStrip datatimeline = view.findViewById(R.id.searchOptions);
        datatimeline.setTitles("Artists", "Albums", "Tracks", "Playlists");
        datatimeline.setAnimationDuration(300);
        datatimeline.setTabIndex(0);



        // Recommendations
        GridView gridRecommendations = (GridView) view.findViewById(R.id.gridRecommendations);
        // ArrayList<Track> recommendations = getRecommendations("asd");


        // Custom Exploration


        // Search


        return view;
    }


    public ArrayList<Track> getRecommendations(String seeds) {

        final Map<String, Object> options = new HashMap<>();
        options.put("seed_artists", seeds);
        final ArrayList<Track> trackslist = new ArrayList<Track>();

        spotify.getRecommendations(options, new Callback<Recommendations>() {
            @Override
            public void success(Recommendations recommendations, Response response) {
                for(Track track : recommendations.tracks){
                    trackslist.add(track);
                }

            }

            @Override
            public void failure(RetrofitError error) {

            }
        });

        return trackslist;
    }

}


