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
        public static final String COLUMN_NAME_LOCATION_CITY = "LocationCity";
        public static final String COLUMN_NAME_LOCATION_COUNTRY = "LocationCountry";
        public static final String COLUMN_NAME_LOCATION_LATLONG = "LocationLatLong";
        public static final String COLUMN_NAME_SYNCED = "Synced";
        public static final String COLUMN_NAME_INSERTEDTIMESTAMP = "InsertedTimestamp";
        public static final String COLUMN_NAME_EMAILADDRESS = "EmailAddress";
    }

    public static class EncounterPictureTable implements  BaseColumns{
        public static final String TABLE_NAME = "EncounterPicture_OP";
        public static final String COLUMN_NAME_IMAGEFILEPATH = "ImageFilePath";
        public static final String COLUMN_NAME_SYNCED = "Synced";
        public static final String COLUMN_NAME_ENCOUNTERID = "EncounterId";

    }

    public static class TextResourceLang {
        public static final String TABLE_NAME = "TextResourceLang";
        public static final String COLUMN_NAME_TextResourceId = "TextResourceId";
        public static final String COLUMN_NAME_LanguageCode = "LanguageCode";
        public static final String COLUMN_NAME_Text = "Text";
    }

    public static class Language {
        public static final String TABLE_NAME = "Language";
        public static final String COLUMN_NAME_LanguageCode = "LanguageCode";
        public static final String COLUMN_NAME_LanguageName = "LanguageName";
        public static final String COLUMN_NAME_ImageName = "ImageName";
    }

    public static class SyncTableVersion{
        public static final String TABLE_NAME = "SyncTableVersion";
        public static final String COLUMN_NAME_TableName = "TableName";
        public static final String COLUMN_NAME_Version = "Version";
    }

    public static final String SQL_CREATE_ENCOUNTERTABLE =
            "CREATE TABLE " + EncounterTable.TABLE_NAME + " (" +
                    EncounterTable._ID + " TEXT PRIMARY KEY," +
                    EncounterTable.COLUMN_NAME_NAME + " TEXT," +
                    EncounterTable.COLUMN_NAME_LOCATION_CITY + " TEXT," +
                    EncounterTable.COLUMN_NAME_LOCATION_COUNTRY + " TEXT," +
                    EncounterTable.COLUMN_NAME_LOCATION_LATLONG + " TEXT," +
                    EncounterTable.COLUMN_NAME_SYNCED + " INTEGER," +
                    EncounterTable.COLUMN_NAME_INSERTEDTIMESTAMP + " TEXT, " +
                    EncounterTable.COLUMN_NAME_EMAILADDRESS + " TEXT, " +
                    EncounterTable.COLUMN_NAME_MESSAGE + " TEXT)"
            ;

    public static final String SQL_CREATE_ENCOUNTERPICTURETABLE =
            "CREATE TABLE " + EncounterPictureTable.TABLE_NAME + " (" +
                    EncounterPictureTable._ID + " TEXT PRIMARY KEY," +
                    EncounterPictureTable.COLUMN_NAME_ENCOUNTERID + " TEXT NOT NULL," +
                    EncounterPictureTable.COLUMN_NAME_IMAGEFILEPATH + " TEXT," +
                    EncounterPictureTable.COLUMN_NAME_SYNCED + " INTEGER)"
            ;

    public static final String SQL_CREATE_TEXTRESOURCELANG =
            "CREATE TABLE " + TextResourceLang.TABLE_NAME + " (" +
                    TextResourceLang.COLUMN_NAME_TextResourceId + " TEXT NOT NULL," +
                    TextResourceLang.COLUMN_NAME_LanguageCode + " TEXT NOT NULL," +
                    TextResourceLang.COLUMN_NAME_Text + " TEXT," +
                    "PRIMARY KEY (" + TextResourceLang.COLUMN_NAME_TextResourceId + "," + TextResourceLang.COLUMN_NAME_LanguageCode + "))";

    public static final String SQL_CREATELANGUAGETABLE =
            "CREATE TABLE " + Language.TABLE_NAME + " (" +
                    Language.COLUMN_NAME_LanguageCode + " TEXT PRIMARY KEY," +
                    Language.COLUMN_NAME_ImageName + " TEXT," +
                    Language.COLUMN_NAME_LanguageName + " TEXT)";

    public static final String SQL_CREATE_SYNCTABLEVERSIONTABLE =
            "CREATE TABLE " + SyncTableVersion.TABLE_NAME + " (" +
                    SyncTableVersion.COLUMN_NAME_TableName + " TEXT PRIMARY KEY," +
                    SyncTableVersion.COLUMN_NAME_Version + " INTEGER)";

    public static final String SQL_INITIALIZE_SYNCTABLEVERSIONTABLE =
            "INSERT INTO " + SyncTableVersion.TABLE_NAME + "(" +
                SyncTableVersion.COLUMN_NAME_TableName + ", " +
                SyncTableVersion.COLUMN_NAME_Version + ")" +
                "VALUES ('" + Language.TABLE_NAME + "', -1), " +
                "('" + TextResourceLang.TABLE_NAME + "', -1)";
}


