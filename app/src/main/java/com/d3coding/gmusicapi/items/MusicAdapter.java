package com.d3coding.gmusicapi.items;

import android.graphics.Bitmap;
import android.graphics.Color;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.d3coding.gmusicapi.GmusicFile;
import com.d3coding.gmusicapi.R;

import java.util.List;

public class MusicAdapter extends RecyclerView.Adapter<MusicAdapter.MyViewHolder> {

    private List<MusicItems> convertList;
    GmusicFile gmusicFile;
    private OnItemClickListener clickListener;
    private OnItemLongClickListener longListener;

    public MusicAdapter(List<MusicItems> value) {
        this.convertList = value;
    }

    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        gmusicFile = new GmusicFile(parent.getContext());
        return new MyViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.music_item, parent, false));
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
        MusicItems musicItems = convertList.get(position);

        holder.title.setText(musicItems.getTitle());
        holder.album.setText(musicItems.getAlbum());
        holder.artist.setText(musicItems.getArtist());
        holder.time.setText(musicItems.getDuration());

        if (musicItems.getDownloadStatus())
            holder.download_status.setBackgroundColor(Color.rgb(0, 255, 0));
        else
            holder.download_status.setBackgroundColor(Color.rgb(255, 0, 0));


        Bitmap bitmap = gmusicFile.getBitmapThumbImage(musicItems.getUid());
        if (bitmap == null)
            bitmap = gmusicFile.getDefaultThumbTemp(this, position, musicItems);
        holder.albumArt.setImageBitmap(bitmap);

        // TODO: getDownloadStatus
        // this.notifyItemChanged(position);

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
        TextView title, artist, album, time;
        ImageView albumArt, download_status;

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
            download_status = view.findViewById(R.id.download_status);
        }
    }


}
