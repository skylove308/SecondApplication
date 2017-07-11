package com.example.secondapplication;

import android.Manifest;
import android.content.Intent;
import android.content.pm.LauncherApps;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.ExifInterface;
import android.media.MediaPlayer;
import android.os.AsyncTask;
import android.os.Environment;
import android.os.SystemClock;
import android.speech.RecognitionListener;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
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

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.Buffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Random;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class ButtonD extends AppCompatActivity {
    private ImageView profile;
    private TextView nameText, profileResult;
    private Button voiceButton, fbLogout;
    private TextView resultText, expectedText, showStatus;
    private RelativeLayout voiceLayout;
    private SpeechRecognizer mRecognizer;
    final int REQUEST_AUDIO = 1;
    private Intent intent;
    private int successTry, failTry;
    final String[] testStr = {"Worcestershire", "Specific", "Squirrel", "Brewery", "Phenomenon", "Derby", "Regularly", "February", "Edited", "Heir"};

    private boolean isLogin;
    private boolean first = true;
    private CallbackManager callbackManager;

    private String userId, userName, userPicUrl;
    private Bitmap userBitmap;
    private int attempt, success;

    private class getProfileTask extends AsyncTask<String, Void, String>{
        @Override
        protected String doInBackground(String... picurl){
            System.out.println("trying to GET " + picurl[0]);
            Bitmap bitmap = null;
            InputStream iStream = null;
            HttpURLConnection urlConnection = null;
            try {
                URL url = new URL(picurl[0]);
                urlConnection = (HttpURLConnection) url.openConnection();
                urlConnection.connect();
                iStream = urlConnection.getInputStream();
                bitmap = BitmapFactory.decodeStream(iStream);
                iStream.close();
                urlConnection.disconnect();
                System.out.println("GET 200 OK / " + picurl[0]);
                userBitmap = bitmap;
            } catch (Exception e) {
                e.printStackTrace();
                return null;
            }
            OkHttpClient client = new OkHttpClient();
            Request request = new Request.Builder()
                    .url("http://52.78.17.108:50045/voice/user/"+userId+"/")
                    .build();
            try{
                Response response = client.newCall(request).execute();
                String res = response.body().string();
                return res;
            }catch(Exception e){
                e.printStackTrace();
                return null;
            }
        }
        @Override
        protected void onPostExecute(String str){
            if(str == null) return;
            super.onPostExecute(str);
            profile.setImageBitmap(userBitmap);
            try{
                JSONObject jsonObj = new JSONObject(str);
                attempt = jsonObj.getInt("attempt");
                success = jsonObj.getInt("success");
                System.out.println(attempt);
                System.out.println(success);
                profileResult.setText(success + " success / " + attempt + " attempt");
            }catch(Exception e){
                e.printStackTrace();
            }
            voiceLayout.setVisibility(View.VISIBLE);
        }
    };
    void loginTask(){
        if(AccessToken.getCurrentAccessToken() == null){
            if(isLogin || first){
                profile.setImageResource(R.drawable.ic_account_circle_orange_24dp);
                nameText.setText("로그인 해주세요");
                fbLogout.setText("LOG IN");
                attempt = 0; success = 0;
                profileResult.setText("0 success / 0 attempt");
                voiceLayout.setVisibility(View.GONE);
            }
            isLogin = false;
        }else {
            if(!isLogin || first) {
                new GraphRequest(
                        AccessToken.getCurrentAccessToken(),
                        "/me/?fields=id,name,picture.type(large)&locale=ko_KR",
                        null,
                        HttpMethod.GET,
                        new GraphRequest.Callback() {
                            public void onCompleted(GraphResponse response) {
                                try {
                                    System.out.println(response);
                                    userId = response.getJSONObject().getString("id");
                                    userName = response.getJSONObject().getString("name");
                                    userPicUrl = response.getJSONObject().getJSONObject("picture").getJSONObject("data").getString("url");
                                    System.out.println(userId);
                                    showStatus.setText("아래 마이크 버튼을 누르세요");
                                    nameText.setText(userName);
                                    fbLogout.setText("LOG OUT");
                                    new getProfileTask().execute(userPicUrl);
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                            }
                        }
                ).executeAsync();
            }
            isLogin = true;
        }
        first = false;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_button_d);

        profile = (ImageView) findViewById(R.id.profileImage);
        nameText = (TextView) findViewById(R.id.profileName);
        fbLogout = (Button) findViewById(R.id.fbLogout);
        profileResult = (TextView) findViewById(R.id.profileResult);

        voiceButton = (Button) findViewById(R.id.voiceButton);

        resultText = (TextView) findViewById(R.id.resultText);
        expectedText = (TextView) findViewById(R.id.expectedText);
        showStatus = (TextView) findViewById(R.id.showStatus);

        successTry = 0;
        failTry = 0;

        voiceLayout = (RelativeLayout) findViewById(R.id.voiceLayout);
        voiceLayout.setVisibility(View.GONE);
        loginTask();

        intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        intent.putExtra(RecognizerIntent.EXTRA_CALLING_PACKAGE, getPackageName());
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, "en-US");

        mRecognizer = SpeechRecognizer.createSpeechRecognizer(this);
        mRecognizer.setRecognitionListener(listener);

        fbLogout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(AccessToken.getCurrentAccessToken() == null) {
                    callbackManager = CallbackManager.Factory.create();
                    LoginManager.getInstance().logInWithReadPermissions(ButtonD.this, Arrays.asList("public_profile", "email", "user_friends"));
                    LoginManager.getInstance().registerCallback(callbackManager, new FacebookCallback<LoginResult>() {
                        @Override
                        public void onSuccess(LoginResult loginResult) {
                            Toast.makeText(ButtonD.this, "Login Success", Toast.LENGTH_SHORT).show();
                            loginTask();
                        }

                        @Override
                        public void onCancel() {
                            Toast.makeText(ButtonD.this, "Login Canceled", Toast.LENGTH_SHORT).show();
                            loginTask();
                        }

                        @Override
                        public void onError(FacebookException error) {
                            Toast.makeText(ButtonD.this, "Login Error", Toast.LENGTH_SHORT).show();
                            loginTask();
                        }
                    });
                }else{
                    if(AccessToken.getCurrentAccessToken() != null){
                        LoginManager.getInstance().logOut();
                        Toast.makeText(ButtonD.this, "로그아웃됨", Toast.LENGTH_SHORT).show();
                    }
                    loginTask();
                }
            }
        });
        voiceButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(ContextCompat.checkSelfPermission(ButtonD.this, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED
                        || ContextCompat.checkSelfPermission(ButtonD.this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED
                        || ContextCompat.checkSelfPermission(ButtonD.this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED){
                    ActivityCompat.requestPermissions(ButtonD.this,
                            new String[]{Manifest.permission.RECORD_AUDIO, Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE}, REQUEST_AUDIO);
                }
                else{
                    doVoiceTask();
                }
            }
        });
    }
    public void doVoiceTask(){
        voiceButton.setVisibility(View.INVISIBLE);
        //String targetStr = testStr[new Random().nextInt(testStr.length)];
        //expectedText.setText(targetStr);
        expectedText.setText("");
        resultText.setText("");
        showStatus.setText("띠링 소리가 나면 말하세요");
        new getProblemTask().execute();
    }
    private class getProblemTask extends AsyncTask<String, Void, String>{
        private String myPath;
        private int wordSuccess, wordAttempt;
        private File createMP3File() throws IOException {
            //String fileName = "tmp_" + String.valueOf(System.currentTimeMillis()) + ".mp3";
            String fileName = "tmp.mp3";
            File storeDir = new File(Environment.getExternalStorageDirectory(), "/tempMP3/");
            if (!storeDir.exists()) storeDir.mkdirs();
            File newFile = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + "/tempMP3/" + fileName);
            myPath = newFile.getAbsolutePath();
            return newFile;
        }
        String sendPost(){
            String url = "http://52.78.17.108:50045/voice/problem/";
            OkHttpClient client = new OkHttpClient();
            Request request =
                    new Request.Builder()
                            .url(url)
                            .build();
            try{
                Response response = client.newCall(request).execute();
                return response.body().string();
            }catch(Exception e){
                e.printStackTrace();
                return null;
            }
        }
        @Override
        protected String doInBackground(String... str){
            String clientId = "onDywWpTHdgQXsXD5U6m";
            String clientSecret = "PnnycmUyHY";
            try {
                String jsonStr = sendPost();
                if(jsonStr == null) return null;
                JSONObject jsonObj = new JSONObject(jsonStr);
                String targetStr = "";
                targetStr = jsonObj.getString("word");
                wordSuccess = jsonObj.getInt("success");
                wordAttempt = jsonObj.getInt("attempt");
                String apiUrl = "https://openapi.naver.com/v1/voice/tts.bin";
                URL url = new URL(apiUrl);
                HttpURLConnection con = (HttpURLConnection) url.openConnection();
                con.setRequestMethod("POST");
                con.setRequestProperty("X-Naver-Client-Id", clientId);
                con.setRequestProperty("X-Naver-Client-Secret", clientSecret);
                String postParams = "speaker=clara&speed=-1&text=" + targetStr;
                con.setDoOutput(true);
                DataOutputStream wr = new DataOutputStream(con.getOutputStream());
                wr.writeBytes(postParams);
                wr.flush();
                wr.close();
                int responseCode = con.getResponseCode();
                System.out.println(responseCode);
                BufferedReader br;
                if(responseCode == 200){
                    InputStream is = con.getInputStream();
                    int read = 0;
                    byte[] bytes = new byte[1024];
                    File mp3File = createMP3File();
                    mp3File.createNewFile();
                    OutputStream outputStream = new FileOutputStream(mp3File);
                    while((read = is.read(bytes)) != -1) {
                        outputStream.write(bytes, 0, read);
                    }
                    is.close();
                    return targetStr;
                }else{
                    return null;
                }
            }catch(Exception e){
                e.printStackTrace();
                return null;
            }
        }
        @Override
        protected void onPostExecute(String res){
            if(res == null) return;
            super.onPostExecute(res);
            System.out.println(res);
            try {
                showStatus.setText(wordAttempt + "명 중 " + wordSuccess + "명이 성공한 단어");
                expectedText.setText(res);
                resultText.setText("");
                MediaPlayer audioPlay = new MediaPlayer();
                audioPlay.setDataSource(myPath);
                audioPlay.prepare();
                audioPlay.start();
                SystemClock.sleep(2000);
                mRecognizer.startListening(intent);
            }catch(Exception e){
                e.printStackTrace();
            }
        }
    };
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults){
        switch (requestCode){
            case REQUEST_AUDIO:
                if(grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED
                        && grantResults[1] == PackageManager.PERMISSION_GRANTED
                        && grantResults[2] == PackageManager.PERMISSION_GRANTED){
                    doVoiceTask();
                }
                else{
                    Toast.makeText(getApplicationContext(),"Audio Permission denied",Toast.LENGTH_LONG).show();
                }
                return;
        }
    }
    private class sendResultTask extends AsyncTask<String, Void, Integer>{
        private final MediaType JSON = MediaType.parse("application/json; charset=utf-8");
        String makeJson(String expected, String result){
            return "{\"uid\" : \"" + userId + "\"," +
                    "\"expected\" : \"" + expected + "\"," +
                    "\"result\" : \"" + result + "\"}";
        }
        String sendPost(String url, String jsonStr){
            OkHttpClient client = new OkHttpClient();
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
                return null;
            }
        }
        @Override
        protected Integer doInBackground(String... str){
            try {
                String jsonStr = makeJson(str[0], str[1]);
                String url = "http://52.78.17.108:50045/voice/result/update/";
                String result = sendPost(url, jsonStr);
                System.out.println(result);
                if(result == null) return null;
                else{
                    JSONObject jsonObj = new JSONObject(result);
                    return jsonObj.getInt("ok");
                }
            }catch(Exception e){
                e.printStackTrace();
                return null;
            }
        }
        @Override
        protected void onPostExecute(Integer res){
            if(res == null) {
                System.out.println("Server Error");
                return;
            }
            System.out.println("OK MAN!");
            super.onPostExecute(res);
            attempt += 1; success += res;
            profileResult.setText(success + " Success / " + attempt + " Attempt");
            showStatus.setText("아래 마이크 버튼을 누르세요");
            voiceButton.setVisibility(View.VISIBLE);
        }
    };
    private RecognitionListener listener = new RecognitionListener() {
        @Override
        public void onReadyForSpeech(Bundle params) {
        }
        @Override
        public void onBeginningOfSpeech() {
        }
        @Override
        public void onRmsChanged(float rmsdB) {
        }
        @Override
        public void onBufferReceived(byte[] buffer) {
        }
        @Override
        public void onEndOfSpeech() {
        }
        @Override
        public void onError(int error) {
            if(error == mRecognizer.ERROR_NETWORK_TIMEOUT){
                Toast.makeText(getApplicationContext(),"네트워크 타임아웃 에러",Toast.LENGTH_SHORT).show();
                voiceButton.setVisibility(View.VISIBLE);
            }
            else if(error == mRecognizer.ERROR_NETWORK){
                Toast.makeText(getApplicationContext(),"네트워크 에러",Toast.LENGTH_SHORT).show();
                voiceButton.setVisibility(View.VISIBLE);
            }

            else if(error == mRecognizer.ERROR_AUDIO){
                Toast.makeText(getApplicationContext(),"녹음 에러",Toast.LENGTH_SHORT).show();
                voiceButton.setVisibility(View.VISIBLE);
            }

            else if(error == mRecognizer.ERROR_SERVER){
                Toast.makeText(getApplicationContext(),"서버 에러",Toast.LENGTH_SHORT).show();
                voiceButton.setVisibility(View.VISIBLE);
            }

            else if(error == mRecognizer.ERROR_CLIENT){
                Toast.makeText(getApplicationContext(),"클라이언트 에러",Toast.LENGTH_SHORT).show();
                voiceButton.setVisibility(View.VISIBLE);
            }
            else if(error == mRecognizer.ERROR_SPEECH_TIMEOUT){
                Toast.makeText(getApplicationContext(),"No - 이해 불가",Toast.LENGTH_SHORT).show();
                resultText.setText("Can't Understand");
                failTry++;
                new sendResultTask().execute(expectedText.getText().toString().trim().toLowerCase(), "NO_ANSWER");
            }
            else if(error == mRecognizer.ERROR_NO_MATCH){
                Toast.makeText(getApplicationContext(),"No - 이해 불가",Toast.LENGTH_SHORT).show();
                resultText.setText("Can't Understand");
                failTry++;
                new sendResultTask().execute(expectedText.getText().toString().trim().toLowerCase(), "NO_ANSWER");
            }
            else if(error == mRecognizer.ERROR_RECOGNIZER_BUSY){
                Toast.makeText(getApplicationContext(),"인스턴스가 바쁨",Toast.LENGTH_SHORT).show();
                voiceButton.setVisibility(View.VISIBLE);
            }
        }
        @Override
        public void onResults(Bundle results) {
            String key = "";
            key = SpeechRecognizer.RESULTS_RECOGNITION;
            ArrayList<String> mResult = results.getStringArrayList(key);
            String[] rs = new String[mResult.size()];
            System.out.println(rs);
            mResult.toArray(rs);
            resultText.setText(""+rs[0]);
            if(rs[0].trim().toLowerCase().compareTo(expectedText.getText().toString().trim().toLowerCase()) == 0){
                successTry++;
                Toast.makeText(getApplicationContext(),"Yes - 정답!",Toast.LENGTH_SHORT).show();
            }else{
                failTry++;
                Toast.makeText(getApplicationContext(),"No - 틀렸습니다",Toast.LENGTH_SHORT).show();
            }
            new sendResultTask().execute(expectedText.getText().toString().trim().toLowerCase(), rs[0].trim().toLowerCase());
            //showStatus.setText((successTry + failTry) + "개 중 " + successTry + "개 성공");
        }
        @Override
        public void onPartialResults(Bundle partialResults) {
        }
        @Override
        public void onEvent(int eventType, Bundle params) {
        }
    };
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        callbackManager.onActivityResult(requestCode, resultCode, data);
    }
}
