package com.jukka.jugify;

import android.animation.ArgbEvaluator;
import android.animation.ValueAnimator;
import android.app.ProgressDialog;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.Point;
import android.graphics.Typeface;
import android.graphics.drawable.TransitionDrawable;
import android.media.AudioManager;
import android.support.annotation.FontRes;
import android.support.v4.app.Fragment;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.content.res.ResourcesCompat;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.graphics.Palette;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Display;
import android.view.View;
import android.view.animation.Animation;
import android.widget.ImageView;

import com.gigamole.navigationtabstrip.NavigationTabStrip;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;
import com.spotify.protocol.client.CallResult;
import com.spotify.sdk.android.authentication.AuthenticationClient;
import com.spotify.sdk.android.authentication.AuthenticationRequest;
import com.spotify.sdk.android.authentication.AuthenticationResponse;
import com.spotify.sdk.android.player.Config;
import com.spotify.sdk.android.player.ConnectionStateCallback;
import com.spotify.sdk.android.player.Error;
import com.spotify.sdk.android.player.Player;
import com.spotify.sdk.android.player.PlayerEvent;
import com.spotify.sdk.android.player.Spotify;
import com.spotify.sdk.android.player.SpotifyPlayer;

import kaaes.spotify.webapi.android.SpotifyApi;
import kaaes.spotify.webapi.android.SpotifyService;
import kaaes.spotify.webapi.android.models.Album;
import kaaes.spotify.webapi.android.models.Artist;
import kaaes.spotify.webapi.android.models.Pager;
import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;

import com.spotify.android.appremote.api.ConnectionParams;
import com.spotify.android.appremote.api.Connector;
import com.spotify.android.appremote.api.SpotifyAppRemote;

import com.spotify.protocol.client.Subscription;
import com.spotify.protocol.types.PlayerState;
import com.spotify.protocol.types.Track;

import java.util.HashMap;
import java.util.Map;


public class MainActivity extends AppCompatActivity {

    private static final String CLIENT_ID = "55e7a5c941d54aed915d3f9e0140350d";
    private static final String REDIRECT_URI = "com.jukka.jugify://callback";
    private static final int REQUEST_CODE = 1995;
    public static int displayheight;
    //public static int displaywidth;
    public static SpotifyAppRemote mSpotifyAppRemote;
    public static String atoken;
    static SpotifyService spotify;
    public static int audioSessionId;

    public static String trackName;
    public static String trackArtist;
    public static ViewPager viewPager;
    public static ProgressDialog dialog;
    public static boolean userAuthd = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        ImageLoaderConfiguration config = new ImageLoaderConfiguration.Builder(this).build();
        ImageLoader.getInstance().init(config);

        AuthenticationRequest.Builder builder = new AuthenticationRequest.Builder(CLIENT_ID, AuthenticationResponse.Type.TOKEN, REDIRECT_URI);
        builder.setScopes(new String[]{"user-read-private", "streaming", "user-top-read", "user-library-modify", "user-read-currently-playing", "user-library-read", "user-modify-playback-state", "user-read-playback-state", "app-remote-control", "playlist-read-private", "playlist-modify-public", "playlist-modify-private", "playlist-read-collaborative"});
        AuthenticationRequest request = builder.build();

        AuthenticationClient.openLoginActivity(this, REQUEST_CODE, request);
        Display display = getWindowManager().getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);
        displayheight = size.y;


        NavigationTabStrip tabLayout2 = findViewById(R.id.tab_layout_2);
        tabLayout2.setTitles("Stats", "Listen", "Library", "Search");
        tabLayout2.setAnimationDuration(150);
        tabLayout2.setTypeface(Typeface.DEFAULT_BOLD);



        dialog = new ProgressDialog(this);
        dialog.setMessage("Waiting for Spotify authentication..");
        dialog.setCancelable(false);
        dialog.setInverseBackgroundForced(true);
        dialog.show();

        viewPager = findViewById(R.id.pager);

        final PagerAdapter adapter = new PageAdapter
                (getSupportFragmentManager(), 4);
        viewPager.setAdapter(adapter);
        viewPager.setCurrentItem(1);

       // tabLayout2.setTabIndex(1, true);
        tabLayout2.setViewPager(viewPager);


        /*  ANIMATION
        int ColorFrom = Color.BLACK;
       // int ColorFrom = Color.parseColor("#090335");

        int ColorTo = Color.parseColor("#1d1651");
       // int ColorTo = Color.parseColor("#4C40AD");
        ValueAnimator anim = ValueAnimator.ofObject(new ArgbEvaluator(),
                ColorFrom, ColorTo);

        anim.setDuration(4000);
        anim.setRepeatCount(Animation.INFINITE);



            anim.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {

                @Override
                public void onAnimationUpdate(ValueAnimator animation) {
                    MainActivity.viewPager.setBackgroundColor((Integer) animation.getAnimatedValue());


                }
            });

            anim.setRepeatMode(ValueAnimator.REVERSE);
            anim.start();
           */

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent intent) {
        super.onActivityResult(requestCode, resultCode, intent);

        // Spotify auth
        if (requestCode == REQUEST_CODE) {
            AuthenticationResponse response = AuthenticationClient.getResponse(resultCode, intent);
            if (response.getType() == AuthenticationResponse.Type.TOKEN) {

                // Authenticate Spotify Web API
                SpotifyApi api = new SpotifyApi();
                api.setAccessToken(response.getAccessToken());
                atoken = response.getAccessToken();
                spotify = api.getService();


                // Authenticate Spotify App Remote
                ConnectionParams connectionParams = new ConnectionParams.Builder(CLIENT_ID)
                        .setRedirectUri(REDIRECT_URI)
                        .setPreferredImageSize(1000).setPreferredThumbnailImageSize(100)
                        .showAuthView(false)
                        .build();

                SpotifyAppRemote.CONNECTOR.connect(this, connectionParams,
                        new Connector.ConnectionListener() {

                            public void onConnected(SpotifyAppRemote spotifyAppRemote) {
                                mSpotifyAppRemote = spotifyAppRemote;
                                Log.i("MainActivity", "Connected! Yay!");

                                // Spotify auth finished -> close dialog -> update tabs
                                userAuthd = true;

                                dialog.hide();

                                viewPager.getAdapter().notifyDataSetChanged();
                            }

                            public void onFailure(Throwable throwable) {
                                Log.e("MyActivity", throwable.getMessage(), throwable);
                            }
                        });

            }
        }

    }


    /*
    @Override
    protected void onStop() {
        super.onStop();
        // Disconnect the app remote
        SpotifyAppRemote.CONNECTOR.disconnect(mSpotifyAppRemote);
    }

    @Override
    protected void onRestart(){
        super.onRestart();

        // Reconnect the app remote
        dialog = new ProgressDialog(this);
        dialog.setMessage("Reconnecting to Spotify..");
        dialog.setCancelable(false);
        dialog.setInverseBackgroundForced(false);
        dialog.show();

        ConnectionParams connectionParams = new ConnectionParams.Builder(CLIENT_ID)
                .setRedirectUri(REDIRECT_URI)
                .setPreferredImageSize(1000).setPreferredThumbnailImageSize(100)
                .showAuthView(false)
                .build();
        SpotifyAppRemote.CONNECTOR.connect(this, connectionParams,
                new Connector.ConnectionListener() {

                    public void onConnected(SpotifyAppRemote spotifyAppRemote) {
                        mSpotifyAppRemote = spotifyAppRemote;
                        viewPager.getAdapter().notifyDataSetChanged();
                        dialog.hide();
                    }

                    public void onFailure(Throwable throwable) {
                        Log.i("MyActivity", throwable.getMessage(), throwable);
                        // Something went wrong when attempting to connect! Handle errors here
                    }
                });
        Log.i("Is remote connected?", Boolean.toString(mSpotifyAppRemote.isConnected()));
    }
    */







}
