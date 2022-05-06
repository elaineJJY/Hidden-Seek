package com.example.hiddenseek.navigation_button.friend;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.hiddenseek.R;
import com.example.hiddenseek.DialogActivity;
import com.example.hiddenseek.data.MsgContainer;
import com.google.firebase.auth.FirebaseAuth;

import java.util.List;

import static com.example.hiddenseek.data.FileController.getUserIcon;

public class FriendAdapter extends ArrayAdapter<Friend> {
    private int resourceId;
    private Context context;

    public FriendAdapter(Context context,
                       int textViewResourceId,
                       List<Friend> objects) {
        super(context, textViewResourceId, objects);
        this.context=context;
        resourceId = textViewResourceId;
    }
    private static final int RC_FINISH_DIALOG = 36767;
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        Friend friend = getItem(position);
        View view;
        FriendAdapter.ViewHolder viewHolder;
        if (convertView == null){
            view = LayoutInflater.from(getContext()).inflate(resourceId, null);
            viewHolder = new FriendAdapter.ViewHolder();
            viewHolder.friendImage = (ImageView) view.findViewById(R.id.friend_image);
            viewHolder.friendImage.setClickable(true);
            viewHolder.friendImage.setOnClickListener( new View.OnClickListener() {
                public void onClick(View view){
                    Intent intent = new Intent(context, DialogActivity.class);
                    Bundle bundle = new Bundle();
                    String targetID=friend.getUid();
                    String targetName = friend.getName();
                    bundle.putString("targetID",targetID);
                    bundle.putString("targetName",targetName);
                    MsgContainer.setProfile(context, FirebaseAuth.getInstance().getCurrentUser().getUid(), targetID, friend.getName());
                    intent.putExtras(bundle);
                    ((Activity)context).startActivityForResult(intent,RC_FINISH_DIALOG);
                }
            });
            viewHolder.friendName = (TextView) view.findViewById(R.id.friend_name);
            view.setTag(viewHolder);
        }else{
            view = convertView;
            viewHolder = (FriendAdapter.ViewHolder) view.getTag();
        }
        getUserIcon(getContext(),friend.getUid(),viewHolder.friendImage);

        viewHolder.friendName.setText(friend.getName());

        return view;

    }

    class ViewHolder{
        ImageView friendImage;
        TextView friendName;

    }

}
