package com.example.secondapplication;

import android.graphics.Bitmap;

/**
 * Created by q on 2017-07-08.
 */

public class ContactInfo {
    private String name;
    private String phone;
    private String email;
    private String picUrl;
    private Bitmap picThumb;
    ContactInfo(String _name, String _phone, String _email, String _picUrl){
        name = _name; phone = _phone; email = _email; picUrl = _picUrl; picThumb = null;
    }
    public String getName(){ return name; }
    public String getPhone(){ return phone; }
    public String getEmail(){ return email; }
    public String getPicUrl(){ return picUrl; }
    public Bitmap getThumb(){ return picThumb; }
    public void setThumb(Bitmap bm){
        picThumb = bm;
    }
}
