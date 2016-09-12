package ocdownload;

import android.content.ContentValues;
import android.content.Context;

import net.sqlcipher.Cursor;
import net.sqlcipher.database.SQLiteDatabase;

import java.util.ArrayList;

/**
 * Created by Donal on 16/9/9.
 */
public class FileSQLManager {
    private SQLiteHelper dbhelper;
    private SQLiteDatabase db;
    private int doSaveTimes = 0;

    private static FileSQLManager m_pInstance;

    public FileSQLManager(Context context){
        this.dbhelper = new SQLiteHelper(context);
    }

    public static FileSQLManager getInstance(Context context) {
        synchronized (FileSQLManager.class) {
            if (m_pInstance == null) {
                m_pInstance = new FileSQLManager(context);
            }

            return m_pInstance;
        }
    }
    /**
     * (保存一个任务的下载信息到数据库)
     * @param downloadInfo
     */
    public void saveDownLoadInfo(DownloadInfo downloadInfo){
        ContentValues cv = new ContentValues();
        cv.put("user_id", downloadInfo.getUserID());
        cv.put("file_id", downloadInfo.getFileID());
        cv.put("file_name", downloadInfo.getTitle());
        cv.put("file_page", downloadInfo.getFilePath());
        cv.put("file_size", downloadInfo.getFileSize());
        cv.put("file_type", downloadInfo.getFileType());
        cv.put("file_id_type", downloadInfo.getFileIdType());
        cv.put("file_download_status", downloadInfo.getStatus());
        cv.put("file_local_path", downloadInfo.getFilePath());
        cv.put("file_md5", downloadInfo.getFileMD5());
        cv.put("url", downloadInfo.getUrl());
        cv.put("downloadsize", downloadInfo.getDownloadedSize());
        Cursor cursor = null;
        try{
            db = dbhelper.getWritableDatabase("123456");
            cursor = db.rawQuery(
                    "SELECT * from " + SQLiteHelper.TABLE_NAME
                            + " WHERE user_id = ? AND file_id = ? ", new String[]{downloadInfo.getUserID(),downloadInfo.getFileID()});
            if(cursor.moveToNext()){
                db.update(SQLiteHelper.TABLE_NAME, cv, "user_id = ? AND file_id = ? ", new String[]{downloadInfo.getUserID(),downloadInfo.getFileID()});
            }else{
                db.insert(SQLiteHelper.TABLE_NAME, null, cv);
            }
            cursor.close();
            db.close();
        }catch(Exception e){
            doSaveTimes ++;
            if(doSaveTimes < 5){ //最多只做5次数据保存，降低数据保存失败率
                saveDownLoadInfo(downloadInfo);
            }else{
                doSaveTimes = 0;
            }
            if(cursor != null){
                cursor.close();
            }
            if(db != null){
                db.close();
            }
        }
        doSaveTimes = 0;
    }

    public DownloadInfo getDownLoadInfo(String userID, String fileID){
        DownloadInfo downloadinfo= null;
        db = dbhelper.getWritableDatabase("123456");
        Cursor cursor = db.rawQuery(
                "SELECT * from " + SQLiteHelper.TABLE_NAME
                        + "WHERE user_id = ? AND file_id = ? ", new String[]{userID, fileID});
        if(cursor.moveToNext()){
            downloadinfo = new DownloadInfo();
            downloadinfo.setDownloadedSize(cursor.getLong(cursor.getColumnIndex("downloadsize")));
            downloadinfo.setTitle(cursor.getString(cursor.getColumnIndex("file_name")));
            downloadinfo.setFilePath(cursor.getString(cursor.getColumnIndex("file_local_path")));
            downloadinfo.setFileSize(cursor.getLong(cursor.getColumnIndex("file_size")));
            downloadinfo.setFilePage(cursor.getInt(cursor.getColumnIndex("file_page")));
            downloadinfo.setUrl(cursor.getString(cursor.getColumnIndex("url")));
            downloadinfo.setFileID(cursor.getString(cursor.getColumnIndex("file_id")));
            downloadinfo.setUserID(cursor.getString(cursor.getColumnIndex("user_id")));
            downloadinfo.setFileIdType(cursor.getInt(cursor.getColumnIndex("file_id_type")));
            downloadinfo.setFileType(cursor.getInt(cursor.getColumnIndex("file_type")));
            downloadinfo.setStatus(cursor.getInt(cursor.getColumnIndex("file_download_status")));
            downloadinfo.setFileMD5(cursor.getString(cursor.getColumnIndex("file_md5")));
        }
        cursor.close();
        db.close();
        return downloadinfo;
    }

    public ArrayList<DownloadInfo> getUserDownLoadInfo(String userID){
        ArrayList<DownloadInfo> downloadinfoList = new ArrayList<DownloadInfo>();
        db = dbhelper.getWritableDatabase("123456");
        try {
            Cursor cursor = null;
            cursor = db.rawQuery(
                    "SELECT * from " + SQLiteHelper.TABLE_NAME + " WHERE user_id = '" + userID +"'", null);
            while(cursor.moveToNext()){
                DownloadInfo downloadinfo = new DownloadInfo();
                downloadinfo.setDownloadedSize(cursor.getLong(cursor.getColumnIndex("downloadsize")));
                downloadinfo.setTitle(cursor.getString(cursor.getColumnIndex("file_name")));
                downloadinfo.setFilePath(cursor.getString(cursor.getColumnIndex("file_local_path")));
                downloadinfo.setFileSize(cursor.getLong(cursor.getColumnIndex("file_size")));
                downloadinfo.setFilePage(cursor.getInt(cursor.getColumnIndex("file_page")));
                downloadinfo.setUrl(cursor.getString(cursor.getColumnIndex("url")));
                downloadinfo.setFileID(cursor.getString(cursor.getColumnIndex("file_id")));
                downloadinfo.setUserID(cursor.getString(cursor.getColumnIndex("user_id")));
                downloadinfo.setFileIdType(cursor.getInt(cursor.getColumnIndex("file_id_type")));
                downloadinfo.setFileType(cursor.getInt(cursor.getColumnIndex("file_type")));
                downloadinfo.setStatus(cursor.getInt(cursor.getColumnIndex("file_download_status")));
                downloadinfo.setFileMD5(cursor.getString(cursor.getColumnIndex("file_md5")));
                downloadinfoList.add(downloadinfo);
            }
            cursor.close();
        } catch (Exception e) {
            e.printStackTrace();
        }

        db.close();
        return downloadinfoList;
    }

    public ArrayList<DownloadInfo> getUserDownLoadInfoUnfinish(String userID){
        ArrayList<DownloadInfo> downloadinfoList = new ArrayList<DownloadInfo>();
        db = dbhelper.getWritableDatabase("123456");
        try {
            Cursor cursor = null;
            cursor = db.rawQuery(
                    "SELECT * from " + SQLiteHelper.TABLE_NAME + "WHERE user_id = ? AND file_download_status != ? ", new String[]{userID, DownloadConfig.FINISH+""});
            while(cursor.moveToNext()){
                DownloadInfo downloadinfo = new DownloadInfo();
                downloadinfo.setDownloadedSize(cursor.getLong(cursor.getColumnIndex("downloadsize")));
                downloadinfo.setTitle(cursor.getString(cursor.getColumnIndex("file_name")));
                downloadinfo.setFilePath(cursor.getString(cursor.getColumnIndex("file_local_path")));
                downloadinfo.setFileSize(cursor.getLong(cursor.getColumnIndex("file_size")));
                downloadinfo.setFilePage(cursor.getInt(cursor.getColumnIndex("file_page")));
                downloadinfo.setUrl(cursor.getString(cursor.getColumnIndex("url")));
                downloadinfo.setFileID(cursor.getString(cursor.getColumnIndex("file_id")));
                downloadinfo.setUserID(cursor.getString(cursor.getColumnIndex("user_id")));
                downloadinfo.setFileIdType(cursor.getInt(cursor.getColumnIndex("file_id_type")));
                downloadinfo.setFileType(cursor.getInt(cursor.getColumnIndex("file_type")));
                downloadinfo.setStatus(cursor.getInt(cursor.getColumnIndex("file_download_status")));
                downloadinfo.setFileMD5(cursor.getString(cursor.getColumnIndex("file_md5")));
                downloadinfoList.add(downloadinfo);
            }
            cursor.close();
        } catch (Exception e) {
            e.printStackTrace();
        }

        db.close();
        return downloadinfoList;
    }

    public ArrayList<DownloadInfo> getUserDownLoadInfofinish(String userID){
        ArrayList<DownloadInfo> downloadinfoList = new ArrayList<DownloadInfo>();
        db = dbhelper.getWritableDatabase("123456");
        try {
            Cursor cursor = null;
            cursor = db.rawQuery(
                    "SELECT * from " + SQLiteHelper.TABLE_NAME + "WHERE user_id = ? AND file_download_status = ? ", new String[]{userID, DownloadConfig.FINISH+""});
            while(cursor.moveToNext()){
                DownloadInfo downloadinfo = new DownloadInfo();
                downloadinfo.setDownloadedSize(cursor.getLong(cursor.getColumnIndex("downloadsize")));
                downloadinfo.setTitle(cursor.getString(cursor.getColumnIndex("file_name")));
                downloadinfo.setFilePath(cursor.getString(cursor.getColumnIndex("file_local_path")));
                downloadinfo.setFileSize(cursor.getLong(cursor.getColumnIndex("file_size")));
                downloadinfo.setFilePage(cursor.getInt(cursor.getColumnIndex("file_page")));
                downloadinfo.setUrl(cursor.getString(cursor.getColumnIndex("url")));
                downloadinfo.setFileID(cursor.getString(cursor.getColumnIndex("file_id")));
                downloadinfo.setUserID(cursor.getString(cursor.getColumnIndex("user_id")));
                downloadinfo.setFileIdType(cursor.getInt(cursor.getColumnIndex("file_id_type")));
                downloadinfo.setFileType(cursor.getInt(cursor.getColumnIndex("file_type")));
                downloadinfo.setStatus(cursor.getInt(cursor.getColumnIndex("file_download_status")));
                downloadinfo.setFileMD5(cursor.getString(cursor.getColumnIndex("file_md5")));
                downloadinfoList.add(downloadinfo);
            }
            cursor.close();
        } catch (Exception e) {
            e.printStackTrace();
        }

        db.close();
        return downloadinfoList;
    }

    public ArrayList<DownloadInfo> getUserDownLoadInfoUnfinishFiletype(String userID, int fileType){
        ArrayList<DownloadInfo> downloadinfoList = new ArrayList<DownloadInfo>();
        db = dbhelper.getWritableDatabase("123456");
        try {
            Cursor cursor = null;
            cursor = db.rawQuery(
                    "SELECT * from " + SQLiteHelper.TABLE_NAME + "WHERE user_id = ? AND file_download_status != ? AND file_type = ? ", new String[]{userID, DownloadConfig.FINISH+"", fileType+""});
            while(cursor.moveToNext()){
                DownloadInfo downloadinfo = new DownloadInfo();
                downloadinfo.setDownloadedSize(cursor.getLong(cursor.getColumnIndex("downloadsize")));
                downloadinfo.setTitle(cursor.getString(cursor.getColumnIndex("file_name")));
                downloadinfo.setFilePath(cursor.getString(cursor.getColumnIndex("file_local_path")));
                downloadinfo.setFileSize(cursor.getLong(cursor.getColumnIndex("file_size")));
                downloadinfo.setFilePage(cursor.getInt(cursor.getColumnIndex("file_page")));
                downloadinfo.setUrl(cursor.getString(cursor.getColumnIndex("url")));
                downloadinfo.setFileID(cursor.getString(cursor.getColumnIndex("file_id")));
                downloadinfo.setUserID(cursor.getString(cursor.getColumnIndex("user_id")));
                downloadinfo.setFileIdType(cursor.getInt(cursor.getColumnIndex("file_id_type")));
                downloadinfo.setFileType(cursor.getInt(cursor.getColumnIndex("file_type")));
                downloadinfo.setStatus(cursor.getInt(cursor.getColumnIndex("file_download_status")));
                downloadinfo.setFileMD5(cursor.getString(cursor.getColumnIndex("file_md5")));
                downloadinfoList.add(downloadinfo);
            }
            cursor.close();
        } catch (Exception e) {
            e.printStackTrace();
        }

        db.close();
        return downloadinfoList;
    }

    public ArrayList<DownloadInfo> getUserDownLoadInfoFinishFiletype(String userID, int fileType){
        ArrayList<DownloadInfo> downloadinfoList = new ArrayList<DownloadInfo>();
        db = dbhelper.getWritableDatabase("123456");
        try {
            Cursor cursor = null;
            cursor = db.rawQuery(
                    "SELECT * from " + SQLiteHelper.TABLE_NAME + "WHERE user_id = ? AND file_download_status = ? AND file_type = ? ", new String[]{userID, DownloadConfig.FINISH+"", fileType+""});
            while(cursor.moveToNext()){
                DownloadInfo downloadinfo = new DownloadInfo();
                downloadinfo.setDownloadedSize(cursor.getLong(cursor.getColumnIndex("downloadsize")));
                downloadinfo.setTitle(cursor.getString(cursor.getColumnIndex("file_name")));
                downloadinfo.setFilePath(cursor.getString(cursor.getColumnIndex("file_local_path")));
                downloadinfo.setFileSize(cursor.getLong(cursor.getColumnIndex("file_size")));
                downloadinfo.setFilePage(cursor.getInt(cursor.getColumnIndex("file_page")));
                downloadinfo.setUrl(cursor.getString(cursor.getColumnIndex("url")));
                downloadinfo.setFileID(cursor.getString(cursor.getColumnIndex("file_id")));
                downloadinfo.setUserID(cursor.getString(cursor.getColumnIndex("user_id")));
                downloadinfo.setFileIdType(cursor.getInt(cursor.getColumnIndex("file_id_type")));
                downloadinfo.setFileType(cursor.getInt(cursor.getColumnIndex("file_type")));
                downloadinfo.setStatus(cursor.getInt(cursor.getColumnIndex("file_download_status")));
                downloadinfo.setFileMD5(cursor.getString(cursor.getColumnIndex("file_md5")));
                downloadinfoList.add(downloadinfo);
            }
            cursor.close();
        } catch (Exception e) {
            e.printStackTrace();
        }

        db.close();
        return downloadinfoList;
    }

    public void deleteDownLoadInfo(String userID,String fileID){
        db = dbhelper.getWritableDatabase("123456");
        db.delete(SQLiteHelper.TABLE_NAME, "user_id = ? AND file_id = ? ", new String[]{userID, fileID});
        db.close();
    }

}
