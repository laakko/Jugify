package com.jukka.jugify;

import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.TransitionDrawable;
import android.os.Handler;
import android.provider.MediaStore;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.graphics.Palette;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.ScrollView;
import android.widget.SeekBar;
import android.widget.TextView;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.assist.ImageSize;
import com.ohoussein.playpause.PlayPauseView;
import com.spotify.protocol.client.CallResult;
import com.spotify.protocol.client.Subscription;
import com.spotify.protocol.types.Image;
import com.spotify.protocol.types.PlayerState;
import com.spotify.protocol.types.Track;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;
import java.util.concurrent.TimeUnit;

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
import static com.jukka.jugify.MainActivity.trackArtist;
import static com.jukka.jugify.MainActivity.trackName;
import static com.jukka.jugify.MainActivity.userAuthd;

public class ListenTab extends Fragment {

    TextView txtTitle;
    TextView txtNowPlaying;
    TextView txtNowArtist;
    TextView lyrics;
    PlayPauseView playpause;
    ImageButton skip;
    ImageButton prev;
    ImageButton shuffle;
    Boolean isplaying = false;
    Boolean shuffling = false;
    Float instrumental;
    ImageView imgnowplaying;
    SeekBar seekbar;
    static String imguri;
    Boolean image_gotten = false;
    TextView key, tempo, loudness, timesignature;
    static String keystr;
    TextView songduration, songposition;
    TextView songinformation;
    TrackProgressBar mTrackProgressBar;
    LinearLayout bottomlayout, songcard;
    ProgressBar popularitybar, valencebar, dancebar, energybar, acousticbar;
    ImageView imageNowPlayingBig;
    TextView txtpopularity,txtvalence,txtdance, txtenergy;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.activity_listen_tab, container, false);

        txtNowPlaying = (TextView) view.findViewById(R.id.txtNowPlaying);
        txtNowArtist = (TextView) view.findViewById(R.id.txtNowArtist);
        playpause = (PlayPauseView) view.findViewById(R.id.btnPlay);
        skip = (ImageButton) view.findViewById(R.id.btnNext);
        prev = (ImageButton) view.findViewById(R.id.btnPrev);
        shuffle = (ImageButton) view.findViewById(R.id.btnShuffle);
        //imgnowplaying = (ImageView) view.findViewById(R.id.imgNowPlaying);
        key = (TextView) view.findViewById(R.id.txtKey);
        tempo = (TextView) view.findViewById(R.id.txtBPM);
        loudness = (TextView) view.findViewById(R.id.txtLoudness);
        timesignature = (TextView) view.findViewById(R.id.txtTimeSignature);
        lyrics = (TextView) view.findViewById(R.id.txtLyrics);
        songduration = (TextView) view.findViewById(R.id.txtSongDuration);
        songposition = (TextView) view.findViewById(R.id.txtSongPosition);
        seekbar = (SeekBar) view.findViewById(R.id.seekBar);
        bottomlayout = (LinearLayout) view.findViewById(R.id.bottomlayout);
        songinformation = (TextView) view.findViewById(R.id.txtSongInformation);
        popularitybar = (ProgressBar) view.findViewById(R.id.popularityBar);
        valencebar = (ProgressBar) view.findViewById(R.id.valenceBar);
        dancebar = (ProgressBar) view.findViewById(R.id.danceBar);
        energybar = (ProgressBar) view.findViewById(R.id.energyBar);
       // acousticbar = (ProgressBar) view.findViewById(R.id.acousticBar);
        txtTitle = (TextView) view.findViewById(R.id.txtTitle);
        imageNowPlayingBig = (ImageView) view.findViewById(R.id.imageArtistBig);
        songcard = (LinearLayout) view.findViewById(R.id.songcard);
        txtpopularity = (TextView) view.findViewById(R.id.txtPopularity);
        txtvalence = (TextView) view.findViewById(R.id.txtValence);
        txtenergy = (TextView) view.findViewById(R.id.txtEnergy);
        txtdance = (TextView) view.findViewById(R.id.txtDanceability);


        final ScrollView scrollview = (ScrollView) view.findViewById(R.id.scrollview);

        if(displayheight == 1794) {
            scrollview.getLayoutParams().height = 1298;
            scrollview.requestLayout();
        }

        mTrackProgressBar = new TrackProgressBar(seekbar);




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
                        shuffle.setColorFilter(Color.DKGRAY);
                        shuffling = true;
                    } else {
                        shuffling = false;
                        shuffle.setColorFilter(Color.parseColor("#427DD1"));
                    }

                    if(playerState.playbackSpeed > 0) {
                        mTrackProgressBar.unpause();
                    } else {
                        mTrackProgressBar.pause();
                    }


                    if (track != null) {

                        // Get basic information
                        trackName = track.name;
                        trackArtist = track.artist.name;
                        txtNowPlaying.setText(trackName);
                        txtNowArtist.setText(trackArtist);
                        //txtNowArtist.setTextColor(Color.GRAY);
                        txtTitle.setText(track.name + "\n by " + track.artist.name);
                        String duration = String.format("%d:%d",
                                TimeUnit.MILLISECONDS.toMinutes(track.duration),
                                TimeUnit.MILLISECONDS.toSeconds(track.duration) -
                                        TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(track.duration))
                        );
                        songduration.setText(duration);


                        // Get progress bar
                        mTrackProgressBar.setDuration(track.duration);
                        mTrackProgressBar.update(playerState.playbackPosition);
                        seekbar.setEnabled(true);

                        // Get track information
                        getNowPlayingInformation(track.album.uri.substring(14));

                        // Get audio features
                        getAudioFeatures(track.uri.substring(14));
                        String requestURL = "https://api.lyrics.ovh/v1/" + track.artist.name + "/" + track.name;
                        lyricsApi(requestURL);


                        // Get Album Image
                        imguri = track.imageUri.raw;
                        mSpotifyAppRemote.getImagesApi().getImage(track.imageUri)
                                .setResultCallback(new CallResult.ResultCallback<Bitmap>() {
                                    @Override
                                    public void onResult(Bitmap bitmap) {
                                        imageNowPlayingBig.setImageBitmap(bitmap);

                                        Palette p = Palette.from(bitmap).maximumColorCount(8).generate();
                                        Palette.Swatch vibrant = p.getDominantSwatch();
                                    /*


                                        energybar.getProgressDrawable().setColorFilter(vibrant.getRgb(), PorterDuff.Mode.MULTIPLY);
                                        valencebar.getProgressDrawable().setColorFilter(vibrant.getRgb(), PorterDuff.Mode.MULTIPLY);
                                        dancebar.getProgressDrawable().setColorFilter(vibrant.getRgb(), PorterDuff.Mode.MULTIPLY);
                                        popularitybar.getProgressDrawable().setColorFilter(vibrant.getRgb(), PorterDuff.Mode.MULTIPLY);
                                        txtTitle.setTextColor(vibrant.getTitleTextColor());
                                        songinformation.setTextColor(vibrant.getBodyTextColor());
                                        txtdance.setTextColor(vibrant.getRgb());
                                        txtvalence.setTextColor(vibrant.getRgb());
                                        txtenergy.setTextColor(vibrant.getRgb());
                                        txtpopularity.setTextColor(vibrant.getRgb());
                                        GradientDrawable gd = new GradientDrawable(
                                                GradientDrawable.Orientation.TOP_BOTTOM,
                                                new int[] {vibrant.getRgb(), vibrant.getTitleTextColor()});
                                        gd.setCornerRadius(0f);
                                        songcard.setBackground(gd);
                                        */


                                    }
                                });


                    } else {
                        seekbar.setEnabled(false);
                    }
                }
            });


            // Play/Pause, Skip, Prev and shuffle buttons
            playpause.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if(isplaying){
                        playpause.toggle(true);
                        mSpotifyAppRemote.getPlayerApi().pause();
                    } else {
                        mSpotifyAppRemote.getPlayerApi().resume();
                        playpause.toggle(true);
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

    public void getNowPlayingInformation(final String uri) {

        spotify.getAlbum(uri, new Callback<Album>() {
            @Override
            public void success(Album a, Response response) {

                songinformation.setText("From " + '"' + a.name + '"' + "\nreleased " + a.release_date);
                popularitybar.setProgress(a.popularity, true);



            }
            @Override
            public void failure(RetrofitError error){
                Log.d("Album failure", error.toString());
            }
        });
    }


    public void getQueue() {

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

                energybar.setProgress(Math.round(aft.energy * 100));
                dancebar.setProgress(Math.round(aft.danceability * 100));
                valencebar.setProgress(Math.round(aft.valence * 100));
               // acousticbar.setProgress(Math.round(aft.acousticness * 100));
                instrumental = aft.instrumentalness;

            }

            @Override
            public void failure(RetrofitError error){
                Log.d("Audio features failure", error.toString());
            }
        });
    }

    public void lyricsApi(String url) {

        // API: https://lyricsovh.docs.apiary.io
        if(instrumental > 0.8) {
            lyrics.setText("This track is instrumental!");
        } else {
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
                            lyrics.setText("Lyrics not found for the song :( \n Check back later");

                        }
                    });

            queue.add(jsonObjectRequest);
        }

    }



    private void createAlbumColorPalette(Bitmap bitmap) {

        Palette.from(bitmap).generate(new Palette.PaletteAsyncListener() {
            @Override
            public void onGenerated(Palette palette) {








            }
        });
    }


    private class TrackProgressBar {

        private static final int LOOP_DURATION = 500;
        private final SeekBar mSeekBar;
        private final Handler mHandler;


        private final SeekBar.OnSeekBarChangeListener mSeekBarChangeListener = new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                String position = String.format("%d:%d",
                        TimeUnit.MILLISECONDS.toMinutes(seekBar.getProgress()),
                        TimeUnit.MILLISECONDS.toSeconds(seekBar.getProgress()) -
                                TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(seekBar.getProgress()))
                );
                songposition.setText(position);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                mSpotifyAppRemote.getPlayerApi().seekTo(seekBar.getProgress());

            }
        };

        private final Runnable mSeekRunnable = new Runnable() {
            @Override
            public void run() {
                int progress = mSeekBar.getProgress();
                mSeekBar.setProgress(progress + LOOP_DURATION);
                String position = String.format("%d:%d",
                        TimeUnit.MILLISECONDS.toMinutes(progress),
                        TimeUnit.MILLISECONDS.toSeconds(progress) -
                                TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(progress))
                );
                songposition.setText(position);

                mHandler.postDelayed(mSeekRunnable, LOOP_DURATION);
            }
        };

        private TrackProgressBar(SeekBar seekBar) {
            mSeekBar = seekBar;
            mSeekBar.setOnSeekBarChangeListener(mSeekBarChangeListener);
            mHandler = new Handler();
        }

        private void setDuration(long duration) {
            mSeekBar.setMax((int) duration);
        }

        private void update(long progress) {
            mSeekBar.setProgress((int) progress);
        }

        private void pause() {
            mHandler.removeCallbacks(mSeekRunnable);
        }

        private void unpause() {
            mHandler.removeCallbacks(mSeekRunnable);
            mHandler.postDelayed(mSeekRunnable, LOOP_DURATION);
        }
    }


}