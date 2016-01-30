package com.lucas.freeshots;

import com.lucas.freeshots.common.Dribbble;
import com.lucas.freeshots.model.Comment;
import com.lucas.freeshots.model.Shot;

import java.util.List;

import retrofit2.http.GET;
import retrofit2.http.Headers;
import retrofit2.http.Path;
import retrofit2.http.Query;
import rx.Observable;

public interface DribbbleService {
    @Headers(Dribbble.AUTHORIZATION)
    @GET("shots")
    Observable<List<Shot>> listShots(@Query("page") int page, @Query("sort") String sort);

    @Headers(Dribbble.AUTHORIZATION)
    @GET("shots/{id}/comments")
    Observable<List<Comment>> getComment(@Path("id") int id);
}
