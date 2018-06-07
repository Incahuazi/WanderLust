package be.ictera.wanderlust;

import android.app.ListActivity;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.drawable.Drawable;
import android.net.ConnectivityManager;
import android.os.Bundle;
import android.support.v4.content.res.ResourcesCompat;
import android.view.View;
import android.widget.ListView;

import java.util.ArrayList;

import Database.WanderLustDbHelper;
import SelectLanguage.LanguageItem;
import SelectLanguage.LanguageListAdapter;
import sync.NetworkStateChecker;

public class LanguageSelectActivity extends ListActivity {

    private LanguageListAdapter adapter;
    private WanderLustDbHelper dbHelper = null;
    private SQLiteDatabase db = null;
    private LanguageItem[] languageItems;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        registerReceiver(new NetworkStateChecker(), new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION));

        dbHelper = new WanderLustDbHelper(this);
//        db = dbHelper.getReadableDatabase();
        db = dbHelper.getWritableDatabase();


        ArrayList<String[]> languages = dbHelper.GetAllLanguages(db);

        languageItems = new LanguageItem[languages.size()];

        int languageCounter = 0;
        int colorSwitcher = 1;
        for (String[] item : languages) {

            if (item[2] != null && !item[2].isEmpty() && !item[2].equalsIgnoreCase("null")) {
                int resourceId = getResources().getIdentifier(item[2], "drawable", getPackageName());
                languageItems[languageCounter] = new LanguageItem(item[1], ResourcesCompat.getDrawable(getResources(), resourceId, null), false, item[0]);
            } else {
                Drawable drawable;
                switch (colorSwitcher) {
                    case 1:
                        languageItems[languageCounter] = new LanguageItem(item[1], ResourcesCompat.getDrawable(getResources(), R.drawable.blank_blue, null), true, item[0]);
                        colorSwitcher++;
                        break;
                    case 2:
                        languageItems[languageCounter] = new LanguageItem(item[1], ResourcesCompat.getDrawable(getResources(), R.drawable.blank_orange, null), true, item[0]);
                        colorSwitcher++;
                        break;
                    case 3:
                        languageItems[languageCounter] = new LanguageItem(item[1], ResourcesCompat.getDrawable(getResources(), R.drawable.blank_green, null), true, item[0]);
                        colorSwitcher = 1;
                        break;
                }
            }
            languageCounter++;
        }

        this.adapter = new LanguageListAdapter(this, R.layout.activity_language_select, languageItems);
        this.getListView().setDivider(null);
        this.setListAdapter(adapter);
    }

    @Override
    protected void onListItemClick(ListView l, View v, int position, long id) {
        LanguageItem foo = this.adapter.getItem(position);
        Intent intent = new Intent(this, HomeScreenActivity.class);
        intent.putExtra("LanguageCode", languageItems[position].LanguageCode);
        startActivity(intent);
    }
}
