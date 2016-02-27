package com.lucas.freeshots.Dribbble;


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

    public static Observable<Shot> getShots(int page, String sort) {
        return service.getShots(Dribbble.getAccessTokenStr(), page, sort)
                .compose(new Dribbble.Transformer<>());
    }

    public static Observable<Shot> getMyShots(int page) {
        return service.getMyShots(Dribbble.getAccessTokenStr(), page)
                .compose(new Dribbble.Transformer<>());
    }

    public static Observable<Shot> getLikesShots(int page) {
        return service.getLikesShots(Dribbble.getAccessTokenStr(), page)
                .compose(new Dribbble.Transformer<List<Likes>, Likes>())
                .map(new Func1<Likes, Shot>() {
                    @Override
                    public Shot call(Likes likes) {
                        return likes.shot;
                    }
                });
    }

    public static Observable<Shot> getFollowingShots(int page) {
        return service.getFollowingShots(Dribbble.getAccessTokenStr(), page)
                .compose(new Dribbble.Transformer<>());
    }

    private interface Service {
        @GET("shots")
        Observable<List<Shot>> getShots(@Query("access_token") String accessToken,
                                        @Query("page") int page,
                                        @Query("sort") String sort);

        /**
         * 得到当前登录用户的shots
         */
        @GET("user/shots")
        Observable<List<Shot>> getMyShots(@Query("access_token") String accessToken,
                                          @Query("page") int page);

        /**
         * 得到当前登录用户like的shots
         */
        @GET("user/likes")
        Observable<List<Likes>> getLikesShots(@Query("access_token") String accessToken,
                                              @Query("page") int page);

        /**
         * 得到当前登录用户following用户的shots
         */
        @GET("user/following/shots")
        Observable<List<Shot>> getFollowingShots(@Query("access_token") String accessToken,
                                                 @Query("page") int page);
    }
}
