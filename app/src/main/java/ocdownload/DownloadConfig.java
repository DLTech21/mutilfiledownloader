package ocdownload;

import android.os.Environment;

/**
 * Created by Donal on 16/9/9.
 */
public class DownloadConfig {
    public static final int MAX_DOWNLOADING_TASK = 3; // 最大同时下载数

    public static String userID = "test";
    public static String baseFilePath = Environment.getExternalStorageDirectory().toString()+ "/filedownloader";
    public static String dowloadFilePath = baseFilePath + "/" + userID + "/FILETEMP";
    /**下载文件的临时路径*/
    public static String tempDirPath = baseFilePath + "/" + userID + "/TEMPDir";

    public final static String TASK_DATA = "TASK_DATA";
    public final static String TASK_ACTION = "NEW_TASK";

    //service update intent action
    public static final String SINGLE_FINISH_UPDATE = "update_singel_finish";
    public static final String SINGLE_UPDATE = "update_singel";
    public static final String ALL_UPDATE = "update_all";
    public static final String SINGLE_DELETE = "update_singel_delete";

    public static final String PROGRESSBARLENGTH = "progressBarLength";
    public static final String DOWNLOADEDSIZE = "downloadedSize";
    public static final String TOTALSIZE = "totalSize";

    public static final int FAILED = 0;
    public static final int PAUSED = 1;
    public static final int WAITTING = 2;
    public static final int DOWNLOADING = 3;
    public static final int FINISH = 4;
}
