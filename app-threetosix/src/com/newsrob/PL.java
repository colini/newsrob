package com.newsrob;

import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import android.content.Context;
import android.util.Log;

import com.newsrob.threetosix.R;
import com.newsrob.util.U;

public class PL {

    static SimpleDateFormat sdf = new SimpleDateFormat("MM dd kk:mm");
    private final static String PATH = "/sdcard/newsrob.log";

    private static BlockingQueue<String> logQueue = new LinkedBlockingQueue<String>();

    private static Boolean isDebuggingEnabled = null;
    private static boolean threadStarted = false;

    public static void log(String message, Context context) {
        if (isDebuggingEnabled == null)
            isDebuggingEnabled = NewsRob.isDebuggingEnabled(context);

        if (isDebuggingEnabled) {
            checkThreadStarted();
            try {
                message = "<" + sdf.format(new Date()) + "> " + message;
                Log.d("NEWSROB LOGGING", message);

                logQueue.add(message);

            } catch (Throwable t) {
                Log.d("Oooops", "Problem when trying to write to debug log.", t);
            }
        }
    }

    protected static void checkThreadStarted() {

        if (!threadStarted) {
            Thread t = new Thread(new Runnable() {

                @Override
                public void run() {
                    while (true)
                        try {
                            writeToDisk(logQueue.take());
                        } catch (InterruptedException e) {
                            return;
                        }

                }
            }, "Logging Queue Writer");
            t.setDaemon(true);
            t.start();
            threadStarted = true;
        }

    }

    protected static void writeToDisk(String line) {

        PrintWriter pw;
        try {
            pw = new PrintWriter(new FileWriter(PATH, true));
            pw.println(line);
            pw.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    public static void log(String message, Throwable throwable, Context context) {
        StringBuilder msg = new StringBuilder(message + "\n");
        U.renderStackTrace(throwable, msg);
    }

}
