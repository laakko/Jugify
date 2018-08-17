package com.jukka.jugify;

import android.app.ProgressDialog;
import android.graphics.Point;
import android.graphics.Typeface;
import android.graphics.drawable.TransitionDrawable;
import android.support.v4.app.Fragment;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.content.res.ResourcesCompat;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Display;
import android.view.View;

import com.gigamole.navigationtabstrip.NavigationTabStrip;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;
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
import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;

import com.spotify.android.appremote.api.ConnectionParams;
import com.spotify.android.appremote.api.Connector;
import com.spotify.android.appremote.api.SpotifyAppRemote;

import com.spotify.protocol.client.Subscription;
import com.spotify.protocol.types.PlayerState;
import com.spotify.protocol.types.Track;



public class MainActivity extends AppCompatActivity {

    private static final String CLIENT_ID = "55e7a5c941d54aed915d3f9e0140350d";
    private static final String REDIRECT_URI = "com.jukka.jugify://callback";
    private static final int REQUEST_CODE = 1995;
    public static int displayheight;
    //public static int displaywidth;
    public static SpotifyAppRemote mSpotifyAppRemote;
    public static String atoken;
    static SpotifyService spotify;
    public static boolean userAuthd = false;
    public static boolean updateTabs = true;
    public static String trackName;
    public static String trackArtist;
    public static ViewPager viewPager;
    public static ProgressDialog dialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);



        ImageLoaderConfiguration config = new ImageLoaderConfiguration.Builder(this).build();
        ImageLoader.getInstance().init(config);

        AuthenticationRequest.Builder builder = new AuthenticationRequest.Builder(CLIENT_ID, AuthenticationResponse.Type.TOKEN, REDIRECT_URI);
        builder.setScopes(new String[]{"user-read-private", "streaming", "user-top-read", "user-read-currently-playing", "user-library-read", "user-modify-playback-state", "app-remote-control", "playlist-read-private", "playlist-read-collaborative"});
        AuthenticationRequest request = builder.build();

        AuthenticationClient.openLoginActivity(this, REQUEST_CODE, request);
        Display display = getWindowManager().getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);
        displayheight = size.y;


        /* Uncomment to enable toolbar
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        */

        /*
        TabLayout tabLayout = findViewById(R.id.tab_layout);
        tabLayout.addTab(tabLayout.newTab().setText("User"));
        tabLayout.addTab(tabLayout.newTab().setText("Listen"));
        tabLayout.addTab(tabLayout.newTab().setText("Explore"));
        tabLayout.setTabGravity(TabLayout.GRAVITY_FILL);
        */

        NavigationTabStrip tabLayout2 = findViewById(R.id.tab_layout_2);
        tabLayout2.setTitles("User", "Listen", "Explore");
        tabLayout2.setAnimationDuration(150);


        dialog = new ProgressDialog(this);
        dialog.setMessage("Waiting for Spotify authentication..");
        dialog.setCancelable(false);
        dialog.setInverseBackgroundForced(false);
        dialog.show();

        viewPager = findViewById(R.id.pager);

        final PagerAdapter adapter = new PageAdapter
                (getSupportFragmentManager(), 3);
        viewPager.setAdapter(adapter);
        viewPager.setCurrentItem(1);
        /*
        viewPager.addOnPageChangeListener(new TabLayout.TabLayoutOnPageChangeListener(tabLayout));
        tabLayout.setOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                viewPager.setCurrentItem(tab.getPosition());
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {
            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {
            }
        });
        */

       // tabLayout2.setTabIndex(1, true);
        tabLayout2.setViewPager(viewPager);
        /*
        TransitionDrawable transition = (TransitionDrawable) MainActivity.viewPager.getBackground();
        transition.startTransition(1000);
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
                spotify = api.getService();

                //dialog.hide();





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
                                Log.i("MyActivity", throwable.getMessage(), throwable);

                                // Something went wrong when attempting to connect! Handle errors here
                            }
                        });

            }
        }

    }


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

    /*
    private void connected() {


        // Subscribe to PlayerState
        mSpotifyAppRemote.getPlayerApi()
                .subscribeToPlayerState().setEventCallback(new Subscription.EventCallback<PlayerState>() {

            public void onEvent(PlayerState playerState) {
                final Track track = playerState.track;
                if (track != null) {
                    trackName = track.name + " by " + track.artist.name;
                }
            }
        });

    }
    */
}
