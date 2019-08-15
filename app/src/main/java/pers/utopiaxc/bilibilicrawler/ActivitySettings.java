package pers.utopiaxc.bilibilicrawler;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.provider.Settings;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;


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

        Settings = new String[3];
        Settings[0] = this.getResources().getString(R.string.developer_message);
        Settings[1] = this.getResources().getString(R.string.open_source_license);
        Settings[2] = this.getResources().getString(R.string.update_cheak);

        adapter = new ArrayAdapter<String>(ActivitySettings.this,
                android.R.layout.simple_list_item_1, Settings);
        list = findViewById(R.id.set_list);
        list.setAdapter(adapter);
        list.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            //ListView的列表项的单击事件
            @Override
            //第一个参数：指的是这个ListView；第二个参数：当前单击的那个item
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                //既然当前点击的那个item是一个TextView，那我们可以将其强制转型为TextView类型，然后通过getText()方法取出它的内容,紧接着以吐司的方式显示出来

                if (id == 0) {
                    AlertDialog.Builder builder = new AlertDialog.Builder(ActivitySettings.this);
                    builder.setTitle(getString(R.string.developer_message));
                    builder.setMessage(getString(R.string.developer_info) + "\nGithub:https://github.com/UtopiaXC/BilibiliCrawler");
                    builder.setPositiveButton(getText(R.string.confirm), null);
                    builder.setNegativeButton(getText(R.string.goto_github), new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://github.com/UtopiaXC/BilibiliCrawler")));
                        }
                    });
                    builder.show();
                } else if (id == 1) {
                    AlertDialog.Builder builder = new AlertDialog.Builder(ActivitySettings.this);
                    builder.setTitle(getString(R.string.open_source_license));
                    builder.setMessage(getString(R.string.open_source_info));
                    builder.setPositiveButton(getText(R.string.confirm), null);
                    builder.show();
                } else if (id == 2) {
                    update_check(ActivitySettings.this);
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

    private static Context context;
    private static String mSaveMessage = "恭喜，您正在使用最新版本！";
    private final static String TAG = "PictureActivity";
    private static ProgressDialog mSaveDialog = null;

    public void update_check(Context contexts) {
        context = contexts;
        mSaveDialog = ProgressDialog.show(context, getText(R.string.update_cheak), "检查更新中，请稍等...", true);

        new Thread(checkupdateRunnable).start();
    }

    private Runnable checkupdateRunnable = new Runnable() {
        @Override
        public void run() {
            try {
                PackageManager packageManager = getPackageManager();
                PackageInfo packInfo = packageManager.getPackageInfo(getPackageName(), 0);
                String version = packInfo.versionName;

                String latest_version = getHTML("https://raw.githubusercontent.com/UtopiaXC/BilibiliCrawler/master/version_check");


                if (latest_version.equals("error")) {
                    mSaveMessage = (String) getText(R.string.net_error);
                } else if (latest_version.equals(version)) {
                    mSaveMessage = (String) getText(R.string.no_update);
                } else {
                    mSaveMessage = (String) getText(R.string.has_update);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            messageHandler.sendMessage(messageHandler.obtainMessage());
        }
    };

    private Handler messageHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            mSaveDialog.dismiss();
            Log.d(TAG, mSaveMessage);
            if (mSaveMessage.equals(getText(R.string.has_update))) {
                android.app.AlertDialog.Builder builder = new AlertDialog.Builder(context);
                builder.setTitle(getString(R.string.download_newversion));
                builder.setMessage(getString(R.string.has_update));
                builder.setPositiveButton(getText(R.string.download_newversion), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://github.com/UtopiaXC/BilibiliCrawler/blob/master/app/release/app-release.apk?raw=true")));
                    }
                });
                builder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() { //设定“取消"按钮的功能
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                    }
                });
                builder.setNeutralButton(getText(R.string.goto_github), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://github.com/UtopiaXC/BilibiliCrawler")));
                    }
                });
                builder.show();
            } else if (mSaveMessage.equals(getText(R.string.no_update))) {
                AlertDialog.Builder builder = new AlertDialog.Builder(ActivitySettings.this);
                builder.setTitle(getString(R.string.congratulations));
                builder.setMessage(getString(R.string.no_update));
                builder.setPositiveButton(getText(R.string.confirm), null);
                builder.show();
            } else {
                AlertDialog.Builder builder = new AlertDialog.Builder(ActivitySettings.this);
                builder.setTitle(getString(R.string.unknow_error));
                builder.setMessage(getString(R.string.again_please ));
                builder.setPositiveButton(getText(R.string.confirm), null);
                builder.show();
            }
        }
    };

    public String getHTML(String version_address) {
        System.out.println(version_address);
        URL url;
        int responsecode;
        HttpURLConnection urlConnection;
        BufferedReader reader;
        String line;
        try {
            url = new URL(version_address);
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
