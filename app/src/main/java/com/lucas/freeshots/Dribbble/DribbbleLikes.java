package com.lucas.freeshots.Dribbble;


import com.lucas.freeshots.common.Dribbble;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Retrofit;
import retrofit2.http.DELETE;
import retrofit2.http.GET;
import retrofit2.http.Headers;
import retrofit2.http.POST;
import retrofit2.http.Path;

public class DribbbleLikes {

    private static Service service;

    static {
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(Dribbble.API_ADDRESS)
                .build();
        service = retrofit.create(Service.class);
    }

    public static Call<ResponseBody> checkLikeShot(int id) {
        return service.checkLikeShot(id);
    }

    public static Call<ResponseBody> likeShot(int id) {
        return service.likeShot(id);
    }

    public static Call<ResponseBody> unlikeShot(int id) {
        return service.unlikeShot(id);
    }

    private interface Service {
        /**
         * Check if you like a shot
         */
        @Headers(Dribbble.AUTHORIZATION)
        @GET("shots/{id}/like")
        Call<ResponseBody> checkLikeShot(@Path("id") int id);

        /**
         * Like a shot
         */
        @Headers(Dribbble.AUTHORIZATION)
        @POST("shots/{id}/like")
        Call<ResponseBody> likeShot(@Path("id") int id);

        /**
         * Unlike a shot
         */
        @Headers(Dribbble.AUTHORIZATION)
        @DELETE("shots/{id}/like")
        Call<ResponseBody> unlikeShot(@Path("id") int id);
    }
}