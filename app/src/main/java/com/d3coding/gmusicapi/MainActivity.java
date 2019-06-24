package com.d3coding.gmusicapi;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.design.widget.NavigationView;
import android.support.design.widget.TabLayout;
import android.support.v4.app.ActivityCompat;
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

public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {

    static final int LOGIN_ACTIVITY = 11;

    private boolean mState = false;

    private DrawerLayout drawer;
    private NavigationView navigationView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.ac_blank);

        ActivityCompat.requestPermissions(MainActivity.this,
                new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, 1);

        SharedPreferences mPresets = getApplicationContext().getSharedPreferences(getString(R.string.preferences_user), Context.MODE_PRIVATE);

        if (mPresets.contains(getString(R.string.token))) {

            // TODO: validate token

            setContentView(R.layout.ac_all);

            SectionsPagerAdapter sectionsPagerAdapter = new SectionsPagerAdapter(this, getSupportFragmentManager());
            ViewPager viewPager = findViewById(R.id.view_pager);
            viewPager.setAdapter(sectionsPagerAdapter);
            TabLayout tabs = findViewById(R.id.tabs);
            tabs.setupWithViewPager(viewPager);

            Toolbar toolbar = findViewById(R.id.toolbar);
            setSupportActionBar(toolbar);

            drawer = findViewById(R.id.drawer_layout);
            navigationView = findViewById(R.id.nav_view);
            ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                    this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
            drawer.addDrawerListener(toggle);
            toggle.syncState();
            navigationView.setNavigationItemSelectedListener(this);

            if (!mPresets.contains(getString(R.string.last_update)))
                refreshDB();

        } else {
            startActivityForResult(new Intent(this, Login.class), LOGIN_ACTIVITY);
        }

    }

    @Override
    public void onBackPressed() {
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main, menu);
        menu.findItem(R.id.act_icon_refresh_log).setVisible(mState);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        if (id == R.id.act_icon_refresh_log) {
            return true;
        } else if (id == R.id.act_icon_search) {
            return true;
        } else if (id == R.id.action_clean_db) {
            getApplicationContext().deleteDatabase(Gmusicdb.DATABASE_NAME);
            recreate();
            return true;
        } else if (id == R.id.action_refresh_db) {
            // TODO: UpdateAlertDialog
            refreshDB();
            return true;
        } else if (id == R.id.action_recreate) {
            recreate();
        } else if (id == R.id.action_logout) {
            getApplicationContext().getSharedPreferences(getString(R.string.preferences_user), Context.MODE_PRIVATE).edit().remove(getString(R.string.token))
                    .remove(getString(R.string.last_update)).apply();
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
            case (LOGIN_ACTIVITY): {
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
        getApplicationContext().deleteDatabase(Gmusicdb.DATABASE_NAME);
        SharedPreferences mPresets = getApplicationContext().getSharedPreferences(getString(R.string.preferences_user), Context.MODE_PRIVATE);
        new Gmusicnet(MainActivity.this).execute(mPresets.getString(getString(R.string.token), ""));
    }

    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_all) {
            Toast.makeText(getApplicationContext(), "NULL", Toast.LENGTH_SHORT).show();
        } else if (id == R.id.nav_download) {
            // TODO: DownloadActivity
            Toast.makeText(getApplicationContext(), "NULL", Toast.LENGTH_SHORT).show();
        } else if (id == R.id.nav_offline) {
            // TODO: OfflineActivity
            Toast.makeText(getApplicationContext(), "NULL", Toast.LENGTH_SHORT).show();
        } else if (id == R.id.nav_playlist) {
            // TODO: PlaylistActivity
            Toast.makeText(getApplicationContext(), "NULL", Toast.LENGTH_SHORT).show();
        } else if (id == R.id.nav_settings) {
            // TODO: SettingsActivity
            Toast.makeText(getApplicationContext(), "NULL", Toast.LENGTH_SHORT).show();
        } else if (id == R.id.nav_log) {
            // TODO: LogActivity
            Toast.makeText(getApplicationContext(), "NULL", Toast.LENGTH_SHORT).show();
            mState = true;
            supportInvalidateOptionsMenu();
        } else if (id == R.id.nav_info) {
            // TODO: InfoActivity
            Toast.makeText(getApplicationContext(), "NULL", Toast.LENGTH_SHORT).show();
            mState = false;
            supportInvalidateOptionsMenu();
        }

        drawer.closeDrawer(GravityCompat.START);
        return true;
    }


}
