package com.example.hiddenseek.data;

import android.app.Activity;
import android.content.Context;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.bumptech.glide.Glide;
import com.example.hiddenseek.DialogActivity;
import com.example.hiddenseek.MainActivity;
import com.example.hiddenseek.R;
import com.example.hiddenseek.navigation_button.shop.Gift;
import com.example.hiddenseek.navigation_button.shop.GiftAdapter;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.GenericTypeIndicator;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.ListResult;
import com.google.firebase.storage.StorageReference;

import java.io.File;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Stack;

import static com.example.hiddenseek.data.Helper.addItemInDatabase;
import static com.example.hiddenseek.data.Helper.getCertainItemInHashMapRetrieve;
import static com.example.hiddenseek.data.Helper.getCurrentUserID;
import static com.firebase.ui.auth.AuthUI.getApplicationContext;

public class FirebaseListener {
    public static Context currentActivityNow = null;
    public static Query Messagequery;
    public static ChildEventListener MessageListener;
    public static Map<String, Object> userInfo;
    public static Query scorequery;
    public static ValueEventListener scoreListener;
    public static View currentView=null;
    public static ListResult iconList =null;
    public static ArrayList<Gift> shopTreeList =null ;


    public static ListResult getIconList(){
        return iconList;
    }

    public static ArrayList<Gift> getShopTreeList(){
        return shopTreeList;
    }

    public static boolean isReady(){
        return shopTreeList !=null && iconList !=null && userInfo!=null;
    }
    private static void ShopDownload(){
        File CacheRoot = currentActivityNow.getFilesDir();
        shopTreeList= new ArrayList<>();
        FirebaseStorage.getInstance().getReference().child("shop").child("tree").listAll().addOnSuccessListener(new OnSuccessListener<ListResult>() {
            @Override
            public void onSuccess(ListResult listResult) {
                // All the items under listRef.
                for (StorageReference prefix : listResult.getPrefixes()) {
                    String itemUID = prefix.getName();
                    // get the information about the item
                    FirebaseDatabase.getInstance().getReference("shop").child("tree").child(itemUID)
                            .get().addOnCompleteListener(new OnCompleteListener<DataSnapshot>(){
                        @Override
                        public void onComplete(@NonNull Task<DataSnapshot> task) {
                            if (!task.isSuccessful()) {
                                Log.e("firebase", "Error getting data", task.getException());
                            }
                            else {
                                // All the informations of this item
                                GenericTypeIndicator<Map<String, Object>> t = new GenericTypeIndicator<Map<String, Object>>() {
                                };
                                Map<String,Object> info = task.getResult().getValue(t);
                                Map<String,String> p = (Map<String,String>) info.get("photoRef");
                                String price = String.valueOf(info.get("price"));
                                String name = (String) info.get("name");
                                if ((boolean)info.get("available")) {
                                    Gift tempGift = new Gift();
                                    tempGift.setPrice(Integer.valueOf(price).intValue());
                                    tempGift.setName(name);
                                    tempGift.setRid(itemUID);
                                    tempGift.setType("tree");
                                    shopTreeList.add(tempGift);
                                }
                                for(Map.Entry<String, String> titem: p.entrySet()) {
                                    String treeLevel = titem.getKey();
                                    String pRef = titem.getValue();

                                    //set image
                                    StorageReference item = prefix.child(pRef);
                                    // List all the picture of the tree
                                    try {
                                        String UserDirPath = CacheRoot.getCanonicalPath()
                                                + File.separator + "tree"+File.separator + itemUID;
                                        File localFile = new File(UserDirPath, treeLevel);
                                        if (localFile.list() != null) return;
                                        if (!localFile.exists()) {
                                            localFile.mkdirs();
                                        }
                                        if (!localFile.isFile()) {
                                            localFile.createNewFile();
                                        }
                                        File tempFile = File.createTempFile("tree", "png", localFile);
                                        item.getFile(tempFile);
                                    } catch (IOException e) {
                                        e.printStackTrace();
                                    }
                                }
                            }
                        }
                    });

                }
            }
        })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {

                    }
                });
        FirebaseStorage.getInstance().getReference().child("users").listAll().addOnSuccessListener(new OnSuccessListener<ListResult>() {
            @Override
            public void onSuccess(ListResult listResult) {
                // All the items under listRef.
                iconList=listResult;
                for (StorageReference item : listResult.getItems()) {
                    // item Image
                    String itemUID = item.getName();

                    try {
                        String UserDirPath = CacheRoot.getCanonicalPath()
                                + File.separator + "icons";
                        File localFile = new File(UserDirPath, itemUID);
                        if (localFile.list() == null) {
                            if (!localFile.exists()) {
                                localFile.mkdirs();
                            }
                            if (!localFile.isFile()) {
                                localFile.createNewFile();
                            }
                            File tempFile = File.createTempFile("icon", "png", localFile);
                            item.getFile(tempFile);
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
            }
        });
    }

    public static void addMsgListener(Activity currentActivity) {
        if (currentActivityNow == null) {
            currentActivityNow = currentActivity;
            FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

            String currentUserID = user.getUid();
            DatabaseReference MessageRef=FirebaseDatabase.getInstance().getReference("users").child(currentUserID).child("msgs");
            Messagequery = MessageRef;
            scorequery = FirebaseDatabase.getInstance().getReference("users").child(currentUserID);
            scoreListener= new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    GenericTypeIndicator<Map<String, Object>> t = new GenericTypeIndicator<Map<String, Object>>() {
                    };
                    Log.e("refresh userinfo","now");
                    userInfo = dataSnapshot.getValue(t);
                    if (currentView!=null)
                    {
                        refreshProfilView();
                    }
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {
                    // Getting Post failed, log a message
                    Log.w("Database", "loadPost:onCancelled", databaseError.toException());
                }
            };
            MessageListener = new ChildEventListener() {
                @Override
                public void onChildAdded(DataSnapshot dataSnapshot, String previousChildName) {
                    // msg sb
                    GenericTypeIndicator<HashMap<String, Msg>> t = new GenericTypeIndicator<HashMap<String, Msg>>() {
                    };
                    HashMap<String, Msg> messages = dataSnapshot.getValue(t);
                    String senderUID = dataSnapshot.getKey();
                    MsgContainer msgContainer = new MsgContainer();
                    msgContainer.writeMsgsToTemp(currentActivityNow, messages, currentUserID, senderUID, true);
                    if (currentActivityNow.getClass().equals(MainActivity.class)) {
                        BottomNavigationView bottomNavigationView = ((MainActivity) currentActivityNow).findViewById(R.id.nav_menu);
                        bottomNavigationView.getMenu().findItem(R.id.navigation_friend).setTitle("New message arrives");
                        bottomNavigationView.getMenu().findItem(R.id.navigation_friend).setIcon(R.drawable.ic_stat_name);
                    }
                    MessageRef.child(senderUID).removeValue();
                }

                @Override
                public void onChildChanged(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
                    GenericTypeIndicator<Map<String, Msg>> t = new GenericTypeIndicator<Map<String, Msg>>() {
                    };
                    Map<String, Msg> messages = snapshot.getValue(t);

                    Log.e("ChatMessageDatabase:", messages.toString());

                }

                @Override
                public void onChildRemoved(@NonNull DataSnapshot snapshot) {

                }

                @Override
                public void onChildMoved(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {

                }

                @Override
                public void onCancelled(DatabaseError databaseError) {
                    // ...
                }
            };
            Messagequery.addChildEventListener(MessageListener);
            scorequery.addValueEventListener(scoreListener);
            ShopDownload();
        } else
            setcurrentActivityNow(currentActivity);
    }

    public static Map<String, Object> getUserInfo() {
        return userInfo;
    }

    public static void setcurrentActivityNow(Activity currentActivity) {
        currentActivityNow = currentActivity;
        if (currentActivity == null) {
            Messagequery.removeEventListener(MessageListener);
            scorequery.removeEventListener(scoreListener);
        }
    }

    public static void setCurrentView(View view){
        currentView=view;
        if (view!=null)
            refreshProfilView();
    }

    private static void refreshProfilView(){
        ImageView userIcon = currentView.findViewById(R.id.profil_image);
        String fileName = (String)userInfo.get("icon");
        String UserDirPath;
        // Reference to an image file in Cloud Storage
        File CacheRoot = currentActivityNow.getFilesDir();
        // ImageView in the Activity
        try {
            UserDirPath = CacheRoot.getCanonicalPath()
                    + File.separator + "icons";
            File localFile = new File(UserDirPath,fileName);
            File[] tempFiles = localFile.listFiles();
            if (localFile.exists()) {
                Glide.with(currentActivityNow)
                        .load(tempFiles[0])
                        .into(userIcon);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }


        if(userInfo.get("sun") != null){

            int sun = Integer.parseInt(userInfo.get("sun").toString());
            int fertilizer = Integer.parseInt(userInfo.get("fertilizer").toString());

            ((TextView)currentView.findViewById(R.id.treeSun)).setText(": "+sun);
            if(sun==0){
                ((TextView)currentView.findViewById(R.id.treeSun)).setText(": "+sun+"(Sun not enough!)");
            }
            ((TextView)currentView.findViewById(R.id.treeFertilizer)).setText("X"+fertilizer);
            ArrayList<String> currentTreePath= new ArrayList<>();
            currentTreePath.add("treeList");
            currentTreePath.add("current");
            Map<String, Object> currentTree = (Map<String, Object>) getCertainItemInHashMapRetrieve(userInfo,currentTreePath);
            long currentTime = new Date().getTime();

            if (currentTree == null) {
                Log.e("Current Tree", "null");
                ((TextView)currentView.findViewById(R.id.treeLevel)).setText("Please plant a tree!");
                return;
            } else {
                Log.e("Current Tree", currentTree.toString());
                //get level
                int currentLevel = Integer.parseInt(currentTree.get("Level").toString());
                int maxLevel = Integer.parseInt(currentTree.get("maxLevel").toString());
                String treeName = currentTree.get("name").toString();
                int middleLvel = maxLevel/2;
                int imageLevel = 0;
                if(currentLevel>=0&&currentLevel<middleLvel){
                    imageLevel = 0;
                }
                if(currentLevel>=middleLvel&&currentLevel<maxLevel){
                    imageLevel = 5;
                }
                if(currentLevel>=maxLevel-2){ imageLevel = 10;}

                ((TextView)currentView.findViewById(R.id.treeLevel)).setText(treeName+": Lv. "+currentLevel+"/"+maxLevel);
                //get treeid
                String treeID = currentTree.get("itemUID").toString();

                // Reference to an image file in Cloud Storage
                try {
                    UserDirPath = CacheRoot.getCanonicalPath()
                            + File.separator + "tree"+  File.separator + treeID;
                    File localFile = new File(UserDirPath, String.valueOf(imageLevel));
                    File[] tempFiles = localFile.listFiles();
                    if (localFile.exists()) {
                        Glide.with( currentActivityNow)
                                .load(tempFiles[0]).override( 700, 1300)
                                .into( (ImageView)currentView.findViewById(R.id.treeImage));

                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
                int leveldiff = maxLevel - currentLevel;
                if (leveldiff <= 0) {
                    Map<String, Object> userUpdates = new HashMap<>();
                    userUpdates.put("treeList/current", null);
                    DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd");
                    currentTree.put("date", dateFormat.format(new Date()));
                    currentTree.put("lastUpdate", currentTime);
                    userUpdates.put("treeList/history/" + currentTime, currentTree);
                    addItemInDatabase("score",Integer.parseInt(currentTree.get("price").toString())*2);
                    Toast.makeText(currentActivityNow.getApplicationContext(), "You have harvested the tree and got "+ Integer.parseInt(currentTree.get("price").toString())*2 + " Coins!", Toast.LENGTH_SHORT).show();
                    DatabaseReference userRef = FirebaseDatabase.getInstance().getReference("users").child(getCurrentUserID());
                    userRef.updateChildren(userUpdates);
                }
            }
        }
    }
}
