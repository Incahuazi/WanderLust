package be.ictera.wanderlust;

import android.content.Intent;
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

import Entity.Encounter;
import Entity.EncounterPicture;
import ShowEncounters.ScreenSlidePageFragment;

public class ShowEncounterScreenSlidePagerActivity extends FragmentActivity implements ScreenSlidePageFragment.OnFragmentInteractionListener {

    private Encounter[] encounters;
    /**
     * The number of pages (wizard steps) to show in this demo.
     */
    private static final int NUM_PAGES = 5;
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

        //add some encounter data
        encounters = new Encounter[5];
        encounters[0] = new Encounter();
        encounters[0].Name = "Djengis Khan";
        encounters[0].Message = "Rhaaaaa!!!";
        encounters[0].Location = "Mongolia";
        encounters[0].encounterPicture = new EncounterPicture[] {new EncounterPicture(), new EncounterPicture(), new EncounterPicture()};
        encounters[0].encounterPicture[0].imageFilePath = "Djengis image 1";
        encounters[0].encounterPicture[1].imageFilePath = "Djengis image 2";
        encounters[0].encounterPicture[2].imageFilePath = "Djengis image 3";

        encounters[1] = new Encounter();
        encounters[1].Name = "Grote smurf";
        encounters[1].Message = "smurf";
        encounters[1].Location = "Beestenbos";
        encounters[1].encounterPicture = new EncounterPicture[] {new EncounterPicture(), new EncounterPicture(), new EncounterPicture()};
        encounters[1].encounterPicture[0].imageFilePath = "Grote smurf image 1";
        encounters[1].encounterPicture[1].imageFilePath = "Grote smurf image 2";
        encounters[1].encounterPicture[2].imageFilePath = "Grote smurf image 3";

        encounters[2] = new Encounter();
        encounters[2].Name = "Yoko Tsuno";
        encounters[2].Message = "Konichiwa";
        encounters[2].Location = "Japan";
        encounters[2].encounterPicture = new EncounterPicture[] {new EncounterPicture(), new EncounterPicture(), new EncounterPicture()};
        encounters[2].encounterPicture[0].imageFilePath = "Yoko image 1";
        encounters[2].encounterPicture[1].imageFilePath = "Yoko image 2";
        encounters[2].encounterPicture[2].imageFilePath = "Yoko image 3";

        encounters[3] = new Encounter();
        encounters[3].Name = "PJ Harvey";
        encounters[3].Message = "big fish little fish";
        encounters[3].Location = "UK";
        encounters[3].encounterPicture = new EncounterPicture[] {new EncounterPicture(), new EncounterPicture(), new EncounterPicture()};
        encounters[3].encounterPicture[0].imageFilePath = "PJ image 1";
        encounters[3].encounterPicture[1].imageFilePath = "PJ image 2";
        encounters[3].encounterPicture[2].imageFilePath = "PJ image 3";

        encounters[4] = new Encounter();
        encounters[4].Name = "Trump";
        encounters[4].Message = "blablabla";
        encounters[4].Location = "USA";
        encounters[4].encounterPicture = new EncounterPicture[] {new EncounterPicture(), new EncounterPicture(), new EncounterPicture()};
        encounters[4].encounterPicture[0].imageFilePath = "Idiot image 1";
        encounters[4].encounterPicture[1].imageFilePath = "Idiot image 2";
        encounters[4].encounterPicture[2].imageFilePath = "Idiot image 3";


//        ((AppCompatActivity) getActivity()).getSupportActionBar().setSubtitle(R.id.toolbar);

        // Instantiate a ViewPager and a PagerAdapter.
        mPager = (ViewPager) findViewById(R.id.pager);
        mPagerAdapter = new ScreenSlidePagerAdapter(getSupportFragmentManager());
        mPager.setAdapter(mPagerAdapter);
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
            argsBundle.putString("dataName", encounters[position].Name);
            argsBundle.putString("dataMessage", encounters[position].Message);
            argsBundle.putString("dataLocation", encounters[position].Location);
            argsBundle.putString("Image1", encounters[position].encounterPicture[0].imageFilePath);
            argsBundle.putString("Image2", encounters[position].encounterPicture[1].imageFilePath);
            argsBundle.putString("Image3", encounters[position].encounterPicture[2].imageFilePath);

            screenSlidePageFragment.setArguments(argsBundle);
            return screenSlidePageFragment;
        }

        @Override
        public void startUpdate(ViewGroup container) {
            super.startUpdate(container);
            int currentItemTemp = mPager.getCurrentItem();
            if (currentItemTemp!=currentItem){
                currentItem = currentItemTemp;
                TextView textView = (TextView) findViewById(R.id.TextViewProfileName);
                textView.setText(encounters[currentItem].Name);
            }
        }

        @Override
        public int getCount() {
            return NUM_PAGES;
        }
    }
}