package com.d3coding.gmusicapi;

import android.Manifest;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.core.app.ActivityCompat;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.bottomappbar.BottomAppBar;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.navigation.NavigationView;


public class HomeActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {

    static final int LOGIN_ACTIVITY = 11;

    private static final int MAX_DOWNLOADS = 25;

    private boolean mState = false;
    private DrawerLayout drawer;
    private NavigationView navigationView;

    private BottomAppBar toolbar;
    private FloatingActionButton fab;
    SearchView searchView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.ac_blank);

        ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1);

        SharedPreferences mPresets = getSharedPreferences(getString(R.string.preferences_user), Context.MODE_PRIVATE);

        if (mPresets.contains(getString(R.string.token))) {

            // TODO: validate token
            setContentView(R.layout.ac_home);

            toolbar = findViewById(R.id.toolbar);

            drawer = findViewById(R.id.drawer_layout);
            navigationView = findViewById(R.id.nav_view);

            setSupportActionBar(toolbar);

            fab = findViewById(R.id.filter_button);
            fab.setOnClickListener((v) -> {
                HomeFragment homeFragment = (HomeFragment) getFragment(fragment.HOME);
                if (homeFragment != null)
                    homeFragment.showFilter();
            });

            ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
            drawer.addDrawerListener(toggle);
            toggle.syncState();
            navigationView.setNavigationItemSelectedListener(this);

            if (!mPresets.contains(getString(R.string.last_update)))
                refreshDB();
            else if (mPresets.getInt(getString(R.string.database_version), 0) != GMusicDB.getDatabaseVersion()) {
                refreshDB();
                mPresets.edit().putInt(getString(R.string.database_version), GMusicDB.getDatabaseVersion()).apply();
            }

            swapFragment(fragment.HOME);


        } else
            startActivityForResult(new Intent(this, LoginActivity.class), LOGIN_ACTIVITY);

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        menu.findItem(R.id.act_icon_refresh_log).setVisible(mState);

        MenuItem menuItem = menu.findItem(R.id.act_icon_search);

        menuItem.setOnActionExpandListener(new MenuItem.OnActionExpandListener() {
            @Override
            public boolean onMenuItemActionExpand(MenuItem item) {
                fab.hide();
                return true;
            }

            @Override
            public boolean onMenuItemActionCollapse(MenuItem item) {
                fab.show();
                return true;
            }
        });

        searchView = (SearchView) menuItem.getActionView();
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                HomeFragment homeFragment = (HomeFragment) getFragment(fragment.HOME);
                if (homeFragment != null)
                    homeFragment.filter(query);
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                HomeFragment homeFragment = (HomeFragment) getFragment(fragment.HOME);
                if (homeFragment != null)
                    homeFragment.filter(newText);
                return false;
            }
        });

        searchView.findViewById(androidx.appcompat.R.id.search_plate).setBackgroundColor(Color.TRANSPARENT);

        EditText searchEditText = searchView.findViewById(androidx.appcompat.R.id.search_src_text);
        searchEditText.setHintTextColor(Color.GRAY);

        return true;
    }

    @Override
    public void onBackPressed() {
        if (drawer.isDrawerOpen(GravityCompat.START))
            drawer.closeDrawer(GravityCompat.START);
        else {
            if (searchView != null) {
                searchView.setIconified(true);
            } else
                super.onBackPressed();
        }
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
        } else if (id == R.id.action_download_list) {
            HomeFragment homeFragment = (HomeFragment) getFragment(fragment.HOME);
            if (homeFragment != null)
                if (homeFragment.getNumItems() <= MAX_DOWNLOADS)
                    new AlertDialog.Builder(this, R.style.AppTheme_AlertDialog).setPositiveButton("Download!", (DialogInterface dialog, int which) -> {
                        homeFragment.downloadFilter();
                    }).setTitle("Caution!").setMessage("By clicking download you will init a thread for every music in the screen, this can take some space and last longer...").setCancelable(true).create().show();
                else
                    new AlertDialog.Builder(this, R.style.AppTheme_AlertDialog).setTitle("Too many musics!").setMessage("You can filter to get a minor number...").setCancelable(true).create().show();

        } else if (id == R.id.action_clean_db) {
            deleteDatabase(GMusicDB.DATABASE_NAME);
            swapFragment(fragment.HOME);
            return true;
        } else if (id == R.id.action_refresh_db) {
            mState = true;
            supportInvalidateOptionsMenu();
            refreshDB();
            return true;
        } else if (id == R.id.action_recreate) {
            swapFragment(fragment.HOME);
        } else if (id == R.id.action_logout) {
            this.getSharedPreferences(getString(R.string.preferences_user), Context.MODE_PRIVATE).edit().remove(getString(R.string.token)).remove(getString(R.string.last_update)).apply();
            deleteDatabase(GMusicDB.DATABASE_NAME);
            swapFragment(fragment.HOME);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void swapFragment(fragment mFragment) {
        if (mFragment == fragment.PLAYLIST) {
            getSupportFragmentManager().beginTransaction().replace(R.id.fragment_content, new PlaylistFragment()).commit();
        } else {
            getSupportFragmentManager().beginTransaction().replace(R.id.fragment_content, new HomeFragment()).commit();
            getSupportFragmentManager().executePendingTransactions();

            HomeFragment homeFragment = (HomeFragment) getFragment(fragment.HOME);
            if (homeFragment != null) {
                homeFragment.setOnReachListEndListener(new HomeFragment.OnReachListEndListener() {
                    @Override
                    public void OnReachListEnd(RecyclerView recyclerView) {
                        if (fab != null) {
                            recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
                                @Override
                                public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                                    super.onScrollStateChanged(recyclerView, newState);
                                    if (!recyclerView.canScrollVertically(1))
                                        fab.hide();
                                    else if (!fab.isExpanded()) {
                                        fab.show();
                                        fab.setCompatElevation(10);
                                    }
                                }
                            });
                        }
                    }
                });
            }

        }

    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == LOGIN_ACTIVITY) {
            if (resultCode == RESULT_OK) {
                (getSharedPreferences(getString(R.string.preferences_user), Context.MODE_PRIVATE).edit()).putString(getString(R.string.token), data.getStringExtra(getString(R.string.token))).apply();
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

    private Fragment getFragment(fragment mFragment) {
        Fragment myFragment = getSupportFragmentManager().findFragmentById(R.id.fragment_content);
        if (mFragment == fragment.HOME && myFragment.getClass().getName() == HomeFragment.class.getName()) {
            return myFragment;
        } else if (mFragment == fragment.PLAYLIST && myFragment.getClass().getName() == PlaylistFragment.class.getName()) {
            return myFragment;
        } else
            return null;
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
        // Handle navigation view item clicks here.
        int id = menuItem.getItemId();

        if (id == R.id.nav_all) {
            swapFragment(fragment.HOME);
        } else if (id == R.id.nav_playlist) {
            swapFragment(fragment.PLAYLIST);
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

    private enum fragment {
        HOME, PLAYLIST
    }
}

