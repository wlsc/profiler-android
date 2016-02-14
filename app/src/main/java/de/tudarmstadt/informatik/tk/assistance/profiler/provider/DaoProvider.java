package de.tudarmstadt.informatik.tk.assistance.profiler.provider;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;

import de.greenrobot.dao.identityscope.IdentityScopeType;
import de.tudarmstadt.informatik.tk.assistance.profiler.Config;
import de.tudarmstadt.informatik.tk.assistance.profiler.db.DaoMaster;
import de.tudarmstadt.informatik.tk.assistance.profiler.db.DaoSession;
import de.tudarmstadt.informatik.tk.assistance.profiler.provider.dao.MeasurementDao;
import de.tudarmstadt.informatik.tk.assistance.profiler.provider.dao.MeasurementDaoImpl;
import de.tudarmstadt.informatik.tk.assistance.profiler.util.DbMeasurementOpenHelper;

/**
 * @author Wladimir Schmidt (wlsc.dev@gmail.com)
 * @date 01.02.2016
 */
public class DaoProvider {

    private static DaoProvider INSTANCE;

    private SQLiteDatabase mDb;
    private DaoMaster mDaoMaster;
    private DaoSession mDaoSession;

    /**
     * Constructor
     *
     * @param context
     */
    private DaoProvider(Context context) {

        DbMeasurementOpenHelper helper = new DbMeasurementOpenHelper(context, Config.DATABASE_NAME, null);
        mDb = helper.getWritableDatabase();

        mDaoMaster = new DaoMaster(mDb);
        mDaoSession = mDaoMaster.newSession(IdentityScopeType.None);
    }

    /**
     * Get database singleton
     *
     * @param context
     * @return
     */
    public static DaoProvider getInstance(Context context) {

        if (INSTANCE == null) {
            INSTANCE = new DaoProvider(context);
        }

        return INSTANCE;
    }

    /**
     * UserDao
     *
     * @return
     */
    public MeasurementDao getMeasurementDao() {
        return MeasurementDaoImpl.getInstance(mDaoSession);
    }
}