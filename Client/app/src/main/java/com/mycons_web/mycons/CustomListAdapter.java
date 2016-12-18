package com.mycons_web.mycons;


import java.util.ArrayList;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

public class CustomListAdapter extends BaseAdapter {
    private ArrayList<ListItem> listData;
    private LayoutInflater layoutInflater;

    public CustomListAdapter(Context context, ArrayList<ListItem> listData) {
        this.listData = listData;
        layoutInflater = LayoutInflater.from(context);
    }

    @Override
    public int getCount() {
        return listData.size();
    }
//
    @Override
    public Object getItem(int position) {
        return listData.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder;
        if (convertView == null) {
            convertView = layoutInflater.inflate(R.layout.list_row_layout, null);
            holder = new ViewHolder();
            holder.headlineView = (TextView) convertView.findViewById(R.id.categoryTitle);
            holder.reporterNameView = (TextView) convertView.findViewById(R.id.reporter);
            holder.imageView = (ImageView) convertView.findViewById(R.id.thumbImage);
            convertView.setTag(holder);

        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        ListItem newsItem = listData.get(position);
        holder.headlineView.setText(newsItem.getName());
        holder.reporterNameView.setText("Tags: " + newsItem.getTags());
        if (holder.imageView != null) {
            new ImageDownloaderTask(holder.imageView).execute(newsItem.getImage());
        }

        return convertView;
    }

    static class ViewHolder {
        TextView headlineView;
        TextView reporterNameView;
        ImageView imageView;
    }
}
