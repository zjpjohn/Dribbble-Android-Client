package com.lucas.freeshots.ui;

import android.app.Activity;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.view.ViewCompat;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.lucas.freeshots.R;
import com.lucas.freeshots.common.Dribbble;
import com.lucas.freeshots.model.Bucket;

import java.util.ArrayList;
import java.util.List;

import rx.Subscriber;
import timber.log.Timber;

import static com.lucas.freeshots.util.Util.$;


public class BucketsFragment extends Fragment {
//    // TODO: Rename parameter arguments, choose names that match
//    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
//    private static final String ARG_PARAM1 = "param1";
//    private static final String ARG_PARAM2 = "param2";
//
//    // TODO: Rename and change types of parameters
//    private String mParam1;
//    private String mParam2;
//
////    private OnFragmentInteractionListener mListener;
//
//    /**
//     * Use this factory method to create a new instance of
//     * this fragment using the provided parameters.
//     *
//     * @param param1 Parameter 1.
//     * @param param2 Parameter 2.
//     * @return A new instance of fragment BucketsFragment.
//     */
//    // TODO: Rename and change types and number of parameters
    public static BucketsFragment newInstance(/*String param1, String param2*/) {
        BucketsFragment fragment = new BucketsFragment();
//        Bundle args = new Bundle();
//        args.putString(ARG_PARAM1, param1);
//        args.putString(ARG_PARAM2, param2);
//        fragment.setArguments(args);
        return fragment;
    }

    public BucketsFragment() {
        // Required empty public constructor
    }

//    @Override
//    public void onCreate(Bundle savedInstanceState) {
//        super.onCreate(savedInstanceState);
//        if (getArguments() != null) {
//            mParam1 = getArguments().getString(ARG_PARAM1);
//            mParam2 = getArguments().getString(ARG_PARAM2);
//        }
//    }

    private BucketAdapter adapter;
    private List<Bucket> buckets = new ArrayList<>();

    private boolean isLoading = false;
    private Activity activity;

    private SwipeRefreshLayout refreshLayout;
    private RecyclerView recyclerView;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_buckets, container, false);
        refreshLayout = $(v, R.id.refresh_layout);
        recyclerView = $(v, R.id.buckets);
        return v;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        activity = getActivity();

        //int spacing = Util.dp2px(activity, 8); // 间隔为8dp

        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(activity);

        recyclerView.setLayoutManager(linearLayoutManager);
        recyclerView.setAdapter(adapter = new BucketAdapter(buckets));
        //recyclerView.addItemDecoration(new LinearVerticalDividerItemDecoration(activity));
        //recyclerView.setPadding(spacing / 2, spacing / 2, spacing / 2, spacing / 2);
        recyclerView.setItemAnimator(new DefaultItemAnimator());

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

    private int currPage = 0;

    /**
     * 加载第一页shots
     */
    private void loadFirstPage() {
        if(!isLoading) {
            isLoading = true;
            adapter.setBottomItemVisible(true);
            buckets.clear();
            currPage = 1;
            //source.get(currPage).subscribe(new BucketsReceivedSubscriber());
            Dribbble.downloadMyBuckets(currPage).subscribe(new BucketsReceivedSubscriber());
        }
    }

    /**
     * 加载下一页shots
     */
    private void loadNextPage() {
        if(!isLoading) {
            isLoading = true;
            adapter.setBottomItemVisible(true);
            //source.get(++currPage).subscribe(new BucketsReceivedSubscriber());
            Dribbble.downloadMyBuckets(++currPage).subscribe(new BucketsReceivedSubscriber());
        }
    }

    private class BucketsReceivedSubscriber extends Subscriber<Bucket> {

        private void over() {
            isLoading = false;
            adapter.setBottomItemVisible(false);
            if(refreshLayout != null && refreshLayout.isRefreshing()) {
                refreshLayout.setRefreshing(false);
            }
        }

        @Override
        public void onCompleted() {
            Timber.e("Completed!");
            // adapter.notifyItemInserted();
            adapter.notifyDataSetChanged();
            over();
        }

        @Override
        public void onError(Throwable e) {
            // TODO: 如果是超时的话，怎么处理，是不是要重启下载！！！！！！！！！！！！！
            Timber.e("Failure: %s", e.getMessage());
            Toast.makeText(activity, e.getMessage(), Toast.LENGTH_LONG).show();  ////////////////////////
            over();
        }

        @Override
        public void onNext(Bucket bucket) {
            if(!buckets.contains(bucket)) {
                buckets.add(bucket);
            }
        }
    }

    private static class BucketAdapter extends PullUpLoadAdapter<Bucket, BucketAdapter.ViewHolder> {

        public BucketAdapter(@NonNull List<Bucket> data) {
            super(data);
        }

        @Override
        public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View v = null;
            if (viewType == VIEW_TYPE_ITEM) {
                v = LayoutInflater.from(parent.getContext()).inflate(R.layout.bucket, parent, false);
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

            Bucket bucket = data.get(position);
            holder.bucketNameTv.setText(bucket.name != null ? bucket.name : "");
            holder.shotCountTv.setText(bucket.shots_count + " shots");
        }

        public static class ViewHolder extends RecyclerView.ViewHolder {
            TextView bucketNameTv;
            TextView shotCountTv;
            int viewType;

            public ViewHolder(View itemView, int viewType) {
                super(itemView);
                this.viewType = viewType;

                if (viewType == VIEW_TYPE_ITEM) {
                    bucketNameTv = $(itemView, R.id.bucket_name);
                    shotCountTv = $(itemView, R.id.shot_count);
                }
            }
        }
    }

    // TODO: Rename method, update argument and hook method into UI event
//    public void onButtonPressed(Uri uri) {
//        if (mListener != null) {
//            mListener.onFragmentInteraction(uri);
//        }
//    }

//    @Override
//    public void onAttach(Activity activity) {
//        super.onAttach(activity);
//        try {
//            mListener = (OnFragmentInteractionListener) activity;
//        } catch (ClassCastException e) {
//            throw new ClassCastException(activity.toString()
//                    + " must implement OnFragmentInteractionListener");
//        }
//    }
//
//    @Override
//    public void onDetach() {
//        super.onDetach();
//        mListener = null;
//    }

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     * <p>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
//     */
//    public interface OnFragmentInteractionListener {
//        // TODO: Update argument type and name
//        public void onFragmentInteraction(Uri uri);
//    }

}
