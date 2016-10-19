package ocdownload;

import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;


import com.dl.filedownloader.R;

import java.util.ArrayList;


public class OCDownloadManagerActivity extends AppCompatActivity implements OCDownloadAdapter.OnRecycleViewClickCallBack{

    RecyclerView downloadList;
    OCDownloadAdapter downloadAdapter;
    DownloadService downloadService;
    LocalBroadcastManager broadcastManager;
    UpdateHandler updateHandler;
    ServiceConnection serviceConnection;

    ArrayList<DownloadInfo> downloadInfos;


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_download_manager);
        downloadInfos = new ArrayList<>();
        //RecycleView 的 Adapter 创建与点击事件的绑定
        downloadAdapter = new OCDownloadAdapter(downloadInfos);
        downloadAdapter.setRecycleViewClickCallBack(this);

        //RecyclerView 的创建与相关操作
        downloadList = (RecyclerView)findViewById(R.id.download_list);
        downloadList.setLayoutManager(new LinearLayoutManager(this,LinearLayoutManager.VERTICAL,false));
        downloadList.setHasFixedSize(true);
        downloadList.setAdapter(downloadAdapter);
        findData();
        //广播过滤器的创建
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(DownloadConfig.ALL_UPDATE);       //更新整个列表的 Action
        intentFilter.addAction(DownloadConfig.SINGLE_UPDATE);    //更新单独条目的 Action
        intentFilter.addAction(DownloadConfig.SINGLE_DELETE);    //删除单独条目的 Action
        //广播接收器 与 本地广播 的创建和注册
        updateHandler = new UpdateHandler();
        broadcastManager = LocalBroadcastManager.getInstance(this);
        broadcastManager.registerReceiver(updateHandler,intentFilter);

//        创建服务连接
        serviceConnection = new ServiceConnection() {
            @Override
            public void onServiceConnected(ComponentName name, IBinder service) {
                downloadService = ((DownloadService.GetServiceClass)service).getService();
//                downloadAdapter.updateAllItem(downloadService.getTaskList());
            }

            @Override
            public void onServiceDisconnected(ComponentName name) {
                if (broadcastManager != null && updateHandler != null){
                    broadcastManager.unregisterReceiver(updateHandler);
                }
            }
        };

//        连接服务并进行绑定
        startService(new Intent(this, DownloadService.class));
        bindService(new Intent(this, DownloadService.class),serviceConnection,BIND_AUTO_CREATE);

    }

    /**
     * RecyclerView 的单击事件
     * @param bean  点击条目中的 下载信息Bean
     */
    @Override
    public void onRecycleViewClick(DownloadInfo bean) {
        if (bean.getStatus() == DownloadConfig.FINISH) {

        }
        else {
            if (downloadService != null) {
                downloadService.clickTask(bean, false);
            }
        }
    }

    /**
     * RecyclerView 的长按事件
     * @param bean  点击条目中的 下载信息Bean
     */
    @Override
    public void onRecycleViewLongClick(DownloadInfo bean) {
        if (downloadService != null){
            downloadService.clickTask(bean,true);
        }
    }

    /**
     * 本地广播接收器  负责更新UI
     */
    class UpdateHandler extends BroadcastReceiver{

        @Override
        public void onReceive(Context context, Intent intent) {
            switch (intent.getAction()){
                case DownloadConfig.ALL_UPDATE:
                    //更新所有项目
                    ArrayList<DownloadInfo> list = (ArrayList<DownloadInfo>)intent.getExtras().getSerializable(DownloadConfig.TASK_DATA);
                    update(list);
//                    downloadAdapter.updateAllItem(downloadService.getTaskList());
                    break;
                case DownloadConfig.SINGLE_DELETE:
                    DownloadInfo downloadInfo = (DownloadInfo)intent.getExtras().getSerializable(DownloadConfig.TASK_DATA);
                    delete(downloadInfo);
                    break;
                case DownloadConfig.SINGLE_UPDATE:
                    //仅仅更新当前项
                    DownloadInfo bean = (DownloadInfo)intent.getExtras().getSerializable(DownloadConfig.TASK_DATA);
                    String downloadedSize = intent.getExtras().getString(DownloadConfig.DOWNLOADEDSIZE);
                    String totalSize = intent.getExtras().getString(DownloadConfig.TOTALSIZE);
                    int progressLength = intent.getExtras().getInt(DownloadConfig.PROGRESSBARLENGTH);
                    if (bean != null){
                        int index = 0;
                        for (int i= 0 ; i<downloadInfos.size();i++) {
                            if (bean.getFileID().equals(downloadInfos.get(i).getFileID())) {
                                index = i;
                                break;
                            }
                        }
                        View itemView = downloadList.getChildAt(index);
                        if (itemView != null){
                            TextView textProgress = (TextView)itemView.findViewById(R.id.textView_download_length);
                            ProgressBar progressBar = (ProgressBar)itemView.findViewById(R.id.progressBar_download);
                            textProgress.setText(downloadedSize+"MB / "+totalSize+"MB");
                            progressBar.setProgress(progressLength);
                            TextView status = (TextView)itemView.findViewById(R.id.textView_download_status);
                            switch (bean.getStatus()){
                                case DownloadConfig.DOWNLOADING:
                                    status.setText("Downloading");
                                    break;
                                case DownloadConfig.WAITTING:
                                    status.setText("Waitting");
                                    break;
                                case DownloadConfig.FAILED:
                                    status.setText("Failed");
                                    break;
                                case DownloadConfig.PAUSED:
                                    status.setText("Paused");
                                    break;
                                case DownloadConfig.FINISH:
                                    status.setText("Finish");
                            }
                        }
                    }
                    break;
            }
        }

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        //解绑接收器
        broadcastManager.unregisterReceiver(updateHandler);
        //解绑服务
        unbindService(serviceConnection);
    }

    private void findData() {
        downloadInfos.addAll(FileSQLManager.getInstance(this).getUserDownLoadInfoWhileFileTypeAndFileIDTypeEqual2(DownloadConfig.userID));
        downloadAdapter.notifyDataSetChanged();
    }

    private void update(ArrayList<DownloadInfo> temp) {
        for (DownloadInfo down: temp) {
            for (DownloadInfo originalDown : downloadInfos) {
                if (down.getFileID().equals(originalDown.getFileID())) {
                    originalDown.setStatus(down.getStatus());
                    originalDown.setDownloadedSize(down.getDownloadedSize());
                    originalDown.setFileSize(down.getFileSize());
                    break;
                }
            }
        }
        downloadAdapter.notifyDataSetChanged();
    }

    private void delete(DownloadInfo downloadInfo) {
        int index = -1;
        for (int i= 0 ; i<downloadInfos.size();i++) {
            if (downloadInfo.getFileID().equals(downloadInfos.get(i).getFileID())) {
                index = i;
                break;
            }
        }
        if (index != -1) {
            downloadInfos.remove(index);
            downloadAdapter.notifyDataSetChanged();
        }
    }

}
