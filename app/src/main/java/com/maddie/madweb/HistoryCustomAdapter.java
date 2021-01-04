package com.maddie.madweb;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.List;

public class HistoryCustomAdapter extends BaseAdapter {

    private List<HistoryItem> itemList;
    private LayoutInflater inflater;

    public HistoryCustomAdapter(Activity context, List<HistoryItem> history) {
        super();
        this.itemList = history;
        this.inflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    //abstract methods used by parent - except getItemID?
    public int getCount() { return itemList.size(); }
    public Object getItem(int position) { return itemList.get(position); }
    public long getItemId(int position) { return position; }


    //convenient store to be used with tagging
    public static class ViewHolder {
        TextView titleTxt;
        TextView urlTxt;
    }

    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder;

        if (convertView == null) {
            //no previous view, instantiate holder, inflate view and setup views of holder
            holder = new ViewHolder();
            convertView = inflater.inflate(R.layout.listitem_row, null);

            holder.titleTxt = (TextView) convertView.findViewById(R.id.titleTxt);
            holder.urlTxt = (TextView) convertView.findViewById(R.id.urlTxt);

            //Remember the holder
            convertView.setTag(holder);
        } else {
            //Get holder to prepare for update with new data
            holder = (ViewHolder) convertView.getTag();
        }

        //Update views
        HistoryItem histItem = (HistoryItem) itemList.get(position);
        holder.titleTxt.setText(histItem.getTitle());
        holder.urlTxt.setText(histItem.getUrl());
        return convertView;
    }
}
