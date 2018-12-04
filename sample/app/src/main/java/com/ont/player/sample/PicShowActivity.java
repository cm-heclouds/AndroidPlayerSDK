package com.ont.player.sample;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.FutureTarget;
import com.bumptech.glide.request.target.Target;
import com.ont.player.sample.utils.LogUtil;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by armou on 2018/7/20.
 */

public class PicShowActivity extends AppCompatActivity {
    private ImageView imageView ;
    private String mPicUrl, mPicName;
    private Button btn;
    private String filename;
    private Bitmap bitmap;
    private File file;
    private int picRet;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Intent intent = getIntent();
        mPicUrl = intent.getStringExtra("picurl");
        mPicName = intent.getStringExtra("picname");

        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setTitle(mPicName);
            actionBar.setDisplayHomeAsUpEnabled(true);
        }

        setContentView(R.layout.activity_picture);

        /* test data */
        mPicUrl = "https://gss3.bdstatic.com/-Po3dSag_xI4khGkpoWK1HF6hhy/baike/w%3D268%3Bg%3D0/sign=44f5f5397ac6a7efb926af20c5c1c86c/8ad4b31c8701a18b243242a9922f07082938fe7f.jpg";

        imageView = (ImageView) findViewById(R.id.image_view);

        verifyStoragePermissions(this);

        //使用glide加载图片
        Glide.with(this).load(mPicUrl).into(imageView);

        btn = findViewById(R.id.btn_download_pic);
        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                changeFilename();
                download(mPicUrl);
            }
        });
    }

    public int savePic(){
        File pictureFolder = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES).getAbsoluteFile();

        File appDir = new File(pictureFolder ,"Beauty");
        if (!appDir.exists()) {
            appDir.mkdirs();
        }

        File destFile = new File(appDir, filename);
        FileInputStream fileInputStream = null;
        FileOutputStream fileOutputStream = null;
        try {
            fileInputStream = new FileInputStream(file);
            fileOutputStream = new FileOutputStream(destFile);
            byte[] buffer = new byte[1024];
            while (fileInputStream.read(buffer) > 0) {
                fileOutputStream.write(buffer);
            }
            return 0;
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                if (fileInputStream != null) {
                    fileInputStream.close();
                }
                if (fileOutputStream != null) {
                    fileOutputStream.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return 1;
    }

    // 保存图片到手机
    public void download(final String url) {

        new AsyncTask<Void, Integer, File>() {

            @Override
            protected File doInBackground(Void... params) {

                try {
                    FutureTarget<File> future = Glide
                            .with(PicShowActivity.this)
                            .load(url)
                            .downloadOnly(Target.SIZE_ORIGINAL, Target.SIZE_ORIGINAL);

                    file = future.get();
                    picRet = savePic();
                } catch (Exception e) {
                    LogUtil.e("", e.getMessage());
                }
                return file;
            }

            @Override
            protected void onPostExecute(File file) {

                if(picRet == 0){
                    showToast("保存成功");
                }else{
                    showToast("保存失败");
                }
            }
        }.execute();
    }

    public void showToast(final String msg) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(PicShowActivity.this, msg, Toast.LENGTH_SHORT).show();
            }
        });
    }

    public void changeFilename(){
        Date date = new Date(System.currentTimeMillis());
        SimpleDateFormat format = new SimpleDateFormat("yyyyMMddHHmmss");
        filename = format.format(date) + ".png";
    }

    private static final int REQUEST_EXTERNAL_STORAGE = 1;
    private static String[] PERMISSIONS_STORAGE = {
            "android.permission.READ_EXTERNAL_STORAGE",
            "android.permission.WRITE_EXTERNAL_STORAGE" };


    public static void verifyStoragePermissions(Activity activity) {

        try {
            //检测是否有写的权限
            int permission = ActivityCompat.checkSelfPermission(activity,
                    "android.permission.WRITE_EXTERNAL_STORAGE");
            if (permission != PackageManager.PERMISSION_GRANTED) {
                // 没有写的权限，去申请写的权限，会弹出对话框
                ActivityCompat.requestPermissions(activity, PERMISSIONS_STORAGE, REQUEST_EXTERNAL_STORAGE);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}