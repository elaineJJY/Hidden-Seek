package com.example.hiddenseek.navigation_button.map.LangPressEvent;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.DialogFragment;

import com.example.hiddenseek.R;

public class LangPressFragment extends DialogFragment {
    TextView question_view;
    //pass ButtonClickEventHandler to DialogFragment-Host; this interface should be implemented in DialogFragment-Host
    public interface NoticeDialogListener{
        public void onDialogPositiveClick(DialogFragment dialogFragment);
        public void onDialogNegativeClick(DialogFragment dialogFragment);
    }
    NoticeDialogListener noticeDialogListener;

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        try{
            noticeDialogListener = (NoticeDialogListener) getParentFragment();
        } catch (ClassCastException e) {
            throw new ClassCastException(getActivity().toString()+" must implement NoticeDialogListeneer");
        }
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        LayoutInflater inflater = requireActivity().getLayoutInflater();
        View typeView = inflater.inflate(R.layout.langpress_dialogview, null);
        question_view = typeView.findViewById(R.id.questionview);
        builder.setView(typeView)
                .setPositiveButton(R.string.langpress_set, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        noticeDialogListener.onDialogPositiveClick(LangPressFragment.this);
                    }
                }).setNegativeButton(R.string.langpress_cancel, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                noticeDialogListener.onDialogNegativeClick(LangPressFragment.this);
            }
        });
        // Create the AlertDialog object and return it
        return builder.create();
    }
}
