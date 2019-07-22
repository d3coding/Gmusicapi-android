package com.d3coding.gmusicapi.items;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.d3coding.gmusicapi.R;
import com.d3coding.gmusicapi.gmusic.Download;

import java.util.List;

public class PlaylistAdapter extends RecyclerView.Adapter<PlaylistAdapter.MyViewHolder> {

    private List<PlaylistItem> convertList;
    private Download mDownload;
    private OnItemClickListener clickListener;

    public PlaylistAdapter(List<PlaylistItem> value) {
        this.convertList = value;
    }

    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        mDownload = new Download(parent.getContext());
        return new MyViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.rec_playlist_item, parent, false));
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        this.clickListener = listener;
    }


    @Override
    public void onBindViewHolder(final MyViewHolder holder, int position) {
        PlaylistItem playlistItem = convertList.get(position);
        holder.title.setText(playlistItem.getTitle());
    }

    @Override
    public int getItemCount() {
        return convertList.size();
    }

    public interface OnItemClickListener {
        void onItemClick(View itemView, int position);
    }


    class MyViewHolder extends RecyclerView.ViewHolder {
        TextView title;

        MyViewHolder(View view) {
            super(view);

            itemView.setOnClickListener((v) -> {
                if (clickListener != null) {
                    int position = getAdapterPosition();
                    if (position != RecyclerView.NO_POSITION)
                        clickListener.onItemClick(itemView, position);

                }
            });

            title = view.findViewById(R.id.playlist_name);
        }
    }


}
