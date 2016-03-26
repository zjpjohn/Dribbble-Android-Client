package com.lucas.freeshots.ui.activity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.widget.Toast;

import com.lucas.freeshots.R;
import com.lucas.freeshots.ui.fragment.BucketsFragment;

import timber.log.Timber;

import static com.lucas.freeshots.util.Util.$;

public class AddShotToBucketActivity extends AppCompatActivity implements BucketsFragment.OnAddShotToBucket {

    public static void startMyself(Context context, int shotId) {
        Intent intent = new Intent(context, AddShotToBucketActivity.class);
        intent.putExtra("shotId", shotId);
        context.startActivity(intent);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_shot_to_bucket);

        Toolbar toolbar = $(this, R.id.toolbar);
        toolbar.setTitle("Add a shot to a bucket");
        setSupportActionBar(toolbar);
        toolbar.setNavigationIcon(R.mipmap.abc_ic_action_back);
        toolbar.setNavigationOnClickListener(view -> finish());

        int shotId = getIntent().getIntExtra("shotId", -1);
        if(shotId == -1) {
            Timber.e("shot id 不合法, shotId = " + shotId);
            Toast.makeText(this, "shot id 不合法, shotId = " + shotId, Toast.LENGTH_LONG).show();
            return;
        }

        FragmentManager fragmentManager = getSupportFragmentManager();
        BucketsFragment fragment = (BucketsFragment) fragmentManager.findFragmentById(R.id.choose_bucket);
        fragment.setAddMode(shotId);
    }

    @Override
    public void onSuccess() {
        // 添加成功，退出。
        finish();
    }

    @Override
    public void onFailed() {
        // do nothing
    }
}
