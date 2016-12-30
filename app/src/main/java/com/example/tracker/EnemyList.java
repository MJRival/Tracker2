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
 * Created by 柏宏 on 2016/12/16.
 */

public class EnemyList extends AppCompatActivity {
    private Button add;
    private Button delete;
    private Button done;
    private Button cancel;
    private Button radar;
    private Button friends;
    private EditText name;
    private EditText PhoneNum;
    private List<Detail> detailList = new ArrayList<Detail>();
    private ListView eneListView;

    //设置从文件当中存放和取出数据的方法
    private Object getDetail(String name){
        FileInputStream fileInputStream = null;
        ObjectInputStream objectInputStream = null;
        try {
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
        try{
            fileOutputStream = this.openFileOutput(name,MODE_PRIVATE);
            objectOutputStream = new ObjectOutputStream(fileOutputStream);
            objectOutputStream.writeObject(data);
        }catch (Exception except){
            except.printStackTrace();
        }
    }

    //建立一个由字符串对应列表中每一项的对照表,使得获取的字符串能够与列表中的数据对应
    private List<Map<String,Object>> getData(){
     List<Map<String,Object>> mapList = new ArrayList<Map<String,Object>>();
        Detail detail;
        int i = 0;
        for (Iterator<Detail> iterator = detailList.iterator();iterator.hasNext();){
            i++;
            Map<String,Object> map = new HashMap<String, Object>();
            detail = iterator.next();
            map.put("name",detail.getName());
            mapList.add(map);
        }
        return mapList;
    }

    //设置更新敌人列表数据的函数
    void refresh(){
        SimpleAdapter simpleAdapter = new SimpleAdapter(this,getData(),R.layout.enemies_list_item,new String[]{"name"},new int[]{R.id.name_cell}){
        @Override
           public View getView(final int location, View convertView, ViewGroup parent){
            View view = super.getView(location,convertView,parent);
            //设置删除按钮和删除对话框
            delete = (Button)view.findViewById(R.id.delete_button_cell);
            delete.setOnClickListener(new View.OnClickListener(){
                @Override
                public void onClick(View view){
                    final Dialog dialog = new Dialog(EnemyList.this);
                    dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
                    dialog.getWindow().setGravity(Gravity.CENTER);
                    dialog.setContentView(R.layout.dialog_delete);
                    dialog.show();
                    TextView enemy_name = (TextView)dialog.findViewById(R.id.txt_friend_name);
                    enemy_name.setText(detailList.get(location).getName());
                    TextView enemy_PhoneNum = (TextView)dialog.findViewById(R.id.txt_friend_number);
                    enemy_PhoneNum.setText(detailList.get(location).getPhoneNum());
                    //设置删除对话框中的确认按钮
                    done = (Button)dialog.findViewById(R.id.btn_dialog_ok);
                    done.setOnClickListener(new View.OnClickListener(){
                        @Override
                        public void onClick(View view){
                            detailList.remove(location);
                            saveDetail("enemies",detailList);
                            refresh();
                            dialog.dismiss();
                        }
                    });
                    //设置删除对话框中取消按钮
                    cancel = (Button)dialog.findViewById(R.id.btn_dialog_close);
                    cancel.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            dialog.dismiss();
                            }
                        });
                    //dialog.setOnKeyListener(onKeyListener);
                    }
                });
            return view;
            }
        };
        //设置编辑敌人的对话框
        eneListView.setAdapter(simpleAdapter);
        eneListView.setOnItemClickListener(new AdapterView.OnItemClickListener(){
            @Override
            public void onItemClick(AdapterView<?> parent,View view,final int location,long id){
                Intent intent = new Intent();
                intent.setClass(EnemyList.this,EnemyInfo.class);
                startActivity(intent);
            }
        });
    }
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
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.enemies_list);
        eneListView = (ListView)findViewById(R.id.lvw_enemies_list);
        detailList = (List<Detail>)getDetail("enemies");
        refresh();
        //设置列表界面的添加按钮
        add = (Button)findViewById(R.id.btn_enemies_list_add);
        add.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view){
                final Dialog dialog = new Dialog(EnemyList.this);
                dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
                dialog.setContentView(R.layout.dialog_add_enemy);
                dialog.show();
                name = (EditText)dialog.findViewById(R.id.txt_enemy_name);
                PhoneNum = (EditText)dialog.findViewById(R.id.txt_enemy_number);
                //设置对话框的确认按钮
                done = (Button)dialog.findViewById(R.id.btn_dialog_ok);
                done.setOnClickListener(new View.OnClickListener(){
                    @Override
                    public void onClick(View view){
                        Detail detail = new Detail();
                        detail.setName(name.getText().toString());
                        detail.setPhoneNum(PhoneNum.getText().toString());
                        detail.setLat("");
                        detail.setLng("");
                        detailList.add(detail);
                        saveDetail("enemies",detailList);
                        refresh();
                        dialog.dismiss();
                    }
                });
                //设置对话框的取消按钮
                cancel = (Button)dialog.findViewById(R.id.btn_dialog_close);
                cancel.setOnClickListener(new View.OnClickListener(){
                    @Override
                    public void onClick(View v){
                        dialog.dismiss();
                    }
                });
                dialog.setOnKeyListener(onKeyListener);
            }
        });
        radar = (Button)findViewById(R.id.btn_enemies_list_radar);
        radar.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view){
                EnemyList.this.finish();
            }
        });
        friends = (Button)findViewById(R.id.btn_enemies_list_friends);
        friends.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view){
                Intent intent = new Intent(EnemyList.this,FriendList.class);
                startActivity(intent);
                finish();
            }
        });
    }
}
