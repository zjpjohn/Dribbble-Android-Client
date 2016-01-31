package com.lucas.freeshots.ui;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.Html;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.facebook.drawee.view.SimpleDraweeView;
import com.lucas.freeshots.R;
import com.lucas.freeshots.model.Shot;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import timber.log.Timber;

public class ShowShotActivity extends AppCompatActivity {

    public static void startMyself(Context context, @NonNull Shot shot) {
        Intent intent = new Intent(context, ShowShotActivity.class);
        intent.putExtra("shot", shot);
        context.startActivity(intent);
    }

    @Bind(R.id.app_bar) AppBarLayout appBarLayout;
    @Bind(R.id.toolbar_layout) CollapsingToolbarLayout toolbarLayout;
    @Bind(R.id.shot) SimpleDraweeView shotDv;
    @Bind(R.id.author_icon) SimpleDraweeView authorIconDv;
    @Bind(R.id.shot_title) TextView shotTitleTv;
    @Bind(R.id.author_name) TextView authorNameTv;

    @Bind(R.id.likes_count) TextView likesCountTv;
    @Bind(R.id.comments_count) TextView commentsCountTv;
    @Bind(R.id.buckets_count) TextView bucketsCountTv;
    @Bind(R.id.views_count) TextView viewsCountTv;

    @Bind(R.id.like) ImageView likeIv;
    @Bind(R.id.comment) ImageView commentIv;
    @Bind(R.id.bucket) ImageView bucketIv;

    @Bind(R.id.shot_describe) TextView shotDescribeTv;
    @Bind(R.id.label_zone) LinearLayout labelZoneLyout;

    @OnClick(R.id.like)
    public void clickLike(ImageView likeIv) {
    }

    //@BindColor(R.color.cardview_light_background) int cc;///////////////////////////////////////////////////

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_show_shot);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        toolbar.setNavigationIcon(R.mipmap.abc_ic_action_back);
        toolbar.setNavigationOnClickListener(view -> finish());

        ButterKnife.bind(this);

        //toolbarLayout.setTitle("   ");
        //d.setContentScrimColor(cc);
        appBarLayout.addOnOffsetChangedListener(new AppBarLayout.OnOffsetChangedListener() {
            @Override
            public void onOffsetChanged(AppBarLayout appBarLayout, int verticalOffset) {
                Timber.e("verticalOffset:      " + verticalOffset);
            }
        });

        Shot shot = (Shot) getIntent().getSerializableExtra("shot");

        shotDv.setImageURI(Uri.parse(shot.images.getHeightImageUri()));
        shotTitleTv.setText(shot.title + " : " + shot.images.getType() + " : " + shot.width + ", " + shot.height);
        shotDescribeTv.setText(Html.fromHtml(shot.description));

        likesCountTv.setText(String.valueOf(shot.likes_count));
        commentsCountTv.setText(String.valueOf(shot.comments_count));
        bucketsCountTv.setText(String.valueOf(shot.buckets_count));
        viewsCountTv.setText(String.valueOf(shot.views_count));

        for(String tag : shot.tags) {
            Button b = new Button(this);
            b.setText(tag);
            labelZoneLyout.addView(b);
        }

        if(shot.user != null) {
            if(shot.user.avatar_url != null) {
                authorIconDv.setImageURI(Uri.parse(shot.user.avatar_url));
            }

            String s = String.format("by %s, %s",
                    String.valueOf(shot.user.name), String.valueOf(shot.user.updated_at));
            authorNameTv.setText(s);
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
