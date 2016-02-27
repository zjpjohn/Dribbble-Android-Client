package com.lucas.freeshots.ui;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.NavigationView;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;

import com.lucas.freeshots.R;
import com.lucas.freeshots.common.Dribbble;

import java.io.Serializable;

import timber.log.Timber;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener, Serializable {

    private FragmentManager fragmentManager;

    private HomeFragment homeFragment;
    private DisplayShotsFragment likesFragment;
    private DisplayShotsFragment myShotsFragment;
    private BucketsFragment bucketsFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

//        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
//        fab.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
//                        .setAction("Action", null).show();
//            }
//        });

        //FrameLayout mainFrameLayout = Util.$(this, R.id.main_frame_layout);

        initDrawer(toolbar);

        homeFragment = HomeFragment.newInstance();
        likesFragment = DisplayShotsFragment.newInstance("likesFragment");
        likesFragment.setSource(Dribbble::downloadLikesShots);

        myShotsFragment = DisplayShotsFragment.newInstance("myShotsFragment");
        myShotsFragment.setSource(Dribbble::downloadMyShots);

        bucketsFragment = BucketsFragment.newInstance();

        fragmentManager = getSupportFragmentManager();

        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.add(R.id.main_frame_layout, homeFragment);
        fragmentTransaction.commit();

    //    initTabLayoutAndViewPager(toolbar);
    }

    /**
     * 初始化左侧的抽屉
     */
    private void initDrawer(Toolbar toolbar) {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this, drawer, toolbar,
                R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        View navHeader = View.inflate(this, R.layout.nav_header_main, null);
//        ViewGroup.LayoutParams params = new ViewGroup.LayoutParams(WRAP_CONTENT, WRAP_CONTENT);
//        params.height = Util.dp2px(this, 180);
//        navHeader.setLayoutParams(params);
        navigationView.addHeaderView(navHeader);

        ImageView userIconIv = (ImageView) navHeader.findViewById(R.id.user_icon);


        //String LOGIN_CALLBACK = "dribbble-auth-callback";
//        String loginUrl = "https://dribbble.com/oauth/authorize?client_id="
//                + Dribbble.CLIENT_ID
//                + "&redirect_uri=freeshots%3A%2F%2Fdribbble-auth-callback"
//                + "&scope=public+write;" ;
//                //+comment+upload";

        String loginUrl = String.format("https://dribbble.com/oauth/authorize?client_id=%s&redirect_uri=%s&scope=%s",
                                Dribbble.CLIENT_ID,
                                "freeshots://dribbble-auth-callback",
                                "public");  // +write+comment+upload

        Timber.e(loginUrl);

        userIconIv.setOnClickListener(v -> {

            Timber.e("ffffffffffffffffffffffffffffff");
            startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(loginUrl)));
            //DribbbleLoginActivity.startMyself(this);

//            Call<ResponseBody> call = DribbbleOAuth.authorize();
//            call.enqueue(new Callback<ResponseBody>() {
//                @Override
//                public void onResponse(Response<ResponseBody> response) {
//                    Timber.e(response.message());
//                    try {
//                        Timber.e(response.body().string());
//                    } catch (IOException e) {
//                        e.printStackTrace();
//                    }
//                }
//
//                @Override
//                public void onFailure(Throwable t) {
//                    Timber.e(t.getMessage());
//                }
//            });
        });
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.home) {
            FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
            fragmentTransaction.replace(R.id.main_frame_layout, homeFragment);
            fragmentTransaction.commit();
        } else if (id == R.id.my_buckets) {
            FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
            fragmentTransaction.replace(R.id.main_frame_layout, bucketsFragment);
            fragmentTransaction.commit();
        } else if (id == R.id.my_likes) {
            FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
            fragmentTransaction.replace(R.id.main_frame_layout, likesFragment);
            fragmentTransaction.commit();
        } else if (id == R.id.my_shots) {
            FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
            fragmentTransaction.replace(R.id.main_frame_layout, myShotsFragment);
            fragmentTransaction.commit();
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }
}
