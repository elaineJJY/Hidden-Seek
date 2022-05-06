package com.example.hiddenseek.navigation_button.shop;

import android.content.Context;
import android.os.Environment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.DrawableRes;
import androidx.annotation.NonNull;

import com.android.volley.toolbox.ImageLoader;
import com.bumptech.glide.Glide;
import com.bumptech.glide.Registry;
import com.bumptech.glide.annotation.GlideModule;
import com.bumptech.glide.module.AppGlideModule;
import com.example.hiddenseek.R;
import com.firebase.ui.storage.images.FirebaseImageLoader;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.storage.FileDownloadTask;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;

public class GiftAdapter extends ArrayAdapter<Gift> {

    private int resourceId;
    private static File CacheRoot;
    private String UserDirPath;

    public GiftAdapter(Context context,
                       int textViewResourceId,
                       List<Gift> objects){
        super(context, textViewResourceId, objects);
        resourceId = textViewResourceId;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        Gift gift = getItem(position);
        View view;
        ViewHolder viewHolder;
        if (convertView == null){
            view = LayoutInflater.from(getContext()).inflate(resourceId, null);
            viewHolder = new ViewHolder();
            viewHolder.giftImage = (ImageView) view.findViewById(R.id.gift_image);
            if(view.findViewById(R.id.gift_info)!=null)
            viewHolder.giftName = (TextView) view.findViewById(R.id.gift_info);
            if(view.findViewById(R.id.gift_price)!=null)
            viewHolder.giftPrice = (TextView) view.findViewById(R.id.gift_price);
            view.setTag(viewHolder);
        }else{
            view = convertView;
            viewHolder = (ViewHolder) view.getTag();
        }

        String fileName = gift.getRid();
        // Reference to an image file in Cloud Storage
        CacheRoot = getContext().getFilesDir();
        try {
            if (!gift.getType().equals("fertilizer"))
                UserDirPath = CacheRoot.getCanonicalPath()
                    + File.separator + "tree"+  File.separator + fileName;
            if (gift.getType().equals("icon"))
                UserDirPath = CacheRoot.getCanonicalPath()
                        + File.separator + "icons";
        } catch (IOException e) {
            e.printStackTrace();
        }
        if (!gift.getType().equals("fertilizer") && !gift.getType().equals("icon"))
        {
            File localFile = new File(UserDirPath,"10");
            File[] tempFiles = localFile.listFiles();
            if (localFile.exists()) {
                Glide.with(getContext())
                        .load(tempFiles[0])
                        .into(viewHolder.giftImage);
            }
        }
        else
        if(gift.getType().equals("icon")){
            File localFile = new File(UserDirPath,fileName);
            File[] tempFiles = localFile.listFiles();
            if (localFile.exists()) {
                Glide.with(getContext())
                        .load(tempFiles[0])
                        .into(viewHolder.giftImage);
            }
        }
        else
        {
            viewHolder.giftImage.setImageResource(R.drawable.fertilizer);
        }

        if(viewHolder.giftName!=null) {
            if (gift.getType().equals("tree")) {
                viewHolder.giftName.setText(gift.getName());
            }
            if (gift.getType().equals("histree")) {
                viewHolder.giftName.setText(gift.getName() + " on " + gift.getAmount());
            }
            if (gift.getType().equals("bag")) {
                viewHolder.giftName.setText(gift.getName() + ": " + gift.getAmount() + " Seeds");
            }
            if (gift.getType().equals("fertilizer")) {
                viewHolder.giftName.setText("Fertilizer");
            }
        }
        if(viewHolder.giftPrice!=null)
        viewHolder.giftPrice.setText(gift.getPrice()+"");

        return view;

    }

    class ViewHolder{
        ImageView giftImage;
        TextView giftName;
        TextView giftPrice;
    }


}
