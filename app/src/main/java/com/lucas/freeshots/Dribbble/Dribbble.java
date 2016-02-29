package com.lucas.freeshots.Dribbble;


import java.util.List;

import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Func1;
import rx.schedulers.Schedulers;

public class Dribbble {

    public static final String API_ADDRESS = "https://api.dribbble.com/v1/";

    public static final String CLIENT_ID
            = "2e560b1af2d6522626e6ba9d0941ea32bc775be3ca2727ceb919647ce6a086bc";

    public static final String CLIENT_SECRET
            = "7aeb68a381ee2733d069533b0e0a04dc029433306539baa34936abfc84e4e894";

    public static final String READONLY_ACCESS_TOKEN
            = "58ec4fa5f75302ac89167d02d2f1ff8227496ce83684992f29d757daefbf6230";

    /*
     * An unguessable random string.
     * It is used to protect against cross-site request forgery attacks.
     */
    public static final String STATE = new StringBuilder(CLIENT_SECRET).reverse().toString();

    public static final String REDIRECT_URI = "freeshots://dribbble-auth-callback";

    private static String accessToken = "";

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
