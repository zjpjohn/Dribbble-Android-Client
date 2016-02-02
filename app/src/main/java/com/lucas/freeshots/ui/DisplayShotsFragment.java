package com.lucas.freeshots.ui;

import android.app.Activity;
import android.graphics.Rect;
import android.os.Bundle;
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
import android.widget.Toast;

import com.lucas.freeshots.R;
import com.lucas.freeshots.ShotAdapter;
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
        Observable<List<Shot>> get(int page);
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
    }

    //private static final int INVALID_PAGE = 0;
    private int currPage = 0;

    /**
     * 加载第一页shots
     */
    private void loadFirstPage() {
        if(source != null && !isLoading) {
            isLoading = true;
            shots.clear();
            currPage = 1;
            source.get(currPage).subscribe(new ShotsReceivedSubscriber());
        }
    }

    /**
     * 加载下一页shots
     */
    private void loadNextPage() {
        if(source != null && !isLoading) {
            isLoading = true;
            source.get(++currPage).subscribe(new ShotsReceivedSubscriber());
        }
    }

    private class ShotsReceivedSubscriber extends Subscriber<List<Shot>> {

        @Override
        public void onCompleted() {
            Timber.e("%s: RecentFragment Completed!", logTag);
            // adapter.notifyItemInserted();
            adapter.notifyDataSetChanged();
            isLoading = false;
            //loadingMore.setVisibility(View.GONE);
            if(refreshLayout != null && refreshLayout.isRefreshing()) {
                refreshLayout.setRefreshing(false);
            }
        }

        @Override
        public void onError(Throwable e) {

            // TODO: 如果是超时的话，怎么处理，是不是要重启下载！！！！！！！！！！！！！

            Timber.e("%s: listShots Failure: %s", logTag, e.getMessage());
            Toast.makeText(activity, e.getMessage(), Toast.LENGTH_LONG).show();  ////////////////////////
            isLoading = false;
            //loadingMore.setVisibility(View.GONE);
            if(refreshLayout != null && refreshLayout.isRefreshing()) {
                refreshLayout.setRefreshing(false);
            }
        }

        @Override
        public void onNext(List<Shot> newShots) {
            Timber.e("%s: FFFFFFFFFFFFFFFFFFFFFF       %d", logTag, newShots.size());
            if(newShots.size() == 0) {
                // TODO: 无数据，这时要隐藏底部的"loading more..."
                return;
            }

            for(Shot shot : newShots) {
                if(!shots.contains(shot)) {
                    shots.add(shot);
                }
            }
            //shots.addAll(newShots);
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
}
