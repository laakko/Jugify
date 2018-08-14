package com.jukka.jugify;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.TransitionDrawable;
import android.support.annotation.ColorInt;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
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
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.TextView;

import com.gigamole.navigationtabstrip.NavigationTabStrip;
import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.utils.ColorTemplate;
import com.spotify.sdk.android.player.Spotify;

import java.sql.Array;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import es.dmoral.toasty.Toasty;
import kaaes.spotify.webapi.android.SpotifyApi;
import kaaes.spotify.webapi.android.SpotifyService;
import kaaes.spotify.webapi.android.models.Album;
import kaaes.spotify.webapi.android.models.Artist;
import kaaes.spotify.webapi.android.models.Pager;
import kaaes.spotify.webapi.android.models.PlaylistSimple;
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
    public ArrayList<Artist> topartistslist = new ArrayList<Artist>();
    public ArrayList<Track> toptrackslist = new ArrayList<>();
    public ArrayList<PlaylistSimple> myplaylistslist = new ArrayList<>();
    public ArrayList<SavedAlbum> myalbumslist = new ArrayList<>();
    public static TopArtistsGridAdapter tagadapter;
    public static TopTracksListAdapter trackadapter;
    public static MyPlaylistsGridAdapter padapter;
    public static MyAlbumsGridAdapter albadapter;
    private static ArrayList<Entry> pientrys = new ArrayList<>();
    private static ArrayList<String> pietitles = new ArrayList<String>();
    private PieData piedata;
    private PieDataSet piedataset;
    private PopupWindow popup;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.activity_user_tab, container, false);
        final TextView username = (TextView) view.findViewById(R.id.txtUsername);
        final GridView gridTopArtists = (GridView) view.findViewById(R.id.gridTopArtists);
        final ListView listTopTracks = (ListView) view.findViewById(R.id.listTopTracks);
        final GridView gridPlaylists = (GridView) view.findViewById(R.id.gridPlaylists);
        final GridView gridAlbums = (GridView) view.findViewById(R.id.gridAlbums);
        final PieChart genrePie = (PieChart) view.findViewById(R.id.genrePie);
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
                    username.setTextColor(Color.LTGRAY);
                }
                @Override
                public void failure(RetrofitError error) {
                    Log.d("User failure", error.toString());
                }
            });



            final Map<String, Object> options = new HashMap<>();

            if(!topartists_gotten){
                options.put("time_range", "medium_term");
                tagadapter = new TopArtistsGridAdapter(getContext().getApplicationContext(), topartistslist);
                TopArtists(options, tagadapter, gridTopArtists, genrePie);

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

            gridPlaylists.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                    // Play the clicked playlist
                    mSpotifyAppRemote.getPlayerApi().play(padapter.getItem(i).uri);
                    toast("Now playing: "+padapter.getItem(i).name, R.drawable.ic_playlist_play_black_36dp, Color.BLACK);
                }
            });

            if(!myalbums_gotten){
                albadapter = new MyAlbumsGridAdapter(getContext().getApplicationContext(), myalbumslist);
                MyAlbums(albadapter, gridAlbums);
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
                    String popupAlbumInfo = popupAlbum.album.name + "\n by " + popupAlbum.album.artists.get(0).name;

                    final TextView popupalbumname = layout.findViewById(R.id.txtPopupAlbumName);
                    popupalbumname.setText(popupAlbumInfo);

                    final TextView popupinfo = layout.findViewById(R.id.txtPopupInfo2);
                    popupinfo.setText("Released " + popupAlbum.album.release_date);

                    ListView popuplist = layout.findViewById(R.id.listPopupTracks);

                    final TracksListAdapter popuptrackadapter = new TracksListAdapter(getContext().getApplicationContext(), new ArrayList<TrackSimple>());
                    popuptrackadapter.clear();
                    for(TrackSimple track : popupAlbum.album.tracks.items) {
                        popuptrackadapter.add(track);
                    }

                    popuplist.setAdapter(popuptrackadapter);
                    popuplist.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                        @Override
                        public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                            mSpotifyAppRemote.getPlayerApi().play(popuptrackadapter.getItem(i).uri);
                            // mSpotifyAppRemote.getPlayerApi().queue(trackadapter.getItem(i).uri);
                            toast("Now playing: "+ popuptrackadapter.getItem(i).name, R.drawable.ic_play_circle_outline_black_36dp, Color.BLACK);
                        }
                    });
                }
            });


            datatimeline.setOnTabStripSelectedIndexListener(new NavigationTabStrip.OnTabStripSelectedIndexListener() {
                @Override
                public void onStartTabSelected(String title, int index) {
                    Log.i("CLicked: " , Integer.toString(index));
                    if(index == 0) {
                        options.clear();
                        options.put("time_range", "short_term");
                        TopArtists(options, tagadapter, gridTopArtists, genrePie);
                        TopTracks(options, trackadapter, listTopTracks);
                    } else if(index == 1) {
                        options.clear();
                        options.put("time_range", "medium_term");
                        TopArtists(options, tagadapter, gridTopArtists, genrePie);
                        TopTracks(options, trackadapter, listTopTracks);
                    } else if(index == 2) {
                        options.clear();
                        options.put("time_range", "long_term");
                        TopArtists(options, tagadapter, gridTopArtists, genrePie);
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
    public void TopArtists( Map<String, Object> options, final TopArtistsGridAdapter adapter, final GridView grid, final PieChart piechart) {

        spotify.getTopArtists(options, new Callback<Pager<Artist>>() {
            @Override
            public void success(Pager<Artist> pager, Response response) {

                adapter.clear();
                pietitles.clear();
                pientrys.clear();
                int counter=0;
                for(Artist a : pager.items){
                    counter++;
                    if(counter<10){
                        adapter.add(a);
                    }

                    try{
                        String temp = a.name + " " + a.genres.get(0) + " " + a.genres.get(1);
                        Log.i("Artist + genre:", temp);
                        pietitles.add(a.genres.get(0));
                        pientrys.add(new BarEntry(1.0f, counter));
                        piedataset = new PieDataSet(pientrys, "");
                        piedata = new PieData(pietitles, piedataset);
                        piedataset.setColors(ColorTemplate.PASTEL_COLORS);
                        piechart.setData(piedata);
                        piechart.setDrawHoleEnabled(false);
                        piechart.setEnabled(false);
                        piechart.notifyDataSetChanged();
                        piechart.setEnabled(true);
                        piedata.notifyDataChanged();
                        piedataset.notifyDataSetChanged();


                    } catch(IndexOutOfBoundsException e){
                        e.printStackTrace();
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


    public void MyAlbums(final MyAlbumsGridAdapter adapter, final GridView grid) {
        spotify.getMySavedAlbums(new Callback<Pager<SavedAlbum>>() {
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


    public void topGenres() {

    }

    public void toast(String message, int drawable, int tintcolor) {
        Toasty.custom(getContext(), message, drawable, tintcolor, 700, true, true).show();
    }

}