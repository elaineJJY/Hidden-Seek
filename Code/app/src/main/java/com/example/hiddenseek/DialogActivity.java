package com.example.hiddenseek;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.os.Bundle;

import android.os.Handler;
import android.os.Looper;
import android.os.Message;

import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.hiddenseek.data.Msg;
import com.example.hiddenseek.MsgAdapter.MsgAdapter;
import com.example.hiddenseek.data.MsgContainer;
import com.example.hiddenseek.data.FirebaseListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.FirebaseDatabase;


import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;


import static com.example.hiddenseek.data.Helper.sendMsg;
import static com.example.hiddenseek.data.MsgContainer.writeLastMsg;


public class DialogActivity extends AppCompatActivity {
    private List<Msg> msgList = new ArrayList<>();
    private EditText inputText;
    private Button send;
    private ImageButton back;
    private RecyclerView msgRecyclerView;
    private MsgAdapter adapter;
    private String currentUserID;
    private String currentUserName;
    private FirebaseAuth mAuth;
    private TextView userNameText;
    private LinearLayout layout_edit;


    Handler handler = new Handler(Looper.myLooper()){
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what){
                case 0:
                    msgRecyclerView.scrollToPosition(msgList.size()-1);
                    break;
            }
        }

    };
    private String receiverUID;

    @SuppressLint("ClickableViewAccessibility")
    @Override
    protected void onCreate(Bundle savesInstanceState) {
        super.onCreate(savesInstanceState);
        setContentView(R.layout.activity_dialog);
        FirebaseListener.setcurrentActivityNow(this);
        //get currentUserID/receiverUID
        Bundle initInformation=getIntent().getExtras();
        receiverUID = initInformation.getString("targetID");
        currentUserID = FirebaseAuth.getInstance().getUid();
        currentUserName = initInformation.getString("targetName");

        //access local cached message and display
        MsgContainer msgContainer = new MsgContainer();
        List<Msg> msgs = msgContainer.readJson(this, currentUserID, receiverUID);
        Log.e("DialogActivity",msgs.toString());
        msgList = msgs;
        back = (ImageButton) findViewById(R.id.DialogtoFPage_button);
        inputText = (EditText) findViewById(R.id.input_text);
        layout_edit = (LinearLayout) findViewById(R.id.layout_edit);
        msgRecyclerView = (RecyclerView) findViewById(R.id.msg_recycler_view);
        userNameText = (TextView) findViewById(R.id.textView_username);
        userNameText.setText(currentUserName);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        msgRecyclerView.setLayoutManager(linearLayoutManager);
        adapter = new MsgAdapter(msgList);
        msgRecyclerView.setAdapter(adapter);
        msgRecyclerView.scrollToPosition(msgList.size() - 1);

        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                hideSoftInput(DialogActivity.this, msgRecyclerView);
                finish();
            }
        });

        layout_edit.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view){
                inputText.requestFocus();
                showSoftInput(DialogActivity.this, inputText);
                handler.sendEmptyMessageDelayed(0,250);
            }
        });

        msgRecyclerView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                hideSoftInput(DialogActivity.this, inputText);
                return false;
            }
        });

    }
    @Override
    protected void onStart() {
        super.onStart();
    }

    public String getReceiverUID() {
        return receiverUID;
    }

    public void toDb(View v) {
        if (v.getId() == R.id.messageSend) {

            String content = inputText.getText().toString();
            if (!"".equals(content)) {
                // send Message (Firebase)
                Msg msg=sendMsg (this,content,receiverUID,currentUserID);
                setMsg(msg);
                notifyData();
                inputText.setText("");
                //send to the database, for the opposite user to receive that message

            }
        }
    }

    public MsgAdapter getMsgAdapter() {
        return adapter;
    }

    public List<Msg> getMsgList() {
        return msgList;
    }

    public RecyclerView getMsgRecyclerView() {
        return msgRecyclerView;
    }

    public void setMsg(Msg msg) {
        msgList.add(msg);
    }

    public void notifyData(){
        adapter.notifyDataSetChanged();
        msgRecyclerView.scrollToPosition(msgList.size() - 1);
    }
    public static void showSoftInput(Context context, View view) {
        InputMethodManager imm = (InputMethodManager) context.getSystemService(Activity.INPUT_METHOD_SERVICE);
        imm.showSoftInput(view, InputMethodManager.SHOW_FORCED);
    }
    public static void hideSoftInput(Context context, View view) {
        InputMethodManager imm = (InputMethodManager) context.getSystemService(Activity.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(view.getWindowToken(), 0);

    }
}