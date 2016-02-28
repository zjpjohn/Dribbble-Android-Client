/*
 * Copyright 2015 Google Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.lucas.freeshots.ui;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.widget.Toast;

import com.lucas.freeshots.Dribbble.Dribbble;
import com.lucas.freeshots.Dribbble.DribbbleOAuth;
import com.lucas.freeshots.R;
import com.lucas.freeshots.model.AccessToken;

import rx.Subscriber;
import timber.log.Timber;

/**
 * 此Activity用来相应登录时从浏览器返回的数据。
 */
public class DribbbleLoginActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dribbble_login);

        onAuthCallback(getIntent());
    }

    @Override
    public void onBackPressed() {
        // 什么也不干，屏蔽返回按键。
    }

    private void onSuccess(String msg) {
        Timber.e(msg);
        Toast.makeText(this, msg, Toast.LENGTH_LONG).show();
        // TODO: 登录成功后取登录用户信息。
        finish();
    }

    private void onFailure(String msg) {
        Timber.e(msg);
        Toast.makeText(this, msg, Toast.LENGTH_LONG).show();
        finish();
    }

    private void onAuthCallback(Intent intent) {
        Uri uri = intent.getData();
        if(uri == null) {
            onFailure("login failed, don't get uri");
            return;
        }

        String code = uri.getQueryParameter("code");
        if(code != null) {
            Timber.e("code: " + code);
            getAccessToken(code);
        } else {
            onFailure("login failed, don't get code");
        }
    }

    private void getAccessToken(String code) {
        DribbbleOAuth.getAccessToken(code).subscribe(new Subscriber<AccessToken>() {
            @Override
            public void onCompleted() {
                onSuccess("login success");
            }

            @Override
            public void onError(Throwable e) {
                onFailure("login failed: " + e.getMessage());
            }

            @Override
            public void onNext(AccessToken accessToken) {
                Timber.e("New Access Token: " + accessToken.toString());
                Dribbble.setAccessTokenStr(accessToken.access_token);
            }
        });
    }
}
