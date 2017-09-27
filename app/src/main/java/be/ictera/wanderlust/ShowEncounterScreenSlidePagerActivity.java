package be.ictera.wanderlust;

import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import java.util.HashMap;
import java.util.Map;

import Database.WanderLustDb;
import Database.WanderLustDbHelper;
import Entity.Encounter;
import Entity.EncounterPicture;
import ShowEncounters.ScreenSlidePageFragment;

public class ShowEncounterScreenSlidePagerActivity extends FragmentActivity implements ScreenSlidePageFragment.OnFragmentInteractionListener {

    private Map EncountersMap = new HashMap<Integer,Encounter>();

    private int mMaxScrollSize;

    /**
     * The pager widget, which handles animation and allows swiping horizontally to access previous
     * and next wizard steps.
     */
    private ViewPager mPager;

    /**
     * The pager adapter, which provides the pages to the view pager widget.
     */
    private PagerAdapter mPagerAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_screen_slide);

        readEncounters(); //Reads encounters from db into EncountersMap HashMap

//        Encounter encounter;
//
//        encounter = new Encounter();
//        encounter.Name = "Djengis Khan";
//        encounter.Message = "Rhaaaaa!!!";
//        encounter.Location = "Mongolia";
//        encounter.encounterPicture = new EncounterPicture[] {new EncounterPicture(), new EncounterPicture(), new EncounterPicture()};
//        encounter.encounterPicture[0].imageFilePath = "";
//        encounter.encounterPicture[1].imageFilePath = "";
//        encounter.encounterPicture[2].imageFilePath = "";
//        EncountersMap.put(EncountersMap.size(), encounter);
//
//        encounter = new Encounter();
//        encounter.Name = "Grote smurf";
//        encounter.Message = "smurf";
//        encounter.Location = "Beestenbos";
//        encounter.encounterPicture = new EncounterPicture[] {new EncounterPicture(), new EncounterPicture(), new EncounterPicture()};
//        encounter.encounterPicture[0].imageFilePath = "";
//        encounter.encounterPicture[1].imageFilePath = "";
//        encounter.encounterPicture[2].imageFilePath = "";
//        EncountersMap.put(EncountersMap.size(), encounter);
//
//        encounter = new Encounter();
//        encounter.Name = "Yoko Tsuno";
//        encounter.Message = "Konichiwa";
//        encounter.Location = "Japan";
//        encounter.encounterPicture = new EncounterPicture[] {new EncounterPicture(), new EncounterPicture(), new EncounterPicture()};
//        encounter.encounterPicture[0].imageFilePath = "";
//        encounter.encounterPicture[1].imageFilePath = "";
//        encounter.encounterPicture[2].imageFilePath = "";
//        EncountersMap.put(EncountersMap.size(), encounter);
//
//        encounter = new Encounter();
//        encounter.Name = "PJ Harvey";
//        encounter.Message = "big fish little fish";
//        encounter.Location = "UK";
//        encounter.encounterPicture = new EncounterPicture[] {new EncounterPicture(), new EncounterPicture(), new EncounterPicture()};
//        encounter.encounterPicture[0].imageFilePath = "";
//        encounter.encounterPicture[1].imageFilePath = "";
//        encounter.encounterPicture[2].imageFilePath = "";
//        EncountersMap.put(EncountersMap.size(), encounter);
//
//        encounter = new Encounter();
//        encounter.Name = "Kabouter Wesley";
//        encounter.Message = "Godmiljaar!";
//        encounter.Location = "Beestenbos";
//        encounter.encounterPicture = new EncounterPicture[] {new EncounterPicture(), new EncounterPicture(), new EncounterPicture()};
//        encounter.encounterPicture[0].imageFilePath = "";
//        encounter.encounterPicture[1].imageFilePath = "";
//        encounter.encounterPicture[2].imageFilePath = "";
//        EncountersMap.put(EncountersMap.size(), encounter);

//        ((AppCompatActivity) getActivity()).getSupportActionBar().setSubtitle(R.id.toolbar);

        // Instantiate a ViewPager and a PagerAdapter.
        mPager = (ViewPager) findViewById(R.id.pager);
        mPagerAdapter = new ScreenSlidePagerAdapter(getSupportFragmentManager());
        mPager.setAdapter(mPagerAdapter);
    }

    private void readEncounters(){
        //Map result = new HashMap<Integer, Encounter>();
        WanderLustDbHelper dbHelper = new WanderLustDbHelper(this);

        try {
            SQLiteDatabase db = dbHelper.getReadableDatabase();

            String allEncountersAndPicsQuery = "SELECT " +
                    "ET." + WanderLustDb.EncounterTable._ID + " AS ETID, " +
                    WanderLustDb.EncounterTable.COLUMN_NAME_NAME + ", " +
                    WanderLustDb.EncounterTable.COLUMN_NAME_MESSAGE + ", " +
                    WanderLustDb.EncounterTable.COLUMN_NAME_LOCATION + ", " +
                    "EPT." +WanderLustDb.EncounterPictureTable._ID + " AS EPID, " +
                    WanderLustDb.EncounterPictureTable.COLUMN_NAME_IMAGEFILEPATH + ", " +
                    WanderLustDb.EncounterPictureTable.COLUMN_NAME_SYNCED +
                    " FROM " + WanderLustDb.EncounterTable.TABLE_NAME + " ET" +
                    " LEFT JOIN " + WanderLustDb.EncounterPictureTable.TABLE_NAME + " EPT" +
                    " ON ET." + WanderLustDb.EncounterTable._ID + " = EPT." + WanderLustDb.EncounterPictureTable.COLUMN_NAME_ENCOUNTERID +
                    " ORDER BY ETID ASC, EPID ASC";
            Cursor cursor = db.rawQuery(allEncountersAndPicsQuery, null);

            int previousEncounterId = -1;
            int encounterCounter = 0;
            int encounterPictureCounter = 0;
            while(cursor.moveToNext()) {
                int currentEncounterId = cursor.getInt(cursor.getColumnIndex("ETID"));
                if (currentEncounterId==previousEncounterId){
                    //Same encounter, add picture
                    encounterPictureCounter++;
                    Encounter encounter = (Encounter)EncountersMap.get(encounterCounter-1);
                    EncounterPicture encounterPicture = new EncounterPicture();
                    encounterPicture.imageFilePath = cursor.getString(cursor.getColumnIndex(WanderLustDb.EncounterPictureTable.COLUMN_NAME_IMAGEFILEPATH));
                    encounterPicture.synced = (cursor.getInt(cursor.getColumnIndex(WanderLustDb.EncounterPictureTable.COLUMN_NAME_SYNCED))==1);

                    encounter.encounterPicture[encounterPictureCounter] = encounterPicture;
                }
                else{
                    //next encounter, add new encounter
                    encounterPictureCounter = 0;
                    Encounter encounter = new Encounter();
                    encounter.Name = cursor.getString(cursor.getColumnIndex(WanderLustDb.EncounterTable.COLUMN_NAME_NAME));
                    encounter.Message = cursor.getString(cursor.getColumnIndex(WanderLustDb.EncounterTable.COLUMN_NAME_MESSAGE));
                    encounter.Location = cursor.getString(cursor.getColumnIndex(WanderLustDb.EncounterTable.COLUMN_NAME_LOCATION));

                    EncounterPicture encounterPicture = new EncounterPicture();
                    encounterPicture.imageFilePath =  cursor.getString(cursor.getColumnIndex(WanderLustDb.EncounterPictureTable.COLUMN_NAME_IMAGEFILEPATH));
                    encounterPicture.synced = (cursor.getInt(cursor.getColumnIndex(WanderLustDb.EncounterPictureTable.COLUMN_NAME_SYNCED))==1);

                    encounter.encounterPicture[encounterPictureCounter] = encounterPicture;

                    EncountersMap.put(encounterCounter, encounter);
                    encounterCounter++;
                }
                previousEncounterId = currentEncounterId;
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onImageClicked(String imagePath) {
        Intent intent = new Intent(this, DisplayPictureActivity.class);
        intent.putExtra("ImagePath", imagePath);

        startActivity(intent);
    }
//    @Override
//    public void onBackPressed() {
//        if (mPager.getCurrentItem() == 0) {
//            // If the user is currently looking at the first step, allow the system to handle the
//            // Back button. This calls finish() on this activity and pops the back stack.
//            super.onBackPressed();
//        } else {
//            // Otherwise, select the previous step.
//            mPager.setCurrentItem(mPager.getCurrentItem() - 1);
//        }
//    }

    /**
     * A simple pager adapter that represents 5 ScreenSlidePageFragment objects, in
     * sequence.
     */
    private class ScreenSlidePagerAdapter extends FragmentStatePagerAdapter {

        private int currentItem = -1;

        public ScreenSlidePagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {

            ScreenSlidePageFragment screenSlidePageFragment = new ScreenSlidePageFragment();
            Bundle argsBundle = new Bundle();

            argsBundle.putString("dataName", ((Encounter)EncountersMap.get(position)).Name);
            argsBundle.putString("dataMessage", ((Encounter)EncountersMap.get(position)).Message);
            argsBundle.putString("dataLocation", ((Encounter)EncountersMap.get(position)).Location);
            argsBundle.putString("Image1", ((Encounter)EncountersMap.get(position)).encounterPicture[0]==null?"":((Encounter)EncountersMap.get(position)).encounterPicture[0].imageFilePath);
            argsBundle.putString("Image2", ((Encounter)EncountersMap.get(position)).encounterPicture[1]==null?"":((Encounter)EncountersMap.get(position)).encounterPicture[1].imageFilePath);;
            argsBundle.putString("Image3", ((Encounter)EncountersMap.get(position)).encounterPicture[2]==null?"":((Encounter)EncountersMap.get(position)).encounterPicture[2].imageFilePath);;

            screenSlidePageFragment.setArguments(argsBundle);
            return screenSlidePageFragment;
        }

        @Override
        public void startUpdate(ViewGroup container) {
            super.startUpdate(container);
            if (EncountersMap.size()!=0) {
                int currentItemTemp = mPager.getCurrentItem();
                if (currentItemTemp!=currentItem){
                    currentItem = currentItemTemp;
                    TextView textView = (TextView) findViewById(R.id.TextViewProfileName);
                    textView.setText(((Encounter)EncountersMap.get(currentItem)).Name);
                }
            }
        }

        @Override
        public int getCount() {
            return EncountersMap.size();
        }
    }
}