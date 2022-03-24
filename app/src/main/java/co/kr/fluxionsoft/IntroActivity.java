package co.kr.fluxionsoft;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.dynamiclinks.FirebaseDynamicLinks;
import com.google.firebase.dynamiclinks.PendingDynamicLinkData;
import com.google.firebase.remoteconfig.FirebaseRemoteConfig;
import com.google.firebase.remoteconfig.FirebaseRemoteConfigSettings;
import co.kr.fluxionsoft.R;

import org.json.JSONException;
import org.json.JSONObject;

public class IntroActivity extends AppCompatActivity {

    String goodsno = null;
    String orderPath = null;
    String link = null;
    String notification="";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_intro);

        final FirebaseRemoteConfig firebaseRemoteConfig = FirebaseRemoteConfig.getInstance();
        FirebaseRemoteConfigSettings configSettings = new FirebaseRemoteConfigSettings.Builder()
                .build();
        firebaseRemoteConfig.setConfigSettingsAsync(configSettings);
        firebaseRemoteConfig.setDefaultsAsync(R.xml.remote_config_default);
        firebaseRemoteConfig.fetch()
                .addOnCompleteListener(this, task -> {
                    String installed_version ="";
                    String firebase_version ="0@0@0";
                    boolean versionPass = true;

                    if(task.isSuccessful()){
                        firebaseRemoteConfig.activate();
                        installed_version = BuildConfig.VERSION_NAME.replace(".","@");

                        if(firebaseRemoteConfig.getString("android_min_version").length()>0){
                            firebase_version = firebaseRemoteConfig.getString("android_min_version").replace(".","@");
                            Log.d("android_max_version ", firebase_version +" | "+ firebase_version);
                        }
                        if(Integer.parseInt(installed_version.split("@")[0]) > Integer.parseInt(firebase_version.split("@")[0])){
                            versionPass = true;
                        } else if( Integer.parseInt(installed_version.split("@")[0]) == Integer.parseInt(firebase_version.split("@")[0]) ) {
                            if (Integer.parseInt(installed_version.split("@")[1]) > Integer.parseInt(firebase_version.split("@")[1])) {
                                versionPass = true;
                            } else if(Integer.parseInt(installed_version.split("@")[1]) == Integer.parseInt(firebase_version.split("@")[1])) {
                                if (Integer.parseInt(installed_version.split("@")[2]) > Integer.parseInt(firebase_version.split("@")[2])) {
                                    versionPass = true;
                                } else if(Integer.parseInt(installed_version.split("@")[2]) == Integer.parseInt(firebase_version.split("@")[2])) {
                                    versionPass = true;
                                } else{
                                    versionPass = false;
                                }
                            }
                            else {
                                versionPass = false;
                            }
                        } else {
                            versionPass = false;
                        }



                        if(!versionPass){
                            Log.d("version_check_fail ", installed_version +" | "+ firebase_version);
                            AppUpdatecheck();
                        }else{
                            Log.d("version_check_success ", installed_version +" | "+ firebase_version);
                            beforeIntro();
                        }

                    } else{
                        Log.d("remoteconfig", "failed!");
                    }



                });


        Intent myIntent=getIntent();
        Bundle myBundle=myIntent.getExtras();
        if(myBundle!=null){
            String mp=(String)myBundle.getString("mp_message");
            if(mp !=null){

                JSONObject jsonObj = null;
                try {
                    jsonObj = new JSONObject(mp);
                } catch (JSONException e) {
                    e.printStackTrace();
                }


                try {
                    goodsno=jsonObj.getString("goodsno");
                } catch (JSONException e) {
                    e.printStackTrace();
                }

                try {
                    link=jsonObj.getString("link");
                } catch (JSONException e) {
                    e.printStackTrace();
                }

                notification="Y";

            }
            String goodsnoFromFirebase=(String)myBundle.getString("goodsno");
            if(goodsnoFromFirebase!=null){
                goodsno=goodsnoFromFirebase;
            }

            String linkFromFirebase=(String)myBundle.getString("link");
            if(linkFromFirebase!=null){
                link=linkFromFirebase;
            }
        }

        FirebaseDynamicLinks.getInstance()
                .getDynamicLink(getIntent())
                .addOnSuccessListener(this, new OnSuccessListener<PendingDynamicLinkData>() {
                    @Override
                    public void onSuccess(PendingDynamicLinkData pendingDynamicLinkData) {
                        Log.d("TAG deepLink :: ", "SUCCESS");
                        // Get deep link from result (may be null if no link is found)
                        Uri deepLink = null;
                        if (pendingDynamicLinkData != null) {
                            deepLink = pendingDynamicLinkData.getLink();

                            Log.d("TAG deepLink :: ", deepLink.toString());
//                            String urlScheme = deepLink.toString().split("\\?")[1];
//
//                            HashMap paramMap = CommonUtil.getParamMap(urlScheme, null);
//
//                            if(paramMap.get("goodsno") != null)
//                                goodsno = (String)paramMap.get("goodsno");
//
//                            if(paramMap.get("orderPath") != null)
//                                orderPath = (String)paramMap.get("orderPath");
                            link = deepLink.toString();
                        }
                    }
                })
                .addOnFailureListener(this, new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.d("TAG", "getDynamicLink:onFailure", e);
                    }
                });

//        new Handler().postDelayed(new Runnable()
//        {
//            @Override
//            public void run()
//            {
//                //여기에 딜레이 후 시작할 작업들을 입력
//                Intent intent = new Intent(IntroActivity.this, MainActivity.class);
//                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
//                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
//
//                if(goodsno != null){
//                    intent.putExtra("goodsno", goodsno);
//                }
//
//                if(orderPath != null){
//                    intent.putExtra("orderPath", orderPath);
//                }
//
//                if(link != null){
//                    intent.putExtra("link", link);
//                }
//
//                intent.putExtra("notification", notification);
//
//                startActivity(intent);
//
//                finish();
//            }
//        }, 2000);// 초 정도 딜레이를 준 후 시작

    }
    // 인트로 화면
    private void beforeIntro() {
        // 약 2초간 인트로 화면을 출력.
        getWindow().getDecorView().postDelayed(new Runnable() {
            @Override
            public void run() {
//                Intent intent = new Intent(IntroActivity.this, MainActivity.class);
//                intent.putExtra("intro", false);
//                startActivity(intent);
//                finish();

                    //여기에 딜레이 후 시작할 작업들을 입력
                    Intent intent = new Intent(IntroActivity.this, MainActivity.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

                    //if(goodsno != null){
                        //intent.putExtra("goodsno", goodsno);
                    //}

                    //if(orderPath != null){
                        //intent.putExtra("orderPath", orderPath);
                    //}

                    //if(link != null){
                        //intent.putExtra("link", link);
                    //}

                    //intent.putExtra("notification", notification);

                    startActivity(intent);

                    finish();
            }
        }, 2000);
    }

    public void AppUpdatecheck() {

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("업데이트 안내");
        builder.setMessage("고객님께 더 나은 서비스 경험을 드리기 위해 새로운 버전의 앱을 출시하였습니다.");
        builder.setPositiveButton("업데이트 하기",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        String url2 = "https://play.google.com/store/apps/details?id=";
                        Intent i = new Intent(Intent.ACTION_VIEW, Uri.parse(url2));
                        startActivity(i);
                        System.runFinalization();
                        System.exit(0);
                    }
                });
        builder.setNegativeButton("나가기",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        System.runFinalization();
                        System.exit(0);
                    }
                });
        //builder.show();
        AlertDialog alertDialog = builder.create();
        alertDialog.setCanceledOnTouchOutside(false);
        alertDialog.show();


    }
}
