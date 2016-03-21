package com.lucas.freeshots.Dribbble;


import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.lucas.freeshots.model.Comment;

import java.util.List;

import retrofit2.GsonConverterFactory;
import retrofit2.Retrofit;
import retrofit2.RxJavaCallAdapterFactory;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Path;
import retrofit2.http.Query;
import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

public class DribbbleComment {

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
     * 得到某个shot的comment
     * @param id 要下载Comment的Shot的id
     */
    public static @Nullable Observable<Comment> getComment(int id, int page) {
        String accessTokenStr = Dribbble.getAccessTokenStr();
        return accessTokenStr.isEmpty()
                ? null
                : service.getComment(id, accessTokenStr, page)
                .compose(new Dribbble.Transformer<>());
    }

    /**
     * 得到某个shot的comment
     * @param id 要下载Comment的Shot的id
     */
    public static @Nullable Observable<Comment> createComment(int id, @NonNull String comment) {
        String accessTokenStr = Dribbble.getAccessTokenStr();
        return accessTokenStr.isEmpty()
                ? null
                : service.createComment(id, accessTokenStr, comment)
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread());
    }

    private interface Service {
        @GET("shots/{id}/comments")
        Observable<List<Comment>> getComment(@Path("id") int id,
                                             @Query("access_token") String accessToken,
                                             @Query("page") int page);

        @POST("shots/{id}/comments")
        Observable<Comment> createComment(@Path("id") int id,
                                          @Query("access_token") String accessToken,
                                          @Query("body") String comment);
    }
}
