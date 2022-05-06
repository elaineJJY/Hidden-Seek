package com.example.hiddenseek.navigation_button.friend;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ImageButton;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import com.example.hiddenseek.DialogActivity;
import com.example.hiddenseek.R;
import com.example.hiddenseek.data.FileController;
import com.example.hiddenseek.data.MsgContainer;
import com.example.hiddenseek.view.XListView;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.storage.FileDownloadTask;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.ListResult;
import com.google.firebase.storage.StorageReference;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

public class FriendPage extends Fragment implements XListView.IXListViewListener {
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";
    private MessageBoxAdapter adapter1;
    private XListView listView;
    private ArrayList<MessageBox> messageBoxList = new ArrayList<>();
    private Handler mHandler;

    public FriendPage() {
        // Required empty public constructor
    }

    public static FriendPage newInstance(String param1, String param2) {
        FriendPage fragment = new FriendPage();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    View rootView;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        rootView = inflater.inflate(R.layout.fragment_friendpage, container, false);


        ImageButton button = (ImageButton) rootView.findViewById(R.id.friendListButton);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onButtonClicked(v);
            }
        });

        messageBoxList = MsgContainer.getCommunicatedUsers(getActivity(), FirebaseAuth.getInstance().getCurrentUser().getUid());
        //set list view
        //initMessageboxes();
        listView = rootView.findViewById(R.id.list_view_messageBox);
        adapter1 = new MessageBoxAdapter(getActivity(),
                R.layout.messagebox_list_view_item, messageBoxList);
        listView.setAdapter(adapter1);
        //listView.setPullLoadEnable(true);
        listView.setPullRefreshEnable(true);
        listView.setXListViewListener(FriendPage.this);
        mHandler = new Handler(Looper.myLooper());
        //open message
        listView.setOnItemClickListener((adapterView, view, position, id) -> {
            MessageBox item = adapter1.getItem(position-1);
            Intent intent = new Intent(getActivity(), DialogActivity.class);
            Bundle bundle = new Bundle();
            String targetID = item.getFriend().getUid();
            String targetName = item.getFriend().getName();
            bundle.putString("targetID", targetID);
            bundle.putString("targetName", targetName);
            intent.putExtras(bundle);
            getActivity().startActivityForResult(intent, RC_FINISH_DIALOG);
        });
        listView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view,
                                           final int position, long id) {
                AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                builder.setMessage("delete Dialog?");
                builder.setTitle("Notice");
                MessageBox item = adapter1.getItem(position - 1);
                String targetID = item.getFriend().getUid();
                builder.setPositiveButton("confirm", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if (getActivity() == null || messageBoxList.get(position - 1) == null) {
                        } else if (messageBoxList.remove(position - 1) != null && getActivity() != null) {
                            FileController.deleteFiles(getActivity(), FirebaseAuth.getInstance().getCurrentUser().getUid(), targetID);
                            System.out.println("success");
                        } else {
                            System.out.println("failed");
                        }
                        adapter1.notifyDataSetChanged();
                    }
                });

                builder.setNegativeButton("cancel", new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                    }
                });
                builder.create().show();
                return true;
            }
        });
        
        // Inflate the layout for this fragment
        return rootView;
    }

    private static final int RC_FINISH_DIALOG = 36767;

    public void onButtonClicked(View view) {
        final FragmentTransaction ft = getFragmentManager().beginTransaction();
        ft.replace(R.id.friendFragment, new AddFriendFragment(), "NewFragmentTag");
        ft.commit();

        ft.addToBackStack(null);
    }

    private void onLoad() {
        BottomNavigationView bottomNavigationView = getActivity().findViewById(R.id.nav_menu);
        bottomNavigationView.getMenu().findItem(R.id.navigation_friend).setTitle("Friend");
        bottomNavigationView.getMenu().findItem(R.id.navigation_friend).setIcon(R.drawable.ic_baseline_people_outline_24);
        listView.stopRefresh();
        listView.setRefreshTime("seconds ago");
    }

    @Override
    public void onRefresh() {
        mHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                messageBoxList.clear();
                messageBoxList = MsgContainer.getCommunicatedUsers(getActivity(), FirebaseAuth.getInstance().getCurrentUser().getUid());
                adapter1 = new MessageBoxAdapter(getActivity(), R.layout.messagebox_list_view_item, messageBoxList);
                listView.setAdapter(adapter1);
                adapter1.notifyDataSetChanged();
                onLoad();
            }
        }, 0);
    }

    @Override
    public void onLoadMore() {
        mHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                messageBoxList = MsgContainer.getCommunicatedUsers(getActivity(), FirebaseAuth.getInstance().getCurrentUser().getUid());
                adapter1.notifyDataSetChanged();
                onLoad();
            }
        }, 2000);

    }

    public XListView getListView() {
        return listView;
    }
}
