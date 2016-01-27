package com.videonasocialmedia.kamarada.presentation.views.dialog;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.videonasocialmedia.kamarada.R;
import com.videonasocialmedia.kamarada.presentation.listener.OnCamaradaDialogClickListener;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class KamaradaDialogFragment extends DialogFragment {

    OnCamaradaDialogClickListener clickListener;
    @Bind(R.id.acceptDialog)
    Button acceptDialog;
    @Bind(R.id.cancelDialog)
    Button cancelDialog;
    @Bind(R.id.titleDialog)
    TextView titleDialog;
    @Bind(R.id.messageDialog )
    TextView messageDialog;
    private int idDialog;


    public KamaradaDialogFragment(){
        // Empty constructor required for DialogFragment
    }

    public static KamaradaDialogFragment newInstance(String title, String message, String accept,
                                                    String cancel, int idDialog) {
        KamaradaDialogFragment frag = new KamaradaDialogFragment();
        Bundle args = new Bundle();
        args.putString("title", title);
        args.putString("message", message);
        args.putString("accept", accept);
        args.putString("cancel", cancel);
        args.putInt("idDialog", idDialog);
        frag.setArguments(args);
        return frag;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        clickListener = (OnCamaradaDialogClickListener) getTargetFragment();
    }


    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {


        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity(), R.style.CamaradaDialog);
        LayoutInflater inflater = getActivity().getLayoutInflater();
        View v = inflater.inflate(R.layout.dialog_videona, null);
        builder.setView(v);

        ButterKnife.bind(this, v);

        setTitleDialog(getArguments().getString("title"));
        setMessageDialog(getArguments().getString("message"));
        setAcceptDialog(getArguments().getString("accept"));
        setCancelDialog(getArguments().getString("cancel"));

        idDialog = getArguments().getInt("idDialog");

        return builder.create();
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);

        try {
            clickListener = (OnCamaradaDialogClickListener) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString() + " must implement " +
                    "OnVideonaDialogButtonsListener");
        }
    }


    public void dismissDialog(){
        this.dismiss();
    }

    private void setTitleDialog(String restTitle){
        titleDialog.setText(restTitle);
    }

    private void setMessageDialog(String resMessage){
        messageDialog.setText(resMessage);
    }

    private void setAcceptDialog(String resAccept){
        acceptDialog.setText(resAccept);
    }

    public void hideAcceptDialog(){
        acceptDialog.setVisibility(View.GONE);
    }

    private void setCancelDialog(String resCancel){
        cancelDialog.setText(resCancel);
    }

    public void hideCancelDialog(){
        acceptDialog.setVisibility(View.GONE);
    }


    @OnClick(R.id.acceptDialog)
    public void onClickAcceptDialog(){
        clickListener.onClickAcceptDialogListener(idDialog);
    }

    @OnClick(R.id.cancelDialog)
    public void onClickCancelDialog(){
        clickListener.onClickCancelDialogListener(idDialog);
    }


}
