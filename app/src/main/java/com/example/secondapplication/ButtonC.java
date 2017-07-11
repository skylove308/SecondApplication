package com.example.secondapplication;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.nfc.Tag;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.MediaStore;
import android.speech.RecognitionListener;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
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
import java.util.ArrayList;
import java.util.Date;

import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

/**
 * Created by q on 2017-07-10.
 */

public class ButtonC extends AppCompatActivity {
    Intent intent;
    SpeechRecognizer mRecognizer;
    TextView txv;
    TextView problem;
    final int REQUEST_AUDIO = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_button_c);

        Button btn = (Button) findViewById(R.id.listen);
        ImageButton ib = (ImageButton) findViewById(R.id.mike);
        problem = (TextView) findViewById(R.id.problem);
        problem.setText("hello");
        txv = (TextView) findViewById(R.id.answer);
        final Activity activity_c = this;
        intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        intent.putExtra(RecognizerIntent.EXTRA_CALLING_PACKAGE, getPackageName());
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, "en-US");
        mRecognizer = SpeechRecognizer.createSpeechRecognizer(this);
        mRecognizer.setRecognitionListener(listener);

        final Context mContext = this;

        ib.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(ContextCompat.checkSelfPermission(activity_c, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED){
                    if(ActivityCompat.shouldShowRequestPermissionRationale(activity_c, Manifest.permission.RECORD_AUDIO)){

                    }
                    else{
                        ActivityCompat.requestPermissions(activity_c, new String[]{Manifest.permission.RECORD_AUDIO}, REQUEST_AUDIO);
                    }
                }
                else{
                    mRecognizer.startListening(intent);
                }
            }
        });

        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try{
                    String apiURL = "https://openapi.naver.com/v1/voice/tts.bin";
                    URL url = new URL(apiURL);
                    String str = problem.getText().toString();
                    String urltoString = url.toString();
                    new postAPI().execute(urltoString, str);
                }
                catch(Exception e){
                    Toast.makeText(mContext, ""+e, Toast.LENGTH_LONG).show();
                }
            }
        });
    }

    private class postAPI extends AsyncTask <String, Void, InputStream> {
        @Override
        protected InputStream doInBackground(String... strs) {
            String clientId = "yOEwQnVHqevl28ryDqY1";
            String clientSecret = "bC17RXc5Um";
            try {
                URL url = new URL(strs[0]);
                HttpURLConnection con = (HttpURLConnection) url.openConnection();
                con.setRequestMethod("POST");
                con.setRequestProperty("X-Naver-Client-Id", clientId);
                con.setRequestProperty("X-Naver-Client-Secret", clientSecret);
                String postParams = "speaker=mijin&speed=0&text=" + strs[1];
                con.setDoOutput(true);
                DataOutputStream wr = new DataOutputStream(con.getOutputStream());
                wr.writeBytes(postParams);
                wr.flush();
                wr.close();
                int responseCode = con.getResponseCode();
                BufferedReader br;
                if (responseCode == 200) { // 정상 호출
                    InputStream is = con.getInputStream();
                    return is;
                } else {  // 에러 발생
                    return null;
                }
            }
            catch(Exception e){
                    return null;
            }
        }

        @Override
        protected void onPostExecute(InputStream res){
            if(res != null) {
                try{
                    int read = 0;
                    byte[] bytes = new byte[1024];
                    String tempname = Long.valueOf(new Date().getTime()).toString();
                    File f = new File(tempname + ".mp3");
                    f.createNewFile();
                    OutputStream outputStream = new FileOutputStream(f);
                    while((read = res.read(bytes)) != -1){
                        outputStream.write(bytes, 0, read);
                    }
                    res.close();

                }
                catch(Exception e){

                }
                Toast.makeText(ButtonC.this, ""+res, Toast.LENGTH_LONG).show();
            }
            else{
                Toast.makeText(ButtonC.this, ""+res, Toast.LENGTH_LONG).show();
            }
        }
    }


    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults){
        switch (requestCode){
            case REQUEST_AUDIO:
                if(grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED){
                    mRecognizer.startListening(intent);
                }
                else{
                    Toast.makeText(getApplicationContext(),"Audio Permission denied",Toast.LENGTH_LONG).show();
                }
                return;
        }
    }

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
            }
            else if(error == mRecognizer.ERROR_NETWORK){
                Toast.makeText(getApplicationContext(),"네트워크 에러",Toast.LENGTH_SHORT).show();
            }

            else if(error == mRecognizer.ERROR_AUDIO){
                Toast.makeText(getApplicationContext(),"녹음 에러",Toast.LENGTH_SHORT).show();
            }

            else if(error == mRecognizer.ERROR_SERVER){
                Toast.makeText(getApplicationContext(),"서버 에러",Toast.LENGTH_SHORT).show();
            }

            else if(error == mRecognizer.ERROR_CLIENT){
                Toast.makeText(getApplicationContext(),"클라이언트 에러",Toast.LENGTH_SHORT).show();
            }
            else if(error == mRecognizer.ERROR_SPEECH_TIMEOUT){
                Toast.makeText(getApplicationContext(),"아무 음성도 듣지 못함",Toast.LENGTH_SHORT).show();
            }
            else if(error == mRecognizer.ERROR_NO_MATCH){
                Toast.makeText(getApplicationContext(),"적당한 결과를 찾지 못함",Toast.LENGTH_SHORT).show();
            }
            else if(error == mRecognizer.ERROR_RECOGNIZER_BUSY){
                Toast.makeText(getApplicationContext(),"인스턴스가 바쁨",Toast.LENGTH_SHORT).show();
            }
        }
        @Override
        public void onResults(Bundle results) {
            String key = "";
            key = SpeechRecognizer.RESULTS_RECOGNITION;
            ArrayList<String> mResult = results.getStringArrayList(key);
            String[] rs = new String[mResult.size()];
            mResult.toArray(rs);
            txv.setText(""+rs[0]);
        }
        @Override
        public void onPartialResults(Bundle partialResults) {
        }
        @Override
        public void onEvent(int eventType, Bundle params) {
        }
    };
}

