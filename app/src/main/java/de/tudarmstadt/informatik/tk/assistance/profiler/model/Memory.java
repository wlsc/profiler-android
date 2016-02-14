package de.tudarmstadt.informatik.tk.assistance.profiler.model;

/**
 * @author Wladimir Schmidt (wlsc.dev@gmail.com)
 * @date 05.02.2016
 */
public class Memory {

    // in KBs
    private long totalPss;

    public Memory(long totalPss) {
        this.totalPss = totalPss;
    }

    public long getTotalPss() {
        return this.totalPss;
    }

    @Override
    public String toString() {
        return "Memory{" +
                "totalPss=" + totalPss +
                '}';
    }
}