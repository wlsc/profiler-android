package de.tudarmstadt.informatik.tk.assistance.profiler.activity;

import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.PowerManager;
import android.support.v4.app.NotificationCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.AppCompatButton;
import android.support.v7.widget.AppCompatTextView;
import android.util.Log;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.List;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import de.tudarmstadt.informatik.tk.assistance.profiler.Config;
import de.tudarmstadt.informatik.tk.assistance.profiler.R;
import de.tudarmstadt.informatik.tk.assistance.profiler.db.Measurement;
import de.tudarmstadt.informatik.tk.assistance.profiler.event.QueueNextExperimentEvent;
import de.tudarmstadt.informatik.tk.assistance.profiler.model.Memory;
import de.tudarmstadt.informatik.tk.assistance.profiler.provider.DaoProvider;
import de.tudarmstadt.informatik.tk.assistance.profiler.provider.dao.MeasurementDao;
import de.tudarmstadt.informatik.tk.assistance.profiler.util.SystemUtils;

public class WatcherActivity extends AppCompatActivity {

    private static final String TAG = "WatcherActivity";

    // export to CSV
    public static final char SEPARATOR_CSV = ';';
    public static final char NEW_LINE = '\n';

    // measure interval
    private static final int SAMPLING_RATE_IN_MILLIS = 50;
    // time to setup and turn off display
    private static final int SETUP_DELAY_IN_SEC = 10;
    // max running time of experiment
    private static final int MAX_RUNNING_INTERVAL_TIME_IN_SEC = 60;
    // how many intervals to repeat?
    private static final int MAX_EXPERIMENTS_NUMBER = 3;

    @Bind(R.id.maxIntervals)
    protected AppCompatTextView maxIntervals;

    @Bind(R.id.maxRunningTime)
    protected AppCompatTextView maxRunningTime;

    @Bind(R.id.startMeasurements)
    protected AppCompatButton startMeasurement;

//    @Bind(R.id.counter)
//    protected AppCompatTextView counterTv;

    private SystemUtils systemUtils;

    private boolean running;
    private boolean isFinished;

    private static ScheduledThreadPoolExecutor experimentScheduler;
    private static ScheduledThreadPoolExecutor notificationScheduler;

    private MeasurementDao dao;
    private PowerManager powerManager;
    private PowerManager.WakeLock wakeLock;
    private long startMeasurementsTime;
    private long lastMeasurementTime;
//    private boolean isUpdateUi;

    private int currentExperimentCounter;
    private Runnable experimentRunnable;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_watcher);

        if (!EventBus.getDefault().isRegistered(this)) {
            EventBus.getDefault().register(this);
        }

        systemUtils = SystemUtils.getInstance(this);

        dao = DaoProvider.getInstance(this).getMeasurementDao();

        powerManager = (PowerManager) getSystemService(Context.POWER_SERVICE);
        wakeLock = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, TAG);

//        isUpdateUi = true;

        setupExperiment();
    }

    private void setupExperiment() {

        byte[] buffer = new byte[100];

        experimentRunnable = () -> runOnUiThread(() -> {

            try {

                if (currentExperimentCounter > MAX_EXPERIMENTS_NUMBER) {
                    running = false;
                    isFinished = true;
                    if (wakeLock != null) {
                        if (wakeLock.isHeld()) {
                            wakeLock.release();
                            Log.d(TAG, "Wakelock was released");
                        }
                    } else {
                        Log.d(TAG, "Wakelock was NULL");
                    }
                    showCompletionNotification();
                    experimentScheduler.shutdownNow();
                    return;
                }

                if (startMeasurementsTime == 0) {
                    startMeasurementsTime = System.currentTimeMillis();
                }

                long probeTime = System.currentTimeMillis();
                long timeDiff = probeTime - startMeasurementsTime;
                long elapsedSec = TimeUnit.MILLISECONDS.toSeconds(timeDiff);

//                if (isUpdateUi) {
//                    counterTv.setText(String.valueOf(elapsedSec));
//                }

                if (elapsedSec >= MAX_RUNNING_INTERVAL_TIME_IN_SEC) {

                    experimentScheduler.shutdown();
                    exportDatabase();

                    if (currentExperimentCounter <= MAX_EXPERIMENTS_NUMBER) {
                        EventBus.getDefault().post(new QueueNextExperimentEvent());
                    }

                    return;
                }

                int len = 0;
                int voltageRaw, currentRaw = 0;

                // GET voltage (micro volt)
                try (FileInputStream fis = new FileInputStream(new File(Config.VOLTAGE_NOW_PATH))) {

                    len = fis.read(buffer);
                    voltageRaw = Integer.parseInt(new String(buffer, 0, len).trim());
                }

                // GET current (micro ampere)
                try (FileInputStream fis = new FileInputStream(new File(Config.CURRENT_NOW_PATH))) {

                    len = fis.read(buffer);
                    currentRaw = Integer.parseInt(new String(buffer, 0, len).trim());
                }

                // convert to normal
                float voltage = (float) (voltageRaw / 1_000.0);
                float current = (float) (currentRaw / 1_000.0);

                // get memory information
                Memory memory = systemUtils.getMemoryUsage();

                Measurement measurement = new Measurement();

                measurement.setPower((float) (voltage * current / 1_000.0));
                measurement.setMemory(memory.getTotalPss());
                measurement.setCpuLoad(systemUtils.getCPUUsage());
                measurement.setTimestamp(probeTime);

                dao.insert(measurement);


                lastMeasurementTime = probeTime;

            } catch (Exception e) {
                Log.e(TAG, "Error getting energy", e);
                if (wakeLock != null) {
                    if (wakeLock.isHeld()) {
                        wakeLock.release();
                        Log.d(TAG, "Wakelock was released");
                    }
                } else {
                    Log.d(TAG, "Wakelock was NULL");
                }
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
//        isUpdateUi = true;
        ButterKnife.bind(this);

        maxRunningTime.setText(String.valueOf(MAX_RUNNING_INTERVAL_TIME_IN_SEC) + " sec");
        maxIntervals.setText(String.valueOf(MAX_EXPERIMENTS_NUMBER));

        if (isFinished) {
//            counterTv.setText(String.valueOf(MAX_RUNNING_INTERVAL_TIME_IN_SEC));
            startMeasurement.setText(R.string.startMeasure);
        }

        if (notificationScheduler != null) {
            notificationScheduler.shutdownNow();
        }

        currentExperimentCounter = 0;
    }

    @Override
    protected void onPause() {
        super.onPause();
//        isUpdateUi = false;
        ButterKnife.unbind(this);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (EventBus.getDefault().isRegistered(this)) {
            EventBus.getDefault().unregister(this);
        }
    }

    @OnClick(R.id.startMeasurements)
    protected void onStartClick() {

        if (running) {

            if (wakeLock != null) {
                if (wakeLock.isHeld()) {
                    wakeLock.release();
                    Log.d(TAG, "Wakelock was released");
                }
            } else {
                Log.d(TAG, "Wakelock was NULL");
            }

            startMeasurement.setText(R.string.startMeasure);
            experimentScheduler.shutdown();
            experimentScheduler = null;
            isFinished = true;
            running = false;

            startMeasurementsTime = 0;

        } else {

            startMeasurement.setText(R.string.stopMeasure);
//            counterTv.setText("0");

            if (!wakeLock.isHeld()) {
                wakeLock.acquire();
                Log.d(TAG, "Wakelock was acquired");
            }

            EventBus.getDefault().post(new QueueNextExperimentEvent());
        }
    }

    /**
     * Plays a notification about completion of task
     */
    private void showCompletionNotification() {

        if (notificationScheduler != null) {
            notificationScheduler.shutdownNow();
            notificationScheduler = null;
        }

        notificationScheduler = new ScheduledThreadPoolExecutor(1);
        notificationScheduler.scheduleAtFixedRate(() -> {

            NotificationCompat.Builder builder =
                    new NotificationCompat.Builder(WatcherActivity.this);

            Uri alarmSound = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM);
            builder.setSound(alarmSound);

            NotificationManager manager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
            manager.notify(1, builder.build());

        }, 0, 2, TimeUnit.SECONDS);
    }

    private void exportDatabase() {

        try {

            File dir = new File(Environment.getExternalStorageDirectory() + File.separator + "/BatteryWatcher/");
            dir.mkdirs();

//            StorageUtils.exportDatabase(
//                    this,
//                    dir.getPath() + "/" + startMeasurementsTime + "_" + Config.DATABASE_NAME);

            // export to csv file: prepare data
            StringBuilder builder = new StringBuilder();
            List<Measurement> dataToExport = dao.getAll();

            for (Measurement measurement : dataToExport) {

                builder.append(measurement.getTimestamp())
                        .append(SEPARATOR_CSV)
                        .append(measurement.getPower())
                        .append(SEPARATOR_CSV)
                        .append(measurement.getCpuLoad())
                        .append(SEPARATOR_CSV)
                        .append(measurement.getMemory())
                        .append(NEW_LINE);
            }

            // export to csv file: export
            File file = new File(dir.getPath() + "/" + "exp" +
                    currentExperimentCounter + "_batterywatcher.csv");

            file.createNewFile();

            if (file.exists()) {
                OutputStream fo = new FileOutputStream(file);
                fo.write(builder.toString().getBytes("UTF-8"));
                fo.close();
            }

            // set it visible
            Intent intent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
            intent.setData(Uri.fromFile(file));
            sendBroadcast(intent);


        } catch (IOException e) {
            Log.e(TAG, "Cannot export database. Error: ", e);
        }
    }

    @Subscribe
    public void onEvent(QueueNextExperimentEvent event) {

        try {

            startMeasurementsTime = 0;

            // flush db
            dao.delete(dao.getAll());

            experimentScheduler = new ScheduledThreadPoolExecutor(1);
            experimentScheduler.scheduleAtFixedRate(
                    experimentRunnable,
                    SETUP_DELAY_IN_SEC * 1_000,
                    SAMPLING_RATE_IN_MILLIS,
                    TimeUnit.MILLISECONDS);

            currentExperimentCounter++;

        } catch (Exception e) {
            Log.d(TAG, "some error");
            if (wakeLock != null) {
                if (wakeLock.isHeld()) {
                    wakeLock.release();
                    Log.d(TAG, "Wakelock was released");
                }
            } else {
                Log.d(TAG, "Wakelock was NULL");
            }
        }

        running = true;
    }
}