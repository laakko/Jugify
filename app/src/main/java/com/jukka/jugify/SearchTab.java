package com.jukka.jugify;

import android.app.SearchManager;
import android.content.Context;
import android.graphics.Color;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.SearchView;
import android.widget.TextView;

import com.gigamole.navigationtabstrip.NavigationTabStrip;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import es.dmoral.toasty.Toasty;
import kaaes.spotify.webapi.android.models.Album;
import kaaes.spotify.webapi.android.models.AlbumSimple;
import kaaes.spotify.webapi.android.models.AlbumsPager;
import kaaes.spotify.webapi.android.models.Artist;
import kaaes.spotify.webapi.android.models.ArtistsPager;
import kaaes.spotify.webapi.android.models.Playlist;
import kaaes.spotify.webapi.android.models.PlaylistSimple;
import kaaes.spotify.webapi.android.models.PlaylistsPager;
import kaaes.spotify.webapi.android.models.Track;
import kaaes.spotify.webapi.android.models.TracksPager;
import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;

import static android.content.Context.SEARCH_SERVICE;
import static com.jukka.jugify.MainActivity.mSpotifyAppRemote;
import static com.jukka.jugify.MainActivity.spotify;

public class SearchTab extends Fragment {

    String chosen_tab;
    ArrayList<String> names;
    ArrayList<String> ids;
    ArrayList<Artist> artists;
    ArrayList<AlbumSimple> albums;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.activity_search_tab, container, false);

        final UserTab usertab = new UserTab();
        final SearchView search = (SearchView) view.findViewById(R.id.searchView);
        ListView listResults = (ListView) view.findViewById(R.id.listResults);
        final NavigationTabStrip datatimeline = view.findViewById(R.id.searchFilter);
        datatimeline.setTitles("Artists", "Albums", "Tracks");
        datatimeline.setAnimationDuration(50);
        datatimeline.setTabIndex(0);
        chosen_tab = "artists";

        final SearchManager searchManager = (SearchManager) view.getContext().getSystemService(SEARCH_SERVICE);
        search.setIconifiedByDefault(false);
        search.setQueryHint("Search for " + chosen_tab);
        search.setFocusable(true);
        search.requestFocusFromTouch();



        names = new ArrayList<String>();
        ids = new ArrayList<String>();
        artists = new ArrayList<Artist>();
        albums = new ArrayList<AlbumSimple>();
        final ArrayAdapter<String> adapter = new ArrayAdapter<String>(getContext(), android.R.layout.simple_list_item_1, names);

        datatimeline.setOnTabStripSelectedIndexListener(new NavigationTabStrip.OnTabStripSelectedIndexListener() {
            @Override
            public void onStartTabSelected(String title, int index) {
                if(index == 0) {
                    chosen_tab = "artists";
                    search.setQueryHint("Search for " + chosen_tab);
                } else if(index == 1) {
                    chosen_tab = "albums";
                    search.setQueryHint("Search for " + chosen_tab);
                } else if(index == 2) {
                    chosen_tab = "tracks";
                    search.setQueryHint("Search for " + chosen_tab);
                }
            }

            @Override
            public void onEndTabSelected(String title, int index) {
            }
        });


        listResults.setAdapter(adapter);

        search.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String s) {

                names.clear();
                ids.clear();
                artists.clear();
                albums.clear();
                adapter.notifyDataSetChanged();
                spotifySearch(s, chosen_tab, adapter);


                return false;
            }

            @Override
            public boolean onQueryTextChange(String s) {

                names.clear();
                ids.clear();
                artists.clear();
                albums.clear();
                adapter.notifyDataSetChanged();
                spotifySearch(s, chosen_tab, adapter);


                return false;
            }
        });

        listResults.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, final View view, int i, long l) {
                if(chosen_tab == "tracks") {
                    mSpotifyAppRemote.getPlayerApi().play(ids.get(i));
                    toast("Now playing: "+ adapter.getItem(i), R.drawable.ic_play_circle_outline_black_36dp, Color.BLACK, getContext());
                }
                else if(chosen_tab == "albums") {

                    spotify.getAlbum(albums.get(i).id, new Callback<Album>() {
                        @Override
                        public void success(Album album, Response response) {
                            usertab.AlbumPopup(album, view, true, false, true, 0);
                        }

                        @Override
                        public void failure(RetrofitError error) {
                        }
                    });
                } else if(chosen_tab == "artists") {
                    usertab.ArtistPopup(artists.get(i), view, true);
                }
            }
        });


        return view;
    }


    public void spotifySearch(String query, String type, final ArrayAdapter adapter) {


        final Map<String, Object> options = new HashMap<>();
        options.put("limit", 10);

        if(type == "artists") {

            spotify.searchArtists(query, options, new Callback<ArtistsPager>() {
                @Override
                public void success(ArtistsPager artistsPager, Response response) {
                    names.clear();
                    artists.clear();
                    for(Artist a : artistsPager.artists.items) {
                        names.add(a.name);
                        artists.add(a);
                    }
                    adapter.notifyDataSetChanged();
                }

                @Override
                public void failure(RetrofitError error) {

                }
            });

        } else if(type == "albums") {


            spotify.searchAlbums(query, options, new Callback<AlbumsPager>() {
                @Override
                public void success(AlbumsPager albumsPager, Response response) {
                    names.clear();
                    albums.clear();
                    for(AlbumSimple a : albumsPager.albums.items) {
                        names.add(a.name);
                        albums.add(a);
                    }
                    adapter.notifyDataSetChanged();
                }

                @Override
                public void failure(RetrofitError error) {

                }
            });

        } else if(type == "tracks") {

            spotify.searchTracks(query, options, new Callback<TracksPager>() {
                @Override
                public void success(TracksPager tracksPager, Response response) {
                    names.clear();
                    ids.clear();
                    for(Track t : tracksPager.tracks.items) {
                        names.add(t.name + " - " + t.artists.get(0).name);
                        ids.add(t.uri);
                    }
                    adapter.notifyDataSetChanged();

                }

                @Override
                public void failure(RetrofitError error) {

                }
            });

        }

    }

    public void toast(String message, int drawable, int tintcolor, Context ctx) {
        Toasty.custom(ctx, message, drawable, tintcolor, 700, true, true).show();
    }

}
