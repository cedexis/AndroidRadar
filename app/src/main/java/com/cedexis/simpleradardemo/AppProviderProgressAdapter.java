package com.cedexis.simpleradardemo;

import android.app.Activity;
import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TableRow;
import android.widget.TextView;

public class AppProviderProgressAdapter extends ArrayAdapter<AppProvider> {

    private static final String TAG = "ProgressAdapter";

    private Context _context;
    private int _resource;

    public AppProviderProgressAdapter(Context context, int resource) {
        super(context, resource);
        this._context = context;
        this._resource = resource;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View row = convertView;
        MyTag tag;
        if (row == null) {
            LayoutInflater inflater = ((Activity)_context).getLayoutInflater();
            row = inflater.inflate(_resource, parent, false);
            tag = new MyTag();
            tag.providerName = (TextView)row.findViewById(R.id.provider_name);
            tag.connectTime = (TextView)row.findViewById(R.id.connect_time);
            tag.responseTime = (TextView)row.findViewById(R.id.response_time);
            tag.throughput = (TextView)row.findViewById(R.id.throughput);
            row.setTag(tag);
        } else {
            tag = (MyTag)row.getTag();
        }

        AppProvider item = this.getItem(position);
        Log.v(TAG, item.toString());
        tag.providerName.setText(item.get_providerName());
        if (item.has_connectTimeText()) {
            Log.v(TAG, item.get_connectTimeText());
            tag.connectTime.setText(String.format("Connect: %s", item.get_connectTimeText()));
            tag.connectTime.setLayoutParams(new TableRow.LayoutParams());
        } else {
            Log.v(TAG, "Setting connect height to zero");
            tag.connectTime.setHeight(0);
        }
        if (item.has_responseTimeText()) {
            Log.v(TAG, item.get_responseTimeText());
            tag.responseTime.setText(String.format("Response Time: %s", item.get_responseTimeText()));
            tag.responseTime.setLayoutParams(new TableRow.LayoutParams());
        } else {
            Log.v(TAG, "Setting rtt height to zero");
            tag.responseTime.setHeight(0);
        }
        if (item.has_throughputText()) {
            Log.v(TAG, item.get_throughputText());
            tag.throughput.setText(String.format("Throughput: %s", item.get_throughputText()));
            tag.throughput.setLayoutParams(new TableRow.LayoutParams());
        } else {
            Log.v(TAG, "Setting throughput height to zero");
            tag.throughput.setHeight(0);
        }

        return row;
    }

    static class MyTag {
        TextView providerName;
        TextView connectTime;
        TextView responseTime;
        TextView throughput;
    }
}
