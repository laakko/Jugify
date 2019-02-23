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

import java.util.ArrayList;

import kaaes.spotify.webapi.android.models.Album;


public class AlbumsGridAdapter extends ArrayAdapter<Album> {

    private Context mContext;
    private ArrayList<Album> albumslist = new ArrayList<Album>();

    public AlbumsGridAdapter(@NonNull Context context, ArrayList<Album> list) {
        super(context, 0, list);
        mContext = context;
        albumslist = list;
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        View listItem = convertView;
        if (listItem == null)
            listItem = LayoutInflater.from(mContext).inflate(R.layout.layout_my_albums, parent, false);


        Album album = albumslist.get(position);
        ImageView albumImage = listItem.findViewById(R.id.imgArtist);
        TextView albumName = listItem.findViewById(R.id.txtArtistName);

      //  String aname = album.name;
      //  albumName.setText(aname);

        albumName.setText(Html.fromHtml(album.name + "<br/>" +  "<i>" + album.release_date.substring(0,4) + "</i>"));

        String albumImageUrl = album.images.get(0).url;
        ImageLoader imgloader = ImageLoader.getInstance();
        //imgloader.displayImage(albumImageUrl, albumImage, new ImageSize(100,100));
        DisplayImageOptions defaultOptions = new DisplayImageOptions.Builder()
                .showStubImage(R.drawable.baseline_album_24).cacheInMemory(true).build();
        ImageLoaderConfiguration config = new ImageLoaderConfiguration.Builder(getContext()).defaultDisplayImageOptions(defaultOptions).build();
        imgloader.displayImage(albumImageUrl, albumImage, defaultOptions);


        return listItem;

    }
}