package com.jukka.jugify;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;
import com.nostra13.universalimageloader.core.assist.ImageSize;

import java.util.ArrayList;
import java.util.List;

import kaaes.spotify.webapi.android.models.Artist;

public class TopArtistsGridAdapter extends ArrayAdapter<Artist> {

    private Context mContext;
    private ArrayList<Artist> artistslist = new ArrayList<Artist>();

    public TopArtistsGridAdapter(@NonNull Context context, ArrayList<Artist> list) {
        super(context, 0, list);
        mContext = context;
        artistslist = list;
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        View listItem = convertView;
        if (listItem == null)
            listItem = LayoutInflater.from(mContext).inflate(R.layout.layout_top_artists, parent, false);

        Artist currentArtist = artistslist.get(position);
        ImageView currentArtistImage = listItem.findViewById(R.id.imgArtist);
        TextView currentArtistName = listItem.findViewById(R.id.txtArtistName);
        currentArtistName.setText(currentArtist.name);
        String artistImageUrl = currentArtist.images.get(0).url;
        ImageLoader imgloader = ImageLoader.getInstance();
       // imgloader.displayImage(artistImageUrl, currentArtistImage);
        imgloader.displayImage(artistImageUrl, currentArtistImage, new ImageSize(100,100));

        return listItem;

    }


}
