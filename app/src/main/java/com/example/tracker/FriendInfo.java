package com.example.tracker;

import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.TextView;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Created by 柏宏 on 2016/12/16.
 */

public class FriendInfo extends AppCompatActivity {
    TextView name;
    TextView PhoneNum;
    TextView lat_lng;
    TextView altitude;
    TextView accuracy;
    TextView nearest_city;
    TextView last_update;
    TextView next_update;

    private Button list;
    private Button delete;
    private Button radar;
    private Button enemies;
    private Button done;
    private Button cancel;

    String Name;
    String Phonenum;
    String Lat;
    String Lng;

    //设置存放和取出数据的方法，与FriendList里面的方法相同
    private Object getDetail(String name){
        FileInputStream fileInputStream = null;
        ObjectInputStream objectInputStream = null;
        try{
            fileInputStream = this.openFileInput(name);
            objectInputStream = new ObjectInputStream(fileInputStream);
            return objectInputStream.readObject();
        }catch (Exception except){
            except.printStackTrace();
            return null;
        }
    }

    private void saveDetail(String name,List<Detail> data){
        FileOutputStream fileOutputStream = null;
        ObjectOutputStream objectOutputStream = null;
        try {
            fileOutputStream = this.openFileOutput(name, MODE_PRIVATE);
            objectOutputStream = new ObjectOutputStream(fileOutputStream);
            objectOutputStream.writeObject(data);
        }catch (Exception except){
            except.printStackTrace();
        }
    }

    private List<Detail> detailList = new ArrayList<Detail>();
    @Override
    protected void onCreate(final Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.friend_detail);
        name = (TextView)findViewById(R.id.txt_friend_name);
        PhoneNum = (TextView)findViewById(R.id.txt_friend_number);
        lat_lng = (TextView)findViewById(R.id.txt_friend_long_lang);
        altitude = (TextView)findViewById(R.id.txt_friend_altitude);
        accuracy = (TextView)findViewById(R.id.txt_friend_accuracy);
        nearest_city = (TextView)findViewById(R.id.txt_friend_nearest_city);
        last_update = (TextView)findViewById(R.id.txt_friend_secs_last_update);
        next_update = (TextView)findViewById(R.id.txt_friend_secs_next_update);

        Name = (String)this.getIntent().getExtras().get("Name");
        Phonenum = (String)this.getIntent().getExtras().get("Phonenum");
        Lat = (String)this.getIntent().getExtras().get("Lat");
        Lng = (String)this.getIntent().getExtras().get("Lng");

        name.setText(Name);
        PhoneNum.setText(Phonenum);
        lat_lng.setText(Lat+"|"+Lng);

        //设置list按钮，点击后返回FriendList
        list = (Button)findViewById(R.id.btn_friends_list);
        list.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view){
                Intent intent = new Intent(FriendInfo.this,FriendList.class);
                startActivity(intent);
                finish();
            }
        });
        //设置radar按钮，使得点击后返回雷达显示界面
        radar = (Button)findViewById(R.id.btn_radar);
        radar.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view){
                FriendInfo.this.finish();
            }
        });
        //设置enemy按钮，使得点击后跳转到enemy显示界面
        enemies = (Button)findViewById(R.id.btn_enemies);
        enemies.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view){
                Intent intent = new Intent(FriendInfo.this,EnemyList.class);
                startActivity(intent);
                finish();
            }
        });
        detailList = (List<Detail>)getDetail("friends");
        //设置删除按钮，并设置点击后产生的对话框
        delete = (Button)findViewById(R.id.btn_delete);
        delete.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view){
                //显示删除确认的对话框
                final Dialog dialog = new Dialog(FriendInfo.this);
                dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
                dialog.setContentView(R.layout.dialog_delete);
                dialog.show();
                TextView name = (TextView)dialog.findViewById(R.id.txt_friend_name);
                name.setText(Name);
                TextView PhoneNum = (TextView)dialog.findViewById(R.id.txt_friend_number);
                PhoneNum.setText(Phonenum);
                //设置确认按钮
                done = (Button)dialog.findViewById(R.id.btn_dialog_ok);
                done.setOnClickListener(new View.OnClickListener(){
                    @Override
                    public void onClick(View view){
                        Iterator<Detail> iterator = detailList.iterator();    //使用迭代器加载detailList里面的数据
                        while(iterator.hasNext()){
                            Detail detail = iterator.next();                  //一步步查找出对应要删除的对象，然后调用remove函数进行删除
                            if(detail.getName().equals(Name) && detail.getPhoneNum().equals(Phonenum)){
                                iterator.remove();
                                break;
                            }
                        }
                        saveDetail("friends",detailList);                     //将删除后的数据保存回文件中
                        dialog.dismiss();
                        Intent intent = new Intent(FriendInfo.this,MainActivity.class);
                        startActivity(intent);
                        finish();
                    }
                });
                //设置取消按钮
                cancel = (Button)dialog.findViewById(R.id.btn_dialog_close);
                cancel.setOnClickListener(new View.OnClickListener(){
                    @Override
                    public void onClick(View view){
                        dialog.dismiss();
                    }
                });
            }
        });
    }
}
