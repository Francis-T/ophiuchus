package net.oukranos.ophiuchus.utils;

import android.app.Activity;
import android.app.Service;
import android.content.Context;
import android.os.Environment;
import android.telephony.TelephonyManager;

import net.oukranos.ophiuchus.OphiuchusApp;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.Calendar;

/**
 * Created by francis on 3/25/16.
 */
public class AppUtils {
    /* Application Reference Utilities */
    public static OphiuchusApp getApplication(Activity activity) {
        if (activity == null) {
            return null;
        }

        return (OphiuchusApp) activity.getApplication();
    }

    public static OphiuchusApp getApplication(Service service) {
        if (service == null) {
            return null;
        }

        return (OphiuchusApp) service.getApplication();
    }

    /* Misc Utilities */
    public static String getTimestamp() {
        Calendar cal = Calendar.getInstance();

        int iTimeFields[] = {
                cal.get(Calendar.YEAR),
                cal.get(Calendar.MONTH),
                cal.get(Calendar.DAY_OF_MONTH),
                cal.get(Calendar.HOUR_OF_DAY),
                cal.get(Calendar.MINUTE),
                cal.get(Calendar.SECOND)
        };

        String result = "";
        for (int iTimeField : iTimeFields) {
            if (iTimeField < 0) {
                Logger.err("Invalid time value: " + iTimeField);
                return "";
            }

            if (iTimeField < 10) {
                result += "0" + iTimeField;
                continue;
            }

            result += iTimeField;
        }

        return result;
    }

    public static String getDeviceId(Context context) {
        TelephonyManager mgr =
                (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);

        return mgr.getDeviceId();
    }

    /* Filesystem Utilities */
    public static boolean fileExists(String path, String filename) {
        File checkFile = new File(path, filename);

        return checkFile.exists();
    }

    public static String getStoragePath() {
        return Environment.getExternalStorageDirectory().toString() + "/Ophiuchus/";
    }

    public static void prepareDirectory(String path) {
        File storagePath = new File(path);

        if (!storagePath.exists()) {
            storagePath.mkdirs();
        }

        return;
    }

    public static String getFileContentString(String filePath) {
        File loadFile = new File(filePath);

        if (!loadFile.exists()) {
            Logger.err("File does not exist: " + filePath);
            return null;
        }

        String contentStr = "";
        try {
            InputStream is = new FileInputStream(loadFile);

            byte buf[] = new byte[32];
            int iBytesRead = 0;

            while (is.available() > 0) {
                iBytesRead = is.read(buf, 0, 32);
                contentStr += new String(buf, 0, iBytesRead);
//                Logger.info("Current contents: " + contentStr);
//                contentStr += (char)(is.read());
            }

            is.close();
        } catch (Exception e) {
            Logger.err("Exception occurred: " + e.getMessage());
            contentStr = null;
        }

        return contentStr;
    }

    public static void saveToFile(String path, String filename, boolean bAppend, byte[] data) {
        File saveFile = new File(path, filename);

        try {
            if (!saveFile.exists()) {
                saveFile.createNewFile();
            }

            FileOutputStream fos = new FileOutputStream(saveFile, bAppend);
            fos.write(data);
            fos.close();

        } catch (Exception e) {
            Logger.err("Exception occurred: " + e.getMessage());
        }

        return;
    }
}
