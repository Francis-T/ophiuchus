package net.oukranos.ophiuchus.utils;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * Created by francis on 3/24/16.
 */
public class HealthDbHelper extends SQLiteOpenHelper {
    private static final String DB_NAME = "DB_HEALTH";
    private static final int DB_VERSION = 1;

    public static final String TB_HDATA_NAME  = "TB_HEALTH_DATA";
    public static final String C_ID             = "_id";
    public static final String C_STATUS         = "C_STATUS";
    public static final String C_CONTENT        = "C_CONTENT";
    public static final String C_AUDIO_FILE     = "C_AUDIO_FILE_NAME";

    private static final String C_ID_TYPE           = "INTEGER PRIMARY KEY";
    private static final String C_STATUS_TYPE       = "CHAR(10)";
    private static final String C_CONTENT_TYPE      = "VARCHAR(1024)";
    private static final String C_AUDIO_FILE_TYPE   = "C_AUDIO_FILE_NAME";


    private static final String TB_HDATA_CREATE =
            "CREATE TABLE " + TB_HDATA_NAME + "( " +
                    C_ID            + " " + C_ID_TYPE + ", " +
                    C_STATUS        + " " + C_STATUS_TYPE + ", " +
                    C_CONTENT       + " " + C_CONTENT_TYPE + ", " +
                    C_AUDIO_FILE    + " " + C_AUDIO_FILE_TYPE + ");";

    public HealthDbHelper(Context context) {
        super(context, DB_NAME, null, DB_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(TB_HDATA_CREATE);
        return;
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }
}
