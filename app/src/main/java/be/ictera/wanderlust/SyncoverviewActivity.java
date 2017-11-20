package be.ictera.wanderlust;

import android.animation.ObjectAnimator;
import android.database.DatabaseUtils;
import android.database.sqlite.SQLiteDatabase;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.animation.DecelerateInterpolator;
import android.widget.TextView;

import com.github.lzyzsd.circleprogress.DonutProgress;

import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

import Database.WanderLustDb;
import Database.WanderLustDbHelper;
import Helper.MyDonutProgress;

public class SyncoverviewActivity extends AppCompatActivity {

    private static final String TAG = "SyncoverviewActivity";
    private DonutProgress donutProgress;
    private MyDonutProgress donutEncounterProgress;
    private MyDonutProgress donutPictureProgress;
    private TextView textViewheader;
    private Timer Encountertimer;
    private Timer Picturetimer;
    private static WanderLustDbHelper dbHelper = null;

    private String LanguageCode = "ENG";
    private String strNoSyncNeeded = "No synchronization needed";
    private String strSynced = "Synced";
    private String strProgress = "progress";

    private static int countertester = 20;

    private enum CounterType{
        Encounter,
        Pictures
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_syncoverview);

        dbHelper =  new WanderLustDbHelper(this);
        LanguageCode= getIntent().getStringExtra("LanguageCode");


        ResolveLanguageResource(LanguageCode);

        donutEncounterProgress = (MyDonutProgress) findViewById(R.id.donut_progress_encounters);
        donutPictureProgress = (MyDonutProgress) findViewById(R.id.donut_progress_pictures);
        textViewheader = (TextView) findViewById(R.id.textViewHeader);

        HandleEncounterProgress();
        HandlePicturesProgress();
    }

    private void HandleHeaderTextOnCounterZero(CounterType counterHavingZero)
    {
        switch (counterHavingZero){
            case Encounter:
                //check if other sync is also 0
                if(returnNrOfUnsyncedPictures()==0){
                    textViewheader.setText(strNoSyncNeeded);
                }
                break;
            case Pictures:
                //check if other sync is also 0
                if(returnNrOfUnsyncedEncounters()==0){
                    textViewheader.setText(strNoSyncNeeded);
                }
                break;
        }
    }

    private void HandleEncounterProgress() {
        int nrOfUnsyncedEncounters = this.returnNrOfUnsyncedEncounters();

        if (nrOfUnsyncedEncounters==0){
            donutEncounterProgress.setSuffixText("");
            donutEncounterProgress.setProgress(100);
            donutEncounterProgress.setText(strSynced);
            HandleHeaderTextOnCounterZero(CounterType.Encounter);
        }
        else{
            donutEncounterProgress.setMax(nrOfUnsyncedEncounters);
            donutEncounterProgress.CountDownIsoUp(true);
            donutEncounterProgress.setSuffixText("");

            Encountertimer = new Timer();
            Encountertimer.schedule(new TimerTask() {
                @Override
                public void run() {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {

                            //Check how many encounters need to be synced
                            int currentNrOfUnsyncedEncounters = returnNrOfUnsyncedEncounters();
                            if (donutEncounterProgress.getProgress()!=currentNrOfUnsyncedEncounters){
                                //update the progress bar
                                ObjectAnimator anim = ObjectAnimator.ofInt(donutEncounterProgress, strProgress, donutEncounterProgress.getProgress(), donutEncounterProgress.getMax() - currentNrOfUnsyncedEncounters);
                                anim.setInterpolator(new DecelerateInterpolator());
                                anim.setDuration(500);
                                anim.start();
                            }
                            if (currentNrOfUnsyncedEncounters==0) {
                                Encountertimer.cancel();
                                donutEncounterProgress.setProgress(donutEncounterProgress.getMax());
                                donutEncounterProgress.setText(strSynced);
                                HandleHeaderTextOnCounterZero(CounterType.Encounter);
                            }
                        }
                    });
                }
            }, 0, 5000);
        }
    }

    private void HandlePicturesProgress() {
        int nrOfUnsyncedPictures = this.returnNrOfUnsyncedPictures();

        if (nrOfUnsyncedPictures==0){
            donutPictureProgress.setSuffixText("");
            donutPictureProgress.setProgress(100);
            donutPictureProgress.setText(strSynced);
            HandleHeaderTextOnCounterZero(CounterType.Pictures);
        }
        else{
            donutPictureProgress.setMax(nrOfUnsyncedPictures);
            donutPictureProgress.CountDownIsoUp(true);
            donutPictureProgress.setSuffixText("");

            Picturetimer = new Timer();
            Picturetimer.schedule(new TimerTask() {
                @Override
                public void run() {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {

                            //Check how many encounters need to be synced
                            int currentNrOfUnsyncedPictures = returnNrOfUnsyncedPictures();
                            if (donutPictureProgress.getProgress()!=currentNrOfUnsyncedPictures){
                                //update the progress bar
                                ObjectAnimator anim = ObjectAnimator.ofInt(donutPictureProgress, strProgress, donutPictureProgress.getProgress(), donutPictureProgress.getMax() - currentNrOfUnsyncedPictures);
                                anim.setInterpolator(new DecelerateInterpolator());
                                anim.setDuration(500);
                                anim.start();
                            }
                            if (currentNrOfUnsyncedPictures==0) {
                                Picturetimer.cancel();
                                donutPictureProgress.setProgress(donutPictureProgress.getMax());
                                donutPictureProgress.setText(strSynced);
                                HandleHeaderTextOnCounterZero(CounterType.Pictures);
                            }
                        }
                    });
                }
            }, 0, 1000);
        }
    }

    private int returnNrOfUnsyncedPictures() {
//            this.countertester--;
//            if (this.countertester<0) this.countertester = 0;
//            return this.countertester;
        SQLiteDatabase db = null;
        try{
            db = dbHelper.getReadableDatabase();
            String unSyncedPicturesQuery =
                    "SELECT COUNT(*) FROM " + WanderLustDb.EncounterPictureTable.TABLE_NAME +
                            " WHERE NOT " + WanderLustDb.EncounterPictureTable.COLUMN_NAME_SYNCED + " =1";

            return (int)DatabaseUtils.longForQuery(db, unSyncedPicturesQuery, null);
        }
        catch(Exception e){
            Log.d(TAG, "SyncService: could not get unsyced count from database" + e.getMessage());
        }
        finally {
            if (db!=null)db.close();
        }
        return -1;
    }

    private int returnNrOfUnsyncedEncounters() {
//        this.countertester--;
//        if (this.countertester<0) this.countertester = 0;
//        return this.countertester;
        SQLiteDatabase db = null;
        try{
            db = dbHelper.getReadableDatabase();
            String unSyncedEncountersQuery =
                    "SELECT COUNT(*) FROM " + WanderLustDb.EncounterTable.TABLE_NAME +
                    " WHERE NOT " + WanderLustDb.EncounterTable.COLUMN_NAME_SYNCED + " =1";

            return (int)DatabaseUtils.longForQuery(db, unSyncedEncountersQuery, null);
        }
        catch(Exception e){
            Log.d(TAG, "SyncService: could not get unsyced count from database" + e.getMessage());
        }
        finally {
            if (db!=null)db.close();
        }
        return -1;
    }

    private void ResolveLanguageResource(String languageCode) {
        SQLiteDatabase db = null;
        try {
            db = dbHelper.getReadableDatabase();
            Map<String, String> resLang = dbHelper.GetAllTextResouceForActivityAndLanguage("SyncoverviewActivity", languageCode, db);

            if (resLang.containsKey("SONoSyncNeeded")) {
                strNoSyncNeeded = resLang.get("SONoSyncNeeded");
            }

            if (resLang.containsKey("SOSynced")) {
                strSynced = resLang.get("SOSynced");
            }

            if (resLang.containsKey("SOProgress")) {
                strProgress = resLang.get("SOProgress");
            }

        } catch (Exception e) {
        Log.d(TAG, "could not get language resources from db" + e.getMessage());

        } finally{
            if (db!=null)db.close();
        }

    }

}
