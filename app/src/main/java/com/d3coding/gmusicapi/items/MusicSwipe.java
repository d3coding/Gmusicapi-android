package com.d3coding.gmusicapi.items;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.RecyclerView;

import com.d3coding.gmusicapi.R;

public class MusicSwipe extends ItemTouchHelper.Callback {
    private MusicAdapter mAdapter;

    private Drawable icon_download;
    private Drawable icon_play;
    private ColorDrawable background;

    @Override
    public int getMovementFlags(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder) {
        return makeFlag(ItemTouchHelper.ACTION_STATE_SWIPE, ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT);
    }

    public MusicSwipe(MusicAdapter adapter, Context context) {
        mAdapter = adapter;
        icon_play = ContextCompat.getDrawable(context, R.drawable.ic_play);
        icon_download = ContextCompat.getDrawable(context, R.drawable.ic_download);
        background = new ColorDrawable(Color.WHITE);
    }

    @Override
    public void onSwiped(RecyclerView.ViewHolder viewHolder, int direction) {
        int position = viewHolder.getAdapterPosition();

        if (direction == ItemTouchHelper.LEFT)
            mAdapter.downloadItem.OnDownloadItem(mAdapter.getItemUUID(position));
        else if (direction == ItemTouchHelper.RIGHT)
            mAdapter.playItem.OnPlayItem(mAdapter.getItemUUID(position));

        mAdapter.notifyItemChanged(position);
    }

    @Override
    public void onChildDraw(@NonNull Canvas c, @NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, float dX, float dY, int actionState, boolean isCurrentlyActive) {
        super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive);
        View itemView = viewHolder.itemView;
        int backgroundCornerOffset = 20;

        CardView cardView = itemView.findViewById(R.id.card_view);

        cardView.setRadius(4);
        cardView.setCardElevation(4);

        if (dX > 0) { // Swiping to the right
            int iconMargin = (itemView.getHeight() - icon_play.getIntrinsicHeight()) / 2;
            int iconTop = itemView.getTop() + (itemView.getHeight() - icon_play.getIntrinsicHeight()) / 2;
            int iconBottom = iconTop + icon_play.getIntrinsicHeight();

            int iconRight = itemView.getLeft() + iconMargin + icon_play.getIntrinsicWidth();
            int iconLeft = itemView.getLeft() + iconMargin;
            icon_play.setBounds(iconLeft, iconTop, iconRight, iconBottom);

            background.setBounds(itemView.getLeft(), itemView.getTop(), itemView.getLeft() + ((int) dX) + backgroundCornerOffset, itemView.getBottom());
            background.setColor(Color.rgb(255, 204, 51));
            background.draw(c);
            icon_play.draw(c);
        } else if (dX < 0) { // Swiping to the left
            int iconMargin = (itemView.getHeight() - icon_download.getIntrinsicHeight()) / 2;
            int iconTop = itemView.getTop() + (itemView.getHeight() - icon_download.getIntrinsicHeight()) / 2;
            int iconBottom = iconTop + icon_download.getIntrinsicHeight();

            int iconLeft = itemView.getRight() - iconMargin - icon_download.getIntrinsicWidth();
            int iconRight = itemView.getRight() - iconMargin;
            icon_download.setBounds(iconLeft, iconTop, iconRight, iconBottom);

            background.setBounds(itemView.getRight() + ((int) dX) - backgroundCornerOffset, itemView.getTop(), itemView.getRight(), itemView.getBottom());
            background.setColor(Color.rgb(165, 123, 187));
            background.draw(c);
            icon_download.draw(c);
        }
        if (dX == 0) {
            cardView.setRadius(0);
            cardView.setCardElevation(0);
            // view is unSwiped
            background.setBounds(0, 0, 0, 0);
            background.draw(c);
        }

    }

    @Override
    public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, @NonNull RecyclerView.ViewHolder target) {
        return false;
    }
}