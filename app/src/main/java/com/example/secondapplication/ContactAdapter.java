package com.example.secondapplication;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
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

/**
 * Created by q on 2017-07-08.
 */

public class ContactAdapter extends BaseAdapter{
    private ArrayList<ContactInfo> contactList = new ArrayList<ContactInfo>();
    private ArrayList<ContactInfo> facebookList = new ArrayList<ContactInfo>();
    private ArrayList<ContactInfo> phoneList = new ArrayList<ContactInfo>();
    private ArrayList<ContactInfo> customList = new ArrayList<ContactInfo>();

    private int imgMetric;

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
        return contactList.size();
    }
    @Override
    public long getItemId(int position){
        return position;
    }
    @Override
    public Object getItem(int position){
        return contactList.get(position);
    }

    public void addItem(String name, String email, String phone, String picUrl){
        ContactInfo newContact = new ContactInfo(name, email, phone, picUrl);
        contactList.add(newContact);
    }
    public void deleteAll(){
        contactList.clear();
    }
    public void sortItem() {
        Collections.sort(contactList, cmpAsc);
    }

    private void showItem(ImageView imageView, TextView textView, ContactInfo nowInfo){
        textView.setText(nowInfo.getName());
        imageView.setLayoutParams(new LinearLayout.LayoutParams((imgMetric * 14 / 10), (imgMetric * 11 / 10)));
        imageView.setScaleType(ImageView.ScaleType.FIT_XY);
        imageView.setPadding(10 + (imgMetric * 3 / 10), 10, 10, 10);
        Bitmap userBitmap = nowInfo.getThumb();
        if(userBitmap == null) {
            imgDownloadTask downTask = new imgDownloadTask(imageView, nowInfo);
            downTask.execute(nowInfo.getPicUrl());
        }else{
            imageView.setImageBitmap(userBitmap);
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
        ContactInfo nowInfo = (ContactInfo) getItem(position);
        showItem(imageView, textView, nowInfo);
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
                System.out.println("GET 200 OK / " + imgUrl);
            }
            nowInfo.setThumb(bitmap);
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
                iv.invalidate();
            }
        }
    }
}
