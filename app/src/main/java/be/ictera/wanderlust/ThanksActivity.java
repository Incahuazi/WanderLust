package be.ictera.wanderlust;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

public class ThanksActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_thanks);

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
        this.finish();
        startActivity(i);
    }
}
