package com.jukka.jugify;


import android.content.Context;
import android.graphics.Color;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.assist.ImageSize;

import java.util.ArrayList;

import kaaes.spotify.webapi.android.models.Artist;
import kaaes.spotify.webapi.android.models.Track;

public class TopTracksListAdapter extends ArrayAdapter<Track> {

    private Context mContext;
    private ArrayList<Track> trackslist = new ArrayList<Track>();

    public TopTracksListAdapter(@NonNull Context context, ArrayList<Track> list) {
        super(context, 0, list);
        mContext = context;
        trackslist = list;
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        View listItem = convertView;
        if (listItem == null)
            listItem = LayoutInflater.from(mContext).inflate(R.layout.layout_top_tracks, parent, false);

        Track currentTrack = trackslist.get(position);
        TextView currentTrackName = listItem.findViewById(R.id.txtTopTrack);
        TextView currentTrackInfo = listItem.findViewById(R.id.txtTopTrackInfo);

        currentTrackName.setText(currentTrack.name);
        currentTrackName.setTextColor(Color.WHITE);

        currentTrackInfo.setText(currentTrack.artists.get(0).name + "\n" + currentTrack.album.name + ", track " + currentTrack.track_number);
        currentTrackInfo.setTextColor(Color.LTGRAY);

        ImageView trackimg = listItem.findViewById(R.id.imgTopTrack);
        String trackimageurl = currentTrack.album.images.get(0).url;
        ImageLoader imgloader = ImageLoader.getInstance();
        imgloader.displayImage(trackimageurl, trackimg, new ImageSize(70,70));


        return listItem;

    }
}
