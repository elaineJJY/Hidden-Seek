package com.example.hiddenseek.navigation_button.profil;

import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.ImageButton;
import android.widget.ListView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.hiddenseek.R;
import com.example.hiddenseek.navigation_button.shop.Gift;
import com.example.hiddenseek.navigation_button.shop.GiftAdapter;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.GenericTypeIndicator;
import com.google.firebase.storage.FileDownloadTask;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.ListResult;
import com.google.firebase.storage.StorageReference;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import static com.example.hiddenseek.data.FirebaseListener.getIconList;

public class IconSelectFragment extends Fragment {

    private ArrayList<Gift> giftList = new ArrayList<Gift>();
    private ImageButton back;
    private File CacheRoot;
    private String UserDirPath;


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_iconpage,container,false);

        GridView listView = view.findViewById(R.id.list_view_icon);
        ListResult listResult=getIconList();
        for (StorageReference item : listResult.getItems()) {
            // item Image
            String itemUID = item.getName();
            giftList.add(new Gift());
            int num = itemUID.indexOf(".");
            String name = itemUID.substring(0,num-1);
            giftList.get(giftList.size()-1).setRid(itemUID);
            giftList.get(giftList.size()-1).setName(name);
            giftList.get(giftList.size()-1).setType("icon");
        }
        GiftAdapter adapter1 = new GiftAdapter(getActivity(),
                R.layout.icon_gridview_item, giftList);
        listView.setAdapter(adapter1);

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                /*FirebaseAuth mAuth = FirebaseAuth.getInstance();
                FirebaseUser user = mAuth.getCurrentUser();
                FirebaseDatabase.getInstance().getReference("users").child(user.getUid()).child("icon").setValue(giftList.get(i).getRid());*/

                String userUID = FirebaseAuth.getInstance().getCurrentUser().getUid();
                String itemUID = giftList.get(i).getRid();

                FirebaseDatabase.getInstance().getReference("users").child(userUID).child("icon").setValue(itemUID);
            }
        });

        back = view.findViewById(R.id.backTreeButtonIcon);
        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getFragmentManager().popBackStack();
            }
        });

        return view;
    }
}
