package com.d3coding.gmusicapi;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.design.widget.NavigationView;
import android.support.design.widget.TabLayout;
import android.support.v4.view.GravityCompat;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.Toast;

import com.d3coding.gmusicapi.ui.main.SectionsPagerAdapter;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    private static final int LOG_IN_ACTIVITY = 50;

    private static final int RESULT_ERROR = -4;

    private boolean mState = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.ac_blank);

        SharedPreferences mPresets = getApplicationContext().getSharedPreferences(getString(R.string.preferences_user), Context.MODE_PRIVATE);

        if (mPresets.contains(getString(R.string.token))) {

            setContentView(R.layout.ac_all);

            SectionsPagerAdapter sectionsPagerAdapter = new SectionsPagerAdapter(this, getSupportFragmentManager());
            ViewPager viewPager = findViewById(R.id.view_pager);
            viewPager.setAdapter(sectionsPagerAdapter);
            TabLayout tabs = findViewById(R.id.tabs);
            tabs.setupWithViewPager(viewPager);

            Toolbar toolbar = findViewById(R.id.toolbar);
            setSupportActionBar(toolbar);

            DrawerLayout drawer = findViewById(R.id.drawer_layout);
            NavigationView navigationView = findViewById(R.id.nav_view);
            ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                    this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
            drawer.addDrawerListener(toggle);
            toggle.syncState();
            navigationView.setNavigationItemSelectedListener(this);

            if (!mPresets.contains(getString(R.string.last_update)))
                refreshDB();

        } else {
            startActivityForResult(new Intent(this, Login.class), LOG_IN_ACTIVITY);
        }

    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        // MenuItem item = menu.findItem(R.id.addAction);
        // getMenuInflater().inflate(R.menu.main, menu);
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main, menu);

        menu.findItem(R.id.refresh).setVisible(mState);
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
            return true;
        } else if (id == R.id.action_logout) {
            getApplicationContext().getSharedPreferences(getString(R.string.preferences_user), Context.MODE_PRIVATE).edit().remove(getString(R.string.token)).apply();
            getApplicationContext().deleteDatabase(Gmusicdb.DATABASE_NAME);
            recreate();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case (LOG_IN_ACTIVITY): {
                if (resultCode == RESULT_OK) {
                    (getApplicationContext().getSharedPreferences(getString(R.string.preferences_user), Context.MODE_PRIVATE).edit())
                            .putString(getString(R.string.token), data.getStringExtra(getString(R.string.token)))
                            .apply();
                    recreate();
                } else if (resultCode == RESULT_CANCELED) {
                    finish();
                }
            }
            break;
        }
    }

    private void refreshDB() {
        SharedPreferences mPresets = getApplicationContext().getSharedPreferences(getString(R.string.preferences_user), Context.MODE_PRIVATE);
        new Gmusicnet(getApplicationContext()).execute(mPresets.getString(getString(R.string.token), ""));
    }

    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_all) {
            Toast.makeText(getApplicationContext(), "NULL", Toast.LENGTH_SHORT).show();
        } else if (id == R.id.nav_offline) {
            Toast.makeText(getApplicationContext(), "NULL", Toast.LENGTH_SHORT).show();
        } else if (id == R.id.nav_download) {
            Toast.makeText(getApplicationContext(), "NULL", Toast.LENGTH_SHORT).show();
        } else if (id == R.id.nav_advanced) {
            Toast.makeText(getApplicationContext(), "NULL", Toast.LENGTH_SHORT).show();
        } else if (id == R.id.nav_config) {
            Toast.makeText(getApplicationContext(), "NULL", Toast.LENGTH_SHORT).show();
        } else if (id == R.id.nav_log) {
            Toast.makeText(getApplicationContext(), "NULL", Toast.LENGTH_SHORT).show();
            mState = true;
            supportInvalidateOptionsMenu();
        } else if (id == R.id.nav_refresh) {
            refreshDB();
        } else if (id == R.id.nav_recreate) {
            recreate();
        } else if (id == R.id.nav_info) {
            Toast.makeText(getApplicationContext(), "NULL", Toast.LENGTH_SHORT).show();
            mState = false;
            supportInvalidateOptionsMenu();
        }

        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }
}
