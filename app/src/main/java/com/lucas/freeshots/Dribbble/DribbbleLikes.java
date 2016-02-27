package com.lucas.freeshots.Dribbble;


import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Retrofit;
import retrofit2.http.DELETE;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Path;
import retrofit2.http.Query;

public class DribbbleLikes {

    private static Service service;

    static {
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(Dribbble.API_ADDRESS)
                .build();
        service = retrofit.create(Service.class);
    }

    public static Call<ResponseBody> checkLikeShot(int id) {
        return service.checkLikeShot(id, Dribbble.getAccessTokenStr());
    }

    public static Call<ResponseBody> likeShot(int id) {
        return service.likeShot(id, Dribbble.getAccessTokenStr());
    }

    public static Call<ResponseBody> unlikeShot(int id) {
        return service.unlikeShot(id, Dribbble.getAccessTokenStr());
    }

    private interface Service {
        /**
         * Check if you like a shot
         */
        @GET("shots/{id}/like")
        Call<ResponseBody> checkLikeShot(@Path("id") int id, @Query("access_token") String accessToken);

        /**
         * Like a shot
         */
        @POST("shots/{id}/like")
        Call<ResponseBody> likeShot(@Path("id") int id, @Query("access_token") String accessToken);

        /**
         * Unlike a shot
         */
        @DELETE("shots/{id}/like")
        Call<ResponseBody> unlikeShot(@Path("id") int id, @Query("access_token") String accessToken);
    }
}
