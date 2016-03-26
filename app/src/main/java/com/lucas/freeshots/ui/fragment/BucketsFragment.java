package com.lucas.freeshots.ui.fragment;

import android.app.Activity;
import android.app.AlertDialog;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewCompat;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.lucas.freeshots.Dribbble.DribbbleBucket;
import com.lucas.freeshots.R;
import com.lucas.freeshots.common.Common;
import com.lucas.freeshots.model.Bucket;
import com.lucas.freeshots.ui.LinearVerticalDividerItemDecoration;
import com.lucas.freeshots.ui.PullUpLoadAdapter;
import com.lucas.freeshots.ui.activity.DisplayOneBucketActivity;
import com.lucas.freeshots.view.ShowInfoAlertDialog;

import java.util.ArrayList;
import java.util.List;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import rx.Observable;
import rx.Subscriber;
import timber.log.Timber;

import static com.lucas.freeshots.util.Util.$;


public class BucketsFragment extends Fragment {
    public static BucketsFragment newInstance() {
        return new BucketsFragment();
    }

    public BucketsFragment() {
        // Required empty public constructor
    }

    private BucketAdapter adapter;
    private List<Bucket> buckets = new ArrayList<>();

    private boolean isLoading = false;
    private Activity activity;

    private SwipeRefreshLayout refreshLayout;
    private RecyclerView recyclerView;
    private FloatingActionButton fab;

    private enum Mode {
        SHOW, ADD
    }

    private Mode mode = Mode.SHOW; // 默认为SHOW模式
    private int addedShotId = -1;

    public interface OnAddShotToBucket {
        void onSuccess();
        void onFailed();
    }

    @SuppressWarnings("unused")
    public void setShowMode() {
        mode = Mode.SHOW;
    }

    public void setAddMode(int shotId) {
        if(!(getActivity() instanceof OnAddShotToBucket)) {
            throw new RuntimeException("设置ADD模式必须实现接口OnAddShotToBucket");
        }

        if(shotId > 0) {
            mode = Mode.ADD;
            addedShotId = shotId;
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_buckets, container, false);
        refreshLayout = $(v, R.id.refresh_layout);
        recyclerView = $(v, R.id.buckets);
        fab = $(v, R.id.fab);
        return v;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        activity = getActivity();

        // add new bucket
        fab.setOnClickListener(v -> addBucket());

        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(activity);

        recyclerView.setLayoutManager(linearLayoutManager);
        recyclerView.setAdapter(adapter = new BucketAdapter(buckets));
        recyclerView.addItemDecoration(new LinearVerticalDividerItemDecoration(activity));
        recyclerView.setItemAnimator(new DefaultItemAnimator());

        recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);
            }

            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);

                // 滑到了底部则自动加载下一页
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
     * 加载第一页
     */
    private void loadFirstPage() {
        if(!isLoading) {
            isLoading = true;
            adapter.setBottomItemVisible(true);
            buckets.clear();
            currPage = 1;

            Observable<Bucket> observable = DribbbleBucket.getMyBuckets(currPage);
            if(observable != null) {
                observable.subscribe(new BucketsReceivedSubscriber());
            } else {
                Common.writeErrToLogAndShow(activity, "未登录");
            }
        }
    }

    /**
     * 加载下一页
     */
    private void loadNextPage() {
        if(!isLoading) {
            isLoading = true;
            adapter.setBottomItemVisible(true);

            Observable<Bucket> observable = DribbbleBucket.getMyBuckets(++currPage);
            if(observable != null) {
                observable.subscribe(new BucketsReceivedSubscriber());
            } else {
                Common.writeErrToLogAndShow(activity, "未登录");
            }
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
            Common.writeErrToLogAndShow(activity, e.getMessage());
            over();
        }

        @Override
        public void onNext(Bucket bucket) {
            if(!buckets.contains(bucket)) {
                buckets.add(bucket);
            }
        }
    }

    private void addBucket() {
        View contentView = LayoutInflater.from(activity).inflate(R.layout.alert_dialog_add_bucket, null);

        EditText bucketNameEt = $(contentView, R.id.bucket_name);
        EditText bucketDescriptionEt = $(contentView, R.id.bucket_description);

        TextView cancelTv = $(contentView, R.id.cancel);
        TextView createTv = $(contentView, R.id.create);

        AlertDialog dialog = new AlertDialog.Builder(activity)
                .setTitle(R.string.create_new_bucket)
                .setView(contentView)
                .show();

        cancelTv.setOnClickListener(v1 -> dialog.dismiss());

        createTv.setOnClickListener(v1 -> {
            String name = bucketNameEt.getText().toString().trim();
            String description = bucketDescriptionEt.getText().toString().trim();

            if(name.isEmpty()) {
                Toast.makeText(activity, "bucket name should not empty", Toast.LENGTH_LONG).show();
                return;
            }

            Observable<Bucket> observable = DribbbleBucket.addOneBucket(name, description);
            if(observable == null) {
                Common.writeErrToLogAndShow(activity, "未登录");
                return;
            }

            observable.subscribe(new Subscriber<Bucket>() {
                @Override
                public void onCompleted() {
                    Toast.makeText(activity, "bucket created", Toast.LENGTH_LONG).show();
                    loadFirstPage();
                    dialog.dismiss();
                }

                @Override
                public void onError(Throwable e) {
                    Toast.makeText(activity, e.getMessage(), Toast.LENGTH_LONG).show();
                }

                @Override
                public void onNext(Bucket bucket) {

                }
            });
        });
    }

    private boolean deleteBucket(int bucketId) {
        Call<ResponseBody> call = DribbbleBucket.deleteOneBucket(bucketId);
        if(call == null) {
            Common.writeErrToLogAndShow(activity, "未登录");
            return false;
        }

        call.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Response<ResponseBody> response) {
                if(response.code() == 204) { // success
                    loadFirstPage();
                    Common.writeErrToLogAndShow(activity, "删除bucket成功，id：" + bucketId);
                } else {
                    String msg = String.format("删除bucket失败，id=%d, code=%d", bucketId, response.code());
                    Common.writeErrToLogAndShow(activity, msg);
                }
            }

            @Override
            public void onFailure(Throwable t) {
                Common.writeErrToLogAndShow(activity, "bucket.id: " + bucketId + " " + t.getMessage());
            }
        });

        return true;
    }

    private class BucketAdapter extends PullUpLoadAdapter<Bucket, BucketAdapter.ViewHolder> {

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
            holder.shotCountTv.setText(String.format("%d  shots", bucket.shots_count));

            if(holder.viewType == VIEW_TYPE_ITEM) {
                if(mode == Mode.ADD) {
                    holder.itemView.setOnClickListener(v -> addShotToBucket(bucket.id));
                } else {
                    holder.itemView.setOnClickListener(v -> DisplayOneBucketActivity.startMyself(v.getContext(), bucket));
                }

                holder.itemView.setOnLongClickListener(v -> {
                    String delete = "DELETE";
                    String cancel = "CENCEL";

                    new ShowInfoAlertDialog(activity, "title", "content", null,
                            new String[]{delete, cancel}, actionName -> {
                        if (actionName.equals(delete)) {
                            deleteBucket(bucket.id);
                        }
                    }).show();

                    return true;
                });
            }
        }

        private void addShotToBucket(int bucketId) {
            Call<ResponseBody> call = DribbbleBucket.addShotToBucket(bucketId, addedShotId);
            if(call == null) {
                Common.writeErrToLogAndShow(activity, "未登录");
                return;
            }

            call.enqueue(new Callback<ResponseBody>() {
                private OnAddShotToBucket onAddShotToBucket = (OnAddShotToBucket) activity;

                @Override
                public void onResponse(Response<ResponseBody> response) {
                    if(response.code() == 204) { // success
                        Common.writeErrToLogAndShow(activity, "add shot to bucket success");
                        onAddShotToBucket.onSuccess();
                    } else {
                        Common.writeErrToLogAndShow(activity, String.format(
                                    "Add a shot to a bucket 失败，bucketId=%d, shotId=%d, code=%d",
                                    bucketId, addedShotId, response.code()));
                        onAddShotToBucket.onFailed();
                    }
                }

                @Override
                public void onFailure(Throwable t) {
                    Common.writeErrToLogAndShow(activity, String.format(
                                    "Add a shot to a bucket 失败，bucketId=%d, shotId=%d, %s",
                                    bucketId, addedShotId, t.getMessage()));
                    onAddShotToBucket.onFailed();
                }
            });
        }

        public class ViewHolder extends RecyclerView.ViewHolder {
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
}
