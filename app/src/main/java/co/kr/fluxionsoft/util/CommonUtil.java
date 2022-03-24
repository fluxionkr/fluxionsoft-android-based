package co.kr.fluxionsoft.util;

import android.content.Context;
import android.content.CursorLoader;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.database.Cursor;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Build;
import android.provider.MediaStore;
import android.util.DisplayMetrics;
import android.util.Log;
import android.widget.TextView;
import co.kr.fluxionsoft.base.AppConst;

import java.io.File;
import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.NumberFormat;
import java.util.Formatter;
import java.util.HashMap;
import java.util.Locale;


public class CommonUtil {
    public static boolean checkNetworkConnection(Context context) {
        ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        boolean result = false;

        if (activeNetwork != null) { // connected to the internet
            if (activeNetwork.getType() == ConnectivityManager.TYPE_WIFI) {
                // connected to wifi
                result = true;
            } else if (activeNetwork.getType() == ConnectivityManager.TYPE_MOBILE) {
                // connected to the mobile provider's data plan
                result = true;
            }
        } else {
            // not connected to the internet
            result = false;
        }

        return result;
    }


    public static String getAppVersion(Context context) {

        // application version
        String versionName = "";
        try {
            PackageInfo info = context.getPackageManager().getPackageInfo(context.getPackageName(), 0);
            versionName = info.versionName;
        } catch (PackageManager.NameNotFoundException e) {
            Log.i(AppConst.LOG_TAG, "getAppVersion Exception :" + e);
        }

        return versionName;
    }

    public static String encryptSha1(String password)
    {
        String sha1 = "";
        try
        {
            MessageDigest crypt = MessageDigest.getInstance("SHA-1");
            crypt.reset();
            crypt.update(password.getBytes("UTF-8"));
            sha1 = byteToHex(crypt.digest());
        }
        catch(NoSuchAlgorithmException e)
        {
            e.printStackTrace();
        }
        catch(UnsupportedEncodingException e)
        {
            e.printStackTrace();
        }
        return sha1;
    }

    private static String byteToHex(final byte[] hash)
    {
        Formatter formatter = new Formatter();
        for (byte b : hash)
        {
            formatter.format("%02x", b);
        }
        String result = formatter.toString();
        formatter.close();
        return result;
    }


    public static HashMap getParamMap(String queryString, String div){
        if(div == null){
            div = "=";
        }
        String params[] = queryString.split("&");

        HashMap paramMap = new HashMap();
        for(String param : params){
            String keyValues[] = param.split(div);
            String key = keyValues[0];
            String value = "";
            if(keyValues.length == 2){
                value = keyValues[1];
            }
            paramMap.put(key, value);
        }

        return paramMap;
    }

    public static String getNumberFormatComma(Long num){
        String commaNum = "";

        NumberFormat nf = NumberFormat.getInstance();
        commaNum = nf.format(num);

        return commaNum;
    }

    public static String getNumberFormatComma(Integer num){
        String commaNum = "";

        NumberFormat nf = NumberFormat.getInstance();
        commaNum = nf.format(num);

        return commaNum;
    }

    public static String replaeZeroStr(String str, String replaceStr){
        if(str == null || str.equals("")){
            return replaceStr;
        }else{
            return str;
        }
    }


    public static String getLocalLanguage(Context mContext){
        Locale locale;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            locale = mContext.getResources().getConfiguration().getLocales().get(0);
        } else {
            locale = mContext.getResources().getConfiguration().locale;
        }

        return locale.getLanguage();
    }

    public static String getLocalCountry(Context mContext){
        Locale locale;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            locale = mContext.getResources().getConfiguration().getLocales().get(0);
        } else {
            locale = mContext.getResources().getConfiguration().locale;
        }

        return locale.getCountry();
    }

    public static void setDateTimeTextView(Long sec, Long add_dt, TextView textView, String date_format){
        if(sec < 60){
            textView.setText(sec + " seconds");

        }else if(sec < 3600){
            long minute = (sec%3600)/60;

            textView.setText(minute + " minutes");
        }else if(sec < 86400){
            long hour =  sec/3600;
            textView.setText(hour + " hours");
        }else{
            if(date_format == null){
                textView.setText(DateUtil.getDateString(add_dt, "yyyy.MM.dd"));
            }else{
                textView.setText(DateUtil.getDateString(add_dt, date_format));
            }

            /*System.out.println("=============>" + add_dt);
            System.out.println("=============>" + add_dt);
            System.out.println("=============>" + add_dt);
            System.out.println("=============>" + DateUtil.getDateString(add_dt, date_format));
            System.out.println("=============>" + DateUtil.getDateString(add_dt, "yyyy.MM.dd.ss"));

            long now = System.currentTimeMillis();
            System.out.println("=============>" + now);
            System.out.println("=============>" + DateUtil.getDateString(now, date_format));*/
        }
    }

    public static String getDateTime(Long sec, Long add_dt, String date_format){
        if(sec < 60){
            return  sec + " seconds";

        }else if(sec < 3600){
            long minute = (sec%3600)/60;
            return minute + " minutes";
        }else if(sec < 86400){
            long hour =  sec/3600;
            return hour + " hours";
        }else{
            if(date_format == null){
                return DateUtil.getDateString(add_dt, "yyyy.MM.dd");
            }else{
                return DateUtil.getDateString(add_dt, date_format);
            }
        }
    }


    public static float convertDpToPixel(Context context, float dp){
        Resources resources = context.getResources();
        DisplayMetrics metrics = resources.getDisplayMetrics();
        float px = dp * ((float)metrics.densityDpi / DisplayMetrics.DENSITY_DEFAULT);
        return px;
    }

    public static float convertPixelsToDp(Context context, float px){
        Resources resources = context.getResources();
        DisplayMetrics metrics = resources.getDisplayMetrics();
        float dp = px / ((float)metrics.densityDpi / DisplayMetrics.DENSITY_DEFAULT);
        return dp;
    }


    /**
     * Uri 를 File Path 로 변환하기 ( Uri -> Path )
     * @param context
     * @param uri
     * @return
     */
    public String getPathFromUri(Context context, Uri uri){

        Cursor cursor = context.getContentResolver().query(uri, null, null, null, null );

        cursor.moveToNext();

        String path = cursor.getString( cursor.getColumnIndex( "_data" ) );

        cursor.close();

        return path;

    }


    public static String getRealPathFromURI(Context context,Uri contentUri) {
        String[] proj = { MediaStore.Images.Media.DATA };
        CursorLoader loader = new CursorLoader(context, contentUri, proj, null, null, null);
        Cursor cursor = loader.loadInBackground();
        int column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
        cursor.moveToFirst();
        String result = cursor.getString(column_index);
        cursor.close();
        return result;
    }


    public static Uri getImageContentUri(Context context, Uri uri) {
        Cursor cursor = context.getContentResolver().query( Uri.parse(uri.toString() ),
        null, null, null, null );

        cursor.moveToNext(); // 예외처리는 생략했습니다. 실제 코드에서는 예외처리를 잘 해주세요.

        String path = cursor.getString( cursor.getColumnIndex( "_data" ) );

        Uri returnUri = Uri.fromFile(new File(path));

        cursor.close();

        return  returnUri;

    }

}
