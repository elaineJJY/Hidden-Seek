package com.example.hiddenseek.navigation_button.profil;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ListView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.hiddenseek.R;
import com.example.hiddenseek.navigation_button.shop.Gift;
import com.example.hiddenseek.navigation_button.shop.GiftAdapter;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.GenericTypeIndicator;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.SortedSet;
import java.util.TreeSet;

import static com.example.hiddenseek.data.FirebaseListener.getUserInfo;
import static com.example.hiddenseek.data.Helper.getCertainItemInHashMapRetrieve;

public class HonourFragment extends Fragment {

    private ArrayList<Gift> giftList = new ArrayList<Gift>();
    private ImageButton back;


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_honourpage,container,false);

        ListView listView = view.findViewById(R.id.list_view_tree);
        Map<String,Object> userInfo=getUserInfo();
        ArrayList<String> currentTreePath = new ArrayList<>();
        currentTreePath.add("treeList");
        currentTreePath.add("history");
        Map<String, Object> history=(Map<String, Object>)getCertainItemInHashMapRetrieve(userInfo, currentTreePath);
        if (history!= null) {
            SortedSet<String> keys = new TreeSet<>(history.keySet());
            for (String treeKeys : keys) {
                Map<String,Object> tree = (Map<String, Object>) history.get(treeKeys);
                String date = tree.get("date").toString();
                String name = tree.get("name").toString();
                String treeID = tree.get("itemUID").toString();
                Gift histree=new Gift();
                histree.setName(name);
                histree.setRid(treeID);
                histree.setType("histree");
                histree.setAmount(date);
                giftList.add(histree);
            }
            GiftAdapter adapter1 = new GiftAdapter(getActivity(),
                    R.layout.bag_list_view_item, giftList);
            listView.setAdapter(adapter1);
        }

        back = view.findViewById(R.id.backTreeButton);
        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getFragmentManager().popBackStack();
            }
        });

        return view;
    }
}
