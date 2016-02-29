package com.lucas.freeshots.common;


import android.content.Context;
import android.content.SharedPreferences;
import android.support.annotation.NonNull;

import com.lucas.freeshots.Dribbble.Dribbble;

public class Common {

    /******************************************************************************/

    // SharedPreferences 文件的名称
    public static final String SHARED_PREFERENCES_NAME = "FreeShotsSharedPreferences";
    public static final String SP_KEY_ACCESS_TOKEN = "spKeyAccessToken";

    public static SharedPreferences getSharedPreferences(Context context) {
        return context.getSharedPreferences(SHARED_PREFERENCES_NAME, Context.MODE_PRIVATE);
    }

    public static void putAccessTokenStrToSharedPreferences(Context context, String accessTokenStr) {
        SharedPreferences.Editor editor = getSharedPreferences(context).edit();
        editor.putString(SP_KEY_ACCESS_TOKEN, accessTokenStr);
        editor.apply();
    }

    public static @NonNull String getAccessTokenStrFromSharedPreferences(Context context) {
        return getSharedPreferences(context).getString(SP_KEY_ACCESS_TOKEN, "");
    }

    /******************************************************************************/

    public static final String LOGIN_ACTION = "com.lucas.freeshots.login.action";
    public static final String LOGOUT_ACTION = "com.lucas.freeshots.logout.action";

    /******************************************************************************/

//    private static boolean isLogin = false; // 登录状态，是否登录
//
//    public static synchronized void setLoginState(boolean isLogin) {
//        Common.isLogin = isLogin;
//    }

    /**
     * 判断是否登录
     * @return
     */
    public static synchronized boolean isLogin() {
        return !Dribbble.getAccessTokenStr().isEmpty();
    }

    /******************************************************************************/
}
