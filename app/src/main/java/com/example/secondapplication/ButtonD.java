package com.example.secondapplication;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
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
import android.widget.TextView;
import android.widget.Toast;

import com.facebook.AccessToken;
import com.facebook.GraphRequest;
import com.facebook.GraphResponse;
import com.facebook.HttpMethod;

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
import java.util.Random;

public class ButtonD extends AppCompatActivity {
    private Button voiceButton;
    private TextView resultText, expectedText, showStatus;
    private SpeechRecognizer mRecognizer;
    final int REQUEST_AUDIO = 1;
    private Intent intent;
    private int successTry, failTry;
    final String[] testStr = {"Worcestershire", "Specific", "Squirrel", "Brewery", "Phenomenon", "Derby", "Regularly", "February", "Edited", "Heir"};
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_button_d);
        voiceButton = (Button) findViewById(R.id.voiceButton);
        resultText = (TextView) findViewById(R.id.resultText);
        expectedText = (TextView) findViewById(R.id.expectedText);
        showStatus = (TextView) findViewById(R.id.showStatus);
        successTry = 0;
        failTry = 0;
        showStatus.setText("0개 중 0개 성공");
        new GraphRequest(
                AccessToken.getCurrentAccessToken(),
                "/me/?fields=id,name,email,picture.type(large)&locale=ko_KR",
                null,
                HttpMethod.GET,
                new GraphRequest.Callback() {
                    public void onCompleted(GraphResponse response) {
                        try {
                            System.out.println(response);
                            String id = response.getJSONObject().getString("id");
                            String email = response.getJSONObject().getString("email");
                            String name = response.getJSONObject().getString("name");
                            String picture = response.getJSONObject().getJSONObject("picture").getJSONObject("data").getString("url");
                            System.out.println(id);
                        }catch(Exception e){
                            e.printStackTrace();
                        }
                    }
                }
        ).executeAsync();
        intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        intent.putExtra(RecognizerIntent.EXTRA_CALLING_PACKAGE, getPackageName());
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, "en-US");

        mRecognizer = SpeechRecognizer.createSpeechRecognizer(this);
        mRecognizer.setRecognitionListener(listener);

        voiceButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(ContextCompat.checkSelfPermission(ButtonD.this, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED){
                    if(ActivityCompat.shouldShowRequestPermissionRationale(ButtonD.this, Manifest.permission.RECORD_AUDIO)){

                    }
                    else{
                        ActivityCompat.requestPermissions(ButtonD.this, new String[]{Manifest.permission.RECORD_AUDIO}, REQUEST_AUDIO);
                    }
                }
                else{
                    String targetStr = testStr[new Random().nextInt(testStr.length)];
                    expectedText.setText(targetStr);
                    resultText.setText("");
                    new getSoundTask().execute(targetStr);
                }
            }
        });
    }
    private class getSoundTask extends AsyncTask<String, Void, String>{
        private String myPath;
        private File createMP3File() throws IOException {
            //String fileName = "tmp_" + String.valueOf(System.currentTimeMillis()) + ".mp3";
            String fileName = "tmp.mp3";
            File storeDir = new File(Environment.getExternalStorageDirectory(), "/tempMP3/");
            if (!storeDir.exists()) storeDir.mkdirs();
            File newFile = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + "/tempMP3/" + fileName);
            myPath = newFile.getAbsolutePath();
            return newFile;
        }
        @Override
        protected String doInBackground(String... str){
            String clientId = "onDywWpTHdgQXsXD5U6m";
            String clientSecret = "PnnycmUyHY";
            try {
                String apiUrl = "https://openapi.naver.com/v1/voice/tts.bin";
                URL url = new URL(apiUrl);
                HttpURLConnection con = (HttpURLConnection) url.openConnection();
                con.setRequestMethod("POST");
                con.setRequestProperty("X-Naver-Client-Id", clientId);
                con.setRequestProperty("X-Naver-Client-Secret", clientSecret);
                String postParams = "speaker=matt&speed=0&text=" + str[0];
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
                    return str[0];
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
                resultText.setText("Can't Understand");
                failTry++;
            }
            else if(error == mRecognizer.ERROR_NO_MATCH){
                resultText.setText("Can't Understand");
                failTry++;
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
            System.out.println(rs);
            mResult.toArray(rs);
            resultText.setText(""+rs[0]);
            if(rs[0].trim().toLowerCase().compareTo(expectedText.getText().toString().trim().toLowerCase()) == 0){
                successTry++;
            }else{
                failTry++;
            }
            showStatus.setText((successTry + failTry) + "개 중 " + successTry + "개 성공");
        }
        @Override
        public void onPartialResults(Bundle partialResults) {
        }
        @Override
        public void onEvent(int eventType, Bundle params) {
        }
    };
}
