package de.tudarmstadt.informatik.tk.assistance.profiler;

/**
 * @author Wladimir Schmidt (wlsc.dev@gmail.com)
 * @date 01.02.2016
 */
public class Config {

    private Config() {
    }

    public static final String DATABASE_NAME = "profiler.sqlite";

    public static final String VOLTAGE_NOW_PATH = "/sys/class/power_supply/battery/voltage_now";
    public static final String CURRENT_NOW_PATH = "/sys/class/power_supply/battery/current_now";
}