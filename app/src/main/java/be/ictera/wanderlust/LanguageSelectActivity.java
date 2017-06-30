package be.ictera.wanderlust;

import android.app.ListActivity;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.v4.content.res.ResourcesCompat;
import android.view.View;
import android.widget.ListView;

import SelectLanguage.LanguageItem;
import SelectLanguage.LanguageListAdapter;

public class LanguageSelectActivity extends ListActivity {

    private LanguageListAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        LanguageItem[] languageItems = new LanguageItem[3];

        Drawable foo = ResourcesCompat.getDrawable(getResources(), R.drawable.nederlands, null);

        languageItems[0] = new LanguageItem("nederlands", ResourcesCompat.getDrawable(getResources(), R.drawable.nederlands, null));
        languageItems[1] = new LanguageItem("English", ResourcesCompat.getDrawable(getResources(), R.drawable.nederlands, null));
        languageItems[2] = new LanguageItem("Francais", ResourcesCompat.getDrawable(getResources(), R.drawable.nederlands, null));

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
