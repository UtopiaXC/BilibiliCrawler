package pers.utopiaxc.bilibilicrawler;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import com.cjj.MaterialRefreshLayout;
import com.cjj.MaterialRefreshListener;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MainActivity extends AppCompatActivity {
    //变量常量
    public static ListView list;
    public static String[] names;
    public static String[] address;
    public static String[] authorlist;
    public static String[] video;
    public static String line;
    public static boolean connerror = false;
    public MaterialRefreshLayout materialRefreshLayout;
    public ArrayAdapter<String> adapter;
    public final String bili_address = "https://www.bilibili.com/ranking";
    public boolean isExit = false;
    public MainActivity MA = this;
    public boolean RefreshCheck=false;
    public SharedPreferences config;

    //异步消息
    @SuppressLint("HandlerLeak")
    public Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            if (msg.what == 0) {
                isExit = false;
            } else if(msg.what==1){
                adapter = new ArrayAdapter<String>(MainActivity.this,
                        android.R.layout.simple_list_item_1, names);
                list.setAdapter(adapter);
                materialRefreshLayout.finishRefresh();
                Toast.makeText(MainActivity.this,"刷新成功",Toast.LENGTH_LONG).show();
            }
        }
    };

    //UI入口
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        //创建UI
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        list = findViewById(R.id.settings_list);

        //启动并同步网络处理线程
        Thread newConnection = new Thread(new Connect());
        newConnection.start();
        try {
            newConnection.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        if(line.equals("error")){
            new AlertDialog.Builder(this)
                    .setMessage(getText(R.string.net_error))
                    .setPositiveButton(getText(R.string.quit), new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            finish();
                        }
                    })
                    .setCancelable(false)
                    .show();
        }else {

            adapter = new ArrayAdapter<String>(MainActivity.this,
                    android.R.layout.simple_list_item_1, names);
            list.setAdapter(adapter);
        }
        list.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            //ListView的列表项的单击事件
            @Override
            //第一个参数：指的是这个ListView；第二个参数：当前单击的那个item
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                //既然当前点击的那个item是一个TextView，那我们可以将其强制转型为TextView类型，然后通过getText()方法取出它的内容,紧接着以吐司的方式显示出来

                if (id != 0) {
                    jumptoAV(address[(int)id],authorlist[(int)id],video[(int)id]);
                }else if(id==0){
                    new AlertDialog.Builder(MainActivity.this)
                            .setTitle(getString(R.string.this_time)+names[0])
                            .setMessage(getString(R.string.refreah_info))
                            .setPositiveButton(getString(R.string.confirm), new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {

                                }
                            })
                            .show();
                }
            }
        });
        materialRefreshLayout = (MaterialRefreshLayout) findViewById(R.id.refresh);
        materialRefreshLayout.setMaterialRefreshListener(
                new MaterialRefreshListener() {
                    @Override
                    public void onRefresh(final MaterialRefreshLayout materialRefreshLayout) {
                        new Thread(new Refrash()).start();
                        //下拉刷新....
                    }
                });
    }

    void jumptoAV(String url,String author,String name){
        Intent intent = new Intent(this,ActivityVideos.class);
        intent.putExtra("url",url);
        intent.putExtra("author",author);
        intent.putExtra("name",name);
        startActivity(intent);
    }

    class Refrash implements Runnable{
        @Override
        public void run() {
            Looper.prepare();
            try {
                Thread.sleep(500);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            Thread newConnection = new Thread(new Connect());
            newConnection.start();
            try {
                newConnection.join();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            handler.sendEmptyMessage(1);
        }
    }

    //创建菜单
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    //菜单功能
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            Intent intent_settings = new Intent(this,ActivitySettings.class);
            startActivity(intent_settings);
            finish();
        }

        return super.onOptionsItemSelected(item);
    }

    //双击退出
    public void onBackPressed() {
        if (!isExit) {
            isExit = true;
            Toast.makeText(MainActivity.this, getText(R.string.exit_alart), Toast.LENGTH_SHORT).show();
            handler.sendEmptyMessageDelayed(0, 2000);
        } else {
            this.finish();
        }
    }
}

//用于抓取分析并保存网页数据
class Connect extends MainActivity implements Runnable {
    @Override
    public void run() {
        connerror = false;
        line = getHTML();

        if(line.equals("error")){
            return;
        }

        String reg1 = "<div class=\"num\">(.+?\\综合得分)";
        Pattern r1 = Pattern.compile(reg1);
        Matcher m1 = r1.matcher(line);
        List<String> result1 = new ArrayList<String>();
        while (m1.find()) {
            result1.add(m1.group());
        }
        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        names = new String[101];
        address = new String[101];
        authorlist=new String[101];
        video=new String[101];
        address[0] = "http://time.tianqi.com/";
        names[0] = "抓取时间" + df.format(new Date());
        try {
            for (int i = 1; i < 101; i++) {
                names[i] = "排名：" + String.valueOf(i);
            }
            for (int i = 0; i < 100; i++) {
                String reg2 = "class=\"title\">(.+?)\\</a>";
                Pattern r2 = Pattern.compile(reg2);
                Matcher m2 = r2.matcher(result1.get(i));
                String work = null;
                if (m2.find()) {
                    work = m2.group(0).replace("class=\"title\">", "");
                    work = work.replace("</a>", "");
                }

                String reg3 = "author\"></i>(.+?)\\</span>";
                Pattern r3 = Pattern.compile(reg3);
                Matcher m3 = r3.matcher(result1.get(i));
                String author = null;
                if (m3.find()) {
                    author = m3.group(0).replace("author\"></i>", "");
                    author = author.replace("</span>", "");
                }
                names[i + 1] += " \n作者：" + author;
                authorlist[i+1]=author;
                names[i + 1] += " \n作品：" + work;
                video[i+1]=work;

                String reg4 = "\"img\"><a href=\"//(.+?)\\\" target=\"";
                Pattern r4 = Pattern.compile(reg4);
                Matcher m4 = r4.matcher(result1.get(i));

                if (m4.find()) {
                    address[i + 1] = "https://";
                    address[i + 1] += m4.group(0).replace("\"img\"><a href=\"//", "");
                    address[i + 1] = address[i + 1].replace("\" target=\"", "");
                }
            }
        } catch (Exception e) {
            names = new String[1];
            names[1] = "网络错误！抓取失败";
            connerror = true;
        }
    }

    public String getHTML() {
        URL url;
        int responsecode;
        HttpURLConnection urlConnection;
        BufferedReader reader;
        String line;
        try {
            url = new URL(bili_address);
            //打开URL
            urlConnection = (HttpURLConnection) url.openConnection();
            urlConnection.setRequestProperty("User-agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/75.0.3770.142 Safari/537.36");
            //获取服务器响应代码
            responsecode = urlConnection.getResponseCode();
            if (responsecode == 200) {
                //得到输入流，即获得了网页的内容
                reader = new BufferedReader(new InputStreamReader(urlConnection.getInputStream(), "UTF-8"));
                String result = "";
                while ((line = reader.readLine()) != null) {
                    result += line;
                }
                return result;
            } else {
                return "error";

            }
        } catch (Exception e) {
            return "error";
        }
    }
}