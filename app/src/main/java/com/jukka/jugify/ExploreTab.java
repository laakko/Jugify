package com.jukka.jugify;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.AsyncTask;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.graphics.Palette;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.GridView;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.SearchView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.androidbuts.multispinnerfilter.KeyPairBoolData;
import com.androidbuts.multispinnerfilter.MultiSpinnerListener;
import com.androidbuts.multispinnerfilter.MultiSpinnerSearch;
import com.androidbuts.multispinnerfilter.SpinnerListener;
import com.gigamole.navigationtabstrip.NavigationTabStrip;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;
import com.nostra13.universalimageloader.core.assist.ImageSize;
import com.nostra13.universalimageloader.core.listener.SimpleImageLoadingListener;

import org.w3c.dom.Text;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import es.dmoral.toasty.Toasty;
import kaaes.spotify.webapi.android.models.Album;
import kaaes.spotify.webapi.android.models.Artist;
import kaaes.spotify.webapi.android.models.AudioFeaturesTrack;
import kaaes.spotify.webapi.android.models.Pager;
import kaaes.spotify.webapi.android.models.PlaylistSimple;
import kaaes.spotify.webapi.android.models.PlaylistTrack;
import kaaes.spotify.webapi.android.models.Recommendations;
import kaaes.spotify.webapi.android.models.SavedAlbum;
import kaaes.spotify.webapi.android.models.Track;
import kaaes.spotify.webapi.android.models.TrackSimple;
import kaaes.spotify.webapi.android.models.UserPrivate;
import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;

import static android.content.ContentValues.TAG;
import static android.content.Context.LAYOUT_INFLATER_SERVICE;
import static android.view.ViewGroup.LayoutParams.MATCH_PARENT;
import static com.jukka.jugify.MainActivity.atoken;
import static com.jukka.jugify.MainActivity.mSpotifyAppRemote;
import static com.jukka.jugify.MainActivity.spotify;
import static com.jukka.jugify.MainActivity.userAuthd;





public class ExploreTab extends Fragment {

    public static boolean myplaylists_gotten = false;
    public static boolean myalbums_gotten = false;
    public String userid;
    public static MyPlaylistsGridAdapter padapter;
    public static MyAlbumsGridAdapter albadapter;
    public static ArtistsGridAdapter pinnedadapter;

    public ArrayList<PlaylistSimple> myplaylistslist = new ArrayList<>();
    public ArrayList<SavedAlbum> myalbumslist = new ArrayList<>();
    public ArrayList<Artist> pinnedartistslist = new ArrayList<>();

    private PopupWindow popup;
    private PopupWindow pinnedartistsPopup;
    Common cm = new Common();

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.activity_explore_tab, container, false);
        final GridView gridPlaylists = (GridView) view.findViewById(R.id.gridPlaylists);
        final GridView gridAlbums = (GridView) view.findViewById(R.id.gridAlbums);
        final GridView gridPinned = (GridView) view.findViewById(R.id.gridPinnedArtists);

        if(userAuthd) {

            spotify.getMe(new Callback<UserPrivate>() {
                @Override
                public void success(UserPrivate user, Response response) {
                    userid = user.id;
                }
                @Override
                public void failure(RetrofitError error) {
                    Log.d("User failure", error.toString());
                }
            });

            if(!myplaylists_gotten){
                padapter = new MyPlaylistsGridAdapter(getContext().getApplicationContext(), myplaylistslist);
                MyPlaylists(padapter, gridPlaylists);
            } else {
                gridPlaylists.setAdapter(padapter);
                cm.expandGridView(gridPlaylists, 2);
            }

            gridPlaylists.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
                @Override
                public boolean onItemLongClick(AdapterView<?> adapterView, View view, int i, long l) {
                    // Play the clicked playlist
                    mSpotifyAppRemote.getPlayerApi().play(padapter.getItem(i).uri);
                    cm.toast("Now playing: "+padapter.getItem(i).name, R.drawable.ic_playlist_play_black_36dp, Color.BLACK, getContext());
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
                    final PlaylistSimple playlist = padapter.getItem(i);

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
                            .showStubImage(R.drawable.baseline_album_24).cacheOnDisk(true).build();
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

                            Log.d("PLTOTAL", Integer.toString(pager.total));
                            Log.d("PLITEMS", Integer.toString(pager.items.size()));
                            for(PlaylistTrack p : pager.items){
                                popuptrackadapter.add(p.track);
                            }

                            if(pager.total > 100) {
                                final Map<String, Object> nextPage = new HashMap<>();
                                nextPage.put("offset", "100");
                                spotify.getPlaylistTracks(userid, playlist.id, nextPage, new Callback<Pager<PlaylistTrack>>() {
                                    @Override
                                    public void success(Pager<PlaylistTrack> playlistTrackPager, Response response) {
                                        Log.d("PLITEMS", Integer.toString(playlistTrackPager.items.size()));
                                        for(PlaylistTrack p : playlistTrackPager.items){
                                            popuptrackadapter.add(p.track);
                                        }
                                        myplaylists_gotten = true;
                                        popuplist.setAdapter(popuptrackadapter);
                                    }

                                    @Override
                                    public void failure(RetrofitError error) {

                                    }
                                });


                            } else {
                                myplaylists_gotten = true;
                                popuplist.setAdapter(popuptrackadapter);
                            }


                            popuplist.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                                @Override
                                public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                                    mSpotifyAppRemote.getPlayerApi().play(popuptrackadapter.getItem(i).uri);
                                    cm.toast("Now playing: "+ popuptrackadapter.getItem(i).name, R.drawable.ic_play_circle_outline_black_36dp, Color.BLACK, getContext());
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

            final Map<String, Object> optionsAlbum = new HashMap<>();
            optionsAlbum.put("limit", "50");

            if(!myalbums_gotten){
                albadapter = new MyAlbumsGridAdapter(getContext().getApplicationContext(), myalbumslist);
                MyAlbums(optionsAlbum, albadapter, gridAlbums);
            } else {
                gridAlbums.setAdapter(albadapter);
                cm.expandGridView(gridAlbums, 2);
            }

            gridAlbums.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
                @Override
                public boolean onItemLongClick(AdapterView<?> adapterView, View view, int i, long l) {
                    // Play the clicked album
                    mSpotifyAppRemote.getPlayerApi().play(albadapter.getItem(i).album.uri);
                    cm.toast("Now playing: "+albadapter.getItem(i).album.name, R.drawable.baseline_album_24, Color.BLACK, getContext());

                    return true;
                }
            });

            gridAlbums.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {

                    Album popupalbum = albadapter.getItem(i).album;
                    cm.AlbumPopup(popupalbum, getContext(), view, false, false, false, 0);

                }
            });


            pinnedadapter = new ArtistsGridAdapter(getContext().getApplicationContext(), pinnedartistslist);

            String artistString = FileService.readFile(getContext(), "artistlist.txt");
            final String[] artistListTemp = artistString.split("-");

            try{
                for(String artist : artistListTemp) {
                    spotify.getArtist(artist.split("\\.")[0].trim(), new Callback<Artist>() {
                        @Override
                        public void success(Artist artist, Response response) {
                           pinnedadapter.add(artist);
                           cm.expandGridView(gridPinned, 2);

                        }

                        @Override
                        public void failure(RetrofitError error) {
                            Log.d("Pinned artist fail", error.toString());
                        }
                    });

                }

                gridPinned.setAdapter(pinnedadapter);
            } catch(IndexOutOfBoundsException aio) {

            }

            gridPinned.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                    cm.ArtistPopup(pinnedadapter.getItem(i), view, false, getContext());
                }
            });


        }
        return view;
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
                cm.expandGridView(grid, 2);
            }

            @Override
            public void failure(RetrofitError error) {
                Log.d("My playlists failure", error.toString());
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

                if(savedAlbumPager.total > 50) {

                    final Map<String, Object> nextPage = new HashMap<>();
                    nextPage.put("limit", "50");
                    nextPage.put("offset", "50");

                    spotify.getMySavedAlbums(nextPage, new Callback<Pager<SavedAlbum>>() {
                        @Override
                        public void success(Pager<SavedAlbum> savedAlbumPager, Response response) {
                            for(SavedAlbum sa : savedAlbumPager.items){
                                adapter.add(sa);
                            }

                            myalbums_gotten = true;
                            grid.setAdapter(adapter);
                            cm.expandGridView(grid, 2);
                        }

                        @Override
                        public void failure(RetrofitError error) {

                        }
                    });


                } else {
                    myalbums_gotten = true;
                    grid.setAdapter(adapter);
                    cm.expandGridView(grid, 2);
                }



            }



            @Override
            public void failure(RetrofitError error) {
                Log.d("My albums failure", error.toString());
            }
        });
    }


}


