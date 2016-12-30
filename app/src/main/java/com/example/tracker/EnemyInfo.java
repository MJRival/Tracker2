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

public class EnemyInfo extends AppCompatActivity {
    TextView name;
    TextView PhoneNum;
    TextView lat_lng;
    TextView altitude;
    TextView accurancy;
    TextView nearest_city;
    TextView last_update;
    TextView next_update;

    private Button list;
    private Button radar;
    private Button friends;
    private Button delete;
    private Button done;
    private Button cancel;

    String Name;
    String Phonenum;
    String Lat;
    String Lng;

    //设置存放和取出数据的方法，与FriendInfo里面的方法相同
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
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.enemy_detail);
        name = (TextView)findViewById(R.id.txt_enemy_name);
        PhoneNum = (TextView)findViewById(R.id.txt_enemy_number);
        lat_lng = (TextView)findViewById(R.id.txt_enemy_long_lang);
        altitude = (TextView)findViewById(R.id.txt_enemy_altitude);
        accurancy = (TextView)findViewById(R.id.txt_enemy_accuracy);
        nearest_city = (TextView)findViewById(R.id.txt_enemy_nearest_city);
        last_update = (TextView)findViewById(R.id.txt_enemy_secs_last_update);
        next_update = (TextView)findViewById(R.id.txt_friend_secs_next_update);
        Name = (String)this.getIntent().getExtras().get("name");
        Phonenum = (String)this.getIntent().getExtras().get("Phonenum");
        Lat = (String)this.getIntent().getExtras().get("Lat");
        Lng = (String)this.getIntent().getExtras().get("Lng");

        name.setText(Name);
        PhoneNum.setText(Phonenum);
        lat_lng.setText(Lat+"/"+Lng);
        //设置list按钮返回敌人列表
        list = (Button)findViewById(R.id.btn_enemies_list);
        list.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view){
                Intent intent = new Intent(EnemyInfo.this,EnemyList.class);
                startActivity(intent);
                finish();
            }
        });
        //设置radar按钮
        radar = (Button)findViewById(R.id.btn_radar);
        radar.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view){
                EnemyInfo.this.finish();
            }
        });
        //设置friends按钮进入FriendList界面
        friends = (Button)findViewById(R.id.btn_friends);
        friends.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view){
                Intent intent = new Intent(EnemyInfo.this,FriendList.class);
                startActivity(intent);
                finish();
            }
        });

        detailList = (List<Detail>)getDetail("enemies");
        //设置删除按钮
        delete = (Button)findViewById(R.id.btn_delete);
        delete.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view){
                final Dialog dialog = new Dialog(EnemyInfo.this);
                dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
                dialog.setContentView(R.layout.dialog_delete);
                dialog.show();
                TextView name = (TextView)dialog.findViewById(R.id.txt_enemy_name);
                TextView PhoneNum = (TextView)dialog.findViewById(R.id.txt_enemy_number);
                name.setText(Name);
                PhoneNum.setText(Phonenum);
                //设置对话框中的确认按钮
                done = (Button)dialog.findViewById(R.id.btn_dialog_ok);
                done.setOnClickListener(new View.OnClickListener(){
                    @Override
                    public void onClick(View view){
                        Iterator<Detail> iterator = detailList.iterator();
                        while(iterator.hasNext()){
                            Detail detail = iterator.next();
                            if(detail.getName().equals(Name)&&detail.getPhoneNum().equals(Phonenum)){
                                iterator.remove();
                                break;
                            }
                        }
                        saveDetail("enemies",detailList);
                        dialog.dismiss();
                        Intent intent = new Intent(EnemyInfo.this,MainActivity.class);
                        startActivity(intent);
                        finish();
                    }
                });
                //设置对话框中的取消按钮
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
