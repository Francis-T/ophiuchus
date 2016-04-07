package net.oukranos.ophiuchus;

import android.app.Application;
import android.content.SharedPreferences;

/**
 * Created by francis on 3/25/16.
 */
public class OphiuchusApp extends Application {
    private static final String DATA_CACHE_ID = "net.oukranos.ophiuchus.cached_data";

    public void cacheHealthRecord(String data) {
        SharedPreferences prefs = getSharedPreferences(DATA_CACHE_ID, MODE_PRIVATE);
        prefs.edit().putString("HEALTH_RECORD_TEMP", data).commit();
        return;
    }

    public String retrieveCachedHealthRecord() {
        SharedPreferences prefs = getSharedPreferences(DATA_CACHE_ID, MODE_PRIVATE);
        return prefs.getString("HEALTH_RECORD_TEMP", "");
    }

    public void clearCachedHealthRecord() {
        SharedPreferences prefs = getSharedPreferences(DATA_CACHE_ID, MODE_PRIVATE);
        prefs.edit().putString("HEALTH_RECORD_TEMP", "").commit();

        return;
    }

    public void cacheAudioFilename(String filename) {
        SharedPreferences prefs = getSharedPreferences(DATA_CACHE_ID, MODE_PRIVATE);
        prefs.edit().putString("AUDIO_FILENAME_TEMP", filename).commit();
    }

    public String retrieveCachedAudioFilename() {
        SharedPreferences prefs = getSharedPreferences(DATA_CACHE_ID, MODE_PRIVATE);
        return prefs.getString("AUDIO_FILENAME_TEMP", "");
    }

    public void clearCachedAudioFilename() {
        SharedPreferences prefs = getSharedPreferences(DATA_CACHE_ID, MODE_PRIVATE);
        prefs.edit().putString("AUDIO_FILENAME_TEMP", "").commit();

        return;
    }

    public void setUploadServerUrl(String url) {
        SharedPreferences prefs = getSharedPreferences(DATA_CACHE_ID, MODE_PRIVATE);
        prefs.edit().putString("UPLOAD_SERVER_URL", url).commit();
    }

    public String getUploadServerUrl() {
        SharedPreferences prefs = getSharedPreferences(DATA_CACHE_ID, MODE_PRIVATE);
        return prefs.getString("UPLOAD_SERVER_URL", "");
    }


    public void setMedicalQueryFilePath(String path) {
        SharedPreferences prefs = getSharedPreferences(DATA_CACHE_ID, MODE_PRIVATE);
        prefs.edit().putString("MEDICAL_QUERY_FILE_PATH", path).commit();
    }

    public String getMedicalQueryFilePath() {
        SharedPreferences prefs = getSharedPreferences(DATA_CACHE_ID, MODE_PRIVATE);
        return prefs.getString("MEDICAL_QUERY_FILE_PATH", "");
    }
}
