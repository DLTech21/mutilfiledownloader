package ocdownload;

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.support.v4.content.LocalBroadcastManager;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Toast;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.concurrent.Callable;
import java.util.concurrent.FutureTask;

import okhttp3.Call;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.ResponseBody;

public class DownloadService extends Service{

    static final String TAG = "DownloadService";

    private LocalBroadcastManager broadcastManager;
    private HashMap<String, DownloadInfo> allTaskList;
    private ThreadExecutor threadExecutor;

    private boolean keepAlive = false;      //是否被bind的标记
    private int runningThread = 0;          //当前运行计数
    private FileSQLManager fileSQLManager;

    @Override
    public void onCreate() {
        Log.d(TAG, "onCreate");
        super.onCreate();

        //创建任务线程池
        if (threadExecutor == null){
            threadExecutor = new ThreadExecutor(DownloadConfig.MAX_DOWNLOADING_TASK, "downloading");
        }

        if (fileSQLManager == null) {
            fileSQLManager = new FileSQLManager(this);
        }

        //创建总表对象
        if (allTaskList == null){
            allTaskList = new HashMap<>();
        }

        //创建本地广播器
        if (broadcastManager == null){
            broadcastManager = LocalBroadcastManager.getInstance(this);
        }
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        //检测传过来的请求是否完整
        if (intent != null && intent.getAction() != null && intent.getAction().equals(DownloadConfig.TASK_ACTION)){
            ArrayList<DownloadInfo> downloadInfo = (ArrayList<DownloadInfo>)intent.getExtras().getSerializable(DownloadConfig.TASK_DATA);

            //检测得到的数据是否有效
            if (downloadInfo == null){
                Toast.makeText(DownloadService.this,"Invail data", Toast.LENGTH_SHORT).show();
                return super.onStartCommand(intent, flags, startId);
            }else {
                //如果有效则执行检查步骤
                for (DownloadInfo download: downloadInfo) {
                    checkTask(download);
                }
            }
        }
        return super.onStartCommand(intent, flags, startId);
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        Log.d(TAG, "onBind");
        keepAlive = true;
        return new GetServiceClass();
    }

    @Override
    public boolean onUnbind(Intent intent) {
        keepAlive = false;
        if (threadExecutor.getActiveCount() <= 0 && saveDownloadList()){
            //如果解除bind的时候，已经没有正在运行的任务，保存状态成功，则结束服务
            stopSelf();
        }
        return super.onUnbind(intent);
    }
//
//    @Override
//    public void onRebind(Intent intent) {
//        super.onRebind(intent);
//        Log.d(TAG, "onRebind");
//        keepAlive = true;
//    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "onDestroy");
    }

    /**
     * 保存当前下载的列表
     * @return  保存结果。 返回 false：保存失败，服务空闲时不销毁。 返回true：保存成功，服务空闲时销毁
     */
    private boolean saveDownloadList(){
        if (allTaskList.size() <= 0){
            return true;
        }else {
            //在此添加保存操作
            for (DownloadInfo downloadInfo : allTaskList.values()) {
                if (downloadInfo.getStatus() == DownloadConfig.DOWNLOADING){
                    downloadInfo.setStatus(DownloadConfig.PAUSED);
                    fileSQLManager.saveDownLoadInfo(downloadInfo);
                    threadExecutor.cancelTask(downloadInfo.getUrl());
                    threadExecutor.removeTag(downloadInfo.getUrl());
                    runningThread--;
                }
                allTaskList.remove(downloadInfo.getFileID());
            }

            return true;
        }
    }

    /**
     * 传递服务对象的类
     */
    public class GetServiceClass extends Binder {

        public DownloadService getService(){
            return DownloadService.this;
        }

    }

    /**
     * 检查新的下载任务
     * @param requestBean   下载对象的信息Bean
     */
    private synchronized boolean checkTask(@Nullable DownloadInfo requestBean){
        if (requestBean != null){
            DownloadConfig.userID = requestBean.getUserID();
            FileHelper.newDirFile(new File(DownloadConfig.dowloadFilePath));
            //先检查是否存在同名的文件
            if (new File(DownloadConfig.dowloadFilePath+"/"+requestBean.getTitle()).exists()){
                Toast.makeText(DownloadService.this,"File is already downloaded", Toast.LENGTH_SHORT).show();
            }else {

                //再检查是否在总表中
                if (allTaskList.containsKey(requestBean.getFileID())){
                    DownloadInfo bean = allTaskList.get(requestBean.getFileID());
                    //检测当前的状态
                    //如果是 暂停 或 失败 状态的则当作新任务开始下载
                    switch (bean.getStatus()){
                        case DownloadConfig.DOWNLOADING:
                            Toast.makeText(DownloadService.this,"Task is downloading", Toast.LENGTH_SHORT).show();
                            return false;
                        case DownloadConfig.WAITTING:
                            Toast.makeText(DownloadService.this,"Task is in the queue", Toast.LENGTH_SHORT).show();
                            return false;
                        case DownloadConfig.PAUSED:
                        case DownloadConfig.FAILED:
                            requestBean.setStatus(DownloadConfig.WAITTING);
                            startTask(requestBean);
                            break;
                    }
                }else {
                    //如果不存在,则添加到总表和数据库
                    fileSQLManager.saveDownLoadInfo(requestBean);
                    requestBean.setStatus(DownloadConfig.WAITTING);
                    allTaskList.put(requestBean.getFileID(),requestBean);
                    startTask(requestBean);

                }

            }

            return true;
        }else{

            return false;
        }
    }

    /**
     * 开始执行下载任务
     * @param requestBean   下载对象的信息Bean
     */
    private void startTask(DownloadInfo requestBean){
        if (runningThread < DownloadConfig.MAX_DOWNLOADING_TASK){
            //如果当前没有正在执行的下载则直接下载 , 否则就是在等待中
            requestBean.setStatus(DownloadConfig.DOWNLOADING);
            runningThread += 1;
            threadExecutor.submit(new FutureTask<>(new DownloadThread(requestBean)),requestBean.getFileID());
        }
        updateList();
    }

    /**
     * 得到一份总表的 ArrayList 的拷贝
     * @return  总表的拷贝
     */
    public ArrayList<DownloadInfo> getTaskList(){
        return new ArrayList<>(allTaskList.values());
    }

    /**
     * 任务的操作
     * @param downloadInfo   任务
     * @param isLongClick   是否是长按 (删除操作)
     */
    public void clickTask(DownloadInfo downloadInfo , boolean isLongClick){
        if (allTaskList.containsKey(downloadInfo.getFileID())){
            //获得对象
            downloadInfo = allTaskList.get(downloadInfo.getFileID());
            if (isLongClick){
                //删除文件以及移除任务
                allTaskList.remove(downloadInfo.getFileID());
                if (downloadInfo.getStatus() == DownloadConfig.DOWNLOADING){
                    threadExecutor.cancelTask(downloadInfo.getFileID());
                    threadExecutor.removeTag(downloadInfo.getUrl());
                }
                updateDownloadInfoDeleted(downloadInfo);
            }else {
                //普通操作
                switch (downloadInfo.getStatus()){
                    case DownloadConfig.DOWNLOADING:
                        //下载则暂停
                        downloadInfo.setStatus(DownloadConfig.PAUSED);
                        threadExecutor.cancelTask(downloadInfo.getFileID());
                        threadExecutor.removeTag(downloadInfo.getFileID());
                        updateList();
                        break;
                    case DownloadConfig.WAITTING:
                        //等待则暂停
                        downloadInfo.setStatus(DownloadConfig.PAUSED);
                        updateList();
                        break;
                    case DownloadConfig.PAUSED:
                    case DownloadConfig.FAILED:
                        //暂停 失败 则创建新的任务
                        checkTask(downloadInfo);
                        break;
                }
            }
        }
        else {
            if (isLongClick){
                //删除文件以及移除任务
                allTaskList.remove(downloadInfo.getFileID());
                if (downloadInfo.getStatus() == DownloadConfig.DOWNLOADING){
                    threadExecutor.cancelTask(downloadInfo.getFileID());
                    threadExecutor.removeTag(downloadInfo.getUrl());
                }
                updateDownloadInfoDeleted(downloadInfo);
            }
            else {
                switch (downloadInfo.getStatus()){
                    case DownloadConfig.DOWNLOADING:
                        //下载则暂停
                        downloadInfo.setStatus(DownloadConfig.PAUSED);
                        threadExecutor.cancelTask(downloadInfo.getFileID());
                        threadExecutor.removeTag(downloadInfo.getFileID());
                        updateList();
                        break;
                    case DownloadConfig.WAITTING:
                        //等待则暂停
                        downloadInfo.setStatus(DownloadConfig.PAUSED);
                        updateList();
                        break;
                    case DownloadConfig.PAUSED:
                    case DownloadConfig.FAILED:
                        //暂停 失败 则创建新的任务
                        checkTask(downloadInfo);
                        break;
                }
            }
        }
    }

    /**
     * 更新整个下载列表
     */
    private void updateList(){
        Intent intent = new Intent(DownloadConfig.ALL_UPDATE);
        intent.putExtra(DownloadConfig.TASK_DATA, getTaskList());
        broadcastManager.sendBroadcast(intent);
    }

    /**
     * 更新当前项目的进度
     * @param totalSize 下载文件的总大小
     * @param downloadedSize    当前下载的进度
     */
    private void updateItem(DownloadInfo bean , long totalSize, long downloadedSize){
        int progressBarLength = (int) (((float)  downloadedSize / totalSize) * 100);
        Intent intent = new Intent(DownloadConfig.SINGLE_UPDATE);
        intent.putExtra(DownloadConfig.PROGRESSBARLENGTH, progressBarLength);
        intent.putExtra(DownloadConfig.DOWNLOADEDSIZE, String.format("%.2f", downloadedSize/(1024.0*1024.0)));
        intent.putExtra(DownloadConfig.TOTALSIZE, String.format("%.2f", totalSize/(1024.0*1024.0)));
        intent.putExtra(DownloadConfig.TASK_DATA,bean);
        broadcastManager.sendBroadcast(intent);
    }

    /**
     * 更新当前项目的完成
     */
    private void updateItem(DownloadInfo bean){
        Intent intent = new Intent(DownloadConfig.SINGLE_FINISH_UPDATE);
        intent.putExtra(DownloadConfig.TASK_DATA,bean);
        broadcastManager.sendBroadcast(intent);
    }

    /**
     * 更新当前项目的完成
     */
    private void updateDownloadInfoDeleted(DownloadInfo downloadInfo) {
        new File(DownloadConfig.dowloadFilePath+"/"+ downloadInfo.getTitle()+".octmp").delete();
        new File(DownloadConfig.dowloadFilePath+"/"+ downloadInfo.getTitle()).delete();
        fileSQLManager.deleteDownLoadInfo(downloadInfo.getUserID(), downloadInfo.getFileID());
        Intent intent = new Intent(DownloadConfig.SINGLE_DELETE);
        intent.putExtra(DownloadConfig.TASK_DATA,downloadInfo);
        broadcastManager.sendBroadcast(intent);
    }

    /**
     * 执行的下载任务方法
     */
    private class DownloadThread implements Callable<String> {

        private DownloadInfo bean;
        private File downloadFile;
        private String fileSize = null;

        public DownloadThread(DownloadInfo bean) {
            this.bean = bean;
        }

        @Override
        public String call() throws Exception {

            //先检查是否有之前的临时文件
            downloadFile = new File(DownloadConfig
                    .dowloadFilePath+"/"+bean.getTitle()+".octmp");
            if (downloadFile.exists()){
                fileSize = "bytes=" + downloadFile.length() + "-";
            }

            //创建 OkHttp 对象相关
            OkHttpClient client = new OkHttpClient();

            //如果有临时文件,则在下载的头中添加下载区域
            Request request;
            if ( !TextUtils.isEmpty(fileSize) ){
                request = new Request.Builder().url(bean.getUrl()).header("Range",fileSize).build();
            }else {
                request = new Request.Builder().url(bean.getUrl()).build();
            }
            Call call = client.newCall(request);
            try {
                bytes2File(call);
            } catch (IOException e) {
                Log.e("OCException",""+e);
                if (e.getMessage().contains("interrupted")){
                    Log.e("OCException","Download task: "+bean.getUrl()+" Canceled");
                    downloadPaused();
                }else {
                    downloadFailed();
                }
                return null;
            }
            downloadCompleted();
            return null;
        }

        /**
         * 当产生下载进度时
         * @param downloadedSize    当前下载的数据大小
         */
        public void onDownload(long downloadedSize) {
            bean.setDownloadedSize(downloadedSize);
            Log.d("下载进度", "名字:"+bean.getTitle()+"  总长:"+bean.getFileSize()+"  已下载:"+bean.getDownloadedSize() );
            updateItem(bean, bean.getFileSize(), downloadedSize);
        }

        /**
         * 下载完成后的操作
         */
        private void downloadCompleted(){
            //当前下载数减一
            runningThread -= 1;
            //将临时文件名更改回正式文件名
            downloadFile.renameTo(new File(DownloadConfig
                    .dowloadFilePath+"/"+bean.getTitle()));
            bean.setFilePath(DownloadConfig
                    .dowloadFilePath+"/"+bean.getTitle());
            bean.setFileMD5(FileHelper.getFileMD5(downloadFile));
            bean.setStatus(DownloadConfig.FINISH);
            fileSQLManager.saveDownLoadInfo(bean);
            //更新列表
            updateList();
            allTaskList.remove(bean.getFileID());
            threadExecutor.removeTag(bean.getFileID());

            startNextTask();
        }

        /**
         * 下载失败后的操作
         */
        private void downloadFailed(){
            runningThread -= 1;
            bean.setStatus(DownloadConfig.FAILED);
            updateList();
            threadExecutor.removeTag(bean.getFileID());

            startNextTask();
        }

        /**
         * 下载暂停后的操作
         */
        private void downloadPaused(){
            runningThread -= 1;
            bean.setStatus(DownloadConfig.PAUSED);
            updateList();
            threadExecutor.removeTag(bean.getFileID());

            startNextTask();
        }

        /**
         * 执行下一个等待中的任务
         */
        private void startNextTask(){
            if (allTaskList.size() > 0){
                //执行剩余的等待任务
                if(!checkTask(searchNextWaittingTask()) && !keepAlive && saveDownloadList()){
                    //如果当前没有被bind，而且没有等待中的任务，并保存状态成功，则服务结束
                    stopSelf();
                }
            }else if (allTaskList.size() <= 0 && !keepAlive){
                //如果没有了任务，同时也没有被bind，则服务结束
                stopSelf();
            }
        }

        /**
         * 查找一个等待中的任务
         * @return  查找到的任务信息Bean , 没有则返回 Null
         */
        private DownloadInfo searchNextWaittingTask(){
            for (DownloadInfo downloadInfo : allTaskList.values()) {
                if (downloadInfo.getStatus() == DownloadConfig.WAITTING) {
                    downloadInfo.setStatus(DownloadConfig.PAUSED);
                    return downloadInfo;
                }
            }
            return null;
        }

        /**
         * 将下载的数据存到本地文件
         * @param call  OkHttp的Call对象
         * @throws IOException  下载的异常
         */
        private void bytes2File(Call call) throws IOException {

            //设置输出流.
            OutputStream outPutStream;

            //检测是否支持断点续传
            Response response = call.execute();
            ResponseBody responseBody = response.body();
            String responeRange = response.headers().get("Content-Range");
            if (responeRange == null || !responeRange.contains(Long.toString(downloadFile.length()))){

                //最后的标记为 true 表示下载的数据可以从上一次的位置写入,否则会清空文件数据.
                outPutStream = new FileOutputStream(downloadFile,false);
            }else {
                outPutStream = new FileOutputStream(downloadFile,true);
            }

            InputStream inputStream = responseBody.byteStream();

            //如果有下载过的历史文件,则把下载总大小设为 总数据大小+文件大小 . 否则就是总数据大小
            if ( TextUtils.isEmpty(fileSize) ){
                bean.setFileSize(responseBody.contentLength());
            }else {
                bean.setFileSize(responseBody.contentLength() + downloadFile.length());
            }

            int length;
            //设置缓存大小
            byte[] buffer = new byte[1024];

            //开始写入文件
            while ((length = inputStream.read(buffer)) != -1){
                outPutStream.write(buffer,0,length);
                onDownload(downloadFile.length());
            }

            //清空缓冲区
            outPutStream.flush();
            outPutStream.close();
            inputStream.close();
            responseBody.close();
            response.close();
        }

    }


}
