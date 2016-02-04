package com.lucas.freeshots.ui;


import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.ViewGroup;

import java.util.List;

import hugo.weaving.DebugLog;

/**
 *
 * @param <T> 本Adapter要绑定的数据源类型
 * @param <VH> ViewHolder
 */
public abstract class PullUpLoadAdapter<T, VH extends RecyclerView.ViewHolder> extends RecyclerView.Adapter<VH> {
    protected static final int VIEW_TYPE_ITEM = 1;
    protected static final int VIEW_TYPE_BOTTOM = 2;

    protected List<T> data;
    private boolean bottomItemVisible = false;

    public PullUpLoadAdapter(@NonNull List<T> data) {
        this.data = data;
    }

    /**
     * 设置“loading more”item的显隐。
     * @param visible: true 显示，false 隐藏。
     */
    protected void setBottomItemVisible(boolean visible) {
        bottomItemVisible = visible;
        notifyDataSetChanged();
    }

    @Override
    public abstract VH onCreateViewHolder(ViewGroup parent, int viewType);

    @Override
    public abstract void onBindViewHolder(VH holder, int position);

    @Override
    public int getItemViewType(int position) {
        return bottomItemVisible
                ? (position == getItemCount() - 1 ? VIEW_TYPE_BOTTOM : VIEW_TYPE_ITEM)
                : VIEW_TYPE_ITEM;
    }

    @DebugLog
    @Override
    public int getItemCount() {
        int size = data.size();
        // 加1为加上Bottom View，shots.size() == 0时没有Bottom View
        return size == 0 ? 0 : (bottomItemVisible ? size + 1 : size);
    }

    protected boolean isBottomView(int position) {
        return position == data.size();
    }
}
