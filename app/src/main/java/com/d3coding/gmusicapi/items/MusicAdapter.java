package com.d3coding.gmusicapi.items;

import android.app.Activity;
import android.graphics.Color;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.d3coding.gmusicapi.GMusicFile;
import com.d3coding.gmusicapi.R;

import java.io.File;
import java.util.List;

public class MusicAdapter extends RecyclerView.Adapter<MusicAdapter.MyViewHolder> {

    private List<MusicItem> convertList;
    private GMusicFile gmusicFile;
    private OnItemClickListener clickListener;
    private OnItemLongClickListener longListener;

    public MusicAdapter(List<MusicItem> value) {
        this.convertList = value;
    }

    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        gmusicFile = new GMusicFile(parent.getContext());
        return new MyViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.rec_music_item, parent, false));
    }

    // Define the method that allows the parent activity or fragment to define the listener
    public void setOnItemClickListener(OnItemClickListener listener) {
        this.clickListener = listener;
    }


    public void setOnItemLongClickListener(OnItemLongClickListener listener) {
        this.longListener = listener;
    }

    @Override
    public void onBindViewHolder(final MyViewHolder holder, int position) {
        MusicItem musicItems = convertList.get(position);

        holder.title.setText(musicItems.getTitle());
        holder.album.setText(musicItems.getAlbum());
        holder.artist.setText(musicItems.getArtist());
        holder.time.setText(musicItems.getDuration());

        if (musicItems.getDownloadStatus()) {
            holder.status.setText(R.string.radio_offline);
            holder.status.setTextColor(Color.rgb(0, 192, 0));
        } else {
            holder.status.setText(R.string.radio_online);
            holder.status.setTextColor(Color.rgb(192, 0, 0));
        }

        if (new File(gmusicFile.getPathJPG(musicItems.getUUID())).exists())
            holder.albumArt.setImageBitmap(gmusicFile.getBitmapThumbImage(musicItems.getUUID()));
        else {
            if (musicItems.getAlbumArtUrl().equals(""))
                holder.albumArt.setImageBitmap(gmusicFile.getDefaultThumb());
            else {
                new Thread(() -> {

                    if (gmusicFile.tryDownloadThumb(musicItems.getUUID(), musicItems.getAlbumArtUrl()) == 2)
                        musicItems.removeURL();

                    synchronized (this) {
                        ((Activity) holder.title.getContext()).runOnUiThread(() -> notifyItemChanged(position));
                    }

                }).start();
            }
        }

    }


    @Override
    public int getItemCount() {
        return convertList.size();
    }

    // Define the listener interface
    public interface OnItemClickListener {
        void onItemClick(View itemView, int position);
    }

    public interface OnItemLongClickListener {
        void onItemLongClick(View itemView, int position);
    }

    class MyViewHolder extends RecyclerView.ViewHolder {
        TextView title, artist, album, time, status;
        ImageView albumArt;

        MyViewHolder(View view) {
            super(view);

            itemView.setOnClickListener((v) -> {
                if (clickListener != null) {
                    int position = getAdapterPosition();
                    if (position != RecyclerView.NO_POSITION)
                        clickListener.onItemClick(itemView, position);

                }
            });

            itemView.setOnLongClickListener((v) -> {
                if (longListener != null) {
                    int position = getAdapterPosition();
                    if (position != RecyclerView.NO_POSITION)
                        longListener.onItemLongClick(itemView, position);

                }
                return true;
            });

            title = view.findViewById(R.id.title);
            artist = view.findViewById(R.id.artist);
            album = view.findViewById(R.id.album);
            albumArt = view.findViewById(R.id.thumb);
            time = view.findViewById(R.id.time);
            status = view.findViewById(R.id.status);
        }
    }


}
