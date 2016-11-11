package com.bpump.dvtapplication.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.TextView;

import com.bpump.dvtapplication.R;

import java.util.ArrayList;
import java.util.List;

/**
 * 作者：Create on 2016/11/10 09:46  by  qinli
 * 邮箱：
 * 描述：TODO gridview适配器
 * 最近修改：2016/11/10 09:46 modify by qinli
 */

public class GridViewAdapter extends BaseAdapter {
    private Context c;
    private List<String> list = new ArrayList<String>();

    public GridViewAdapter(Context c, List<String> list) {
        this.c = c;
        this.list = list;
    }

    @Override
    public int getCount() {
        return list.size();
    }

    @Override
    public Object getItem(int i) {
        return list.get(i);
    }

    @Override
    public long getItemId(int i) {
        return 0;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder;
        if (convertView == null) {
            convertView = LayoutInflater.from(c).inflate(R.layout.grid_btn_item, null);
            holder = new ViewHolder();
            holder.btn_grid_for_item = (TextView) convertView.findViewById(R.id.btn_grid_for_item);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }
        String str = list.get(position).toString();
        holder.btn_grid_for_item.setText(str);
        return convertView;
    }

    private class ViewHolder {
        TextView btn_grid_for_item;
    }
}
