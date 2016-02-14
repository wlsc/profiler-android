package de.tudarmstadt.informatik.tk.assistance.profiler.util;

import android.content.Context;
import android.os.Build;
import android.os.Environment;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.nio.channels.FileChannel;


public class FileUtils {

    private FileUtils() {
    }

    /**
     * Checks if external storage is available for read and write.
     */
    public static boolean isExternalStorageWritable() {
        String state = Environment.getExternalStorageState();
        return Environment.MEDIA_MOUNTED.equals(state);
    }

    public static File getPrivateFilesDir(Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.FROYO) {
            return context.getExternalFilesDir(null);
        } else {
            String packageName = context.getPackageName();
            File externalPath = Environment.getExternalStorageDirectory();
            File appFiles = new File(externalPath.getAbsolutePath() +
                    "/Android/data/" + packageName + "/files");
            appFiles.mkdirs();
            return appFiles;
        }
    }

    /**
     * Reads a file and returns the contents as String.
     *
     * @param stream the InputStream from the file
     * @return a String with the file contents
     * @throws IOException
     */
    public static String readFile(InputStream stream) throws IOException {
        BufferedReader reader = new BufferedReader(new InputStreamReader(stream));
        StringBuilder sb = new StringBuilder();
        String line;
        while ((line = reader.readLine()) != null) {
            sb.append(line);
        }
        return sb.toString();
    }

    /**
     * Reads a file and returns the contents as String.
     *
     * @param f the File object
     * @return a String with the file contents.
     * @throws IOException
     */
    public static String readFile(File f) throws IOException {
        FileInputStream stream = new FileInputStream(f);
        return readFile(stream);
    }

    /**
     * Reads file into a string
     *
     * @param file
     * @return
     * @throws IOException
     * @throws FileNotFoundException
     * @throws UnsupportedEncodingException
     */
    public static String readData(File file) throws IOException, FileNotFoundException, UnsupportedEncodingException {

        FileInputStream fileInputStream = null;

        try {

            fileInputStream = new FileInputStream(file);
            StringBuilder sb = new StringBuilder();
            int read;

            while ((read = fileInputStream.read()) != -1) {
                sb.append((char) read);
            }

            return sb.toString();
        } finally {
            if (fileInputStream != null) {
                fileInputStream.close();
            }
        }
    }

    /**
     * Writes a file with the given content.
     *
     * @param f    the File object
     * @param text the contents
     * @throws IOException
     */
    public static void writeFile(File f, final String text) throws IOException {

        OutputStreamWriter osw = new OutputStreamWriter(new FileOutputStream(f), "UTF-8");
        osw.write(text);
        osw.close();
    }

    /**
     * Write data to given file
     *
     * @param file
     * @param data
     */
    public static void writeData(File file, String data) throws IOException, FileNotFoundException, UnsupportedEncodingException {

        FileOutputStream fileOutputStream = null;

        try {
            if (!file.exists()) {
                file.createNewFile();
            }

            fileOutputStream = new FileOutputStream(file);
            byte[] bytes = data.getBytes("UTF-8");
            fileOutputStream.write(bytes);
        } finally {
            if (fileOutputStream != null) {
                fileOutputStream.close();
            }
        }
    }

    /**
     * Copies sqlite database file from source to destination folder
     *
     * @param fromFile
     * @param toFile
     * @throws IOException
     */
    public static void copyFile(FileInputStream fromFile, FileOutputStream toFile) throws IOException {

        FileChannel fromChannel = null;
        FileChannel toChannel = null;

        try {
            fromChannel = fromFile.getChannel();
            toChannel = toFile.getChannel();
            fromChannel.transferTo(0, fromChannel.size(), toChannel);
        } finally {
            try {
                if (fromChannel != null) {
                    fromChannel.close();
                }
            } finally {
                if (toChannel != null) {
                    toChannel.close();
                }
            }
        }
    }
}
