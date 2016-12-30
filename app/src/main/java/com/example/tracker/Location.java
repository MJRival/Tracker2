package com.example.tracker;

import android.content.Context;
import android.graphics.Typeface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

import com.baidu.mapapi.map.BaiduMap;
import com.baidu.mapapi.map.BitmapDescriptor;
import com.baidu.mapapi.map.BitmapDescriptorFactory;
import com.baidu.mapapi.map.MarkerOptions;
import com.baidu.mapapi.map.OverlayOptions;
import com.baidu.mapapi.map.PolylineOptions;
import com.baidu.mapapi.map.TextOptions;
import com.baidu.mapapi.model.LatLng;
import com.baidu.mapapi.utils.DistanceUtil;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by 柏宏 on 2016/12/15.
 */

public class Location {
    public LatLng myLoc;
    public LatLng tarLoc;
    public Bundle exDetail;
    public OverlayOptions options;
    public OverlayOptions object;
    public OverlayOptions line;
    public OverlayOptions object_distance;
    public BitmapDescriptor locate;
    public Context context;

    Location(Context context){
        this.context = context;
    }

    public BaiduMap location(BaiduMap mBaiduMap, BitmapDescriptor bitmapDescriptor, Double lat, Double lng, List<Detail> detailList,String match){
        exDetail = new Bundle();
        String name;
        String PhoneNum;
        String Lat;
        String Lng;
        myLoc = new LatLng(lat,lng);
        for(Detail target:detailList){
            name = target.getName();
            PhoneNum = target.getPhoneNum();
            Lat = target.getLat();
            Lng = target.getLng();
            if (Lat.equals("")||Lng.equals(""))
                continue;
            tarLoc = new LatLng(Double.valueOf(Lat),Double.valueOf(Lng));
            exDetail.putString("name",name);
            exDetail.putString("PhoneNum",PhoneNum);
            exDetail.putString("Lat",Lat);
            exDetail.putString("Lng",Lng);

            if (match.equals("FRIEND")){
                LayoutInflater display =(LayoutInflater) context
                        .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                View EntryView = display.inflate(R.layout.name_phone,null);
                TextView Name = (TextView)EntryView.findViewById(R.id.name);
                TextView Phonenum = (TextView)EntryView.findViewById(R.id.PhoneNum);
                Name.setText(name);
                Name.setTextColor(context.getResources().getColor(R.color.Green));
                Phonenum.setText(PhoneNum);
                locate = BitmapDescriptorFactory.fromView(EntryView);
                object = new MarkerOptions().position(tarLoc).icon(locate).extraInfo(exDetail);

                List<LatLng> points = new ArrayList<LatLng>();
                points.add(myLoc);
                points.add(tarLoc);
                line = new PolylineOptions().width(15).color(0xAAFF0000).points(points);
                mBaiduMap.addOverlay(line);
            }else if(match.equals("ENEMY")){
                LayoutInflater display =(LayoutInflater) context
                        .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                View EntryView = display.inflate(R.layout.name_phone,null);
                TextView Name =(TextView)EntryView.findViewById(R.id.name);
                TextView Phonenum = (TextView)EntryView.findViewById(R.id.PhoneNum);
                Name.setText(name);
                Name.setTextColor(context.getResources().getColor(R.color.Red));
                Phonenum.setText(PhoneNum);
                locate = BitmapDescriptorFactory.fromView(EntryView);
                object = new MarkerOptions().position(tarLoc).icon(locate).extraInfo(exDetail);
                mBaiduMap.addOverlay(object);

                List<LatLng> points = new ArrayList<LatLng>();
                points.add(myLoc);
                points.add(tarLoc);
                line = new PolylineOptions().width(15).color(0xAAFF0000).points(points);
                mBaiduMap.addOverlay(line);
            }

            //添加定位图标
            options = new MarkerOptions().position(tarLoc).icon(locate).extraInfo(exDetail);
            mBaiduMap.addOverlay(options);

            //计算出两个定位之间的距离
            Double range = DistanceUtil.getDistance(myLoc,tarLoc);
            String distance = String.format("%.2f",range) + "m";
            object_distance = new TextOptions().bgColor(0xAAFFFF00).fontSize(24).fontColor(0xFFFF00FF).text(distance).position(tarLoc).typeface(Typeface.DEFAULT_BOLD);
            mBaiduMap.addOverlay(object_distance);
        }
        return mBaiduMap;
    }
}
