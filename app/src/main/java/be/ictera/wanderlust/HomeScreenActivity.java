package be.ictera.wanderlust;

import android.content.Intent;
import android.content.res.ColorStateList;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.graphics.Typeface;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

import java.util.Map;

import Database.WanderLustDbHelper;


public class HomeScreenActivity extends AppCompatActivity {

    private WanderLustDbHelper dbHelper = null;
    private SQLiteDatabase db = null;
    private String LanguageCode = "ENG";

    private String HSSyncHeaderAskSync = "";
    private String HSSyncTextAskSync = "";
    private String HSSyncHeader = "";
    private String HSSyncText = "";

    private ColorStateList oldSyncHeaderColor;
    private ColorStateList oldSyncTextColor;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home_screen);

        dbHelper = new WanderLustDbHelper(this);
        db = dbHelper.getReadableDatabase();


        //Save old sync text color
        TextView HHSyncHeaderTextView = (TextView)findViewById(R.id.HSSyncHeader);
        TextView HSSyncTextTextView = (TextView)findViewById(R.id.HSSyncText);
        oldSyncHeaderColor =  HHSyncHeaderTextView.getTextColors(); //save original colors
        oldSyncTextColor =  HSSyncTextTextView.getTextColors(); //save original colors

        LanguageCode= getIntent().getStringExtra("LanguageCode");
        ResolveLanguageResource(LanguageCode);

        TableRow addEncounterTableRow = (TableRow)findViewById(R.id.AddEncounterRow);
        addEncounterTableRow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), AddEncounterActivity.class);
//                Intent intent = new Intent(getApplicationContext(), ThanksActivity.class);
                intent.putExtra("LanguageCode", LanguageCode);
                startActivity(intent);
            }
        });

        TableRow viewEncountersTableRow = (TableRow)findViewById(R.id.ViewEncountersRow);
        viewEncountersTableRow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), ShowEncounterScreenSlidePagerActivity.class);
                startActivity(intent);
            }
        });

        TableRow viewSynOverviewRow = (TableRow)findViewById(R.id.ViewSynOverviewRow);
        viewSynOverviewRow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), SyncoverviewActivity.class);
                intent.putExtra("LanguageCode", LanguageCode);
                startActivity(intent);
            }
        });

        TableRow TheIdeaRow = (TableRow)findViewById(R.id.TheIdeaRow);
        TheIdeaRow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), IdeaActivity.class);
                intent.putExtra("LanguageCode", LanguageCode);
                startActivity(intent);
            }
        });
    }

    @Override
    protected void onResume(){
        super.onResume();
        handleSyncText();
    }

    private void showAskForSyncToast(String text, int duration){
        LayoutInflater inflater = getLayoutInflater();
        View layout = inflater.inflate(R.layout.askforsync_toast,
                (ViewGroup) findViewById(R.id.AskForSync_toast_layout_root));

        ImageView image = (ImageView) layout.findViewById(R.id.AskForSync_image);
        image.setImageResource(R.drawable.mycloud);
        TextView textView = (TextView) layout.findViewById(R.id.AskForSync_text);
        textView.setText(text);
        textView.setTextSize(18);

        Toast toast = new Toast(getApplicationContext());
        toast.setGravity(Gravity.CENTER_VERTICAL, 0, 0);
        toast.setDuration(duration);
        toast.setView(layout);
        toast.show();
    }

    private void handleSyncText(){
        boolean unsyncedData = dbHelper.dbHasUnsyncedData(db);

        TextView HHSyncHeaderTextView = (TextView)findViewById(R.id.HSSyncHeader);
        TextView HSSyncTextTextView = (TextView)findViewById(R.id.HSSyncText);

        if (unsyncedData){
            //set color red
            HHSyncHeaderTextView.setTextColor(Color.parseColor("#f45f42"));
            HSSyncTextTextView.setTextColor(Color.parseColor("#f45f42"));
            HHSyncHeaderTextView.setTypeface(null, Typeface.BOLD);

            HHSyncHeaderTextView.setText(HSSyncHeaderAskSync);
            HSSyncTextTextView.setText(HSSyncTextAskSync);

            showAskForSyncToast(HSSyncTextAskSync,Toast.LENGTH_LONG);
        }
        else {
            HHSyncHeaderTextView.setText(HSSyncHeader);
            HSSyncTextTextView.setText(HSSyncText);
            HHSyncHeaderTextView.setTextColor(oldSyncHeaderColor);
            HSSyncTextTextView.setTextColor(oldSyncTextColor);
            HHSyncHeaderTextView.setTypeface(null, Typeface.NORMAL);
        }
    }

    private void ResolveLanguageResource(String languageCode) {
        Map<String, String> resLang = dbHelper.GetAllTextResouceForActivityAndLanguage("HomeScreenActivity", languageCode, db);

        if (resLang.containsKey("HomeScreenTitle")) {
            TextView HomeScreenTitleTextView = (TextView)findViewById(R.id.HomeScreenTitle);
            HomeScreenTitleTextView.setText(resLang.get("HomeScreenTitle"));
        }

        if (resLang.containsKey("HSSubTitle")) {
            TextView HomeScreenTitleTextView = (TextView)findViewById(R.id.HSSubTitle);
            HomeScreenTitleTextView.setText(resLang.get("HSSubTitle"));
        }

        if (resLang.containsKey("HSWhatHeader")) {
            TextView HomeScreenTitleTextView = (TextView)findViewById(R.id.HSWhatHeader);
            HomeScreenTitleTextView.setText(resLang.get("HSWhatHeader"));
        }

        if (resLang.containsKey("HSWhatText")) {
            TextView HomeScreenTitleTextView = (TextView)findViewById(R.id.HSWhatText);
            HomeScreenTitleTextView.setText(resLang.get("HSWhatText"));
        }

        if (resLang.containsKey("HSAddEncounterHeader")) {
            TextView HomeScreenTitleTextView = (TextView)findViewById(R.id.HSAddEncounterHeader);
            HomeScreenTitleTextView.setText(resLang.get("HSAddEncounterHeader"));
        }

        if (resLang.containsKey("HSAddEncounterText")) {
            TextView HomeScreenTitleTextView = (TextView)findViewById(R.id.HSAddEncounterText);
            HomeScreenTitleTextView.setText(resLang.get("HSAddEncounterText"));
        }

        if (resLang.containsKey("HSHistoryHeader")) {
            TextView HomeScreenTitleTextView = (TextView)findViewById(R.id.HSHistoryHeader);
            HomeScreenTitleTextView.setText(resLang.get("HSHistoryHeader"));
        }

        if (resLang.containsKey("HSHistoryText")) {
            TextView HomeScreenTitleTextView = (TextView)findViewById(R.id.HSHistoryText);
            HomeScreenTitleTextView.setText(resLang.get("HSHistoryText"));
        }


        if (resLang.containsKey("HSSyncHeaderAskSync")){
            HSSyncHeaderAskSync = resLang.get("HSSyncHeaderAskSync");
        }
        else {
            HSSyncHeaderAskSync = "Synchronize";
        }

        if (resLang.containsKey("HSSyncTextAskSync")){
            HSSyncTextAskSync = resLang.get("HSSyncTextAskSync");
        }
        else {
            HSSyncTextAskSync = "Please Synchronize with website";
        }

        if (resLang.containsKey("HSSyncHeader")){
            HSSyncHeader = resLang.get("HSSyncHeader");
        }
        else {
            HSSyncHeader = "Synchronize";
        }

        if (resLang.containsKey("HSSyncText")){
            HSSyncText = resLang.get("HSSyncText");
        }
        else {
            HSSyncText = "Synchronize with website";
        }
        handleSyncText();
    }
}
