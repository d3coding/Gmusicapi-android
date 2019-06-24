package com.d3coding.gmusicapi.ui.main;

import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AnimationUtils;
import android.view.animation.LayoutAnimationController;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.d3coding.gmusicapi.GmusicFile;
import com.d3coding.gmusicapi.Gmusicdb;
import com.d3coding.gmusicapi.R;
import com.d3coding.gmusicapi.items.MusicAdapter;
import com.d3coding.gmusicapi.items.MusicItems;

import java.util.ArrayList;
import java.util.List;

/**
 * A placeholder fragment containing a simple view.
 */
public class PlaceholderFragment extends Fragment {

    private static final String INDEX = "index";
    int index = 1;

    Gmusicdb db;
    GmusicFile gmusicFile;
    private RecyclerView recyclerView;
    private List<MusicItems> ConvertList = new ArrayList<>();
    private MusicAdapter mAdapter;

    public static PlaceholderFragment newInstance(int index) {
        PlaceholderFragment fragment = new PlaceholderFragment();
        Bundle bundle = new Bundle();
        bundle.putInt(INDEX, index);
        fragment.setArguments(bundle);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        gmusicFile = new GmusicFile(getContext());
        if (getArguments() != null)
            index = getArguments().getInt(INDEX);

    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View root = inflater.inflate(R.layout.fragment_main2, container, false);
        recyclerView = root.findViewById(R.id.music_item);

        mAdapter = new MusicAdapter(ConvertList);

        int resId = R.anim.layout_animation_fall_down;
        LayoutAnimationController animation = AnimationUtils.loadLayoutAnimation(getContext(), resId);
        recyclerView.setLayoutAnimation(animation);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        recyclerView.setAdapter(mAdapter);

        {
            if (db == null) {
                db = new Gmusicdb(getContext());
                if (index == 1)
                    ConvertList.addAll(db.getMusicItems(Gmusicdb.Sort.title, false, false));
                else if (index == 2)
                    ConvertList.addAll(db.getMusicItems(Gmusicdb.Sort.artist, false, false));
                else if (index == 3)
                    ConvertList.addAll(db.getMusicItems(Gmusicdb.Sort.album, false, false));
                else if (index == 4)
                    ConvertList.addAll(db.getMusicItems(Gmusicdb.Sort.genre, false, false));

            }

            mAdapter.notifyDataSetChanged();

        }

        mAdapter.setOnItemClickListener((view, position) -> {

            AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
            ViewGroup vView = (ViewGroup) getLayoutInflater().inflate(R.layout.music_info, null);

            ImageView imageView = vView.findViewById(R.id.info_albumArt);
            TextView textViewTitle = vView.findViewById(R.id.info_title);
            TextView textViewAlbum = vView.findViewById(R.id.info_album);
            TextView textViewArtist = vView.findViewById(R.id.info_artist);
            TextView textViewTime = vView.findViewById(R.id.info_time);

            Bitmap bitmap = gmusicFile.getBitmapThumbImage(ConvertList.get(position).getUid());
            if (bitmap == null)
                bitmap = gmusicFile.getDefaultThumb();
            imageView.setImageBitmap(bitmap);

            textViewTitle.setText(ConvertList.get(position).getTitle());
            textViewAlbum.setText(ConvertList.get(position).getAlbum());
            textViewArtist.setText(ConvertList.get(position).getArtist());
            textViewTime.setText(ConvertList.get(position).getDuration());

            builder.setView(vView).setPositiveButton(getString(R.string.box_ok), null);
            final AlertDialog alert = builder.create();
            alert.setOnShowListener((DialogInterface dialog) -> (alert.getButton(AlertDialog.BUTTON_POSITIVE)).setOnClickListener((View v) -> alert.cancel()));
            alert.show();

        });


        mAdapter.setOnItemLongClickListener((view, position) -> {
            AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
            ViewGroup vView = (ViewGroup) getLayoutInflater().inflate(R.layout.music_opt, null);

            ImageView imageView = vView.findViewById(R.id.opt_album_art);
            TextView textViewTitle = vView.findViewById(R.id.opt_title);
            TextView textViewArtist = vView.findViewById(R.id.opt_artist);

            LinearLayout linearLayoutComplete = vView.findViewById(R.id.status_complete);
            LinearLayout linearLayoutDownloading = vView.findViewById(R.id.status_downloading);

            Bitmap bitmap = gmusicFile.getBitmapThumbImage(ConvertList.get(position).getUid());
            if (bitmap == null)
                bitmap = gmusicFile.getDefaultThumb();
            imageView.setImageBitmap(bitmap);

            textViewTitle.setText(ConvertList.get(position).getTitle());
            textViewArtist.setText(ConvertList.get(position).getArtist());

            if (ConvertList.get(position).getDownloadStatus()) {
                linearLayoutComplete.setVisibility(View.VISIBLE);
                linearLayoutDownloading.setVisibility(View.GONE);
            } else {
                linearLayoutComplete.setVisibility(View.GONE);
                linearLayoutDownloading.setVisibility(View.VISIBLE);

                gmusicFile.addToQueue(ConvertList.get(position).getUid(), linearLayoutComplete, linearLayoutDownloading);

            }

            builder.setView(vView).setPositiveButton(getString(R.string.box_ok), null);
            final AlertDialog alert = builder.create();
            alert.setOnShowListener((DialogInterface dialog) -> (alert.getButton(AlertDialog.BUTTON_POSITIVE)).setOnClickListener((View v) -> alert.cancel()));
            alert.show();

        });

        return root;
    }


}