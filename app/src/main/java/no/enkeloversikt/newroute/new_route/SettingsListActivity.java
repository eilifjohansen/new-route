package no.enkeloversikt.newroute.new_route;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import no.enkeloversikt.newroute.new_route.database.DatabaseHelper;

public class SettingsListActivity extends AppCompatActivity {
    DatabaseHelper db;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings_list);

        db = new DatabaseHelper(this);

        Button reset = (Button) findViewById(R.id.reset);
        final Intent InformationActivity = new Intent(this, InformationActivity.class);
        reset.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(db.killLocations()){
                    Toast.makeText(getBaseContext(), "Location reset", Toast.LENGTH_SHORT).show();
                    startActivity(InformationActivity);
                } else {
                    Toast.makeText(getBaseContext(), "No locations to reset.", Toast.LENGTH_SHORT).show();
                }
            }
        });

        Button score = (Button) findViewById(R.id.points);
        score.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(db.killScore()){
                    Toast.makeText(getBaseContext(), "Score reset", Toast.LENGTH_SHORT).show();
                    startActivity(InformationActivity);
                } else {
                    Toast.makeText(getBaseContext(), "No score to reset.", Toast.LENGTH_SHORT).show();
                }
            }
        });


    }
}
