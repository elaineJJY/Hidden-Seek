package com.example.hiddenseek.navigation_button.map;

import android.app.Dialog;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.DialogFragment;

import com.example.hiddenseek.R;
import com.example.hiddenseek.data.DailyMission;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.GenericTypeIndicator;

import org.osmdroid.util.GeoPoint;

import java.util.HashMap;
import java.util.Map;

import static com.example.hiddenseek.data.Helper.addItemInDatabase;
import static com.example.hiddenseek.data.Helper.getCurrentUserID;
import static com.example.hiddenseek.data.FirebaseListener.getUserInfo;


public class  DaliyMissionFragment extends DialogFragment {
    private GeoPoint mCurrentLocation;
    private int score;
    public DaliyMissionFragment(GeoPoint mCurrentLocation) {
        this.mCurrentLocation = mCurrentLocation;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState)
    {

        View view = inflater.inflate(R.layout.mission_dialogview, container);
        getDialog().requestWindowFeature(Window.FEATURE_NO_TITLE);
        TextView nav_button = view.findViewById(R.id.description_mission);
        LinearLayout seedpart = view.findViewById(R.id.seedpart);
        ImageView descriptionIcon = view.findViewById(R.id.descriptionicon_mission);

        Button cButton = view.findViewById(R.id.complete_mission);

        DailyMission dayMission=new DailyMission();
        if (dayMission.ifPOINear(getContext(), FirebaseAuth.getInstance().getCurrentUser().getUid(),mCurrentLocation))
            cButton.setVisibility(View.VISIBLE);
        else
            cButton.setVisibility(View.GONE);
        nav_button.setText(dayMission.readDailyMission(getContext(), FirebaseAuth.getInstance().getCurrentUser().getUid()));
        String targetposition = dayMission.readPosition(getContext(), FirebaseAuth.getInstance().getCurrentUser().getUid());
        if(targetposition.equals("cinema")){
            descriptionIcon.setImageResource(R.drawable.whitebear);
        }else if(targetposition.equals("park")){
            descriptionIcon.setImageResource(R.drawable.unicorn);
        }else if(targetposition.equals("supermarket")){
            descriptionIcon.setImageResource(R.drawable.girl);
        }
        if(targetposition.equals("supermarket")){
            cButton.setText("Buy it!");
            seedpart.setVisibility(View.VISIBLE);
        }
        cButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String userUID = FirebaseAuth.getInstance().getCurrentUser().getUid();
                Map<String, Object> userInfo = getUserInfo();
                DatabaseReference userRef = FirebaseDatabase.getInstance().getReference().child("users").child(userUID);
                if(targetposition.equals("cinema")){
                    addItemInDatabase("score",100);
                    Toast.makeText(getActivity().getApplicationContext(), "Mission complete! You get 100 coin!", Toast.LENGTH_SHORT).show();
                }
                if(targetposition.equals("park")){
                    addItemInDatabase("fertilizer",1);
                    Toast.makeText(getActivity().getApplicationContext(), "Mission complete! You get a fertilizer!", Toast.LENGTH_SHORT).show();
                }
                if(targetposition.equals("supermarket")){

                    score = Integer.parseInt(userInfo.get("score").toString());
                    DatabaseReference itemRef = FirebaseDatabase.getInstance().getReference().child("shop").child("tree").child("WaterLily");
                    itemRef.get().addOnCompleteListener(new OnCompleteListener<DataSnapshot>() {
                        @Override
                        public void onComplete(@NonNull Task<DataSnapshot> task) {
                            if (!task.isSuccessful()) {
                                Log.e("firebase", "Error getting data", task.getException());
                            } else {
                                GenericTypeIndicator<Map<String, Object>> t = new GenericTypeIndicator<Map<String, Object>>() {
                                };
                                Map<String, Object> itemInfo = task.getResult().getValue(t);
                                int price = Integer.parseInt(itemInfo.get("price").toString());
                                if (score >= price) {
                                    userRef.child("bag").child("tree").child("WaterLily").child("number").get().addOnCompleteListener(new OnCompleteListener<DataSnapshot>() {
                                        @Override
                                        public void onComplete(@NonNull Task<DataSnapshot> task) {
                                            if (!task.isSuccessful()) {
                                                Log.e("firebase", "Error getting data", task.getException());
                                            } else {
                                                Map<String, Object> userUpdates = new HashMap<>();
                                                userUpdates.put("score", score - price);

                                                itemInfo.put("number", 1);
                                                if (task.getResult().exists()) {
                                                    itemInfo.put("number", task.getResult().getValue(Integer.class) + 1);
                                                }
                                                userUpdates.put("bag/tree/WaterLily", itemInfo);
                                                userRef.updateChildren(userUpdates);
                                            }
                                        }
                                    });
                                    Toast.makeText(getActivity().getApplicationContext(), "You have bought the Water lily seed!", Toast.LENGTH_SHORT).show();
                                } else {
                                    Toast.makeText(getActivity().getApplicationContext(), "Coin not enough!", Toast.LENGTH_SHORT).show();
                                    Log.e("Buy", "Score Not Enough");
                                }
                            }
                        }
                    });
                }
                dayMission.installDailyMission(getContext(), getCurrentUserID(),true);
                cButton.setVisibility(View.GONE);
                nav_button.setText(dayMission.readDailyMission(getContext(), FirebaseAuth.getInstance().getCurrentUser().getUid()));
            }
        });
        return view;
    }

    @Override
    public void onStart() {
        super.onStart();
        Window window = getDialog().getWindow();
        window.setBackgroundDrawable( new ColorDrawable(Color.WHITE));

        WindowManager.LayoutParams windowParams = window.getAttributes();
        windowParams.dimAmount = 0.0f;
        windowParams.y = 100;

        window.setAttributes(windowParams);
        Dialog dialog = getDialog();
        if (dialog != null) {
            DisplayMetrics dm = new DisplayMetrics();
            getActivity().getWindowManager().getDefaultDisplay().getMetrics(dm);
            dialog.getWindow().setLayout((int) (dm.widthPixels * 0.75), (int) (dm.heightPixels * 0.3));
        }
        DisplayMetrics dm = new DisplayMetrics();
        getActivity().getWindowManager().getDefaultDisplay().getMetrics( dm );

        WindowManager.LayoutParams params = window.getAttributes();
        params.height = ViewGroup.LayoutParams.WRAP_CONTENT;
        window.setAttributes(params);

    }


}
