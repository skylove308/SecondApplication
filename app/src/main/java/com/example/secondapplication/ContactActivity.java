package com.example.secondapplication;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
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
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
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
import java.util.Arrays;

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

    @Override
    public void onSaveInstanceState(Bundle savedInstanceState){
        savedInstanceState.putBoolean("isLogin", isLogin);
        savedInstanceState.putString("userName", userName);
        savedInstanceState.putString("userEmail", userEmail);
        savedInstanceState.putParcelable("userBitmap", userBitmap);
        super.onSaveInstanceState(savedInstanceState);
    }
    void loginTask(int loginParam){
        boolean isNowLogin;
        if(loginParam == 1) isNowLogin = false;
        else isNowLogin = !(AccessToken.getCurrentAccessToken() == null);
        System.out.println(isLogin + " " + isNowLogin);
        navMenu.findItem(R.id.nav_login).setVisible(!isNowLogin);
        navMenu.findItem(R.id.nav_logout).setVisible(isNowLogin);
        navMenu.findItem(R.id.nav_showCustom).setVisible(isNowLogin);
        navMenu.findItem(R.id.nav_addContact).setVisible(isNowLogin);
        navMenu.findItem(R.id.nav_sync).setVisible(isNowLogin);
        navMenu.findItem(R.id.nav_showFacebook).setVisible(isNowLogin);
        try {
            final boolean canApply = (imageView != null && tvName != null && tvEmail != null);
            if (!isNowLogin) {
                userBitmap = null;
                userName = "로그인 해주세요";
                userEmail = "";
                System.out.println(canApply);
                System.out.println(imageView + " " + tvName + " " + tvEmail);
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
                            "/me?fields=name,picture,email",
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
                                        String myEmail = myInfo.getString("email");
                                        String myPictureUrl = myInfo.getJSONObject("picture").getJSONObject("data").getString("url");
                                        userName = myName;
                                        userEmail = myEmail;
                                        if (canApply) {
                                            TextView tvName = (TextView) findViewById(R.id.userName);
                                            tvName.setText(myName);
                                            TextView tvEmail = (TextView) findViewById(R.id.userEmail);
                                            tvEmail.setText(myEmail);
                                        }
                                        imgDownloadTask downTask = new imgDownloadTask();
                                        downTask.execute(myPictureUrl);
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
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.setTitle("주소록");
        setContentView(R.layout.activity_contact);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);
        navMenu = navigationView.getMenu();
        if(savedInstanceState != null) {
            try {
                isLogin = savedInstanceState.getBoolean("isLogin");
                userName = savedInstanceState.getString("userName");
                userEmail = savedInstanceState.getString("userEmail");
                userBitmap = savedInstanceState.getParcelable("userBitmap");
                System.out.println(isLogin);
                System.out.println(userName);
                System.out.println(userEmail);
            }catch(Exception e){
                e.printStackTrace();
            }
        }
        adapter = new ContactAdapter(DPtoPX(50));
        listView = (ListView) findViewById(R.id.contactList);
        listView.setAdapter(adapter);
        /*
        adapter.addItem("Jihoon Ko", "jihoonko@kaist.ac.kr", "01025569631",
                "https://scontent.xx.fbcdn.net/v/t1.0-1/p50x50/12227170_906747612747392_849221077459147265_n.jpg?oh=a76984fe10c8e05746e112643ad95c86&oe=59C63D5A");
        adapter.addItem("Jihoon Ko", "jihoonko@kaist.ac.kr", "01025569631",
                "https://scontent.xx.fbcdn.net/v/t1.0-1/p50x50/12227170_906747612747392_849221077459147265_n.jpg?oh=a76984fe10c8e05746e112643ad95c86&oe=59C63D5A");
        adapter.addItem("Jihoon Ko", "jihoonko@kaist.ac.kr", "01025569631",
                "https://scontent.xx.fbcdn.net/v/t1.0-1/p50x50/12227170_906747612747392_849221077459147265_n.jpg?oh=a76984fe10c8e05746e112643ad95c86&oe=59C63D5A");
        adapter.addItem("Jihoon Ko", "jihoonko@kaist.ac.kr", "01025569631",
                "https://scontent.xx.fbcdn.net/v/t1.0-1/p50x50/12227170_906747612747392_849221077459147265_n.jpg?oh=a76984fe10c8e05746e112643ad95c86&oe=59C63D5A");
        */
        //adapter.deleteAll();
        //System.out.println("Token: " + AccessToken.getCurrentAccessToken());
        //loginTask();
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
    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();
        if(id == R.id.nav_showAll){
        } else if(id == R.id.nav_showFacebook){
        } else if(id == R.id.nav_showContact){
        }

        if (id == R.id.nav_sync) {
            adapter.deleteAll();
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
                                adapter.addItem(fName, "", "", fUrl);
                                adapter.notifyDataSetChanged();
                            }
                            adapter.sortItem();
                            adapter.notifyDataSetChanged();
                            /*listView.invalidateViews();
                            listView.postInvalidate();*/
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
                new GraphRequest(AccessToken.getCurrentAccessToken(), "me/taggable_friends", null, HttpMethod.GET, graphCallBack).executeAsync();
            }
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
            }
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        callbackManager.onActivityResult(requestCode, resultCode, data);
    }
}
