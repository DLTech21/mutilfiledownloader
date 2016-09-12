package ocdownload;

import java.io.Serializable;

public class DownloadInfo implements Serializable{

    private String title;
    private String url;
    private long downloadedSize = 0L;
    private int status;

    private String userID;
    private String fileID;
    private String filePath;
    private long fileSize = 0L;
    private String fileMD5;
    private int filePage;
    private int fileIdType;
    private int fileType;

    public DownloadInfo() {
    }

    public DownloadInfo(String title, String url, String fileID) {
        this.title = title;
        this.url = url;
        this.status = DownloadConfig.PAUSED;
        this.fileMD5 = "";
        this.filePath = "";
        this.userID = "test";
        this.fileID = fileID;
    }

    public DownloadInfo(String title, String url, String userID, String fileID, long fileSize, int filePage, int fileIdType, int fileType) {
        this.title = title;
        this.url = url;
        this.userID = userID;
        this.fileID = fileID;
        this.fileSize = fileSize;
        this.filePage = filePage;
        this.fileIdType = fileIdType;
        this.fileType = fileType;
        //default
        this.status = DownloadConfig.PAUSED;
        this.fileMD5 = "";
        this.filePath = "";
    }

    public String getTextProgress(){
        return String.format("%.2f", downloadedSize/(1024.0*1024.0)) +"MB / "+String.format("%.2f", fileSize/(1024.0*1024.0))+"MB";
    }

    public int getProgress(){
        return (int) (((float)  downloadedSize / fileSize) * 100);
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }


    public long getDownloadedSize() {
        return downloadedSize;
    }

    public void setDownloadedSize(long downloadedSize) {
        this.downloadedSize = downloadedSize;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public String getUserID() {
        return userID;
    }

    public void setUserID(String userID) {
        this.userID = userID;
    }

    public String getFileID() {
        return fileID;
    }

    public void setFileID(String fileID) {
        this.fileID = fileID;
    }

    public String getFilePath() {
        return filePath;
    }

    public void setFilePath(String filePath) {
        this.filePath = filePath;
    }

    public long getFileSize() {
        return fileSize;
    }

    public void setFileSize(long fileSize) {
        this.fileSize = fileSize;
    }

    public String getFileMD5() {
        return fileMD5;
    }

    public void setFileMD5(String fileMD5) {
        this.fileMD5 = fileMD5;
    }

    public int getFilePage() {
        return filePage;
    }

    public void setFilePage(int filePage) {
        this.filePage = filePage;
    }

    public int getFileIdType() {
        return fileIdType;
    }

    public void setFileIdType(int fileIdType) {
        this.fileIdType = fileIdType;
    }

//    public int getFileIsDownloaded() {
//        return fileIsDownloaded;
//    }
//
//    public void setFileIsDownloaded(int fileIsDownloaded) {
//        this.fileIsDownloaded = fileIsDownloaded;
//    }

    public int getFileType() {
        return fileType;
    }

    public void setFileType(int fileType) {
        this.fileType = fileType;
    }
}
