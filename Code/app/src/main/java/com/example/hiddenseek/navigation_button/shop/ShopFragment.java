package com.example.hiddenseek.navigation_button.shop;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.GridView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.viewpager.widget.ViewPager;


import com.example.hiddenseek.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.GenericTypeIndicator;


import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.io.File;

import static com.example.hiddenseek.data.FirebaseListener.getShopTreeList;
import static com.example.hiddenseek.data.FirebaseListener.getUserInfo;
import static com.example.hiddenseek.data.Helper.addItemInDatabase;

public class ShopFragment extends Fragment {
    private TextView textView;
    private int coin;
    //item项
    private ArrayList<Gift> giftList1 = new ArrayList<>();
    private ArrayList<Gift> giftList2 = new ArrayList<>();
    private Button bagbutton;
    private ViewPager vpager_one;
    private ArrayList<View> aList;
    private ShopPagerAdapter mAdapter;
    //cache root
    private static File CacheRoot;
    private String UserDirPath;



    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        //View view= this.getLayoutInflater().inflate((R.layout.fragment_shop), null);
        View view = inflater.inflate(R.layout.fragment_shop,container,false);
        textView = view.findViewById(R.id.cointext);
        //set pages
        vpager_one = (ViewPager) view.findViewById(R.id.vpager_one);

        aList = new ArrayList<View>();
        //git all three viewpagers
        View grid_view1 = inflater.inflate(R.layout.shop_page_view_one,null,false);
        View grid_view2 = inflater.inflate(R.layout.shop_page_view_two,null,false);

        aList.add(grid_view1);
        aList.add(grid_view2);
        mAdapter = new ShopPagerAdapter(aList);
        vpager_one.setAdapter(mAdapter);

        GridView gridView1 = grid_view1.findViewById(R.id.grid_view_shop1);
        GridView gridView2 = grid_view2.findViewById(R.id.grid_view_shop2);

        giftList1=getShopTreeList();
        GiftAdapter adapter1 = new GiftAdapter(getActivity(), R.layout.shop_grid_view_item, giftList1);
        gridView1.setAdapter(adapter1);


        //set gifts
        //set score
        Map<String, Object> userInfo = getUserInfo();
        coin = Integer.parseInt(userInfo.get("score").toString());
        textView.setText(""+coin);

        //buy items
        gridView1.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                int price = giftList1.get(i).getPrice();

                String userUID = FirebaseAuth.getInstance().getCurrentUser().getUid();
                String catalog = "tree";
                String itemUID = giftList1.get(i).getRid();
                Log.e("TAG00", String.valueOf(i));

                DatabaseReference userRef = FirebaseDatabase.getInstance().getReference().child("users").child(userUID);
                DatabaseReference itemRef = FirebaseDatabase.getInstance().getReference().child("shop").child(catalog).child(itemUID);

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
                            String treeName = itemInfo.get("name").toString();
                            if (coin >= price) {
                                coin = coin - price;
                                userRef.child("bag").child(catalog).child(itemUID).child("number").get().addOnCompleteListener(new OnCompleteListener<DataSnapshot>() {
                                    @Override
                                    public void onComplete(@NonNull Task<DataSnapshot> task) {
                                        if (!task.isSuccessful()) {
                                            Log.e("firebase", "Error getting data", task.getException());
                                        } else {
                                            Map<String, Object> userUpdates = new HashMap<>();
                                            userUpdates.put("score", coin);
                                            itemInfo.put("number", 1);
                                            if (task.getResult().exists()) {
                                                itemInfo.put("number", task.getResult().getValue(Integer.class) + 1);
                                            }
                                            userUpdates.put("bag/tree/" + itemUID, itemInfo);
                                            userRef.updateChildren(userUpdates);
                                        }
                                    }
                                });
                                textView.setText("" + coin);
                                Toast.makeText(getActivity().getApplicationContext(), "You have bought the "+ treeName+" seed!", Toast.LENGTH_SHORT).show();
                            } else {
                                Toast.makeText(getActivity().getApplicationContext(), "Coin not enough!", Toast.LENGTH_SHORT).show();
                                Log.e("Buy", "Score Not Enough");
                            }
                        }
                    }
                });
            }
        });

        Gift f = new Gift();
        f.setName("fertilizer");
        f.setPrice(1000);
        f.setType("fertilizer");
        giftList2.add(f);

        GiftAdapter adapter2 = new GiftAdapter(getActivity(),
                R.layout.shop_grid_view_item, giftList2);
        gridView2.setAdapter(adapter2);
        gridView2.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                Map<String, Object> userInfo = getUserInfo();
                if ((long)userInfo.get("score")>=1000) {
                    addItemInDatabase("score", -1000);
                    addItemInDatabase("fertilizer", 1);
                    Toast.makeText(getActivity().getApplicationContext(), "You have bought a fertilizer!", Toast.LENGTH_SHORT).show();
                }else{
                    Toast.makeText(getActivity().getApplicationContext(), "Coin not enough!", Toast.LENGTH_SHORT).show();
                }
                textView.setText("" + userInfo.get("score").toString());

            }

        });

        return view;
    }

}
