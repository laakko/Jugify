package com.jukka.jugify;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.TransitionDrawable;
import android.support.annotation.ColorInt;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.graphics.Palette;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AnimationUtils;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.gigamole.navigationtabstrip.NavigationTabStrip;
import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.utils.ColorTemplate;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;
import com.nostra13.universalimageloader.core.assist.ImageSize;
import com.nostra13.universalimageloader.core.listener.ImageLoadingListener;
import com.nostra13.universalimageloader.core.listener.SimpleImageLoadingListener;
import com.spotify.sdk.android.player.Spotify;

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
    public static boolean myplaylists_gotten = false;
    public static boolean myalbums_gotten = false;
    public static boolean trackinfo_gotten = false;
    public ArrayList<Artist> topartistslist = new ArrayList<Artist>();
    public ArrayList<Track> toptrackslist = new ArrayList<>();
    public ArrayList<PlaylistSimple> myplaylistslist = new ArrayList<>();
    public ArrayList<SavedAlbum> myalbumslist = new ArrayList<>();
    public static TopArtistsGridAdapter tagadapter;
    public static TopTracksListAdapter trackadapter;
    public static MyPlaylistsGridAdapter padapter;
    public static MyAlbumsGridAdapter albadapter;
    private PieData piedata;
    private PieDataSet piedataset;
    private PopupWindow popup;
    public String userid;
    public int trackinfoCounter;
    private float acousticnesAvg, danceabilityAvg, valencyAvg, energyAvg, tempoAvg, instruAvg, popularityAvg;
    private int durationAvg;
    TextView txtAvgTempo;
    TextView txtAvgDuration;
    TextView txtTopGenres;
    ProgressBar AvgValenceBar, AvgEnergyBar, AvgDanceBar, AvgInstruBar, AvgPopularityBar;
    HashMap<String, Double> genreList = new HashMap<String, Double>();



    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.activity_user_tab, container, false);
        final TextView username = (TextView) view.findViewById(R.id.txtUsername);
        final GridView gridTopArtists = (GridView) view.findViewById(R.id.gridTopArtists);
        final ListView listTopTracks = (ListView) view.findViewById(R.id.listTopTracks);
        final GridView gridPlaylists = (GridView) view.findViewById(R.id.gridPlaylists);
        final GridView gridAlbums = (GridView) view.findViewById(R.id.gridAlbums);
        txtTopGenres = (TextView) view.findViewById(R.id.txtTopGenres);
        txtAvgTempo = (TextView) view.findViewById(R.id.avgTempo);
        txtAvgDuration = (TextView) view.findViewById(R.id.avgDuration);
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


            spotify.getMe(new Callback<UserPrivate>() {
                @Override
                public void success(UserPrivate user, Response response) {
                    username.setText("Hello, " + user.id);
                    userid = user.id;
                    username.setTextColor(Color.LTGRAY);
                }
                @Override
                public void failure(RetrofitError error) {
                    Log.d("User failure", error.toString());
                }
            });



            final Map<String, Object> options = new HashMap<>();
            final Map<String, Object> optionsAlbum = new HashMap<>();
            optionsAlbum.put("limit", "50");


            if(!topartists_gotten){
                options.put("time_range", "medium_term");
                tagadapter = new TopArtistsGridAdapter(getContext().getApplicationContext(), topartistslist);
                TopArtists(options, tagadapter, gridTopArtists);

            } else {
                gridTopArtists.setAdapter(tagadapter);
            }

            if(!toptracks_gotten) {

                trackadapter = new TopTracksListAdapter(getContext().getApplicationContext(), toptrackslist);
                TopTracks(options, trackadapter, listTopTracks);

            } else {
                listTopTracks.setAdapter(trackadapter);
            }
            listTopTracks.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                   mSpotifyAppRemote.getPlayerApi().play(trackadapter.getItem(i).uri);
                   // mSpotifyAppRemote.getPlayerApi().queue(trackadapter.getItem(i).uri);
                    toast("Now playing: "+trackadapter.getItem(i).name, R.drawable.ic_play_circle_outline_black_36dp, Color.BLACK);
                }
            });
            listTopTracks.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
                @Override
                public boolean onItemLongClick(AdapterView<?> adapterView, View view, int i, long l) {
                    mSpotifyAppRemote.getPlayerApi().queue(trackadapter.getItem(i).uri);
                    toast("Added to queue: "+trackadapter.getItem(i).name, R.drawable.ic_queue_black_36dp, Color.BLACK);
                    return true;
                }
            });

            if(!myplaylists_gotten){
                padapter = new MyPlaylistsGridAdapter(getContext().getApplicationContext(), myplaylistslist);
                MyPlaylists(padapter, gridPlaylists);
            } else {
                gridPlaylists.setAdapter(padapter);
            }

            gridPlaylists.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
                @Override
                public boolean onItemLongClick(AdapterView<?> adapterView, View view, int i, long l) {
                    // Play the clicked playlist
                    mSpotifyAppRemote.getPlayerApi().play(padapter.getItem(i).uri);
                    toast("Now playing: "+padapter.getItem(i).name, R.drawable.ic_playlist_play_black_36dp, Color.BLACK);
                    return true;
                }
            });

            gridPlaylists.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {

                    LayoutInflater inflater = (LayoutInflater) getActivity().getSystemService(LAYOUT_INFLATER_SERVICE);
                    View layout = inflater.inflate(R.layout.popup_album,
                            (ViewGroup) view.findViewById(R.id.tab_layout_2));

                    popup = new PopupWindow(layout, MATCH_PARENT, MATCH_PARENT, true);
                    popup.showAtLocation(layout, Gravity.TOP, 0, 0);

                    // SavedAlbum popupAlbum = padapter.getItem(i);
                    PlaylistSimple playlist = padapter.getItem(i);

                    final String popupPlaylistInfo = playlist.name + "\n by " + playlist.owner.id;

                    final LinearLayout popupbg = layout.findViewById(R.id.popupBG);
                    final ImageView popupImg = layout.findViewById(R.id.imgPopupAlbumImg);
                    final TextView popupalbumname = layout.findViewById(R.id.txtPopupAlbumName);
                    popupalbumname.setText(popupPlaylistInfo);
                    final TextView popupinfo = layout.findViewById(R.id.txtPopupInfo2);
                    popupinfo.setText(playlist.tracks.total + " tracks");

                    final ListView popuplist = layout.findViewById(R.id.listPopupTracks);
                    ImageLoader imgloader = ImageLoader.getInstance();

                    DisplayImageOptions defaultOptions = new DisplayImageOptions.Builder()
                            .showStubImage(R.drawable.baseline_album_24).build();
                    ImageLoaderConfiguration config = new ImageLoaderConfiguration.Builder(getContext()).defaultDisplayImageOptions(defaultOptions).build();
                    ImageSize targetSize = new ImageSize(200, 200); // result Bitmap will be fit to this size
                    imgloader.loadImage(playlist.images.get(0).url, targetSize, defaultOptions, new SimpleImageLoadingListener() {
                        @Override
                        public void onLoadingComplete(String imageUri, View view, Bitmap loadedImage) {
                            popupImg.setImageBitmap(loadedImage);

                            Palette p = Palette.from(loadedImage).maximumColorCount(8).generate();
                            Palette.Swatch vibrant;
                            try {
                                vibrant = p.getVibrantSwatch();
                                popupbg.setBackgroundColor(vibrant.getRgb());
                                popupalbumname.setTextColor(vibrant.getTitleTextColor());
                                popupinfo.setTextColor(vibrant.getBodyTextColor());
                            } catch (NullPointerException e) {
                                vibrant = p.getDominantSwatch();
                                popupbg.setBackgroundColor(vibrant.getRgb());
                                popupalbumname.setTextColor(vibrant.getTitleTextColor());
                                popupinfo.setTextColor(vibrant.getBodyTextColor());
                            }


                        }

                    });

                    final TracksListAdapter popuptrackadapter = new TracksListAdapter(getContext().getApplicationContext(), new ArrayList<TrackSimple>(), true);
                    popuptrackadapter.clear();

                    spotify.getPlaylistTracks(userid, playlist.id, new Callback<Pager<PlaylistTrack>>() {
                        @Override
                        public void success(Pager<PlaylistTrack> pager, Response response) {


                            for(PlaylistTrack p : pager.items){
                                popuptrackadapter.add(p.track);
                            }

                            myplaylists_gotten = true;
                            popuplist.setAdapter(popuptrackadapter);
                            popuplist.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                                @Override
                                public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                                    mSpotifyAppRemote.getPlayerApi().play(popuptrackadapter.getItem(i).uri);
                                    toast("Now playing: "+ popuptrackadapter.getItem(i).name, R.drawable.ic_play_circle_outline_black_36dp, Color.BLACK);
                                }
                            });
                        }

                        @Override
                        public void failure(RetrofitError error) {
                            Log.d("My playlists failure", error.toString());
                        }
                    });


                }
            });

            if(!myalbums_gotten){
                albadapter = new MyAlbumsGridAdapter(getContext().getApplicationContext(), myalbumslist);
                MyAlbums(optionsAlbum, albadapter, gridAlbums);
            } else {
                gridAlbums.setAdapter(albadapter);
            }

            gridAlbums.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
                @Override
                public boolean onItemLongClick(AdapterView<?> adapterView, View view, int i, long l) {
                    // Play the clicked album
                    mSpotifyAppRemote.getPlayerApi().play(albadapter.getItem(i).album.uri);
                    toast("Now playing: "+albadapter.getItem(i).album.name, R.drawable.baseline_album_24, Color.BLACK);

                    return true;
                }
            });

            gridAlbums.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {

                    LayoutInflater inflater = (LayoutInflater) getActivity().getSystemService(LAYOUT_INFLATER_SERVICE);
                    View layout = inflater.inflate(R.layout.popup_album,
                            (ViewGroup) view.findViewById(R.id.tab_layout_2));

                    popup = new PopupWindow(layout, MATCH_PARENT, MATCH_PARENT, true);
                    popup.showAtLocation(layout, Gravity.TOP, 0, 0);

                    SavedAlbum popupAlbum = albadapter.getItem(i);
                    final String popupAlbumInfo = popupAlbum.album.name + "\n by " + popupAlbum.album.artists.get(0).name;
                    final LinearLayout popupbg = layout.findViewById(R.id.popupBG);
                    final ImageView popupImg = layout.findViewById(R.id.imgPopupAlbumImg);
                    final TextView popupalbumname = layout.findViewById(R.id.txtPopupAlbumName);
                    popupalbumname.setText(popupAlbumInfo);
                    final TextView popupinfo = layout.findViewById(R.id.txtPopupInfo2);
                    popupinfo.setText("Released " + popupAlbum.album.release_date);

                    ListView popuplist = layout.findViewById(R.id.listPopupTracks);
                    ImageLoader imgloader = ImageLoader.getInstance();


                    DisplayImageOptions defaultOptions = new DisplayImageOptions.Builder()
                            .showStubImage(R.drawable.baseline_album_24).build();
                    ImageLoaderConfiguration config = new ImageLoaderConfiguration.Builder(getContext()).defaultDisplayImageOptions(defaultOptions).build();
                    ImageSize targetSize = new ImageSize(200 , 200); // result Bitmap will be fit to this size
                    imgloader.loadImage(popupAlbum.album.images.get(0).url, targetSize, defaultOptions, new SimpleImageLoadingListener() {
                        @Override
                        public void onLoadingComplete(String imageUri, View view, Bitmap loadedImage) {
                            popupImg.setImageBitmap(loadedImage);

                            Palette p = Palette.from(loadedImage).maximumColorCount(8).generate();
                            Palette.Swatch vibrant;
                            try {
                                vibrant = p.getVibrantSwatch();
                                popupbg.setBackgroundColor(vibrant.getRgb());
                                popupalbumname.setTextColor(vibrant.getTitleTextColor());
                                popupinfo.setTextColor(vibrant.getBodyTextColor());
                            } catch(NullPointerException e) {
                                vibrant = p.getDominantSwatch();
                                popupbg.setBackgroundColor(vibrant.getRgb());
                                popupalbumname.setTextColor(vibrant.getTitleTextColor());
                                popupinfo.setTextColor(vibrant.getBodyTextColor());
                            }






                        }
                    });


                    final TracksListAdapter popuptrackadapter = new TracksListAdapter(getContext().getApplicationContext(), new ArrayList<TrackSimple>(), false);
                    popuptrackadapter.clear();
                    for(TrackSimple track : popupAlbum.album.tracks.items) {
                        popuptrackadapter.add(track);
                    }

                    popuplist.setAdapter(popuptrackadapter);
                    popuplist.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                        @Override
                        public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                            mSpotifyAppRemote.getPlayerApi().play(popuptrackadapter.getItem(i).uri);
                            toast("Now playing: "+ popuptrackadapter.getItem(i).name, R.drawable.ic_play_circle_outline_black_36dp, Color.BLACK);
                        }
                    });
                }
            });

            gridTopArtists.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                    ArtistPopup(tagadapter.getItem(i) ,view);
                }
            });


            datatimeline.setOnTabStripSelectedIndexListener(new NavigationTabStrip.OnTabStripSelectedIndexListener() {
                @Override
                public void onStartTabSelected(String title, int index) {
                    if(index == 0) {
                        options.clear();
                        options.put("time_range", "short_term");
                        TopArtists(options, tagadapter, gridTopArtists);
                        TopTracks(options, trackadapter, listTopTracks);
                    } else if(index == 1) {
                        options.clear();
                        options.put("time_range", "medium_term");
                        TopArtists(options, tagadapter, gridTopArtists);
                        TopTracks(options, trackadapter, listTopTracks);
                    } else if(index == 2) {
                        options.clear();
                        options.put("time_range", "long_term");
                        TopArtists(options, tagadapter, gridTopArtists);
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
    public void TopArtists( Map<String, Object> options, final TopArtistsGridAdapter adapter, final GridView grid) {

        spotify.getTopArtists(options, new Callback<Pager<Artist>>() {
            @Override
            public void success(Pager<Artist> pager, Response response) {

                adapter.clear();
                genreList.clear();
                int counter=0;

                for(Artist a : pager.items){
                    counter++;

                    // Add artist to GridView
                    if(counter<13) { adapter.add(a); }


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
                        
                    } catch(NullPointerException npe) {
                        npe.printStackTrace();
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


                String topgenres = ""; int genrecount = 1;
                for(Object topgenre : obj){
                    if(genrecount < 11) {
                        topgenres += "#" + Integer.toString(genrecount) + " " + ((Map.Entry<String, Double>) topgenre).getKey() + "\n";
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
                energyAvg = 0; tempoAvg = 0; instruAvg = 0; durationAvg = 0;
                adapter.clear();
                for(Track t : pager.items){
                    adapter.add(t);
                    popularityAvg += t.popularity;
                    getAudioInfo(t.id);
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


    public void MyAlbums(Map<String, Object> options, final MyAlbumsGridAdapter adapter, final GridView grid) {
        spotify.getMySavedAlbums(options, new Callback<Pager<SavedAlbum>>() {
            @Override
            public void success(Pager<SavedAlbum> savedAlbumPager, Response response) {

                adapter.clear();
                for(SavedAlbum sa : savedAlbumPager.items){
                    adapter.add(sa);
                }

                myalbums_gotten = true;
                grid.setAdapter(adapter);
            }

            @Override
            public void failure(RetrofitError error) {
                Log.d("My albums failure", error.toString());
            }
        });
    }

    public void MyPlaylists(final MyPlaylistsGridAdapter adapter, final GridView grid) {

        spotify.getMyPlaylists(new Callback<Pager<PlaylistSimple>>() {
            @Override
            public void success(Pager<PlaylistSimple> pager, Response response) {

                adapter.clear();
                for(PlaylistSimple p : pager.items){
                    adapter.add(p);
                }

                myplaylists_gotten = true;
                grid.setAdapter(adapter);
            }

            @Override
            public void failure(RetrofitError error) {
                Log.d("My playlists failure", error.toString());
            }
        });
    }


    public void myDevices() {

    }


    public void getAudioInfo(String uri) {


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

                    String tempoRounded = Float.toString((int)Math.round(tempoAvg));
                    String duration = String.format(Locale.US, "%d:%02d",
                            TimeUnit.MILLISECONDS.toMinutes(durationAvg),
                            TimeUnit.MILLISECONDS.toSeconds(durationAvg) -
                                    TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(durationAvg)));

                    txtAvgTempo.setText(tempoRounded + " BPM");
                    txtAvgDuration.setText(duration);

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



    public void ArtistPopup(Artist artist, View view){

        LayoutInflater inflater = (LayoutInflater) getActivity().getSystemService(LAYOUT_INFLATER_SERVICE);
        View layout = inflater.inflate(R.layout.popup_artist,
                (ViewGroup) view.findViewById(R.id.tab_layout_2));

        popup = new PopupWindow(layout, MATCH_PARENT, MATCH_PARENT, true);
        popup.showAtLocation(layout, Gravity.TOP, 0, 0);


        final TextView popupArtistName = layout.findViewById(R.id.txtPopupArtistName);
        final TextView popupArtistInfo = layout.findViewById(R.id.popupArtistInfo);
        final ImageView popupArtistImage = layout.findViewById(R.id.popupArtistImg);
        final LinearLayout popupArtistBG = layout.findViewById(R.id.popupArtistBG);
        popupArtistName.setText(artist.name);


        String popupArtistGenres = "Genres: ";
        for(int i=0; i< artist.genres.size(); ++i){
            popupArtistGenres += artist.genres.get(i);
        }

        popupArtistGenres += "\n" + artist.followers.total + " followers";

        popupArtistInfo.setText(popupArtistGenres);

        ImageLoader imgloader = ImageLoader.getInstance();
        DisplayImageOptions defaultOptions = new DisplayImageOptions.Builder()
                .showStubImage(R.drawable.baseline_album_24).build();
        ImageLoaderConfiguration config = new ImageLoaderConfiguration.Builder(getContext()).defaultDisplayImageOptions(defaultOptions).build();
        ImageSize targetSize = new ImageSize(200 , 200); // result Bitmap will be fit to this size
        imgloader.loadImage(artist.images.get(0).url, targetSize, defaultOptions, new SimpleImageLoadingListener() {
            @Override
            public void onLoadingComplete(String imageUri, View view, Bitmap loadedImage) {
                popupArtistImage.setImageBitmap(loadedImage);

                Palette p = Palette.from(loadedImage).maximumColorCount(8).generate();
                Palette.Swatch vibrant;
                try {
                    vibrant = p.getVibrantSwatch();
                    popupArtistBG.setBackgroundColor(vibrant.getRgb());
                    popupArtistName.setTextColor(vibrant.getTitleTextColor());
                    popupArtistInfo.setTextColor(vibrant.getBodyTextColor());
                } catch(NullPointerException e) {
                    vibrant = p.getDominantSwatch();
                    popupArtistBG.setBackgroundColor(vibrant.getRgb());
                    popupArtistName.setTextColor(vibrant.getTitleTextColor());
                    popupArtistInfo.setTextColor(vibrant.getBodyTextColor());
                }

            }
        });



        /*
        spotify.getArtistTopTrack(artist.id, "finland", new Callback<Pager<Album>>() {
            @Override
            public void success(Pager<Album> pager, Response response) {


                for(Album p : pager.items){
                    popuptrackadapter.add(p);
                }

                popuplist.setAdapter(popuptrackadapter);
                popuplist.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                    @Override
                    public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                        // Play track
                    }
                });
            }

            @Override
            public void failure(RetrofitError error) {
                Log.d("My playlists failure", error.toString());
            }
        });

        */

    };



    public void toast(String message, int drawable, int tintcolor) {
        Toasty.custom(getContext(), message, drawable, tintcolor, 700, true, true).show();
    }

}
