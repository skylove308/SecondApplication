package com.example.secondapplication;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.Image;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.provider.MediaStore;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Base64;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.ExecutionException;

import okhttp3.FormBody;
import okhttp3.HttpUrl;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

/**
 * Created by q on 2017-07-08.
 */

public class ButtonB extends AppCompatActivity {
    final int REQUEST_CAMERA = 1;
    final int REQUEST_TAKE_PHOTO = 2;
    ArrayList<Bitmap> imageArray = new ArrayList<>();
    GalleryAdapter adapter;
    Context mContext = this;

    @Override
    protected void onCreate (Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_button_b);
        Bitmap bitmap1 = BitmapFactory.decodeResource(getResources(), R.drawable.jisoo);
        imageArray.add(bitmap1);

        adapter = new GalleryAdapter(getApplicationContext(), imageArray);
        final Context context_b = getApplicationContext();
        final Activity activity_b = this;

        GridView gv = (GridView) findViewById(R.id.gridview);
        gv.setAdapter(adapter);
        gv.setOnItemClickListener(galleryListener);
        gv.setOnItemLongClickListener(galleryLongListener);

        HttpUrl.Builder urlBuilder = HttpUrl.parse("http://52.78.17.108:3001/api/books/").newBuilder();
        String url = urlBuilder.build().toString();
        GetHandler handler = new GetHandler();
        try{
            handler.execute(url).get();

        }
        catch(Exception e){
            e.printStackTrace();
        }

        Button camera_button = (Button) findViewById(R.id.camera_button);
        camera_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(ContextCompat.checkSelfPermission(activity_b, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED){
                    if(ActivityCompat.shouldShowRequestPermissionRationale(activity_b, Manifest.permission.CAMERA)){

                    }
                    else{
                        ActivityCompat.requestPermissions(activity_b, new String[]{Manifest.permission.CAMERA}, REQUEST_CAMERA);
                    }
                }
                else{
                    Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                    startActivityForResult(cameraIntent, REQUEST_TAKE_PHOTO);
                }
            }
        });
    }

    private AdapterView.OnItemClickListener galleryListener = new AdapterView.OnItemClickListener(){
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long l_position){
            Intent intent = new Intent(getApplicationContext(), com.example.secondapplication.Image.class);
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            Bitmap bitmap = imageArray.get(position);
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, baos);
            byte[] b = baos.toByteArray();
            intent.putExtra("image", b);
            startActivity(intent);
        }
    };

    private AdapterView.OnItemLongClickListener galleryLongListener = new AdapterView.OnItemLongClickListener(){
        @Override
        public boolean onItemLongClick(AdapterView<?> parent, View view, final int position, long l_position){
            AlertDialog.Builder alert = new AlertDialog.Builder(ButtonB.this);
            alert.setTitle("사진 삭제");
            alert.setMessage("삭제하시겠습니까?");
            alert.setPositiveButton("확인", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int which) {
                    ByteArrayOutputStream baos = new ByteArrayOutputStream();
                    Bitmap bitmap = imageArray.get(position);
                    imageArray.remove(bitmap);
                    adapter.notifyDataSetChanged();
                    bitmap.compress(Bitmap.CompressFormat.PNG, 100, baos);
                    byte[] b = baos.toByteArray();
                    String imageEncoded = Base64.encodeToString(b, Base64.DEFAULT);
                    HttpUrl.Builder urlBuilder = HttpUrl.parse("http://52.78.17.108:3001/api/books/delete/title").newBuilder();
                    String url = urlBuilder.build().toString();
                    DeleteHandler handler = new DeleteHandler(imageEncoded);
                    handler.execute(url);

                }
            });
            alert.setNegativeButton("취소", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int which) {
                    dialog.dismiss();
                }
            });
            alert.show();
            /*

            */
            return true;
        }
    };

    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults){
        switch (requestCode){
            case REQUEST_CAMERA:
                if(grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED){
                    Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                    startActivityForResult(cameraIntent, REQUEST_TAKE_PHOTO);
                }
                else{
                    Toast.makeText(getApplicationContext(),"Camera Permission denied",Toast.LENGTH_LONG).show();
                }
                return;
        }
    }

    @Override
    protected  void onActivityResult(int requestCode, int resultCode, Intent data){
        super.onActivityResult(requestCode, resultCode, data);
        if(resultCode == RESULT_OK && data != null){
            Bitmap photo = (Bitmap) data.getExtras().get("data");
            imageArray.add(photo);
            adapter.notifyDataSetChanged();
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            photo.compress(Bitmap.CompressFormat.PNG, 100, baos);
            byte[] b = baos.toByteArray();
            String imageEncoded = Base64.encodeToString(b, Base64.DEFAULT);
            HttpUrl.Builder urlBuilder = HttpUrl.parse("http://52.78.17.108:3001/api/books").newBuilder();
            String url = urlBuilder.build().toString();
            PostHandler handler = new PostHandler(imageEncoded, "photo1");
            String result = null;
            try {
                result = handler.execute(url).get();
            } catch (InterruptedException e) {
                e.printStackTrace();
            } catch (ExecutionException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void onBackPressed(){
        super.onBackPressed();
    }

    public class PostHandler extends AsyncTask<String, Void, String> {
        OkHttpClient client = new OkHttpClient();
        String name, author;
        public PostHandler(String name, String author) {
            this.name = name;
            this.author = author;
        }
        @Override
        protected String doInBackground(String... params) {
            RequestBody formBody = new FormBody.Builder()
                    .add("name", name)
                    .add("author", author)
                    .build();
            Request request = new Request.Builder()
                    .url(params[0]).post(formBody)
                    .build();
            try {
                Response response = client.newCall(request).execute();
                if (!response.isSuccessful())
                    throw new IOException("Unexpected code " + response.toString());
                return response.body().string();
            } catch (Exception e) {}
            return null;
        }
    }

    public class GetHandler extends AsyncTask<String, Void, String> {
        OkHttpClient client = new OkHttpClient();

        public GetHandler() {

        }
        @Override
        protected String doInBackground(String... params) {

            Request request = new Request.Builder()
                    .url(params[0])
                    .build();
            try {
                Response response = client.newCall(request).execute();
                if (!response.isSuccessful())
                    throw new IOException("Unexpected code " + response.toString());
                return response.body().string();
            } catch (Exception e) {}
            return null;
        }

        @Override
        protected void onPostExecute(String str){
            try{
                JSONArray obj = new JSONArray(str);
                for(int i = 0; i < obj.length(); i++){
                    JSONObject jason = obj.getJSONObject(i);
                    String str1 = jason.getString("title");
                    if(str1 != null){
                        byte[] decodedbyte = Base64.decode(str1, 0);
                        Bitmap bitmap_hi = BitmapFactory.decodeByteArray(decodedbyte, 0, decodedbyte.length);
                        imageArray.add(bitmap_hi);
                        adapter.notifyDataSetChanged();
                    }
                }
            }
            catch(Exception e){

            }

        }
    }

    public class GetauthorHandler extends AsyncTask<String, Void, String> {
        OkHttpClient client = new OkHttpClient();
        String author;
        public GetauthorHandler(String author) {
            this.author = author;
        }
        @Override
        protected String doInBackground(String... params) {

            Request request = new Request.Builder()
                    .url(params[0])
                    .build();
            try {
                Response response = client.newCall(request).execute();
                if (!response.isSuccessful())
                    throw new IOException("Unexpected code " + response.toString());
                return response.body().string();
            } catch (Exception e) {}
            return null;
        }
    }

    public class DeleteHandler extends AsyncTask<String, Void, String> {
        OkHttpClient client = new OkHttpClient();
        String title;
        public DeleteHandler(String title) {
            this.title = title;
        }
        @Override
        protected String doInBackground(String... params) {
            RequestBody formBody = new FormBody.Builder()
                    .add("title", title)
                    .build();
            Request request = new Request.Builder()
                    .url(params[0]).post(formBody)
                    .build();
            try {
                Response response = client.newCall(request).execute();
                if (!response.isSuccessful())
                    throw new IOException("Unexpected code " + response.toString());
                return response.body().string();
            } catch (Exception e) {}
            return null;
        }
    }
}


class GalleryAdapter extends BaseAdapter{
    private Context mContext;
    private ArrayList<Bitmap> imageShow = new ArrayList<>();
    public GalleryAdapter(Context context, ArrayList<Bitmap> list){
        mContext = context;
        imageShow = list;
    }
    public View getView(int position, View convertView, ViewGroup parent){
        View gridView;
        if (convertView == null){
            LayoutInflater inflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = inflater.inflate(R.layout.image_view, parent, false);
        }
        Bitmap bitmap = imageShow.get(position);
        if(bitmap != null){
            new GetView(convertView).execute(position);
        }
        return convertView;
    }
    public int getCount() {
        return imageShow.size();
    }
    @Override
    public Object getItem(int position){
        return imageShow.get(position);
    }
    @Override
    public long getItemId(int position){
        return position;
    }

    public class GetView extends AsyncTask<Integer, Void, Bitmap> {
        View convertView;
        public GetView(View convertView) {
            this.convertView = convertView;
        }
        @Override
        protected Bitmap doInBackground(Integer ...positions) {
            /*
            Bitmap bitmap = imageShow.get(positions[0]);
            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inSampleSize = 4;
            Bitmap resized = Bitmap.createScaledBitmap(bitmap, 90, 100, true);
            return resized;
            */
            return imageShow.get(positions[0]);
        }
        @Override
            protected void onPostExecute(Bitmap resized){
            ImageView imageView = (ImageView) convertView.findViewById(R.id.imageView);
            imageView.setImageBitmap(resized);
        }
    }

}
