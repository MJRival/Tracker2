package com.example.tracker;

import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.graphics.Typeface;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.telephony.SmsManager;
import android.telephony.SmsMessage;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.baidu.location.BDLocation;
import com.baidu.location.BDLocationListener;
import com.baidu.location.LocationClient;
import com.baidu.location.LocationClientOption;
import com.baidu.mapapi.SDKInitializer;
import com.baidu.mapapi.map.BaiduMap;
import com.baidu.mapapi.map.BitmapDescriptor;
import com.baidu.mapapi.map.BitmapDescriptorFactory;
import com.baidu.mapapi.map.MapStatus;
import com.baidu.mapapi.map.MapStatusUpdate;
import com.baidu.mapapi.map.MapStatusUpdateFactory;
import com.baidu.mapapi.map.MapView;
import com.baidu.mapapi.map.Marker;
import com.baidu.mapapi.map.MarkerOptions;
import com.baidu.mapapi.map.MyLocationData;
import com.baidu.mapapi.map.OverlayOptions;
import com.baidu.mapapi.map.PolylineOptions;
import com.baidu.mapapi.map.TextOptions;
import com.baidu.mapapi.model.LatLng;
import com.baidu.mapapi.utils.DistanceUtil;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MainActivity extends AppCompatActivity {
    private SharedPreferences sharedPreferences;
    private SharedPreferences.Editor editor;
    private List<Detail> detailList = new ArrayList<Detail>();
    private List<Detail> detailList1 = new ArrayList<Detail>();
    private SMSBroadcastReceiver smsBroadcastReceiver;
    private IntentFilter receiveFilter;
    private BaiduMap mBaiduMap;
    private Button relocate;
    private Button refresh;
    private Button refresh_friend;
    private Button refresh_enemy;
    private Button friends;
    private Button enemies;
    private BitmapDescriptor Friend_marker;      //bitmap
    private BitmapDescriptor Enemy_marker;       //bitmap1
    private BitmapDescriptor UnKnown_marker;     //bitmap2
    private BitmapDescriptor locate;             //bitmap3
    public LatLng myLoc;
    public LatLng tarLoc;
    public Bundle exDetail;
    public LocationClient mLocationClient = null;
    public OverlayOptions options;
    public OverlayOptions object;
    public OverlayOptions line;
    public OverlayOptions object_distance;
    boolean isFirstLocation = true;
    MapView mMapView = null;

    public BDLocationListener MyLocationListener = new BDLocationListener(){
        @Override
        public void onReceiveLocation(BDLocation bdLocation) {
            //MapView销毁后不再处理新的位置
            if(bdLocation ==null || mMapView == null)
                return;

            //将获取的location信息给百度map
            MyLocationData data = new MyLocationData.Builder()
                    .accuracy(bdLocation.getRadius())
                    // 此处设置开发者获取到的方向信息，顺时针0-360
                    .direction(100)
                    .latitude(bdLocation.getLatitude())
                    .longitude(bdLocation.getLongitude())
                    .build();
            mBaiduMap.setMyLocationData(data);
            if (isFirstLocation) {
                isFirstLocation = false;
                //获取经纬度
                LatLng ll = new LatLng(bdLocation.getLatitude(), bdLocation.getLongitude());
                MapStatusUpdate status = MapStatusUpdateFactory.newLatLngZoom(ll,16);
                //mBaiduMap.setMapStatus(status);//直接到中间
                mBaiduMap.animateMapStatus(status);//动画的方式到中间
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState){

        //第一次登陆sharepreference的“first_login”为“true”
        sharedPreferences = getSharedPreferences("phone",Context.MODE_PRIVATE);
        //判断是否为第一次登陆
        if(sharedPreferences.getBoolean("first_login",true)){
            editor = sharedPreferences.edit();
            //修改sharePreference的“first_login”为“false”
            editor.putBoolean("first_login",false);
            editor.commit();
            //对friends和enemies文件进行初始化
            detailList.clear();
            saveDetail("enemies",detailList);
            detailList1.clear();
            saveDetail("friends",detailList1);
        }

        super.onCreate(savedInstanceState);

        //在使用SDK各组件之前初始化context信息，传入ApplicationContext
        //注意该方法要再setContentView方法之前实现
        SDKInitializer.initialize(getApplicationContext());
        setContentView(R.layout.activity_main);
        detailList = (List<Detail>)getDetail("enemies");
        detailList1 = (List<Detail>)getDetail("friends");
        //获取地图控件引用
        mMapView = (MapView) findViewById(R.id.bmapView);
        // 不显示缩放比例尺
        mMapView.showZoomControls(false);
        // 不显示百度地图Logo
        mMapView.removeViewAt(1);
        //百度地图
        mBaiduMap = mMapView.getMap();
        // 改变地图状态
        MapStatus mMapStatus = new MapStatus.Builder().zoom(15).build();
        MapStatusUpdate mMapStatusUpdate = MapStatusUpdateFactory.newMapStatus(mMapStatus);
        mBaiduMap.setMapStatus(mMapStatusUpdate);

        // 开启定位图层
        mBaiduMap.setMyLocationEnabled(true);
        mLocationClient = new LocationClient(getApplicationContext());
        mLocationClient.registerLocationListener(MyLocationListener);     //注册监听函数
        this.initLocation();
        mLocationClient.start();


        //设置定位按钮，点击重新定位
        relocate = (Button) findViewById(R.id.btn_locate);
        relocate.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view){
                LatLng latLng = new LatLng(mLocationClient.getLastKnownLocation().getLatitude(),mLocationClient.getLastKnownLocation().getLongitude());
                double zoomLevel = mBaiduMap.getMapStatus().zoom;
                MapStatusUpdate status = MapStatusUpdateFactory.newLatLngZoom(latLng,(float)zoomLevel);   //重新设置地图中心点以及缩放级别
                mBaiduMap.animateMapStatus(status);
            }
        });

        //设置刷新按钮，点击后发送短信
        refresh = (Button)findViewById(R.id.btn_refresh);
        refresh.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view){
                PendingIntent pendingIntent = PendingIntent.getActivity(MainActivity.this,0,new Intent(),0);
                SmsManager smsManager = SmsManager.getDefault();
                for(Detail detail:detailList){
                    smsManager.sendTextMessage(detail.getPhoneNum(),null,"Where are you?",pendingIntent,null);
                }
                for(Detail detail:detailList1){
                    smsManager.sendTextMessage(detail.getPhoneNum(),null,"Where are you?",pendingIntent,null);
                }
                Toast.makeText(MainActivity.this,"短信已发送，请等待更新实时数据",Toast.LENGTH_SHORT);
            }
        });
        Friend_marker = BitmapDescriptorFactory.fromResource(R.drawable.friend_marker);
        Enemy_marker = BitmapDescriptorFactory.fromResource(R.drawable.enemy_marker);
        UnKnown_marker = BitmapDescriptorFactory.fromResource(R.drawable.locate);

        //点击Friends按钮，跳转到友方列表
        friends = (Button)findViewById(R.id.btn_friends);
        friends.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view){
                Intent intent = new Intent();
                intent.setClass(MainActivity.this,FriendList.class);
                startActivity(intent);
            }
        });
        //点击Enemies按钮，跳转到敌方列表
        enemies = (Button)findViewById(R.id.btn_enemies);
        enemies.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view){
                Intent intent = new Intent();
                intent.setClass(MainActivity.this,EnemyList.class);
                startActivity(intent);
            }
        });

        receiveFilter = new IntentFilter();
        receiveFilter.addAction("android.provider.Telephony.SMS_RECEIVED");
        smsBroadcastReceiver = new SMSBroadcastReceiver();
        registerReceiver(smsBroadcastReceiver,receiveFilter);

        mBaiduMap.setOnMarkerClickListener(new BaiduMap.OnMarkerClickListener(){
            @Override
            public boolean onMarkerClick(Marker marker){
                Intent intent = new Intent();
                String Name = marker.getExtraInfo().getString("Name");
                String PhoneNum = marker.getExtraInfo().getString("PhoneNum");
                String Lat = marker.getExtraInfo().getString("Latitude");
                String Lng = marker.getExtraInfo().getString("Longitude");
                intent.putExtra("Name",Name);
                intent.putExtra("PhoneNum",PhoneNum);
                intent.putExtra("Latitude",Lat);
                intent.putExtra("Longitude",Lng);
                if(marker.getIcon().equals(UnKnown_marker)){
                    Toast.makeText(MainActivity.this,"不明身份的电话号码："+PhoneNum+"\n位置信息："+Lat+"/"+Lng,Toast.LENGTH_SHORT).show();
                }else{
                    if(marker.getIcon().equals(Friend_marker)){
                        intent.setClass(MainActivity.this,FriendInfo.class);
                        startActivity(intent);
                    }else if(marker.getIcon().equals(Enemy_marker)){
                        intent.setClass(MainActivity.this,EnemyInfo.class);
                        startActivity(intent);
                    }
                }
                return false;
            }
        });
    }

    protected void onDestroy(){
        super.onDestroy();
        unregisterReceiver(smsBroadcastReceiver);
    }

    public class SMSBroadcastReceiver extends BroadcastReceiver {
        String body;
        String date;
        String sender;

        @Override
        public void onReceive(Context context, Intent intent) {
            Object[] pdus = (Object[]) intent.getExtras().get("pdus");   //接收数据
            for (Object p : pdus) {
                byte[] pdu = (byte[]) p;
                SmsMessage message = SmsMessage.createFromPdu(pdu); //根据获得的byte[]封装成SmsMessage
                body = message.getMessageBody();             //发送内容
                date = new Date(message.getTimestampMillis()).toLocaleString();//发送时间
                sender = message.getOriginatingAddress();    //短信发送方
            }
            System.out.println("您有新的短信记录");
            System.out.println(sender + ":" + body);
            Toast.makeText(MainActivity.this, "接收到来自" + sender + "的一条短信：" + body, Toast.LENGTH_SHORT).show();
            Double myLat = mLocationClient.getLastKnownLocation().getLatitude();
            Double myLng = mLocationClient.getLastKnownLocation().getLongitude();
            if (body.equals("Where are you?")) {
                Toast.makeText(MainActivity.this, "正在向对方发送自己的位置信息", Toast.LENGTH_SHORT).show();
                PendingIntent pendingIntent = PendingIntent.getActivity(MainActivity.this, 0, new Intent(), 0);
                SmsManager smsManager = SmsManager.getDefault();
                smsManager.sendTextMessage(sender, null, myLat + "/" + myLng, pendingIntent, null);
            }
            //对发来短信的内容格式进行判别
            Pattern pattern = Pattern.compile("^D+(./d+)?[//]/d+(./d+)?$");
            Matcher matcher = pattern.matcher(body);
            boolean b = matcher.matches();
            if (b) {
                String t[] = body.split("/");
                String Name = "";
                String Phonenum = sender;
                String Latitude = t[0];
                String Longitude = t[1];
                //载入我的位置信息
                myLoc = new LatLng(myLat, myLng);
                //根据发来的短信，找到敌方目标的位置
                tarLoc = new LatLng(Double.valueOf(Latitude), Double.valueOf(Longitude));
                exDetail = new Bundle();
                exDetail.putString("Latitude", Latitude);
                exDetail.putString("Longitude", Longitude);
                boolean As_Know = false;
                boolean As_Enemy = false;
                for (Iterator<Detail> iterator = detailList.iterator(); iterator.hasNext(); ) {
                    Detail detail = iterator.next();
                    //在对方的位置设置图标
                    if (body.contains(detail.getPhoneNum())) {
                        Enemy_marker = BitmapDescriptorFactory.fromResource(R.drawable.enemy_marker);
                        Name = detail.getName();
                        Phonenum = detail.getPhoneNum();
                        detail.setLat(Latitude);
                        detail.setLng(Longitude);
                        saveDetail("enemies", detailList);
                        exDetail.putString("Name", Name);
                        exDetail.putString("PhoneNum", Phonenum);
                        LayoutInflater layoutInflater = LayoutInflater.from(MainActivity.this);
                        View EntryView = layoutInflater.inflate(R.layout.name_phone, null);
                        TextView name = (TextView) EntryView.findViewById(R.id.name);
                        TextView PhoneNum = (TextView) EntryView.findViewById(R.id.PhoneNum);
                        name.setText(Name);
                        name.setTextColor(getResources().getColor(R.color.Red));
                        name.setText(Phonenum);
                        locate = BitmapDescriptorFactory.fromView(EntryView);
                        object = new MarkerOptions().position(tarLoc).icon(locate).extraInfo(exDetail);
                        mBaiduMap.addOverlay(object);
                        //在目标位置添加图标
                        options = new MarkerOptions().position(tarLoc).icon(Enemy_marker).extraInfo(exDetail);
                        mBaiduMap.addOverlay(options);
                        //在本位置与目标位置之间建立连线
                        List<LatLng> points = new ArrayList<LatLng>();
                        points.add(myLoc);
                        points.add(tarLoc);
                        line = new PolylineOptions().width(15).color(0xAAFF0000).points(points);
                        mBaiduMap.addOverlay(line);
                        //计算出两个定位之间的距离
                        Double range = DistanceUtil.getDistance(myLoc, tarLoc);
                        String distance = String.format("%.2f", range) + "m";
                        object_distance = new TextOptions().bgColor(0xAAFFFF00).fontSize(24).fontColor(0xFFFF00FF).text(distance).position(tarLoc).typeface(Typeface.DEFAULT_BOLD);
                        mBaiduMap.addOverlay(object_distance);
                        As_Enemy = true;
                        As_Know = true;
                        break;
                    }
                }
                //根据发来的短信，找到友方的目标位置
                if (!As_Enemy) {
                    for (Iterator<Detail> iterator = detailList1.iterator(); iterator.hasNext(); ) {
                        Detail detail = iterator.next();
                        if (sender.contains(detail.getPhoneNum())) {
                            Friend_marker = BitmapDescriptorFactory.fromResource(R.drawable.friend_marker);
                            Name = detail.getName();
                            Phonenum = detail.getPhoneNum();
                            detail.setLat(Latitude);
                            detail.setLng(Longitude);
                            saveDetail("friends", detailList1);
                            exDetail.putString("Name", Name);
                            exDetail.putString("PhoneNum", Phonenum);
                            LayoutInflater layoutInflater = LayoutInflater.from(MainActivity.this);
                            View EntryView = layoutInflater.inflate(R.layout.name_phone, null);
                            TextView name = (TextView) EntryView.findViewById(R.id.name);
                            TextView PhoneNum = (TextView) EntryView.findViewById(R.id.PhoneNum);
                            name.setText(Name);
                            name.setTextColor(getResources().getColor(R.color.Green));
                            PhoneNum.setText(Phonenum);
                            locate = BitmapDescriptorFactory.fromView(EntryView);
                            object = new MarkerOptions().position(tarLoc).icon(locate).extraInfo(exDetail);
                            mBaiduMap.addOverlay(object);
                            //在友方的位置上添加图标
                            options = new MarkerOptions().position(tarLoc).icon(Friend_marker).extraInfo(exDetail);
                            mBaiduMap.addOverlay(options);
                            //在本位置与目标位置之间建立连线
                            List<LatLng> points = new ArrayList<LatLng>();
                            points.add(myLoc);
                            points.add(tarLoc);
                            line = new PolylineOptions().width(15).color(0xAAFF0000).points(points);
                            mBaiduMap.addOverlay(line);
                            //计算出两个定位之间的距离
                            Double range = DistanceUtil.getDistance(myLoc, tarLoc);
                            String distance = String.format("%.2f", range) + "m";
                            object_distance = new TextOptions().bgColor(0xAAFFFF00).fontSize(24).fontColor(0xFFFF00FF).text(distance).position(tarLoc).typeface(Typeface.DEFAULT_BOLD);
                            mBaiduMap.addOverlay(object_distance);
                            As_Know = true;
                            break;
                        }
                    }
                }
                //根据发来的短信，找到不明身份的目标位置
                if (!As_Know) {
                    exDetail.putString("name", "不明身份");
                    exDetail.putString("number", Phonenum);
                    LayoutInflater layoutInflater = LayoutInflater.from(MainActivity.this);
                    View EntryView = layoutInflater.inflate(R.layout.name_phone, null);
                    TextView name = (TextView) EntryView.findViewById(R.id.name);
                    TextView PhoneNum = (TextView) EntryView.findViewById(R.id.PhoneNum);
                    name.setText("不明身份");
                    name.setTextColor(getResources().getColor(R.color.Black));
                    PhoneNum.setText(Phonenum);
                    locate = BitmapDescriptorFactory.fromView(EntryView);
                    object = new MarkerOptions().position(tarLoc).icon(locate).extraInfo(exDetail);
                    mBaiduMap.addOverlay(object);
                    //在不明身份人员的位置加上图标
                    options = new MarkerOptions().position(tarLoc).icon(UnKnown_marker).extraInfo(exDetail);
                    mBaiduMap.addOverlay(options);
                    //在本位置与目标位置之间建立连线
                    List<LatLng> points = new ArrayList<LatLng>();
                    points.add(myLoc);
                    points.add(tarLoc);
                    line = new PolylineOptions().width(15).color(0xAAFF0000).points(points);
                    mBaiduMap.addOverlay(line);
                    //计算出两个定位之间的距离
                    Double range = DistanceUtil.getDistance(myLoc, tarLoc);
                    String distance = String.format("%.2f", range) + "m";
                    object_distance = new TextOptions().bgColor(0xAAFFFF00).fontSize(24).fontColor(0xFFFF00FF).text(distance).position(tarLoc).typeface(Typeface.DEFAULT_BOLD);
                    mBaiduMap.addOverlay(object_distance);
                }
            }
        }
    }

    private void initLocation() {
        //定位客户端的设置
        mLocationClient = new LocationClient(this);
        //配置定位
        LocationClientOption option = new LocationClientOption();
        option.setCoorType("bd09ll");//坐标类型
        option.setIsNeedAddress(true);//可选，设置是否需要地址信息，默认不需要
        option.setOpenGps(true);//打开Gps
        option.setScanSpan(1000);//1000毫秒定位一次
        //option.setIsNeedLocationPoiList(true);//可选，默认false，设置是否需要POI结果，可以在BDLocation.getPoiList里得到
        mLocationClient.setLocOption(option);
    }

    //设置从文件当中存放和取出数据的方法
    private Object getDetail(String name) {
        FileInputStream fileInputStream = null;
        ObjectInputStream objectInputStream = null;
        try {
            fileInputStream = this.openFileInput(name);
            objectInputStream = new ObjectInputStream(fileInputStream);
            return objectInputStream.readObject();
        } catch (Exception except) {
            except.printStackTrace();
            return null;
        }
    }

    private void saveDetail(String name, List<Detail> data) {
        FileOutputStream fileOutputStream = null;
        ObjectOutputStream objectOutputStream = null;
        try {
            fileOutputStream = this.openFileOutput(name, MODE_PRIVATE);
            objectOutputStream = new ObjectOutputStream(fileOutputStream);
            objectOutputStream.writeObject(data);
        } catch (Exception except) {
            except.printStackTrace();
        }
    }
}