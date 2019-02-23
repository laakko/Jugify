package com.jukka.jugify;

import android.content.Context;
import android.graphics.Color;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.assist.ImageSize;

import java.util.ArrayList;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

import es.dmoral.toasty.Toasty;
import kaaes.spotify.webapi.android.models.Track;
import kaaes.spotify.webapi.android.models.TrackSimple;


import static com.jukka.jugify.MainActivity.mSpotifyAppRemote;

public class TracksListAdapter extends ArrayAdapter<TrackSimple> {
    private Context mContext;
    private ArrayList<TrackSimple> trackslist = new ArrayList<TrackSimple>();
    private Boolean trackInfo;

    public TracksListAdapter(@NonNull Context context, ArrayList<TrackSimple> list, Boolean withInfo) {
        super(context, 0, list);
        mContext = context;
        trackslist = list;
        trackInfo = withInfo;
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        View listItem = convertView;
        if (listItem == null)
            listItem = LayoutInflater.from(mContext).inflate(R.layout.layout_tracks, parent, false);

        final TrackSimple curTrack = trackslist.get(position);
        TextView curTrackName = listItem.findViewById(R.id.txtListTrack);
        TextView curTrackDuration = listItem.findViewById(R.id.txtListDuration);

        curTrackName.setTextColor(Color.LTGRAY);
        curTrackDuration.setTextColor(Color.DKGRAY);

        String duration = String.format(Locale.US, "%d:%02d",
                TimeUnit.MILLISECONDS.toMinutes(curTrack.duration_ms),
                TimeUnit.MILLISECONDS.toSeconds(curTrack.duration_ms) -
                        TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(curTrack.duration_ms))
        );

        if(trackInfo){
            curTrackName.setText(Html.fromHtml(curTrack.name + "<br/>" +  "<i>" + curTrack.artists.get(0).name + "</i>"));
            curTrackDuration.setText(duration);
        } else {
            curTrackName.setText(curTrack.name);
            curTrackDuration.setText(duration);
        }


        ImageButton queueBtn = listItem.findViewById(R.id.imgAddQueue);
        queueBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mSpotifyAppRemote.getPlayerApi().queue(curTrack.uri);
                toast("'" + curTrack.name + "' added to queue.", R.drawable.ic_queue_black_36dp, Color.BLACK);
            }
        });




        return listItem;

    }


    public void toast(String message, int drawable, int tintcolor) {
        Toasty.custom(getContext(), message, drawable, tintcolor, 700, true, true).show();
    }
}
