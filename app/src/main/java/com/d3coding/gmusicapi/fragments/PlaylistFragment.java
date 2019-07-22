package com.d3coding.gmusicapi.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AnimationUtils;
import android.view.animation.LayoutAnimationController;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.d3coding.gmusicapi.R;
import com.d3coding.gmusicapi.gmusic.Database;
import com.d3coding.gmusicapi.gmusic.Download;
import com.d3coding.gmusicapi.gmusic.Network;
import com.d3coding.gmusicapi.items.PlaylistAdapter;
import com.d3coding.gmusicapi.items.PlaylistItem;

import java.util.ArrayList;
import java.util.List;

public class PlaylistFragment extends Fragment {

    OnClickListener mOnClickListener;
    private Database db;
    private String filterText = "";
    private List<PlaylistItem> ConvertList = new ArrayList<>();
    private PlaylistAdapter mAdapter;
    private RecyclerView recyclerView;
    private Download mDownload;
    private Network mNetwork;

    public void setOnClickListener(OnClickListener mOnClickListener) {
        this.mOnClickListener = mOnClickListener;
    }

    @Override
    public void onViewCreated(@NonNull View layoutView, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(layoutView, savedInstanceState);

        recyclerView = layoutView.findViewById(R.id.items);
        mDownload = new Download(getContext());
        mNetwork = new Network(getContext());
        mAdapter = new PlaylistAdapter(ConvertList);

        LayoutAnimationController animation = AnimationUtils.loadLayoutAnimation(getContext(), R.anim.layout_animation_fall_down);
        recyclerView.setLayoutAnimation(animation);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        recyclerView.setAdapter(mAdapter);

        updateList();

        mAdapter.setOnItemClickListener((view, position) -> new Thread(() -> mOnClickListener.OnClickListener(ConvertList.get(position).getUUID())).start());

    }

    void updateList() {
        if (db == null)
            db = new Database(getContext());

        ConvertList.clear();
        ConvertList.addAll(db.getPlaylists());
        mAdapter.notifyDataSetChanged();

        recyclerView.startLayoutAnimation();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_home, container, false);
    }

    public interface OnClickListener {
        void OnClickListener(String name);
    }

}
