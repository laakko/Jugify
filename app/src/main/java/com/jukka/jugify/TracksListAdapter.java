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

import kaaes.spotify.webapi.android.models.Track;
import kaaes.spotify.webapi.android.models.TrackSimple;

public class TracksListAdapter extends ArrayAdapter<TrackSimple> {
    private Context mContext;
    private ArrayList<TrackSimple> trackslist = new ArrayList<TrackSimple>();

    public TracksListAdapter(@NonNull Context context, ArrayList<TrackSimple> list) {
        super(context, 0, list);
        mContext = context;
        trackslist = list;
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        View listItem = convertView;
        if (listItem == null)
            listItem = LayoutInflater.from(mContext).inflate(R.layout.layout_tracks, parent, false);

        TrackSimple curTrack = trackslist.get(position);
        TextView curTrackName = listItem.findViewById(R.id.txtListTrack);
        curTrackName.setText(curTrack.name);
        curTrackName.setTextColor(Color.LTGRAY);



        return listItem;

    }
}
