package com.jukka.jugify;

import android.content.Context;
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


public class SearchListAdapter extends ArrayAdapter<String> {

    private Context mContext;
    private ArrayList<String> nameslist = new ArrayList<String>();
    private ArrayList<String> urlslist = new ArrayList<String>();

    public SearchListAdapter(@NonNull Context context, ArrayList<String> list, ArrayList<String> list2) {
        super(context, 0, list);
        mContext = context;
        nameslist = list;
        urlslist = list2;
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        View listItem = convertView;
        if (listItem == null)
            listItem = LayoutInflater.from(mContext).inflate(R.layout.layout_search, parent, false);

        ImageView imgSearch = (ImageView) listItem.findViewById(R.id.imgSearch);
        TextView txtSearch = (TextView) listItem.findViewById(R.id.txtSearch);
        txtSearch.setText(nameslist.get(position));
        try{
            setImage(imgSearch, urlslist.get(position));
        } catch (IndexOutOfBoundsException ioobe) {

        }

        return listItem;

    }

    public void setImage(ImageView img, String imgurl) {
        ImageLoader imgloader = ImageLoader.getInstance();
        imgloader.displayImage(imgurl, img, new ImageSize(70,70));
    }
}
