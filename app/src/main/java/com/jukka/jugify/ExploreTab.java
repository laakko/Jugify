package com.jukka.jugify;

import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.GridView;
import android.widget.SearchView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.androidbuts.multispinnerfilter.KeyPairBoolData;
import com.androidbuts.multispinnerfilter.MultiSpinnerListener;
import com.androidbuts.multispinnerfilter.MultiSpinnerSearch;
import com.androidbuts.multispinnerfilter.SpinnerListener;
import com.gigamole.navigationtabstrip.NavigationTabStrip;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import kaaes.spotify.webapi.android.models.Artist;
import kaaes.spotify.webapi.android.models.AudioFeaturesTrack;
import kaaes.spotify.webapi.android.models.Pager;
import kaaes.spotify.webapi.android.models.Recommendations;
import kaaes.spotify.webapi.android.models.Track;
import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;

import static android.content.ContentValues.TAG;
import static com.jukka.jugify.MainActivity.spotify;
import static com.jukka.jugify.MainActivity.userAuthd;


public class ExploreTab extends Fragment {

    public String top5artists_short;
    public String top5artists_long;
    public String top5tracks_long;
    public String top5tracks_short;
    public boolean top5artists_gotten = false;
    public boolean top5tracks_gotten = false;
    String seed_genres = "";

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.activity_explore_tab, container, false);

        GridView gridRecommendations = (GridView) view.findViewById(R.id.gridRecommendations);
        TextView debug = (TextView) view.findViewById(R.id.debugTextView);

        MultiSpinnerSearch searchSpinner = (MultiSpinnerSearch) view.findViewById(R.id.genreSpinner);

        if(userAuthd) {


            // Recommendations
            if(!top5tracks_gotten) {
                getTop5Tracks();

            }

            getRecommendations(getTop5Artists());


            if(top5tracks_gotten) {
                debug.setText(top5artists_short);
            }







            // Custom Exploration
            final Map<String, Object> customExploreOptions = new HashMap<>();

            // Genre multispinner
            String[] genreseeds = getGenreSeeds();
            final List<KeyPairBoolData> listArray = new ArrayList<KeyPairBoolData>();
            for(int i=0; i<genreseeds.length; i++) {
                KeyPairBoolData h = new KeyPairBoolData();
                h.setId(i+1);
                h.setName(genreseeds[i]);
                h.setSelected(false);
                listArray.add(h);
            }

            searchSpinner.setItems(listArray, -1, new SpinnerListener() {
                @Override
                public void onItemsSelected(List<KeyPairBoolData> items) {

                    seed_genres = "";
                    for (int i = 0; i < items.size(); i++) {
                        if (items.get(i).isSelected()) {
                            seed_genres += items.get(i).getName() + ",";
                        }
                    }

                    seed_genres = seed_genres.substring(0, seed_genres.length()-1);
                    customExploreOptions.put("seed_genres", seed_genres);

                }
            });
            searchSpinner.setLimit(5, new MultiSpinnerSearch.LimitExceedListener() {
                @Override
                public void onLimitListener(KeyPairBoolData data) {
                    Toast.makeText(getContext(),
                            "Limit exceed ", Toast.LENGTH_LONG).show();
                }
            });





        }
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

    public String getTop5Artists() {
        Map<String, Object> options = new HashMap<>();
        options.put("limit", "5");
        options.put("time_range", "short_term");

        Map<String, Object> options2 = new HashMap<>();
        options2.put("limit", "5");
        options2.put("time_range", "long_term");



        spotify.getTopArtists(options, new Callback<Pager<Artist>>() {
            @Override
            public void success(Pager<Artist> artistPager, Response response) {
                top5artists_short = "";
                for(Artist a: artistPager.items) {
                    top5artists_short += a.id + ",";
                }
            }

            @Override
            public void failure(RetrofitError error) {

            }
        });

        spotify.getTopArtists(options2, new Callback<Pager<Artist>>() {
            @Override
            public void success(Pager<Artist> artistPager, Response response) {
                top5artists_long = "";
                for(Artist a: artistPager.items) {
                    top5artists_long += a.id + ",";
                }
            }

            @Override
            public void failure(RetrofitError error) {
            }
        });

        top5artists_gotten = true;
        return top5artists_short;

    }

    public void getTop5Tracks() {
        Map<String, Object> options = new HashMap<>();
        options.put("limit", "5");
        options.put("time_range", "short_term");

        Map<String, Object> options2 = new HashMap<>();
        options2.put("limit", "5");
        options2.put("time_range", "long_term");

        spotify.getTopTracks(options, new Callback<Pager<Track>>() {
            @Override
            public void success(Pager<Track> trackPager, Response response) {
                top5tracks_short = "";
                for(Track t : trackPager.items) {
                    top5tracks_short += t.id + ",";
                }
            }

            @Override
            public void failure(RetrofitError error) {

            }
        });

        spotify.getTopTracks(options2, new Callback<Pager<Track>>() {
            @Override
            public void success(Pager<Track> trackPager, Response response) {
                top5tracks_long = "";
                for(Track t : trackPager.items) {
                    top5tracks_long += t.id + ",";
                }
            }

            @Override
            public void failure(RetrofitError error) {

            }
        });

        top5tracks_gotten = true;
    }

    public String[] getGenreSeeds(){

        String[] genreSeeds =   {
                "acoustic",
                "afrobeat",
                "alt-rock",
                "alternative",
                "ambient",
                "anime",
                "black-metal",
                "bluegrass",
                "blues",
                "bossanova",
                "brazil",
                "breakbeat",
                "british",
                "cantopop",
                "chicago-house",
                "children",
                "chill",
                "classical",
                "club",
                "comedy",
                "country",
                "dance",
                "dancehall",
                "death-metal",
                "deep-house",
                "detroit-techno",
                "disco",
                "disney",
                "drum-and-bass",
                "dub",
                "dubstep",
                "edm",
                "electro",
                "electronic",
                "emo",
                "folk",
                "forro",
                "french",
                "funk",
                "garage",
                "german",
                "gospel",
                "goth",
                "grindcore",
                "groove",
                "grunge",
                "guitar",
                "happy",
                "hard-rock",
                "hardcore",
                "hardstyle",
                "heavy-metal",
                "hip-hop",
                "holidays",
                "honky-tonk",
                "house",
                "idm",
                "indian",
                "indie",
                "indie-pop",
                "industrial",
                "iranian",
                "j-dance",
                "j-idol",
                "j-pop",
                "j-rock",
                "jazz",
                "k-pop",
                "kids",
                "latin",
                "latino",
                "malay",
                "mandopop",
                "metal",
                "metal-misc",
                "metalcore",
                "minimal-techno",
                "movies",
                "mpb",
                "new-age",
                "new-release",
                "opera",
                "pagode",
                "party",
                "philippines-opm",
                "piano",
                "pop",
                "pop-film",
                "post-dubstep",
                "power-pop",
                "progressive-house",
                "psych-rock",
                "punk",
                "punk-rock",
                "r-n-b",
                "rainy-day",
                "reggae",
                "reggaeton",
                "road-trip",
                "rock",
                "rock-n-roll",
                "rockabilly",
                "romance",
                "sad",
                "salsa",
                "samba",
                "sertanejo",
                "show-tunes",
                "singer-songwriter",
                "ska",
                "sleep",
                "songwriter",
                "soul",
                "soundtracks",
                "spanish",
                "study",
                "summer",
                "swedish",
                "synth-pop",
                "tango",
                "techno",
                "trance",
                "trip-hop",
                "turkish",
                "work-out",
                "world-music"   };

        return genreSeeds;
    }

}


