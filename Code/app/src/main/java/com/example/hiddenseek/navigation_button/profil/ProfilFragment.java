package com.example.hiddenseek.navigation_button.profil;

import android.content.Context;
import android.os.Bundle;
import android.os.Environment;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import com.example.hiddenseek.R;
import com.example.hiddenseek.navigation_button.shop.BagFragment;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.HashMap;
import java.util.Map;

import static com.example.hiddenseek.data.Helper.getCurrentUserID;
import static com.example.hiddenseek.data.FirebaseListener.getUserInfo;
import static com.example.hiddenseek.data.FirebaseListener.setCurrentView;

public class ProfilFragment extends Fragment {
    private FirebaseAuth mAuth=FirebaseAuth.getInstance();

    ImageView fertilizerButton;
    private ImageView bagbutton;
    // Declare Context variable at class level in Fragment
    private Map<String, Object> userInfo;

    @Override
    public void onPause() {
        setCurrentView(null);
        super.onPause();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_profil,container,false);
        setCurrentView(view);
        fertilizerButton = view.findViewById(R.id.fertilizer);
        userInfo = getUserInfo();
        ImageView userIcon = view.findViewById(R.id.profil_image);

        WindowManager wm = (WindowManager) getActivity().getSystemService(Context.WINDOW_SERVICE);
        DisplayMetrics dm = new DisplayMetrics();
        wm.getDefaultDisplay().getMetrics(dm);
        int width = dm.widthPixels;
        int height= dm.heightPixels;
        float density = dm.density;
        int screenWidth = (int) (width/density);
        int screenHeight = (int)(height/density);
        /*LinearLayout.LayoutParams params = (LinearLayout.LayoutParams) userIcon.getLayoutParams();
        params.width = (int) (width*0.2);
        params.height = (int) (width*0.2);
        userIcon.setLayoutParams(params);*/

        ScrollView treeName = view.findViewById(R.id.treeNamebar);
        LinearLayout.LayoutParams params2 = (LinearLayout.LayoutParams) treeName.getLayoutParams();
        params2.width = (int) (width*0.6);
        treeName.setLayoutParams(params2);


        // information of the tree
        //fertilizer
        fertilizerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DatabaseReference userRef = FirebaseDatabase.getInstance().getReference("users").child(getCurrentUserID());
                userInfo=getUserInfo();
                Map<String, Object> treeList = (Map<String, Object>) userInfo.get("treeList");
                //Get Fertilizer
                int fertilizer = Integer.parseInt(userInfo.get("fertilizer").toString());
                if (fertilizer > 0) {
                    // Tree
                    if (treeList == null) {
                        Log.e("Tree List", "Tree List is empty");
                    } else {
                        // Current Tree Information
                        Map<String, Object> currentTree = (Map<String, Object>) treeList.get("current");
                        if (currentTree == null) {
                            Log.e("Current Tree", "null");
                            Toast.makeText(getActivity().getApplicationContext(), "Please plant a tree.", Toast.LENGTH_SHORT).show();
                        } else {
                            int level = Integer.parseInt(currentTree.get("Level").toString());
                            Map<String, Object> userUpdates = new HashMap<>();
                            userUpdates.put("treeList/current/Level", level + 1);
                            userUpdates.put("fertilizer", fertilizer - 1);
                            userRef.updateChildren(userUpdates);
                            Toast.makeText(getActivity().getApplicationContext(), "You have used a fertilizer!", Toast.LENGTH_SHORT).show();
                        }

                    }
                }
                else{
                    Toast.makeText(getActivity().getApplicationContext(), "You have no fertilizer!", Toast.LENGTH_SHORT).show();
                }
            }
        });


        //list_user = view.findViewById(R.id.list_view);
        TextView textView = (TextView) view.findViewById(R.id.textView_username);
        textView.setText(mAuth.getCurrentUser().getDisplayName());


        userIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final FragmentTransaction ft = getFragmentManager().beginTransaction();

                ft.replace(R.id.profile_constraint, new IconSelectFragment());
                ft.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN);
                ft.commit();

                ft.addToBackStack(null);
            }
        });

        ImageView honour = view.findViewById(R.id.honour);
        honour.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final FragmentTransaction ft = getFragmentManager().beginTransaction();

                ft.replace(R.id.profile_constraint, new HonourFragment());
                ft.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN);
                ft.commit();

                ft.addToBackStack(null);
            }
        });

        bagbutton = view.findViewById(R.id.bagbutton);
        bagbutton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final FragmentTransaction ft = getFragmentManager().beginTransaction();

                ft.replace(R.id.profile_constraint, new BagFragment());
                ft.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN);
                ft.commit();

                ft.addToBackStack(null);
            }
        });


        return view;
    }
}
