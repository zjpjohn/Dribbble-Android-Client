package com.lucas.freeshots.ui.activity;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.NavigationView;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.facebook.drawee.view.SimpleDraweeView;
import com.lucas.freeshots.Dribbble.Dribbble;
import com.lucas.freeshots.Dribbble.DribbbleShot;
import com.lucas.freeshots.Dribbble.DribbbleUser;
import com.lucas.freeshots.R;
import com.lucas.freeshots.common.Common;
import com.lucas.freeshots.model.User;
import com.lucas.freeshots.ui.fragment.BucketsFragment;
import com.lucas.freeshots.ui.fragment.DisplayShotsFragment;
import com.lucas.freeshots.ui.fragment.HomeFragment;
import com.lucas.freeshots.ui.ShowInfoAlertDialog;
import com.lucas.freeshots.util.Util;

import java.io.Serializable;

import rx.Observable;
import rx.Subscriber;
import timber.log.Timber;

import static android.view.ViewGroup.LayoutParams.WRAP_CONTENT;
import static com.lucas.freeshots.util.Util.$;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener, Serializable {

    private FragmentManager fragmentManager;

    private HomeFragment homeFragment;
    private DisplayShotsFragment likesFragment;
    private DisplayShotsFragment myShotsFragment;
    private BucketsFragment bucketsFragment;

    private SimpleDraweeView userIconIv;
    private TextView nameTv;
    private TextView userNameTv;

    private boolean isLoginning = false;  // 是否正在登录中

    private LoginBroadcastReceiver loginBroadcastReceiver = null;

    private class LoginBroadcastReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            onLogin();
        }
    }

    /**
     * 更新已登录用户的信息
     */
    private void updateAuthenticatedUserInfo() {
        Observable<User> observable = DribbbleUser.getAuthenticatedUser();
        if(observable == null) {
            Timber.e("login error");
            isLoginning = false;
            return;
        }

        observable.subscribe(new Subscriber<User>() {
            @Override
            public void onCompleted() {
                Timber.e("login success");
                isLoginning = false;
            }

            @Override
            public void onError(Throwable e) {
                Timber.e("login error. " + e.getMessage());
                isLoginning = false;
            }

            @Override
            public void onNext(User user) {
                if (user.avatar_url != null) {
                    userIconIv.setImageURI(Uri.parse(user.avatar_url));
                }

                nameTv.setText(user.name);
                userNameTv.setText(user.username);
            }
        });
    }

    /**
     * 重置用户信息
     */
    private void resetUserInfo() {
        userIconIv.setImageDrawable(getResources().getDrawable(R.mipmap.ic_launcher));
        nameTv.setText("未登录，点击登录");
        userNameTv.setText("未登录，点击登录");
    }

    /**
     * 响应登录成功
     */
    private void onLogin() {
        homeFragment = HomeFragment.newInstance();

        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.replace(R.id.main_frame_layout, homeFragment);

        /*
         * 调用 fragmentTransaction.commit() 会报如下异常
         * java.lang.IllegalStateException: Can not perform this action after onSaveInstanceState().
         * 登录过程需要打开浏览器，可能会导致onSaveInstanceState()的调用.
         * commitAllowingStateLoss() allows the commit to be executed after an activity's state is saved.
         */
        fragmentTransaction.commitAllowingStateLoss();

        updateAuthenticatedUserInfo();
    }

    /**
     * 响应登出成功
     */
    private void onLogOut() {
        Dribbble.setAccessTokenStr("");
        Common.putAccessTokenStrToSharedPreferences(this, "");

        homeFragment = HomeFragment.newInstance();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.replace(R.id.main_frame_layout, homeFragment);
        fragmentTransaction.commit();

        resetUserInfo();
    }

    private static final String TITLE_HOME = "Free Shots";
    private static final String TITLE_MY_BUCKETS = "My Buckets";
    private static final String TITLE_MY_LIKES = "My Likes";
    private static final String TITLE_MY_SHOTS = "My Shots";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setTitle(TITLE_HOME);
        setSupportActionBar(toolbar);
        initDrawer(toolbar);

        String accessTokenStr = Common.getAccessTokenStrFromSharedPreferences(this);
        Dribbble.setAccessTokenStr(accessTokenStr);
        if (!accessTokenStr.isEmpty()) {
            updateAuthenticatedUserInfo();
        } else {
            resetUserInfo();
        }

        loginBroadcastReceiver = new LoginBroadcastReceiver();
        registerReceiver(loginBroadcastReceiver, new IntentFilter(Common.LOGIN_ACTION));

        homeFragment = HomeFragment.newInstance();

        likesFragment = DisplayShotsFragment.newInstance("likesFragment");
        likesFragment.setSource(DribbbleShot::getLikesShots);

        myShotsFragment = DisplayShotsFragment.newInstance("myShotsFragment");
        myShotsFragment.setSource(DribbbleShot::getMyShots);

        bucketsFragment = BucketsFragment.newInstance();

        fragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.add(R.id.main_frame_layout, homeFragment);
        fragmentTransaction.commit();
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
        ViewGroup.LayoutParams params = new ViewGroup.LayoutParams(WRAP_CONTENT, WRAP_CONTENT);
        params.height = Util.dp2px(this, 180);
        navHeader.setLayoutParams(params);
        navigationView.addHeaderView(navHeader);

        View.OnClickListener listener = v -> {
            if(Common.isLogin()) {
                String logout = "LOGOUT";
                String cancel = "CANCEL";
                new ShowInfoAlertDialog(this, "退出登录", "确认退出登录？", null,
                        new String[]{ logout, cancel }, actionName -> {
                            if(actionName.equals(logout)) {
                                onLogOut();
                            }
                        }).show();
            } else if(!isLoginning){ // 未登录且未在登录中
                isLoginning = true;
                String loginUrl = String.format(
                        "https://dribbble.com/oauth/authorize?client_id=%s&redirect_uri=%s&scope=%s&state=%s",
                        Dribbble.CLIENT_ID,
                        Dribbble.REDIRECT_URI,
                        "public+write+comment+upload",
                        Dribbble.STATE);

                // 未登录，打开浏览器登录
                startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(loginUrl)));
            }
        };

        userIconIv = $(navHeader, R.id.user_icon);
        nameTv = $(navHeader, R.id.name);
        userNameTv = $(navHeader, R.id.user_name);

        userIconIv.setOnClickListener(listener);
        nameTv.setOnClickListener(listener);
        userNameTv.setOnClickListener(listener);
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

    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.home) {
            FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
            fragmentTransaction.replace(R.id.main_frame_layout, homeFragment);
            fragmentTransaction.commit();
            onShowHomeFragment();
        } else if (id == R.id.my_buckets) {
            if(Common.isLogin()) {
                FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
                fragmentTransaction.replace(R.id.main_frame_layout, bucketsFragment);
                fragmentTransaction.commit();
                onShowMyBucketsFragment();
            } else {
                // 没有登录，无法打开 my buckets
                Toast.makeText(this, "没有登录，无法打开 my buckets", Toast.LENGTH_LONG).show();
            }
        } else if (id == R.id.my_likes) {
            if(Common.isLogin()) {
                FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
                fragmentTransaction.replace(R.id.main_frame_layout, likesFragment);
                fragmentTransaction.commit();
                onShowMyLikesFragment();
            }else {
                // 没有登录，无法打开 my likes
                Toast.makeText(this, "没有登录，无法打开 my likes", Toast.LENGTH_LONG).show();
            }
        } else if (id == R.id.my_shots) {
            if(Common.isLogin()) {
                FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
                fragmentTransaction.replace(R.id.main_frame_layout, myShotsFragment);
                fragmentTransaction.commit();
                onShowMyShotsFragment();
            } else {
                // 没有登录，无法打开 my shots
                Toast.makeText(this, "没有登录，无法打开 my shots", Toast.LENGTH_LONG).show();
            }
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    private void onShowHomeFragment() {
        ActionBar actionBar = getSupportActionBar();
        if(actionBar != null) {
            actionBar.setTitle(TITLE_HOME);
        }
    }

    private void onShowMyBucketsFragment() {
        ActionBar actionBar = getSupportActionBar();
        if(actionBar != null) {
            actionBar.setTitle(TITLE_MY_BUCKETS);
        }
    }

    private void onShowMyLikesFragment() {
        ActionBar actionBar = getSupportActionBar();
        if(actionBar != null) {
            actionBar.setTitle(TITLE_MY_LIKES);
        }
    }

    private void onShowMyShotsFragment() {
        ActionBar actionBar = getSupportActionBar();
        if(actionBar != null) {
            actionBar.setTitle(TITLE_MY_SHOTS);
        }
    }

    @Override
    protected void onDestroy() {
        if(loginBroadcastReceiver != null) {
            unregisterReceiver(loginBroadcastReceiver);
        }

        super.onDestroy();
    }
}
