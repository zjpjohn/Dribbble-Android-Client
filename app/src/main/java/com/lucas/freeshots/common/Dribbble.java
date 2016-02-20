package com.lucas.freeshots.common;


import android.support.annotation.NonNull;

import com.lucas.freeshots.DribbbleService;
import com.lucas.freeshots.model.Bucket;
import com.lucas.freeshots.model.Comment;
import com.lucas.freeshots.model.Likes;
import com.lucas.freeshots.model.Shot;

import java.util.List;

import retrofit2.GsonConverterFactory;
import retrofit2.Retrofit;
import retrofit2.RxJavaCallAdapterFactory;
import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Func1;
import rx.schedulers.Schedulers;

public class Dribbble {

    public static final String API_ADDRESS = "https://api.dribbble.com/v1/";

    public static final String AUTHORIZATION
            = "Authorization: Bearer 25fbfa9133a464b3ee7edc444abf3ecb4137d3f4b9834c0f342b1f8685cf07cd";

    // GET /shots的排序依据
    public static final String SHOT_SORT_BY_COMMENTS = "comments";
    public static final String SHOT_SORT_BY_RECENT = "recent";
    public static final String SHOT_SORT_BY_VIEWS = "views";

    private static DribbbleService service;

    static {
        Retrofit retrofit = new Retrofit.Builder()
                .addConverterFactory(GsonConverterFactory.create())
                .addCallAdapterFactory(RxJavaCallAdapterFactory.create())
                .baseUrl(Dribbble.API_ADDRESS)
                .build();
        service = retrofit.create(DribbbleService.class);
    }

//    private static Observable.Transformer<List<Shot>, Shot> shotListToShotTransformer = new Observable.Transformer<List<Shot>, Shot>() {
//        @Override
//        public Observable<Shot> call(Observable<List<Shot>> observable) {
//            return observable
//                    .subscribeOn(Schedulers.io())
//                    .observeOn(AndroidSchedulers.mainThread())
//                    .flatMap(new Func1<List<Shot>, Observable<Shot>>() {
//                        @Override
//                        public Observable<Shot> call(List<Shot> shots) {
//                            return Observable.from(shots);
//                        }
//                    });
//        }
//    };

    /**
     *
     * @param <T> 持有R类型的List
     * @param <R>
     */
    private static class Transformer<T extends List, R> implements Observable.Transformer<T, R> {

        @Override
        public Observable<R> call(Observable<T> observable) {
            return observable
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .flatMap(new Func1<T, Observable<R>>() {
                        @Override
                        public Observable<R> call(T list) {
                            return Observable.from(list);
                        }
                    });
        }
    }

    public static Observable<Shot> downloadShots(int page, String sort) {
        return service.listShots(page, sort).compose(new Transformer<>());
    }

    public static Observable<Shot> downloadMyShots(int page) {
        return service.listMyShots(page).compose(new Transformer<>());
    }

    public static Observable<Shot> downloadLikesShots(int page) {
        return service.listLikesShots(page)
                .compose(new Transformer<List<Likes>, Likes>())
                .map(new Func1<Likes, Shot>() {
                    @Override
                    public Shot call(Likes likes) {
                        return likes.shot;
                    }
                });
    }

    public static Observable<Shot> downloadFollowingShots(int page) {
        return service.listFollowingShots(page).compose(new Transformer<>());
    }

    public static Observable<Bucket> downloadMyBuckets(int page) {
        return service.listMyBuckets(page).compose(new Transformer<>());
    }

    /**
     * 得到一个bucket中的shots
     * @param id bucket id
     */
    public static Observable<Shot> downloadOneBucketShots(int id, int page) {
        return service.listOneBucketShots(id, page).compose(new Transformer<>());
    }

    public static Observable<Bucket> addOneBucket(@NonNull String name, @NonNull String description) {
        return service.addOneBucket(name, description)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread());
    }

    /**
     *
     * @param id 要下载Comment的Shot的id
     */
    public static Observable<Comment> downloadComment(int id, int page) {
        return service.getComment(id, page).compose(new Transformer<>());
    }

//    public static Call<ResponseBody> checkLikeShot(int id) {
//        return service.checkLikeShot(id);
//    }
}
