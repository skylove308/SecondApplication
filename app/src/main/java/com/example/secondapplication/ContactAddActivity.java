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
import android.graphics.BitmapFactory;
import android.media.MediaScannerConnection;
import android.media.ThumbnailUtils;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.os.storage.StorageManager;
import android.os.storage.StorageVolume;
import android.provider.MediaStore;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Base64;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
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

    private String mCurrentPhotoPath;
    Uri photoUri, albumUri;
    boolean isAlbum = false;

    @Override
    public void onSaveInstanceState(Bundle savedInstanceState) {
        savedInstanceState.putParcelable("newProfilePic", newProfilePic);
    }
    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults){
        switch(requestCode){
            case 42:
                if(grantResults[0] == PackageManager.PERMISSION_GRANTED && grantResults[1] == PackageManager.PERMISSION_GRANTED && grantResults[2] == PackageManager.PERMISSION_GRANTED){
                    takePhoto();
                }
                break;
        }
    }
    private File createImageFile() throws IOException{
        String fileName = "tmp_" + String.valueOf(System.currentTimeMillis()) + ".jpg";
        File storeDir = new File(Environment.getExternalStorageDirectory(), "/tempImg/");
        if (!storeDir.exists()) storeDir.mkdirs();
        File newFile = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + "/tempImg/" + fileName);
        mCurrentPhotoPath = newFile.getAbsolutePath();
        return newFile;
    }
    void takePhoto(){
        String state = Environment.getExternalStorageState();
        if(Environment.MEDIA_MOUNTED.equals(state)) {
            Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            if(intent.resolveActivity(getPackageManager()) != null) {
                File newFile = null;
                try {
                    newFile = createImageFile();
                } catch (Exception e) {
                    e.printStackTrace();
                }
                if(newFile != null) {
                    photoUri = FileProvider.getUriForFile(ContactAddActivity.this, "com.example.secondapplication", newFile);
                    intent.putExtra(android.provider.MediaStore.EXTRA_OUTPUT, photoUri);
                    Toast.makeText(ContactAddActivity.this, "TakePhoto", Toast.LENGTH_SHORT).show();
                    startActivityForResult(intent, 21);
                }
            }
        }else{
            Toast.makeText(ContactAddActivity.this, "Didn't support external memory", Toast.LENGTH_SHORT).show();
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
    void getPhoto(){
        Toast.makeText(ContactAddActivity.this, "GetPhoto", Toast.LENGTH_SHORT).show();
        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setType(android.provider.MediaStore.Images.Media.CONTENT_TYPE);
        startActivityForResult(intent, 22);
    }
    private EditText nameText, emailText, phoneText;
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
                        getPhoto();
                    }
                };
                DialogInterface.OnClickListener cancelListener = new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Toast.makeText(ContactAddActivity.this, "Cancel", Toast.LENGTH_SHORT).show();
                        dialog.dismiss();
                    }
                };
                DialogInterface.OnClickListener clearListener = new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Toast.makeText(ContactAddActivity.this, "Clear", Toast.LENGTH_SHORT).show();
                        newProfilePic = null;
                        imgView.setImageResource(R.drawable.ic_account_circle_gray_24dp);
                    }
                };
                new AlertDialog.Builder(v.getContext())
                        .setTitle("업로드 이미지 선택")
                        .setPositiveButton("사진 촬영", cameraListener)
                        .setNegativeButton("앨범 선택", albumListener)
                        .setNeutralButton("CLEAR", clearListener)
                        .show();
            }
        });
        nameText = (EditText) findViewById(R.id.editAddName);
        emailText = (EditText) findViewById(R.id.editAddEmail);
        phoneText = (EditText) findViewById(R.id.editAddPhone);
        addButton = (Button) findViewById(R.id.addContactButton);
        addButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String name = nameText.getText().toString();
                String email = emailText.getText().toString();
                String phone = phoneText.getText().toString();
                if(name.length() > 0 && (email.length() > 0 || phone.length() > 0)){
                    sendInformation(name, email, phone);
                }else{
                    Toast.makeText(v.getContext(), "내용을 채워주세요", Toast.LENGTH_LONG).show();
                }
            }
        });
        if(savedInstanceState != null){
            newProfilePic = savedInstanceState.getParcelable("newProfilePic");
        }
        if(newProfilePic != null) imgView.setImageBitmap(newProfilePic);
        else imgView.setImageResource(R.drawable.ic_account_circle_gray_24dp);
    }
    public String encodeTobase64()
    {
        if(newProfilePic == null) return "";
        Bitmap immagex = newProfilePic;
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        immagex.compress(Bitmap.CompressFormat.PNG, 100, baos);
        byte[] b = baos.toByteArray();
        String imageEncoded = Base64.encodeToString(b, Base64.NO_WRAP);
        return imageEncoded;
    }
    String makeJson(String name, String email, String phone, String imgEnc){
        return "{\"name\" : \"" + name + "\"," +
                "\"email\" : \"" + email + "\"," +
                "\"phone\" : \"" + phone + "\"," +
                "\"picEnc\" : \"" + imgEnc + "\"}";
    }
    String sendPost(String url, String jsonStr){
        client = new OkHttpClient();
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
    Handler handler = new Handler(){
        public void handleMessage(Message msg){
            Bundle bun = msg.getData();
            String result = bun.getString("RESPONSE");
            System.out.println(result);
            Intent intent = new Intent();
            intent.putExtra("Name", bun.getString("Name"));
            intent.putExtra("Phone", bun.getString("Phone"));
            intent.putExtra("Email", bun.getString("Email"));
            intent.putExtra("Pic", bun.getString("Pic"));
            setResult(RESULT_OK, intent);
            //Toast.makeText(ContactAddActivity.this, "SUCCESS!", Toast.LENGTH_SHORT);
            finish();
        }
    };
    String sendInformation(final String name, final String email, final String phone){
        new Thread() {
            public void run() {
                String imgEnc = encodeTobase64().trim();
                String jsonStr = makeJson(name, email, phone, imgEnc);
                System.out.println(jsonStr);
                String response = sendPost("http://52.78.17.108:50045/contact/add/"+uid+"/custom", jsonStr);
                Bundle bun = new Bundle();
                bun.putString("RESPONSE", response);
                bun.putString("Name", name);
                bun.putString("Email", email);
                bun.putString("Phone", phone);
                bun.putString("Pic", imgEnc);
                Message msg = handler.obtainMessage();
                msg.setData(bun);
                handler.sendMessage(msg);
            }
        }.start();
        return "";
    }
    void cropImage(){
        Intent cropIntent = new Intent("com.android.camera.action.CROP");
        cropIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        cropIntent.addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
        cropIntent.setDataAndType(photoUri, "image/*");
        cropIntent.putExtra("outputX", 100);
        cropIntent.putExtra("outputY", 100);
        cropIntent.putExtra("aspectX", 1);
        cropIntent.putExtra("aspectY", 1);
        cropIntent.putExtra("scale", true);
        if(isAlbum){
            cropIntent.putExtra("output", albumUri);
        }else{
            cropIntent.putExtra("output", photoUri);
        }
        startActivityForResult(cropIntent, 23);
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
                isAlbum = true;
                File albumFile = null;
                try {
                    albumFile = createImageFile();
                }catch(IOException e){
                    e.printStackTrace();
                }
                if(albumFile != null){
                    albumUri = Uri.fromFile(albumFile);
                    photoUri = data.getData();
                    cropImage();
                }
                break;
            case 21:
                isAlbum = false;
                cropImage();
                break;
            case 23:
                System.out.println("Noww.....");
                File f = new File(mCurrentPhotoPath);
                Uri contentUri = Uri.fromFile(f);
                BitmapFactory.Options options = new BitmapFactory.Options();
                options.inJustDecodeBounds = false;
                try {
                    newProfilePic = BitmapFactory.decodeStream(ContactAddActivity.this.getContentResolver().openInputStream(contentUri), null, options);
                    int imageHeight = options.outHeight;
                    int imageWidth = options.outWidth;
                    System.out.println("imgSize: " + imageHeight + " " + imageWidth);
                    imgView.setImageBitmap(newProfilePic);
                }catch(Exception e){
                    e.printStackTrace();
                }
                break;
        }
    }
}
