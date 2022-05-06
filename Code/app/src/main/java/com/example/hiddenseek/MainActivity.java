package com.example.hiddenseek;

import android.content.Context;
import android.location.LocationManager;
import android.os.Bundle;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;


import android.util.DisplayMetrics;
import android.view.Menu;
import android.view.MenuItem;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;

import android.content.Intent;

import com.example.hiddenseek.data.FirebaseListener;
import com.example.hiddenseek.navigation_button.friend.FriendPage;
import com.example.hiddenseek.navigation_button.shop.ShopFragment;

import com.example.hiddenseek.navigation_button.map.MapFragment;
import com.example.hiddenseek.navigation_button.profil.ProfilFragment;
import com.google.android.gms.tasks.Task;

import android.util.Log;

import android.view.WindowManager;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.firebase.auth.FirebaseAuth;

import androidx.annotation.StringRes;


import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.GenericTypeIndicator;

import static com.example.hiddenseek.data.FirebaseListener.getUserInfo;
import static com.example.hiddenseek.data.FirebaseListener.setcurrentActivityNow;
import static com.example.hiddenseek.data.Helper.addItemInDatabase;


public class MainActivity extends AppCompatActivity {
    public static LocationManager locationManager;
    Fragment mapFragment = new MapFragment();
    private Timer treeGrowTimer;
    private TimerTask treeGrow;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        setcurrentActivityNow(this);
        TreeGrow();
        //add toolbar on the top
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        BottomNavigationView bottomNavigationView = findViewById(R.id.nav_menu);
        bottomNavigationView.setOnNavigationItemSelectedListener(listener);
        getSupportFragmentManager().beginTransaction().add(R.id.nav_host_fragment,mapFragment,null).commit();
        bottomNavigationView.setOnNavigationItemReselectedListener(new BottomNavigationView.OnNavigationItemReselectedListener() {
            @Override
            public void onNavigationItemReselected(@NonNull MenuItem item) {
                //do nothing
            }
        });


    }

    private BottomNavigationView.OnNavigationItemSelectedListener listener =
            new BottomNavigationView.OnNavigationItemSelectedListener() {
        @Override
        public boolean onNavigationItemSelected(@NonNull MenuItem item) {
            Fragment fragment = mapFragment;
            switch (item.getItemId()){
                case R.id.navigation_profil:
                    fragment = new ProfilFragment();
                    break;
                case R.id.navigation_shop:
                    fragment = new ShopFragment();
                    break;
                case R.id.navigation_friend:
                    fragment = new FriendPage();
                    break;
                case R.id.navigation_map:
                    fragment = mapFragment;
                    break;
            }
            Fragment currentFragment = getSupportFragmentManager().findFragmentById(R.id.nav_host_fragment);
            if(currentFragment instanceof MapFragment){
                getSupportFragmentManager().beginTransaction().add(R.id.nav_host_fragment, fragment,null)
                        .hide(currentFragment).show(fragment).commit();
            } else if (!(currentFragment instanceof MapFragment) && !(fragment instanceof MapFragment)){
                getSupportFragmentManager().beginTransaction().replace(R.id.nav_host_fragment,fragment).commit();
            } else {
                getSupportFragmentManager().beginTransaction().remove(currentFragment).show(mapFragment).commit();
            }
            Log.d("report3",String.valueOf(getSupportFragmentManager().getFragments()));
            return true;
        }
    };

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.toolbar_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case R.id.logout_toolbar:
                finish();
        }
        return true;
        //return super.onOptionsItemSelected(item);
    }


    //Authen System features
    private void showSnackbar(@StringRes int errorMessageRes) {
        Toast.makeText(getApplicationContext(), errorMessageRes, Toast.LENGTH_SHORT).show();
    }
    private static final int RC_FINISH_DIALOG = 36767;
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == RC_FINISH_DIALOG) {
            setcurrentActivityNow(this);
        }
    }


    private void TreeGrow(){
        String userUID = FirebaseAuth.getInstance().getCurrentUser().getUid();
        DatabaseReference userRef = FirebaseDatabase.getInstance().getReference("users").child(userUID);
        treeGrowTimer = new Timer();
        // Tree Grow
        treeGrow = new TimerTask() {
            public void run(){
                Map<String, Object> userInfo=getUserInfo();
                Map<String, Object> treeList=null;
                if (userInfo!=null)
                    treeList = (Map<String, Object>) userInfo.get("treeList");
                if (treeList!=null) {
                    Map<String, Object> currentTree = (Map<String, Object>) treeList.get("current");
                    if (currentTree != null) {
                        // Sun and Fertilizer
                        if (userInfo.get("sun") != null) {
                            int sun = Integer.parseInt(userInfo.get("sun").toString());
                            int level = Integer.parseInt(currentTree.get("Level").toString());
                            int maxLevel = Integer.parseInt(currentTree.get("maxLevel").toString());
                            long currentTime = new Date().getTime();
                            long lastUpdateTime = Long.parseLong(currentTree.get("lastUpdate").toString());
                            long ms = currentTime - lastUpdateTime;
                            long hour = ms / (60 * 60 * 1000);
                            int count = (int) hour; // grow up each hour
                            Map<String, Object> userUpdates = new HashMap<>();
                            int leveldiff = maxLevel - level;

                            if (leveldiff > 0){
                                // tree is growing
                                if (count >= 1) {
                                    userUpdates.put("treeList/current/lastUpdate", currentTime);
                                    if(sun >0){
                                        int grow = Math.min(leveldiff, Math.min(sun, count));
                                        userUpdates.put("/sun/", sun - grow);
                                        userUpdates.put("treeList/current/Level", level + grow);
                                    }
                                }
                            }
                            userRef.updateChildren(userUpdates);
                        }
                    }

                }
            }
        };
        // For Test
        //treeGrowTimer.schedule(treeGrow, 0, 1000);
        treeGrowTimer.schedule(treeGrow, 0, 10000);
    }




}
