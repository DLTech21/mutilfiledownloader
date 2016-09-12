package ocdownload;

import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;


import com.dl.filedownloader.R;

import net.sqlcipher.database.SQLiteDatabase;

import java.util.ArrayList;


public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    Button t1,t2,t3,t4,t5,dl;
    String path;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_download);
        path = Environment.getExternalStorageDirectory().getPath()+"/";
        t1 = (Button)findViewById(R.id.button_task_1);
        t2 = (Button)findViewById(R.id.button_task_2);
        t3 = (Button)findViewById(R.id.button_task_3);
        t4 = (Button)findViewById(R.id.button_task_4);
        t5 = (Button)findViewById(R.id.button_task_5);
        dl = (Button)findViewById(R.id.button_task_dl);
        t1.setOnClickListener(this);
        t2.setOnClickListener(this);
        t3.setOnClickListener(this);
        t4.setOnClickListener(this);
        t5.setOnClickListener(this);
        dl.setOnClickListener(this);
        SQLiteDatabase.loadLibs(this);
    }

    @Override
    public void onClick(View v) {
        ArrayList<DownloadInfo> list = new ArrayList<>();
        switch (v.getId()){
            case R.id.button_task_1:
                Intent intent = new Intent(this, DownloadService.class);
                intent.setAction(DownloadConfig.TASK_ACTION);
                DownloadInfo downloadInfo = new DownloadInfo("QQPC.exe", "http://dldir1.qq.com/qqfile/qq/QQ8.2/17724/QQ8.2.exe", "t1");
                list.add(downloadInfo);
                list.add(new DownloadInfo("QQAndroid.apk", "http://sqdd.myapp.com/myapp/qqteam/AndroidQQ/mobileqq_android.apk", "t2"));
                list.add(new DownloadInfo("QQPad.apk", "http://sqdd.myapp.com/myapp/qqteam/qq_hd/apad/home/qqhd_release_forhome.apk", "t3"));
                list.add(new DownloadInfo("QQIntPC.apk", "http://dldir1.qq.com/qqfile/QQIntl/QQi_PC/QQIntl2.11.exe", "t4"));
                list.add(new DownloadInfo("QQIntAndroid.apk", "http://dldir1.qq.com/qqfile/QQIntl/QQi_wireless/Android/qqi_4.6.13.6034_office.apk", "t5"));
                intent.putExtra(DownloadConfig.TASK_DATA, list);
                startService(intent);
                break;
            case R.id.button_task_2:
                Intent intent2 = new Intent(this,DownloadService.class);
                intent2.setAction(DownloadConfig.TASK_ACTION);
                DownloadInfo downloadInfo2 = new DownloadInfo("QQAndroid.apk", "http://sqdd.myapp.com/myapp/qqteam/AndroidQQ/mobileqq_android.apk", "t2");
                intent2.putExtra(DownloadConfig.TASK_DATA, downloadInfo2);
                startService(intent2);
                break;
            case R.id.button_task_3:
                Intent intent3 = new Intent(this, DownloadService.class);
                intent3.setAction(DownloadConfig.TASK_ACTION);
                DownloadInfo downloadInfo3 = new DownloadInfo("QQPad.apk", "http://sqdd.myapp.com/myapp/qqteam/qq_hd/apad/home/qqhd_release_forhome.apk", "t3");
                list.add(downloadInfo3);
                intent3.putExtra(DownloadConfig.TASK_DATA, list);
                startService(intent3);
                break;
            case R.id.button_task_4:
                Intent intent4 = new Intent(this, DownloadService.class);
                intent4.setAction(DownloadConfig.TASK_ACTION);
                DownloadInfo downloadInfo4 = new DownloadInfo("QQIntPC.apk", "http://dldir1.qq.com/qqfile/QQIntl/QQi_PC/QQIntl2.11.exe", "t4");
                list.add(downloadInfo4);
                intent4.putExtra(DownloadConfig.TASK_DATA, list);
                startService(intent4);
                break;
            case R.id.button_task_5:
                Intent intent5 = new Intent(this, DownloadService.class);
                intent5.setAction(DownloadConfig.TASK_ACTION);
                DownloadInfo downloadInfo5 = new DownloadInfo("QQIntAndroid.apk", "http://dldir1.qq.com/qqfile/QQIntl/QQi_wireless/Android/qqi_4.6.13.6034_office.apk", "t5");
                list.add(downloadInfo5);
                intent5.putExtra(DownloadConfig.TASK_DATA, list);
                startService(intent5);
                break;
            case R.id.button_task_dl:
                startActivity(new Intent(this,OCDownloadManagerActivity.class));
                break;
        }
    }

}
