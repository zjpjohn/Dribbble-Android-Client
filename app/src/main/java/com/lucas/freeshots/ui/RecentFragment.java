package com.lucas.freeshots.ui;

import android.app.Activity;
import android.graphics.Rect;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewCompat;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.lucas.freeshots.R;
import com.lucas.freeshots.ShotAdapter;
import com.lucas.freeshots.common.Dribbble;
import com.lucas.freeshots.model.Shot;
import com.lucas.freeshots.util.Util;

import java.util.ArrayList;
import java.util.List;

import rx.Subscriber;
import timber.log.Timber;

public class RecentFragment extends Fragment {

    public RecentFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_recent, container, false);
        //ButterKnife.bind(this, view);
        return view;
    }

    private List<Shot> shots = new ArrayList<>();
    private ShotAdapter adapter;

    private boolean isLoading = false;

  //  @Bind(R.id.loading_more) View loadingMore;

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        Activity activity = getActivity();

        int spacing = Util.dp2px(activity, 8); // 间隔为8dp

        RecyclerView recyclerView = (RecyclerView) activity.findViewById(R.id.recentShotRecyclerView);
        recyclerView.setLayoutManager(new GridLayoutManager(activity, 2));
        recyclerView.setAdapter(adapter = new ShotAdapter(shots));
        recyclerView.addItemDecoration(new GridItemDecoration(spacing));
        recyclerView.setPadding(spacing / 2, spacing / 2, spacing / 2, spacing / 2);
        recyclerView.setItemAnimator(new DefaultItemAnimator());

        recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);
            }

            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);

                // 滑到了底部则自动加载新shot
                if (!isLoading && !ViewCompat.canScrollVertically(recyclerView, 1)) {
                    loadShots();
                }
            }
        });

        loadShots();
    }

    private int currPage = 0;

    /**
     * 加载下一页shots
     */
    private void loadShots() {
        isLoading = true;
     //   loadingMore.setVisibility(View.VISIBLE);
        Dribbble.downloadShots(++currPage, Dribbble.SHOT_SORT_BY_RECENT).subscribe(new ShotsReceivedSubscriber());
    }

//    private void loadShotsSucceeded() {
//
//    }

    private class ShotsReceivedSubscriber extends Subscriber<List<Shot>> {

        @Override
        public void onCompleted() {
            Timber.e("Completed!");
            // adapter.notifyItemInserted();
            adapter.notifyDataSetChanged();
            isLoading = false;
            //loadingMore.setVisibility(View.GONE);
        }

        @Override
        public void onError(Throwable e) {
            Timber.e("listShots Failure: " + e.getMessage());
            isLoading = false;
            //loadingMore.setVisibility(View.GONE);
        }

        @Override
        public void onNext(List<Shot> newShots) {
            if(currPage == 1) {
                shots.clear();
            }

            for(Shot shot : newShots) {
                if(!shots.contains(shot)) {
                    shots.add(shot);
                }
            }
            //shots.addAll(newShots);
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
    //    ButterKnife.unbind(this);
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
