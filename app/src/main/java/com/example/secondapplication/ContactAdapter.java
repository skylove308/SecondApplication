package com.example.secondapplication;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.ShapeDrawable;
import android.graphics.drawable.shapes.OvalShape;
import android.os.AsyncTask;
import android.util.Base64;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

/**
 * Created by q on 2017-07-08.
 */

public class ContactAdapter extends BaseAdapter{
    private ArrayList<ContactInfo> contactList = new ArrayList<ContactInfo>();
    private ArrayList<ContactInfo> facebookList = new ArrayList<ContactInfo>();
    private ArrayList<ContactInfo> phoneList = new ArrayList<ContactInfo>();
    private ArrayList<ContactInfo> customList = new ArrayList<ContactInfo>();
    private ArrayList<ContactInfo> searchList = new ArrayList<ContactInfo>();
    private int imgMetric;
    private int showMode = 0;
    // 0: all, 1:facebook, 2:phone, 3:custom
    private boolean search = false;
    private String searchStr = "";

    public ArrayList<ContactInfo> getContactList(){return contactList;}
    public ArrayList<ContactInfo> getFacebookList(){return facebookList;}
    public ArrayList<ContactInfo> getPhoneList(){return phoneList;}
    public ArrayList<ContactInfo> getCustomList(){return customList;}
    public int getShowMode(){return showMode;}
    public boolean getSearchMode(){return search;}

    public void setContactList(ArrayList<ContactInfo> list){contactList = list;}
    public void setFacebookList(ArrayList<ContactInfo> list){facebookList = list;}
    public void setPhoneList(ArrayList<ContactInfo> list){phoneList = list;}
    public void setCustomList(ArrayList<ContactInfo> list){customList = list;}

    Comparator<ContactInfo> cmpAsc = new Comparator<ContactInfo>() {
        @Override
        public int compare(ContactInfo o1, ContactInfo o2) {
            return (o1.getName()).compareTo(o2.getName());
        }
    };

    public ContactAdapter(int imgSz){
        imgMetric = imgSz;
    }
    @Override
    public int getCount(){
        if(search) return searchList.size();
        return contactList.size();
    }
    @Override
    public long getItemId(int position){
        return position;
    }
    @Override
    public Object getItem(int position){
        if(search) return searchList.get(position);
        return contactList.get(position);
    }

    public void changeSearchMode(boolean newMode, String newStr){
        search = newMode;
        searchStr = newStr;
        searchList.clear();
        notifyDataSetChanged();
        for(int i=0;i<contactList.size();i++){
            ContactInfo target = contactList.get(i);
            if(target.can(searchStr)){
                searchList.add(target);
                notifyDataSetChanged();
            }
        }
    }

    public void changeShowMode(int newMode){
        contactList.clear();
        this.notifyDataSetChanged();
        showMode = newMode;
        if(showMode == 0 || showMode == 1){
            for(int i=0;i<facebookList.size();i++){
                contactList.add(facebookList.get(i));
                if(!search) this.notifyDataSetChanged();
            }
        }
        if(showMode == 0 || showMode == 2){
            for(int i=0;i<phoneList.size();i++){
                contactList.add(phoneList.get(i));
                if(!search) this.notifyDataSetChanged();
            }
        }
        if(showMode == 0 || showMode == 3){
            for(int i=0;i<customList.size();i++){
                contactList.add(customList.get(i));
                if(!search) notifyDataSetChanged();
            }
        }
        sortItem();
        notifyDataSetChanged();
        if(search) changeSearchMode(search, searchStr);
    }
    public void addItem(int mode, String name, String email, String phone, String picUrl, String uniqueId){
        ContactInfo newContact = new ContactInfo(mode, name, email, phone, picUrl, uniqueId);
        if(mode == 1) facebookList.add(newContact);
        if(mode == 2) phoneList.add(newContact);
        if(mode == 3) customList.add(newContact);
        if(showMode == mode || showMode == 0){
            contactList.add(newContact);
            notifyDataSetChanged();
            sortItem();
            if(search) changeSearchMode(search, searchStr);
        }
    }
    public void editItem(int mode, String name, String email, String phone, String picUrl, String uniqueId){
        if(mode == 3){
            System.out.println(name + " " + email + " " + phone);
            for(int i=0;i<customList.size();i++){
                if(customList.get(i).getUniqueId().compareTo(uniqueId) == 0){
                    customList.set(i, new ContactInfo(mode, name, email, phone, picUrl, uniqueId));
                    break;
                }
            }
            if(showMode == mode || showMode == 0){
                System.out.println("upd: "+name + " " + email + " " + phone);
                for(int i=0;i<contactList.size();i++){
                    if(contactList.get(i).getUniqueId().compareTo(uniqueId) == 0){
                        contactList.set(i, new ContactInfo(mode, name, email, phone, picUrl, uniqueId));
                        break;
                    }
                }
                notifyDataSetChanged();
                sortItem();
                if(search) changeSearchMode(search, searchStr);
            }
        }
    }
    private class deleteContactTask extends AsyncTask<String, String, String>{
        private final MediaType JSON = MediaType.parse("application/json; charset=utf-8");
        private String uniqueId;
        String sendPost(String uid){
            OkHttpClient client = new OkHttpClient();
            RequestBody body = RequestBody.create(JSON, "{\"contactId\": \"" + uniqueId + "\"}");
            Request request =
                    new Request.Builder()
                            .url("http://52.78.17.108:50045/contact/delete/" + uid + "/custom/")
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
        public deleteContactTask(String _uniqueId){
            uniqueId = _uniqueId;
        }
        @Override
        protected String doInBackground(String... url){
            return sendPost(url[0]);
        }
        @Override
        protected void onPostExecute(String data){
            if(data != null){
                super.onPostExecute(data);
                System.out.println(data);
                for(int i=0;i<customList.size();i++){
                    if(customList.get(i).getUniqueId().compareTo(uniqueId) == 0){
                        customList.remove(i);
                        break;
                    }
                }
                if(showMode == 3 || showMode == 0){
                    for(int i=0;i<contactList.size();i++){
                        if(contactList.get(i).getUniqueId().compareTo(uniqueId) == 0){
                            contactList.remove(i);
                            break;
                        }
                    }
                    notifyDataSetChanged();
                    sortItem();
                    if(search) changeSearchMode(search, searchStr);
                }
            }
        }
    };
    public void deleteItem(int mode, String uid, String uniqueId){
        if(mode == 3){
            new deleteContactTask(uniqueId).execute(uid);
        }
    }
    public void deleteAll(int mode){
        if(mode == 1) facebookList.clear();
        if(mode == 2) phoneList.clear();
        if(mode == 3) customList.clear();
        changeShowMode(showMode);
        if(search) changeSearchMode(search, searchStr);
    }
    public void sortItem() {
        Collections.sort(contactList, cmpAsc);
        notifyDataSetChanged();
    }

    private void showItem(ImageView imageView, TextView textView, ContactInfo nowInfo){
        int[] chooseColor = {R.drawable.ic_account_circle_gray_24dp, R.drawable.ic_account_circle_green_24dp, R.drawable.ic_account_circle_orange_24dp};
        textView.setText(nowInfo.getName());
        imageView.setLayoutParams(new LinearLayout.LayoutParams((imgMetric * 14 / 10), (imgMetric * 11 / 10)));
        imageView.setScaleType(ImageView.ScaleType.FIT_XY);
        imageView.setPadding(10 + (imgMetric * 3 / 10), 10, 10, 10);
        Bitmap userBitmap = nowInfo.getThumb();
        if(nowInfo.getMode() == 3) {
            String userStr = nowInfo.getPicUrl();
            if(userStr.length() == 0){
                imageView.setImageResource(chooseColor[Math.abs((nowInfo.getName() + nowInfo.getPhone()).hashCode()) % chooseColor.length]);
            }else{
                imgDownloadTask downTask = new imgDownloadTask(imageView, nowInfo);
                downTask.execute(userStr);
            }
        }else {
            if (userBitmap == null && nowInfo.getPicUrl().compareTo("") != 0) {
                imgDownloadTask downTask = new imgDownloadTask(imageView, nowInfo);
                downTask.execute(nowInfo.getPicUrl());
            } else {
                if (userBitmap != null) {
                    imageView.setImageBitmap(userBitmap);
                } else {
                    imageView.setImageResource(chooseColor[Math.abs((nowInfo.getName() + nowInfo.getPhone()).hashCode()) % chooseColor.length]);
                }
            }
        }
    }
    @Override
    public View getView(int position, View convertView, ViewGroup parent){
        final Context cxt = parent.getContext();
        if(convertView == null){
            LayoutInflater inflater = (LayoutInflater) cxt.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = inflater.inflate(R.layout.single_contact, parent, false);
        }
        ImageView imageView = (ImageView) convertView.findViewById(R.id.contactImage);
        TextView textView = (TextView) convertView.findViewById(R.id.contactName);
        try {
            ContactInfo nowInfo = (ContactInfo) getItem(position);
            showItem(imageView, textView, nowInfo);
        }catch(Exception e){
            e.printStackTrace();
        }
        return convertView;
    }
    class imgDownloadTask extends AsyncTask<String, Void, Bitmap> {
        private ImageView iv;
        private ContactInfo nowInfo;
        imgDownloadTask(ImageView targetIv, ContactInfo nwInfo) {
            iv = targetIv;
            nowInfo = nwInfo;
        }
        @Override
        protected void onPreExecute() {
            iv.setImageResource(R.drawable.ic_image_black_24dp);
            super.onPreExecute();
        }
        private Bitmap downloadUrl(String imgUrl) throws IOException {
            if(nowInfo.getMode() == 3){
                byte[] decodedByte = Base64.decode(imgUrl, 0);
                Bitmap bitmap = BitmapFactory.decodeByteArray(decodedByte, 0, decodedByte.length);
                nowInfo.setThumb(bitmap);
                return bitmap;
            }else {
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
                    System.out.println("GET 200 OK / " + imgUrl);
                }
                nowInfo.setThumb(bitmap);
                return bitmap;
            }
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
                iv.invalidate();
            }
        }
    }
}
