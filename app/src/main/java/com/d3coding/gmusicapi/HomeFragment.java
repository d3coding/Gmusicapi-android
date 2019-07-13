package com.d3coding.gmusicapi;

import android.app.Activity;
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
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Spinner;
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
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;

public class HomeFragment extends Fragment {

    private static final int DOWNLOADS_NUM = 5;

    private GMusicDB db;

    private int sort = 0;
    private int sortOnline = 0;

    private String filterText = "";
    private boolean desc;


    private List<MusicItem> ConvertList = new ArrayList<>();
    private MusicAdapter mAdapter;

    private RecyclerView recyclerView;

    private GMusicFile gmusicFile;

    public OnReachListEndListener onReachListEndListener;
    private ThreadPoolExecutor downloadQueueService;

    void setOnReachListEndListener(OnReachListEndListener mOnReachListEndListener) {
        this.onReachListEndListener = mOnReachListEndListener;
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

        this.onReachListEndListener.OnReachListEnd(recyclerView);

        updateList();

        new Thread(() -> {
            Log.i("Checkup", "Started");
            List<String> online = new ArrayList<>();
            for (MusicItem x : ConvertList)
                if (gmusicFile.scan(x.getUUID()))
                    db.insertUUIDbyDownloads(x.getUUID());
                else
                    online.add(x.getUUID());

            // TODO: removeOnlineUUIDFromDB
            Log.i("Checkup", "Finished");
        }).start();


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

                initThreads();

                downloadQueueService.execute(() -> {
                    if (gmusicFile.getQueue(musicItem.getUUID()) != 0) {
                        synchronized (this) {
                            ((Activity) getContext()).runOnUiThread(() -> {
                                linearComplete.setVisibility(View.VISIBLE);
                                linearDownloading.setVisibility(View.GONE);
                                mAdapter.notifyItemChanged(position);
                            });
                        }
                    } else
                        Log.e("DownloadQueueThread", "Finished with error");
                });

                // TODO: getDownloadStatus
                if (gmusicFile.getQueue(musicItem.getUUID()) != 0)
                    synchronized (this) {
                        ((Activity) getContext()).runOnUiThread(() -> {
                            linearComplete.setVisibility(View.VISIBLE);
                            linearDownloading.setVisibility(View.GONE);
                            mAdapter.notifyItemChanged(position);
                        });
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

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.frag_items, container, false);
    }

    void initThreads() {
        if (downloadQueueService == null)
            downloadQueueService = (ThreadPoolExecutor) Executors.newFixedThreadPool(DOWNLOADS_NUM);
    }

    void downloadFilter() {
        initThreads();

        for (MusicItem musicItem : ConvertList)
            downloadQueueService.execute(() -> gmusicFile.getQueue(musicItem.getUUID()));
    }

    int getNumItems() {
        return ConvertList.size();
    }

    void showFilter() {
        {
            ViewGroup vView = (ViewGroup) getLayoutInflater().inflate(R.layout.ad_filter, null);
            Spinner spinner_organize = vView.findViewById(R.id.filter_organize);
            Spinner spinner_filter = vView.findViewById(R.id.filter_filter);
            ArrayAdapter<CharSequence> adapter_organize = ArrayAdapter.createFromResource(getContext(), R.array.organize_by, android.R.layout.simple_spinner_item);
            ArrayAdapter<CharSequence> adapter_filter = ArrayAdapter.createFromResource(getContext(), R.array.filter_by, android.R.layout.simple_spinner_item);
            adapter_organize.setDropDownViewResource(R.layout.spinner_simple_text_box);
            adapter_filter.setDropDownViewResource(R.layout.spinner_simple_text_box);
            spinner_organize.setAdapter(adapter_organize);
            spinner_filter.setAdapter(adapter_filter);
            spinner_organize.setSelection(sort);
            spinner_filter.setSelection(sortOnline);
            new AlertDialog.Builder(getContext(), R.style.AppTheme_AlertDialog).setPositiveButton(R.string.act_icon_filter, (dialog, which) -> {
                sort = spinner_organize.getSelectedItemPosition();
                sortOnline = spinner_filter.getSelectedItemPosition();
                updateList();
            }).setView(vView).create().show();

        }

    }

    void filter(String filterText) {
        this.filterText = filterText;
        updateList();
    }

    public interface OnReachListEndListener {
        void OnReachListEnd(RecyclerView recyclerView);
    }

    void updateList() {
        if (db == null)
            db = new GMusicDB(getContext());

        ConvertList.clear();
        ConvertList.addAll(db.getMusicItems(sort, sortOnline, filterText, desc));
        mAdapter.notifyDataSetChanged();
    }

}
