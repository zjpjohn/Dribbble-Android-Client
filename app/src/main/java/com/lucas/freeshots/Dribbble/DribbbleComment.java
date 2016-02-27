package com.lucas.freeshots.Dribbble;


import com.lucas.freeshots.model.Comment;

import java.util.List;

import retrofit2.GsonConverterFactory;
import retrofit2.Retrofit;
import retrofit2.RxJavaCallAdapterFactory;
import retrofit2.http.GET;
import retrofit2.http.Path;
import retrofit2.http.Query;
import rx.Observable;

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
    public static Observable<Comment> getComment(int id, int page) {
        return service.getComment(id, Dribbble.getAccessTokenStr(), page)
                .compose(new Dribbble.Transformer<>());
    }

    private interface Service {
        @GET("shots/{id}/comments")
        Observable<List<Comment>> getComment(@Path("id") int id,
                                             @Query("access_token") String accessToken,
                                             @Query("page") int page);
    }
}
