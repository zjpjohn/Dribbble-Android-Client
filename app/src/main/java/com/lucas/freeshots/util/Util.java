package com.lucas.freeshots.util;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Point;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Environment;
import android.support.annotation.IdRes;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.Toast;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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

    public static String getUniqueFileName() {
        SimpleDateFormat format = new SimpleDateFormat("yyyyMMddhhmmss", Locale.CHINA);
        return "F" + format.format(new Date());
    }

    public static File getEmptyFile(Context context) {
        return getFileInSdcardByName(context, getUniqueFileName(), true);
    }

    public static File getFileInSdcardByName(Context context,
                                             String fileName, boolean createIfNoeExists) {
        String state = Environment.getExternalStorageState();
        if(!state.equals(Environment.MEDIA_MOUNTED)) {
            Log.e("MBM", "SD卡没有插入或没有写权限");
            Toast.makeText(context, "SD卡没有插入或没有写权限", Toast.LENGTH_LONG).show();
            return null;
        }

        File file = new File(Environment.getExternalStorageDirectory(), "NotesOnCloud");  /////////////////////////////////////////////

        if(!file.exists()) {
            if(!file.mkdirs()) {
                Log.e(TAG, "在SD卡中创建文件夹失败");
                Toast.makeText(context, "在SD卡中创建文件夹失败", Toast.LENGTH_LONG).show();
                return null;
            }
        }

        File f =  new File(file, fileName);
        if(!f.exists()) {
            if(!createIfNoeExists) {
                return null;
            }

            try {
                f.createNewFile();
            } catch (IOException e) {
                Log.e(TAG, e.getMessage());
            }

        }

        return f;
    }


    /**
     * 检查邮箱地址是否合法。
     * 来源：http://blog.csdn.net/fatherican/article/details/8853062
     * @param email 需要检测的email地址
     * @return 是否是有效的email地址
     */
    public static boolean isEmailValid(String email) {
        String check = "^\\s*\\w+(?:\\.{0,1}[\\w-]+)*@[a-zA-Z0-9]+(?:[-.][a-zA-Z0-9]+)*\\.[a-zA-Z]+\\s*$";
        Pattern regex = Pattern.compile(check);
        Matcher matcher = regex.matcher(email);
        return matcher.matches();
    }

    /**
     * 判断是否有可用的网络
     * @param context
     * @return
     */
    public static boolean isNetworkAvailable(Context context) {
        ConnectivityManager cm
                = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        if (cm == null) {
            return false;
        }

        NetworkInfo info = cm.getActiveNetworkInfo();
        return info != null && info.isAvailable();
    }

    public static byte[] readFile(String path) {
        InputStream is = null;
        ByteArrayOutputStream os = null;
        try {
            is = new FileInputStream(path);
            os = new ByteArrayOutputStream();
            int len;
            byte[] buf = new byte[1024];
            while ((len = is.read(buf)) != -1) {
                os.write(buf, 0, len);
            }
            os.flush();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            return null;
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        } finally {
            try {
                if (os != null) {
                    os.close();
                }
                if(is != null) {
                    is.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        return os.toByteArray();
    }

    public static String writeFile(Context context, byte[] bytes) {
        File file = getFileInSdcardByName(context, getUniqueFileName(), true);
        if(file == null) {
            return null;
        }

        FileOutputStream os = null;
        try {
            os = new FileOutputStream(file);
            os.write(bytes);
            os.flush();
            return file.getCanonicalPath();
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        } finally {
            if(os != null) {
                try {
                    os.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    /**
     * 重启当前App
     * @param activity
     */
    public static void rebootApp(Activity activity) {
        PackageManager pm = activity.getPackageManager();
        Intent intent = pm.getLaunchIntentForPackage(activity.getPackageName());
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        activity.startActivity(intent);
    }
}
