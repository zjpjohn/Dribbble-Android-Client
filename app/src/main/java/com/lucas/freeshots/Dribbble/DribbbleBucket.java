package com.lucas.freeshots.Dribbble;


import android.support.annotation.NonNull;

import com.lucas.freeshots.model.Bucket;
import com.lucas.freeshots.model.Shot;

import java.util.List;

import retrofit2.GsonConverterFactory;
import retrofit2.Retrofit;
import retrofit2.RxJavaCallAdapterFactory;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.PUT;
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

    public static Observable<Bucket> getMyBuckets(int page) {
        return service.getMyBuckets(Dribbble.getAccessTokenStr(), page)
                .compose(new Dribbble.Transformer<>());
    }

    /**
     * 得到一个bucket中的shots
     * @param id bucket id
     */
    public static Observable<Shot> getOneBucketShots(int id, int page) {
        return service.getOneBucketShots(id, Dribbble.getAccessTokenStr(), page)
                .compose(new Dribbble.Transformer<>());
    }

    public static Observable<Bucket> addOneBucket(@NonNull String name, @NonNull String description) {
        return service.addOneBucket(Dribbble.getAccessTokenStr(), name, description)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread());
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
        @PUT("buckets/{id}")
        void deleteOneBucket(@Path("id") int id, @Query("access_token") String accessToken);
    }
}
