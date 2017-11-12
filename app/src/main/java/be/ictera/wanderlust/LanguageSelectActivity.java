package be.ictera.wanderlust;

import android.app.ListActivity;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.drawable.Drawable;
import android.net.ConnectivityManager;
import android.os.Bundle;
import android.support.v4.content.res.ResourcesCompat;
import android.view.View;
import android.widget.ListView;

import SelectLanguage.LanguageItem;
import SelectLanguage.LanguageListAdapter;
import Sync.NetworkStateChecker;

public class LanguageSelectActivity extends ListActivity {

    private LanguageListAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        registerReceiver(new NetworkStateChecker(), new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION));

        LanguageItem[] languageItems = new LanguageItem[5];

        Drawable foo = ResourcesCompat.getDrawable(getResources(), R.drawable.nederlands, null);

        languageItems[0] = new LanguageItem("nederlands", ResourcesCompat.getDrawable(getResources(), R.drawable.nederlands_round, null));
        languageItems[1] = new LanguageItem("Francais", ResourcesCompat.getDrawable(getResources(), R.drawable.spanish_round, null));
        languageItems[2] = new LanguageItem("English", ResourcesCompat.getDrawable(getResources(), R.drawable.english_round, null));
        languageItems[3] = new LanguageItem("Francais", ResourcesCompat.getDrawable(getResources(), R.drawable.french_round, null));
        languageItems[4] = new LanguageItem("Francais", ResourcesCompat.getDrawable(getResources(), R.drawable.german_round, null));



        this.adapter = new LanguageListAdapter(this, R.layout.activity_language_select, languageItems);
        this.setListAdapter(adapter);
    }

    @Override
    protected void onListItemClick(ListView l, View v, int position, long id) {
//        this.adapter.getItem(position).click(this.getApplicationContext());
        LanguageItem foo = this.adapter.getItem(position);
//        Intent intent = new Intent(this, AddEncounterActivity.class);
//        Intent intent = new Intent(this, personListActivity.class);

//        Intent intent = new Intent(this, ShowEncounterScreenSlidePagerActivity.class);

        Intent intent = new Intent(this,HomeScreenActivity.class);

        startActivity(intent);


    }
}
