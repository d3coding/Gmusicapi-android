package com.d3coding.gmusicapi.ui.main;

import android.content.DialogInterface;
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
import android.widget.TextView;

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
                    ConvertList.addAll(db.getMusicItemsTitle());
                else if (index == 2)
                    ConvertList.addAll(db.getMusicItemsArtist());
                else if (index == 3)
                    ConvertList.addAll(db.getMusicItemsAlbum());
                else if (index == 4)
                    ConvertList.addAll(db.getMusicItemsGenre());

            }

            mAdapter.notifyDataSetChanged();

        }

        mAdapter.setOnItemClickListener((View view, int position) -> {

            AlertDialog.Builder builder = new AlertDialog.Builder(getContext());

            ViewGroup vView = (ViewGroup) getLayoutInflater().inflate(R.layout.music_info, null);

            ImageView imageView = vView.findViewById(R.id.info_albumArt);
            TextView textViewTitle = vView.findViewById(R.id.info_title);
            TextView textViewAlbum = vView.findViewById(R.id.info_album);
            TextView textViewArtist = vView.findViewById(R.id.info_artist);
            TextView textViewTime = vView.findViewById(R.id.info_time);

            imageView.setImageResource(R.drawable.vr);

            textViewTitle.setText(ConvertList.get(position).getTitle());
            textViewAlbum.setText(ConvertList.get(position).getAlbum());
            textViewArtist.setText(ConvertList.get(position).getArtist());
            textViewTime.setText(ConvertList.get(position).getTime());

            builder.setView(vView).setPositiveButton(getString(R.string.box_ok), null);

            final AlertDialog alert = builder.create();

            alert.setOnShowListener((DialogInterface dialog) -> (alert.getButton(AlertDialog.BUTTON_POSITIVE)).setOnClickListener((View v) -> alert.cancel()));

            alert.show();


        });

        return root;
    }
}