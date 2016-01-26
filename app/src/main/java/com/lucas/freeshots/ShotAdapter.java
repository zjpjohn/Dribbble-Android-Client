package com.lucas.freeshots;


import android.net.Uri;
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
    private List<Shot> shots;

    public ShotAdapter(List<Shot> shots) {
        if(shots == null) {
            return;
        }

        this.shots = shots;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        // create a new view
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.shot, parent, false);
        // set the view's size, margins, paddings and layout parameters
        //...
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, int position) {
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

    @DebugLog
    @Override
    public int getItemCount() {
        return shots.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        @Bind(R.id.shot) public SimpleDraweeView shotDv;
        @Bind(R.id.title) public TextView titleTv;
        @Bind(R.id.author_icon) public SimpleDraweeView authorIconDv;
        @Bind(R.id.author_name) public TextView authorNameTv;
        @Bind(R.id.views_count) public TextView viewsCountTv;
        @Bind(R.id.comments_count) public TextView commentsCountTv;
        @Bind(R.id.likes_count) public TextView likesCountTv;

        public ViewHolder(View v) {
            super(v);
            ButterKnife.bind(this, v);
        }
    }
}
