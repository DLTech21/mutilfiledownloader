package ocdownload;

import android.content.Context;
import android.content.Intent;
import android.text.TextUtils;

import java.io.File;
import java.util.ArrayList;

/**
 * Created by Donal on 16/9/9.
 */
public class FileLoader {
    private static FileLoader m_pInstance;
    private FileSQLManager fileSQLManager;
    private Context context;

    public FileLoader(Context context) {
        this.context = context;
        fileSQLManager = new FileSQLManager(context);
    }

    public static FileLoader getInstance(Context context) {
        synchronized (FileLoader.class) {
            if (m_pInstance == null) {
                m_pInstance = new FileLoader(context);
            }

            return m_pInstance;
        }
    }

    public String getLocalPathFrom(String userID, String fileID) {
        DownloadInfo downloadInfo = fileSQLManager.getDownLoadInfo(userID, fileID);
        if (downloadInfo != null) {
            if (downloadInfo.getStatus() == DownloadConfig.FINISH && !(TextUtils.isEmpty(downloadInfo.getFilePath())) ) {
                File file = new File(downloadInfo.getFilePath());
                if (file.exists()) {
                    String md5 = FileHelper.getFileMD5(file);
                    if (md5.equals(downloadInfo.getFileMD5())) {
                        return downloadInfo.getFilePath();
                    }
                    else

                        return null;
                }
                else
                    return null;
            }
            else {
                return null;
            }
        }
        return null;
    }

    public void startDownloadFile(ArrayList<DownloadInfo> downloadInfo) {
        Intent intent = new Intent(context, DownloadService.class);
        intent.setAction(DownloadConfig.TASK_ACTION);
        intent.putExtra(DownloadConfig.TASK_DATA, downloadInfo);
        context.startService(intent);
    }

}
