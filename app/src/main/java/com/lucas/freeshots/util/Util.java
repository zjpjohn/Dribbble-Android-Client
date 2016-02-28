package com.lucas.freeshots.util;

import android.app.Activity;
import android.content.Context;
import android.graphics.Point;
import android.support.annotation.IdRes;
import android.view.View;
import android.view.WindowManager;

public class Util {
    private final static String TAG = "Util";

    private static int screenWidth = -1;
    private static int screenHeight = -1;

    public static int getScreenWidth(Context context) {
        if(screenWidth != -1) {
            return screenWidth;
        }

        Point size = new Point();
        ((WindowManager) context.getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay().getSize(size);

        screenWidth = size.x;
        return screenWidth;
    }

    public static int getScreenHeight(Context context) {
        if(screenHeight != -1) {
            return screenHeight;
        }

        Point size = new Point();
        ((WindowManager) context.getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay().getSize(size);

        screenHeight = size.y;
        return screenHeight;
    }

    /**
     * 根据手机的分辨率从 dp 的单位 转成为 px(像素)
     * @param context
     * @param dpValue
     * @return
     */
    public static int dp2px(Context context, float dpValue) {
        final float scale = context.getResources().getDisplayMetrics().density;
        return (int) (dpValue * scale + 0.5f);

        //    Resources res = context.getResources();
        //    DisplayMetrics dm = res.getDisplayMetrics();
        //    return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 50, dm);
    }

    /**
     * 根据手机的分辨率从 px(像素) 的单位 转成为 dp
     * @param context
     * @param pxValue
     * @return
     */
    public static int px2dp(Context context, float pxValue) {
        final float scale = context.getResources().getDisplayMetrics().density;
        return (int) (pxValue / scale + 0.5f);
    }

    @SuppressWarnings("unchecked")
    public static <T extends View> T $(Activity activity, @IdRes int id){
        return (T) activity.findViewById(id);
    }

    @SuppressWarnings("unchecked")
    public static <T extends View> T $(View v, @IdRes int id){
        return (T) v.findViewById(id);
    }
}
