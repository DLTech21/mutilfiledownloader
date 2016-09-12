//
//  SQLiteHelper.java
//  FeOA
//
//  Created by LuTH on 2011-12-17.
//  Copyright 2011 flyrise. All rights reserved.
//
package ocdownload;

import android.content.Context;

import net.sqlcipher.database.SQLiteDatabase;
import net.sqlcipher.database.SQLiteOpenHelper;

/**
 * Created by donal on 16/7/30.
 */
public class SQLiteHelper extends SQLiteOpenHelper {

	private static final String mDatabasename = "filedownloader";
	private static SQLiteDatabase.CursorFactory mFactory = null;
	private static final int mVersion = 1;
	public static final String TABLE_NAME = "downloadinfo"; //文件下载信息数据表名称

	public SQLiteHelper(Context context) {
		super(context, mDatabasename, mFactory, mVersion);
	}

	public SQLiteHelper(Context context, String name, SQLiteDatabase.CursorFactory factory,
			int version) {
		super(context, name, factory, version);
	}

	@Override
	public void onCreate(SQLiteDatabase db) {

		//创建文件下载信息数据表
		String downloadsql = "CREATE TABLE IF NOT EXISTS "+ TABLE_NAME +" ("
                + "id INTEGER PRIMARY KEY  AUTOINCREMENT  NOT NULL , "
                + "user_id VARCHAR, "
                + "file_id VARCHAR, "
                + "url VARCHAR, " 
                + "file_name VARCHAR, "
                + "file_page VARCHAR, "
                + "file_size VARCHAR, "
				+ "file_type INTEGER, "
				+ "file_id_type INTEGER, "
				+ "file_download_status INTEGER, "
				+ "file_local_path VARCHAR, "
				+ "file_md5 VARCHAR, "
				+ "downloadsize VARCHAR "
                + ")";
        db.execSQL(downloadsql);
        

	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
	}

	@Override
	public void onOpen(SQLiteDatabase db) {
		super.onOpen(db);
	}
}
