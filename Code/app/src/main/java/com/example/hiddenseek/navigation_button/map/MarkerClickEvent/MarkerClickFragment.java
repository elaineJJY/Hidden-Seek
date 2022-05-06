package com.example.hiddenseek.navigation_button.map.MarkerClickEvent;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.net.IpSecManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.DialogFragment;

import com.example.hiddenseek.R;
import com.example.hiddenseek.data.Msg;
import com.example.hiddenseek.navigation_button.shop.Gift;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.GenericTypeIndicator;

import org.jetbrains.annotations.NotNull;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.Marker;

import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

import static com.example.hiddenseek.data.FirebaseListener.getShopTreeList;
import static com.example.hiddenseek.data.Helper.addItemInDatabase;
import static com.example.hiddenseek.data.Helper.rewriteSunOrBoxInFile;
import static com.example.hiddenseek.data.Helper.sendMsg;

public class MarkerClickFragment extends DialogFragment {
    private Marker marker;
    private GeoPoint mCurrentLocation;
    private boolean specialseed;
    private Context context;
    private MapView mapView;
    private HashMap<Marker, Integer> boxValue;

    public MarkerClickFragment(Marker marker, MapView mapView, HashMap<Marker, Integer> boxValue,
                               GeoPoint mCurrentLocation, boolean specialseed, Context c) {
        this.marker = marker;
        this.mapView = mapView;
        this.mCurrentLocation = mCurrentLocation;
        this.specialseed = specialseed;
        this.context = c;
        this.boxValue = boxValue;
    }

    public Marker getMarker() {
        return this.marker;
    }

    private void tryToGetSeed(Marker marker, String itemID) {
        String key = marker.getId();
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference().child("global_seed");
        String uid = FirebaseAuth.getInstance().getUid();
        ref.child(key).get().addOnCompleteListener(new OnCompleteListener<DataSnapshot>() {
            @Override
            public void onComplete(@NonNull @NotNull Task<DataSnapshot> task) {
                if (task.isSuccessful()) {
                    if (task.getResult().exists()) {
                        task.getResult().getRef().child("property").get()
                                .addOnCompleteListener(new OnCompleteListener<DataSnapshot>() {
                                    @Override
                                    public void onComplete(@NonNull @NotNull Task<DataSnapshot> task) {
                                        if (task.isSuccessful()) {
                                            GenericTypeIndicator<Map<String, Object>> t = new GenericTypeIndicator<Map<String, Object>>() {
                                            };
                                            Map<String, Object> info = task.getResult().getValue(t);
                                            int capture_time = Integer.valueOf((String) info.get("capture_time"));
                                            if (!info.containsKey(uid)) {
                                                task.getResult().getRef().child("capture_time")
                                                        .setValue(String.valueOf(capture_time - 1));
                                                task.getResult().getRef().child(uid).setValue("1");

                                                String userUID = FirebaseAuth.getInstance().getCurrentUser().getUid();
                                                String catalog = "tree";

                                                DatabaseReference userRef = FirebaseDatabase.getInstance().getReference().child("users").child(userUID);
                                                DatabaseReference itemRef = FirebaseDatabase.getInstance().getReference().child("shop").child(catalog).child(itemID);
                                                itemRef.get().addOnCompleteListener(new OnCompleteListener<DataSnapshot>() {
                                                    @Override
                                                    public void onComplete(@NonNull Task<DataSnapshot> task) {
                                                        if (!task.isSuccessful()) {
                                                        } else {
                                                            GenericTypeIndicator<Map<String, Object>> t = new GenericTypeIndicator<Map<String, Object>>() {
                                                            };
                                                            Map<String, Object> itemInfo = task.getResult().getValue(t);

                                                            userRef.child("bag").child(catalog).child(itemID).child("number").get().addOnCompleteListener(new OnCompleteListener<DataSnapshot>() {
                                                                @Override
                                                                public void onComplete(@NonNull Task<DataSnapshot> task) {
                                                                    if (!task.isSuccessful()) {
                                                                    } else {
                                                                        Map<String, Object> userUpdates = new HashMap<>();
                                                                        itemInfo.put("number", 1);
                                                                        if (task.getResult().exists()) {
                                                                            itemInfo.put("number", task.getResult().getValue(Integer.class) + 1);
                                                                        }
                                                                        userUpdates.put("bag/tree/" + itemID, itemInfo);
                                                                        userRef.updateChildren(userUpdates);
                                                                    }
                                                                }
                                                            });

                                                        }
                                                    }
                                                });
                                                Toast.makeText(context, "You have picked a " + itemID +" successfully!",
                                                        Toast.LENGTH_LONG).show();
                                            } else {
                                                Toast.makeText(context, "You have already picked the seed!",
                                                        Toast.LENGTH_LONG).show();
                                            }
                                        } else {
                                            Toast.makeText(context, R.string.connection_failed, Toast.LENGTH_LONG)
                                                    .show();
                                        }
                                    }
                                });
                    } else {
                        Toast.makeText(context, "You come too late!", Toast.LENGTH_LONG).show();
                    }
                } else {
                    Toast.makeText(context, R.string.connection_failed, Toast.LENGTH_LONG).show();
                }
            }
        });
    }

    private void requestToBag() {
    }

    public String formatLongToTimeStr(Long l) {
        int hour = 0;
        int minute = 0;
        int second = 0;
        second = l.intValue();
        if (second > 60) {
            minute = second / 60;
            second = second % 60;
        }
        if (minute > 60) {
            hour = minute / 60;
            minute = minute % 60;
        }
        String strtime = hour + " hour " + minute + " minutes";
                //+ second + "s";
        return strtime;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        LayoutInflater inflater = requireActivity().getLayoutInflater();
        View view = inflater.inflate(R.layout.markerclick_dialogview, null);
        TextView distance = view.findViewById(R.id.distance_marker);
        TextView description = view.findViewById(R.id.description_marker);
        TextView title = view.findViewById(R.id.title_marker);
        ImageView image = view.findViewById(R.id.descriptionicon_marker);
        TextView countdown = view.findViewById(R.id.countdown_marker);
        Drawable drawable;
        if (specialseed) {
            DatabaseReference ref = FirebaseDatabase.getInstance().getReference().child("global_seed").child(marker.getId()).child("property");
            ref.get().addOnCompleteListener(new OnCompleteListener<DataSnapshot>() {
                @Override
                public void onComplete(@NonNull @NotNull Task<DataSnapshot> task) {
                    if (task.isSuccessful()) {
                        GenericTypeIndicator<Map<String, Object>> t = new GenericTypeIndicator<Map<String, Object>>() {
                        };
                        Map<String, Object> itemInfo = task.getResult().getValue(t);
                        countdown.setVisibility(View.VISIBLE);
                        long add_time = Long.valueOf(itemInfo.get("add_time").toString());
                        long left_time = 2l * 60l * 60l * 1000l - (new Date().getTime() - add_time);
                        String formatLongToTimeStr = formatLongToTimeStr(left_time / 1000);
                        countdown.setText(" Seeds available: " + itemInfo.get("capture_time")+"/3");
                        countdown.append("\n");
                        countdown.append(" Time Left: " + formatLongToTimeStr);
                    }
                }
            });
            drawable = ContextCompat.getDrawable(getContext(), R.drawable.snowlotus);
            title.setText("Special Seed founded!");
            description.setVisibility(View.GONE);
            //description.setText(R.string.descriptor_marker_for_sepcialseed);
            image.setImageDrawable(drawable);
        } else {
            drawable = ContextCompat.getDrawable(getContext(), R.drawable.box1);
            image.setImageDrawable(drawable);
            title.setText("Confused Treasure Box");
            description.setText(R.string.descriptor_marker);
        }
        double dis = marker.getPosition().distanceToAsDouble(mCurrentLocation);
        if (dis > 1000d) {
            distance.setText(new DecimalFormat("0.00").format(dis / 1000) + " km");
        } else {
            distance.setText(new DecimalFormat("0.00").format(dis) + " m");
        }
        builder.setView(view);
        if (dis < 50d) {
            builder.setPositiveButton("open", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    if (specialseed == true) {
                        //Toast.makeText(context, "触发点击事件", Toast.LENGTH_SHORT).show();
                        double roll = Math.random();
                        String itemId = "SnowLotus";
                        if (roll > 0.5) {
                            itemId = "WaterLily";
                        }
                        tryToGetSeed(marker, itemId);
                    } else {
                        if (marker.getId().equals("local")) {
                            rewriteSunOrBoxInFile(getContext(), marker, "boxList");
                            addItemInDatabase("score", boxValue.get(marker));
                            String text = "Got " + String.valueOf(boxValue.get(marker)) + " Coins!";
                            Toast.makeText(context, text, Toast.LENGTH_SHORT).show();
                            marker.remove(mapView);
                        } else {
                            FirebaseAuth mAuth = FirebaseAuth.getInstance();
                            DatabaseReference mapRef = FirebaseDatabase.getInstance().getReference("map");
                            DatabaseReference bomb_property_Ref = mapRef.child(marker.getId()).child("property");
                            DatabaseReference c_userRef = FirebaseDatabase.getInstance().getReference("users").child(mAuth.getUid());
                            bomb_property_Ref.child("Uid").get().addOnCompleteListener(new OnCompleteListener<DataSnapshot>() {
                                @Override
                                public void onComplete(@NonNull Task<DataSnapshot> task) {
                                    if (task.isSuccessful()) {
                                        String trapMakerUid = task.getResult().getValue(String.class);
                                        DatabaseReference r_userRef = FirebaseDatabase.getInstance().getReference("users").child(trapMakerUid);

                                        if (!task.getResult().getValue(String.class).equals(mAuth.getUid())) {
                                            addItemInDatabase("score", -100);
                                            r_userRef.child("score").get().addOnCompleteListener(new OnCompleteListener<DataSnapshot>() {
                                                @Override
                                                public void onComplete(@NonNull Task<DataSnapshot> task) {
                                                    if (task.isSuccessful()) {
                                                        r_userRef.child("score").setValue(task.getResult().getValue(Integer.class) + 400);
                                                    }
                                                }
                                            });
                                            //sendMsg(context, "Congradulation! You get 100 coins from me!", trapMakerUid, mAuth.getUid());
                                            sendMsg(context, "OHOH! You opened my bomb. I get 100 coins from you.", mAuth.getUid(), trapMakerUid);
                                            Msg msg = new Msg("OOps. I was blown to bits.You get 100 coins from me.", Msg.TYPE_RECEIVED,  new SimpleDateFormat("MM.dd.yyyy HH:mm").format(Calendar.getInstance().getTime()));
                                            FirebaseDatabase.getInstance()
                                                    .getReference()
                                                    .child("users")
                                                    .child(trapMakerUid)
                                                    .child("msgs")
                                                    .child(mAuth.getUid())
                                                    .push()
                                                    .setValue(msg);
                                            Toast.makeText(context, "OHOH! You opened a bomb! 100 Coins deducted!", Toast.LENGTH_LONG).show();
                                        } else {
                                            Toast.makeText(context, "You opened a bomb from yourself! Get nothing.", Toast.LENGTH_LONG).show();
                                        }
                                        mapRef.child(marker.getId()).removeValue();
                                    } else {
                                        Toast.makeText(context, R.string.connection_failed, Toast.LENGTH_LONG).show();
                                    }
                                }
                            });
                        }
                    }
                }
            });
        } else {
            description.append(" You need to approach me first!");
        }
        return builder.create();
    }
}