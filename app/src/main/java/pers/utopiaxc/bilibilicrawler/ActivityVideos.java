package pers.utopiaxc.bilibilicrawler;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.provider.Settings;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Random;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ActivityVideos extends AppCompatActivity {

    public WebView cover;
    public Button download;
    public Button open;
    public Button share;
    public static String line;
    public static String url;
    public static String picture;
    public static String author;
    public static String videoname;
    public static int av;
    public static TextView info;
    public static String authorurl;
    public static Button authorspace;

    protected void onCreate(Bundle savedInstanceState) {
        //创建UI
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_video);
        Toolbar toolbar = findViewById(R.id.toolbar_video);
        setSupportActionBar(toolbar);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        Intent intent = getIntent();
        url = intent.getStringExtra("url");
        videoname = intent.getStringExtra("name");
        author = intent.getStringExtra("author");
        String avt = url.replace("https://www.bilibili.com/video/av", "");
        av = Integer.parseInt(avt.replace("/", ""));


        Thread conn = new Thread(new connect());
        conn.start();
        try {
            conn.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        cover = findViewById(R.id.cover);
        cover.getSettings().setSupportZoom(true);//缩放
        cover.getSettings().setBuiltInZoomControls(true);
        cover.getSettings().setDisplayZoomControls(false);//不显示控制器
        cover.getSettings().setUseWideViewPort(true);
        cover.getSettings().setLayoutAlgorithm(WebSettings.LayoutAlgorithm.SINGLE_COLUMN);
        cover.getSettings().setLoadWithOverviewMode(true);
        cover.loadUrl(picture);

        download = findViewById(R.id.download);
        download.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                android.app.AlertDialog.Builder builder = new AlertDialog.Builder(ActivityVideos.this);
                builder.setTitle(getString(R.string.Confirm_to_download));
                builder.setMessage(getString(R.string.Confirm_info));
                builder.setPositiveButton(getText(R.string.confirm), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        donwloadImg(ActivityVideos.this, picture);
                    }
                });
                builder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() { //设定“取消"按钮的功能
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                    }
                });
                builder.setNeutralButton(getText(R.string.open_setting), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        opensettings();
                    }
                });
                builder.show();


            }
        });

        share = findViewById(R.id.share);
        share.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ClipboardManager cmb = (ClipboardManager) ActivityVideos.this.getSystemService(Context.CLIPBOARD_SERVICE);
                cmb.setText(url);
                Toast.makeText(ActivityVideos.this, "视频链接已复制", Toast.LENGTH_LONG).show();
            }
        });

        authorspace = findViewById(R.id.author);
        authorspace.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(ActivityVideos.this, authorurl, Toast.LENGTH_LONG).show();
                Uri uri = Uri.parse(authorurl);
                Intent intent_web = new Intent(Intent.ACTION_VIEW, uri);
                startActivity(intent_web);
            }
        });

        open = findViewById(R.id.video);
        open.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(ActivityVideos.this, url, Toast.LENGTH_LONG).show();
                Uri uri = Uri.parse(url);
                Intent intent_web = new Intent(Intent.ACTION_VIEW, uri);
                startActivity(intent_web);
            }
        });

        info = findViewById(R.id.video_infomation);
        info.setText("视频名：" + videoname + "\n作者名：" + author + "\nav号：" + String.valueOf(av));

    }

    void opensettings(){
        Intent intent = new Intent();
        intent.setAction(android.provider.Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
        intent.setData(Uri.parse("package:" + this.getPackageName()));
        this.startActivity(intent);
    }

    class connect extends ActivityVideos implements Runnable {
        @Override
        public void run() {
            line = getHTML(url);

            String reg1 = "itemprop=\"thumbnailUrl\" content=\"(.+?)\\\">";
            Pattern r1 = Pattern.compile(reg1);
            Matcher m1 = r1.matcher(line);
            if (m1.find()) {
                picture = m1.group(0).replace("itemprop=\"thumbnailUrl\" content=\"", "");
                picture = picture.replace("\">", "");
            } else {
                picture = "https://timgsa.baidu.com/timg?image&quality=80&size=b9999_10000&sec=1563995707867&di=fc3634eb7f3f3a017c101fce3a682ac8&imgtype=0&src=http%3A%2F%2Fbpic.588ku.com%2Felement_origin_min_pic%2F17%2F02%2F10%2F83ee6b9b091fa1b9d10d4da70621d25f.jpg";
            }

            String reg2 = "space.bilibili.com/(.+?)\"";
            Pattern r2 = Pattern.compile(reg2);
            Matcher m2 = r2.matcher(line);
            if (m2.find()) {
                authorurl = "https://space.bilibili.com/";
                authorurl += m2.group(0).replace("space.bilibili.com/", "");
                authorurl = authorurl.replace("\"", "");
            } else {
                authorurl = "none";
            }

        }

    }


    public String getHTML(String bili_address) {
        System.out.println(bili_address);
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


    private static Context context;
    private static String filePath;
    private static Bitmap mBitmap;
    private static String mSaveMessage = "失败";
    private final static String TAG = "PictureActivity";
    private static ProgressDialog mSaveDialog = null;

    public static void donwloadImg(Context contexts, String filePaths) {
        context = contexts;
        filePath = filePaths;
        mSaveDialog = ProgressDialog.show(context, "保存图片", "图片正在保存中，请稍等...", true);

        new Thread(saveFileRunnable).start();
    }

    private static Runnable saveFileRunnable = new Runnable() {
        @Override
        public void run() {
            try {
                if (!TextUtils.isEmpty(filePath)) { //网络图片
                    // 对资源链接
                    URL url = new URL(filePath);
                    InputStream is = url.openStream();
                    File destDir = new File(Environment.getExternalStorageDirectory() + "/DCIM/Bilibili-cover/");
                    if (!destDir.exists()) {
                        destDir.mkdir();
                    }

                    File file = new File(Environment.getExternalStorageDirectory() + "/DCIM/Bilibili-cover/" + videoname + ".jpg");
                    file.createNewFile();

                    OutputStream os = new FileOutputStream(file);

                    byte[] b = new byte[1024];
                    int temp = 0;
                    while ((temp = is.read(b)) != -1) {
                        os.write(b, 0, temp);
                    }
                    os.close();
                    is.close();
                    update(file);
                }

                mSaveMessage = "图片保存成功！";

            } catch (IOException e) {
                mSaveMessage = "图片保存失败！";
                e.printStackTrace();
            } catch (Exception e) {
                e.printStackTrace();
            }
            messageHandler.sendMessage(messageHandler.obtainMessage());
        }
    };

    private static Handler messageHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            mSaveDialog.dismiss();
            Log.d(TAG, mSaveMessage);
            Toast.makeText(context, mSaveMessage, Toast.LENGTH_SHORT).show();
        }
    };

    public static void update(File file) throws IOException {
        Intent intent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
        Uri uri = Uri.fromFile(file);
        intent.setData(uri);
        context.sendBroadcast(intent);
    }

}



