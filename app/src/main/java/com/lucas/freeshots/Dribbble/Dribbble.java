package com.lucas.freeshots.Dribbble;


import java.util.List;

import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Func1;
import rx.schedulers.Schedulers;

public class Dribbble {

    public static final String API_ADDRESS = "https://api.dribbble.com/v1/";

    public static final String CLIENT_ID
            = "3b1997c2649543412229bc2ed1321748be9c2a57ab0d0b4f55510bcb7437d363";

    public static final String CLIENT_SECRET
            = "9dca070bad796da3e2a983a39a7a5eb2165806e461852881808aa5b36e121c74";

    /*
     * An unguessable random string.
     * It is used to protect against cross-site request forgery attacks.
     */
    public static final String STATE = new StringBuilder(CLIENT_SECRET).reverse().toString();

    public static final String REDIRECT_URI = "freeshots://dribbble-auth-callback";

    private static String accessToken
            = "25fbfa9133a464b3ee7edc444abf3ecb4137d3f4b9834c0f342b1f8685cf07cd";

    public static synchronized void setAccessTokenStr(String newAccessToken) {
        accessToken = newAccessToken;
    }

    public static synchronized String getAccessTokenStr() {
        return accessToken;
    }

    // GET /shots的排序依据
    public static final String SHOT_SORT_BY_COMMENTS = "comments";
    public static final String SHOT_SORT_BY_RECENT = "recent";
    public static final String SHOT_SORT_BY_VIEWS = "views";

    /**
     * @param <T> 持有R类型的List
     * @param <R>
     */
    static class Transformer<T extends List, R> implements Observable.Transformer<T, R> {

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
}
