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

import com.d3coding.gmusicapi.fragments.HomeFragment;
import com.d3coding.gmusicapi.fragments.PlaylistFragment;
import com.d3coding.gmusicapi.gmusic.Database;
import com.d3coding.gmusicapi.gmusic.Network;
import com.google.android.material.bottomappbar.BottomAppBar;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.navigation.NavigationView;

public class HomeActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {

    private DrawerLayout drawer;
    private NavigationView navigationView;

    private BottomAppBar toolbar;
    private FloatingActionButton fab;
    SearchView searchView;

    MenuItem refreshLogItem;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.ac_blank);

        // TODO: Request permissions only when downloading
        ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1);

        SharedPreferences mPresets = getSharedPreferences(getString(R.string.preferences_user), Context.MODE_PRIVATE);

        if (mPresets.contains(getString(R.string.token))) {

            // TODO: validate token
            setContentView(R.layout.ac_home);

            toolbar = findViewById(R.id.toolbar);
            drawer = findViewById(R.id.drawer_layout);
            navigationView = findViewById(R.id.nav_view);
            fab = findViewById(R.id.filter_button);

            setSupportActionBar(toolbar);

            ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
            drawer.addDrawerListener(toggle);
            toggle.syncState();
            navigationView.setNavigationItemSelectedListener(this);

            if (!mPresets.contains(getString(R.string.last_update)))
                refreshDB();

            swapAndSetupFragment(new HomeFragment());

        } else
            startActivityForResult(new Intent(this, LoginActivity.class), Config.LOGIN_ACTIVITY);

    }

    void swapAndSetupFragment(Fragment fragment) {
        getSupportFragmentManager().beginTransaction().replace(R.id.fragment_content, fragment).commit();
        getSupportFragmentManager().executePendingTransactions();

        if (fragment.getClass() == HomeFragment.class) {
            fab.setOnClickListener((v) -> ((HomeFragment) fragment).showFilter());

            ((HomeFragment) fragment).addOnScrollListener(new RecyclerView.OnScrollListener() {
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

        } else if (fragment.getClass() == PlaylistFragment.class) {
            fab.setOnClickListener(null);
        }

        // TODO: reInflateOptionsMenuAccordingToFragment
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);

        MenuItem menuItem = menu.findItem(R.id.act_icon_search);
        refreshLogItem = menu.findItem(R.id.act_icon_refresh_log);

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
                HomeFragment homeFragment = (HomeFragment) getFragment(HomeFragment.class);
                if (homeFragment != null)
                    homeFragment.filter(query);
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                HomeFragment homeFragment = (HomeFragment) getFragment(HomeFragment.class);
                if (homeFragment != null)
                    homeFragment.filter(newText);
                return false;
            }
        });

        searchView.findViewById(androidx.appcompat.R.id.search_plate).setBackgroundColor(Color.TRANSPARENT);
        ((EditText) searchView.findViewById(androidx.appcompat.R.id.search_src_text)).setHintTextColor(Color.GRAY);

        return true;
    }

    @Override
    public void onBackPressed() {
        if (drawer.isDrawerOpen(GravityCompat.START))
            drawer.closeDrawer(GravityCompat.START);
        else {
            if (searchView.hasFocus()) {
                searchView.setIconified(true);
            } else
                super.onBackPressed();
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.act_icon_refresh_log) {
            refreshLogItem.setVisible(false);
            return true;
        } else if (id == R.id.action_download_list) {
            HomeFragment homeFragment = (HomeFragment) getFragment(HomeFragment.class);
            if (homeFragment != null)
                if (homeFragment.getNumItems() <= Config.MAX_DOWNLOADS)
                    new AlertDialog.Builder(this, R.style.AppTheme_AlertDialog).setPositiveButton("Download!", (DialogInterface dialog, int which) -> homeFragment.downloadFilter()).setTitle("Caution!")
                            .setMessage("By clicking download you will init a thread for every music in the screen, this can take some space and last longer...").setCancelable(true).create().show();
                else
                    new AlertDialog.Builder(this, R.style.AppTheme_AlertDialog).setTitle("Too many musics!").setMessage("You can filter to get a minor number...")
                            .setPositiveButton("ok", null).create().show();

        } else if (id == R.id.action_clean_db) {
            deleteDatabase(Database.DATABASE_NAME);
            swapAndSetupFragment(new HomeFragment());
            return true;
        } else if (id == R.id.action_refresh_db) {
            refreshLogItem.setVisible(true);
            refreshDB();
            return true;
        } else if (id == R.id.action_recreate)
            swapAndSetupFragment(new HomeFragment());

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == Config.LOGIN_ACTIVITY) {
            if (resultCode == RESULT_OK) {
                (getSharedPreferences(getString(R.string.preferences_user), Context.MODE_PRIVATE).edit()).putString(getString(R.string.token), data.getStringExtra(getString(R.string.token))).apply();
                recreate();
            } else if (resultCode == RESULT_CANCELED)
                finish();

        }

    }

    private void refreshDB() {
        deleteDatabase(Database.DATABASE_NAME);
        SharedPreferences mPresets = getSharedPreferences(getString(R.string.preferences_user), Context.MODE_PRIVATE);
        new Network(this).execute(mPresets.getString(getString(R.string.token), ""));
    }

    private Fragment getFragment(Class mClass) {
        Fragment myFragment = getSupportFragmentManager().findFragmentById(R.id.fragment_content);
        if (mClass == HomeFragment.class && myFragment.getClass() == HomeFragment.class) {
            return myFragment;
        } else if (mClass == PlaylistFragment.class && myFragment.getClass() == PlaylistFragment.class) {
            return myFragment;
        } else
            return null;
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
        // Handle navigation view item clicks here.
        int id = menuItem.getItemId();

        if (id == R.id.nav_all)
            swapAndSetupFragment(new HomeFragment());
        else if (id == R.id.nav_playlist)
            swapAndSetupFragment(new PlaylistFragment());
        else if (id == R.id.nav_settings)
            Toast.makeText(this, getString(R.string.null_description), Toast.LENGTH_SHORT).show();
        else if (id == R.id.nav_log)
            Toast.makeText(this, getString(R.string.null_description), Toast.LENGTH_SHORT).show();
        else if (id == R.id.nav_info)
            Toast.makeText(this, getString(R.string.null_description), Toast.LENGTH_SHORT).show();
        else if (id == R.id.nav_logout) {
            this.getSharedPreferences(getString(R.string.preferences_user), Context.MODE_PRIVATE).edit().remove(getString(R.string.token)).remove(getString(R.string.last_update)).apply();
            deleteDatabase(Database.DATABASE_NAME);
            recreate();
        }
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

}

