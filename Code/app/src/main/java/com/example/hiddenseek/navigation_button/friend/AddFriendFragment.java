package com.example.hiddenseek.navigation_button.friend;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.AdapterView;

import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.hiddenseek.DialogActivity;
import com.example.hiddenseek.R;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.GenericTypeIndicator;

import java.util.ArrayList;
import java.util.Map;

public class AddFriendFragment  extends Fragment{
    private View rootView;
    private ArrayList<Friend> friendList = new ArrayList<>();
    private EditText searchText;

    private ListView listView;
    private FriendAdapter friendListAdapter;
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.fragment_add_list, container, false);

        //back button
        ImageButton button = (ImageButton) rootView.findViewById(R.id.backFlistButton);
        button.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                onButtonClicked(v);
            }
        });

        //set list view
        listView = rootView.findViewById(R.id.list_view_add_friend_list);

        friendListAdapter = new FriendAdapter(getActivity(),
                R.layout.add_friend_list_item, friendList);
        listView.setAdapter(friendListAdapter);
        AdapterView.OnItemClickListener clickListener= new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Friend item = friendListAdapter.getItem(position);
                Intent intent = new Intent(getActivity(), DialogActivity.class);
                Bundle bundle = new Bundle();
                String targetID=item.getUid();
                bundle.putString("targetID",targetID);
                intent.putExtras(bundle);

            }
        };
        listView.setOnItemClickListener(clickListener);



        //search Function
        searchText =(EditText) rootView.findViewById(R.id.friendSearchText);

        searchText.setOnEditorActionListener(new EditText.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_DONE) {
                    // get 检索
                    for(int i = 0; i < friendList.size(); i++){
                        friendList.remove(i);
                    }
                    FriendAdapter friendListAdapter = new FriendAdapter(getActivity(),
                            R.layout.add_friend_list_item, friendList);
                    listView.setAdapter(friendListAdapter);
                    String content = searchText.getText().toString();
                    if(content.equals("")){
                        Toast.makeText(getContext(), "Please input username!",
                                Toast.LENGTH_LONG).show();
                        return true;
                    }


                    FirebaseDatabase.getInstance().getReference().child("users").orderByChild("username").startAt(content).endAt(content+"\uf8ff").get().addOnCompleteListener(new OnCompleteListener<DataSnapshot>() {
                        @Override
                        public void onComplete(@NonNull Task<DataSnapshot> task) {
                            if (task.isSuccessful()) {
                                GenericTypeIndicator<Map<String, Map<String, Object>>> t = new GenericTypeIndicator<Map<String,Map<String, Object>>>() {};
                                Map<String,Map<String, Object>> findUsers = task.getResult().getValue(t);
                                friendList.clear();
                                if (!(findUsers ==null))
                                    for(Map.Entry<String,Map<String, Object>> user: findUsers.entrySet()){
                                        String uid = user.getKey();
                                        Map<String, Object> friendmap = user.getValue();
                                        String uname = friendmap.get("username").toString();
                                        friendList.add(new Friend(uname,uid));
                                        friendListAdapter.notifyDataSetChanged();

                                    }
                            }
                        }
                    });
                    return true;
                }
                return false;
            }
        });
        // Inflate the layout for this fragment
        return rootView;
    }

    public void onButtonClicked(View view){
        getFragmentManager().popBackStack();
    }
}
