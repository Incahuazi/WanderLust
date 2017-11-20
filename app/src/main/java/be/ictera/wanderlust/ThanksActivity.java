package be.ictera.wanderlust;

import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import java.util.Map;

import Database.WanderLustDbHelper;

public class ThanksActivity extends AppCompatActivity {

    private WanderLustDbHelper dbHelper = null;
    private SQLiteDatabase db = null;
    private String LanguageCode = "ENG";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_thanks);

        dbHelper = new WanderLustDbHelper(this);
        db = dbHelper.getReadableDatabase();

        LanguageCode= getIntent().getStringExtra("LanguageCode");
        ResolveLanguageResource(LanguageCode);

        Button buttonClose = (Button)findViewById(R.id.buttonClose);
        Button buttonIdea = (Button)findViewById(R.id.buttonIdea);

        buttonIdea.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                ShowIdeaActivity();
            }});

        buttonClose.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                CloseActivity();
            }});
    }

    private void CloseActivity() {
        this.finish();
    }

    private void ShowIdeaActivity(){
        this.finish();

        Intent i = new Intent(getApplicationContext(), IdeaActivity.class);
        i.putExtra("LanguageCode", LanguageCode);

        this.finish();
        startActivity(i);
    }

    private void ResolveLanguageResource(String languageCode) {
        Map<String, String> resLang = dbHelper.GetAllTextResouceForActivityAndLanguage("ThanksActivity", languageCode, db);

        if (resLang.containsKey("THXTitle")) {
            TextView textView = (TextView) findViewById(R.id.THXTitle);
            textView.setText(resLang.get("THXTitle"));
        }

        if (resLang.containsKey("THXNext")) {
            TextView textView = (TextView) findViewById(R.id.THXNext);
            textView.setText(resLang.get("THXNext"));
        }

        if (resLang.containsKey("THXNextExplain")) {
            TextView textView = (TextView) findViewById(R.id.THXNextExplain);
            textView.setText(resLang.get("THXNextExplain"));
        }

        if (resLang.containsKey("THXButtonIdea")) {
            Button button = (Button) findViewById(R.id.buttonIdea);
            button.setText(resLang.get("THXButtonIdea"));
        }

        if (resLang.containsKey("THXButtonClose")) {
            Button button = (Button) findViewById(R.id.buttonClose);
            button.setText(resLang.get("THXButtonClose"));
        }
    }
}
