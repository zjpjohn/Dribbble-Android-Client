package com.lucas.freeshots.Dribbble;


import android.support.annotation.Nullable;

import com.lucas.freeshots.model.User;

import retrofit2.GsonConverterFactory;
import retrofit2.Retrofit;
import retrofit2.RxJavaCallAdapterFactory;
import retrofit2.http.GET;
import retrofit2.http.Query;
import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

public class DribbbleUser {
    private static Service service;

    static {
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(Dribbble.API_ADDRESS)
                .addConverterFactory(GsonConverterFactory.create())
                .addCallAdapterFactory(RxJavaCallAdapterFactory.create())
                .build();
        service = retrofit.create(Service.class);
    }

    public static @Nullable Observable<User> getAuthenticatedUser() {
        String accessTokenStr = Dribbble.getAccessTokenStr();
        return accessTokenStr.isEmpty()
                ? null
                : service.getAuthenticatedUser(accessTokenStr)
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread());
    }

    private interface Service {
        /**
         * Get the authenticated user
         */
        @GET("user")
        Observable<User> getAuthenticatedUser(@Query("access_token") String accessToken);
    }
}
