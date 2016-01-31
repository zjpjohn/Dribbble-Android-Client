package com.lucas.freeshots.common;


import com.lucas.freeshots.DribbbleService;
import com.lucas.freeshots.model.Comment;
import com.lucas.freeshots.model.Shot;

import java.util.List;

import retrofit2.GsonConverterFactory;
import retrofit2.Retrofit;
import retrofit2.RxJavaCallAdapterFactory;
import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
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

    public static Observable<List<Shot>> downloadShots(int page, String sort) {
        return service.listShots(page, sort)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread());
    }

    public static Observable<List<Shot>> downloadFollowingShots(int page) {
        return service.listFollowingShots(page)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread());
    }

    /**
     *
     * @param id 要下载Comment的Shot的id
     */
    public static Observable<List<Comment>> downloadComment(int id) {
        return service.getComment(id)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread());
    }
}
