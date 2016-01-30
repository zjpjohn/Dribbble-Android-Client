package com.lucas.freeshots;


import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.facebook.drawee.view.SimpleDraweeView;
import com.lucas.freeshots.model.Shot;
import com.lucas.freeshots.ui.ShowShotActivity;

import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;
import hugo.weaving.DebugLog;
import timber.log.Timber;

public class ShotAdapter extends RecyclerView.Adapter<ShotAdapter.ViewHolder> {
    private static final int VIEW_TYPE_SHOT = 1;
    private static final int VIEW_TYPE_BOTTOM = 2;

    private List<Shot> shots;

    public ShotAdapter(@NonNull List<Shot> shots) {
        this.shots = shots;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = null;
        if(viewType == VIEW_TYPE_SHOT) {
            v = LayoutInflater.from(parent.getContext()).inflate(R.layout.shot, parent, false);
        } else if(viewType == VIEW_TYPE_BOTTOM) {
            v = LayoutInflater.from(parent.getContext()).inflate(R.layout.view_loading_more, parent, false);
        } else {
            Timber.e("Unknown viewType: %d", viewType);
        }

        return new ViewHolder(v, viewType);
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, int position) {
        if(isBottomView(position)) {
            return;
        }

        final Shot shot = shots.get(position);
        String uriStr = shot.images.teaser;
        if(uriStr == null) {
            Timber.e("wwwwwwwwwwwwwwwwwwwwwwwgggggggggggggggggggww");
            return;
        }

        holder.shotDv.setImageURI(Uri.parse(uriStr));
        holder.titleTv.setText(shot.title);

        if(shot.user != null) {
            if(shot.user.avatar_url != null) {
                holder.authorIconDv.setImageURI(Uri.parse(shot.user.avatar_url));
            }
            holder.authorNameTv.setText(shot.user.name);
        }

        holder.viewsCountTv.setText(String.valueOf(shot.views_count));
        holder.commentsCountTv.setText(String.valueOf(shot.comments_count));
        holder.likesCountTv.setText(String.valueOf(shot.likes_count));

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ShowShotActivity.startMyself(holder.itemView.getContext(), shot);
            }
        });
    }

    @Override
    public int getItemViewType(int position) {
        return position == getItemCount() - 1 ? VIEW_TYPE_BOTTOM : VIEW_TYPE_SHOT;
    }

    @DebugLog
    @Override
    public int getItemCount() {
        // 加1为加上Bottom View，shots.size() == 0时没有Bottom View
        return shots.size() == 0 ? 0 : shots.size() + 1;
    }

    public boolean isBottomView(int position) {
        return position == shots.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        @Bind(R.id.shot) public SimpleDraweeView shotDv;
        @Bind(R.id.title) public TextView titleTv;
        @Bind(R.id.author_icon) public SimpleDraweeView authorIconDv;
        @Bind(R.id.author_name) public TextView authorNameTv;
        @Bind(R.id.views_count) public TextView viewsCountTv;
        @Bind(R.id.comments_count) public TextView commentsCountTv;
        @Bind(R.id.likes_count) public TextView likesCountTv;

        int viewType;

        public ViewHolder(View v, int viewType) {
            super(v);
            this.viewType = viewType;
            if(viewType == VIEW_TYPE_SHOT) {
                ButterKnife.bind(this, v);
            }
        }
    }
}
