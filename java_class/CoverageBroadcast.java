/**
 * @Time    : 2025 Mar
 * @Author  : Yuanhong Lan
 */

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class CoverageBroadcast extends BroadcastReceiver {
    private static final String TAG = "CoverageJacoco";

    public static void dumpCoverageData(Context context, String tag, boolean isAppend) {
        Log.d(TAG, "Dump coverage data!");
        OutputStream out = null;

        try {
            File baseDir = context.getExternalFilesDir("coverage" + "/" + context.getPackageName());
            if (baseDir == null) {
                Log.e(TAG, "External storage not available");
                return;
            }

            String timestamp = new SimpleDateFormat("yyyy-MM-dd-HH:mm:ss", Locale.US).format(new Date());
            String fileName = timestamp + "_" + tag + "_coverage.ec";

            File saveFile = new File(baseDir, fileName);

            File parentDir = saveFile.getParentFile();
            if (!parentDir.exists() && !parentDir.mkdirs()) {
                Log.e(TAG, "Failed to create directory: " + parentDir.getAbsolutePath());
                return;
            }

            out = new FileOutputStream(saveFile, isAppend);

            Object agent = Class.forName("org.jacoco.agent.rt.RT")
                    .getMethod("getAgent")
                    .invoke(null);
            out.write((byte[]) agent.getClass()
                    .getMethod("getExecutionData", boolean.class)
                    .invoke(agent, false));

            Log.d(TAG, "Coverage saved: " + saveFile.getAbsolutePath());
        } catch (Exception e) {
            Log.e(TAG, "Coverage dump failed", e);
        } finally {
            if (out != null) {
                try {
                    out.close();
                } catch (IOException e) {
                    Log.e(TAG, "Stream close failed", e);
                }
            }
        }
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        Log.d(TAG, "CoverageBroadcast broadcast received!");
        dumpCoverageData(context, "broadcast", false);
    }
}
