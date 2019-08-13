package pers.utopiaxc.bilibilicrawler;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.provider.Settings;
import android.view.KeyEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;


public class ActivitySettings extends AppCompatActivity {
    public static ListView list;
    public ArrayAdapter<String> adapter;
    public String[] Settings;


    protected void onCreate(Bundle savedInstanceState) {
        //创建UI
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
        Toolbar toolbar = findViewById(R.id.toolbar_settings);
        setSupportActionBar(toolbar);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                back();
                finish();
            }
        });

        Settings=new String[3];
        Settings[0]=this.getResources().getString(R.string.developer_message);
        Settings[1]=this.getResources().getString(R.string.open_source_license);
        Settings[2]=this.getResources().getString(R.string.update_cheak);

        adapter = new ArrayAdapter<String>(ActivitySettings.this,
                android.R.layout.simple_list_item_1, Settings);
        list=findViewById(R.id.set_list);
        list.setAdapter(adapter);
        list.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            //ListView的列表项的单击事件
            @Override
            //第一个参数：指的是这个ListView；第二个参数：当前单击的那个item
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                //既然当前点击的那个item是一个TextView，那我们可以将其强制转型为TextView类型，然后通过getText()方法取出它的内容,紧接着以吐司的方式显示出来

                if (id == 0) {
                    AlertDialog.Builder builder  = new AlertDialog.Builder(ActivitySettings.this);
                    builder.setTitle(getString(R.string.developer_message) ) ;
                    builder.setMessage(getString(R.string.developer_info) ) ;
                    builder.setPositiveButton(getText(R.string.confirm) ,  null );
                    builder.show();
                }else if(id==1){
                    AlertDialog.Builder builder  = new AlertDialog.Builder(ActivitySettings.this);
                    builder.setTitle(getString(R.string.open_source_license) ) ;
                    builder.setMessage(getString(R.string.open_source_info) ) ;
                    builder.setPositiveButton(getText(R.string.confirm) ,  null );
                    builder.show();
                }else if(id==2){
                    AlertDialog.Builder builder  = new AlertDialog.Builder(ActivitySettings.this);
                    builder.setTitle(getString(R.string.update_cheak) ) ;
                    builder.setMessage(getString(R.string.no_update) ) ;
                    builder.setPositiveButton(getText(R.string.confirm) ,  null );
                    builder.show();
                }
            }
        });

    }

    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            back();
            finish();
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    public void back() {
        Intent intent_main = new Intent(this, MainActivity.class);
        startActivity(intent_main);
    }

}
