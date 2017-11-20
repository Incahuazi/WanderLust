package Database;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

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

    public Map<String,String> GetAllTextResouceForActivityAndLanguage (String ActivityName, String LanguageCode, SQLiteDatabase db){

        String sqlQuery = "SELECT " + WanderLustDb.TextResourceLang.COLUMN_NAME_TextResourceId + "," +
                WanderLustDb.TextResourceLang.COLUMN_NAME_Text + " FROM " +
                WanderLustDb.TextResourceLang.TABLE_NAME + " WHERE " +
                WanderLustDb.TextResourceLang.COLUMN_NAME_LanguageCode + "='" + LanguageCode + "' AND " +
                WanderLustDb.TextResourceLang.COLUMN_NAME_ActivityName + "='" + ActivityName + "'";

        Map result = new HashMap<String,String>();

        Cursor cursor = db.rawQuery(sqlQuery, null);
        while(cursor.moveToNext()) {
            result.put(cursor.getString(0), cursor.getString(1));
        }
        return result;
    }

    //returns an arraylist with array of strings
    //0 = LanguageCode
    //1 = LanguageName
    //2 = ImageName
    public ArrayList<String[]> GetAllLanguages (SQLiteDatabase db){

        String sqlQuery = "SELECT " + WanderLustDb.Language.COLUMN_NAME_LanguageCode + "," +
                WanderLustDb.Language.COLUMN_NAME_ImageName + "," +
                WanderLustDb.Language.COLUMN_NAME_LanguageName + " FROM " +
                WanderLustDb.Language.TABLE_NAME;

        ArrayList<String[]> result = new ArrayList<String[]>();

        Cursor cursor = db.rawQuery(sqlQuery, null);

        while(cursor.moveToNext()) {
            String[] strArray =  new String[3];
            strArray[0] = cursor.getString(0); //0 = LanguageCode
            strArray[1] = cursor.getString(2); //1 = LanguageName
            strArray[2] = cursor.getString(1); //2 = ImageName

            result.add(strArray);
        }
        return result;
    }
}
