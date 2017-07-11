package com.example.secondapplication;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONObject;

/**
 * Created by q on 2017-07-11.
 */

public class voiceResultDialog extends Dialog {
    private Context mContext;
    private JSONArray mJsonArray;
    private int mAttempt;
    private String mTarget;
    public voiceResultDialog(Context context, JSONArray jsonArray, int totalAttempt, String target){
        super(context);
        mContext = context;
        mJsonArray = jsonArray;
        mAttempt = totalAttempt;
        mTarget = target;
        System.out.println(mJsonArray);
    }
    void setValues(int pos, TextView leftView, TextView textView, TextView countView){
        try {
            JSONObject jsonObj = mJsonArray.getJSONObject(pos);
            String resName = jsonObj.getString("result");
            int resCount = jsonObj.getInt("count");
            if(resName.compareTo(mTarget.trim().toLowerCase()) == 0){
                resName += " (Answer)";
                leftView.setTextColor(mContext.getResources().getColor(R.color.colorAccent));
                textView.setTextColor(mContext.getResources().getColor(R.color.colorAccent));
                countView.setTextColor(mContext.getResources().getColor(R.color.colorAccent));
            }
            textView.setText(resName);
            countView.setText(String.valueOf(resCount));
        }catch(Exception e){
            e.printStackTrace();
        }
    }
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        WindowManager.LayoutParams lpWindow = new WindowManager.LayoutParams();
        lpWindow.flags = WindowManager.LayoutParams.FLAG_DIM_BEHIND;
        lpWindow.dimAmount = 0.8f;
        getWindow().setAttributes(lpWindow);
        setContentView(R.layout.dialog_voiceresult);
        WindowManager.LayoutParams params = getWindow().getAttributes();
        params.width = WindowManager.LayoutParams.MATCH_PARENT;
        params.height = WindowManager.LayoutParams.WRAP_CONTENT;
        getWindow().setAttributes(params);
        ((Button) findViewById(R.id.showStatDialog)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss();
            }
        });
        ((TextView) findViewById(R.id.titleDialogText)).setText("'"+mTarget+"' 단어 통계 (" + mAttempt + "명)");
        int len = mJsonArray.length();
        if(len <= 2) ((LinearLayout) findViewById(R.id.thirdLayout)).setVisibility(View.GONE);
        if(len <= 1) ((LinearLayout) findViewById(R.id.secondLayout)).setVisibility(View.GONE);
        if(len <= 0) ((LinearLayout) findViewById(R.id.firstLayout)).setVisibility(View.GONE);
        if(len >= 1) setValues(0, ((TextView) findViewById(R.id.firstLeft)), ((TextView) findViewById(R.id.firstText)), ((TextView) findViewById(R.id.firstCount)));
        if(len >= 2) setValues(1, ((TextView) findViewById(R.id.secondLeft)), ((TextView) findViewById(R.id.secondText)), ((TextView) findViewById(R.id.secondCount)));
        if(len >= 3) setValues(2, ((TextView) findViewById(R.id.thirdLeft)), ((TextView) findViewById(R.id.thirdText)), ((TextView) findViewById(R.id.thirdCount)));
    }
}
