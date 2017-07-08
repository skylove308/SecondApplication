package com.example.secondapplication;

import android.Manifest;
import android.content.ComponentName;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.Bitmap;
import android.media.MediaScannerConnection;
import android.media.ThumbnailUtils;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Environment;
import android.os.storage.StorageManager;
import android.os.storage.StorageVolume;
import android.provider.MediaStore;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.List;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class ContactAddActivity extends AppCompatActivity {
    private Button addButton;
    private ImageView imgView;
    private String uid;
    private OkHttpClient client;
    private Bitmap newProfilePic = null;
    private Uri uri;
    private final MediaType JSON = MediaType.parse("application/json; charset=utf-8");

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults){
        switch(requestCode){
            case 42:
                if(grantResults[0] == PackageManager.PERMISSION_GRANTED || grantResults[1] == PackageManager.PERMISSION_GRANTED || grantResults[2] == PackageManager.PERMISSION_GRANTED){
                    takePhoto();
                }
                break;
        }
    }
    void takePhoto(){
        Toast.makeText(ContactAddActivity.this, "TakePhoto", Toast.LENGTH_SHORT).show();
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        try {
            String url = "tmp_" + String.valueOf(System.currentTimeMillis());
            File storeDir = new File(Environment.getExternalStorageDirectory(), "/testImage/");
            if (!storeDir.exists()) storeDir.mkdirs();
            File newFile = File.createTempFile(url, ".jpg", storeDir);
            System.out.println(newFile);
            uri = FileProvider.getUriForFile(ContactAddActivity.this, "com.example.secondapplication", newFile);
            intent.putExtra(android.provider.MediaStore.EXTRA_OUTPUT, uri);
            startActivityForResult(intent, 21);
            System.out.println("!");
        }catch(Exception e){
            e.printStackTrace();
        }
    }
    void tryTakePhoto(){
        if(ContextCompat.checkSelfPermission(ContactAddActivity.this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED
                || ContextCompat.checkSelfPermission(ContactAddActivity.this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED
                || ContextCompat.checkSelfPermission(ContactAddActivity.this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED){
            ActivityCompat.requestPermissions(ContactAddActivity.this, new String[]{Manifest.permission.CAMERA, Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE}, 42);
        }else{
            takePhoto();
        }
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_contact_add);
        this.setTitle("커스텀 연락처 추가");
        Intent intent = getIntent();
        uid = intent.getStringExtra("userId");
        System.out.println(uid);
        imgView = (ImageView) findViewById(R.id.addContactImage);
        imgView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //Toast.makeText(ContactAddActivity.this, "test", Toast.LENGTH_SHORT).show();
                DialogInterface.OnClickListener cameraListener = new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        tryTakePhoto();
                    }
                };
                DialogInterface.OnClickListener albumListener = new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Toast.makeText(ContactAddActivity.this, "GetPhoto", Toast.LENGTH_SHORT).show();
                        Intent intent = new Intent(Intent.ACTION_PICK);
                        intent.setType(MediaStore.Images.Media.CONTENT_TYPE);
                        startActivityForResult(intent, 22);
                    }
                };
                DialogInterface.OnClickListener cancelListener = new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Toast.makeText(ContactAddActivity.this, "Cancel", Toast.LENGTH_SHORT).show();
                        dialog.dismiss();
                    }
                };
                new AlertDialog.Builder(v.getContext())
                        .setTitle("업로드 이미지 선택")
                        .setPositiveButton("사진 촬영", cameraListener)
                        .setNegativeButton("앨범 선택", albumListener)
                        .show();
            }
        });
        addButton = (Button) findViewById(R.id.addContactButton);
        addButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendInformation("jihoon", "email", "phone", "http://www.naver.com");
            }
        });
    }

    String makeJson(String name, String email, String phone, String imgUrl){
        return "{\"name\" : \"" + name + "\"," +
                "\"email\" : \"" + email + "\"," +
                "\"phone\" : \"" + phone + "\"," +
                "\"imgUrl\" : \"" + imgUrl + "\"}";
    }
    String sendPost(String url, String jsonStr){
        RequestBody body = RequestBody.create(JSON, jsonStr);
        Request request =
                new Request.Builder()
                        .url(url)
                        .post(body)
                        .build();
        try{
            Response response = client.newCall(request).execute();
            return response.body().string();
        }catch(Exception e){
            e.printStackTrace();
            return "";
        }
    }
    String sendInformation(String name, String email, String phone, String imgUrl){
        String jsonStr = makeJson(name, email, phone, imgUrl);
        System.out.println(jsonStr);
        String response = sendPost("http://52.78.17.108:50045/contact/add/"+uid+"/custom", jsonStr);
        System.out.println(response);
        return response;
    }
    void cropImage() {
        this.grantUriPermission("com.android.camera", uri,
                Intent.FLAG_GRANT_WRITE_URI_PERMISSION | Intent.FLAG_GRANT_READ_URI_PERMISSION);
        Intent intent = new Intent("com.android.camera.action.CROP");
        intent.setDataAndType(uri, "image/*");

        List<ResolveInfo> list = getPackageManager().queryIntentActivities(intent, 0);
        grantUriPermission(list.get(0).activityInfo.packageName, uri,
                Intent.FLAG_GRANT_WRITE_URI_PERMISSION | Intent.FLAG_GRANT_READ_URI_PERMISSION);
        int size = list.size();
        if (size == 0) {
            Toast.makeText(this, "취소 되었습니다.", Toast.LENGTH_SHORT).show();
            return;
        } else {
            Toast.makeText(this, "용량이 큰 사진의 경우 시간이 오래 걸릴 수 있습니다.", Toast.LENGTH_SHORT).show();
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            intent.addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
            intent.putExtra("crop", "true");
            intent.putExtra("aspectX", 4);
            intent.putExtra("aspectY", 3);
            intent.putExtra("scale", true);
            File croppedFileName = null;
            try {
                String url = "tmp_" + String.valueOf(System.currentTimeMillis());
                File storeDir = new File(Environment.getExternalStorageDirectory(), "/testImage/");
                if (!storeDir.exists()) storeDir.mkdirs();
                croppedFileName = File.createTempFile(url, ".jpg", storeDir);
            } catch (IOException e) {
                e.printStackTrace();
            }

            File folder = new File(Environment.getExternalStorageDirectory() + "/test/");
            File tempFile = new File(folder.toString(), croppedFileName.getName());

            uri = FileProvider.getUriForFile(ContactAddActivity.this,
                    "com.example.secondapplication", tempFile);

            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            intent.addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION);

            intent.putExtra("return-data", false);
            intent.putExtra(MediaStore.EXTRA_OUTPUT, uri);
            intent.putExtra("outputFormat", Bitmap.CompressFormat.JPEG.toString()); //Bitmap 형태로 받기 위해 해당 작업 진행

            Intent i = new Intent(intent);
            ResolveInfo res = list.get(0);
            i.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            i.addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
            grantUriPermission(res.activityInfo.packageName, uri,
                    Intent.FLAG_GRANT_WRITE_URI_PERMISSION | Intent.FLAG_GRANT_READ_URI_PERMISSION);

            i.setComponent(new ComponentName(res.activityInfo.packageName, res.activityInfo.name));
            startActivityForResult(i, 23);

        }

    }
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data){
        super.onActivityResult(requestCode, resultCode, data);
        System.out.println(resultCode);
        if(resultCode != RESULT_OK){
            Toast.makeText(ContactAddActivity.this, "error while handling image", Toast.LENGTH_SHORT).show();
            return;
        }
        switch(requestCode) {
            case 22:
                if (data == null) {
                    Toast.makeText(ContactAddActivity.this, "empty data!!", Toast.LENGTH_SHORT).show();
                    return;
                }
                uri = data.getData();
            case 21:
                this.grantUriPermission("com.android.camera", uri,
                        Intent.FLAG_GRANT_WRITE_URI_PERMISSION | Intent.FLAG_GRANT_READ_URI_PERMISSION);
                Intent intent = new Intent("com.android.camera.action.CROP");
                intent.setDataAndType(uri, "image/*");
                intent.putExtra("crop", "true");
                intent.putExtra("outputX", 200); intent.putExtra("outputY", 200);
                intent.putExtra("aspectX", 1); intent.putExtra("aspectY", 1);
                intent.putExtra("scale", true); intent.putExtra("return-data", true);
                intent.putExtra("outputFormat", Bitmap.CompressFormat.JPEG.toString());
                startActivityForResult(intent, 23);
                break;
            case 23:
                System.out.println("Noww.....");
                break;
        }
    }
}
