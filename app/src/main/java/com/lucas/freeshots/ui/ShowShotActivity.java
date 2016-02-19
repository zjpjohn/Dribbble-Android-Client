package com.lucas.freeshots.ui;

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

import com.facebook.drawee.backends.pipeline.Fresco;
import com.facebook.drawee.controller.BaseControllerListener;
import com.facebook.drawee.view.SimpleDraweeView;
import com.facebook.imagepipeline.image.ImageInfo;
import com.facebook.imagepipeline.request.BasePostprocessor;
import com.facebook.imagepipeline.request.ImageRequest;
import com.facebook.imagepipeline.request.ImageRequestBuilder;
import com.facebook.imagepipeline.request.Postprocessor;
import com.lucas.freeshots.R;
import com.lucas.freeshots.model.Shot;
import com.lucas.freeshots.view.AutoLinefeedLinearLayout;

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
        ProgressBar laodingShotPb = $(this, R.id.loadingShot);

        View titleAuthorZone = $(this, R.id.title_author_zone);
        SimpleDraweeView authorIconDv = $(this, R.id.author_icon);
        TextView shotTitleTv = $(this, R.id.shot_title);
        TextView authorNameTv = $(this, R.id.author_name);

        View shotInfoZone = $(this, R.id.shot_info_zone);
        TextView likesCountTv = $(this, R.id.likes_count);
        TextView commentsCountTv = $(this, R.id.comments_count);
        TextView bucketsCountTv = $(this, R.id.buckets_count);
        TextView viewsCountTv = $(this, R.id.views_count);

        ImageView likeIv = $(this, R.id.like);
        ImageView commentIv = $(this, R.id.comment);
        ImageView bucketIv = $(this, R.id.bucket);

        TextView shotDescribeTv = $(this, R.id.shot_describe);
        AutoLinefeedLinearLayout labelZoneLayout = $(this, R.id.label_zone);

        Shot shot = (Shot) getIntent().getSerializableExtra("shot");

        topBarShotTitleTv.setText(shot.title);

        topBar.post(() -> {
            topBarHeight = topBar.getHeight();
            appBarLayoutHeight = appBarLayout.getHeight();
        });

        appBarLayout.addOnOffsetChangedListener((appBarLayout1, verticalOffset) -> {
            topBar.setAlpha(Math.abs(verticalOffset / (float) appBarLayoutHeight));

            int tmp = appBarLayoutHeight + verticalOffset;
            final float threshold = 2 * topBarHeight;
            if (tmp < threshold) {
                topBarInfoZoneVg.setAlpha(1 - Math.abs(tmp / threshold));
            } else {
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
                                laodingShotPb.setVisibility(View.GONE);
                            }

                            @Override
                            public void onFailure(String id, Throwable throwable) {
                                super.onFailure(id, throwable);
                                laodingShotPb.setVisibility(View.GONE);
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
            Timber.e(tag);
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

        commentIv.setOnClickListener((view) -> {
            CommentActivity.startMyself(this, shot);
        });
    }
}
