package com.d3coding.gmusicapi.items;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;

import com.d3coding.gmusicapi.GmusicFile;
import com.d3coding.gmusicapi.R;

import java.io.File;
import java.util.List;

public class MusicAdapter extends RecyclerView.Adapter<MusicAdapter.MyViewHolder> {

    private List<MusicItems> convertList;
    private OnItemClickListener listener;

    public MusicAdapter(List<MusicItems> value) {
        this.convertList = value;
    }

    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new MyViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.music_item, parent, false));
    }

    // Define the method that allows the parent activity or fragment to define the listener
    public void setOnItemClickListener(OnItemClickListener listener) {
        this.listener = listener;
    }

    @Override
    public void onBindViewHolder(final MyViewHolder holder, int position) {
        MusicItems Music = convertList.get(position);

        holder.title.setText(Music.getTitle());
        holder.album.setText(Music.getAlbum());
        holder.artist.setText(Music.getArtist());
        if (Music.getDownloadStatus())
            holder.download_status.setBackgroundColor(Color.rgb(0, 255, 0));
        else
            holder.download_status.setBackgroundColor(Color.rgb(255, 0, 0));
        // TODO: parseImage

        File imgFile = new File(holder.albumArt.getContext().getApplicationInfo().dataDir + "/t_cache/" + convertList.get(position).getUid() + ".png");
        if (imgFile.exists()) {
            Bitmap myBitmap = BitmapFactory.decodeFile(imgFile.getAbsolutePath());
            holder.albumArt.setImageBitmap(myBitmap);
        } else
            holder.albumArt.setImageDrawable(null);

        //
        holder.time.setText(Music.getDuration());
        holder.time.setText(Music.getDuration());

        holder.linearLayout.setOnClickListener((View view) -> {
            //creating a popup menu
            PopupMenu popup = new PopupMenu(view.getContext(), holder.linearLayout);
            //inflating menu from xml resource
            popup.inflate(R.menu.menu_opt);
            if (Music.getDownloadStatus())
                popup.getMenu().findItem(R.id.men_remove).setVisible(true);
            //adding click listener
            popup.setOnMenuItemClickListener((MenuItem item) -> {
                switch (item.getItemId()) {
                    case R.id.men_download: {

                        GmusicFile gmusicFile = new GmusicFile(view.getContext());
                        gmusicFile.addToQueue(Music.getAlbumArtUrl(), this, Music, position);

                        Toast.makeText(view.getContext(), R.string.download, Toast.LENGTH_LONG).show();

                        return true;
                    }
                    case R.id.men_remove: {


                        notifyItemChanged(position);
                        return true;
                    }
                    default:
                        return false;

                }
            });
            //displaying the popup
            popup.show();


        });

    }

    @Override
    public int getItemCount() {
        return convertList.size();
    }

    // Define the listener interface
    public interface OnItemClickListener {
        void onItemClick(View itemView, int position);
    }

    class MyViewHolder extends RecyclerView.ViewHolder {
        TextView title, artist, album, time;
        ImageView albumArt, download_status;
        LinearLayout linearLayout;


        MyViewHolder(View view) {
            super(view);

            itemView.setOnClickListener((View v) -> {
                // Triggers click upwards to the adapter on click
                if (listener != null) {
                    int position = getAdapterPosition();
                    if (position != RecyclerView.NO_POSITION) {
                        listener.onItemClick(itemView, position);
                    }
                }
            });

            title = view.findViewById(R.id.title);
            artist = view.findViewById(R.id.artist);
            album = view.findViewById(R.id.album);
            albumArt = view.findViewById(R.id.thumb);
            time = view.findViewById(R.id.time);
            download_status = view.findViewById(R.id.download_status);
            linearLayout = view.findViewById(R.id.tree_dots);
        }
    }
}
