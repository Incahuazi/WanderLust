package be.ictera.wanderlust;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.TableRow;
import android.widget.Toast;

public class HomeScreenActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home_screen);

        TableRow addEncounterTableRow = (TableRow)findViewById(R.id.AddEncounterRow);
        addEncounterTableRow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), AddEncounterActivity.class);
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
                startActivity(intent);
            }
        });

        TableRow TheIdeaRow = (TableRow)findViewById(R.id.TheIdeaRow);
        TheIdeaRow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), IdeaActivity.class);
                startActivity(intent);
            }
        });
    }
}
