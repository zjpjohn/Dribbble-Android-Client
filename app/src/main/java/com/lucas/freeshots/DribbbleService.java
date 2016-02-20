package com.lucas.freeshots;

import com.lucas.freeshots.common.Dribbble;
import com.lucas.freeshots.model.Bucket;
import com.lucas.freeshots.model.Comment;
import com.lucas.freeshots.model.Likes;
import com.lucas.freeshots.model.Shot;

import java.util.List;

import retrofit2.http.GET;
import retrofit2.http.Headers;
import retrofit2.http.POST;
import retrofit2.http.PUT;
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

    /**
     * 得到当前登录用户的buckets
     */
    @Headers(Dribbble.AUTHORIZATION)
    @GET("user/buckets")
    Observable<List<Bucket>> listMyBuckets(@Query("page") int page);

    /**
     * 得到一个bucket中的shots
     * @param id bucket id
     */
    @Headers(Dribbble.AUTHORIZATION)
    @GET("buckets/{id}/shots")
    Observable<List<Shot>> listOneBucketShots(@Path("id") int id, @Query("page") int page);

    /**
     * 新建一个bucket
     */
    @Headers(Dribbble.AUTHORIZATION)
    @POST("buckets")
    Observable<Bucket> addOneBucket(@Query("name") String name, @Query("description") String description);

    /**
     * 删除一个bucket
     */
    @Headers(Dribbble.AUTHORIZATION)
    @PUT("buckets/{id}")
    void deleteOneBucket(@Path("id") int id);

    /**
     * 得到某个shot的comment
     * @param id shot id
     */
    @Headers(Dribbble.AUTHORIZATION)
    @GET("shots/{id}/comments")
    Observable<List<Comment>> getComment(@Path("id") int id, @Query("page") int page);


//    /**
//     * Check if you like a shot
//     */
//    @Headers(Dribbble.AUTHORIZATION)
//    @GET("shots/{id}/like")
//    Call<ResponseBody> checkLikeShot(@Path("id") int id);
//
//    /**
//     * Like a shot
//     */
//    @Headers(Dribbble.AUTHORIZATION)
//    @POST("shots/{id}/like")
//    Call<ResponseBody> likeShot(@Path("id") int id);
//
//    /**
//     * Unlike a shot
//     */
//    @Headers(Dribbble.AUTHORIZATION)
//    @DELETE("shots/{id}/like")
//    Call<ResponseBody> unlikeShot(@Path("id") int id);
}
