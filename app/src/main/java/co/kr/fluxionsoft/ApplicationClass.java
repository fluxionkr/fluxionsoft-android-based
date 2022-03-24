package co.kr.fluxionsoft;

import android.app.Activity;
import android.app.Application;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.util.Log;

import com.appboy.AppboyLifecycleCallbackListener;
import com.appsflyer.AppsFlyerConversionListener;
import com.appsflyer.AppsFlyerLib;
import com.kakao.ad.tracker.KakaoAdTracker;

import java.util.Map;

//import io.airbridge.AirBridge;


/**
 * Created by eternite on 2018. 11. 2..
 */

public class ApplicationClass extends Application {
    /** 앱 백그라운드&포그라운드 체크용 **/
    private boolean isBackground = true;
    //private static final String AF_DEV_KEY = "H6wmLCQu5RFSe2jAxPvT9i";

    @Override
    public void onCreate() {
        super.onCreate();
        registerActivityLifecycleCallbacks(new AppboyLifecycleCallbackListener(true, true));
        //AirBridge.init(this, "closetshare", "684008f6589547c59b19d6f2d91e84e8");

        /** 앱 백그라운드&포그라운드 관련 **/
        // Region Start
        listenForForeground();
        listenForScreenTurningOff();
        // Region End
        if (!KakaoAdTracker.isInitialized()) {
            KakaoAdTracker.getInstance().init(getApplicationContext(), getString(R.string.kakao_ad_track_id));
        }

        //앱스플라이어
        AppsFlyerConversionListener conversionListener = new AppsFlyerConversionListener() {
            @Override
            public void onConversionDataSuccess(Map<String, Object> conversionData) {

                for (String attrName : conversionData.keySet()) {
                    Log.d("LOG_TAG", "attribute: " + attrName + " = " + conversionData.get(attrName));
                }
            }

            @Override
            public void onConversionDataFail(String errorMessage) {
                Log.d("LOG_TAG", "error getting conversion data: " + errorMessage);
            }

            @Override
            public void onAppOpenAttribution(Map<String, String> attributionData) {
                for (String attrName : attributionData.keySet()) {
                    Log.d("LOG_TAG", "attribute: " + attrName + " = " + attributionData.get(attrName));
                }
            }

            @Override
            public void onAttributionFailure(String errorMessage) {
                Log.d("LOG_TAG", "error onAttributionFailure : " + errorMessage);
            }
        };

        //AppsFlyerLib.getInstance().init(AF_DEV_KEY, conversionListener, this);

        //AppsFlyerLib.getInstance().start(this);


    }


    // Application클래스의 onTrimMemory에 아래와 같이 추가
    @Override
    public void onTrimMemory(int level) {
        super.onTrimMemory(level);
        /** 앱 백그라운드&포그라운드 관련 **/
        // Region Start
        if (level == TRIM_MEMORY_UI_HIDDEN) {
            isBackground = true;
            notifyBackground();
        }
        // Region End
    }

    /** 앱 백그라운드&포그라운드 체크 관련 함수 **/
    // Region Start
    private void listenForForeground() {
        registerActivityLifecycleCallbacks(new ActivityLifecycleCallbacks() {

            Thread th;

            @Override
            public void onActivitySaveInstanceState(Activity activity, Bundle bundle) {
                Log.d("APPLICATION","onActivitySaveInstanceState" );
            }

            @Override
            public void onActivityCreated(Activity activity, Bundle bundle) {
                Log.d("APPLICATION","onActivityCreated" );
            }

            @Override
            public void onActivityStarted(Activity activity) {
                Log.d("APPLICATION","onActivityStarted" );
            }

            @Override
            public void onActivityPaused(Activity activity) {
                Log.d("APPLICATION","onActivityPaused" );
            }

            @Override
            public void onActivityResumed(Activity activity) {
                Log.d("APPLICATION","onActivityResumed" );
                if(th !=null){
                    th.interrupt();
                    th=null;
                }

            }

            @Override
            public void onActivityStopped(final Activity activity) {
                Log.d("APPLICATION","onActivityStopped" );
//                th = new Thread(new Runnable() {
//                    @Override
//                    public void run() {
//                        try {
//                            while(!Thread.currentThread().isInterrupted()) {
//
//                                Thread.sleep(60000);
//                                Thread.currentThread().interrupt();
//                                Log.d("Application","STOP");
//                                activity.finish();
//
//                            }
//                        }catch(Exception e) {
//                            e.printStackTrace();
//                        }
//                    }
//                });
//                th.start();




            }

            @Override
            public void onActivityDestroyed(Activity activity) {
                Log.d("APPLICATION","onActivityDestroyed" );
            }


        });
    }

    private void listenForScreenTurningOff(){
        try{
            IntentFilter screenStateFilter = new IntentFilter(Intent.ACTION_SCREEN_OFF);
            registerReceiver(new BroadcastReceiver() {
                @Override
                public void onReceive(Context context, Intent intent) {
                    isBackground = true;
                    notifyBackground();
                }
            }, screenStateFilter);
        }catch(Exception e){
            e.printStackTrace();
        }
    }

    private void notifyForeground() {

    }

    private void notifyBackground() {

    }


}
