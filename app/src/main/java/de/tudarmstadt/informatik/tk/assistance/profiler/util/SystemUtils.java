package de.tudarmstadt.informatik.tk.assistance.profiler.util;

import android.app.ActivityManager;
import android.content.Context;
import android.os.Debug;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.List;
import java.util.regex.Pattern;

import de.tudarmstadt.informatik.tk.assistance.profiler.model.Memory;

/**
 * @author Wladimir Schmidt (wlsc.dev@gmail.com)
 * @date 04.02.2016
 */
public class SystemUtils {

    private static SystemUtils INSTANCE;
    private final Context context;

    private SystemUtils(Context context) {
        this.context = context;
    }

    public static SystemUtils getInstance(Context context) {

        if (INSTANCE == null) {
            INSTANCE = new SystemUtils(context);
        }

        return INSTANCE;
    }

    /**
     * Returns CPU usage along all cores
     *
     * @return
     */
    public float getCPUUsage() {

        try (RandomAccessFile reader = new RandomAccessFile("/proc/stat", "r")) {

            String load = reader.readLine();

            String[] toks = load.split(" +");

            long idle1 = Long.parseLong(toks[4]);
            long cpu1 = Long.parseLong(toks[2]) + Long.parseLong(toks[3]) + Long.parseLong(toks[5])
                    + Long.parseLong(toks[6]) + Long.parseLong(toks[7]) + Long.parseLong(toks[8]);

            try {
                Thread.sleep(40);
            } catch (Exception e) {
            }

            reader.seek(0);
            load = reader.readLine();

            toks = load.split(" +");

            long idle2 = Long.parseLong(toks[4]);
            long cpu2 = Long.parseLong(toks[2]) + Long.parseLong(toks[3]) + Long.parseLong(toks[5])
                    + Long.parseLong(toks[6]) + Long.parseLong(toks[7]) + Long.parseLong(toks[8]);

            return (float) (cpu2 - cpu1) / ((cpu2 + idle2) - (cpu1 + idle1));

        } catch (IOException ex) {
            ex.printStackTrace();
        }

        return 0.0f;
    }

    /**
     * Returns memory usage for an app in KBs
     *
     * @return
     */
    public Memory getMemoryUsage() {

        ActivityManager.MemoryInfo memoryInfo = new ActivityManager.MemoryInfo();
        ActivityManager activityManager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        activityManager.getMemoryInfo(memoryInfo);

        int[] pids = new int[6];
//        pids[0] = android.os.Process.myPid();

        ActivityManager manager = (ActivityManager) context.getApplicationContext().getSystemService(
                context.getApplicationContext().ACTIVITY_SERVICE);
        List<ActivityManager.RunningAppProcessInfo> activities = ((ActivityManager) manager).getRunningAppProcesses();

        int c = 0;
        for (ActivityManager.RunningAppProcessInfo processInfo : activities) {

            if (processInfo.processName.contains("de.tudarmstadt.informatik.tk.assistance1") ||
                    processInfo.processName.contains("de.tudarmstadt.informatik.tk.assistance2") ||
                    processInfo.processName.contains("de.tudarmstadt.informatik.tk.assistance3") ||
                    processInfo.processName.contains("de.tudarmstadt.informatik.tk.assistance4") ||
                    processInfo.processName.contains("de.tudarmstadt.informatik.tk.assistance5") ||
                    processInfo.processName.contains("de.tudarmstadt.informatik.tk.assistancemodules")) {

                pids[c] = processInfo.pid;
                c++;
            }

        }

        long totalMemoryPss = 0;

        Debug.MemoryInfo[] info = activityManager.getProcessMemoryInfo(pids);

        for (Debug.MemoryInfo memInfo : info) {
            totalMemoryPss += memInfo.getTotalPss();
        }

        return new Memory(totalMemoryPss);
    }

    /**
     * Returns number of cores
     *
     * @return
     */
    public int getNumCores() {

        try {

            File dir = new File("/sys/devices/system/cpu/");
            File[] files = dir.listFiles(pathname -> Pattern.matches("cpu[0-9]+", pathname.getName()));

            return files.length;

        } catch (Exception e) {
            return 1;
        }
    }
}