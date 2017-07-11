package com.example.secondapplication;

import android.Manifest;
import android.app.Dialog;
import android.content.ContentResolver;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.TypedValue;
import android.view.View;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.facebook.AccessToken;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.GraphRequest;
import com.facebook.GraphResponse;
import com.facebook.HttpMethod;
import com.facebook.login.LoginManager;
import com.facebook.login.LoginResult;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;

import okhttp3.HttpUrl;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class ContactActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    private CallbackManager callbackManager;
    private NavigationView navigationView;
    private Menu navMenu;
    private Bitmap userBitmap;
    private String userName, userEmail;
    private boolean isLogin = false;
    private ImageView imageView;
    private TextView tvName;
    private TextView tvEmail;
    private ContactAdapter adapter;
    private ListView listView;
    private boolean firstChangeMode = true;
    private String userId;
    private EditText searchText;
    private RelativeLayout searchView;

    void addItem(int mode, String name, String email, String phone, String picUrl, String uniqueId){
        adapter.addItem(mode, name, email, phone, picUrl, uniqueId);
        changeShowMode(adapter.getShowMode());
    }
    void changeShowMode(int newMode){
        if(newMode != adapter.getShowMode() || firstChangeMode){
            firstChangeMode = false;
            adapter.changeShowMode(newMode);
        }
        navMenu.findItem(R.id.nav_showAll).setChecked(newMode == 0);
        navMenu.findItem(R.id.nav_showFacebook).setChecked(newMode == 1);
        navMenu.findItem(R.id.nav_showContact).setChecked(newMode == 2);
        navMenu.findItem(R.id.nav_showCustom).setChecked(newMode == 3);
        String titleString = "";
        if(newMode == 0) titleString = "전체";
        if(newMode == 1) titleString = "Facebook";
        if(newMode == 2) titleString = "핸드폰";
        if(newMode == 3) titleString = "커스텀";
        titleString += " 주소록 (" + adapter.getCount() + ")";
        this.setTitle(titleString);
    }
    void loginTask(int loginParam){
        boolean isNowLogin;
        if(loginParam == 1) isNowLogin = false;
        else isNowLogin = !(AccessToken.getCurrentAccessToken() == null);
        System.out.println(isLogin + " " + isNowLogin);
        navMenu.findItem(R.id.nav_login).setVisible(!isNowLogin);
        navMenu.findItem(R.id.nav_logout).setVisible(isNowLogin);
        navMenu.findItem(R.id.nav_addContact).setVisible(isNowLogin);
        navMenu.findItem(R.id.nav_syncFacebook).setVisible(false);
        navMenu.findItem(R.id.nav_syncPhone).setVisible(false);
        navMenu.findItem(R.id.nav_showFacebook).setVisible(isNowLogin);
        if(!isNowLogin){
            navMenu.findItem(R.id.nav_showCustom).setVisible(isNowLogin);
            navMenu.findItem(R.id.nav_syncCustom).setVisible(false);
        }
        try {
            final boolean canApply = (imageView != null && tvName != null && tvEmail != null);
            if (!isNowLogin) {
                userBitmap = null;
                userName = "로그인 해주세요";
                userEmail = "";
                userId = "";
                adapter.deleteAll(1);
                adapter.deleteAll(3);
                //System.out.println(canApply);
                //System.out.println(imageView + " " + tvName + " " + tvEmail);
                if(canApply){
                    imageView.setImageResource(android.R.drawable.sym_def_app_icon);
                    tvName.setText("로그인 해주세요");
                    tvEmail.setText("");
                }
            } else {
                if(isLogin) {
                    if(canApply) {
                        if (tvName.getText() != userName) {
                            imageView.setImageBitmap(userBitmap);
                            tvName.setText(userName);
                            tvEmail.setText(userEmail);
                        }
                    }
                }else{
                    new GraphRequest(
                            AccessToken.getCurrentAccessToken(),
                            "/me?fields=id,name,picture.type(large),email&locale=ko_KR",
                            null,
                            HttpMethod.GET,
                            new GraphRequest.Callback() {
                                class imgDownloadTask extends AsyncTask<String, Void, Bitmap> {
                                    ImageView iv = (ImageView) findViewById(R.id.userImageView);
                                    imgDownloadTask() {
                                    }
                                    @Override
                                    protected void onPreExecute() {
                                        super.onPreExecute();
                                    }

                                    private Bitmap downloadUrl(String imgUrl) throws IOException {
                                        System.out.println("trying to GET " + imgUrl);
                                        Bitmap bitmap = null;
                                        InputStream iStream = null;
                                        HttpURLConnection urlConnection = null;
                                        try {
                                            URL url = new URL(imgUrl);
                                            urlConnection = (HttpURLConnection) url.openConnection();
                                            urlConnection.connect();
                                            iStream = urlConnection.getInputStream();
                                            bitmap = BitmapFactory.decodeStream(iStream);
                                        } catch (Exception e) {
                                            e.printStackTrace();
                                        } finally {
                                            iStream.close();
                                            urlConnection.disconnect();
                                        }
                                        return bitmap;
                                    }

                                    @Override
                                    protected Bitmap doInBackground(String... imgUrl) {
                                        String myUrl = imgUrl[0];
                                        Bitmap bitmap = null;
                                        try {
                                            bitmap = downloadUrl(myUrl);
                                        } catch (Exception e) {
                                            e.printStackTrace();
                                        }
                                        return bitmap;
                                    }

                                    @Override
                                    protected void onPostExecute(Bitmap result) {
                                        if (result != null) {
                                            super.onPostExecute(result);
                                            iv.setImageBitmap(result);
                                            if (canApply) userBitmap = result;
                                            iv.invalidate();
                                        }
                                    }
                                }

                                public void onCompleted(GraphResponse response) {
                                    try {
                                        System.out.println(response);
                                        JSONObject myInfo = response.getJSONObject();
                                        System.out.println(myInfo);
                                        String myName = myInfo.getString("name");
                                        String myEmail = "";
                                        try {
                                            myEmail = myInfo.getString("email");
                                        }catch(Exception e){
                                            myEmail = "NONE";
                                        }
                                        String myId = myInfo.getString("id");
                                        String myPictureUrl = myInfo.getJSONObject("picture").getJSONObject("data").getString("url");
                                        userName = myName;
                                        userEmail = myEmail;
                                        userId = myId;
                                        navMenu.findItem(R.id.nav_showCustom).setVisible(true);
                                        navMenu.findItem(R.id.nav_syncCustom).setVisible(false);
                                        if (canApply) {
                                            TextView tvName = (TextView) findViewById(R.id.userName);
                                            tvName.setText(myName);
                                            TextView tvEmail = (TextView) findViewById(R.id.userEmail);
                                            tvEmail.setText(myEmail);
                                        }
                                        imgDownloadTask downTask = new imgDownloadTask();
                                        downTask.execute(myPictureUrl);
                                        changeShowMode(0);
                                        syncCustom();
                                        syncFacebook();
                                    } catch (Exception e) {
                                        e.printStackTrace();
                                    }
                                }
                            }
                    ).executeAsync();
                }
            }
        }catch(Exception e){
            e.printStackTrace();
        }
        isLogin = isNowLogin;
    }
    public int DPtoPX(int dp){
        return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp, getResources().getDisplayMetrics());
    }
    @Override
    public void onSaveInstanceState(Bundle savedInstanceState){
        savedInstanceState.putBoolean("isLogin", isLogin);
        savedInstanceState.putString("userName", userName);
        savedInstanceState.putString("userEmail", userEmail);
        savedInstanceState.putString("userId", userId);
        savedInstanceState.putParcelable("userBitmap", userBitmap);
        savedInstanceState.putInt("showMode", adapter.getShowMode());
        //savedInstanceState.putParcelableArrayList("contactList", adapter.getContactList());
        savedInstanceState.putParcelableArrayList("facebookList", adapter.getFacebookList());
        savedInstanceState.putParcelableArrayList("phoneList", adapter.getPhoneList());
        savedInstanceState.putParcelableArrayList("customList", adapter.getCustomList());
        savedInstanceState.putBoolean("search", adapter.getSearchMode());
        savedInstanceState.putString("searchKeyword", searchText.getText().toString());
        super.onSaveInstanceState(savedInstanceState);
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.setTitle("주소록");
        setContentView(R.layout.activity_contact);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();
        searchText = ((EditText) findViewById(R.id.contactSearchEdit));
        searchView = ((RelativeLayout) findViewById(R.id.contactSearch));
        navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);
        navMenu = navigationView.getMenu();
        if(savedInstanceState != null) {
            try {
                isLogin = savedInstanceState.getBoolean("isLogin");
                userName = savedInstanceState.getString("userName");
                userEmail = savedInstanceState.getString("userEmail");
                userId = savedInstanceState.getString("userId");
                userBitmap = savedInstanceState.getParcelable("userBitmap");
                String keyword = savedInstanceState.getString("searchKeyword");
                searchText.setText(keyword);
                adapter = new ContactAdapter(DPtoPX(50));
                //ArrayList<ContactInfo> contactList = savedInstanceState.getParcelableArrayList("contactList");
                //adapter.setContactList(contactList);
                int showMode = savedInstanceState.getInt("showMode");
                ArrayList<ContactInfo> facebookList = savedInstanceState.getParcelableArrayList("facebookList");
                adapter.setFacebookList(facebookList);
                ArrayList<ContactInfo> phoneList = savedInstanceState.getParcelableArrayList("phoneList");
                adapter.setPhoneList(phoneList);
                ArrayList<ContactInfo> customList = savedInstanceState.getParcelableArrayList("customList");
                adapter.setCustomList(customList);
                adapter.changeSearchMode(savedInstanceState.getBoolean("search"), keyword);
                changeShowMode(showMode);
                if(adapter.getSearchMode()) searchView.setVisibility(View.VISIBLE);
                else searchView.setVisibility(View.GONE);
            }catch(Exception e){
                e.printStackTrace();
            }
        }else {
            ((RelativeLayout) findViewById(R.id.contactSearch)).setVisibility(View.GONE);
            adapter = new ContactAdapter(DPtoPX(50));
            changeShowMode(0);
            trySyncContact();
        }
        listView = (ListView) findViewById(R.id.contactList);
        listView.setAdapter(adapter);
        listView.invalidateViews();
        listView.postInvalidate();
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener(){
            @Override
            public void onItemClick(AdapterView<?> adView, View view, int pos, long longId){
                final ContactInfo targetInfo = (ContactInfo) adapter.getItem(pos);
                final ContactViewDialog showDialog = new ContactViewDialog(ContactActivity.this, targetInfo);
                View.OnClickListener mEditListener = new View.OnClickListener(){
                    @Override
                    public void onClick(View v){
                        Intent intent = new Intent(ContactActivity.this, ContactAddActivity.class);
                        intent.putExtra("userId", userId);
                        intent.putExtra("uniqueId", targetInfo.getUniqueId());
                        intent.putExtra("name", targetInfo.getName());
                        intent.putExtra("phone", targetInfo.getPhone());
                        intent.putExtra("email", targetInfo.getEmail());
                        intent.putExtra("pic", targetInfo.getThumb());
                        startActivityForResult(intent, 809);
                        showDialog.dismiss();
                    }
                };
                View.OnClickListener mDeleteListener = new View.OnClickListener(){
                    @Override
                    public void onClick(View v){
                        adapter.deleteItem(3, userId, targetInfo.getUniqueId());
                        showDialog.dismiss();
                    }
                };
                showDialog.show();
                showDialog.setEditListener(mEditListener);
                showDialog.setDeleteListener(mDeleteListener);
            }
        });
        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                adapter.changeSearchMode(!(adapter.getSearchMode()), "");
                if(adapter.getSearchMode()) searchView.setVisibility(View.VISIBLE);
                else{
                    searchView.setVisibility(View.GONE);
                    searchText.setText("");
                }
            }
        });
        searchText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}
            @Override
            public void afterTextChanged(Editable s) {
                String str = s.toString();
                adapter.changeSearchMode(adapter.getSearchMode(), str);
            }
        });
    }
    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults){
        switch(requestCode){
            case 11:
                if(grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED){
                    syncContact();
                }else{
                    Toast.makeText(ContactActivity.this, "권한 사용에 동의하지 않으셨습니다!", Toast.LENGTH_LONG).show();
                    finish();
                }
                return;
        }
    }
    void trySyncContact(){
        if(ContextCompat.checkSelfPermission(ContactActivity.this, Manifest.permission.READ_CONTACTS) == PackageManager.PERMISSION_GRANTED){
            syncContact();
        }else{
            ActivityCompat.requestPermissions(ContactActivity.this, new String[]{Manifest.permission.READ_CONTACTS}, 11);
        }
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    public boolean onCreateOptionsMenu(Menu menu) {
        //getMenuInflater().inflate(R.menu.activity_contact_drawer, menu);
        ImageView iv = (ImageView) findViewById(R.id.userImageView);
        iv.setLayoutParams(new LinearLayout.LayoutParams(DPtoPX(100)/2, DPtoPX(100)/2+DPtoPX(16)));
        iv.setScaleType(ImageView.ScaleType.FIT_XY);
        imageView = (ImageView) findViewById(R.id.userImageView);
        tvName = (TextView) findViewById(R.id.userName);
        tvEmail = (TextView) findViewById(R.id.userEmail);
        loginTask(0);
        return true;
    }

/*
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
*/
    public void syncFacebook(){
        adapter.deleteAll(1);
        if(AccessToken.getCurrentAccessToken() == null){
            loginTask(0);
        }else{
            final GraphRequest.Callback graphCallBack = new GraphRequest.Callback(){
                public void onCompleted(GraphResponse response) {
                    try{
                        JSONObject myInfo = response.getJSONObject();
                        JSONArray fList = myInfo.getJSONArray("data");
                        System.out.println(fList.length());
                        System.out.println(fList.getJSONObject(0));
                        for(int i=0;i<fList.length();i++){
                            JSONObject fObject = fList.getJSONObject(i);
                            String fName = fObject.getString("name");
                            String fUrl = fObject.getJSONObject("picture").getJSONObject("data").getString("url");
                            addItem(1, fName, "", "", fUrl, "");
                        }
                        adapter.sortItem();
                            /**/
                        GraphRequest nextRequest = response.getRequestForPagedResults(GraphResponse.PagingDirection.NEXT);
                        if(nextRequest != null){
                            nextRequest.setCallback(this);
                            nextRequest.executeAsync();
                        }
                    }catch(Exception e){
                        e.printStackTrace();
                    }
                }
            };
            new GraphRequest(AccessToken.getCurrentAccessToken(), "me/taggable_friends?fields=name,picture&locale=ko_KR", null, HttpMethod.GET, graphCallBack).executeAsync();
        }
    }
    public void syncContact(){
        adapter.deleteAll(2);
        String[] projection = new String[]{
                ContactsContract.CommonDataKinds.Phone.CONTACT_ID,
                ContactsContract.CommonDataKinds.Phone.NUMBER,
                ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME
        };
        String[] selectionArgs = null;
        String sortOrder = ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME + " COLLATE LOCALIZED ASC";
        Cursor cursor = getApplication().getContentResolver().query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI, projection, null, null, sortOrder);
        if(cursor.moveToFirst()){
            do{
                String number = cursor.getString(1).replaceAll("-","");
                String name = cursor.getString(2);
                addItem(2, name, "", number, "", "");
            }while(cursor.moveToNext());
        }
    }
    private class getCustomTask extends AsyncTask<String, String, String>{
        String sendPost(String uid){
            OkHttpClient client = new OkHttpClient();
            Request request = new Request.Builder()
                    .url("http://52.78.17.108:50045/contact/show/"+uid+"/custom/")
                    .build();
            try{
                Response response = client.newCall(request).execute();
                return response.body().string();
            }catch(Exception e){
                e.printStackTrace();
                return "";
            }
        }
        @Override
        protected String doInBackground(String... apiUrl) {
            return sendPost(apiUrl[0]);
        }
        @Override
        protected void onPostExecute(String res){
            if(res != null) {
                super.onPostExecute(res);
                try {
                    JSONArray jsonArray = new JSONArray(res);
                    System.out.println("length: " + jsonArray.length());
                    for (int i = 0; i < jsonArray.length(); i++) {
                        JSONObject jsonObj = jsonArray.getJSONObject(i);
                        addItem(3, jsonObj.getString("name"), jsonObj.getString("email"), jsonObj.getString("phone"), jsonObj.getString("pictureEnc"), jsonObj.getString("_id"));
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    };
    public void syncCustom(){
        new getCustomTask().execute(userId);
    }
    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();
        if(id == R.id.nav_showAll){
            changeShowMode(0);
        } else if(id == R.id.nav_showFacebook){
            changeShowMode(1);
        } else if(id == R.id.nav_showContact){
            changeShowMode(2);
        } else if(id == R.id.nav_showCustom){
            changeShowMode(3);
        }
        if(id == R.id.nav_addContact){
            Intent intent = new Intent(ContactActivity.this, ContactAddActivity.class);
            System.out.println(userId);
            intent.putExtra("userId", userId);
            intent.putExtra("uniqueId", "");
            startActivityForResult(intent, 808);
        } else if (id == R.id.nav_syncFacebook) {
            syncFacebook();
        } else if(id == R.id.nav_syncPhone){
            trySyncContact();
        } else if (id == R.id.nav_login) {
            callbackManager = CallbackManager.Factory.create();
            LoginManager.getInstance().logInWithReadPermissions(ContactActivity.this, Arrays.asList("public_profile", "email", "user_friends"));
            LoginManager.getInstance().registerCallback(callbackManager, new FacebookCallback<LoginResult>() {
                @Override
                public void onSuccess(LoginResult loginResult) {
                    Toast.makeText(ContactActivity.this, "Login Success", Toast.LENGTH_SHORT).show();
                    loginTask(0);
                }

                @Override
                public void onCancel() {
                    Toast.makeText(ContactActivity.this, "Login Canceled", Toast.LENGTH_SHORT).show();
                    loginTask(0);
                }

                @Override
                public void onError(FacebookException error) {
                    Toast.makeText(ContactActivity.this, "Login Error", Toast.LENGTH_SHORT).show();
                    loginTask(0);
                }
            });
        } else if (id == R.id.nav_logout) {
            loginTask(1);
            if(AccessToken.getCurrentAccessToken() != null){
                LoginManager.getInstance().logOut();
                Toast.makeText(ContactActivity.this, "로그아웃됨", Toast.LENGTH_SHORT).show();
            }
            changeShowMode(0);
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == 808){
            if(resultCode == RESULT_OK) {
                try {
                    String newPhone = data.getExtras().getString("Phone");
                    String newEmail = data.getExtras().getString("Email");
                    String newName = data.getExtras().getString("Name");
                    String newPic = data.getExtras().getString("Pic");
                    String uniqueId = data.getExtras().getString("uniqueId");
                    addItem(3, newName, newEmail, newPhone, newPic, uniqueId);
                } catch (Exception e) {
                }
            }
        }else if(requestCode == 809){
            if(resultCode == RESULT_OK) {
                try {
                    System.out.println("Server Update");
                    String newPhone = data.getExtras().getString("Phone");
                    String newEmail = data.getExtras().getString("Email");
                    String newName = data.getExtras().getString("Name");
                    String newPic = data.getExtras().getString("Pic");
                    String uniqueId = data.getExtras().getString("uniqueId");
                    adapter.editItem(3, newName, newEmail, newPhone, newPic, uniqueId);
                }catch(Exception e){
                }
            }
        }else{
            callbackManager.onActivityResult(requestCode, resultCode, data);
        }
    }
}
