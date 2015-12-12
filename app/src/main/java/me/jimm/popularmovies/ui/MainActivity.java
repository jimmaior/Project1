package me.jimm.popularmovies.ui;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import me.jimm.popularmovies.R;
import me.jimm.popularmovies.SettingsFragment;

public class MainActivity extends AppCompatActivity {

    private final String TAG = MainActivity.class.getSimpleName();


    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // toolbar support
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);


        // check that the activity is using a layout which will contain the fragment
        if (findViewById(R.id.fragment_container) != null) {
            // However, if were being restored from a previous state, then we don't need
            // to do anything
            if (savedInstanceState != null) {
                return;
            }

            // create a new fragment to be placed in the activity layout
            MainFragment mainFragment = new MainFragment();

            // in case, this activity was started with special instructions from an Intent,
            // pass the Intent's extras to the fragment as arguments
            mainFragment.setArguments(getIntent().getExtras());

            // add the fragment to the fragment contain
            getSupportFragmentManager().beginTransaction()
                .add(R.id.fragment_container, mainFragment)
                    .commit();
        }


        // TODO: define a second activity_main layout file for folder layout-sw600dp
        // This file should be build for a two panel mode layout. If the second panel is not null,
        // then, set the mode as TwoPanel and replace it with a fragment that shows the detailed information
        // if the view is null, then we are in single pane mode.
        // http://developer.android.com/guide/practices/screens_support.html#DeclaringTabletLayouts
//        MainFragment mainFragment = (MainFragment) getSupportFragmentManager()
//                .findFragmentById(R.id.fragment);
//

    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.

        getMenuInflater().inflate(R.menu.menu_main, menu);
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
            Toast.makeText(this, "Clicked Settings + id=" + id, Toast.LENGTH_LONG).show();

            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.fragment_container, new SettingsFragment())
                    .addToBackStack(null)   // used ot be able to return to the MainFragment from the settingsFragment
                    .commit();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
