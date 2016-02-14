package com.lucas.freeshots.ui;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
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
import android.widget.TextView;

import com.facebook.drawee.backends.pipeline.Fresco;
import com.facebook.drawee.view.SimpleDraweeView;
import com.facebook.imagepipeline.request.BasePostprocessor;
import com.facebook.imagepipeline.request.ImageRequest;
import com.facebook.imagepipeline.request.ImageRequestBuilder;
import com.facebook.imagepipeline.request.Postprocessor;
import com.lucas.freeshots.R;
import com.lucas.freeshots.model.Shot;

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
        LinearLayout labelZoneLyout = $(this, R.id.label_zone);

        Shot shot = (Shot) getIntent().getSerializableExtra("shot");

        //        toolbarLayout.setTitle(" ");
        topBarShotTitleTv.setText(shot.title);
        //toolbarLayout.setContentScrim(null);
        //toolbarLayout.setContentScrimColor(cc);

        topBar.post(() -> {
            topBarHeight = topBar.getHeight();
            appBarLayoutHeight = appBarLayout.getHeight();
        });

        appBarLayout.addOnOffsetChangedListener(new AppBarLayout.OnOffsetChangedListener() {
            @Override
            public void onOffsetChanged(AppBarLayout appBarLayout, int verticalOffset) {
                Timber.e("verticalOffset:      " + verticalOffset
                                + "    appBarLayoutHeight:     " + appBarLayoutHeight
                                + "  topBarHeight:" + topBarHeight
                );

//                if (verticalOffset == 0) {
//                    return;
//                }

                topBar.setAlpha(Math.abs(verticalOffset / (float) appBarLayoutHeight));

                int tmp = appBarLayoutHeight + verticalOffset;
                if (tmp < topBarHeight) {
                    topBarInfoZoneVg.setAlpha(1 - Math.abs(tmp / (float) topBarHeight));
                } else {
                    topBarInfoZoneVg.setAlpha(0);
                }
            }
        });

        Postprocessor postprocessor = new BasePostprocessor() {
            @Override
            public void process(Bitmap bitmap) {
                super.process(bitmap);

                Palette.from(bitmap).generate(palette -> {
                    Palette.Swatch vibrant = palette.getVibrantSwatch();
                    Palette.Swatch darkVibrant = palette.getDarkVibrantSwatch();
                    //vibrant.getBodyTextColor();
                    if(vibrant != null && darkVibrant != null) {
                        int vibrantRgb = vibrant.getRgb();
                        int darkVibrantRgb = darkVibrant.getRgb();

                        titleAuthorZone.setBackgroundColor(darkVibrantRgb);
                        shotInfoZone.setBackgroundColor(vibrantRgb);

                        topBar.setBackgroundColor(darkVibrantRgb);
                        topBarInfoZoneVg.setBackgroundColor(darkVibrantRgb);
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
                        //.setUri(uri)
                        .setAutoPlayAnimations(true)
                        .setOldController(shotDv.getController())
                        .setImageRequest(request)
                        .build());
               // shotDv.getDrawable();
            }
        }

        shotTitleTv.setText(shot.title + " : " + shot.images.getType() + " : " + shot.width + ", " + shot.height);

        shotDescribeTv.setText(shot.description != null ? Html.fromHtml(shot.description) : "null");

        likesCountTv.setText(String.format("%d likes", shot.likes_count));
        commentsCountTv.setText(String.format("%d comments", shot.comments_count));
        bucketsCountTv.setText(String.format("%d buckets", shot.buckets_count));
        viewsCountTv.setText(String.format("%d views", shot.views_count));

        for(String tag : shot.tags) {
            Button b = new Button(this);
            b.setText(tag);
            labelZoneLyout.addView(b);
        }

        if(shot.user != null) {
            if(shot.user.avatar_url != null) {
                Uri uri = Uri.parse(shot.user.avatar_url);
                authorIconDv.setImageURI(uri);
                topBarAuthorIconDv.setImageURI(uri);
            }

            String s = String.format("by %s, %s",
                    String.valueOf(shot.user.name), String.valueOf(shot.user.updated_at));
            authorNameTv.setText(s);
            topBarAuthorNameTv.setText(s);
        }

        commentIv.setOnClickListener((view) -> {
            CommentActivity.startMyself(ShowShotActivity.this, shot);
        });

//        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
//        fab.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
//                        .setAction("Action", null).show();
//            }
//        });
    }
}
