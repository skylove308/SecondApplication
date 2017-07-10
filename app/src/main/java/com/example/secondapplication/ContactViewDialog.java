package com.example.secondapplication;

import android.Manifest;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import static android.view.View.GONE;

/**
 * Created by q on 2017-07-09.
 */

public class ContactViewDialog extends Dialog {
    private ContactInfo mContent;
    private String phoneNum;
    private Context mContext;
    private Button mEditButton, mDeleteButton, mCloseButton;
    //private View.OnClickListener edit, delete, close;
    public String getPhoneNumber(){
        return phoneNum;
    }
    public ContactViewDialog(Context context, ContactInfo content){
        super(context);
        mContext = context;
        mContent = content;
        //edit = editListener; delete = deleteListener; close = closeListener;
    }
    public void setEditListener(View.OnClickListener editListener){
        mEditButton.setOnClickListener(editListener);
    }
    public void setDeleteListener(View.OnClickListener deleteListener){
        mDeleteButton.setOnClickListener(deleteListener);
    }
    private void doCall(){
        System.out.println("!!!");
        Intent intent = new Intent("android.intent.action.DIAL");
        intent.setData(Uri.parse("tel:" + phoneNum));
        mContext.startActivity(intent);
    }
    private void tryCall(){
        doCall();
    }
    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        WindowManager.LayoutParams lpWindow = new WindowManager.LayoutParams();
        lpWindow.flags = WindowManager.LayoutParams.FLAG_DIM_BEHIND;
        lpWindow.dimAmount = 0.8f;
        getWindow().setAttributes(lpWindow);
        setContentView(R.layout.show_contact);
        final String[] placeArray = {"", "Facebook ", "주소록 ", "커스텀 "};
        int place = mContent.getMode();
        ((TextView) findViewById(R.id.showName)).setText(mContent.getName());
        ((TextView) findViewById(R.id.showWhere)).setText(placeArray[place] + "연락처");
        ((TextView) findViewById(R.id.showPhone)).setText(mContent.getPhone());
        ((TextView) findViewById(R.id.showEmail)).setText(mContent.getEmail());
        if(mContent.getThumb() != null) {
            ((ImageView) findViewById(R.id.contactShowImage)).setImageBitmap(mContent.getThumb());
        }else{
            ((ImageView) findViewById(R.id.contactShowImage)).setImageResource(R.drawable.ic_account_circle_green_24dp);
        }
        ((LinearLayout) findViewById(R.id.showPhoneLayout)).setVisibility(View.VISIBLE);
        ((LinearLayout) findViewById(R.id.showEmailLayout)).setVisibility(View.VISIBLE);
        if(place == 1){
            ((LinearLayout) findViewById(R.id.showPhoneLayout)).setVisibility(View.GONE);
            ((LinearLayout) findViewById(R.id.showEmailLayout)).setVisibility(View.GONE);
        }
        if(place == 2){
            ((LinearLayout) findViewById(R.id.showEmailLayout)).setVisibility(View.GONE);
        }
        mEditButton = (Button) findViewById(R.id.contactButtonEdit);
        mDeleteButton = (Button) findViewById(R.id.contactButtonDelete);
        mCloseButton = (Button) findViewById(R.id.contactButtonClose);
        Button callButton = (Button) findViewById(R.id.contactCallButton);
        mCloseButton.setVisibility(View.VISIBLE);
        if(place == 1 || place == 2){
            mEditButton.setVisibility(View.INVISIBLE);
            mDeleteButton.setVisibility(View.INVISIBLE);
        }else{
            mEditButton.setVisibility(View.VISIBLE);
            mDeleteButton.setVisibility(View.VISIBLE);
        }
        /*
        mEditButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(getContext(), "편집", Toast.LENGTH_SHORT).show();
                dismiss();
            }
        });
        mDeleteButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(getContext(), "삭제", Toast.LENGTH_SHORT).show();
                dismiss();
            }
        });
        */
        mCloseButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(getContext(), "닫기", Toast.LENGTH_SHORT).show();
                dismiss();
            }
        });
        callButton.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v){
                phoneNum = mContent.getPhone();
                tryCall();
            }
        });
    }
}
