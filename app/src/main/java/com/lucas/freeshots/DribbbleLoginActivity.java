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

package com.lucas.freeshots;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.lucas.freeshots.Dribbble.DribbbleOAuth;
import com.lucas.freeshots.common.Dribbble;
import com.lucas.freeshots.model.AccessToken;

import rx.Subscriber;
import timber.log.Timber;

public class DribbbleLoginActivity extends Activity {

    public static void startMyself(Context context) {
        Intent intent = new Intent(context, DribbbleLoginActivity.class);
        context.startActivity(intent);
    }

    boolean isDismissing = false;
    private ViewGroup container;
    private TextView message;
    private Button login;
    private ProgressBar loading;
 //   private DribbblePrefs dribbblePrefs;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dribbble_login);
//        FabDialogMorphSetup.setupSharedEelementTransitions(this, container,
//                getResources().getDimensionPixelSize(R.dimen.dialog_corners));
//
//        container = (ViewGroup) findViewById(R.id.container);
//        message = (TextView) findViewById(R.id.login_message);
//        login = (Button) findViewById(R.id.login);
//        loading = (ProgressBar) findViewById(R.id.loading);
//        loading.setVisibility(View.GONE);
//        dribbblePrefs = DribbblePrefs.get(this);

        checkAuthCallback(getIntent());
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        checkAuthCallback(intent);
    }

    public void doLogin(View view) {
       // showLoading();
        //dribbblePrefs.login(DribbbleLogin.this);
    }

    public void dismiss(View view) {
        isDismissing = true;
        setResult(Activity.RESULT_CANCELED);
        //finishAfterTransition();
    }

    @Override
    public void onBackPressed() {
        dismiss(null);
    }

    private void showLoading() {
        //TransitionManager.beginDelayedTransition(container);
        message.setVisibility(View.GONE);
        login.setVisibility(View.GONE);
        loading.setVisibility(View.VISIBLE);
    }

    private void showLogin() {
        //TransitionManager.beginDelayedTransition(container);
        message.setVisibility(View.VISIBLE);
        login.setVisibility(View.VISIBLE);
        loading.setVisibility(View.GONE);
    }

    private void checkAuthCallback(Intent intent) {
//        if (intent != null
//                && intent.getData() != null
//                && !TextUtils.isEmpty(intent.getData().getAuthority())
//                && DribbblePrefs.LOGIN_CALLBACK.equals(intent.getData().getAuthority())) {
//            showLoading();
//            getAccessToken(intent.getData().getQueryParameter("code"));
//        }

        Uri uri = intent.getData();
        if(uri != null) {
            Timber.e("uri: " + uri.toString());
            String code = uri.getQueryParameter("code");
            if(code != null) {
                Timber.e("code: " + code);
                getAccessToken(code);
            } else {
                Timber.e("code == null");
                Toast.makeText(this, "login failed", Toast.LENGTH_LONG).show();
            }
        } else {
            Timber.e("uri == null");
            Toast.makeText(this, "login failed", Toast.LENGTH_LONG).show();
        }
    }

    private void getAccessToken(String code) {
        DribbbleOAuth.getAccessToken(code).subscribe(new Subscriber<AccessToken>() {
            @Override
            public void onCompleted() {
                Timber.e("login success");
                Toast.makeText(DribbbleLoginActivity.this, "login success", Toast.LENGTH_LONG).show();
            }

            @Override
            public void onError(Throwable e) {
                Timber.e(e.getMessage());
                Toast.makeText(DribbbleLoginActivity.this, "login failed: " + e.getMessage(), Toast.LENGTH_LONG).show();
            }

            @Override
            public void onNext(AccessToken accessToken) {
                Timber.e("New Access Token: " + accessToken.toString());
                Dribbble.setAccessToken(accessToken);
            }
        });

//
//        DribbbleOAuth.getAccessToken(code).enqueue(new Callback<ResponseBody>() {
//            @Override
//            public void onResponse(Response<ResponseBody> response) {
//                Timber.e(response.message());
//                try {
//                    Timber.e(response.body().string());
//                } catch (IOException e) {
//                    e.printStackTrace();
//                }
//            }
//
//            @Override
//            public void onFailure(Throwable t) {
//                Timber.e(t.getMessage());
//            }
//        });



//        RestAdapter restAdapter = new RestAdapter.Builder()
//                .setEndpoint(DribbbleAuthService.ENDPOINT)
//                .build();
//
//        DribbbleAuthService dribbbleAuthApi = restAdapter.create((DribbbleAuthService.class));
//
//        dribbbleAuthApi.getAccessToken(BuildConfig.DRIBBBLE_CLIENT_ID,
//                BuildConfig.DRIBBBLE_CLIENT_SECRET,
//                code, "", new Callback<AccessToken>() {
//                    @Override
//                    public void success(AccessToken accessToken, Response response) {
//                        dribbblePrefs.setAccessToken(accessToken.access_token);
//                        showLoggedInUser();
//                        setResult(Activity.RESULT_OK);
//                        finishAfterTransition();
//                    }
//
//                    @Override
//                    public void failure(RetrofitError error) {
//                        Log.e(getClass().getCanonicalName(), error.getMessage(), error);
//                        // TODO snackbar?
//                        Toast.makeText(getApplicationContext(), "Log in failed: " + error
//                                .getResponse()
//                                .getStatus(), Toast.LENGTH_LONG).show();
//                        showLogin();
//                    }
//                });
    }

//    private void showLoggedInUser() {
//        Gson gson = new GsonBuilder()
//                .setDateFormat(DribbbleService.DATE_FORMAT)
//                .create();
//
//        RestAdapter restAdapter = new RestAdapter.Builder()
//                .setEndpoint(DribbbleService.ENDPOINT)
//                .setConverter(new GsonConverter(gson))
//                .setRequestInterceptor(new AuthInterceptor(dribbblePrefs.getAccessToken()))
//                .build();
//
//        DribbbleService dribbbleApi = restAdapter.create((DribbbleService.class));
//        dribbbleApi.getAuthenticatedUser(new Callback<User>() {
//            @Override
//            public void success(User user, Response response) {
//                dribbblePrefs.setLoggedInUser(user);
//                Toast confirmLogin = new Toast(getApplicationContext());
//                View v = LayoutInflater.from(DribbbleLogin.this).inflate(R.layout
//                        .toast_logged_in_confirmation, null, false);
//                ((TextView) v.findViewById(R.id.name)).setText(user.name);
//                // need to use app context here as the activity will be destroyed shortly
//                Glide.with(getApplicationContext())
//                        .load(user.avatar_url)
//                        .placeholder(R.drawable.ic_player)
//                        .transform(new CircleTransform(getApplicationContext()))
//                        .into((ImageView) v.findViewById(R.id.avatar));
//                v.findViewById(R.id.scrim).setBackground(ScrimUtil.makeCubicGradientScrimDrawable
//                        (ContextCompat.getColor(DribbbleLogin.this, R.color.scrim),
//                                5, Gravity.BOTTOM));
//                confirmLogin.setView(v);
//                confirmLogin.setGravity(Gravity.BOTTOM | Gravity.FILL_HORIZONTAL, 0, 0);
//                confirmLogin.setDuration(Toast.LENGTH_LONG);
//                confirmLogin.show();
//            }
//
//            @Override
//            public void failure(RetrofitError error) {
//            }
//        });
//    }

    private void forceSharedElementLayout() {
        int widthSpec = View.MeasureSpec.makeMeasureSpec(container.getWidth(),
                View.MeasureSpec.EXACTLY);
        int heightSpec = View.MeasureSpec.makeMeasureSpec(container.getHeight(),
                View.MeasureSpec.EXACTLY);
        container.measure(widthSpec, heightSpec);
        container.layout(container.getLeft(), container.getTop(), container.getRight(), container
                .getBottom());
    }

//    private SharedElementCallback sharedElementEnterCallback = new SharedElementCallback() {
//        @Override
//        public View onCreateSnapshotView(Context context, Parcelable snapshot) {
//            // grab the saved fab snapshot and pass it to the below via a View
//            View view = new View(context);
//            final Bitmap snapshotBitmap = getSnapshot(snapshot);
//            if (snapshotBitmap != null) {
//                view.setBackground(new BitmapDrawable(context.getResources(), snapshotBitmap));
//            }
//            return view;
//        }
//
//        @Override
//        public void onSharedElementStart(List<String> sharedElementNames,
//                                         List<View> sharedElements,
//                                         List<View> sharedElementSnapshots) {
//            // grab the fab snapshot and fade it out/in (depending on if we are entering or exiting)
//            for (int i = 0; i < sharedElements.size(); i++) {
//                if (sharedElements.get(i) == container) {
//                    View snapshot = sharedElementSnapshots.get(i);
//                    BitmapDrawable fabSnapshot = (BitmapDrawable) snapshot.getBackground();
//                    fabSnapshot.setBounds(0, 0, snapshot.getWidth(), snapshot.getHeight());
//                    container.getOverlay().clear();
//                    container.getOverlay().add(fabSnapshot);
//                    if (!isDismissing) {
//                        // fab -> login: fade out the fab snapshot
//                        ObjectAnimator.ofInt(fabSnapshot, "alpha", 0).setDuration(100).start();
//                    } else {
//                        // login -> fab: fade in the fab snapshot toward the end of the transition
//                        fabSnapshot.setAlpha(0);
//                        ObjectAnimator fadeIn = ObjectAnimator.ofInt(fabSnapshot, "alpha", 255)
//                                .setDuration(150);
//                        fadeIn.setStartDelay(150);
//                        fadeIn.start();
//                    }
//                    forceSharedElementLayout();
//                    break;
//                }
//            }
//        }
//
//        private Bitmap getSnapshot(Parcelable parcel) {
//            if (parcel instanceof Bitmap) {
//                return (Bitmap) parcel;
//            } else if (parcel instanceof Bundle) {
//                Bundle bundle = (Bundle) parcel;
//                // see SharedElementCallback#onCaptureSharedElementSnapshot
//                return (Bitmap) bundle.getParcelable("sharedElement:snapshot:bitmap");
//            }
//            return null;
//        }
//    };
}
