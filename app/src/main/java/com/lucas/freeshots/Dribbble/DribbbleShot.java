package com.lucas.freeshots.Dribbble;


import android.support.annotation.Nullable;

import com.lucas.freeshots.model.Likes;
import com.lucas.freeshots.model.Shot;

import java.util.List;

import retrofit2.GsonConverterFactory;
import retrofit2.Retrofit;
import retrofit2.RxJavaCallAdapterFactory;
import retrofit2.http.GET;
import retrofit2.http.Query;
import rx.Observable;
import rx.functions.Func1;

public class DribbbleShot {

    private static Service service;

    static {
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(Dribbble.API_ADDRESS)
                .addConverterFactory(GsonConverterFactory.create())
                .addCallAdapterFactory(RxJavaCallAdapterFactory.create())
                .build();
        service = retrofit.create(Service.class);
    }

    /**
     * 取公共shots，没有登录也可以取，所以直接就用 Token READONLY_ACCESS_TOKEN 取了。
     * @param page
     * @param sort
     * @return
     */
    public static Observable<Shot> getShots(int page, String sort) {
        return service.getShots(Dribbble.READONLY_ACCESS_TOKEN, page, sort).compose(new Dribbble.Transformer<>());
    }

    /**
     * 取当前登录用户的shots
     * @param page
     * @return Observable<Shot> or null（没有登录）
     */
    public static @Nullable Observable<Shot> getMyShots(int page) {
        String accessTokenStr = Dribbble.getAccessTokenStr();
        return accessTokenStr.isEmpty()
                ? null
                : service.getMyShots(accessTokenStr, page).compose(new Dribbble.Transformer<>());
    }

    /**
     * 取当前登录用户的liked 的 shots
     * @param page
     * @return Observable<Shot> or null（没有登录）
     */
    public static @Nullable Observable<Shot> getLikesShots(int page) {
        String accessTokenStr = Dribbble.getAccessTokenStr();
        return accessTokenStr.isEmpty()
                ? null
                : service.getLikesShots(accessTokenStr, page)
                        .compose(new Dribbble.Transformer<List<Likes>, Likes>())
                        .map(new Func1<Likes, Shot>() {
                            @Override
                            public Shot call(Likes likes) {
                                return likes.shot;
                            }
                        });
    }

    /**
     * 取当前登录用户following用户的 shots
     * @param page
     * @return Observable<Shot> or null（没有登录）
     */
    public static @Nullable Observable<Shot> getFollowingShots(int page) {
        String accessTokenStr = Dribbble.getAccessTokenStr();
        return accessTokenStr.isEmpty()
                ? null
                : service.getFollowingShots(accessTokenStr, page).compose(new Dribbble.Transformer<>());
    }

    private interface Service {
        @GET("shots")
        Observable<List<Shot>> getShots(@Query("access_token") String accessToken,
                                        @Query("page") int page,
                                        @Query("sort") String sort);

        @GET("user/shots")
        Observable<List<Shot>> getMyShots(@Query("access_token") String accessToken,
                                          @Query("page") int page);

        @GET("user/likes")
        Observable<List<Likes>> getLikesShots(@Query("access_token") String accessToken,
                                              @Query("page") int page);

        @GET("user/following/shots")
        Observable<List<Shot>> getFollowingShots(@Query("access_token") String accessToken,
                                                 @Query("page") int page);
    }
}
