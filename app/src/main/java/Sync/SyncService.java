package Sync;

import android.app.IntentService;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.util.Log;
import org.json.JSONObject;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import Database.WanderLustDb;
import Database.WanderLustDbHelper;
import Entity.Encounter;

import static java.util.regex.Pattern.quote;


/**
 * An {@link IntentService} subclass for handling asynchronous task requests in
 * a service on a separate handler thread.
 * <p>
 * TODO: Customize class - update intent actions and extra parameters.
 */
public class SyncService extends IntentService {

    private static final String TAG = "SyncService";
    private WanderLustDbHelper dbHelper = null;
    private SQLiteDatabase db = null;

    private static final String Env = "T";

    private static final String baseApiUrl =
        (Env.equals("D"))? "http://192.168.0.235/Wanderlust.WebAPI/api":
        (Env.equals("T"))? "http://gowanderlust-testing.azurewebsites.net/Wanderlust.WebAPI/api":
        (Env.equals("P"))? "http://gowanderlust.azurewebsites.net/Wanderlust.WebAPI/api": "wrong env param";


    public SyncService() {super("SyncService");}

    @Override
    protected void onHandleIntent(Intent intent) {
        dbHelper = new WanderLustDbHelper(this);
        try {
            db = dbHelper.getWritableDatabase();
        }
        catch(Exception e){
            Log.d(TAG, "SyncService: could not open database" + e.getMessage());
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
            File file = new File(encounterPictureImagePath);
            byte[] fileData = new byte[(int) file.length()];
            DataInputStream dis = new DataInputStream(new FileInputStream(file));
            dis.readFully(fileData);
            dis.close();

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
