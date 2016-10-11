package com.beesham.popularmovies;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

import static android.R.attr.resource;

/**
 * Created by Beesham on 10/11/2016.
 */

public class TrailersAdapter extends ArrayAdapter<DetailsFragment.Trailer> {

    @BindView(R.id.trailer_name_textview) TextView trailerName;

    public TrailersAdapter(Context context, List list) {
        super(context, 0, list);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        DetailsFragment.Trailer trailer = getItem(position);

        if(convertView == null){
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.list_item_trailer, parent, false);
        }
        ButterKnife.bind(this, convertView);

        trailerName.setText(trailer.getTrailerName());

        return convertView;
    }
}
