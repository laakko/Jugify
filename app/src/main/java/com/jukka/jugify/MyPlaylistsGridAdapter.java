package com.jukka.jugify;


import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;
import com.nostra13.universalimageloader.core.assist.ImageSize;

import java.util.ArrayList;

import kaaes.spotify.webapi.android.models.Artist;
import kaaes.spotify.webapi.android.models.PlaylistSimple;

public class MyPlaylistsGridAdapter extends ArrayAdapter<PlaylistSimple> {

    private Context mContext;
    private ArrayList<PlaylistSimple> playlistslist = new ArrayList<PlaylistSimple>();

    public MyPlaylistsGridAdapter(@NonNull Context context, ArrayList<PlaylistSimple> list) {
        super(context, 0, list);
        mContext = context;
        playlistslist = list;
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        View listItem = convertView;
        if (listItem == null)
            listItem = LayoutInflater.from(mContext).inflate(R.layout.layout_my_playlists, parent, false);


        PlaylistSimple plist = playlistslist.get(position);
        ImageView currentArtistImage = listItem.findViewById(R.id.imgArtist);
        TextView currentPname = listItem.findViewById(R.id.txtPlaylistName);

        String pinfo = Integer.toString(plist.tracks.total) + " tracks";
      //  currentPname.setText(pinfo);
        currentPname.setText(Html.fromHtml(plist.name + "<br/>" +  "<i>" + pinfo + "</i>"));
        String artistImageUrl = plist.images.get(0).url;
        ImageLoader imgloader = ImageLoader.getInstance();
        DisplayImageOptions defaultOptions = new DisplayImageOptions.Builder()
                .showStubImage(R.drawable.baseline_album_24).cacheInMemory(true).build();
        ImageLoaderConfiguration config = new ImageLoaderConfiguration.Builder(getContext()).defaultDisplayImageOptions(defaultOptions).build();
        //imgloader.displayImage(artistImageUrl, currentArtistImage, new ImageSize(100,100));
        imgloader.displayImage(artistImageUrl, currentArtistImage, defaultOptions);


        return listItem;

    }


}