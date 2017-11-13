package Database;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * Created by Erik.Rans on 29/06/2017.
 */

public class WanderLustDbHelper extends SQLiteOpenHelper {

    public static final int DATABASE_VERSION = 1;
    public static final String DATABASE_NAME = "WanderLust.db";

    public WanderLustDbHelper(Context context){
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(WanderLustDb.SQL_CREATE_ENCOUNTERTABLE);
        db.execSQL(WanderLustDb.SQL_CREATE_ENCOUNTERPICTURETABLE);
        db.execSQL(WanderLustDb.SQL_CREATELANGUAGETABLE);
        db.execSQL(WanderLustDb.SQL_CREATE_TEXTRESOURCELANG);
        db.execSQL(WanderLustDb.SQL_CREATE_SYNCTABLEVERSIONTABLE);
        db.execSQL(WanderLustDb.SQL_INITIALIZE_SYNCTABLEVERSIONTABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }
}
