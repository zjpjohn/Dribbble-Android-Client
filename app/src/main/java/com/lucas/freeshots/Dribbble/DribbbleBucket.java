package com.lucas.freeshots.Dribbble;


import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.lucas.freeshots.model.Bucket;
import com.lucas.freeshots.model.Shot;

import java.util.List;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.GsonConverterFactory;
import retrofit2.Retrofit;
import retrofit2.RxJavaCallAdapterFactory;
import retrofit2.http.DELETE;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Path;
import retrofit2.http.Query;
import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

public class DribbbleBucket {
    private static Service service;

    static {
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(Dribbble.API_ADDRESS)
                .addConverterFactory(GsonConverterFactory.create())
                .addCallAdapterFactory(RxJavaCallAdapterFactory.create())
                .build();
        service = retrofit.create(Service.class);
    }

    public static @Nullable Observable<Bucket> getMyBuckets(int page) {
        String accessTokenStr = Dribbble.getAccessTokenStr();
        return accessTokenStr.isEmpty()
                ? null
                : service.getMyBuckets(accessTokenStr, page)
                        .compose(new Dribbble.Transformer<>());
    }

    /**
     * 得到一个bucket中的shots
     * @param id bucket id
     */
    public static @Nullable Observable<Shot> getOneBucketShots(int id, int page) {
        String accessTokenStr = Dribbble.getAccessTokenStr();
        return accessTokenStr.isEmpty()
                ? null
                : service.getOneBucketShots(id, accessTokenStr, page)
                        .compose(new Dribbble.Transformer<>());
    }

    public static @Nullable Observable<Bucket> addOneBucket(@NonNull String name, @NonNull String description) {
        String accessTokenStr = Dribbble.getAccessTokenStr();
        return accessTokenStr.isEmpty()
                ? null
                : service.addOneBucket(accessTokenStr, name, description)
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread());
    }

    public static @Nullable Call<ResponseBody> deleteOneBucket(int id) {
        String accessTokenStr = Dribbble.getAccessTokenStr();
        return accessTokenStr.isEmpty() ? null : service.deleteOneBucket(id, accessTokenStr);
    }

    private interface Service {
        /**
         * 得到当前登录用户的buckets
         */
        @GET("user/buckets")
        Observable<List<Bucket>> getMyBuckets(@Query("access_token") String accessToken,
                                              @Query("page") int page);

        /**
         * 得到一个bucket中的shots
         * @param id bucket id
         */
        @GET("buckets/{id}/shots")
        Observable<List<Shot>> getOneBucketShots(@Path("id") int id,
                                                 @Query("access_token") String accessToken,
                                                 @Query("page") int page);

        /**
         * 新建一个bucket
         */
        @POST("buckets")
        Observable<Bucket> addOneBucket(@Query("access_token") String accessToken,
                                        @Query("name") String name,
                                        @Query("description") String description);

        /**
         * 删除一个bucket
         * @param id bucket id
         */
        @DELETE("buckets/{id}")
        Call<ResponseBody> deleteOneBucket(@Path("id") int id, @Query("access_token") String accessToken);
    }
}
