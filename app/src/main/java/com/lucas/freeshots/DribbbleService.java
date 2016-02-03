package com.lucas.freeshots;

import com.lucas.freeshots.common.Dribbble;
import com.lucas.freeshots.model.Comment;
import com.lucas.freeshots.model.Likes;
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

    /**
     * 得到当前登录用户的shots
     */
    @Headers(Dribbble.AUTHORIZATION)
    @GET("user/shots")
    Observable<List<Shot>> listMyShots(@Query("page") int page);

    /**
     * 得到当前登录用户like的shots
     */
    @Headers(Dribbble.AUTHORIZATION)
    @GET("user/likes")
    Observable<List<Likes>> listLikesShots(@Query("page") int page);

    /**
     * 得到当前登录用户following用户的shots
     */
    @Headers(Dribbble.AUTHORIZATION)
    @GET("user/following/shots")
    Observable<List<Shot>> listFollowingShots(@Query("page") int page);

    @Headers(Dribbble.AUTHORIZATION)
    @GET("shots/{id}/comments")
    Observable<List<Comment>> getComment(@Path("id") int id);
}
