package com.example.tracker;

import java.io.Serializable;

/**
 * Created by 柏宏 on 2016/12/15.
 */

public class Detail implements Serializable{
    String name;
    String PhoneNum;
    String Lat;
    String Lng;

    public String getName(){
        return this.name;
    }
    public void setName(String name){
        this.name = name;
    }
    public String getPhoneNum(){
        return this.PhoneNum;
    }
    public void setPhoneNum(String phoneNum) {
        this.PhoneNum = phoneNum;
    }
    public String getLat(){
        return this.Lat;
    }
    public void setLat(String Lat){
        this.Lat = Lat;
    }
    public String getLng(){
        return this.Lng;
    }
    public void setLng(String Lng){
        this.Lng = Lng;
    }
}
