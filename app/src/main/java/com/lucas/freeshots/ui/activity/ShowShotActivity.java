package com.lucas.freeshots.ui.activity;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.Animatable;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.graphics.Palette;
import android.text.Html;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.facebook.drawee.backends.pipeline.Fresco;
import com.facebook.drawee.controller.BaseControllerListener;
import com.facebook.drawee.view.SimpleDraweeView;
import com.facebook.imagepipeline.image.ImageInfo;
import com.facebook.imagepipeline.request.BasePostprocessor;
import com.facebook.imagepipeline.request.ImageRequest;
import com.facebook.imagepipeline.request.ImageRequestBuilder;
import com.facebook.imagepipeline.request.Postprocessor;
import com.lucas.freeshots.Dribbble.DribbbleLikes;
import com.lucas.freeshots.R;
import com.lucas.freeshots.common.Common;
import com.lucas.freeshots.model.Shot;
import com.lucas.freeshots.view.AutoLinefeedLinearLayout;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import timber.log.Timber;

import static com.lucas.freeshots.util.Util.$;

public class ShowShotActivity extends AppCompatActivity {

    public static void startMyself(Context context, @NonNull Shot shot) {
        Intent intent = new Intent(context, ShowShotActivity.class);
        intent.putExtra("shot", shot);
        context.startActivity(intent);
    }

    private int topBarHeight;
    private int appBarLayoutHeight;

    private static final int UNKNOWN = 0;
    private static final int LIKED = 1;
    private static final int UNLIKED = 2;

    private int isLiked = UNKNOWN; // 是否已经like过了这个shot

    private ImageView likeIv;
    private ProgressBar likeLoadingPb;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_show_shot);

//        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
//        setSupportActionBar(toolbar);
//        toolbar.setNavigationIcon(R.mipmap.abc_ic_action_back);
//        toolbar.setNavigationOnClickListener(view -> finish());

        LinearLayout topBar = $(this, R.id.top_bar);
        SimpleDraweeView topBarAuthorIconDv = $(this, R.id.top_bar_author_icon);
        TextView topBarShotTitleTv = $(this, R.id.top_bar_shot_title);
        TextView topBarAuthorNameTv = $(this, R.id.top_bar_author_name);
        View backV = $(this, R.id.back);
        backV.setOnClickListener(view -> finish());
        ViewGroup topBarInfoZoneVg = $(this, R.id.top_bar_info_zone);

        AppBarLayout appBarLayout = $(this, R.id.app_bar);
        CollapsingToolbarLayout toolbarLayout = $(this, R.id.toolbar_layout);
        SimpleDraweeView shotDv = $(this, R.id.shot);
        ProgressBar loadingShotPb = $(this, R.id.loadingShot);

        View titleAuthorZone = $(this, R.id.title_author_zone);
        SimpleDraweeView authorIconDv = $(this, R.id.author_icon);
        TextView shotTitleTv = $(this, R.id.shot_title);
        TextView authorNameTv = $(this, R.id.author_name);

        View shotInfoZone = $(this, R.id.shot_info_zone);
        TextView likesCountTv = $(this, R.id.likes_count);
        TextView commentsCountTv = $(this, R.id.comments_count);
        TextView bucketsCountTv = $(this, R.id.buckets_count);
        TextView viewsCountTv = $(this, R.id.views_count);

        likeIv = $(this, R.id.like);
        likeLoadingPb = $(this, R.id.likeLoading);
        ImageView commentIv = $(this, R.id.comment);
        ImageView bucketIv = $(this, R.id.bucket);

        TextView shotDescribeTv = $(this, R.id.shot_describe);
        AutoLinefeedLinearLayout labelZoneLayout = $(this, R.id.label_zone);

        Shot shot = (Shot) getIntent().getSerializableExtra("shot");

        checkIfLiked(shot.id);
        topBarShotTitleTv.setText(shot.title);

        topBar.post(() -> {
            topBarHeight = topBar.getHeight();
            appBarLayoutHeight = appBarLayout.getHeight();
        });

        appBarLayout.addOnOffsetChangedListener((appBarLayout1, verticalOffset) -> {
            topBar.setAlpha(Math.abs(verticalOffset / (float) appBarLayoutHeight));

            int tmp = appBarLayoutHeight + verticalOffset;  // 距顶部的距离
            final float threshold = 2.5f * topBarHeight;
            if (tmp < threshold) {
                float ratio = Math.abs(tmp / threshold);
                int paddingTop = (int) (ratio * topBarHeight);
                topBarInfoZoneVg.setAlpha(1 - ratio);

                topBarInfoZoneVg.setPadding(topBarInfoZoneVg.getPaddingLeft(), paddingTop,
                                            topBarInfoZoneVg.getPaddingRight(), -paddingTop);
            } else if(topBarInfoZoneVg.getAlpha() != 0) {
                topBarInfoZoneVg.setAlpha(0);
            }
        });

        Postprocessor postprocessor = new BasePostprocessor() {
            @Override
            public void process(Bitmap bitmap) {
                super.process(bitmap);

                // 提取图片的颜色来改变界面组件的颜色，使风格一致。
                Palette.from(bitmap).generate(palette -> {
                    Palette.Swatch vibrant = palette.getVibrantSwatch();
                    if(vibrant != null) {
                        int vibrantRgb = vibrant.getRgb();
                        titleAuthorZone.setBackgroundColor(vibrantRgb);
                        shotInfoZone.setBackgroundColor(vibrantRgb);
                        topBar.setBackgroundColor(vibrantRgb);
                        topBarInfoZoneVg.setBackgroundColor(vibrantRgb);
                    }
                });
            }
        };

        if(shot.images != null) {
            String uri = shot.images.getHeightImageUri();
            if(uri != null) {
                ImageRequest request = ImageRequestBuilder
                                    .newBuilderWithSource(Uri.parse(uri))
                                    .setPostprocessor(postprocessor)
                                    .build();

                shotDv.setController(Fresco.newDraweeControllerBuilder()
                        .setControllerListener(new BaseControllerListener<ImageInfo>() {
                            // 无论下载成功或失败，都隐藏掉 loading ProcessBar
                            @Override
                            public void onFinalImageSet(String id, ImageInfo imageInfo,
                                                        Animatable animatable) {
                                super.onFinalImageSet(id, imageInfo, animatable);
                                loadingShotPb.setVisibility(View.GONE);
                            }

                            @Override
                            public void onFailure(String id, Throwable throwable) {
                                super.onFailure(id, throwable);
                                loadingShotPb.setVisibility(View.GONE);
                            }
                        })
                        .setAutoPlayAnimations(true)
                        .setOldController(shotDv.getController())
                        .setImageRequest(request)
                        .build());
            }
        }

        shotTitleTv.setText(shot.title != null ? shot.title : "null");
        shotDescribeTv.setText(shot.description != null
                                ? Html.fromHtml(shot.description).toString().trim()
                                : "null");
        likesCountTv.setText(String.format("%d likes", shot.likes_count));
        commentsCountTv.setText(String.format("%d comments", shot.comments_count));
        bucketsCountTv.setText(String.format("%d buckets", shot.buckets_count));
        viewsCountTv.setText(String.format("%d views", shot.views_count));

        for(String tag : shot.tags) {
            Button b = new Button(this);
            b.setText(tag);
            b.setClickable(false);
            labelZoneLayout.addView(b);
        }

        if(shot.user != null) {
            if(shot.user.avatar_url != null) {
                Uri uri = Uri.parse(shot.user.avatar_url);
                authorIconDv.setImageURI(uri);
                topBarAuthorIconDv.setImageURI(uri);
            }

            String userName = "by " + (shot.user.name != null ? shot.user.name : "null");
            authorNameTv.setText(userName);
            topBarAuthorNameTv.setText(userName);
        }

        authorIconDv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });

        likeIv.setOnClickListener(v -> {
            /*
             * like状态为Unknown时，likeIv不会显示出来，无法点击，
             * 所以这里只考虑LIKED 和 UNLIKED两种状态。
             */

            if(isLiked == LIKED) {
                unLikeShot(shot.id);
            } else if(isLiked == UNLIKED) {
                likeShot(shot.id);
            }
        });

        commentIv.setOnClickListener(v -> CommentActivity.startMyself(this, shot));
        bucketIv.setOnClickListener(v -> AddShotToBucketActivity.startMyself(this, shot.id));
    }

    /**
     * Check if liked a shot
     * @param shotId 待check的shot id
     */
    private void checkIfLiked(int shotId) {
        Call<ResponseBody> call = DribbbleLikes.checkLikeShot(shotId);
        if(call == null) {
            Common.writeErrToLogAndShow(this, "未登录");
            return;
        }

        likeIv.setVisibility(View.INVISIBLE);
        likeLoadingPb.setVisibility(View.VISIBLE);

        call.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(retrofit2.Response<ResponseBody> response) {
                if(response.code() == 200) {
                    isLiked = LIKED;
                    likeIv.setVisibility(View.VISIBLE);
                    likeLoadingPb.setVisibility(View.INVISIBLE);
                    likeIv.setImageDrawable(getResources().getDrawable(R.mipmap.ic_action_liked));
                } else if(response.code() == 404) {
                    isLiked = UNLIKED;
                    likeIv.setVisibility(View.VISIBLE);
                    likeLoadingPb.setVisibility(View.INVISIBLE);
                    likeIv.setImageDrawable(getResources().getDrawable(R.mipmap.ic_action_like_empty));
                } else {
                    isLiked = UNKNOWN;
                    Timber.e("取shot的like状态失败，response.code()：" + response.code());
                }
            }

            @Override
            public void onFailure(Throwable t) {
                isLiked = UNKNOWN;
                Timber.e("取shot的like状态失败，" + t.getMessage());
            }
        });
    }

    /**
     * like a shot
     * @param shotId shot id
     */
    private void likeShot(int shotId) {
        Call<ResponseBody> call = DribbbleLikes.likeShot(shotId);
        if(call == null) {
            Toast.makeText(this, "没有登录，不能like", Toast.LENGTH_SHORT).show();
            return;
        }

        likeIv.setVisibility(View.INVISIBLE);
        likeLoadingPb.setVisibility(View.VISIBLE);

        call.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Response<ResponseBody> response) {
                if(response.code() == 201) {  // success
                    Timber.i("like success, shot id: " + shotId);
                    isLiked = LIKED;
                    likeIv.setVisibility(View.VISIBLE);
                    likeIv.setImageDrawable(getResources().getDrawable(R.mipmap.ic_action_liked));
                    likeLoadingPb.setVisibility(View.INVISIBLE);
                } else {
                    likeOperationError(String.format("like 失败，shot id: %d, 错误码：%d", shotId, response.code()));
                }
            }

            @Override
            public void onFailure(Throwable t) {
                likeOperationError(String.format("like 失败，shot id: %d, %s", shotId, t.getMessage()));
            }
        });
    }

    /**
     * unLike a shot
     * @param shotId shot id
     */
    private void unLikeShot(int shotId) {
        Call<ResponseBody> call = DribbbleLikes.unlikeShot(shotId);
        if(call == null) {
            Toast.makeText(this, "没有登录，不能unLike", Toast.LENGTH_SHORT).show();
            return;
        }

        likeIv.setVisibility(View.INVISIBLE);
        likeLoadingPb.setVisibility(View.VISIBLE);

        call.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Response<ResponseBody> response) {
                if(response.code() == 204) { // success
                    Timber.i("unLike success, shot id: " + shotId);
                    isLiked = UNLIKED;
                    likeIv.setVisibility(View.VISIBLE);
                    likeIv.setImageDrawable(getResources().getDrawable(R.mipmap.ic_action_like_empty));
                    likeLoadingPb.setVisibility(View.INVISIBLE);
                } else {
                    likeOperationError(String.format("unLike 失败，shot id: %d, 错误码：%d", shotId, response.code()));
                }
            }

            @Override
            public void onFailure(Throwable t) {
                likeOperationError(String.format("unLike 失败，shot id: %d, %s", shotId, t.getMessage()));
            }
        });
    }

    /**
     * like or unlike 操作失败后的处理
     * @param errStr 错误信息
     */
    private void likeOperationError(String errStr) {
        Timber.e(errStr);
        Toast.makeText(this, errStr, Toast.LENGTH_LONG).show();

        likeIv.setVisibility(View.VISIBLE);
        likeLoadingPb.setVisibility(View.INVISIBLE);
    }
}
