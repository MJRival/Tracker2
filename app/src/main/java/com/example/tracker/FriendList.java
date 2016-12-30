package com.example.tracker;

import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * Created by 柏宏 on 2016/12/15.
 */

public class FriendList extends AppCompatActivity {
    private Button add;
    private Button delete;
    private Button done;
    private Button cancel;
    private Button radar;
    private Button enemies;
    private EditText name;
    private EditText PhoneNum;
    private List<Detail> detailList1 = new ArrayList<Detail>();
    private ListView friListView;

    //设置从文件当中存放和取出数据的方法
    private Object getDetail(String name){     //取出数据
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

    private void saveDetail(String name,List<Detail> data){   //存放数据
        FileOutputStream  fileOutputStream = null;
        ObjectOutputStream objectOutputStream = null;
        try {
            fileOutputStream = this.openFileOutput(name, MODE_PRIVATE);
            objectOutputStream = new ObjectOutputStream(fileOutputStream);
            objectOutputStream.writeObject(data);
        }catch (Exception except){
            except.printStackTrace();
        }
    }

    //建立一个由字符串对应列表中每一项的对照表,使得获取的字符串能够与列表中的数据对应
    private List<Map<String,Object>> getData(){
        List<Map<String,Object>> mapList = new ArrayList<Map<String, Object>>();
        Detail detail;
        int i = 0;
        for(Iterator<Detail> iterator = detailList1.iterator();iterator.hasNext();){
            i++;
            Map<String,Object> map = new HashMap<String,Object>();
            detail = iterator.next();
            map.put("name",detail.getName());
            mapList.add(map);
        }
        return mapList;
    }

    void refresh(){
        SimpleAdapter simpleAdapter = new SimpleAdapter(this,getData(),R.layout.friends_list_item,new String[]{"name"},new int[]{R.id.name_cell}) {
            @Override
            public View getView(final int location, View convertView, ViewGroup parent) {
                View view = super.getView(location, convertView, parent);
                delete = (Button) view.findViewById(R.id.delete_button_cell);
                delete.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        final Dialog dialog = new Dialog(FriendList.this);
                        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
                        dialog.getWindow().setGravity(Gravity.CENTER);
                        dialog.setContentView(R.layout.dialog_delete);
                        dialog.show();
                        TextView friend_name = (TextView) dialog.findViewById(R.id.txt_friend_name);
                        friend_name.setText(detailList1.get(location).getName());
                        TextView friend_phonenum = (TextView) dialog.findViewById(R.id.txt_friend_number);
                        friend_phonenum.setText(detailList1.get(location).getPhoneNum());
                        //确认删除按钮设置
                        done = (Button) dialog.findViewById(R.id.btn_dialog_ok);
                        done.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                detailList1.remove(location);
                                saveDetail("friends", detailList1);
                                refresh();
                                dialog.dismiss();
                            }
                        });
                        //取消删除按钮设置
                        cancel = (Button) dialog.findViewById(R.id.btn_dialog_close);
                        cancel.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                dialog.dismiss();
                            }
                        });
                        dialog.setOnKeyListener(onKeyListener);
                    }
                });
                return view;
            }
        };
        friListView.setAdapter(simpleAdapter);
        friListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, final int location, long id) {
                Intent intent = new Intent();
                intent.setClass(FriendList.this,FriendInfo.class);
                startActivity(intent);
            }
        });
    }
    //按下手机上的返回键也可以取消Dialog
    private DialogInterface.OnKeyListener onKeyListener = new DialogInterface.OnKeyListener(){
        @Override
        public boolean onKey(DialogInterface dialogInterface, int keyCode, KeyEvent keyEvent){
            if(keyCode == KeyEvent.KEYCODE_BACK && keyEvent.getAction() == KeyEvent.ACTION_DOWN){
                dialogInterface.dismiss();
            }
            return false;
        }
    };
    @Override
    protected void onCreate(final Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.friends_list);
        friListView = (ListView)findViewById(R.id.lvw_friends_list);
        detailList1 = (List<Detail>)getDetail("friends");
        refresh();
        add = (Button)findViewById(R.id.btn_friends_list_add);
        add.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view){
                //显示添加对话框
                final Dialog dialog = new Dialog(FriendList.this);
                dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
                dialog.getWindow().setGravity(Gravity.CENTER);
                dialog.setContentView(R.layout.dialog_add_friend);
                dialog.show();
                name = (EditText)dialog.findViewById(R.id.txt_friend_name);
                PhoneNum = (EditText)dialog.findViewById(R.id.txt_friend_number);
                //设置确认按钮
                done = (Button)dialog.findViewById(R.id.btn_dialog_ok);
                done.setOnClickListener(new View.OnClickListener(){
                    @Override
                    public void onClick(View view){
                        Detail detail = new Detail();
                        detail.setName(name.getText().toString());
                        detail.setPhoneNum(PhoneNum.getText().toString());
                        detail.setLat("");
                        detail.setLng("");
                        detailList1.add(detail);
                        saveDetail("friends",detailList1);
                        refresh();
                        dialog.dismiss();
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
                dialog.setOnKeyListener(onKeyListener);
            }
        });
        //设置RADAR按钮
        radar = (Button)findViewById(R.id.btn_friends_list_radar);
        radar.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view){
                FriendList.this.finish();
            }
        });
        //设置ENEMIES按钮
        enemies = (Button)findViewById(R.id.btn_friends_list_enemies);
        enemies.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view){
                Intent intent = new Intent(FriendList.this,EnemyList.class);
                startActivity(intent);
                finish();
            }
        });
    }
}
