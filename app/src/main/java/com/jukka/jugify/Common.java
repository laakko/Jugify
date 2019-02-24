package com.jukka.jugify;


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
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListAdapter;
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
import java.util.HashMap;
import java.util.Map;

import es.dmoral.toasty.Toasty;
import kaaes.spotify.webapi.android.models.Album;
import kaaes.spotify.webapi.android.models.Artist;
import kaaes.spotify.webapi.android.models.Artists;
import kaaes.spotify.webapi.android.models.Pager;
import kaaes.spotify.webapi.android.models.Track;
import kaaes.spotify.webapi.android.models.TrackSimple;
import kaaes.spotify.webapi.android.models.Tracks;
import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;

import static android.content.Context.LAYOUT_INFLATER_SERVICE;
import static android.view.ViewGroup.LayoutParams.MATCH_PARENT;
import static com.jukka.jugify.MainActivity.displayheight;
import static com.jukka.jugify.MainActivity.mSpotifyAppRemote;
import static com.jukka.jugify.MainActivity.spotify;

public class Common {

    private PopupWindow popup;

    void expandGridView(GridView gridView, int columns) {
        ListAdapter listAdapter = gridView.getAdapter();
        if (listAdapter == null) {
            return;
        }

        int totalHeight = 0;
        int items = listAdapter.getCount();
        int rows = 0;

        View listItem = listAdapter.getView(0, null, gridView);
        listItem.measure(0, 0);
        totalHeight = listItem.getMeasuredHeight();

        float x = 1;
        if( items > columns ){
            x = items/columns;
            rows = (int) (x);
            totalHeight *= rows;
        }

        ViewGroup.LayoutParams params = gridView.getLayoutParams();
        params.height = totalHeight;
        gridView.setLayoutParams(params);

    }




    public void ArtistPopup(final Artist artist, View view, final boolean listentab, final Context ctx){

        LayoutInflater inflater = (LayoutInflater) ctx.getSystemService(LAYOUT_INFLATER_SERVICE);
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
        final GridView gridPopupSimilarArtists = layout.findViewById(R.id.gridSimilarArtists);
        final ImageButton btnPinArtist = layout.findViewById(R.id.btnPinArtist);
        popupArtistName.setText(artist.name);
        final String artistString = artist.id + "." + artist.name;

        String popupArtistGenres = "Genres: ";
        for(int i=0; i< artist.genres.size(); ++i){

            if(i == artist.genres.size() - 1) {
                popupArtistGenres += artist.genres.get(i);
            } else {
                popupArtistGenres += artist.genres.get(i) + ", ";
            }


        }


        final TracksListAdapter popupTopTracksAdapter = new TracksListAdapter(ctx, new ArrayList<TrackSimple>(), false);
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
                        toast("Now playing: "+ popupTopTracksAdapter.getItem(i).name, R.drawable.ic_play_circle_outline_black_36dp, Color.BLACK, ctx);
                    }
                });
            }

            @Override
            public void failure(RetrofitError error) {
                Log.d("Artist toptracks fail", error.toString());
            }
        });

        // Get Artist Bio
        GetArtistBio(artist.name, popupArtistBio, ctx, false);

        popupArtistInfo.setText(popupArtistGenres);
        popupArtistInfo2.setText(artist.followers.total + " followers");

        ImageLoader imgloader = ImageLoader.getInstance();
        DisplayImageOptions defaultOptions = new DisplayImageOptions.Builder()
                .showStubImage(R.drawable.baseline_album_24).build();
        ImageLoaderConfiguration config = new ImageLoaderConfiguration.Builder(ctx).defaultDisplayImageOptions(defaultOptions).build();
        ImageSize targetSize = new ImageSize(200 , 200); // result Bitmap will be fit to this size
        try{
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
        } catch (IndexOutOfBoundsException ioe) {

        }

        // Get similar artists
        final ArtistsGridAdapter similaradapter = new ArtistsGridAdapter(ctx, new ArrayList<Artist>());
        similaradapter.clear();


        spotify.getRelatedArtists(artist.id, new Callback<Artists>() {
            @Override
            public void success(Artists artists, Response response) {
                int tempHeight = 0;
                for(Artist a : artists.artists) {
                    Log.i("Similar: ", a.name);
                    similaradapter.add(a);
                    tempHeight += 164;
                }

                /*
                ViewGroup.LayoutParams params = gridPopupSimilarArtists.getLayoutParams();
                params.height = tempHeight / 2;
                gridPopupSimilarArtists.setLayoutParams(params);
                */

                gridPopupSimilarArtists.setAdapter(similaradapter);
                expandGridView(gridPopupSimilarArtists, 2);


            }

            @Override
            public void failure(RetrofitError error) {

            }
        });



        gridPopupSimilarArtists.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                ArtistPopup(similaradapter.getItem(i), view, false, ctx);
            }
        });


        // Get Artist Albums
        final AlbumsGridAdapter albumadapter = new AlbumsGridAdapter(ctx, new ArrayList<Album>());
        albumadapter.clear();
        final AlbumsGridAdapter singleadapter = new AlbumsGridAdapter(ctx, new ArrayList<Album>());
        singleadapter.clear();
        final Map<String, Object> options = new HashMap<>();
        options.put("limit", "50");
        options.put("include_groups", "album,single");


        spotify.getArtistAlbums(artist.id, options, new Callback<Pager<Album>>() {
            @Override
            public void success(Pager<Album> albumPager, Response response) {

                for(Album a : albumPager.items) {
                    if(a.album_type.contains("single")) {
                        singleadapter.add(a);

                    } else if(a.album_type.contains("album")) {
                        albumadapter.add(a);

                    }
                }

                if(albumPager.total > 50) {
                    final Map<String, Object> nextPage = new HashMap<>();
                    nextPage.put("limit", "50");
                    nextPage.put("include_groups", "album,single");
                    nextPage.put("offset", "50");

                    spotify.getArtistAlbums(artist.id, nextPage, new Callback<Pager<Album>>() {
                        @Override
                        public void success(Pager<Album> albumPager, Response response) {

                            for(Album a : albumPager.items) {
                                if(a.album_type.contains("single")) {
                                    singleadapter.add(a);
                                } else if(a.album_type.contains("album")) {
                                    albumadapter.add(a);
                                }
                            }

                            gridPopupArtistAlbums.setAdapter(albumadapter);
                            try{
                                expandGridView(gridPopupArtistAlbums, 2);
                            } catch (IndexOutOfBoundsException o) {

                            }

                            gridPopupArtistSingles.setAdapter(singleadapter);
                            try{
                                expandGridView(gridPopupArtistSingles, 2);
                            } catch (IndexOutOfBoundsException o) {

                            }


                        }

                        @Override
                        public void failure(RetrofitError error) {

                        }
                    });


                } else {
                    gridPopupArtistAlbums.setAdapter(albumadapter);
                    try{
                        expandGridView(gridPopupArtistAlbums, 2);
                    } catch (IndexOutOfBoundsException o) {

                    }

                    gridPopupArtistSingles.setAdapter(singleadapter);
                    try{
                        expandGridView(gridPopupArtistSingles, 2);
                    } catch (IndexOutOfBoundsException o) {

                    }
                }



            }

            @Override
            public void failure(RetrofitError error) {
                Log.d("Artist albums fail", error.toString());
            }
        });


        gridPopupArtistAlbums.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                Album a = albumadapter.getItem(i);
                AlbumPopup(a, ctx, view, true, false, false, 0);
            }
        });
        gridPopupArtistAlbums.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> adapterView, View view, int i, long l) {
                mSpotifyAppRemote.getPlayerApi().play(albumadapter.getItem(i).uri);
                toast("Now shuffling: "+ albumadapter.getItem(i).name, R.drawable.ic_play_circle_outline_black_36dp, Color.BLACK, ctx);
                return true;
            }
        });


        gridPopupArtistSingles.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                mSpotifyAppRemote.getPlayerApi().play(singleadapter.getItem(i).uri);
                toast("Now playing: "+ singleadapter.getItem(i).name, R.drawable.ic_play_circle_outline_black_36dp, Color.BLACK, ctx);
            }
        });

        if(FileService.fileContainsString(ctx, "artistlist.txt", artistString)) {
            btnPinArtist.setColorFilter(Color.parseColor("#427DD1"));
        } else {
            btnPinArtist.setColorFilter(Color.parseColor( "#4C40AD"));
        }

        btnPinArtist.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {


                if(FileService.fileContainsString(ctx, "artistlist.txt", artistString)) {
                    FileService.removeItem(ctx, "artistlist.txt", artistString);
                    toast("Artist unpinned", R.drawable.ic_person_pin_black_36dp, Color.BLACK, ctx);
                    btnPinArtist.setColorFilter(Color.parseColor( "#4C40AD"));

                } else {
                    FileService.writeFile(ctx, "artistlist.txt", artistString + "-");
                    toast("Artist pinned", R.drawable.ic_person_pin_black_36dp, Color.BLACK, ctx);
                    btnPinArtist.setColorFilter(Color.parseColor("#427DD1"));

                }

            }
        });


    };



    public void GetArtistBio(final String artistname, final TextView bio, final Context ctx, final boolean isClicked) {
        // Fetch Artist Bio from Wikipedia

        String query = "https://en.wikipedia.org/w/api.php?format=json&action=query&prop=extracts&exintro&explaintext&redirects=1&titles=" + artistname;
        if(isClicked) {
            query =   "https://en.wikipedia.org/w/api.php?format=json&action=query&prop=extracts&exlimit&explaintext&redirects=1&titles=" + artistname;
        }


        RequestQueue queue = Volley.newRequestQueue(ctx);
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
                                txtbio = txtbio.replaceAll("\\\\n", "\n");
                                txtbio = txtbio.replaceAll("\\\\", "");
                                txtbio = txtbio.replaceAll("=", "\n");


                                // Handle case where artist name refers to something else too
                                if(txtbio.contains("may refer to:")){

                                    if(isClicked) {
                                        GetArtistBio( artistname + " (band)", bio, ctx, true);
                                    } else {
                                        GetArtistBio( artistname + " (band)", bio, ctx, false);
                                    }

                                }
                                bio.setText(txtbio + "\n - Wikipedia \n");

                                bio.setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View view) {
                                        // View more or view less
                                        if(!isClicked) {
                                            GetArtistBio( artistname, bio, ctx, true);
                                        } else {
                                            GetArtistBio( artistname, bio, ctx, false);
                                            GetArtistBio( artistname, bio, ctx, false);
                                        }
                                    }
                                });

                            } catch(ArrayIndexOutOfBoundsException ae) {
                                bio.setText("Artist Bio not found. \n Check back later.");
                            }

                        } catch(JSONException je) {
                            je.printStackTrace();
                        }

                    }
                }, new com.android.volley.Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        bio.setText("Couldn't contact Wikipedia. \n Check back later.");
                    }
                });

        queue.add(jsonObjectRequest);



    }

    public void AlbumPopup(final Album album, Context ctx, View view, Boolean throughArtist, final Boolean listentab, final Boolean searchtab, int y) {

        final Context ctx2 = ctx;
        int height = MATCH_PARENT;
        int width = MATCH_PARENT;

        if(listentab){
            if(displayheight == 2560) {
                // 1440p
                height = 1000;
                width = 1000;
            } else{
                // 1080p
                height = 600;
                width = 600;
            }
        }

        LayoutInflater inflater = (LayoutInflater) ctx.getSystemService(LAYOUT_INFLATER_SERVICE);
        View layout = inflater.inflate(R.layout.popup_album,
                (ViewGroup) view.findViewById(R.id.tab_layout_2));


        popup = new PopupWindow(layout, width, height, true);
        popup.showAtLocation(layout, Gravity.BOTTOM, 0, y);

        final LinearLayout popupParent = layout.findViewById(R.id.popupParentLayout);
        final String popupAlbumInfo = album.name + "\n by " + album.artists.get(0).name;
        final LinearLayout popupbg = layout.findViewById(R.id.popupBG);
        final ImageView popupImg = layout.findViewById(R.id.imgPopupAlbumImg);
        final TextView popupalbumname = layout.findViewById(R.id.txtPopupAlbumName);
        popupalbumname.setText(popupAlbumInfo);
        final TextView popupinfo = layout.findViewById(R.id.txtPopupInfo2);
        final TextView popupinfo2 = layout.findViewById(R.id.txtPopupInfo3);
        popupinfo.setText(album.release_date);
        if(!throughArtist){
            popupinfo2.setText(album.tracks.total + " tracks");
        }
        ListView popuplist = layout.findViewById(R.id.listPopupTracks);

        if(!listentab) {


            popupalbumname.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(final View view) {
                    spotify.getArtist(album.artists.get(0).id, new Callback<Artist>() {
                        @Override
                        public void success(Artist artist, Response response) {
                            ArtistPopup(artist, view, false, ctx2);
                        }
                        @Override
                        public void failure(RetrofitError error) {

                        }
                    });

                }
            });


            ImageLoader imgloader = ImageLoader.getInstance();
            DisplayImageOptions defaultOptions = new DisplayImageOptions.Builder()
                    .showStubImage(R.drawable.baseline_album_24).cacheInMemory(true).build();
            ImageLoaderConfiguration config = new ImageLoaderConfiguration.Builder(ctx).defaultDisplayImageOptions(defaultOptions).build();
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
                        popupinfo2.setTextColor(vibrant.getBodyTextColor());


                    } catch(NullPointerException e) {
                        vibrant = p.getDominantSwatch();
                        popupbg.setBackgroundColor(vibrant.getRgb());
                        popupalbumname.setTextColor(vibrant.getTitleTextColor());
                        popupinfo.setTextColor(vibrant.getBodyTextColor());
                        popupinfo2.setTextColor(vibrant.getBodyTextColor());

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
                    popupinfo2.setText(trackPager.items.size() + " tracks");

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
                if(!listentab && !searchtab){
                    toast("Now playing: "+ popuptrackadapter.getItem(i).name, R.drawable.ic_play_circle_outline_black_36dp, Color.BLACK, ctx2);
                }
            }
        });


    }


    public void toast(String message, int drawable, int tintcolor, Context ctx) {
        Toasty.custom(ctx, message, drawable, tintcolor, 700, true, true).show();
    }


}
