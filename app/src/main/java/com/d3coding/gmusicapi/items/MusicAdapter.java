package com.d3coding.gmusicapi.items;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.Environment;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupMenu;
import android.widget.TextView;

import com.d3coding.gmusicapi.Gmusicdb;
import com.d3coding.gmusicapi.R;
import com.github.felixgail.gplaymusic.api.GPlayMusic;
import com.github.felixgail.gplaymusic.api.TrackApi;
import com.github.felixgail.gplaymusic.model.enums.StreamQuality;
import com.github.felixgail.gplaymusic.util.TokenProvider;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.file.Paths;
import java.util.List;

import svarzee.gps.gpsoauth.AuthToken;

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

        File imgFile = new File(Environment.getExternalStorageDirectory().getPath() + "/Gmusicapi/Thumb/" + Music.getUid() + ".png");
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
                        new Thread(() -> {

                            String MUSIC_PATH = Environment.getExternalStorageDirectory().toString() + "/Gmusicapi/Music/";
                            String THUMB_PATH = Environment.getExternalStorageDirectory().toString() + "/Gmusicapi/Thumb/";

                            try {

                                System.out.println("downloading");
                                SharedPreferences mPresets = view.getContext().getSharedPreferences(view.getContext().getString(R.string.preferences_user), Context.MODE_PRIVATE);
                                AuthToken authToken = TokenProvider.provideToken(mPresets.getString(view.getContext().getString(R.string.token), ""));
                                GPlayMusic api = new GPlayMusic.Builder().setAuthToken(authToken).build();
                                TrackApi trackApi = api.getTrackApi();
                                trackApi.getTrack(Music.getUid()).download(StreamQuality.HIGH, Paths.get(MUSIC_PATH + Music.getUid() + ".mp3"));

                                URL url = new URL(Music.getAlbumArtUrl());
                                HttpURLConnection c = (HttpURLConnection) url.openConnection();
                                c.setRequestMethod("GET");
                                c.setDoOutput(true);
                                c.connect();

                                Log.v("LOG_TAG", "PATH: " + THUMB_PATH);

                                File file = new File(THUMB_PATH);
                                file.mkdirs();
                                File outputFile = new File(file, Music.getUid() + ".png");
                                FileOutputStream fos = new FileOutputStream(outputFile);
                                InputStream is = c.getInputStream();

                                byte[] buffer = new byte[4096];
                                int len1;

                                while ((len1 = is.read(buffer)) != -1)
                                    fos.write(buffer, 0, len1);


                                fos.close();
                                is.close();

                                System.out.println(" A new file is downloaded successfully");

                                (new Gmusicdb(view.getContext())).updateDB(Music.getUid());

                                System.out.println(" Updating database ");

                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }).start();

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
