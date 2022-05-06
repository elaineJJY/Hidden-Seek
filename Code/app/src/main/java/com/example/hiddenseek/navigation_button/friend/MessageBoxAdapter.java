package com.example.hiddenseek.navigation_button.friend;

import android.content.Context;
import android.os.Environment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;

import com.bumptech.glide.Glide;
import com.example.hiddenseek.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.FirebaseDatabase;

import java.io.File;
import java.io.IOException;
import java.util.List;

import static com.example.hiddenseek.data.FileController.getUserIcon;

public class MessageBoxAdapter extends ArrayAdapter<MessageBox> {
    private int resourceId;

    public MessageBoxAdapter(Context context, int textViewResourceId, List<MessageBox> objects) {
        super(context, textViewResourceId, objects);
        resourceId = textViewResourceId;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        MessageBox messageBox = getItem(position);
        View view;
        MessageBoxAdapter.ViewHolder viewHolder;
        if (convertView == null){
            view = LayoutInflater.from(getContext()).inflate(resourceId, null);

            viewHolder = new MessageBoxAdapter.ViewHolder();
            viewHolder.messageBoxImage = (ImageView) view.findViewById(R.id.messagebox_image);
            viewHolder.messageBoxName = (TextView) view.findViewById(R.id.messagebox_name);
            viewHolder.messageBoxContent = (TextView) view.findViewById(R.id.messagebox_content);
            viewHolder.messageBoxTime = (TextView) view.findViewById(R.id.messagebox_time);
            view.setTag(viewHolder);
        }else{
            view = convertView;
            viewHolder = (MessageBoxAdapter.ViewHolder) view.getTag();
        }
        getUserIcon(getContext(),messageBox.getFriend().getUid(),viewHolder.messageBoxImage);
        viewHolder.messageBoxName.setText(messageBox.getFriend().getName());
        viewHolder.messageBoxContent.setText(messageBox.getLastMessage());
        viewHolder.messageBoxTime.setText(messageBox.getLastTime());

        return view;

    }

    class ViewHolder{
        ImageView messageBoxImage;
        TextView messageBoxName;
        TextView messageBoxContent;
        TextView messageBoxTime;
    }
}
