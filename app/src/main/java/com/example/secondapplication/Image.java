package com.example.secondapplication;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.support.v7.app.AppCompatActivity;
import android.util.Base64;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;

import uk.co.senab.photoview.PhotoView;
import uk.co.senab.photoview.PhotoViewAttacher;

/**
 * Created by q on 2017-07-10.
 */

public class Image extends AppCompatActivity {
    PhotoViewAttacher mAttacher;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.image_click);
        ImageView iv = (ImageView) findViewById(R.id.imageView);
        Button btn = (Button) findViewById(R.id.button);

        Intent intent = getIntent();
        byte[] decodedbyte = intent.getByteArrayExtra("image");
        Bitmap bitmap_hi = BitmapFactory.decodeByteArray(decodedbyte, 0, decodedbyte.length);
        iv.setImageBitmap(bitmap_hi);
        iv.setScaleType(ImageView.ScaleType.FIT_CENTER);
        mAttacher = new PhotoViewAttacher(iv);

        //iv.setImageResource(intent.getIntExtra("name", 0));
        btn.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v){
                finish();
            }
        });
    }

    @Override
    public void onBackPressed(){
        super.onBackPressed();
    }
}
