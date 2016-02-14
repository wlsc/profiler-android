package de.tudarmstadt.informatik.tk.assistance.profiler.provider.dao;

import android.support.annotation.Nullable;

import java.util.Collections;
import java.util.List;

import de.greenrobot.dao.AbstractDao;
import de.greenrobot.dao.Property;

/**
 * @author Wladimir Schmidt (wlsc.dev@gmail.com)
 * @date 22.11.2015
 */
public abstract class CommonDaoImpl<T> implements CommonDao<T> {

    protected Property idProperty;

    protected AbstractDao<T, Long> dao;

    public CommonDaoImpl(AbstractDao<T, Long> dao) {

        if (this.dao == null) {
            this.dao = dao;
        }

        // general ID property of an SQLite table
        this.idProperty = new Property(0, Long.class, "id", true, "_id");
    }

    @Nullable
    @Override
    public T get(Long id) {

        if (id == null) {
            return null;
        }

        return dao
                .queryBuilder()
                .where(idProperty.eq(id))
                .limit(1)
                .build()
                .unique();
    }

    @Override
    public List<T> getAll() {
        return dao
                .queryBuilder()
                .build()
                .list();
    }

    @Override
    public List<T> getFirstN(int amount) {

        if (amount <= 0) {
            return Collections.emptyList();
        }

        return dao
                .queryBuilder()
                .limit(amount)
                .build()
                .list();
    }

    @Override
    public List<T> getLastN(int amount) {

        if (amount <= 0) {
            return Collections.emptyList();
        }

        return dao
                .queryBuilder()
                .orderDesc(idProperty)
                .limit(amount)
                .build()
                .list();
    }

    @Override
    public long insert(T dbItem) {

        if (dbItem == null) {
            return -1l;
        }

        return dao.insertOrReplace(dbItem);
    }

    @Override
    public void insert(List<T> dbItems) {

        if (dbItems == null || dbItems.isEmpty()) {
            return;
        }

        dao.insertOrReplaceInTx(dbItems);
    }

    @Override
    public void update(T dbItem) {

        if (dbItem == null) {
            return;
        }

        dao.update(dbItem);
    }

    @Override
    public void update(List<T> dbItems) {

        if (dbItems == null || dbItems.isEmpty()) {
            return;
        }

        dao.updateInTx(dbItems);
    }

    @Override
    public void delete(T dbItem) {

        if (dbItem == null) {
            return;
        }

        dao.delete(dbItem);
    }

    @Override
    public void delete(List<T> dbItems) {

        if (dbItems == null || dbItems.isEmpty()) {
            return;
        }

        dao.deleteInTx(dbItems);
    }
}
