package com.example.myweather.adapter;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.List;

import com.example.myweather.R;
import com.example.myweather.model.CityItem;

/**
 * 已选城市列表项目CityItem的自定义适配器
 */
public class CityItemAdapter extends ArrayAdapter<CityItem> {

    private int resourceId;

    public CityItemAdapter(Context context, int textViewResourceId, List<CityItem> objects) {
        super(context, textViewResourceId, objects);
        resourceId = textViewResourceId; // ListView子布局Id
    }

    @SuppressLint("ViewHolder")
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        CityItem cityItem = getItem(position); // 获取当前项的CityList实例
        View view = LayoutInflater.from(getContext()).inflate(resourceId, null);
        TextView city_name = (TextView) view.findViewById(R.id.city_name);
        city_name.setText(cityItem.getCityName());
        return view;
    }

}
