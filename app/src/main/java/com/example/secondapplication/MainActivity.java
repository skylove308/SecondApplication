package com.example.secondapplication;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import okhttp3.OkHttpClient;

public class MainActivity extends AppCompatActivity {
    OkHttpClient client = new OkHttpClient();
    EditText input1, input2;
    TextView textview;
    String imageEncoded;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Button buttona = (Button) findViewById(R.id.buttona);
        buttona.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), ContactActivity.class);
                startActivity(intent);
            }
        });
        Button buttonb = (Button) findViewById(R.id.buttonb);
        buttonb.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), ButtonB.class);
                startActivity(intent);
            }
        });

        Button buttonc = (Button) findViewById(R.id.buttonc);
        buttonc.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), ButtonD.class);
                startActivity(intent);
            }
        });
    }
}

