package no.enkeloversikt.newroute.new_route;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import no.enkeloversikt.newroute.new_route.database.DatabaseHelper;


public class FinishedActivity extends AppCompatActivity {

    private DatabaseHelper db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_finished);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);


        db = new DatabaseHelper(this);

        TextView statsView = (TextView) findViewById(R.id.stats);

        String level = db.fetchType("level");
        if(level == null){
            db.updateOrInsert("level", "1");
            level = "1";
        }

        String statsText = "You have " + Integer.toString(db.getScore()) + " points and are on level " +
                level;
        statsView.setText(statsText);

        final Intent mapsActivity = new Intent(this, MapsActivity.class);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(mapsActivity);
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_information, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            Intent settingsActivity = new Intent(this, SettingsListActivity.class);
            startActivity(settingsActivity);
        }
        if (id == R.id.action_credits) {
            Intent creditsActivity = new Intent(this, CreditsActivity.class);
            startActivity(creditsActivity);
        }

        return super.onOptionsItemSelected(item);
    }

}
