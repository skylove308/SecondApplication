package com.example.secondapplication;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.ShapeDrawable;
import android.graphics.drawable.shapes.OvalShape;
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
    private int showMode = 0;
    // 0: all, 1:facebook, 2:phone, 3:custom

    public ArrayList<ContactInfo> getContactList(){return contactList;}
    public ArrayList<ContactInfo> getFacebookList(){return facebookList;}
    public ArrayList<ContactInfo> getPhoneList(){return phoneList;}
    public ArrayList<ContactInfo> getCustomList(){return customList;}
    public int getShowMode(){return showMode;}

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

    public void changeShowMode(int newMode){
        contactList.clear();
        this.notifyDataSetChanged();
        showMode = newMode;
        if(showMode == 0 || showMode == 1){
            for(int i=0;i<facebookList.size();i++){
                contactList.add(facebookList.get(i));
                this.notifyDataSetChanged();
            }
        }
        if(showMode == 0 || showMode == 2){
            for(int i=0;i<phoneList.size();i++){
                contactList.add(phoneList.get(i));
                this.notifyDataSetChanged();
            }
        }
        if(showMode == 0 || showMode == 3){
            for(int i=0;i<customList.size();i++){
                contactList.add(customList.get(i));
                this.notifyDataSetChanged();
            }
        }
        sortItem();
        this.notifyDataSetChanged();
    }
    public void addItem(int mode, String name, String email, String phone, String picUrl){
        ContactInfo newContact = new ContactInfo(name, email, phone, picUrl);
        if(mode == 1) facebookList.add(newContact);
        if(mode == 2) phoneList.add(newContact);
        if(mode == 3) customList.add(newContact);
        if(showMode == mode || showMode == 0){
            contactList.add(newContact);
            this.notifyDataSetChanged();
            sortItem();
        }
    }
    public void deleteAll(int mode){
        if(mode == 1) facebookList.clear();
        if(mode == 2) phoneList.clear();
        if(mode == 3) customList.clear();
        changeShowMode(showMode);
    }
    public void sortItem() {
        Collections.sort(contactList, cmpAsc);
        this.notifyDataSetChanged();
    }

    private void showItem(ImageView imageView, TextView textView, ContactInfo nowInfo){
        int[] chooseColor = {R.drawable.ic_account_circle_black_24dp, R.drawable.ic_account_circle_green_24dp, R.drawable.ic_account_circle_orange_24dp};
        textView.setText(nowInfo.getName());
        imageView.setLayoutParams(new LinearLayout.LayoutParams((imgMetric * 14 / 10), (imgMetric * 11 / 10)));
        imageView.setScaleType(ImageView.ScaleType.FIT_XY);
        imageView.setPadding(10 + (imgMetric * 3 / 10), 10, 10, 10);
        Bitmap userBitmap = nowInfo.getThumb();
        if(userBitmap == null && nowInfo.getPicUrl().compareTo("") != 0) {
            imgDownloadTask downTask = new imgDownloadTask(imageView, nowInfo);
            downTask.execute(nowInfo.getPicUrl());
        }else{
            if(userBitmap != null){
                imageView.setImageBitmap(userBitmap);
            }else{
                imageView.setImageResource(chooseColor[Math.abs((nowInfo.getName()+nowInfo.getPhone()).hashCode())%3]);
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
