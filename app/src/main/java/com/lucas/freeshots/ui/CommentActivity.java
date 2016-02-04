package com.lucas.freeshots.ui;

import android.content.Context;
import android.content.Intent;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.view.ViewCompat;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.facebook.drawee.view.SimpleDraweeView;
import com.lucas.freeshots.R;
import com.lucas.freeshots.common.Dribbble;
import com.lucas.freeshots.model.Comment;
import com.lucas.freeshots.model.Shot;

import java.util.ArrayList;
import java.util.List;

import rx.Subscriber;
import timber.log.Timber;

import static com.lucas.freeshots.util.Util.$;

public class CommentActivity extends AppCompatActivity {

    public static void startMyself(Context context, @NonNull Shot shot) {
        Intent intent = new Intent(context, CommentActivity.class);
        intent.putExtra("shot", shot);
        context.startActivity(intent);
    }

    private SwipeRefreshLayout refreshLayout;

    private boolean isLoading = false;
    private List<Comment> comments = new ArrayList<>();
    private CommentAdapter adapter;
    private Shot shot;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_comment);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        toolbar.setNavigationIcon(R.mipmap.abc_ic_action_back);
        toolbar.setNavigationOnClickListener(view -> finish());

        refreshLayout = $(this, R.id.refresh_layout);

        shot = (Shot) getIntent().getSerializableExtra("shot");

        RecyclerView recyclerView = (RecyclerView) findViewById(R.id.comments);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter = new CommentAdapter(comments));
        recyclerView.addItemDecoration(new LinearVerticalDividerItemDecoration(this));
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
            comments.clear();
            currPage = 1;
            Dribbble.downloadComment(shot.id, currPage).subscribe(new CommentsReceivedSubscriber());
            //source.get(currPage).subscribe(new ShotsReceivedSubscriber());
        }
    }

    /**
     * 加载下一页shots
     */
    private void loadNextPage() {
        if(!isLoading) {
            isLoading = true;
            adapter.setBottomItemVisible(true);
            Dribbble.downloadComment(shot.id, ++currPage).subscribe(new CommentsReceivedSubscriber());
            //source.get(++currPage).subscribe(new ShotsReceivedSubscriber());
        }
    }

    private class CommentsReceivedSubscriber extends Subscriber<Comment> {

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
            Toast.makeText(CommentActivity.this, e.getMessage(), Toast.LENGTH_LONG).show();  ////////////////////////
            over();
        }

        @Override
        public void onNext(Comment comment) {
            if(!comments.contains(comment)) {
                comments.add(comment);
            }
        }
    }

//    private void refreshComments() {
//        if(!isLoading) {
//            isLoading = true;
//            Dribbble.downloadComment(shot.id).subscribe(new Subscriber<List<Comment>>() {
//                        @Override
//                        public void onCompleted() {
//                            Timber.e("CommentActivity Completed!");
//                            adapter.notifyDataSetChanged();
//                            isLoading = false;
//                            if (refreshLayout != null && refreshLayout.isRefreshing()) {
//                                refreshLayout.setRefreshing(false);
//                            }
//                        }
//
//                        @Override
//                        public void onError(Throwable e) {
//                            Timber.e("listShots Failure: " + e.getMessage());
//                            Toast.makeText(CommentActivity.this, e.getMessage(), Toast.LENGTH_LONG).show();  ////////////////////////
//                            isLoading = false;
//                            if (refreshLayout != null && refreshLayout.isRefreshing()) {
//                                refreshLayout.setRefreshing(false);
//                            }
//                        }
//
//                        @Override
//                        public void onNext(List<Comment> newComments) {
//                            Timber.e("newComments.size(): " + newComments.size());
//                            comments.clear();
//                            comments.addAll(newComments);
//                        }
//                    });
//        }
//    }

    private static class CommentAdapter extends PullUpLoadAdapter<Comment, CommentAdapter.ViewHolder> {

        public CommentAdapter(@NonNull List<Comment> data) {
            super(data);
        }

        @Override
        public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View v = null;
            if (viewType == VIEW_TYPE_ITEM) {
                v = LayoutInflater.from(parent.getContext()).inflate(R.layout.comment, parent, false);
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

            Comment comment = data.get(position);

            holder.commentTv.setText(Html.fromHtml(comment.body));
            holder.updatedTimeTv.setText(comment.updated_at);

            if(comment.user != null) {
                if(comment.user.avatar_url != null) {
                    holder.authorIconDv.setImageURI(Uri.parse(comment.user.avatar_url));
                }
                holder.authorNameTv.setText(comment.user.name);
            }
        }

        public static class ViewHolder extends RecyclerView.ViewHolder {
            public SimpleDraweeView authorIconDv;
            public TextView authorNameTv;
            public TextView commentTv;
            public TextView updatedTimeTv;

            int viewType;

            public ViewHolder(View v, int viewType) {
                super(v);
                this.viewType = viewType;

                if (viewType == VIEW_TYPE_ITEM) {
                    authorIconDv = $(v, R.id.author_icon);
                    authorNameTv = $(v, R.id.author_name);
                    commentTv = $(v, R.id.comment);
                    updatedTimeTv = $(v, R.id.updated_time);
                }
            }
        }
    }

//    static class CommentAdapter extends RecyclerView.Adapter<CommentAdapter.ViewHolder> {
//        private List<Comment> comments;
//
//        public CommentAdapter(@NonNull List<Comment> comments) {
//            this.comments = comments;
//        }
//
//        @Override
//        public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
//            return new ViewHolder(LayoutInflater.from(
//                                    parent.getContext()).inflate(R.layout.comment, parent, false));
//        }
//
//        @Override
//        public void onBindViewHolder(ViewHolder holder, int position) {
//            Comment comment = comments.get(position);
//
//            holder.commentTv.setText(Html.fromHtml(comment.body));
//            holder.updatedTimeTv.setText(comment.updated_at);
//
//            if(comment.user != null) {
//                if(comment.user.avatar_url != null) {
//                    holder.authorIconDv.setImageURI(Uri.parse(comment.user.avatar_url));
//                }
//                holder.authorNameTv.setText(comment.user.name);
//            }
//        }
//
//        @Override
//        public int getItemCount() {
//            return comments.size();
//        }
//
//        public static class ViewHolder extends RecyclerView.ViewHolder {
//            public SimpleDraweeView authorIconDv;
//            public TextView authorNameTv;
//            public TextView commentTv;
//            public TextView updatedTimeTv;
//
//            public ViewHolder(View v) {
//                super(v);
//                authorIconDv = $(v, R.id.author_icon);
//                authorNameTv = $(v, R.id.author_name);
//                commentTv = $(v, R.id.comment);
//                updatedTimeTv = $(v, R.id.updated_time);
//            }
//        }
//    }

    private static class LinearVerticalDividerItemDecoration extends RecyclerView.ItemDecoration {

        private static final int[] ATTRS = new int[]{
                android.R.attr.listDivider
        };

        private Drawable divider;

        public LinearVerticalDividerItemDecoration(Context context) {
            final TypedArray a = context.obtainStyledAttributes(ATTRS);
            divider = a.getDrawable(0);
            a.recycle();
        }

        @Override
        public void onDrawOver(Canvas c, RecyclerView parent, RecyclerView.State state) {
            // 画水平分割线线
            int left = parent.getPaddingLeft();
            int right = parent.getWidth() - parent.getPaddingRight();

            int childCount = parent.getChildCount();
            for (int i = 0; i < childCount; i++) {
                View child = parent.getChildAt(i);
                RecyclerView.LayoutParams params = (RecyclerView.LayoutParams) child.getLayoutParams();
                int top = child.getBottom() + params.bottomMargin;
                int bottom = top + divider.getIntrinsicHeight();
                divider.setBounds(left, top, right, bottom);
                divider.draw(c);
            }
        }
    }
}
