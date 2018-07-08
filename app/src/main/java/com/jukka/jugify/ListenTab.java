package com.jukka.jugify;

import android.graphics.Bitmap;
import android.provider.MediaStore;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.assist.ImageSize;
import com.spotify.protocol.client.CallResult;
import com.spotify.protocol.client.Subscription;
import com.spotify.protocol.types.PlayerState;
import com.spotify.protocol.types.Track;

import org.json.JSONException;
import org.json.JSONObject;

import kaaes.spotify.webapi.android.SpotifyApi;
import kaaes.spotify.webapi.android.SpotifyService;
import kaaes.spotify.webapi.android.models.Album;
import kaaes.spotify.webapi.android.models.AudioFeaturesTrack;
import kaaes.spotify.webapi.android.models.Pager;
import kaaes.spotify.webapi.android.models.PlaylistSimple;
import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;

import static com.jukka.jugify.MainActivity.displayheight;
import static com.jukka.jugify.MainActivity.mSpotifyAppRemote;
import static com.jukka.jugify.MainActivity.spotify;
import static com.jukka.jugify.MainActivity.trackName;
import static com.jukka.jugify.MainActivity.userAuthd;

public class ListenTab extends Fragment {

    TextView txtNowPlaying;
    TextView lyrics;
    Button playpause;
    Button skip;
    Button prev;
    Button shuffle;
    Boolean isplaying = false;
    Boolean shuffling = false;
    ImageView imgnowplaying;
    static String imguri;
    Boolean image_gotten = false;
    TextView key, tempo, loudness, timesignature;
    static String keystr;
    TextView songduration;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.activity_listen_tab, container, false);

        txtNowPlaying = (TextView) view.findViewById(R.id.txtNowPlaying);
        playpause = (Button) view.findViewById(R.id.btnPlay);
        skip = (Button) view.findViewById(R.id.btnNext);
        prev = (Button) view.findViewById(R.id.btnPrev);
        shuffle = (Button) view.findViewById(R.id.btnShuffle);
        imgnowplaying = (ImageView) view.findViewById(R.id.imgNowPlaying);
        key = (TextView) view.findViewById(R.id.txtKey);
        tempo = (TextView) view.findViewById(R.id.txtBPM);
        loudness = (TextView) view.findViewById(R.id.txtLoudness);
        timesignature = (TextView) view.findViewById(R.id.txtTimeSignature);
        lyrics = (TextView) view.findViewById(R.id.txtLyrics);
        songduration = (TextView) view.findViewById(R.id.txtSongDuration);


        final ScrollView scrollview = (ScrollView) view.findViewById(R.id.scrollview);

        if(displayheight == 1794) {
            scrollview.getLayoutParams().height = 924;
            scrollview.requestLayout();
        }



        if(userAuthd){

            txtNowPlaying.setText(trackName);
            mSpotifyAppRemote.getPlayerApi()
                    .subscribeToPlayerState().setEventCallback(new Subscription.EventCallback<PlayerState>() {
                public void onEvent(PlayerState playerState) {

                    final Track track = playerState.track;

                    if(playerState.isPaused){
                        isplaying = false;
                    } else {
                        isplaying = true;
                    }
                    if(playerState.playbackOptions.isShuffling){
                        shuffling = true;
                    } else {
                        shuffling = false;
                    }

                    if (track != null) {
                        trackName = track.name + "\n" + track.artist.name;
                        txtNowPlaying.setText(trackName);
                        songduration.setText(Long.toString(track.duration));
                        imguri = track.imageUri.raw;
                        image_gotten = true;

                        getAudioFeatures(track.uri.substring(14));
                       // lyrics.setText("loading..");
                        String requestURL = "https://api.lyrics.ovh/v1/" + track.artist.name + "/" + track.name;
                        lyricsApi(requestURL);


                        mSpotifyAppRemote.getImagesApi().getImage(track.imageUri)
                                .setResultCallback(new CallResult.ResultCallback<Bitmap>() {
                                    @Override
                                    public void onResult(Bitmap bitmap) {

                                        imgnowplaying.setImageBitmap(bitmap);
                                    }
                                });

                    }
                }
            });


            // Play/Pause, Skip, Prev and shuffle buttons
            playpause.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if(isplaying){
                        mSpotifyAppRemote.getPlayerApi().pause();
                    } else {
                        mSpotifyAppRemote.getPlayerApi().resume();
                    }
                }
            });

            skip.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    lyrics.setText("loading..");
                    mSpotifyAppRemote.getPlayerApi().skipNext();
                }
            });

            prev.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    lyrics.setText("loading..");
                    mSpotifyAppRemote.getPlayerApi().skipPrevious();
                }
            });

            shuffle.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if(shuffling) {
                        mSpotifyAppRemote.getPlayerApi().setShuffle(false);
                    } else {
                        mSpotifyAppRemote.getPlayerApi().setShuffle(true);
                    }

                }
            });

        }
        return view;
    }

    public void getNowPlayingInformation(String uri) {
        spotify.getAlbum(uri, new Callback<Album>() {
            @Override
            public void success(Album a, Response response) {
                String releasedate = a.release_date;
                String popularity = Integer.toString(a.popularity);
                String albumname = a.name;

            }
            @Override
            public void failure(RetrofitError error){
                Log.d("Album failure", error.toString());
            }
        });
    }


    public void getAudioFeatures(String uri) {
        spotify.getTrackAudioFeatures(uri, new Callback<AudioFeaturesTrack>() {

            @Override
            public void success(AudioFeaturesTrack aft, Response response){

                if(aft.key == 0){
                    keystr = "C";
                } else if(aft.key == 1) {
                    keystr = "C#";
                } else if(aft.key == 2) {
                    keystr = "D";
                } else if(aft.key == 3) {
                    keystr = "D#";
                } else if(aft.key == 4) {
                    keystr = "E";
                } else if(aft.key == 5) {
                    keystr = "F";
                } else if(aft.key == 6) {
                    keystr = "F#";
                } else if(aft.key == 7) {
                    keystr = "G";
                } else if(aft.key == 8) {
                    keystr = "G#";
                } else if(aft.key == 9) {
                    keystr = "A";
                } else if(aft.key == 10) {
                    keystr = "A#";
                } else if(aft.key == 11) {
                    keystr = "B";
                }


                if(aft.mode == 1) {
                    keystr += " Minor";
                } else {
                    keystr += " Major";
                }

                key.setText("Key: "+keystr);
                tempo.setText("Tempo: "+Integer.toString(Math.round(aft.tempo)) + " BPM");
                loudness.setText("Loudness: "+Float.toString(aft.loudness) + "dB");
                timesignature.setText("Time signature: " + Integer.toString(aft.time_signature));

            }

            @Override
            public void failure(RetrofitError error){
                Log.d("Audio features failure", error.toString());
            }
        });
    }

    public void lyricsApi(String url) {

        // API: https://lyricsovh.docs.apiary.io
        RequestQueue queue = Volley.newRequestQueue(getActivity().getApplicationContext());
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest
                (Request.Method.GET, url, null, new com.android.volley.Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        try{
                            String txtlyrics = response.getString("lyrics");
                            lyrics.setText("\n" + txtlyrics + "\n");
                        } catch(JSONException je) {
                            je.printStackTrace();
                        }

                    }
                }, new com.android.volley.Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        lyrics.setText("Lyrics query failed :(");

                    }
                });

        queue.add(jsonObjectRequest);




    }


}