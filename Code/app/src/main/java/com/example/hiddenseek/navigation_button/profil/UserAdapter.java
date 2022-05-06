package com.example.hiddenseek.navigation_button.profil;

import android.content.Context;
import android.text.Layout;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.hiddenseek.R;

import java.util.ArrayList;

public class UserAdapter extends BaseAdapter {
    private ArrayList<User> data;
    private Context context;

    public UserAdapter(ArrayList<User> data,Context context){
        this.data = data;
        this.context = context;
    }
    @Override
    public int getCount() {
        return data.size();
    }

    @Override
    public Object getItem(int position) {
        return data.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        convertView = LayoutInflater.from(context).inflate(R.layout.profil_list_view_item, parent, false);
        TextView t_name = convertView.findViewById(R.id.item_name);
        TextView t_say = convertView.findViewById(R.id.item_say);
        ImageView t_image = convertView.findViewById(R.id.profil_image);
        t_name.setText(data.get(position).name);
        t_say.setText(data.get(position).say);

        return convertView;
    }
}
