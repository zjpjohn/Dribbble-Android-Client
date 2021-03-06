package com.lucas.freeshots.ui.activity;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
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
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.facebook.drawee.view.SimpleDraweeView;
import com.lucas.freeshots.Dribbble.DribbbleComment;
import com.lucas.freeshots.R;
import com.lucas.freeshots.common.Common;
import com.lucas.freeshots.model.Comment;
import com.lucas.freeshots.model.Shot;
import com.lucas.freeshots.ui.LinearVerticalDividerItemDecoration;
import com.lucas.freeshots.ui.PullUpLoadAdapter;

import java.util.ArrayList;
import java.util.List;

import rx.Observable;
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

        RecyclerView recyclerView = $(this, R.id.comments);
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

        createCommentInit();
    }

    private void createCommentInit() {
        FloatingActionButton addCommentBt = $(this, R.id.add_comment_button);
        EditText newCommentEt = $(this, R.id.new_comment_text);
        addCommentBt.setOnClickListener(v -> {
            String comment = newCommentEt.getText().toString().trim();
            if(comment.isEmpty()) {
                Toast.makeText(this, "comment must not be empty", Toast.LENGTH_LONG).show();
                return;
            }

            Observable<Comment> observable = DribbbleComment.createComment(shot.id, comment);
            if(observable == null) {
                Common.writeErrToLogAndShow(CommentActivity.this, "未登录");
                return;
            }

            observable.subscribe(new Subscriber<Comment>() {
                @Override
                public void onCompleted() {

                }

                @Override
                public void onError(Throwable e) {
                    Common.writeErrToLogAndShow(CommentActivity.this, "create comment失败：" + e.getMessage());
                }

                @Override
                public void onNext(Comment comment) {

                }
            });
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

            Observable<Comment> observable = DribbbleComment.getComment(shot.id, currPage);
            if(observable != null) {
                observable.subscribe(new CommentsReceivedSubscriber());
            } else {
                Common.writeErrToLogAndShow(CommentActivity.this, "未登录");
            }
        }
    }

    /**
     * 加载下一页shots
     */
    private void loadNextPage() {
        if(!isLoading) {
            isLoading = true;
            adapter.setBottomItemVisible(true);

            Observable<Comment> observable = DribbbleComment.getComment(shot.id, ++currPage);
            if(observable != null) {
                observable.subscribe(new CommentsReceivedSubscriber());
            } else {
                Common.writeErrToLogAndShow(CommentActivity.this, "未登录");
            }
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
            adapter.notifyDataSetChanged();
            over();
        }

        @Override
        public void onError(Throwable e) {
            Common.writeErrToLogAndShow(CommentActivity.this, e.getMessage());
            over();
        }

        @Override
        public void onNext(Comment comment) {
            if(!comments.contains(comment)) {
                comments.add(comment);
            }
        }
    }

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

            holder.commentTv.setText(Html.fromHtml(comment.body).toString().trim());
            holder.updatedTimeTv.setText(comment.updated_at);

            if(comment.user != null) {
                if(comment.user.avatar_url != null) {
                    holder.authorIconDv.setImageURI(Uri.parse(comment.user.avatar_url));
                }
                holder.authorNameTv.setText(Html.fromHtml(String.format("<b>%s</b>", comment.user.name)));
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
}
