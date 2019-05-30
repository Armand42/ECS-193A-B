package ecs193.speechPrepPal;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import ecs193.speechPrepPal.R;

import java.util.List;

public class PlaybackListAdapter extends ArrayAdapter<PlaybackListItem> {
    public PlaybackListAdapter(Context context, List<PlaybackListItem> objects) {
        super(context, 0, objects);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.past_runs_item, parent, false);
        }

        ((TextView) convertView.findViewById(R.id.runDate))
                .setText(String.valueOf(getItem(position).runDate));

        ((TextView) convertView.findViewById(R.id.runNum))
                .setText(getItem(position).runNum);

        ((TextView) convertView.findViewById(R.id.percentAccuracy))
                .setText(String.valueOf(getItem(position).percentAccuracy));

        return convertView;
    }
}
