package com.lucas.freeshots.ui.activity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;

import com.lucas.freeshots.Dribbble.DribbbleBucket;
import com.lucas.freeshots.R;
import com.lucas.freeshots.model.Bucket;
import com.lucas.freeshots.ui.fragment.DisplayShotsFragment;

public class DisplayOneBucketActivity extends AppCompatActivity {

    public static void startMyself(Context context, @NonNull Bucket bucket) {
        Intent intent = new Intent(context, DisplayOneBucketActivity.class);
        intent.putExtra("bucket", bucket);
        context.startActivity(intent);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_display_one_bucket);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        toolbar.setNavigationIcon(R.mipmap.abc_ic_action_back);
        toolbar.setNavigationOnClickListener(view -> finish());

        Bucket bucket = (Bucket) getIntent().getSerializableExtra("bucket");
        if(bucket == null) {
            return;
        }

        toolbar.setTitle(bucket.name);
        toolbar.setSubtitle(String.format("%d shots", bucket.shots_count));

        DisplayShotsFragment fragment = DisplayShotsFragment.newInstance("DisplayOneBucketActivity");
        fragment.setSource(page -> DribbbleBucket.getOneBucketShots(bucket.id, page));

        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction transaction = fragmentManager.beginTransaction();
        transaction.replace(R.id.frame_layout, fragment);
        transaction.commit();
    }
}
