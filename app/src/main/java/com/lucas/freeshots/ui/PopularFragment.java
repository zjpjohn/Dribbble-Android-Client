package com.lucas.freeshots.ui;

import android.app.Activity;
import android.graphics.Rect;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.lucas.freeshots.DribbbleService;
import com.lucas.freeshots.R;
import com.lucas.freeshots.ShotAdapter;
import com.lucas.freeshots.common.Dribbble;
import com.lucas.freeshots.model.Shot;
import com.lucas.freeshots.util.Util;

import java.util.ArrayList;
import java.util.List;

import hugo.weaving.DebugLog;
import retrofit2.GsonConverterFactory;
import retrofit2.Retrofit;
import retrofit2.RxJavaCallAdapterFactory;
import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;
import timber.log.Timber;

public class PopularFragment extends Fragment {
    private static final String TAG = "PopularFragment";
    public PopularFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_popular, container, false);
    }

    private List<Shot> shots = new ArrayList<>();
    private ShotAdapter adapter;

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        Activity activity = getActivity();

        int spacing = Util.dp2px(activity, 8); // 间隔为8dp

        RecyclerView recyclerView = (RecyclerView) activity.findViewById(R.id.popularShotRecyclerView);
        recyclerView.setLayoutManager(new GridLayoutManager(activity, 2));
        recyclerView.setAdapter(adapter = new ShotAdapter(shots));
        recyclerView.addItemDecoration(new GridItemDecoration(spacing));
        recyclerView.setPadding(spacing / 2, spacing / 2, spacing / 2, spacing / 2);
        recyclerView.setItemAnimator(new DefaultItemAnimator());

        downloadShots();
    }

    private void downloadShots() {
        Retrofit retrofit = new Retrofit.Builder()
                .addConverterFactory(GsonConverterFactory.create())
                .addCallAdapterFactory(RxJavaCallAdapterFactory.create())
                .baseUrl(Dribbble.API_ADDRESS)
                .build();
        DribbbleService service = retrofit.create(DribbbleService.class);

        service.listShots(Dribbble.SHOT_SORT_BY_VIEWS)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Subscriber<List<Shot>>() {
                    @Override
                    public void onCompleted() {
                        Timber.e("Completed!");
                        adapter.notifyDataSetChanged();
                    }

                    @Override
                    public void onError(Throwable e) {
                        Timber.e("listShots Failure: " + e.getMessage());
                    }

                    @DebugLog
                    @Override
                    public void onNext(List<Shot> newShots) {
                        shots.clear();
                        shots.addAll(newShots);
                    }
                });
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
