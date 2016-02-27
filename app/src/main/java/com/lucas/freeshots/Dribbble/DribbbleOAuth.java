package com.lucas.freeshots.Dribbble;


import com.lucas.freeshots.common.Dribbble;
import com.lucas.freeshots.model.AccessToken;

import retrofit2.GsonConverterFactory;
import retrofit2.Retrofit;
import retrofit2.RxJavaCallAdapterFactory;
import retrofit2.http.POST;
import retrofit2.http.Query;
import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

public class DribbbleOAuth {

    private static Service service;

    static {
        Retrofit retrofit = new Retrofit.Builder()
                .addConverterFactory(GsonConverterFactory.create())
                .addCallAdapterFactory(RxJavaCallAdapterFactory.create())
                .baseUrl(Dribbble.API_ADDRESS)
                .build();
        service = retrofit.create(Service.class);
    }

    public static Observable<AccessToken> getAccessToken(String code) {
        return service.getAccessToken(Dribbble.CLIENT_ID, Dribbble.CLIENT_SECRET, code, "")
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread());
    }

    private interface Service {
        @POST("https://dribbble.com/oauth/token")
        Observable<AccessToken> getAccessToken(@Query("client_id") String clientId,
                                                 @Query("client_secret") String clientSecret,
                                                 @Query("code") String code,
                                                 @Query("redirect_uri") String redirectUri);
    }


//    public static Call<ResponseBody> getAccessToken(String code) {
//        return service.getAccessToken(Dribbble.CLIENT_ID, Dribbble.CLIENT_SECRET, code, "");
//    }
//
//    private interface Service {
//        @POST("https://dribbble.com/oauth/token")
//        Call<ResponseBody> getAccessToken(@Query("client_id") String clientId,
//                                           @Query("client_secret") String clientSecret,
//                                           @Query("code") String code,
//                                           @Query("redirect_uri") String redirectUri);
//    }

}
