package com.jukka.jugify;

/*
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.support.v7.graphics.Palette;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.TextView;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;
import com.nostra13.universalimageloader.core.assist.ImageSize;
import com.nostra13.universalimageloader.core.listener.SimpleImageLoadingListener;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

import es.dmoral.toasty.Toasty;
import kaaes.spotify.webapi.android.models.Album;
import kaaes.spotify.webapi.android.models.Artist;
import kaaes.spotify.webapi.android.models.Pager;
import kaaes.spotify.webapi.android.models.Track;
import kaaes.spotify.webapi.android.models.TrackSimple;
import kaaes.spotify.webapi.android.models.Tracks;
import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;

import static android.content.Context.LAYOUT_INFLATER_SERVICE;
import static android.view.ViewGroup.LayoutParams.MATCH_PARENT;
import static com.jukka.jugify.MainActivity.mSpotifyAppRemote;
import static com.jukka.jugify.MainActivity.spotify;

public class Popups {

    public void AlbumPopup(Album album, View view, Boolean throughArtist, final Boolean listentab) {


        Context ctx = getContext();
        int height = MATCH_PARENT;
        int y = 0;
        if(listentab){
            ctx = view.getContext();
            height = 500;
            y = 400;
        }


        LayoutInflater inflater = (LayoutInflater) ctx.getSystemService(LAYOUT_INFLATER_SERVICE);
        View layout = inflater.inflate(R.layout.popup_album,
                (ViewGroup) view.findViewById(R.id.tab_layout_2));


        popup = new PopupWindow(layout, MATCH_PARENT, height, true);
        popup.showAtLocation(layout, Gravity.BOTTOM, 0, y);

        final LinearLayout popupParent = layout.findViewById(R.id.popupParentLayout);
        final String popupAlbumInfo = album.name + "\n by " + album.artists.get(0).name;
        final LinearLayout popupbg = layout.findViewById(R.id.popupBG);
        final ImageView popupImg = layout.findViewById(R.id.imgPopupAlbumImg);
        final TextView popupalbumname = layout.findViewById(R.id.txtPopupAlbumName);
        popupalbumname.setText(popupAlbumInfo);
        final TextView popupinfo = layout.findViewById(R.id.txtPopupInfo2);
        popupinfo.setText("Released " + album.release_date);

        ListView popuplist = layout.findViewById(R.id.listPopupTracks);

        if(!listentab) {
            ImageLoader imgloader = ImageLoader.getInstance();
            DisplayImageOptions defaultOptions = new DisplayImageOptions.Builder()
                    .showStubImage(R.drawable.baseline_album_24).cacheInMemory(true).build();
            ImageLoaderConfiguration config = new ImageLoaderConfiguration.Builder(getContext()).defaultDisplayImageOptions(defaultOptions).build();
            ImageSize targetSize = new ImageSize(200 , 200); // result Bitmap will be fit to this size
            imgloader.loadImage(album.images.get(0).url, targetSize, defaultOptions, new SimpleImageLoadingListener() {
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
        } else {
            popupParent.removeView(popupbg);
        }


        final TracksListAdapter popuptrackadapter = new TracksListAdapter(view.getContext().getApplicationContext(), new ArrayList<TrackSimple>(), false);
        popuptrackadapter.clear();



        if(!throughArtist) {
            // Don't know why this won't work when calling PopupAlbum from PopupArtist
            for(TrackSimple track : album.tracks.items) {
                popuptrackadapter.add(track);
            }
        } else {
            spotify.getAlbumTracks(album.id, new Callback<Pager<Track>>() {
                @Override
                public void success(Pager<Track> trackPager, Response response) {
                    for(TrackSimple track : trackPager.items) {
                        popuptrackadapter.add(track);
                    }

                }

                @Override
                public void failure(RetrofitError error) {
                    Log.d("Album tracks failure", error.toString());
                }
            });
        }


        popuplist.setAdapter(popuptrackadapter);
        popuplist.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                mSpotifyAppRemote.getPlayerApi().play(popuptrackadapter.getItem(i).uri);
                if(!listentab){
                    toast("Now playing: "+ popuptrackadapter.getItem(i).name, R.drawable.ic_play_circle_outline_black_36dp, Color.BLACK);
                }

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
        final TextView popupArtistInfo2 = layout.findViewById(R.id.popupArtistInfo2);
        //final TextView popupGenreText = layout.findViewById(R.id.popupGenreText);
        final TextView popupArtistBio = layout.findViewById(R.id.popupArtistBio);
        final ImageView popupArtistImage = layout.findViewById(R.id.popupArtistImg);
        final LinearLayout popupArtistBG = layout.findViewById(R.id.popupArtistBG);
        final ListView listPopupArtistTopTracks = layout.findViewById(R.id.listPopupTopTracks);
        final GridView gridPopupArtistAlbums = layout.findViewById(R.id.gridPopupAlbums);
        final GridView gridPopupArtistSingles = layout.findViewById(R.id.gridPopupSingles);
        popupArtistName.setText(artist.name);

        String popupArtistGenres = "Genres: ";
        for(int i=0; i< artist.genres.size(); ++i){

            if(i == artist.genres.size() - 1) {
                popupArtistGenres += artist.genres.get(i);
            } else {
                popupArtistGenres += artist.genres.get(i) + ", ";
            }


        }


        final TracksListAdapter popupTopTracksAdapter = new TracksListAdapter(getContext().getApplicationContext(), new ArrayList<TrackSimple>(), false);
        popupTopTracksAdapter.clear();

        // Get Artist Top Tracks
        spotify.getArtistTopTrack(artist.id, "fi", new Callback<Tracks>() {
            @Override
            public void success(Tracks tracks, Response response) {

                for(TrackSimple t : tracks.tracks) {
                    popupTopTracksAdapter.add(t);
                }

                listPopupArtistTopTracks.setAdapter(popupTopTracksAdapter);
                listPopupArtistTopTracks.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                    @Override
                    public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                        mSpotifyAppRemote.getPlayerApi().play(popupTopTracksAdapter.getItem(i).uri);
                        toast("Now playing: "+ popupTopTracksAdapter.getItem(i).name, R.drawable.ic_play_circle_outline_black_36dp, Color.BLACK);
                    }
                });
            }

            @Override
            public void failure(RetrofitError error) {
                Log.d("Artist toptracks fail", error.toString());
            }
        });

        // Get Artist Bio
        GetArtistBio(artist.name, popupArtistBio);

        popupArtistInfo.setText(popupArtistGenres);
        popupArtistInfo2.setText(artist.followers.total + " followers");

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
                    //  popupGenreText.setTextColor(vibrant.getTitleTextColor());
                    popupArtistInfo.setTextColor(vibrant.getBodyTextColor());
                    popupArtistInfo2.setTextColor(vibrant.getBodyTextColor());
                } catch(NullPointerException e) {
                    vibrant = p.getDominantSwatch();
                    popupArtistBG.setBackgroundColor(vibrant.getRgb());
                    popupArtistName.setTextColor(vibrant.getTitleTextColor());
                    //  popupGenreText.setTextColor(vibrant.getTitleTextColor());
                    popupArtistInfo.setTextColor(vibrant.getBodyTextColor());
                    popupArtistInfo2.setTextColor(vibrant.getBodyTextColor());
                }

            }
        });

        final AlbumsGridAdapter albumadapter = new AlbumsGridAdapter(getContext().getApplicationContext(), new ArrayList<Album>());
        albumadapter.clear();
        final AlbumsGridAdapter singleadapter = new AlbumsGridAdapter(getContext().getApplicationContext(), new ArrayList<Album>());
        singleadapter.clear();

        // Get Artist Albums
        spotify.getArtistAlbums(artist.id, new Callback<Pager<Album>>() {
            @Override
            public void success(Pager<Album> albumPager, Response response) {

                for(Album a : albumPager.items) {
                    if(a.album_type.contains("single")) {
                        singleadapter.add(a);
                    } else if(a.album_type.contains("album")) {
                        albumadapter.add(a);
                    }
                }
            }

            @Override
            public void failure(RetrofitError error) {
                Log.d("Artist albums fail", error.toString());
            }
        });

        gridPopupArtistAlbums.setAdapter(albumadapter);
        gridPopupArtistAlbums.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                Album a = albumadapter.getItem(i);
                AlbumPopup(a, view, true, false);
            }
        });
        gridPopupArtistAlbums.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> adapterView, View view, int i, long l) {
                mSpotifyAppRemote.getPlayerApi().play(albumadapter.getItem(i).uri);
                toast("Now shuffling: "+ albumadapter.getItem(i).name, R.drawable.ic_play_circle_outline_black_36dp, Color.BLACK);
                return true;
            }
        });

        gridPopupArtistSingles.setAdapter(singleadapter);
        gridPopupArtistSingles.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                mSpotifyAppRemote.getPlayerApi().play(singleadapter.getItem(i).uri);
                toast("Now playing: "+ singleadapter.getItem(i).name, R.drawable.ic_play_circle_outline_black_36dp, Color.BLACK);
            }
        });
    };

    public void GetArtistBio(final String artistname, final TextView bio) {
        // Fetch Artist Bio from Wikipedia
        String query = "https://en.wikipedia.org/w/api.php?format=json&action=query&prop=extracts&exintro&explaintext&redirects=1&titles=" + artistname;

        RequestQueue queue = Volley.newRequestQueue(getActivity().getApplicationContext());
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest
                (Request.Method.GET, query, null, new com.android.volley.Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        try{
                            JSONObject jobject = response.getJSONObject("query");
                            String txtbio = jobject.getJSONObject("pages").toString();
                            try{
                                txtbio = txtbio.split("extract")[1];
                                txtbio = txtbio.subSequence(2, txtbio.length()-2).toString();
                                txtbio = txtbio.replaceAll("\\\\n", " ");
                                txtbio = txtbio.replaceAll("\\\\", "");

                                // Handle case where artist name refers to something else too
                                if(txtbio.contains("may refer to:")){
                                    GetArtistBio(artistname + " (band)", bio);
                                }
                                bio.setText(txtbio + "\n - Wikipedia \n");
                            } catch(ArrayIndexOutOfBoundsException ae) {
                                bio.setText("Artist Bio not found :( Check back later");
                            }

                        } catch(JSONException je) {
                            je.printStackTrace();
                        }

                    }
                }, new com.android.volley.Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        bio.setText("Couldn't contact Wikipedia :( Check back later");
                    }
                });

        queue.add(jsonObjectRequest);


    }

    public void toast(String message, int drawable, int tintcolor) {
        Toasty.custom(getView().getContext(), message, drawable, tintcolor, 700, true, true).show();
    }
}
*/
