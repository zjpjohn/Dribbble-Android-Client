package com.lucas.freeshots.ui;

import android.app.Activity;
import android.graphics.Rect;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewCompat;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.facebook.drawee.view.SimpleDraweeView;
import com.lucas.freeshots.R;
import com.lucas.freeshots.common.Common;
import com.lucas.freeshots.model.Shot;
import com.lucas.freeshots.util.Util;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import rx.Observable;
import rx.Subscriber;
import timber.log.Timber;

import static com.lucas.freeshots.util.Util.$;

public class DisplayShotsFragment extends Fragment implements Serializable {

    public DisplayShotsFragment() {

    }

    public interface Source {
        @Nullable Observable<Shot> get(int page);
    }

    /**
     *
     * @param logTag 打印日志时的标签
     */
    public static DisplayShotsFragment newInstance(@Nullable String logTag) {
        Bundle args = new Bundle();
        args.putString("logTag", logTag != null ? logTag : "No log tag");

        DisplayShotsFragment fragment = new DisplayShotsFragment();
        fragment.setArguments(args);

        return fragment;
    }

    private Source source;
    private String logTag;

    public void setSource(Source source) {
        this.source = source;
    }

    private LinearLayout downLoadErrorLayout;
    private SwipeRefreshLayout refreshLayout;
    private RecyclerView recyclerView;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Bundle bundle = getArguments();
        logTag = bundle.getString("logTag");
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_display_shots, container, false);
        downLoadErrorLayout = $(v, R.id.download_error_layout);
        refreshLayout = $(v, R.id.refresh_layout);
        recyclerView = $(v, R.id.shots);
        return v;
    }

    private List<Shot> shots = new ArrayList<>();
    private ShotAdapter adapter;

    private boolean isLoading = false;
    private Activity activity;

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        activity = getActivity();

        int spacing = Util.dp2px(activity, 8); // 间隔为8dp

        GridLayoutManager gridLayoutManager = new GridLayoutManager(activity, 2);

        recyclerView.setLayoutManager(gridLayoutManager);
        recyclerView.setAdapter(adapter = new ShotAdapter(shots));
        recyclerView.addItemDecoration(new GridItemDecoration(spacing));
        recyclerView.setPadding(spacing / 2, spacing / 2, spacing / 2, spacing / 2);
        recyclerView.setItemAnimator(new DefaultItemAnimator());

        //设置底部View(下拉至底部loading more的View)占据整行空间
        gridLayoutManager.setSpanSizeLookup(new GridLayoutManager.SpanSizeLookup() {
            @Override
            public int getSpanSize(int position) {
                return adapter.isBottomView(position) ? gridLayoutManager.getSpanCount() : 1;
            }
        });

        recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);
            }

            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);

                // 滑到了底部则自动加载下一页shot
                if (!ViewCompat.canScrollVertically(recyclerView, 1)) {
                    loadNextPage();
                }
            }
        });

        SwipeRefreshLayout.OnRefreshListener listener = this::loadFirstPage;
        refreshLayout.setOnRefreshListener(listener);
        refreshLayout.post(() -> {
            // 自动加载首页
            listener.onRefresh();
            refreshLayout.setRefreshing(true);
        });

        downLoadErrorLayout.setOnClickListener(v -> {
            // 下载失败，点击加载首页
            listener.onRefresh();
            refreshLayout.setRefreshing(true);
            downLoadErrorLayout.setVisibility(View.GONE);
            refreshLayout.setVisibility(View.VISIBLE);
        });
    }

    private int currPage = 0;

    /**
     * 加载第一页shots
     */
    private void loadFirstPage() {
        if(source != null && !isLoading) {
            isLoading = true;
            adapter.setBottomItemVisible(true);
            shots.clear();
            currPage = 1;

            Observable<Shot> observable = source.get(currPage);
            if(observable != null) {
                observable.subscribe(new ShotsReceivedSubscriber());
            } else {
                Common.writeErrToLogAndShow(activity, "未登录");
            }
        }
    }

    /**
     * 加载下一页shots
     */
    private void loadNextPage() {
        if(source != null && !isLoading) {
            isLoading = true;
            adapter.setBottomItemVisible(true);

            Observable<Shot> observable = source.get(++currPage);
            if(observable != null) {
                observable.subscribe(new ShotsReceivedSubscriber());
            } else {
                Common.writeErrToLogAndShow(activity, "未登录");
            }
        }
    }

    private class ShotsReceivedSubscriber extends Subscriber<Shot> {

        private void over() {
            isLoading = false;
            adapter.setBottomItemVisible(false);
            if(refreshLayout != null && refreshLayout.isRefreshing()) {
                refreshLayout.setRefreshing(false);
            }
        }

        @Override
        public void onCompleted() {
            Timber.e("%s: Completed!", logTag);
            // adapter.notifyItemInserted();
            adapter.notifyDataSetChanged();
            over();
        }

        @Override
        public void onError(Throwable e) {
            Timber.e("%s: listShots Failure: %s", logTag, e.getMessage());
            downLoadErrorLayout.setVisibility(View.VISIBLE);
            refreshLayout.setVisibility(View.GONE);
            over();
        }

        @Override
        public void onNext(Shot shot) {
            //Timber.e("get a shot: " + String.valueOf(shot != null)); /////////////////////////////////////////
            if(!shots.contains(shot)) {
                shots.add(shot);
            }
        }
    }

    private static class GridItemDecoration extends RecyclerView.ItemDecoration {
        private int spacing;

        public GridItemDecoration(int spacing) {
            this.spacing = spacing;
        }

        @Override
        public void getItemOffsets(Rect outRect, View view, RecyclerView parent, RecyclerView.State state) {
            super.getItemOffsets(outRect, view, parent, state);
            outRect.set(spacing / 2, spacing / 2, spacing / 2, spacing / 2);
        }
    }

    private static class ShotAdapter extends PullUpLoadAdapter<Shot, ShotAdapter.ViewHolder> {

        public ShotAdapter(@NonNull List<Shot> data) {
            super(data);
        }

        @Override
        public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View v = null;
            if (viewType == VIEW_TYPE_ITEM) {
                v = LayoutInflater.from(parent.getContext()).inflate(R.layout.shot, parent, false);
            } else if (viewType == VIEW_TYPE_BOTTOM) {
                v = LayoutInflater.from(parent.getContext()).inflate(R.layout.view_loading_more, parent, false);
            } else {
                Timber.e("Unknown viewType: %d", viewType);
            }

            return new ViewHolder(v, viewType);
        }

        @Override
        public void onBindViewHolder(ViewHolder holder, int position) {
            if (isBottomView(position)) {
                return;
            }

            final Shot shot = data.get(position);
            if (shot.images != null) {
                String uriStr = shot.images.teaser;
                if (uriStr != null) {
                    holder.shotDv.setImageURI(Uri.parse(uriStr));
                }

                if (shot.images.getType().equalsIgnoreCase("gif")) {
                    holder.signGifIv.setVisibility(View.VISIBLE);
                } else {
                    holder.signGifIv.setVisibility(View.INVISIBLE);
                }
            }

            holder.titleTv.setText(shot.title);

            if (shot.user != null) {
                if (shot.user.avatar_url != null) {
                    holder.authorIconDv.setImageURI(Uri.parse(shot.user.avatar_url));
                }
                holder.authorNameTv.setText(shot.user.name);
            }

            holder.viewsCountTv.setText(String.valueOf(shot.views_count));
            holder.commentsCountTv.setText(String.valueOf(shot.comments_count));
            holder.likesCountTv.setText(String.valueOf(shot.likes_count));

            holder.itemView.setOnClickListener(v -> ShowShotActivity.startMyself(holder.itemView.getContext(), shot));
        }

        public static class ViewHolder extends RecyclerView.ViewHolder {
            public SimpleDraweeView shotDv;
            public ImageView signGifIv;
            public TextView titleTv;
            public SimpleDraweeView authorIconDv;
            public TextView authorNameTv;
            public TextView viewsCountTv;
            public TextView commentsCountTv;
            public TextView likesCountTv;

            int viewType;

            public ViewHolder(View v, int viewType) {
                super(v);
                this.viewType = viewType;

                if (viewType == VIEW_TYPE_ITEM) {
                    shotDv = $(v, R.id.shot);
                    signGifIv = $(v, R.id.sign_gif);
                    titleTv = $(v, R.id.title);
                    authorIconDv = $(v, R.id.author_icon);
                    authorNameTv = $(v, R.id.author_name);
                    viewsCountTv = $(v, R.id.views_count);
                    commentsCountTv = $(v, R.id.comments_count);
                    likesCountTv = $(v, R.id.likes_count);
                }
            }
        }
    }

//    private static class ShotAdapter extends RecyclerView.Adapter<ShotAdapter.ViewHolder> {
//        private static final int VIEW_TYPE_SHOT = 1;
//        private static final int VIEW_TYPE_BOTTOM = 2;
//
//        private List<Shot> shots;
//        private boolean bottomItemVisible = false;
//
//        public ShotAdapter(@NonNull List<Shot> shots) {
//            this.shots = shots;
//        }
//
//        /**
//         * 设置“loading more”item的显隐。
//         * @param visible: true 显示，false 隐藏。
//         */
//        public void setBottomItemVisible(boolean visible) {
//            bottomItemVisible = visible;
//            notifyDataSetChanged();
//        }
//
//        @Override
//        public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
//            View v = null;
//            if (viewType == VIEW_TYPE_SHOT) {
//                v = LayoutInflater.from(parent.getContext()).inflate(R.layout.shot, parent, false);
//            } else if (viewType == VIEW_TYPE_BOTTOM) {
//                v = LayoutInflater.from(parent.getContext()).inflate(R.layout.view_loading_more, parent, false);
//            } else {
//                Timber.e("Unknown viewType: %d", viewType);
//            }
//
//            return new ViewHolder(v, viewType);
//        }
//
//        @Override
//        public void onBindViewHolder(final ViewHolder holder, int position) {
//            if (isBottomView(position)) {
//                return;
//            }
//
//            final Shot shot = shots.get(position);
//            if (shot.images != null) {
//                String uriStr = shot.images.teaser;
//                if (uriStr != null) {
//                    holder.shotDv.setImageURI(Uri.parse(uriStr));
//                }
//            }
//
//            holder.titleTv.setText(shot.title);
//
//            if (shot.user != null) {
//                if (shot.user.avatar_url != null) {
//                    holder.authorIconDv.setImageURI(Uri.parse(shot.user.avatar_url));
//                }
//                holder.authorNameTv.setText(shot.user.name);
//            }
//
//            holder.viewsCountTv.setText(String.valueOf(shot.views_count));
//            holder.commentsCountTv.setText(String.valueOf(shot.comments_count));
//            holder.likesCountTv.setText(String.valueOf(shot.likes_count));
//
//            holder.itemView.setOnClickListener(v -> ShowShotActivity.startMyself(holder.itemView.getContext(), shot));
//        }
//
//        @Override
//        public int getItemViewType(int position) {
//            return bottomItemVisible
//                    ? (position == getItemCount() - 1 ? VIEW_TYPE_BOTTOM : VIEW_TYPE_SHOT)
//                    : VIEW_TYPE_SHOT;
//        }
//
//        @DebugLog
//        @Override
//        public int getItemCount() {
//            int size = shots.size();
//            // 加1为加上Bottom View，shots.size() == 0时没有Bottom View
//            return size == 0 ? 0 : (bottomItemVisible ? size + 1 : size);
//        }
//
//        public boolean isBottomView(int position) {
//            return position == shots.size();
//        }
//
//        public static class ViewHolder extends RecyclerView.ViewHolder {
//            public SimpleDraweeView shotDv;
//            public TextView titleTv;
//            public SimpleDraweeView authorIconDv;
//            public TextView authorNameTv;
//            public TextView viewsCountTv;
//            public TextView commentsCountTv;
//            public TextView likesCountTv;
//
//            int viewType;
//
//            public ViewHolder(View v, int viewType) {
//                super(v);
//                this.viewType = viewType;
//
//                if (viewType == VIEW_TYPE_SHOT) {
//                    shotDv = $(v, R.id.shot);
//                    titleTv = $(v, R.id.title);
//                    authorIconDv = $(v, R.id.author_icon);
//                    authorNameTv = $(v, R.id.author_name);
//                    viewsCountTv = $(v, R.id.views_count);
//                    commentsCountTv = $(v, R.id.comments_count);
//                    likesCountTv = $(v, R.id.likes_count);
//                }
//            }
//        }
//    }
}
