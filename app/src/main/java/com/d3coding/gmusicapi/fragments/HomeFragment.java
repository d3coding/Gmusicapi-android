package com.d3coding.gmusicapi.fragments;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AnimationUtils;
import android.view.animation.LayoutAnimationController;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.core.content.FileProvider;
import androidx.fragment.app.Fragment;
import androidx.palette.graphics.Palette;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.d3coding.gmusicapi.Config;
import com.d3coding.gmusicapi.R;
import com.d3coding.gmusicapi.gmusic.Database;
import com.d3coding.gmusicapi.gmusic.Download;
import com.d3coding.gmusicapi.gmusic.Network;
import com.d3coding.gmusicapi.items.MusicAdapter;
import com.d3coding.gmusicapi.items.MusicItem;
import com.d3coding.gmusicapi.items.MusicSwipe;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;

public class HomeFragment extends Fragment {

    private Database db;

    private int sort = 0;
    private int sortOnline = 0;
    private boolean desc = false;

    private String filterText = "";

    private List<MusicItem> ConvertList = new ArrayList<>();
    private MusicAdapter mAdapter;

    private RecyclerView recyclerView;

    private Download mDownload;
    private Network mNetwork;

    private RecyclerView.OnScrollListener mOnScrollListener;

    private ThreadPoolExecutor downloadQueueService;

    public void addOnScrollListener(RecyclerView.OnScrollListener mOnScrollListener) {
        this.mOnScrollListener = mOnScrollListener;
        if (recyclerView != null)
            recyclerView.addOnScrollListener(mOnScrollListener);
    }

    @Override
    public void onViewCreated(@NonNull View layoutView, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(layoutView, savedInstanceState);

        recyclerView = layoutView.findViewById(R.id.items);
        mDownload = new Download(getContext());
        mNetwork = new Network(getContext());
        mAdapter = new MusicAdapter(ConvertList);

        mAdapter.setOnDownloadItem((UUID) -> {
            if (mDownload.scan(UUID))
                Toast.makeText(getContext(), "Music already downloaded", Toast.LENGTH_SHORT).show();
            else
                downloadQueueService.execute(() -> mDownload.getQueue(UUID));
        });

        mAdapter.setOnPlayItem((UUID) -> {
            if (!openFile(UUID))
                Toast.makeText(getContext(), "Music isn't downloaded", Toast.LENGTH_SHORT).show();
        });

        LayoutAnimationController animation = AnimationUtils.loadLayoutAnimation(getContext(), R.anim.layout_animation_fall_down);
        recyclerView.setLayoutAnimation(animation);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        recyclerView.setAdapter(mAdapter);
        (new ItemTouchHelper(new MusicSwipe(mAdapter, getContext()))).attachToRecyclerView(recyclerView);

        if (mOnScrollListener != null)
            recyclerView.addOnScrollListener(mOnScrollListener);

        updateList();

        new Thread(() -> {

            //if (!mPresets.contains(getString(R.string.last_update))) {
            //                    HomeFragment homeFragment = (HomeFragment) getFragment(HomeFragment.class);
            //                    if (homeFragment != null)
            //                        homeFragment.refreshDB();
            //                }

            Log.i("Checkup", "Started");
            List<String> offline = new ArrayList<>();
            List<String> online = new ArrayList<>();
            for (MusicItem musicItem : ConvertList)
                if (mDownload.scan(musicItem.getUUID()))
                    offline.add(musicItem.getUUID());
                else
                    online.add(musicItem.getUUID());

            for (String UUID : offline)
                db.insertUUIDbyDownloads(UUID);

            // TODO: removeOnlineUUIDFromDB

            synchronized (this) {
                ((Activity) getContext()).runOnUiThread(() -> mAdapter.notifyDataSetChanged());
            }

            Log.i("Checkup", "Finished");
        }).start();

        mAdapter.setOnItemClickListener((view, position) -> {

            Bitmap bitmap = mDownload.getBitmapThumbImage(ConvertList.get(position).getUUID());
            if (bitmap == null)
                bitmap = mDownload.getDefaultThumb();

            AlertDialog.Builder builder = new AlertDialog.Builder(getContext());

            ViewGroup vView = (ViewGroup) getLayoutInflater().inflate(R.layout.alert_music_info, null);

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

    }

    public void refreshDB() {
        getContext().deleteDatabase(Database.DATABASE_NAME);
        mNetwork.execute(getContext().getSharedPreferences(getString(R.string.preferences_user), Context.MODE_PRIVATE).getString(getString(R.string.token), ""));
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_home, container, false);
    }

    boolean openFile(String UUID) {
        if (mDownload.scan(UUID)) {
            Uri uri = FileProvider.getUriForFile(getContext(), getContext().getPackageName() + ".fileprovider",
                    new File(new File(Environment.getExternalStorageDirectory(), "Gmusicapi"), UUID + ".mp3"));

            Intent intent = new Intent(Intent.ACTION_VIEW);
            intent.setDataAndType(uri, getContext().getContentResolver().getType(uri))
                    .addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            startActivity(intent);
            return true;
        } else
            return false;
    }

    void openFolder() {
        startActivity(new Intent(Intent.ACTION_VIEW).setDataAndType(Uri.parse(Environment.getExternalStorageDirectory() + "/Gmusicapi/"), "resource/folder"));
    }

    void initThreads() {
        if (downloadQueueService == null)
            downloadQueueService = (ThreadPoolExecutor) Executors.newFixedThreadPool(Config.SIMULTANEOUS_DOWNLOAD);
    }

    public void downloadFilter() {
        initThreads();

        for (MusicItem musicItem : ConvertList)
            downloadQueueService.execute(() -> mDownload.getQueue(musicItem.getUUID()));
    }

    public int getNumItems() {
        return ConvertList.size();
    }


    public void filter(String filterText) {
        this.filterText = filterText;
        updateList();
    }

    public Bundle getBundle() {
        Bundle args = new Bundle();
        args.putInt("sort", sort);
        args.putInt("sort_online", sortOnline);
        args.putBoolean("desc", desc);
        return args;
    }

    public void setBundle(Bundle bundle) {
        sort = bundle.getInt("sort", 0);
        sortOnline = bundle.getInt("sort_online", 0);
        desc = bundle.getBoolean("desc", false);
        updateList();
    }


    void updateList() {
        if (db == null)
            db = new Database(getContext());

        ConvertList.clear();
        ConvertList.addAll(db.getMusicItems(sort, sortOnline, filterText, desc));
        mAdapter.notifyDataSetChanged();

        recyclerView.startLayoutAnimation();
    }

}
