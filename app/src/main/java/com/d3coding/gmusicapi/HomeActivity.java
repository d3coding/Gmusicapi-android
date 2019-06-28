package com.d3coding.gmusicapi;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.support.design.widget.NavigationView;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.FileProvider;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.graphics.Palette;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AnimationUtils;
import android.view.animation.LayoutAnimationController;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.d3coding.gmusicapi.items.MusicAdapter;
import com.d3coding.gmusicapi.items.MusicItem;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class HomeActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {

    static final int LOGIN_ACTIVITY = 11;
    GMusicDB db;
    GMusicFile gmusicFile;
    private boolean mState = false;
    private DrawerLayout drawer;
    private NavigationView navigationView;
    private RecyclerView recyclerView;
    private List<MusicItem> ConvertList = new ArrayList<>();
    private MusicAdapter mAdapter;

    private GMusicDB.Sort sort;
    private GMusicDB.SortOnline sortOnline;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.ac_blank);

        ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, 1);

        SharedPreferences mPresets = getSharedPreferences(getString(R.string.preferences_user), Context.MODE_PRIVATE);

        if (mPresets.contains(getString(R.string.token))) {

            // TODO: validate token

            setContentView(R.layout.ac_home);

            {

                gmusicFile = new GMusicFile(this);

                recyclerView = findViewById(R.id.music_item);

                mAdapter = new MusicAdapter(ConvertList);

                int resId = R.anim.layout_animation_fall_down;
                LayoutAnimationController animation = AnimationUtils.loadLayoutAnimation(this, resId);
                recyclerView.setLayoutAnimation(animation);
                recyclerView.setLayoutManager(new LinearLayoutManager(this));
                recyclerView.setItemAnimator(new DefaultItemAnimator());
                recyclerView.setAdapter(mAdapter);

                {
                    if (db == null)
                        db = new GMusicDB(this);

                    sort = GMusicDB.Sort.title;
                    sortOnline = GMusicDB.SortOnline.all;

                    ConvertList.addAll(db.getMusicItems(sort, sortOnline, "", false));

                    mAdapter.notifyDataSetChanged();
                }

                mAdapter.setOnItemClickListener((view, position) -> {

                    Bitmap bitmap = gmusicFile.getBitmapThumbImage(ConvertList.get(position).getUUID());
                    if (bitmap == null)
                        bitmap = gmusicFile.getDefaultThumb();

                    AlertDialog.Builder builder = new AlertDialog.Builder(this);

                    ViewGroup vView = (ViewGroup) getLayoutInflater().inflate(R.layout.ad_music_info, null);

                    ((ImageView) vView.findViewById(R.id.info_albumArt)).setImageBitmap(bitmap);
                    Palette p = Palette.from(bitmap).generate();

                    TextView textView = vView.findViewById(R.id.info_title);

                    textView.setText(ConvertList.get(position).getTitle());
                    textView.setTextColor(p.getDarkVibrantColor(Color.BLACK));
                    ((GradientDrawable) textView.getBackground()).setColor(p.getLightVibrantColor(Color.WHITE));

                    vView.findViewById(R.id.info_background).setBackgroundColor(p.getMutedColor(Color.WHITE));

                    ((TextView) vView.findViewById(R.id.info_album)).setText(ConvertList.get(position).getAlbum());
                    ((TextView) vView.findViewById(R.id.info_artist)).setText(ConvertList.get(position).getArtist());
                    ((TextView) vView.findViewById(R.id.info_time)).setText(ConvertList.get(position).getDuration());

                    builder.setView(vView).create().show();

                });


                mAdapter.setOnItemLongClickListener((view, position) -> {

                    Bitmap bitmap = gmusicFile.getBitmapThumbImage(ConvertList.get(position).getUUID());
                    if (bitmap == null)
                        bitmap = gmusicFile.getDefaultThumb();

                    AlertDialog.Builder builder = new AlertDialog.Builder(this);
                    ViewGroup vView = (ViewGroup) getLayoutInflater().inflate(R.layout.ad_music_opt, null);

                    LinearLayout linearLayoutComplete = vView.findViewById(R.id.status_complete);
                    LinearLayout linearLayoutDownloading = vView.findViewById(R.id.status_downloading);
                    vView.findViewById(R.id.status_downloading).setVisibility(View.GONE);

                    ((ImageView) vView.findViewById(R.id.opt_album_art)).setImageBitmap(bitmap);
                    ((TextView) vView.findViewById(R.id.opt_title)).setText(ConvertList.get(position).getTitle());
                    ((TextView) vView.findViewById(R.id.opt_artist)).setText(ConvertList.get(position).getArtist());

                    if (ConvertList.get(position).getDownloadStatus()) {

                        linearLayoutComplete.setVisibility(View.VISIBLE);
                        linearLayoutDownloading.setVisibility(View.GONE);

                    } else {

                        linearLayoutComplete.setVisibility(View.GONE);
                        linearLayoutDownloading.setVisibility(View.VISIBLE);

                        // TODO: getDownloadStatus
                        gmusicFile.addToQueue(ConvertList.get(position).getUUID(), linearLayoutComplete, linearLayoutDownloading);
                    }

                    vView.findViewById(R.id.opt_bt_delete).setOnClickListener((view2) -> Toast.makeText(this, getString(R.string.null_description), Toast.LENGTH_SHORT).show());

                    vView.findViewById(R.id.opt_bt_play).setOnClickListener((view2) -> {
                        Uri uri = FileProvider.getUriForFile(this, this.getPackageName() + ".fileprovider",
                                new File(new File(Environment.getExternalStorageDirectory(), "Gmusicapi"), ConvertList.get(position).getUUID() + ".mp3"));

                        Intent intent = new Intent(Intent.ACTION_VIEW);
                        intent.setDataAndType(uri, getContentResolver().getType(uri))
                                .addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                        startActivity(intent);

                    });

                    vView.findViewById(R.id.opt_bt_open).setOnClickListener((view2) -> {
                        Uri uri = Uri.parse(Environment.getExternalStorageDirectory() + "/Gmusicapi/");
                        startActivity(new Intent(Intent.ACTION_VIEW).setDataAndType(uri, "resource/folder"));

                    });

                    builder.setView(vView).create().show();

                });

            }

            Toolbar toolbar = findViewById(R.id.toolbar);
            setSupportActionBar(toolbar);

            drawer = findViewById(R.id.drawer_layout);
            navigationView = findViewById(R.id.nav_view);
            ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this,
                    drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
            drawer.addDrawerListener(toggle);
            toggle.syncState();
            navigationView.setNavigationItemSelectedListener(this);

            if (!mPresets.contains(getString(R.string.last_update)))
                refreshDB();

        } else {
            startActivityForResult(new Intent(this, LoginActivity.class), LOGIN_ACTIVITY);
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
            return true;
        } else if (id == R.id.act_icon_search) {
            return true;
        } else if (id == R.id.act_icon_filter) {
            {
                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                ViewGroup vView = (ViewGroup) getLayoutInflater().inflate(R.layout.ad_filter, null);

                builder.setPositiveButton(R.string.act_icon_filter, (dialog, which) -> {

                    if (db == null)
                        db = new GMusicDB(this);

                    int x = ((RadioGroup) vView.findViewById(R.id.radioGroup1)).getCheckedRadioButtonId();
                    int y = ((RadioGroup) vView.findViewById(R.id.radioGroup2)).getCheckedRadioButtonId();

                    if (x == R.id.radio_title) {
                        sort = GMusicDB.Sort.title;
                    } else if (x == R.id.radio_artist) {
                        sort = GMusicDB.Sort.artist;
                    } else if (x == R.id.radio_album) {
                        sort = GMusicDB.Sort.album;
                    } else if (x == R.id.radio_genre) {
                        sort = GMusicDB.Sort.genre;
                    } else
                        sort = null;

                    if (y == R.id.radio_all) {
                        sortOnline = GMusicDB.SortOnline.all;
                    } else if (y == R.id.radio_online) {
                        sortOnline = GMusicDB.SortOnline.online;
                    } else if (y == R.id.radio_offline) {
                        sortOnline = GMusicDB.SortOnline.offline;
                    } else
                        sortOnline = null;

                    ConvertList.clear();
                    ConvertList.addAll(db.getMusicItems(sort, sortOnline, ((EditText) vView.findViewById(R.id.filter_text)).getText().toString(), false));

                    mAdapter.notifyDataSetChanged();

                }).setView(vView).create().show();

            }
            return true;
        } else if (id == R.id.action_clean_db) {
            deleteDatabase(GMusicDB.DATABASE_NAME);
            recreate();
            return true;
        } else if (id == R.id.action_refresh_db) {
            // TODO: UpdateAlertDialog
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
        switch (requestCode) {
            case (LOGIN_ACTIVITY): {
                if (resultCode == RESULT_OK) {
                    (getSharedPreferences(getString(R.string.preferences_user), Context.MODE_PRIVATE).edit())
                            .putString(getString(R.string.token), data.getStringExtra(getString(R.string.token))).apply();
                    recreate();
                } else if (resultCode == RESULT_CANCELED) {
                    finish();
                }
            }
            break;
        }
    }

    private void refreshDB() {
        deleteDatabase(GMusicDB.DATABASE_NAME);
        SharedPreferences mPresets = getSharedPreferences(getString(R.string.preferences_user), Context.MODE_PRIVATE);
        new GMusicNet(this).execute(mPresets.getString(getString(R.string.token), ""));
    }

    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_all) {
            Toast.makeText(this, "NULL", Toast.LENGTH_SHORT).show();
        } else if (id == R.id.nav_download) {
            // TODO: DownloadActivity
            Toast.makeText(this, "NULL", Toast.LENGTH_SHORT).show();
        } else if (id == R.id.nav_offline) {
            // TODO: OfflineActivity
            Toast.makeText(this, "NULL", Toast.LENGTH_SHORT).show();
        } else if (id == R.id.nav_playlist) {
            // TODO: PlaylistActivity
            Toast.makeText(this, "NULL", Toast.LENGTH_SHORT).show();
        } else if (id == R.id.nav_settings) {
            // TODO: SettingsActivity
            Toast.makeText(this, "NULL", Toast.LENGTH_SHORT).show();
        } else if (id == R.id.nav_log) {
            // TODO: LogActivity
            Toast.makeText(this, "NULL", Toast.LENGTH_SHORT).show();
            mState = true;
            supportInvalidateOptionsMenu();
        } else if (id == R.id.nav_info) {
            // TODO: InfoActivity
            Toast.makeText(this, "NULL", Toast.LENGTH_SHORT).show();
            mState = false;
            supportInvalidateOptionsMenu();
        }

        drawer.closeDrawer(GravityCompat.START);
        return true;
    }


}
