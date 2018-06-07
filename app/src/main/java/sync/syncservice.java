package sync;

import android.app.IntentService;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.List;

import Database.WanderLustDb;
import Database.WanderLustDbHelper;
import Entity.Encounter;

import static android.database.sqlite.SQLiteDatabase.CONFLICT_REPLACE;


/**
 * An {@link IntentService} subclass for handling asynchronous task requests in
 * a service on a separate handler thread.
 * <p>
 * TODO: Customize class - update intent actions and extra parameters.
 */
public class syncservice extends IntentService {

    private static final String TAG = "syncservice";
    private WanderLustDbHelper dbHelper = null;
    private SQLiteDatabase db = null;

    private static final String Env = "T";

    private static final String baseApiUrl =
        (Env.equals("D"))? "http://192.168.0.235/Wanderlust.WebAPI/api":
        (Env.equals("T"))? "http://gowanderlust-testing.azurewebsites.net/Wanderlust.WebAPI/api":
        (Env.equals("P"))? "http://gowanderlust.azurewebsites.net/Wanderlust.WebAPI/api": "wrong env param";


    public syncservice() {super("syncservice");}

    @Override
    protected void onHandleIntent(Intent intent) {
        dbHelper = new WanderLustDbHelper(this);
        try {
            db = dbHelper.getWritableDatabase();
        }
        catch(Exception e){
            Log.d(TAG, "syncservice: could not open database" + e.getMessage());
        }
        try {
            Log.d(TAG, "onHandleIntent: starting service");
            if (intent != null) {

                while (SyncEncounters()==false){
                    if (IsNetworkConnected()==false){
                        break;
                    }
                }

                while (SyncEncounterPictures() ==false){
                    if (IsNetworkConnected()==false){
                        break;
                    }
                }

                SyncLanguageResource();
            }
            Log.d(TAG, "Service Stopping!");
        } catch (Exception e){
            e.printStackTrace();
        }
        finally {
            if (db!=null)db.close();
        }
    }


    private boolean IsNetworkConnected(){
        ConnectivityManager cm = (ConnectivityManager) this.getApplicationContext().getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        if (activeNetwork != null) {
            if (activeNetwork.getType() == ConnectivityManager.TYPE_WIFI || activeNetwork.getType() == ConnectivityManager.TYPE_MOBILE){
                Log.d(TAG, "IsNetworkConnected: network detected");
                return true;
            }
        }
        return false;
    }

    private boolean SyncEncounters(){
        boolean result = true;
        try {
            //first sync all encounters without pictures
            String allUnsyncedEncounters = "SELECT " +
                    "ET." + WanderLustDb.EncounterTable._ID + " AS ETID, " +
                    WanderLustDb.EncounterTable.COLUMN_NAME_NAME + ", " +
                    WanderLustDb.EncounterTable.COLUMN_NAME_MESSAGE + ", " +
                    WanderLustDb.EncounterTable.COLUMN_NAME_LOCATION_CITY + ", " +
                    WanderLustDb.EncounterTable.COLUMN_NAME_LOCATION_COUNTRY + ", " +
                    WanderLustDb.EncounterTable.COLUMN_NAME_INSERTEDTIMESTAMP + " AS ETInserted, " +
                    WanderLustDb.EncounterTable.COLUMN_NAME_LOCATION_LATLONG + ", " +
                    WanderLustDb.EncounterTable.COLUMN_NAME_EMAILADDRESS +
                    " FROM " + WanderLustDb.EncounterTable.TABLE_NAME + " ET" +
                    " WHERE NOT " + WanderLustDb.EncounterTable.COLUMN_NAME_SYNCED + " =1" +
                    " ORDER BY ETInserted ASC";

            Cursor cursor = db.rawQuery(allUnsyncedEncounters, null);

            while(cursor.moveToNext()) {
                String currentEncounterId = cursor.getString(cursor.getColumnIndex("ETID"));
                Encounter encounter = new Encounter();
                encounter.Name = cursor.getString(cursor.getColumnIndex(WanderLustDb.EncounterTable.COLUMN_NAME_NAME));
                encounter.Message = cursor.getString(cursor.getColumnIndex(WanderLustDb.EncounterTable.COLUMN_NAME_MESSAGE));
                encounter.LocationCity = cursor.getString(cursor.getColumnIndex(WanderLustDb.EncounterTable.COLUMN_NAME_LOCATION_CITY));
                encounter.LocationCountry = cursor.getString(cursor.getColumnIndex(WanderLustDb.EncounterTable.COLUMN_NAME_LOCATION_COUNTRY));
                encounter.LocationLatLong = cursor.getString(cursor.getColumnIndex(WanderLustDb.EncounterTable.COLUMN_NAME_LOCATION_LATLONG));
                encounter.EmailAddress = cursor.getString(cursor.getColumnIndex(WanderLustDb.EncounterTable.COLUMN_NAME_EMAILADDRESS));
                encounter.InsertedTimeStamp = cursor.getString(cursor.getColumnIndex("ETInserted"));

                if(uploadEncounter(currentEncounterId, encounter)==false) result=false;
            }
        }catch (Exception e){
            e.printStackTrace();
            result = false;
        }

        return result;
    }

    private void SyncLanguageResource() {

        HttpURLConnection urlConnection = null;
        try {
            //get the current SyncVersion
            String getLocalLanguageVersion = "SELECT " +
                    WanderLustDb.SyncTableVersion.COLUMN_NAME_TableName + "," +
                    WanderLustDb.SyncTableVersion.COLUMN_NAME_Version + " FROM " +
                    WanderLustDb.SyncTableVersion.TABLE_NAME + " WHERE " +
                    WanderLustDb.SyncTableVersion.COLUMN_NAME_TableName + " = '" + WanderLustDb.Language.TABLE_NAME + "'" +
                    " OR " + WanderLustDb.SyncTableVersion.COLUMN_NAME_TableName + " = '" + WanderLustDb.TextResourceLang.TABLE_NAME + "'";

            Cursor cursor = db.rawQuery(getLocalLanguageVersion, null);

            int localLanguageVersion = 0;
            int localTextResourceLangVersion = 0;
            while(cursor.moveToNext()) {
                if (cursor.getString(0).equals(WanderLustDb.Language.TABLE_NAME)){
                    localLanguageVersion = cursor.getInt(1);
                }
                if (cursor.getString(0).equals(WanderLustDb.TextResourceLang.TABLE_NAME)){
                    localTextResourceLangVersion = cursor.getInt(1);
                }
            }

            //Get the new language from web-api
            URL url;

            String queryParams = "LocalLanguageVersion=" + localLanguageVersion + "&LocalTextResourceLangVersion=" + localTextResourceLangVersion;

            url = new URL(baseApiUrl + "/Config/LanguageSync?" + queryParams);
            urlConnection = (HttpURLConnection) url.openConnection();
            urlConnection.setRequestMethod("GET");

            int status = urlConnection.getResponseCode();

            if (status == HttpURLConnection.HTTP_OK || status == HttpURLConnection.HTTP_NO_CONTENT){

                StringBuffer output = new StringBuffer("");
                InputStream in = urlConnection.getInputStream();
                BufferedReader buffer = new BufferedReader(new InputStreamReader(in));
                String resultString = "";
                while ((resultString = buffer.readLine()) != null)
                    output.append(resultString);

                HandleLanguageSyncResponse(output.toString());
            }
            else Log.d(TAG, "Error retrieving LanguageResource, HttpStatus = " + status);

        } catch (Exception e) {
            e.printStackTrace();
        }
        finally {
            if (urlConnection!=null) urlConnection.disconnect();
        }
    }

    private void HandleLanguageSyncResponse(String response){
        GsonBuilder builder = new GsonBuilder();
        Gson gson = builder.create();
        String foo = response;
        LanguageSyncDTO languageDTO = gson.fromJson(foo, LanguageSyncDTO.class);

        if (!languageDTO.Languages.isEmpty()){UpdateLanguages(languageDTO.Languages);}
        if (!languageDTO.TextResourceLangsDTO.isEmpty()){UpdateTextResourceLanguages(languageDTO.TextResourceLangsDTO);}

        UpdateLanguageSyncVersions(languageDTO.NewLanguageSyncVersion, languageDTO.NewTextResourceLangVersion);
    }

    private void UpdateLanguageSyncVersions(int newLanguageSyncVersion, int newTextResourceLangVersion) {

        if (newLanguageSyncVersion>0){
            String updateSql = "insert or replace into " +
                    WanderLustDb.SyncTableVersion.TABLE_NAME + "(" + WanderLustDb.SyncTableVersion.COLUMN_NAME_TableName + "," + WanderLustDb.SyncTableVersion.COLUMN_NAME_Version + ")" +
                    "values('" + WanderLustDb.Language.TABLE_NAME + "'," + newLanguageSyncVersion + ")";
            db.execSQL(updateSql);
        }

        if (newTextResourceLangVersion>0){
            String updateSql = "insert or replace into " +
                    WanderLustDb.SyncTableVersion.TABLE_NAME + "(" + WanderLustDb.SyncTableVersion.COLUMN_NAME_TableName + "," + WanderLustDb.SyncTableVersion.COLUMN_NAME_Version + ")" +
                    "values('" + WanderLustDb.TextResourceLang.TABLE_NAME + "'," + newTextResourceLangVersion + ")";
            db.execSQL(updateSql);
        }

        String foo = "SELECT " +
                WanderLustDb.SyncTableVersion.COLUMN_NAME_TableName + "," +
                WanderLustDb.SyncTableVersion.COLUMN_NAME_Version +
                " from " + WanderLustDb.SyncTableVersion.TABLE_NAME;

        Cursor cursor = db.rawQuery(foo, null);
        while (cursor.moveToNext()){
            String TN = cursor.getString(0);
            String TV = cursor.getString(1);
        }
    }

    private void UpdateTextResourceLanguages(List<TextResourceLangDTO> newTextResourceLanguages){
        String newValues = "";
        ContentValues cv = new ContentValues();

        for (int i = 0; i < newTextResourceLanguages.size();i++){

            cv.put(WanderLustDb.TextResourceLang.COLUMN_NAME_LanguageCode, newTextResourceLanguages.get(i).LanguageCode.trim());
            cv.put(WanderLustDb.TextResourceLang.COLUMN_NAME_TextResourceId, newTextResourceLanguages.get(i).TextResourceId);
            cv.put(WanderLustDb.TextResourceLang.COLUMN_NAME_ActivityName, newTextResourceLanguages.get(i).ActivityName);
            cv.put(WanderLustDb.TextResourceLang.COLUMN_NAME_Text, newTextResourceLanguages.get(i).Text);

            db.insertWithOnConflict(WanderLustDb.TextResourceLang.TABLE_NAME,null ,cv, CONFLICT_REPLACE);


//            newValues += "('" + newTextResourceLanguages.get(i).LanguageCode.trim() + "','" +
//                    newTextResourceLanguages.get(i).TextResourceId + "','" +
//                    newTextResourceLanguages.get(i).ActivityName + "','" +
//                    newTextResourceLanguages.get(i).Text.replaceAll("'","''") + "'),";
        }
//        newValues = newValues.substring(0,newValues.length()-1);

//        String updateSql = "insert or replace into " +
//                WanderLustDb.TextResourceLang.TABLE_NAME +
//                "(" + WanderLustDb.TextResourceLang.COLUMN_NAME_LanguageCode +"," +
//                WanderLustDb.TextResourceLang.COLUMN_NAME_TextResourceId +"," +
//                WanderLustDb.TextResourceLang.COLUMN_NAME_ActivityName +"," +
//                WanderLustDb.TextResourceLang.COLUMN_NAME_Text + ")" +
//                "values " + newValues;


//        db.execSQL(updateSql);

//        String foo = "SELECT " +
//                WanderLustDb.TextResourceLang.COLUMN_NAME_LanguageCode +"," +
//                WanderLustDb.TextResourceLang.COLUMN_NAME_TextResourceId +"," +
//                WanderLustDb.TextResourceLang.COLUMN_NAME_Text + " from " +
//                WanderLustDb.TextResourceLang.TABLE_NAME;
//        Cursor cursor = db.rawQuery(foo, null);
//        while (cursor.moveToNext()){
//            String LC = cursor.getString(0);
//            String TRID = cursor.getString(1);
//            String TXT = cursor.getString(2);
//        }
    }


    private void UpdateLanguages(List<LanguageDTO> NewLanguages){

        String newValues = "";
        for (int i = 0; i < NewLanguages.size();i++){

            newValues += "('" + NewLanguages.get(i).LanguageCode.trim() + "','" +
                    NewLanguages.get(i).LanguageName + "','" +
                    NewLanguages.get(i).ImageName + "'),";
        }
        newValues = newValues.substring(0,newValues.length()-1);

        String updateSql = "insert or replace into " +
                WanderLustDb.Language.TABLE_NAME +
                "(" + WanderLustDb.Language.COLUMN_NAME_LanguageCode +"," +
                WanderLustDb.Language.COLUMN_NAME_LanguageName +"," +
                WanderLustDb.Language.COLUMN_NAME_ImageName + ")" +
                "values " + newValues;
        db.execSQL(updateSql);

//        String foo = "SELECT " +
//                WanderLustDb.Language.COLUMN_NAME_LanguageCode +"," +
//                WanderLustDb.Language.COLUMN_NAME_LanguageName +"," +
//                WanderLustDb.Language.COLUMN_NAME_ImageName + " from " +
//                WanderLustDb.Language.TABLE_NAME;
//        Cursor cursor = db.rawQuery(foo, null);
//        while (cursor.moveToNext()){
//            String LC = cursor.getString(0);
//            String LN = cursor.getString(1);
//            String LI = cursor.getString(2);
//        }
    }

    private boolean SyncEncounterPictures(){
        boolean result = true;
        try {
            //next try to upload the pictures
            String allUnsyncedEncounterPictures = "SELECT " +
                    WanderLustDb.EncounterPictureTable._ID + " AS EPTID, " +
                    WanderLustDb.EncounterPictureTable.COLUMN_NAME_ENCOUNTERID + ", " +
                    WanderLustDb.EncounterPictureTable.COLUMN_NAME_IMAGEFILEPATH +
                    " FROM " + WanderLustDb.EncounterPictureTable.TABLE_NAME +
                    " WHERE NOT " + WanderLustDb.EncounterPictureTable.COLUMN_NAME_SYNCED + " =1" +
                    " ORDER BY EPTID ASC";
            Cursor cursor = db.rawQuery(allUnsyncedEncounterPictures, null);

            while(cursor.moveToNext()) {
                String currentEncounterPictureId = cursor.getString(cursor.getColumnIndex("EPTID"));
                String encounterId = cursor.getString(cursor.getColumnIndex(WanderLustDb.EncounterPictureTable.COLUMN_NAME_ENCOUNTERID));
                String encounterPictureImagePath = cursor.getString(cursor.getColumnIndex(WanderLustDb.EncounterPictureTable.COLUMN_NAME_IMAGEFILEPATH));
                if(uploadEncounterPicture(currentEncounterPictureId, encounterId, encounterPictureImagePath)==false) result=false;
            }
            return true;
        }catch (Exception e){
            e.printStackTrace();
            result = false;
        }

        return result;
    }

    private boolean uploadEncounterPicture(String encounterPictureId, String encounterId, String encounterPictureImagePath) {
        URL url;
        HttpURLConnection urlConnection = null;
        DataOutputStream printout;
        boolean result = false;
        try {
            String queryParams = "EncounterId=" + encounterId +
                    "&DeviceId=" + 1 +
                    "&ImageFilePath=" + URLEncoder.encode(encounterPictureImagePath, "UTF-8") +
                    "&EncounterPictureId=" + encounterPictureId;

            url = new URL(baseApiUrl + "/EncounterPicture?" + queryParams);
            urlConnection = (HttpURLConnection) url.openConnection();
            urlConnection.setRequestMethod("POST");
            urlConnection.setDoOutput(true);
            urlConnection.setRequestProperty("Content-Type","image/jpeg");

            //attach the file
            byte[] fileData = new byte[0];
            boolean fileRead = false;
            //try to read the file a few times, if it still fails mark as synced
            for (int i=0; i<5; i++) {
                try {
                    File file = new File(encounterPictureImagePath);
                    fileData = new byte[(int) file.length()];
                    DataInputStream dis = new DataInputStream(new FileInputStream(file));
                    dis.readFully(fileData);
                    dis.close();
                    fileRead = true;
                    break;
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            if (!fileRead){
                //mark as synced anyway, don't keep retrying
                setEncounterPictureSynced(encounterPictureId);
                return true;
            }

            printout = new DataOutputStream(urlConnection.getOutputStream ());
            printout.write(fileData);
            printout.flush ();
            printout.close ();

            int status = urlConnection.getResponseCode();

            if (status == HttpURLConnection.HTTP_OK || status == HttpURLConnection.HTTP_NO_CONTENT){
                setEncounterPictureSynced(encounterPictureId);
                result = true;
            }
            else Log.d(TAG, "uploadEncounterPicture: Error sending encounterPicture, HttpStatus = " + status);

        }catch (Exception e){
            e.printStackTrace();
            result = false;
        }
        finally {
            if(urlConnection != null) urlConnection.disconnect();
        }
        return result;
    }

    private boolean uploadEncounter(String encounterId, Encounter encounter){
        URL url;
        HttpURLConnection urlConnection = null;
        DataOutputStream printout;
        boolean result = false;
        try {
            url = new URL(baseApiUrl + "/Encounter");
            urlConnection = (HttpURLConnection) url.openConnection();
            urlConnection.setRequestMethod("POST");
            urlConnection.setDoOutput(true);
            urlConnection.setRequestProperty("Content-Type","application/json");

            JSONObject jsonParam = new JSONObject();
            jsonParam.put("Id", encounterId);
            jsonParam.put("DeviceId", 1);
            jsonParam.put("Name", encounter.Name);
            jsonParam.put("Message", encounter.Message);
            jsonParam.put("LocationCity", encounter.LocationCity);
            jsonParam.put("LocationCountry", encounter.LocationCountry);
            jsonParam.put("LocationLatLong", encounter.LocationLatLong);
            jsonParam.put("EmailAddress", encounter.EmailAddress);
            jsonParam.put("InsertedTimeStamp", encounter.InsertedTimeStamp);

            printout = new DataOutputStream(urlConnection.getOutputStream ());
            printout.writeBytes(jsonParam.toString());
            printout.flush ();
            printout.close ();

            int status = urlConnection.getResponseCode();

            if (status == HttpURLConnection.HTTP_OK || status == HttpURLConnection.HTTP_NO_CONTENT){
                setEncounterSynced(encounterId);
                result = true;
            }

            else Log.d(TAG, "uploadEncounter: Error sending encounter, HttpStatus = " + status);

        }catch (Exception e){
            e.printStackTrace();
            result = false;
        }
        finally {
            if(urlConnection != null) urlConnection.disconnect();
        }
        return result;
    }

    private void setEncounterSynced(String encounterId){
        ContentValues cv = new ContentValues();
        cv.put(WanderLustDb.EncounterTable.COLUMN_NAME_SYNCED, 1);
        db.update(WanderLustDb.EncounterTable.TABLE_NAME, cv, WanderLustDb.EncounterTable._ID + " = \"" + encounterId + "\"",null);
    }

    private void setEncounterPictureSynced(String encounterPictureId){
        ContentValues cv = new ContentValues();
        cv.put(WanderLustDb.EncounterPictureTable.COLUMN_NAME_SYNCED, 1);
        db.update(WanderLustDb.EncounterPictureTable.TABLE_NAME, cv, WanderLustDb.EncounterPictureTable._ID + " = \"" + encounterPictureId + "\"" ,null);
    }
}
