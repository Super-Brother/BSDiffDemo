package com.wenchao.bsdiffdemo;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.TextView;

import com.wenchao.bsdiffdemo.utils.UriParseUtils;

import java.io.File;
import java.io.IOException;

public class MainActivity extends AppCompatActivity {

    // Used to load the 'native-lib' library on application startup.
    static {
        System.loadLibrary("native-lib");
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // 显示点击事件
        TextView tv = findViewById(R.id.sample_text);
        tv.setText(BuildConfig.VERSION_NAME);

        //申请权限
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            String[] pers = {Manifest.permission.WRITE_EXTERNAL_STORAGE};
            if (checkSelfPermission(pers[0]) == PackageManager.PERMISSION_DENIED) {
                requestPermissions(pers, 110);
            }
        }
    }

    /**
     * A native method that is implemented by the 'native-lib' native library,
     * which is packaged with this application.
     */
    public native String stringFromJNI();

    public native void bsPatch(String oldApk, String patch, String output);

    public void update(View view) {
        //1.从服务器下载补丁包
        //2.异步任务合成
        new AsyncTask<Void, Void, File>() {

            //在后台子线程工作
            @Override
            protected File doInBackground(Void... voids) {
                //获取
                String oldApk = getApplicationInfo().sourceDir;
                String patch = new File(Environment.getExternalStorageDirectory(), "patch").getAbsolutePath();
                String output = createNewApk().getAbsolutePath();
                bsPatch(oldApk, patch, output);
                return new File(output);
            }

            //执行完成调用
            @Override
            protected void onPostExecute(File file) {
                UriParseUtils.installApk(MainActivity.this, file);
            }
        }.execute();
    }

    private File createNewApk() {
        File newApk = new File(Environment.getExternalStorageDirectory(), "appNew.apk");
        if (!newApk.exists()) {
            try {
                newApk.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return newApk;
    }
}
