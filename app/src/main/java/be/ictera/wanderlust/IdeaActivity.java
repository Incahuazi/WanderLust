package be.ictera.wanderlust;

import android.database.sqlite.SQLiteDatabase;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.TextView;

import java.util.Map;

import Database.WanderLustDbHelper;

public class IdeaActivity extends AppCompatActivity {

    private WanderLustDbHelper dbHelper = null;
    private SQLiteDatabase db = null;
    private String LanguageCode = "ENG";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_idea);

        dbHelper = new WanderLustDbHelper(this);
        db = dbHelper.getReadableDatabase();

        LanguageCode= getIntent().getStringExtra("LanguageCode");
        ResolveLanguageResource(LanguageCode);
    }

    private void ResolveLanguageResource(String languageCode) {
        Map<String, String> resLang = dbHelper.GetAllTextResouceForActivityAndLanguage("IdeaActivity", languageCode, db);

        if (resLang.containsKey("TIDDestination")) {
            TextView textView = (TextView)findViewById(R.id.TIDDestination);
            textView.setText(resLang.get("TIDDestination"));
        }

        if (resLang.containsKey("TIDExplanation")) {
            TextView textView = (TextView)findViewById(R.id.TIDExplanation);
            textView.setText(resLang.get("TIDExplanation"));
        }

        if (resLang.containsKey("TIDHowWorks")) {
            TextView textView = (TextView)findViewById(R.id.TIDHowWorks);
            textView.setText(resLang.get("TIDHowWorks"));
        }

        if (resLang.containsKey("TIDHowWorksText")) {
            TextView textView = (TextView)findViewById(R.id.TIDHowWorksText);
            textView.setText(resLang.get("TIDHowWorksText"));
        }

        if (resLang.containsKey("TIDStep1Header")) {
            TextView textView = (TextView)findViewById(R.id.TIDStep1Header);
            textView.setText(resLang.get("TIDStep1Header"));
        }

        if (resLang.containsKey("TIDStep2Header")) {
            TextView textView = (TextView)findViewById(R.id.TIDStep2Header);
            textView.setText(resLang.get("TIDStep2Header"));
        }

        if (resLang.containsKey("TIDStep3Header")) {
            TextView textView = (TextView)findViewById(R.id.TIDStep3Header);
            textView.setText(resLang.get("TIDStep3Header"));
        }

        if (resLang.containsKey("TIDTitle")) {
            TextView textView = (TextView)findViewById(R.id.TIDTitle);
            textView.setText(resLang.get("TIDTitle"));
        }
    }
}
