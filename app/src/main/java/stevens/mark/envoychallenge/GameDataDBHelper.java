package stevens.mark.envoychallenge;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * Created by mark.stevens on 8/13/14.
 */
public class GameDataDBHelper extends SQLiteOpenHelper {
    // If you change the database schema, you must increment the database version.
    public static final int DATABASE_VERSION = 1;
    public static final String DATABASE_NAME = "datastore.db";

    private static final String TEXT_TYPE = " TEXT";
    private static final String INT_TYPE = " INTEGER";
    private static final String COMMA_SEP = ",";
    private static final String SQL_CREATE_ENTRIES =
            "CREATE TABLE " + GameEntry.TABLE_NAME + " (" +
                    GameEntry.COLUMN._id + " INTEGER PRIMARY KEY," +
                    GameEntry.COLUMN.game + TEXT_TYPE + COMMA_SEP +
                    GameEntry.COLUMN.console + TEXT_TYPE + COMMA_SEP +
                    GameEntry.COLUMN.image_url + TEXT_TYPE + COMMA_SEP +
                    GameEntry.COLUMN.finished + INT_TYPE + COMMA_SEP +
                    " )";



    private static final String SQL_DELETE_ENTRIES =
            "DROP TABLE IF EXISTS " + GameEntry.TABLE_NAME;

    public GameDataDBHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(SQL_CREATE_ENTRIES);
    }
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // This database is only a cache for online data, so its upgrade policy is
        // to simply to discard the data and start over
        db.execSQL(SQL_DELETE_ENTRIES);
        onCreate(db);
    }

}