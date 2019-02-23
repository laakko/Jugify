package com.jukka.jugify;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.TransitionDrawable;
import android.support.annotation.ColorInt;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.graphics.Palette;
import android.util.AttributeSet;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AnimationUtils;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.GridView;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.gigamole.navigationtabstrip.NavigationTabStrip;
import com.google.gson.JsonObject;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;
import com.nostra13.universalimageloader.core.assist.ImageSize;
import com.nostra13.universalimageloader.core.listener.ImageLoadingListener;
import com.nostra13.universalimageloader.core.listener.SimpleImageLoadingListener;
import com.spotify.sdk.android.player.Spotify;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.sql.Array;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import es.dmoral.toasty.Toasty;
import kaaes.spotify.webapi.android.SpotifyApi;
import kaaes.spotify.webapi.android.SpotifyService;
import kaaes.spotify.webapi.android.models.Album;
import kaaes.spotify.webapi.android.models.Artist;
import kaaes.spotify.webapi.android.models.Artists;
import kaaes.spotify.webapi.android.models.AudioFeaturesTrack;
import kaaes.spotify.webapi.android.models.Pager;
import kaaes.spotify.webapi.android.models.Playlist;
import kaaes.spotify.webapi.android.models.PlaylistSimple;
import kaaes.spotify.webapi.android.models.PlaylistTrack;
import kaaes.spotify.webapi.android.models.SavedAlbum;
import kaaes.spotify.webapi.android.models.Track;
import kaaes.spotify.webapi.android.models.TrackSimple;
import kaaes.spotify.webapi.android.models.Tracks;
import kaaes.spotify.webapi.android.models.UserPrivate;
import retrofit.Callback;
import retrofit.RequestInterceptor;
import retrofit.RestAdapter;
import retrofit.RetrofitError;
import retrofit.client.Response;
import retrofit.http.QueryMap;


import static android.content.Context.LAYOUT_INFLATER_SERVICE;
import static android.view.ViewGroup.LayoutParams.MATCH_PARENT;
import static com.jukka.jugify.MainActivity.atoken;
import static com.jukka.jugify.MainActivity.mSpotifyAppRemote;
import static com.jukka.jugify.MainActivity.spotify;
import static com.jukka.jugify.MainActivity.userAuthd;

public class UserTab extends Fragment {

    public static boolean topartists_gotten = false;
    public static boolean toptracks_gotten = false;

    public static boolean trackinfo_gotten = false;
    public ArrayList<Artist> topartistslist = new ArrayList<Artist>();
    public ArrayList<Track> toptrackslist = new ArrayList<>();

    public static TopArtistsGridAdapter tagadapter;
    public static TopTracksListAdapter trackadapter;



    public int trackinfoCounter;
    public static boolean targetsGotten = false;
    public static float acousticnesAvg, danceabilityAvg, valencyAvg, energyAvg, tempoAvg, instruAvg, popularityAvg;
    private int durationAvg, releaseyAvg;
    TextView txtAvgTempo, txtAvgDuration, txtAvgReleaseYear, txtTopGenres;
    ProgressBar AvgValenceBar, AvgEnergyBar, AvgDanceBar, AvgInstruBar, AvgPopularityBar;
    HashMap<String, Double> genreList = new HashMap<String, Double>();
    Common cm = new Common();
    String topgenres = "";

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.activity_user_tab, container, false);
       // final GridView gridTopArtists = (GridView) view.findViewById(R.id.gridTopArtists);
        final ListView listTopTracks = (ListView) view.findViewById(R.id.listTopTracks);
        final ListView listTopArtists = (ListView) view.findViewById(R.id.listTopArtists);


        txtTopGenres = (TextView) view.findViewById(R.id.txtTopGenres);
        txtAvgTempo = (TextView) view.findViewById(R.id.avgTempo);
        txtAvgDuration = (TextView) view.findViewById(R.id.avgDuration);
        txtAvgReleaseYear = (TextView) view.findViewById(R.id.avgReleaseYear);
        AvgDanceBar = (ProgressBar) view.findViewById(R.id.avgDanceability);
        AvgEnergyBar = (ProgressBar) view.findViewById(R.id.avgEnergy);
        AvgValenceBar = (ProgressBar) view.findViewById(R.id.avgValence);
        AvgInstruBar = (ProgressBar) view.findViewById(R.id.avgInstrumentalness);
        AvgPopularityBar = (ProgressBar) view.findViewById(R.id.avgPopularity);
        Toasty.Config.getInstance().setTextColor(getResources().getColor(R.color.colorAccent)).apply();

        final NavigationTabStrip datatimeline = view.findViewById(R.id.datatimeline);
        datatimeline.setTitles("SHORT", "MEDIUM", "LONG");
        datatimeline.setAnimationDuration(300);
        datatimeline.setTabIndex(1);

        if(userAuthd) {


            final Map<String, Object> options = new HashMap<>();
            options.put("time_range", "medium_term");
            if(!topartists_gotten){
                tagadapter = new TopArtistsGridAdapter(getContext().getApplicationContext(), topartistslist);
                TopArtists(options, tagadapter, listTopArtists);
            } else {
                TopArtists(options, tagadapter, listTopArtists);
            }

            listTopArtists.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                    cm.ArtistPopup(tagadapter.getItem(i) ,view, false, getContext());
                }
            });


            if(!toptracks_gotten) {
                trackadapter = new TopTracksListAdapter(getContext().getApplicationContext(), toptrackslist);
                TopTracks(options, trackadapter, listTopTracks);
            } else {
                TopTracks(options, trackadapter, listTopTracks);
            }

            listTopTracks.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                   mSpotifyAppRemote.getPlayerApi().play(trackadapter.getItem(i).uri);
                   cm.toast("Now playing: "+trackadapter.getItem(i).name, R.drawable.ic_play_circle_outline_black_36dp, Color.BLACK, getContext());
                }
            });
            listTopTracks.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
                @Override
                public boolean onItemLongClick(AdapterView<?> adapterView, View view, int i, long l) {
                    mSpotifyAppRemote.getPlayerApi().queue(trackadapter.getItem(i).uri);
                    cm.toast("Added to queue: "+trackadapter.getItem(i).name, R.drawable.ic_queue_black_36dp, Color.BLACK, getContext());
                    return true;
                }
            });




            datatimeline.setOnTabStripSelectedIndexListener(new NavigationTabStrip.OnTabStripSelectedIndexListener() {
                @Override
                public void onStartTabSelected(String title, int index) {
                    if(index == 0) {
                        options.clear();
                        options.put("time_range", "short_term");
                        TopArtists(options, tagadapter, listTopArtists);
                        TopTracks(options, trackadapter, listTopTracks);
                    } else if(index == 1) {
                        options.clear();
                        options.put("time_range", "medium_term");
                        TopArtists(options, tagadapter, listTopArtists);
                        TopTracks(options, trackadapter, listTopTracks);
                    } else if(index == 2) {
                        options.clear();
                        options.put("time_range", "long_term");
                        TopArtists(options, tagadapter, listTopArtists);
                        TopTracks(options, trackadapter, listTopTracks);
                    }
                }

                @Override
                public void onEndTabSelected(String title, int index) {

                }
            });

        }

        return view;
    }

    // Helper functions for Spotify Web API queries
    public void TopArtists( Map<String, Object> options, final TopArtistsGridAdapter adapter, final ListView grid) {

        spotify.getTopArtists(options, new Callback<Pager<Artist>>() {
            @Override
            public void success(Pager<Artist> pager, Response response) {

                adapter.clear();
                genreList.clear();
                int counter=0;

                for(Artist a : pager.items){
                    counter++;

                    // Add artist to GridView
                    if(counter<21) { adapter.add(a); }

                    // Get top genres based on artist rank + occurence
                    try{
                        if(genreList.containsKey(a.genres.get(0))) {
                            Double prev_value = genreList.get(a.genres.get(0));
                            genreList.put(a.genres.get(0), prev_value + 0.5+((1.0/counter)*2.0));
                        } else {
                            genreList.put(a.genres.get(0), 0.5+((1.0/counter)*2.0));
                        }

                        try{
                            if(genreList.containsKey(a.genres.get(1))) {
                                Double prev_value2 = genreList.get(a.genres.get(1));
                                genreList.put(a.genres.get(1), prev_value2 + 0.5+((1.0/counter)*2.0));
                            } else {
                                genreList.put(a.genres.get(1), 0.5+((1.0/counter)*2.0));
                            }
                        } catch(IndexOutOfBoundsException ie) {
                            ie.printStackTrace();
                        }
                        
                    } catch(IndexOutOfBoundsException ie) {
                        ie.printStackTrace();
                    }

                }

                // Sort the genres
                Object[] obj = genreList.entrySet().toArray();
                Arrays.sort(obj, new Comparator() {
                    public int compare(Object o1, Object o2) {
                        return ((Map.Entry<String, Double>) o2).getValue()
                                .compareTo(((Map.Entry<String, Double>) o1).getValue());
                    }
                });


                int genrecount = 1;
                topgenres = "";
                for(Object topgenre : obj){
                    if(genrecount < 11) {
                        topgenres += ((Map.Entry<String, Double>) topgenre).getKey() + "\n";
                    }

                    genrecount++;
                }


                txtTopGenres.setText(topgenres);



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

                trackinfoCounter = 0;
                acousticnesAvg = 0; danceabilityAvg = 0; valencyAvg = 0; popularityAvg = 0;
                energyAvg = 0; tempoAvg = 0; instruAvg = 0; durationAvg = 0; releaseyAvg = 0;
                adapter.clear();

                for(Track t : pager.items){
                    adapter.add(t);
                    popularityAvg += t.popularity;
                    getAudioInfo(t.id, t.album.id);
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



    public void myDevices() {
        // TODO

    }




    public void getAudioInfo(String uri, String uri2) {

        // Get AVG release year
        spotify.getAlbum(uri2, new Callback<Album>() {
            @Override
            public void success(Album album, Response response) {
                releaseyAvg += Integer.parseInt(album.release_date.substring(0,4));
            }

            @Override
            public void failure(RetrofitError error) {

            }
        });

        // Get audio info
        spotify.getTrackAudioFeatures(uri, new Callback<AudioFeaturesTrack>() {

            @Override
            public void success(AudioFeaturesTrack aft, Response response){


                acousticnesAvg += aft.acousticness;
                danceabilityAvg += aft.danceability;
                valencyAvg += aft.valence;
                energyAvg += aft.energy;
                instruAvg += aft.instrumentalness;
                tempoAvg += aft.tempo;
                durationAvg += aft.duration_ms;
                trackinfoCounter += 1;
                if(trackinfoCounter == 20) {
                    tempoAvg = tempoAvg / 20;
                    acousticnesAvg = acousticnesAvg / 20;
                    danceabilityAvg = danceabilityAvg / 20;
                    valencyAvg = valencyAvg / 20;
                    energyAvg = energyAvg / 20;
                    instruAvg = instruAvg / 20;
                    durationAvg = durationAvg / 20;
                    popularityAvg = popularityAvg / 20;
                    releaseyAvg = releaseyAvg / 20;

                    targetsGotten = true;

                    String tempoRounded = Float.toString((int)Math.round(tempoAvg));
                    String duration = String.format(Locale.US, "%d:%02d",
                            TimeUnit.MILLISECONDS.toMinutes(durationAvg),
                            TimeUnit.MILLISECONDS.toSeconds(durationAvg) -
                                    TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(durationAvg)));

                    txtAvgTempo.setText(tempoRounded + " BPM");
                    txtAvgDuration.setText(duration);
                    txtAvgReleaseYear.setText(Integer.toString(releaseyAvg));

                    AvgEnergyBar.setProgress((int)Math.round(100*energyAvg));
                    AvgValenceBar.setProgress((int)Math.round(100*valencyAvg));
                    AvgDanceBar.setProgress((int)Math.round(100*danceabilityAvg));
                    AvgInstruBar.setProgress((int)Math.round(100*instruAvg));
                    AvgPopularityBar.setProgress((int)Math.round(popularityAvg));

                }
            }

            @Override
            public void failure(RetrofitError error){
                Log.d("Audio features failure", error.toString());
            }
        });
    }




}
