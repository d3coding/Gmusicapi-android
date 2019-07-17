package com.d3coding.gmusicapi.items;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.d3coding.gmusicapi.R;
import com.d3coding.gmusicapi.gmusic.Database;
import com.d3coding.gmusicapi.gmusic.Download;

import java.util.List;

public class MusicAdapter extends RecyclerView.Adapter<MusicAdapter.MyViewHolder> {

    private List<MusicItem> convertList;
    private Download mDownload;
    private OnItemClickListener clickListener;
    private OnItemLongClickListener longListener;

    public MusicAdapter(List<MusicItem> value) {
        this.convertList = value;
    }

    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        mDownload = new Download(parent.getContext());
        return new MyViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.rec_music_item, parent, false));
    }

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
        holder.albumArt.setImageBitmap(null);

        if (new Database(holder.title.getContext()).countDownloadsByUUID(musicItems.getUUID()) > 0)
            holder.status.setCardBackgroundColor(Color.rgb(0, 192, 0));
        else
            holder.status.setCardBackgroundColor(Color.rgb(192, 0, 0));


        new Thread(() -> {
            Bitmap bitmap = mDownload.getThumbBitmap(musicItems.getUUID());
            synchronized (this) {
                ((Activity) holder.title.getContext()).runOnUiThread(() -> holder.albumArt.setImageBitmap(bitmap));
            }
        }).start();

    }

    @Override
    public int getItemCount() {
        return convertList.size();
    }

    public interface OnItemClickListener {
        void onItemClick(View itemView, int position);
    }

    public interface OnItemLongClickListener {
        void onItemLongClick(View itemView, int position);
    }

    class MyViewHolder extends RecyclerView.ViewHolder {
        TextView title, artist, album, time;
        CardView status;
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
            status = view.findViewById(R.id.status_card);
        }
    }


}
