package net.oukranos.ophiuchus.types;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import net.oukranos.ophiuchus.utils.HealthDbHelper;
import net.oukranos.ophiuchus.utils.Logger;

/**
 * Created by francis on 3/26/16.
 */
public class HealthDb {
    private static HealthDb _healthDbInstance = null;
    private HealthDbHelper _dbHelper = null;
    private Context _context = null;

    private HealthDb(Context context) {
        _context = context;
    }

    public static HealthDb getInstance(Context context) {
        if (context == null) {
            return null;
        }

        if (_healthDbInstance == null) {
            _healthDbInstance = new HealthDb(context);
        }

        return _healthDbInstance;
    }

    public Cursor getHealthDataRecordCursor() {
        SQLiteDatabase db = getDatabase(false);

        String columns[] = {
                HealthDbHelper.C_ID,
                HealthDbHelper.C_STATUS,
                HealthDbHelper.C_CONTENT,
                HealthDbHelper.C_AUDIO_FILE
        };

        Cursor c = db.query(HealthDbHelper.TB_HDATA_NAME, columns, null, null, null, null, null);
        c.moveToFirst();

        return c;
    }

    public Cursor getUnsentHealthDataRecordCursor() {
        SQLiteDatabase db = getDatabase(false);

        String columns[] = {
                HealthDbHelper.C_ID,
                HealthDbHelper.C_STATUS,
                HealthDbHelper.C_CONTENT,
                HealthDbHelper.C_AUDIO_FILE
        };

        String filter = HealthDbHelper.C_STATUS + " = ?";
        String filterArgs[] = { "Not Sent" };

        Cursor c = db.query(HealthDbHelper.TB_HDATA_NAME, columns, filter, filterArgs, null, null, null);
        c.moveToFirst();

        return c;
    }

    public boolean storeHealthRecord(String content, String audioFileName) {
        SQLiteDatabase db = getDatabase(true);

        ContentValues values = new ContentValues();
        values.put(HealthDbHelper.C_CONTENT, content);
        values.put(HealthDbHelper.C_STATUS, "Not Sent");
        values.put(HealthDbHelper.C_AUDIO_FILE, audioFileName);

        long newRowId = db.insert(HealthDbHelper.TB_HDATA_NAME, "null", values);
        if (newRowId < 0) {
            return false;
        }

        db.close();

        return true;
    }

    public boolean setHealthRecordStatus(int id, String status) {
        SQLiteDatabase db = getDatabase(true);

        ContentValues values = new ContentValues();
        values.put(HealthDbHelper.C_STATUS, status);

        String where = "_id = ?";
        String whereArgs[] = { String.valueOf(id) };
        db.update(HealthDbHelper.TB_HDATA_NAME, values, where, whereArgs);

        db.close();

        return true;
    }

    public int countUnsentRecords() {
        SQLiteDatabase db = getDatabase(false);

        Cursor c = db.rawQuery("SELECT COUNT(' ') UNSENT_COUNT FROM " +
                        HealthDbHelper.TB_HDATA_NAME +  " WHERE " +
                        HealthDbHelper.C_STATUS + " = 'Not Sent'", null);

        int iUnsentCount;

        try {
            c.moveToFirst();
            int iUnsentCol = c.getColumnIndex("UNSENT_COUNT");
            iUnsentCount = c.getInt(iUnsentCol);
        } catch (Exception e) {
            Logger.err("Exception occurred: " + e.getMessage());
            iUnsentCount = 1;
        }

        db.close();

        return iUnsentCount;
    }

    public int getNextRecordId() {
        SQLiteDatabase db = getDatabase(false);

        Cursor c = db.rawQuery( "SELECT (MAX(" + HealthDbHelper.C_ID + ") + 1) MAX_ID FROM " +
                HealthDbHelper.TB_HDATA_NAME, null);

        int iMaxRecordId;

        try {
            c.moveToFirst();
            int maxIdCol = c.getColumnIndex("MAX_ID");
            iMaxRecordId = c.getInt(maxIdCol);
        } catch (Exception e) {
            Logger.err("Exception occurred: " + e.getMessage());
            iMaxRecordId = 1;
        }

        if (iMaxRecordId == 0) {
            iMaxRecordId = 1;
        }

        db.close();

        return iMaxRecordId;
    }

    private SQLiteDatabase getDatabase(boolean bWriteEnable) {
        if (_dbHelper == null) {
            _dbHelper = new HealthDbHelper(_context);
        }

        if (bWriteEnable) {
            return _dbHelper.getWritableDatabase();
        }

        return _dbHelper.getReadableDatabase();
    }
}
