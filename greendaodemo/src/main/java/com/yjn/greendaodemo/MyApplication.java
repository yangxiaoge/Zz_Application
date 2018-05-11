package com.yjn.greendaodemo;

import android.app.Application;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;

import com.facebook.stetho.Stetho;
import com.github.yuweiguocn.library.greendao.MigrationHelper;
import com.yjn.greendaodemo.bean.DaoMaster;
import com.yjn.greendaodemo.bean.DaoSession;
import com.yjn.greendaodemo.bean.UserDao;

import org.greenrobot.greendao.database.Database;

/**
 * Created by yangjianan on 2018/5/10.
 */

public class MyApplication extends Application {
    private String DBNAME = "sheep.db";
    private static DaoSession daoSession;

    @Override
    public void onCreate() {
        super.onCreate();
        setGreenDao();

        //初始化Stetho
        Stetho.initializeWithDefaults(this);
    }

    /**
     * 配置数据库
     */
    private void setGreenDao() {
//        DaoMaster.DevOpenHelper helper = new DaoMaster.DevOpenHelper(getApplicationContext(), DBNAME, null);
        MySQLiteOpenHelper helper = new MySQLiteOpenHelper(this, DBNAME, null);
        DaoMaster daoMaster = new DaoMaster(helper.getWritableDb());
        daoSession = daoMaster.newSession();
    }

    public static DaoSession getDaoInstant(){
        return daoSession;
    }


    public class MySQLiteOpenHelper extends DaoMaster.OpenHelper {
        public MySQLiteOpenHelper(Context context, String name, SQLiteDatabase.CursorFactory factory) {
            super(context, name, factory);
        }
        @Override
        public void onUpgrade(Database db, int oldVersion, int newVersion) {
            MigrationHelper.migrate(db, new MigrationHelper.ReCreateAllTableListener() {
                @Override
                public void onCreateAllTables(Database db, boolean ifNotExists) {
                    DaoMaster.createAllTables(db, ifNotExists);
                }
                @Override
                public void onDropAllTables(Database db, boolean ifExists) {
                    DaoMaster.dropAllTables(db, ifExists);
                }
            }, UserDao.class);
        }
    }
}
