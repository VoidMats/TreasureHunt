package com.example.mats.treasurehunt;

import android.annotation.SuppressLint;
import android.content.Context;
import android.support.annotation.LayoutRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;   //@SuppressLint("SupportAnnotationUsage")

public class SearchResultAdapter extends ArrayAdapter<ListData> {

    private Context mContext;
    private ArrayList<ListData> resultList = new ArrayList<>();

    public SearchResultAdapter(@NonNull Context _context,  @LayoutRes ArrayList<ListData> _list) {
        super(_context, 0 , _list);
        mContext = _context;
        resultList = _list;
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        View listItem = convertView;
        if(listItem == null)
            listItem = LayoutInflater.from(mContext).inflate(R.layout.searchresult_list,parent,false);

        ListData currentPosition = resultList.get(position);

        TextView name = (TextView) listItem.findViewById(R.id.tvSearchListName);
        name.setText(currentPosition.getName());

        TextView nodes = (TextView) listItem.findViewById(R.id.tvSearchListNodes);
        nodes.setText(String.valueOf(currentPosition.getNoNodes()));

        TextView time = (TextView) listItem.findViewById(R.id.tvSearchListTime);
        TimeCalc tmp = new TimeCalc(currentPosition.getTotalTime());
        time.setText(tmp.getTimeString());

        TextView type = (TextView) listItem.findViewById(R.id.tvSearchListType);
        type.setText(currentPosition.getType());

        return listItem;
    }

    public void refresh(){
        notifyDataSetChanged();
    }
}
