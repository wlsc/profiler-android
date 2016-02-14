package de.tudarmstadt.informatik.tk.assistance.profiler.model;

/**
 * @author Wladimir Schmidt (wlsc.dev@gmail.com)
 * @date 05.02.2016
 */
public class Memory {

    // in KBs
    private long availableMemory;

    // in KBs
    private long totalMemory;

    // in KBs
    private long totalPss;

    public Memory(long availableMemory, long totalMemory, long totalPss) {
        this.availableMemory = availableMemory;
        this.totalMemory = totalMemory;
        this.totalPss = totalPss;
    }

    public long getAvailableMemory() {
        return this.availableMemory;
    }

    public long getTotalMemory() {
        return this.totalMemory;
    }

    public long getTotalPss() {
        return this.totalPss;
    }

    @Override
    public String toString() {
        return "Memory{" +
                "availableMemory=" + availableMemory +
                ", totalMemory=" + totalMemory +
                ", totalPss=" + totalPss +
                '}';
    }
}