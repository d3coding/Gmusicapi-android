package com.d3coding.gmusicapi;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AnimationUtils;
import android.view.animation.LayoutAnimationController;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.core.content.FileProvider;
import androidx.fragment.app.Fragment;
import androidx.palette.graphics.Palette;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.d3coding.gmusicapi.items.MusicAdapter;
import com.d3coding.gmusicapi.items.MusicItem;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public class HomeFragment extends Fragment {

    private static final int DOWNLOADS_NUM = 5;

    private GMusicDB db;

    private GMusicDB.column sort = GMusicDB.column.title;
    private GMusicDB.SortOnline sortOnline = GMusicDB.SortOnline.all;
    private String filterText = "";
    private boolean desc;


    private List<MusicItem> ConvertList = new ArrayList<>();
    private MusicAdapter mAdapter;

    private RecyclerView recyclerView;

    private GMusicFile gmusicFile;

    private ExecutorService downloadQueueService;
    private Download downloadQueue;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.frag_items, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View layoutView, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(layoutView, savedInstanceState);

        recyclerView = layoutView.findViewById(R.id.items);
        gmusicFile = new GMusicFile(getContext());
        mAdapter = new MusicAdapter(ConvertList);

        LayoutAnimationController animation = AnimationUtils.loadLayoutAnimation(getContext(), R.anim.layout_animation_fall_down);
        recyclerView.setLayoutAnimation(animation);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        recyclerView.setAdapter(mAdapter);

        // TODO: scanFilesAndUpdateDB

        updateList();

        mAdapter.setOnItemClickListener((view, position) -> {

            Bitmap bitmap = gmusicFile.getBitmapThumbImage(ConvertList.get(position).getUUID());
            if (bitmap == null)
                bitmap = gmusicFile.getDefaultThumb();

            AlertDialog.Builder builder = new AlertDialog.Builder(getContext());

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
            MusicItem musicItem = ConvertList.get(position);

            Bitmap bitmap = gmusicFile.getBitmapThumbImage(musicItem.getUUID());
            if (bitmap == null)
                bitmap = gmusicFile.getDefaultThumb();

            AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
            ViewGroup vView = (ViewGroup) getLayoutInflater().inflate(R.layout.ad_music_opt, null);

            LinearLayout linearComplete = vView.findViewById(R.id.status_complete),
                    linearDownloading = vView.findViewById(R.id.status_downloading);

            ((ImageView) vView.findViewById(R.id.opt_album_art)).setImageBitmap(bitmap);
            ((TextView) vView.findViewById(R.id.opt_title)).setText(musicItem.getTitle());
            ((TextView) vView.findViewById(R.id.opt_artist)).setText(musicItem.getArtist());

            if (db.countDownloadsByUUID(musicItem.getUUID()) > 0) {
                linearComplete.setVisibility(View.VISIBLE);
                linearDownloading.setVisibility(View.GONE);
            } else {
                // TODO: CheckNetwork
                linearComplete.setVisibility(View.GONE);
                linearDownloading.setVisibility(View.VISIBLE);

                if (downloadQueueService == null)
                    downloadQueueService = Executors.newFixedThreadPool(DOWNLOADS_NUM);
                if (downloadQueue == null)
                    downloadQueue = new Download();

                if (!downloadQueue.hasUUID(musicItem.getUUID())) {
                    downloadQueue.addQueueService(musicItem.getUUID(),
                            downloadQueueService.submit(() -> {
                                // TODO: getDownloadStatus
                                if (gmusicFile.getQueue(musicItem.getUUID()) != 0)
                                    synchronized (this) {
                                        ((Activity) getContext()).runOnUiThread(() -> {
                                            linearComplete.setVisibility(View.VISIBLE);
                                            linearDownloading.setVisibility(View.GONE);
                                            mAdapter.notifyItemChanged(position);
                                            downloadQueue.setComplete(musicItem.getUUID());
                                        });
                                    }
                            })
                    );
                } else {
                    // TODO: getStatus
                }
            }

            vView.findViewById(R.id.opt_bt_play).setOnClickListener((view2) -> {
                Uri uri = FileProvider.getUriForFile(getContext(), getContext().getPackageName() + ".fileprovider",
                        new File(new File(Environment.getExternalStorageDirectory(), "Gmusicapi"), musicItem.getUUID() + ".mp3"));

                Intent intent = new Intent(Intent.ACTION_VIEW);
                intent.setDataAndType(uri, getContext().getContentResolver().getType(uri))
                        .addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                startActivity(intent);

            });

            vView.findViewById(R.id.opt_bt_open).setOnClickListener((view2) ->
                    startActivity(new Intent(Intent.ACTION_VIEW).setDataAndType(Uri.parse(Environment.getExternalStorageDirectory() + "/Gmusicapi/"), "resource/folder")));

            vView.findViewById(R.id.opt_bt_delete).setOnClickListener((view2) -> Toast.makeText(getContext(), getString(R.string.null_description), Toast.LENGTH_SHORT).show());

            builder.setView(vView).create().show();

        });


    }

    void showFilter() {
        {
            AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
            ViewGroup vView = (ViewGroup) getLayoutInflater().inflate(R.layout.ad_filter, null);

            builder.setPositiveButton(R.string.act_icon_filter, (dialog, which) -> {

                int x = ((RadioGroup) vView.findViewById(R.id.radioGroup1)).getCheckedRadioButtonId();
                int y = ((RadioGroup) vView.findViewById(R.id.radioGroup2)).getCheckedRadioButtonId();

                if (x == R.id.radio_title)
                    sort = GMusicDB.column.title;
                else if (x == R.id.radio_artist)
                    sort = GMusicDB.column.artist;
                else if (x == R.id.radio_album)
                    sort = GMusicDB.column.album;
                else if (x == R.id.radio_genre)
                    sort = GMusicDB.column.genre;
                else
                    sort = null;

                if (y == R.id.radio_all)
                    sortOnline = GMusicDB.SortOnline.all;
                else if (y == R.id.radio_online)
                    sortOnline = GMusicDB.SortOnline.online;
                else if (y == R.id.radio_offline)
                    sortOnline = GMusicDB.SortOnline.offline;
                else
                    sortOnline = null;

                updateList();

            }).setView(vView).create().show();

        }

    }

    void filter(String filterText) {
        this.filterText = filterText;
        updateList();
    }

    void updateList() {
        if (db == null)
            db = new GMusicDB(getContext());

        ConvertList.clear();
        ConvertList.addAll(db.getMusicItems(sort, sortOnline, filterText, desc));
        mAdapter.notifyDataSetChanged();
    }

    class Download {
        private List<String> UUID;
        private List<Future> future;

        Download() {
            this.UUID = new ArrayList<>();
            this.future = new ArrayList<>();
        }

        int addQueueService(String UUID, Future future) {
            this.UUID.add(UUID);
            this.future.add(future);
            return this.UUID.size() - 1;
        }

        boolean hasUUID(String UUID) {
            for (String uuid : this.UUID)
                if (UUID.equals(uuid))
                    return true;
            return false;
        }

        void setComplete(String UUID) {
            for (int x = 0; x < this.UUID.size(); ++x)
                if (this.UUID.get(x).equals(UUID)) {
                    this.UUID.remove(x);
                    this.future.remove(x);
                    break;
                }
        }

        public String getUUID(int position) {
            return UUID.get(position);
        }

        public Future getFuture(int position) {
            return future.get(position);
        }

    }

}
