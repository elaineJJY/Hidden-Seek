package com.example.hiddenseek.navigation_button.shop;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.hiddenseek.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.GenericTypeIndicator;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import static com.example.hiddenseek.data.FirebaseListener.getUserInfo;
import static com.example.hiddenseek.data.Helper.getCertainItemInHashMapRetrieve;

public class BagFragment extends Fragment {

    private ArrayList<Gift> giftList = new ArrayList<Gift>();
    private ImageButton backShop;
    private String userUID = FirebaseAuth.getInstance().getCurrentUser().getUid();

    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_bag,container,false);

        ListView listView = view.findViewById(R.id.list_view_bag);
        Map<String, Object> bagInfo = (Map<String, Object>) getUserInfo().get("bag");
        if (bagInfo!=null)
            bagInfo=(Map<String, Object>) bagInfo.get("tree");
        if (bagInfo == null) {
            Log.e("Bag", "Bag is empty");
        } else {
            for (Map.Entry<String, Object> entry : bagInfo.entrySet()) {
                String itemUID = entry.getKey();
                Map<String,Object> itemInfo = (Map<String, Object>) entry.getValue();
                Log.e("item",itemInfo.toString());
                String number = itemInfo.get("number").toString();
                String name = itemInfo.get("name").toString();
                Gift tempGift=new Gift();
                tempGift.setName(name);
                tempGift.setRid(itemUID);
                tempGift.setType("bag");
                tempGift.setAmount(number);
                giftList.add(tempGift);
            }
            GiftAdapter adapter1 = new GiftAdapter(getActivity(),
                    R.layout.bag_list_view_item, giftList);
            listView.setAdapter(adapter1);
        }

        listView.setOnItemClickListener(
                new AdapterView.OnItemClickListener() {
                    @Override
                    public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                        String itemUID = giftList.get(i).getRid();
                        Map<String, Object> userInfo=getUserInfo();
                        ArrayList<String> currentTreePath = new ArrayList<>();
                        currentTreePath.add("treeList");
                        currentTreePath.add("current");
                        if (getCertainItemInHashMapRetrieve(userInfo, currentTreePath) == null) {

                            DatabaseReference treesInBag = FirebaseDatabase.getInstance().getReference().child("users").child(userUID).child("bag").child("tree").child(itemUID);
                            currentTreePath = new ArrayList<>();
                            currentTreePath.add("bag");
                            currentTreePath.add("tree");
                            currentTreePath.add(itemUID);
                            currentTreePath.add("number");
                            long newNumber = ((long)getCertainItemInHashMapRetrieve(userInfo, currentTreePath))-1;
                            if (newNumber == 0) {
                                // delete the item from the bag
                                treesInBag.removeValue();
                            } else {
                                treesInBag.child("number").setValue(newNumber);
                            }
                                // information of the tree
                                FirebaseDatabase.getInstance().getReference("shop").child("tree").child(itemUID).get().addOnCompleteListener(new OnCompleteListener<DataSnapshot>() {
                                    @Override
                                    public void onComplete(@NonNull Task<DataSnapshot> task) {
                                        if (!task.isSuccessful()) {
                                            Log.e("firebase", "Error getting data", task.getException());
                                        } else {

                                            // All the informations of this item
                                            GenericTypeIndicator<Map<String, Object>> t = new GenericTypeIndicator<Map<String, Object>>() {
                                            };
                                            Map<String, Object> info = task.getResult().getValue(t);

                                            info.put("Level", "0");
                                            info.put("itemUID", itemUID);
                                            info.put("lastUpdate", String.valueOf(new Date().getTime()));
                                            FirebaseDatabase.getInstance().getReference().child("users").child(userUID)
                                                    .child("treeList").child("current").setValue(info);
                                            Toast.makeText(getActivity().getApplicationContext(),"You have planted "+ itemUID +" .",Toast.LENGTH_SHORT).show();
                                        }
                                    }
                                });


                        }else{
                            Toast.makeText(getActivity().getApplicationContext(),"You are now planting a tree!",Toast.LENGTH_SHORT).show();
                        }
                        getFragmentManager().popBackStack();
                    }
        });

        backShop = view.findViewById(R.id.backShopButton);
        backShop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getFragmentManager().popBackStack();
            }
        });

        return view;
    }
}
