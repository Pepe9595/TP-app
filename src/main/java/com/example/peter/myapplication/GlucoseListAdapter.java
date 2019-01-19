package com.example.peter.myapplication;


import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.ArrayList;

public class GlucoseListAdapter extends ArrayAdapter<GlucoseListItem> {

    public GlucoseListAdapter(Context context, ArrayList<GlucoseListItem> users) {
        super(context, 0, users);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        // Get the data item for this position
        GlucoseListItem user = getItem(position);
        // Check if an existing view is being reused, otherwise inflate the view
        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.glucose_listview, parent, false);
        }
        // Lookup view for data population
        TextView glucose = (TextView) convertView.findViewById(R.id.glucose_listvalue);
        TextView date = (TextView) convertView.findViewById(R.id.date_listvalue);
        // Populate the data into the template view using the data object
        glucose.setText(user.glucose);
        date.setText(user.datum);
        // Return the completed view to render on screen
        return convertView;
    }
}