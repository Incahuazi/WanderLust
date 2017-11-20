package be.ictera.wanderlust;

import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

import java.util.Map;

import Database.WanderLustDbHelper;

public class HomeScreenActivity extends AppCompatActivity {

    private WanderLustDbHelper dbHelper = null;
    private SQLiteDatabase db = null;
    private String LanguageCode = "ENG";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home_screen);

        dbHelper = new WanderLustDbHelper(this);
        db = dbHelper.getReadableDatabase();


        LanguageCode= getIntent().getStringExtra("LanguageCode");
        ResolveLanguageResource(LanguageCode);

        TableRow addEncounterTableRow = (TableRow)findViewById(R.id.AddEncounterRow);
        addEncounterTableRow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                Intent intent = new Intent(getApplicationContext(), AddEncounterActivity.class);
                Intent intent = new Intent(getApplicationContext(), ThanksActivity.class);
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

        if (resLang.containsKey("HSSyncHeader")) {
            TextView HomeScreenTitleTextView = (TextView)findViewById(R.id.HSSyncHeader);
            HomeScreenTitleTextView.setText(resLang.get("HSSyncHeader"));
        }

        if (resLang.containsKey("HSSyncText")) {
            TextView HomeScreenTitleTextView = (TextView)findViewById(R.id.HSSyncText);
            HomeScreenTitleTextView.setText(resLang.get("HSSyncText"));
        }
    }
}
