package co.kr.fluxionsoft.model;

import android.content.Context;
import android.content.SharedPreferences;

/**
 * Created by ydse on 2017-12-12.
 */

public class FluxionContext {

    private static FluxionContext _instance = null;

    public static FluxionContext getInstance() {

        if (_instance == null) {
            synchronized (FluxionContext.class) {
                _instance = new FluxionContext();
            }
        }

        return _instance;
    }

    public void setMid(Context context, String mid){
        SharedPreferences pref = context.getSharedPreferences("FluxionContext", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = pref.edit();
        editor.putString("mid", mid);
        editor.commit();


    }

    public String getMid(Context context){
        SharedPreferences pref = context.getSharedPreferences("FluxionContext", Context.MODE_PRIVATE);

        return pref.getString("mid", null);
    }

    public void removeMid(Context context){
        SharedPreferences pref = context.getSharedPreferences("FluxionContext", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = pref.edit();
        editor.remove("mid");
        editor.commit();

    }

    public void setPush_yn(Context context, String push_yn){
        SharedPreferences pref = context.getSharedPreferences("FluxionContext", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = pref.edit();
        editor.putString("push_yn", push_yn);
        editor.commit();


    }

    public String getPush_yn(Context context){
        SharedPreferences pref = context.getSharedPreferences("FluxionContext", Context.MODE_PRIVATE);

        return pref.getString("push_yn", null);
    }

    public void removePysh_yn(Context context){
        SharedPreferences pref = context.getSharedPreferences("FluxionContext", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = pref.edit();
        editor.remove("push_yn");
        editor.commit();

    }


    public void setSplash_yn(Context context, String splash_yn){
        SharedPreferences pref = context.getSharedPreferences("FluxionContext", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = pref.edit();
        editor.putString("splash_yn", splash_yn);
        editor.commit();


    }

    public String getSplash_yn(Context context){
        SharedPreferences pref = context.getSharedPreferences("FluxionContext", Context.MODE_PRIVATE);

        return pref.getString("splash_yn", "N");
    }

    public void setPermission_check_yn(Context context, String permission_check_yn){
        SharedPreferences pref = context.getSharedPreferences("FluxionContext", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = pref.edit();
        editor.putString("permission_check_yn", permission_check_yn);
        editor.commit();


    }

    public String getPermission_check_yn(Context context){
        SharedPreferences pref = context.getSharedPreferences("FluxionContext", Context.MODE_PRIVATE);

        return pref.getString("permission_check_yn", "N");
    }
}
