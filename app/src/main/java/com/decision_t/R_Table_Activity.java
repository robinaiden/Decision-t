package com.decision_t;

import android.content.Context;
import android.content.DialogInterface;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.github.clans.fab.FloatingActionButton;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

public class R_Table_Activity extends AppCompatActivity {

    private DrawerLayout drawer;
    private FloatingActionButton fab_left_start;
    private FloatingActionButton fab_right;
    private NavigationView navigationView;
    private Toolbar toolbar;
    private String[] user_info, table_data;
    private ListView r_table_list;
    ArrayList<String[]> data;
    MyAdapter myAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.r_table_activity_main);
        //先取得傳進來的資料
        user_info = getIntent().getStringArrayExtra("user_info");
        table_data = getIntent().getStringArrayExtra("table_data");

/*暫時用不到  layout用menu不符合設計圖規範
        navigationView = (NavigationView) findViewById(R.id.r_table_nav_view);
        navigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                int id = item.getItemId();
                if (id == R.id.nav_r_table_name) {
                    Toast.makeText(getApplicationContext(), "你想修改隨機桌名稱？", Toast.LENGTH_SHORT).show();
                } else if(id == R.id.nav_r_table_description) {
                    Toast.makeText(getApplicationContext(), "想看描述！", Toast.LENGTH_SHORT).show();
                } else if(id == R.id.nav_r_table_member) {
                    Toast.makeText(getApplicationContext(), "想看成員！", Toast.LENGTH_SHORT).show();

                    //跳轉到 MemberActivity 看成員或查詢成員
                    Intent memberIntent = new Intent(R_Table_Activity.this, MemberActivity.class);
                    memberIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    startActivity(memberIntent);
                }

                //按完之後關起來
                drawer = (DrawerLayout) findViewById(R.id.r_table_drawer_layout);
                drawer.closeDrawer(GravityCompat.END);
                return true;
            }
        });*/

        toolbar = (Toolbar) findViewById(R.id.r_table_toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle(table_data[1]);

        drawer = (DrawerLayout) findViewById(R.id.r_table_drawer_layout);

        //初始化右邊的 FloatingActionButton
        fab_right = (FloatingActionButton) findViewById(R.id.r_table_fab_right);
        fab_right.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //點擊新增項目
                final View dialog_text = LayoutInflater.from(R_Table_Activity.this).inflate(R.layout.dialog_text, null);
                AlertDialog.Builder newitem = new AlertDialog.Builder(R_Table_Activity.this);
                newitem.setTitle("請輸入新項目");
                newitem.setView(dialog_text);
                newitem.setPositiveButton("新增", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        TextView text = (TextView) dialog_text.findViewById(R.id.editText);
                        String sql = "INSERT INTO `Tables_item` ( `Name`, `Decision_tables_ID`, `Account_ID`)" +
                                "               VALUES('"+text.getText().toString()+"', "+table_data[0]+", '"+user_info[0]+"');";
                        DBConnector.executeQuery(sql);
                        getItemList(table_data[0]);
                    }
                });
                newitem.show();

            }
        });

        //初始化左邊的 FloatingActionButton
        fab_left_start = (FloatingActionButton) findViewById(R.id.r_table_fab_menu_item_start);
        fab_left_start.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //開始隨機
                // Todo this Action 20161009 15:34
                Toast.makeText(getApplication(), "開始隨機！", Toast.LENGTH_SHORT).show();
            }
        });
        //初始化listview
        r_table_list = (ListView) findViewById(R.id.r_table_list);
        r_table_list.setOnItemLongClickListener(long_click_item_list);

        getItemList(table_data[0]);
    }

    //創建右上角的 info
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.r_table_toolbar_menu, menu);
        return true;
    }

    //info 被點到會有所反應
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_info:
                drawer.openDrawer(GravityCompat.END);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }


    //取得決策桌項目列表
    public void getItemList(String table_id){
        //先清空資料
        data = new ArrayList<String[]>();
        try {
            //顯示決策桌的項目
            String sql = "SELECT *" +
                    "              FROM `Tables_item`" +
                    "          WHERE `Decision_tables_ID` = '"+ table_id +"'";
            String result = DBConnector.executeQuery(sql);
            JSONArray jsonArray = new JSONArray(result);
            for(int i = 0; i < jsonArray.length(); i++) {
                JSONObject jsonData = jsonArray.getJSONObject(i);
                data.add(new String[] {
                        jsonData.getString("ID"),
                        jsonData.getString("Name"),
                        jsonData.getString("Info"),
                        jsonData.getString("Score"),
                        jsonData.getString("Decision_tables_ID"),
                        jsonData.getString("Account_ID")});
            }
            myAdapter = new MyAdapter(R_Table_Activity.this);
            r_table_list.setAdapter(myAdapter);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public class MyAdapter extends BaseAdapter {
        private LayoutInflater myInflater;
        public MyAdapter(Context c) {
            myInflater = LayoutInflater.from(c);
        }
        @Override
        public int getCount() {
            return data.size();
        }

        @Override
        public Object getItem(int position) {
            return data.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            //產生一個table_list_view的view，使用系統原生自帶的就行
            convertView = myInflater.inflate(android.R.layout.simple_list_item_1, null);
            //設定元件內容
            TextView itemtitle = (TextView) convertView.findViewById(android.R.id.text1);
            itemtitle.setText(data.get(position)[1]);
            return convertView;
        }
    }

    private AdapterView.OnItemLongClickListener long_click_item_list = new AdapterView.OnItemLongClickListener() {

        @Override
        public boolean onItemLongClick(AdapterView<?> parent, View view, final int position, long id) {
            AlertDialog.Builder check = new AlertDialog.Builder(R_Table_Activity.this);
            check.setTitle("確定刪除?");
            check.setPositiveButton("確定", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    try {
                        String sql;
                        //先檢查是否為決策桌主持人
                        if(!table_data[6].equals(user_info[0])){
                            //再檢查是否為該項目創建者
                            sql = "SELECT * FROM `Tables_item` WHERE `ID`='"+data.get(position)[0]+"' AND `Account_ID`='"+user_info[0]+"';";
                            String result = DBConnector.executeQuery(sql);
                            JSONArray jsonArray = new JSONArray(result);
                            if(jsonArray.length() == 0) {
                                Toast.makeText(getApplicationContext(), "您不能刪除其他人新增的項目", Toast.LENGTH_SHORT).show();
                                return;
                            }
                        }
                        sql = "DELETE FROM `Tables_item`" +
                                "WHERE `ID` = '"+data.get(position)[0]+"';";
                        DBConnector.executeQuery(sql);
                        getItemList(table_data[0]);
                    } catch (JSONException e) {
                        e.printStackTrace();
                        return;
                    }
                }
            });
            check.show();
            return true;
        }
    };
}
