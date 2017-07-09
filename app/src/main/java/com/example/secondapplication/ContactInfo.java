package com.example.secondapplication;

import android.graphics.Bitmap;
import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by q on 2017-07-08.
 */

public class ContactInfo implements Parcelable{
    private String name;
    private String phone;
    private String email;
    private String picUrl;
    private Bitmap picThumb;
    private int mode;

    ContactInfo(int _mode, String _name, String _email, String _phone, String _picUrl){
        mode = _mode; name = _name; phone = _phone; email = _email; picUrl = _picUrl; picThumb = null;
    }

    public int getMode(){ return mode; }
    public String getName(){ return name; }
    public String getPhone(){ return phone; }
    public String getEmail(){ return email; }
    public String getPicUrl(){ return picUrl; }

    public Bitmap getThumb(){ return picThumb; }
    public void setThumb(Bitmap bm){
        picThumb = bm;
    }

    @Override
    public int describeContents(){
        return 0;
    }
    @Override
    public void writeToParcel(Parcel parcel, int flags){
        parcel.writeInt(mode);
        parcel.writeString(name);
        parcel.writeString(phone);
        parcel.writeString(email);
        parcel.writeString(picUrl);
        //parcel.writeParcelable(picThumb, flags);
    }
    public static final Parcelable.Creator<ContactInfo> CREATOR = new Creator<ContactInfo>() {
        @Override
        public ContactInfo createFromParcel(Parcel source) {
            int _mode = source.readInt();
            String _name = source.readString();
            String _phone = source.readString();
            String _email = source.readString();
            String _picUrl = source.readString();
            ContactInfo cInfo = new ContactInfo(_mode, _name, _phone, _email, _picUrl);
            //cInfo.picThumb = source.readParcelable(Bitmap.class.getClassLoader());
            return cInfo;
        }
        @Override
        public ContactInfo[] newArray(int size) {
            return new ContactInfo[size];
        }
    };
}
