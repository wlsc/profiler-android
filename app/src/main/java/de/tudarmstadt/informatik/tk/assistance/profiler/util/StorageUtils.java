package de.tudarmstadt.informatik.tk.assistance.profiler.util;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Environment;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;

import de.tudarmstadt.informatik.tk.assistance.profiler.Config;


/**
 * @author Wladimir Schmidt (wlsc.dev@gmail.com)
 * @date 01.09.2015
 */
public class StorageUtils {

    private StorageUtils() {
    }

    /**
     * Reads content of a cached internal file
     *
     * @param context
     * @param filename
     * @return
     * @throws IOException
     * @throws FileNotFoundException
     * @throws UnsupportedEncodingException
     */
    public static String readCachedInternalFile(Context context, String filename) throws IOException, FileNotFoundException, UnsupportedEncodingException {

        File folder = context.getCacheDir();
        File file = new File(folder, filename);
        String result = FileUtils.readData(file);

        if (result == null) {
            return "";
        }

        return result;
    }

    /**
     * Reads content of a cached external file
     *
     * @param context
     * @param filename
     * @return
     * @throws IOException
     * @throws FileNotFoundException
     * @throws UnsupportedEncodingException
     */
    public static String readCachedExternalFile(Context context, String filename) throws IOException, FileNotFoundException, UnsupportedEncodingException {

        File folder = context.getExternalCacheDir();
        File file = new File(folder, filename);
        String result = FileUtils.readData(file);

        if (result == null) {
            return "";
        }

        return result;
    }

    /**
     * Reads content of a private external file
     *
     * @param context
     * @param folderName
     * @param filename
     * @return
     * @throws IOException
     * @throws FileNotFoundException
     * @throws UnsupportedEncodingException
     */
    public static String readPrivateExternalFile(Context context, String folderName, String filename) throws IOException, FileNotFoundException, UnsupportedEncodingException {

        File folder = context.getExternalFilesDir(folderName);
        File file = new File(folder, filename);
        String result = FileUtils.readData(file);

        if (result == null) {
            return "";
        }

        return result;
    }

    /**
     * Reads content of a public external file
     *
     * @param folderName
     * @param filename
     * @return
     * @throws IOException
     * @throws FileNotFoundException
     * @throws UnsupportedEncodingException
     */
    public static String readPublicExternalFile(String folderName, String filename) throws IOException, FileNotFoundException, UnsupportedEncodingException {

        File folder = Environment.getExternalStoragePublicDirectory(folderName);
        File file = new File(folder, filename);
        String result = FileUtils.readData(file);

        if (result == null) {
            return "";
        }

        return result;
    }

    /**
     * Writes data to internal cache file
     *
     * @param context
     * @param filename
     * @param data
     * @throws IOException
     * @throws FileNotFoundException
     * @throws UnsupportedEncodingException
     */
    public static void saveCachedInternalFile(Context context, String filename, String data) throws IOException, FileNotFoundException, UnsupportedEncodingException {
        File folder = context.getCacheDir();
        File file = new File(folder, filename);
        FileUtils.writeData(file, data);
    }

    /**
     * Writes data to external cache file
     *
     * @param context
     * @param filename
     * @param data
     * @throws IOException
     * @throws FileNotFoundException
     * @throws UnsupportedEncodingException
     */
    public static void saveCachedExternalFile(Context context, String filename, String data) throws IOException, FileNotFoundException, UnsupportedEncodingException {
        File folder = context.getExternalCacheDir();
        File file = new File(folder, filename);
        FileUtils.writeData(file, data);
    }

    /**
     * Writes data to private external file
     *
     * @param context
     * @param folderName
     * @param filename
     * @param data
     * @throws IOException
     * @throws FileNotFoundException
     * @throws UnsupportedEncodingException
     */
    public static void savePrivateExternalFile(Context context, String folderName, String filename, String data) throws IOException, FileNotFoundException, UnsupportedEncodingException {
        File folder = context.getExternalFilesDir(folderName);
        File file = new File(folder, filename);
        FileUtils.writeData(file, data);
    }

    /**
     * Writes data to public external file
     *
     * @param folderName
     * @param filename
     * @param data
     * @throws IOException
     * @throws FileNotFoundException
     * @throws UnsupportedEncodingException
     */
    public static void savePublicExternalFile(String folderName, String filename, String data) throws IOException, FileNotFoundException, UnsupportedEncodingException {
        File folder = Environment.getExternalStoragePublicDirectory(folderName);
        File file = new File(folder, filename);
        FileUtils.writeData(file, data);
    }

    /**
     * Exports sqlite database to some destination folder
     *
     * @param context
     * @param dbPath
     * @return
     * @throws IOException
     */
    public static boolean exportDatabase(Context context, String dbPath) throws IOException {

        File newDb = new File(dbPath);
        File oldDb = new File(context.getDatabasePath(Config.DATABASE_NAME).getPath());

        // check if it exists
        if (oldDb.exists()) {

            FileInputStream fromFile = new FileInputStream(oldDb);
            FileOutputStream toFile = new FileOutputStream(newDb);

            FileUtils.copyFile(fromFile, toFile);

            Intent intent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
            intent.setData(Uri.fromFile(newDb));
            context.sendBroadcast(intent);

            fromFile.close();
            toFile.close();

            return true;
        }

        return false;
    }
}
