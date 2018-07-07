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
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.assist.ImageSize;
import com.spotify.protocol.client.CallResult;
import com.spotify.protocol.client.Subscription;
import com.spotify.protocol.types.PlayerState;
import com.spotify.protocol.types.Track;

import kaaes.spotify.webapi.android.SpotifyApi;
import kaaes.spotify.webapi.android.SpotifyService;
import kaaes.spotify.webapi.android.models.AudioFeaturesTrack;
import kaaes.spotify.webapi.android.models.Pager;
import kaaes.spotify.webapi.android.models.PlaylistSimple;
import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;

import static com.jukka.jugify.MainActivity.mSpotifyAppRemote;
import static com.jukka.jugify.MainActivity.spotify;
import static com.jukka.jugify.MainActivity.trackName;
import static com.jukka.jugify.MainActivity.userAuthd;

public class ListenTab extends Fragment {

    TextView txtNowPlaying;
    Button playpause;
    Button skip;
    Button prev;
    Boolean isplaying = false;
    ImageView imgnowplaying;
    static String imguri;
    Boolean image_gotten = false;
    TextView key, tempo, loudness, timesignature;
    static  String keystr;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.activity_listen_tab, container, false);

        txtNowPlaying = (TextView) view.findViewById(R.id.txtNowPlaying);
        playpause = (Button) view.findViewById(R.id.btnPlay);
        skip = (Button) view.findViewById(R.id.btnNext);
        prev = (Button) view.findViewById(R.id.btnPrev);
        imgnowplaying = (ImageView) view.findViewById(R.id.imgNowPlaying);
        key = (TextView) view.findViewById(R.id.txtKey);
        tempo = (TextView) view.findViewById(R.id.txtBPM);
        loudness = (TextView) view.findViewById(R.id.txtLoudness);
        timesignature = (TextView) view.findViewById(R.id.txtTimeSignature);

        if(userAuthd){

            txtNowPlaying.setText(trackName);
            mSpotifyAppRemote.getPlayerApi()
                    .subscribeToPlayerState().setEventCallback(new Subscription.EventCallback<PlayerState>() {
                public void onEvent(PlayerState playerState) {
                    final Track track = playerState.track;
                    if(playerState.isPaused){
                        isplaying = false;
                    } else{
                        isplaying = true;
                    }
                    if (track != null) {
                        trackName = track.name + "\n" + track.artist.name + "\n" + track.album.name;
                        txtNowPlaying.setText(trackName);

                        imguri = track.imageUri.raw;
                        image_gotten = true;

                        Log.i("asd", track.uri);
                        getAudioFeatures(track.uri.substring(14));


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




            // Play/Pause, Skip and Prev buttons
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
                    mSpotifyAppRemote.getPlayerApi().skipNext();
                }
            });

            prev.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    mSpotifyAppRemote.getPlayerApi().skipPrevious();
                }
            });
        }
        return view;
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
                Log.d("Album failure", error.toString());
            }
        });
    }


}