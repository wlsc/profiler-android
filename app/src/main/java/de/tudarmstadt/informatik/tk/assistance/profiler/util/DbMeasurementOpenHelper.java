package de.tudarmstadt.informatik.tk.assistance.profiler.util;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;

import de.tudarmstadt.informatik.tk.assistance.profiler.db.DaoMaster;

/**
 * @author Wladimir Schmidt (wlsc.dev@gmail.com)
 * @date 01.02.2016
 */
public class DbMeasurementOpenHelper extends DaoMaster.OpenHelper {

    private static final String TAG = DbMeasurementOpenHelper.class.getSimpleName();

    public DbMeasurementOpenHelper(Context context, String name, SQLiteDatabase.CursorFactory factory) {
        super(context, name, factory);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        super.onCreate(db);

        DaoMaster.createAllTables(db, true);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

        DaoMaster.dropAllTables(db, true);
        DaoMaster.createAllTables(db, true);
    }
}