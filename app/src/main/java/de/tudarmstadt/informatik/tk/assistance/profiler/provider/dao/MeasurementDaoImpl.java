package de.tudarmstadt.informatik.tk.assistance.profiler.provider.dao;

import de.tudarmstadt.informatik.tk.assistance.profiler.db.DaoSession;
import de.tudarmstadt.informatik.tk.assistance.profiler.db.Measurement;

/**
 * @author Wladimir Schmidt (wlsc.dev@gmail.com)
 * @date 01.02.2016
 */
public class MeasurementDaoImpl extends CommonDaoImpl<Measurement> implements MeasurementDao {

    private static MeasurementDao INSTANCE;

    private MeasurementDaoImpl(DaoSession session) {
        super(session.getMeasurementDao());
    }

    public static MeasurementDao getInstance(DaoSession mDaoSession) {

        if (INSTANCE == null) {
            INSTANCE = new MeasurementDaoImpl(mDaoSession);
        }

        return INSTANCE;
    }
}