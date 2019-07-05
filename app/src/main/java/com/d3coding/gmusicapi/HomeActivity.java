package com.d3coding.gmusicapi;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.v4.app.ActivityCompat;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.Toast;


public class HomeActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {


    static final int LOGIN_ACTIVITY = 11;

    private boolean mState = false;
    private DrawerLayout drawer;
    private NavigationView navigationView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.ac_blank);

        ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1);

        SharedPreferences mPresets = getSharedPreferences(getString(R.string.preferences_user), Context.MODE_PRIVATE);

        if (mPresets.contains(getString(R.string.token))) {

            // TODO: validate token
            setContentView(R.layout.ac_home);

            Toolbar toolbar = findViewById(R.id.toolbar);

            drawer = findViewById(R.id.drawer_layout);
            navigationView = findViewById(R.id.nav_view);

            setSupportActionBar(toolbar);

            ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
            drawer.addDrawerListener(toggle);
            toggle.syncState();
            navigationView.setNavigationItemSelectedListener(this);

            getSupportFragmentManager().beginTransaction().add(R.id.fragment_content, new HomeFragment()).commit();

            if (!mPresets.contains(getString(R.string.last_update)))
                refreshDB();
            else if (mPresets.getInt(getString(R.string.database_version), 0) != GMusicDB.getDatabaseVersion()) {
                refreshDB();
                mPresets.edit().putInt(getString(R.string.database_version), GMusicDB.getDatabaseVersion()).apply();
            }

        } else
            startActivityForResult(new Intent(this, LoginActivity.class), LOGIN_ACTIVITY);

    }

    @Override
    public void onBackPressed() {
        if (drawer.isDrawerOpen(GravityCompat.START))
            drawer.closeDrawer(GravityCompat.START);
        else
            super.onBackPressed();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.manu_main, menu);
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
            mState = false;
            supportInvalidateOptionsMenu();
            return true;
        } else if (id == R.id.act_icon_search) {
            Toast.makeText(this, getString(R.string.null_description), Toast.LENGTH_SHORT).show();
            return true;
        } else if (id == R.id.act_icon_filter) {
            ((HomeFragment) getSupportFragmentManager().findFragmentById(R.id.fragment_content)).showFilter();
            return true;
        } else if (id == R.id.action_clean_db) {
            deleteDatabase(GMusicDB.DATABASE_NAME);
            recreate();
            return true;
        } else if (id == R.id.action_refresh_db) {
            mState = true;
            supportInvalidateOptionsMenu();
            refreshDB();
            return true;
        } else if (id == R.id.action_recreate) {
            recreate();
        } else if (id == R.id.action_logout) {
            this.getSharedPreferences(getString(R.string.preferences_user), Context.MODE_PRIVATE).edit().remove(getString(R.string.token))
                    .remove(getString(R.string.last_update)).apply();
            deleteDatabase(GMusicDB.DATABASE_NAME);
            recreate();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == LOGIN_ACTIVITY) {
            if (resultCode == RESULT_OK) {
                (getSharedPreferences(getString(R.string.preferences_user), Context.MODE_PRIVATE).edit())
                        .putString(getString(R.string.token), data.getStringExtra(getString(R.string.token))).apply();
                recreate();
            } else if (resultCode == RESULT_CANCELED)
                finish();

        }

    }

    private void refreshDB() {
        deleteDatabase(GMusicDB.DATABASE_NAME);
        SharedPreferences mPresets = getSharedPreferences(getString(R.string.preferences_user), Context.MODE_PRIVATE);
        new GMusicNet(this).execute(mPresets.getString(getString(R.string.token), ""));
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
        // Handle navigation view item clicks here.
        int id = menuItem.getItemId();

        if (id == R.id.nav_all) {
            // TODO: HomeActivity
            recreate();
        } else if (id == R.id.nav_playlist) {
            // TODO: PlaylistActivity
            Toast.makeText(this, getString(R.string.null_description), Toast.LENGTH_SHORT).show();
        } else if (id == R.id.nav_settings) {
            // TODO: SettingsActivity
            Toast.makeText(this, getString(R.string.null_description), Toast.LENGTH_SHORT).show();
        } else if (id == R.id.nav_log) {
            // TODO: LogActivity
            Toast.makeText(this, getString(R.string.null_description), Toast.LENGTH_SHORT).show();
        } else if (id == R.id.nav_info) {
            // TODO: InfoActivity
            Toast.makeText(this, getString(R.string.null_description), Toast.LENGTH_SHORT).show();
        }

        drawer.closeDrawer(GravityCompat.START);
        return true;
    }
}

