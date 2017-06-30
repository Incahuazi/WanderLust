package Database;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.provider.BaseColumns;

import Entity.EncounterPicture;

/**
 * Created by Erik.Rans on 29/06/2017.
 */

public final class WanderLustDb {

    private WanderLustDb(){}

    public static class EncounterTable implements BaseColumns{
        public static final String TABLE_NAME = "Encounter_OP";
        public static final String COLUMN_NAME_NAME = "Name";
        public static final String COLUMN_NAME_MESSAGE = "Message";
        public static final String COLUMN_NAME_LOCATION = "Location";
    }

    public static class EncounterPictureTable implements  BaseColumns{
        public static final String TABLE_NAME = "EncounterPicture_OP";
        public static final String COLUMN_NAME_IMAGEFILEPATH = "ImageFilePath";
        public static final String COLUMN_NAME_SYNCED = "Synced";
        public static final String COLUMN_NAME_ENCOUNTERID = "EncounterId";

    }

    public static final String SQL_CREATE_ENCOUNTERTABLE =
            "CREATE TABLE " + EncounterTable.TABLE_NAME + " (" +
                    EncounterTable._ID + " INTEGER PRIMARY KEY," +
                    EncounterTable.COLUMN_NAME_NAME + " TEXT," +
                    EncounterTable.COLUMN_NAME_LOCATION + " TEXT," +
                    EncounterTable.COLUMN_NAME_MESSAGE + " TEXT)"
            ;

    public static final String SQL_CREATE_ENCOUNTERPICTURETABLE =
            "CREATE TABLE " + EncounterPictureTable.TABLE_NAME + " (" +
                    EncounterPictureTable._ID + " INTEGER PRIMARY KEY," +
                    EncounterPictureTable.COLUMN_NAME_ENCOUNTERID + " INTEGER NOT NULL," +
                    EncounterPictureTable.COLUMN_NAME_IMAGEFILEPATH + " TEXT," +
                    EncounterPictureTable.COLUMN_NAME_SYNCED + " INTEGER)"
            ;
}


