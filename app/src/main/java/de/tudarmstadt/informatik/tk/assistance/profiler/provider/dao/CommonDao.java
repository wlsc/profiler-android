package de.tudarmstadt.informatik.tk.assistance.profiler.provider.dao;

import android.support.annotation.Nullable;

import java.util.List;

/**
 * @author Wladimir Schmidt (wlsc.dev@gmail.com)
 * @date 22.11.2015
 */
public interface CommonDao<T> {

    @Nullable
    T get(Long id);

    List<T> getAll();

    List<T> getFirstN(int amount);

    List<T> getLastN(int amount);

    long insert(T dbItem);

    void insert(List<T> dbItems);

    void update(T dbItem);

    void update(List<T> dbItems);

    void delete(T dbItem);

    void delete(List<T> dbItems);
}
