package co.kr.fluxionsoft;

import android.Manifest;
import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.content.ActivityNotFoundException;
import android.content.Context;

import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.RemoteException;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.webkit.JavascriptInterface;
import android.webkit.ValueCallback;
import android.webkit.WebChromeClient;
import android.webkit.WebResourceRequest;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.FrameLayout;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;

import com.amplitude.api.Amplitude;
import com.amplitude.api.AmplitudeClient;
import com.android.installreferrer.api.InstallReferrerClient;
import com.android.installreferrer.api.InstallReferrerStateListener;
import com.android.installreferrer.api.ReferrerDetails;
import com.appboy.Appboy;
import com.appsflyer.AppsFlyerLib;
import com.facebook.CallbackManager;
import com.facebook.login.LoginManager;


import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.InstanceIdResult;
import com.google.firebase.messaging.FirebaseMessaging;
import com.gun0912.tedpermission.PermissionListener;
import com.gun0912.tedpermission.TedPermission;
import co.kr.fluxionsoft.R;
import com.mixpanel.android.mpmetrics.MixpanelAPI;
import com.pincrux.tracking.PincruxTracking;
import co.kr.fluxionsoft.base.AppConst;
import co.kr.fluxionsoft.model.FluxionContext;
import co.kr.fluxionsoft.model.response.BaseResponse;
import co.kr.fluxionsoft.util.APIClient;
import co.kr.fluxionsoft.util.CommonUtil;
import co.kr.fluxionsoft.util.PaymentScheme;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Response;

//import android.support.annotation.RequiresApi;
//import android.support.constraint.ConstraintLayout;
//import android.support.v7.app.AppCompatActivity;

//import com.google.firebase.iid.FirebaseInstanceId;



public class MainActivity extends AppCompatActivity {

    public static FrameLayout mContainer;
    public static WebView webview;
    public static WebView mWebViewPop;
    private ApiService apiService;
    private Boolean bInitialAmpllitue = false;

//    ProgressDialog progressDialog;
//    private Dialog loadingDialog;

    private String mainUrl="";
    private String strLaunchedType="오가닉";

    /** install referrerClient */
    private InstallReferrerClient referrerClient;

    ////파일 업로드 ///
    private ValueCallback<Uri> filePathCallbackNormal;
    private ValueCallback<Uri[]> filePathCallbackLollipop;
    private final static int FILECHOOSER_NORMAL_REQ_CODE = 1;
    private final static int FILECHOOSER_LOLLIPOP_REQ_CODE = 2;
    //public static final String MIXPANEL_TOKEN = "8947667328461d4bcd2bfbc6734b9cbf";
    public static final String MIXPANEL_TOKEN = "123123123";
    public MixpanelAPI mixpanel ;
    AmplitudeClient client;
    String m_no;



    private final String APP_SCHEME = "fluxionsoftme://"; //AndroidManifest.xml에서 정의한 것과 동일한 URL scheme사용(substring을 위한 용도)


    private PincruxTracking tracking;
    private LoginCallback mLoginCallback;
    private CallbackManager mCallbackManager;
    private String m_strReferrerUrl;
    private boolean isFirstLoad=true;
    String useYn;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //권한체크
        PermissionListener permissionlistener = new PermissionListener() {
            @Override
            public void onPermissionGranted() {
                FluxionContext.getInstance().setPermission_check_yn(MainActivity.this, "Y");
                //Toast.makeText(MainActivity.this, "권한 허가", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onPermissionDenied(ArrayList<String> deniedPermissions) {
                FluxionContext.getInstance().setPermission_check_yn(MainActivity.this, "Y");
                //Toast.makeText(MainActivity.this, "권한 거부\n" + deniedPermissions.toString(), Toast.LENGTH_SHORT).show();
            }
        };

        if(FluxionContext.getInstance().getPermission_check_yn(MainActivity.this).equals("N")){
            TedPermission.with(this)
                    .setPermissionListener(permissionlistener)
                    .setRationaleMessage("")
                    .setDeniedMessage("[설정] > [권한] 에서 권한을 허용할 수 있어요")
                    .setGotoSettingButtonText("setting")
                    .setPermissions(Manifest.permission.READ_EXTERNAL_STORAGE)
                    .check();
        }


        Bundle bundle=getIntent().getExtras();
        if(bundle!=null){
            String title = getIntent().getExtras().getString("TITLE");
            Log.d("NOTI","============================");
            Log.d("NOTI","============================");
            Log.d("NOTI","============================");
            Log.d("NOTI","============================");
                Log.e("NOTI", "onCreate: NOTI DATA "+title +" "+getIntent().getExtras().getString("BODY") );
            Log.d("NOTI","============onDestroy================");Log.d("NOTI","============================");Log.d("NOTI","============================");
            if ( title != null  )
            {
                strLaunchedType = "푸시";
//                AlertDialog.Builder dlg = new AlertDialog.Builder(MainActivity.this);
//                dlg.setTitle("launched type"); //제목
//                dlg.setMessage(strLaunchedType + " " + strLaunchedType); // 메시지
//                dlg.setPositiveButton("확인",new DialogInterface.OnClickListener(){
//                    public void onClick(DialogInterface dialog, int which) {
//                        dialog.cancel();;
//                    }
//                });
//                dlg.show();
            }

        }

        mixpanel = MixpanelAPI.getInstance(this, MIXPANEL_TOKEN);

        //


        //FacebookSdk.sdkInitialize(getApplicationContext());
        //AppEventsLogger.activateApp(this);

        mCallbackManager = CallbackManager.Factory.create();
        mLoginCallback = new LoginCallback();

        tracking = PincruxTracking.getInstance();
        tracking.init(this, "PIN10000047");
        //tracking.execute(this);

        client = Amplitude.getInstance()
                //*/
                .initialize(getApplicationContext(), "344ca6c2489ca568d1052a4183d34106") // real key
                /*/
                .initialize(getApplicationContext(), "63672bdaaabd87e1f4e63847e9038613") // dev key
                 //*/


                .enableForegroundTracking(getApplication());

        referrerClient = InstallReferrerClient.newBuilder(this).build();
        referrerClient.startConnection(new InstallReferrerStateListener() {
            @Override
            public void onInstallReferrerSetupFinished(int responseCode) {

                switch (responseCode) {
                    case InstallReferrerClient.InstallReferrerResponse.OK: // Connection established.
                        ReferrerDetails response;
                        try {


                            response = referrerClient.getInstallReferrer();
                            if (response == null) return;

                            //String referrerUrl = response.getInstallReferrer();
                            m_strReferrerUrl = response.getInstallReferrer();

//                            AlertDialog.Builder dlg = new AlertDialog.Builder(MainActivity.this);
//                            dlg.setTitle("inapp_message 시작"); //제목
//                            dlg.setMessage(m_strReferrerUrl ); // 메시지
//                            dlg.setPositiveButton("확인",new DialogInterface.OnClickListener(){
//                                public void onClick(DialogInterface dialog, int which) {
//                                    dialog.cancel();;
//                                }
//                            });
//                            dlg.show();
                            long referrerClickTime = response.getReferrerClickTimestampSeconds();
                            long appInstallTime = response.getInstallBeginTimestampSeconds();
                            boolean instantExperienceLaunched = response.getGooglePlayInstantParam();

                        } catch (RemoteException e) {
                            e.printStackTrace();
                        }
                        break;
                    case InstallReferrerClient.InstallReferrerResponse.FEATURE_NOT_SUPPORTED: // API not available on the current Play Store app.
                        break;
                    case InstallReferrerClient.InstallReferrerResponse.SERVICE_UNAVAILABLE: // Connection couldn't be established.
                        break;
                }
            }

            @Override
            public void onInstallReferrerServiceDisconnected() {
                // Try to restart the connection on the next request to
                // Google Play by calling the startConnection() method.
            }
        });
        if(savedInstanceState != null){
            Intent intent = new Intent(this, IntroActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
            return;
        }
        //Log.d(AppConst.LOG_TAG, "firebase token :: " + FirebaseInstanceId.getInstance().getToken());
       // FirebaseCrashlytics.getInstance().setCrashlyticsCollectionEnabled(true);
        setContentView(R.layout.activity_main);

        //progressDialog =  new ProgressDialog(MainActivity.this);

        mContainer = (FrameLayout) findViewById(R.id.webview_frame);
        webview = (WebView)findViewById(R.id.webView);

        //폰트 사이즈 때문에 깨지는 현상이 있다
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.ICE_CREAM_SANDWICH) {
            webview.getSettings().setTextZoom(100);
        }

        webview.setScrollBarStyle(WebView.SCROLLBARS_OUTSIDE_OVERLAY);
        WebSettings set = webview.getSettings();
        set.setJavaScriptEnabled(true);
        set.setCacheMode(WebSettings.LOAD_NO_CACHE);
        set.setSupportMultipleWindows(true);
        set.setJavaScriptCanOpenWindowsAutomatically(true);
        set.setUseWideViewPort(true);
        set.setDomStorageEnabled(true);
        set.setAllowFileAccess(true);
        webview.setWebViewClient(new Callback());
        //webview.setWebChromeClient(new WebChromeClient());
        webview.addJavascriptInterface(new WebAppInterface(this), "tc_api");
        webview.setWebChromeClient(new WebChromeClient() {


            @Override
            public boolean onCreateWindow(WebView view, boolean isDialog, boolean isUserGesture, android.os.Message resultMsg) {

                mWebViewPop = new WebView(view.getContext());
                mWebViewPop.getSettings().setJavaScriptEnabled(true);
                mWebViewPop.getSettings().setJavaScriptCanOpenWindowsAutomatically(true);
                mWebViewPop.getSettings().setSupportMultipleWindows(true);
                mWebViewPop.getSettings().setDomStorageEnabled(true);
                mWebViewPop.getSettings().setAllowFileAccess(true);


                mWebViewPop.setWebChromeClient(new WebChromeClient(){
                    @Override
                    public void onCloseWindow(WebView window) {
                        mContainer.removeView(window);
                        window.destroy();
                    }

                    //파일업로드
                    // For Android < 3.0
                    public void openFileChooser( ValueCallback<Uri> uploadMsg) {
                        Log.d("MainActivity", "3.0 <");
                        openFileChooser(uploadMsg, "");
                    }
                    // For Android 3.0+
                    public void openFileChooser( ValueCallback<Uri> uploadMsg, String acceptType) {
                        Log.d("MainActivity", "3.0+");
                        filePathCallbackNormal = uploadMsg;
                        Intent i = new Intent(Intent.ACTION_GET_CONTENT);
                        i.addCategory(Intent.CATEGORY_OPENABLE);
                        i.setType("image/*");
                        // i.setType("video/*");
                        startActivityForResult(Intent.createChooser(i, "File Chooser"), FILECHOOSER_NORMAL_REQ_CODE);
                    }
                    // For Android 4.1+
                    public void openFileChooser(ValueCallback<Uri> uploadMsg, String acceptType, String capture) {
                        Log.d("MainActivity", "4.1+");
                        openFileChooser(uploadMsg, acceptType);
                    }

                    // For Android 5.0+
                    public boolean onShowFileChooser(
                            WebView webView, ValueCallback<Uri[]> filePathCallback,
                            FileChooserParams fileChooserParams) {
                        Log.d("MainActivity", "5.0+");
                        // Callback 초기화 (중요!)
                        if (filePathCallbackLollipop != null) {
                            filePathCallbackLollipop.onReceiveValue(null);
                            filePathCallbackLollipop = null;
                        }
                        filePathCallbackLollipop = filePathCallback;
                        Intent i = new Intent(Intent.ACTION_GET_CONTENT);
                        i.addCategory(Intent.CATEGORY_OPENABLE);
                        i.setType("image/*");
                        // i.setType("video/*");
                        startActivityForResult(Intent.createChooser(i, "File Chooser"), FILECHOOSER_LOLLIPOP_REQ_CODE);

                        return true;
                    }
                });
                //mWebViewPop.setWebViewClient(new Callback());
                mWebViewPop.setWebViewClient(new WebViewClient() {
                    @Override
                    public void onPageFinished(WebView view, String url) {
                        super.onPageFinished(view, url);
                        //childCount = 1;
                    }

                    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
                    @Override
                    public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest request)
                    {
                        String url = request.getUrl().toString();
                        Log.d("WEB REQUEST", url.toString());
                        if (url.startsWith("tel:")) {
                            Intent call_phone = new Intent(Intent.ACTION_CALL);
                            call_phone.setData(Uri.parse(url));
                            startActivity(call_phone);
                        } else if (url.startsWith("sms:")) {
                            Intent i = new Intent(Intent.ACTION_SENDTO, Uri.parse(url));
                            startActivity(i);
                        } else if ( request.getUrl().getScheme().equals("intent")) {
                            try {
                                Intent intent = Intent.parseUri(url, Intent.URI_INTENT_SCHEME);
                                Intent existPackage = getPackageManager().getLaunchIntentForPackage(intent.getPackage());
                                if ( existPackage != null ) {
                                    startActivity(intent);
                                    //Log.d("startActivity", ${intent.`package`})
                                    return true;
                                }
                                else {
                                    Intent marketIntent = new Intent(Intent.ACTION_VIEW);
                                    marketIntent.setData(Uri.parse("market://details?id=" + intent.getPackage()));
                                    startActivity(marketIntent);
                                }


                                String fallbackUrl = intent.getStringExtra("browser_fallback_url");
                                if ( fallbackUrl != null) {
                                    view.loadUrl(fallbackUrl);
                                    //Log.d();
                                    return true;
                                }


                            } catch ( URISyntaxException e )
                            {
                                Log.e("WEB REQUEST", "Invalid intent request", e);
                            }
                        }

                        return false;
                    }


                    @SuppressLint("MissingPermission")
                    @Override
                    public boolean shouldOverrideUrlLoading(WebView view, String url) {
                        if (url.startsWith("tel:")) {
                            Intent call_phone = new Intent(Intent.ACTION_CALL);
                            call_phone.setData(Uri.parse(url));
                            startActivity(call_phone);
                        } else if (url.startsWith("sms:")) {
                            Intent i = new Intent(Intent.ACTION_SENDTO, Uri.parse(url));
                            startActivity(i);
                        } else if (url.startsWith("intent:")) {
                            try {
                                Intent intent = Intent.parseUri(url, Intent.URI_INTENT_SCHEME);
                                Intent existPackage = getPackageManager().getLaunchIntentForPackage(intent.getPackage());
                                if (existPackage != null) {
                                    startActivity(intent);
                                } else {
                                    Intent marketIntent = new Intent(Intent.ACTION_VIEW);
                                    marketIntent.setData(Uri.parse("market://details?id=" + intent.getPackage()));
                                    startActivity(marketIntent);
                                    return true;
                                }
                                String fallbackUrl = intent.getStringExtra("browser_fallback_url");
                                if ( fallbackUrl != null) {
                                    view.loadUrl(fallbackUrl);
                                    //Log.d();
                                    return true;
                                }
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        } else {
                            if (url.startsWith("market")) {
                                startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(url)));
                            }else{
                                view.loadUrl(url);
                                Log.d("확인",url);
                            }
                        }
                        return false;
                    }


                });

                mWebViewPop.setOnKeyListener(new View.OnKeyListener() {
                    @Override
                    public boolean onKey(View v, int keyCode, KeyEvent event) {
                        Log.d("확인","키 : " + keyCode);
                        if (keyCode == KeyEvent.KEYCODE_BACK) {
                           // childCount = 0;
                            mWebViewPop.loadUrl("javascript:self.close();");
                            return true;
                        }

                        return false;
                    }
                });
                mWebViewPop.setLayoutParams(new ConstraintLayout.LayoutParams(ConstraintLayout.LayoutParams.MATCH_PARENT, ConstraintLayout.LayoutParams.MATCH_PARENT));
                mContainer.addView(mWebViewPop);
                //webview.addView(mWebViewPop);
                WebView.WebViewTransport transport = (WebView.WebViewTransport) resultMsg.obj;
                transport.setWebView(mWebViewPop);
                resultMsg.sendToTarget();
                return true;
            };


            @Override
            public void onProgressChanged(WebView view, int newProgress) {

            }

            //파일업로드
            // For Android < 3.0
            public void openFileChooser( ValueCallback<Uri> uploadMsg) {
                Log.d("MainActivity", "3.0 <");
                openFileChooser(uploadMsg, "");
            }
            // For Android 3.0+
            public void openFileChooser( ValueCallback<Uri> uploadMsg, String acceptType) {
                Log.d("MainActivity", "3.0+");
                filePathCallbackNormal = uploadMsg;
                Intent i = new Intent(Intent.ACTION_GET_CONTENT);
                i.addCategory(Intent.CATEGORY_OPENABLE);
                i.setType("image/*");
                // i.setType("video/*");
                startActivityForResult(Intent.createChooser(i, "File Chooser"), FILECHOOSER_NORMAL_REQ_CODE);
            }
            // For Android 4.1+
            public void openFileChooser(ValueCallback<Uri> uploadMsg, String acceptType, String capture) {
                Log.d("MainActivity", "4.1+");
                openFileChooser(uploadMsg, acceptType);
            }

            // For Android 5.0+
            public boolean onShowFileChooser(
                    WebView webView, ValueCallback<Uri[]> filePathCallback,
                    FileChooserParams fileChooserParams) {
                Log.d("MainActivity", "5.0+");
                // Callback 초기화 (중요!)
                if (filePathCallbackLollipop != null) {
                    filePathCallbackLollipop.onReceiveValue(null);
                    filePathCallbackLollipop = null;
                }
                filePathCallbackLollipop = filePathCallback;
                Intent i = new Intent(Intent.ACTION_GET_CONTENT);
                i.addCategory(Intent.CATEGORY_OPENABLE);
                i.setType("image/*");
                // i.setType("video/*");
                startActivityForResult(Intent.createChooser(i, "File Chooser"), FILECHOOSER_LOLLIPOP_REQ_CODE);

                return true;
            }

        });
        webview.requestFocusFromTouch();



        //webview 성능 개선 추가 20190911 eden.
        if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.O) {
            //기기에 따라서 동작할수도있는걸 확인
            set.setRenderPriority(WebSettings.RenderPriority.HIGH);

            //최신 SDK 에서는 Deprecated 이나 아직 성능상에서는 유용하다
            set.setLayoutAlgorithm(WebSettings.LayoutAlgorithm.SINGLE_COLUMN);

            //부드러운 전환 또한 아직 동작
            set.setEnableSmoothTransition(true);
        }


        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP)
        {// https 이미지.
            set.setMixedContentMode(WebSettings.MIXED_CONTENT_ALWAYS_ALLOW);
        }

        //set.setJavaScriptCanOpenWindowsAutomatically(true);

        PackageInfo pi = null;
        try {
            pi = getPackageManager().getPackageInfo(getPackageName(), 0);
        } catch (PackageManager.NameNotFoundException e)
        {
            Log.e("ERROR", e.getMessage());
        }
        String appVersion = pi.versionName;
        int appCode = pi.versionCode;



        //UserAgent 추가
        String userAgent = webview.getSettings().getUserAgentString();
        webview.getSettings().setUserAgentString(userAgent + " theclozet_android /ver="+appCode);

        webview.setOnKeyListener(new View.OnKeyListener(){
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                //This is the filter
                if (event.getAction() != KeyEvent.ACTION_DOWN)
                    return true;
                if (keyCode == KeyEvent.KEYCODE_BACK) {
                    if ( mWebViewPop != null && mWebViewPop.getVisibility() == View.VISIBLE ) {
                            mContainer.removeView(mWebViewPop);
                            mWebViewPop.destroy();
                            mWebViewPop = null;

                    } else
                        {
                        if (webview.canGoBack()) {
                            Log.i(AppConst.LOG_TAG, "canGoBack");
                            webview.goBack();
                        } else {
                            Log.i(AppConst.LOG_TAG, "canNotGoBack");
                            finish();
                        }
                    }
                    return true;
                }
                return false;
            }
        });

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            webview.setLayerType(View.LAYER_TYPE_HARDWARE, null);
        } else {
            webview.setLayerType(View.LAYER_TYPE_SOFTWARE, null);
        }

//        String url = mainUrl+ "";
//        String orderPath="";
//        String link="";
//        if(FluxionContext.getInstance().getMid(getApplicationContext()) != null){
//
//            if(getIntent().getStringExtra("orderPath") != null){
//                orderPath=getIntent().getStringExtra("orderPath");
//            }
//
//            url = mainUrl+ "/theclozet/front/member/appLogin?orderPath="+orderPath+"&m_id=" + FluxionContext.getInstance().getMid(getApplicationContext());
//
//            if(getIntent().getStringExtra("link") != null){
//                link=getIntent().getStringExtra("link");
//                url = mainUrl+ link;
//            }
//
//            if(getIntent().getStringExtra("goodsno") != null){
//                url = url + "&goodsno=" + getIntent().getStringExtra("goodsno");
//            }
//        }else{
//            if(getIntent().getStringExtra("orderPath") != null){
//                orderPath=getIntent().getStringExtra("orderPath");
//            }
//
//            url = mainUrl+ "/theclozet/front/member/appLogin?orderPath="+orderPath;
//
//            if(getIntent().getStringExtra("link") != null){
//                link=getIntent().getStringExtra("link");
//                url = mainUrl+ link;
//            }
//
//
//            if(getIntent().getStringExtra("goodsno") != null){
//                //http://dev.theclozet.co.kr/theclozet/front/goods/tc_goods_view?goodsno=68534
//                url = mainUrl+ "/theclozet/front/goods/tc_goods_view?orderPath=notiGoods&goodsno=" + getIntent().getStringExtra("goodsno");
//            }
//        }
//        goHome(url);


        Intent intent = getIntent();
        Uri intentData = intent.getData();

        if ( intentData != null ) {
            //isp 인증 후 복귀했을 때 결제 후속조치
            String urlStr = intentData.toString();
            if ( urlStr.startsWith(APP_SCHEME) ) {
                //My앱의 WebView가 표시해야 할 웹 컨텐츠의 주소가 전달됩니다.
                String redirectURL = urlStr.substring(APP_SCHEME.length()+3);
                webview.loadUrl(redirectURL);
            }
        }

    }



    private File createImageFile() throws IOException {
        // Create an image file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";
        File storageDir = null;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.FROYO) {
            storageDir = Environment.getExternalStoragePublicDirectory(
                    Environment.DIRECTORY_PICTURES);
        }
        File imageFile = File.createTempFile(
                imageFileName,  /* prefix */
                ".jpg",         /* suffix */
                storageDir      /* directory */
        );
        return imageFile;
    }

    String url = "";
    @Override
    public void onResume(){
        super.onResume();

        Intent intent=getIntent();
        Bundle bundle=intent.getExtras();
        if(intent.getExtras()!=null){
            String notification=intent.getStringExtra("notification");
            if(notification!=null && notification.equals("Y")){
                isFirstLoad=true;
            }
        }

        if(!isFirstLoad){
            return;
        }

        String locale = getApplicationContext().getResources().getConfiguration().locale.getCountry();

        Log.d("LOCALE","==========:"+locale );
        Log.d("LOCALE","Locale:"+locale );
        //if(locale.toUpperCase().contains("KR")){
            //mainUrl=AppConst.server_url;
        //}
        //한국 URL 강제로...
        mainUrl=AppConst.server_url;


        url = mainUrl+ "";
        String orderPath="";
        String link="";
        String mid=FluxionContext.getInstance().getMid(getApplicationContext());
        System.out.println("==============mid :: " + mid);
        System.out.println("==============mid :: " + mid);
        if(mid != null){

            if(getIntent().getStringExtra("link") != null){
                url = getIntent().getStringExtra("link");
            }else{
                if(intent.getStringExtra("orderPath") != null){
                    orderPath=getIntent().getStringExtra("orderPath");
                }

                url = mainUrl+ "/theclozet/front/member/appLogin?orderPath="+orderPath;


                if(getIntent().getStringExtra("goodsno") != null){
                    url = url + "&goodsno=" + getIntent().getStringExtra("goodsno");
                }
            }

            String div = "?";
            if(url.indexOf("?") > -1){
                div = "&";
            }
            url = url + div + "m_id=" + FluxionContext.getInstance().getMid(getApplicationContext());
        }else{

            if(FluxionContext.getInstance().getSplash_yn(this).equals("N")){
                url = AppConst.server_url + "/StaticContentFront/new/splash.html";
            }else{
                if(getIntent().getStringExtra("link") != null){
                    url = getIntent().getStringExtra("link");
                }else{
                    if(getIntent().getStringExtra("orderPath") != null) {
                        orderPath = getIntent().getStringExtra("orderPath");
                    }

                    if(getIntent().getStringExtra("goodsno") != null){
                        //http://dev.theclozet.co.kr/theclozet/front/goods/tc_goods_view?goodsno=68534
                        if(orderPath != null){
                            url = mainUrl+ "/theclozet/front/goods/tc_goods_view?orderPath=" + orderPath + "&goodsno=" + getIntent().getStringExtra("goodsno");
                        }else{
                            url = mainUrl+ "/theclozet/front/goods/tc_goods_view?orderPath=notiGoods&goodsno=" + getIntent().getStringExtra("goodsno");
                        }

                    }
                }
            }
        }

        goHome(url);
        isFirstLoad=false;
    }

    public void goHome(String url){

        try{
            webview.loadUrl(url);
        }catch(Exception e){
            e.printStackTrace();
        }
    }




    private class Callback extends WebViewClient {  //HERE IS THE MAIN CHANGE.


        @SuppressWarnings("deprecation")
        @Override
        public boolean shouldOverrideUrlLoading(WebView view, String url) {

            return urlCheck(view,url);
        }

        @TargetApi(Build.VERSION_CODES.N)
        @Override
        public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest request) {
            final Uri uri = request.getUrl();
            String url = uri.toString();

            return urlCheck(view,url);
        }

        public boolean urlCheck(WebView view,String url){
            Log.d("URL","checkURL:"+url);

            if (url.startsWith("intent://")) {
                Intent intent = null;
                try {
                    intent = Intent.parseUri(url, Intent.URI_INTENT_SCHEME);
                    if (intent != null) {
                        startActivity(intent);
                    }
                } catch (URISyntaxException e) {

                } catch (ActivityNotFoundException e) {
                    String packageName = intent.getPackage();
                    if ( packageName != null && !packageName.equals("") ) {
                        Intent i = new Intent(Intent.ACTION_VIEW, Uri.parse("market://datails?id=" + packageName));
                        try {
                            startActivity(i);
                        } catch ( ActivityNotFoundException ex ) {
                            Intent webIntent = new Intent(Intent.ACTION_VIEW);
                            webIntent.setData(Uri.parse("https://play.google.com/store/apps/details?id=" + packageName));
                            if(webIntent.resolveActivity(getPackageManager()) != null) {
                                startActivity(webIntent);
                            }
                        }
                    }
                }
                return true;
            }
            String url_str[] = url.split("\\?");
            if(url_str.length > 1){
                HashMap paramMap = CommonUtil.getParamMap(url_str[1], null);

                //파라미터 mid 값이 있으면 자동로그인을 위해 저장한다.
                if(paramMap.get("app_mid") != null && !((String)paramMap.get("app_mid")).equals("")){
                    String app_mid = (String)paramMap.get("app_mid");

                    System.out.println("login success :: app_mid ::: " + app_mid);
                    FluxionContext.getInstance().setMid(getApplicationContext(), app_mid);

                    if(FluxionContext.getInstance().getPush_yn(getApplicationContext()) == null || FluxionContext.getInstance().getPush_yn(getApplicationContext()).equals("Y") ){

                        //브레이즈에 회원번호로 맵핑을해줘야 해서 서버 호출을 해서 회원번호를 받아온다.
                        apiService = APIClient.getClient().create(ApiService.class);
                        Call<String> call = apiService.selectMnoFromMid(app_mid);

                        call.enqueue(new retrofit2.Callback<String>() {
                            @Override
                            public void onResponse(Call<String> call, Response<String> response) {
                                Log.d(AppConst.LOG_TAG, response.code()+"");

                                String m_no = response.body();

                                Log.d(AppConst.LOG_TAG, m_no + " ::: selectMnoFromMid");
                                Appboy.getInstance(getApplicationContext()).changeUser(m_no);   //브레이즈 회원맵핑
                            }

                            @Override
                            public void onFailure(Call<String> call, Throwable t) {
                                call.cancel();
                            }
                        });


                        insertUpdatePushToken(app_mid, "Y");

                        FluxionContext.getInstance().setPush_yn(getApplicationContext(), "Y");
                    }
                }


                //로그아웃
                if(paramMap.get("app_logout") != null && ((String)paramMap.get("app_logout")).equals("Y")){
                    FluxionContext.getInstance().removeMid(getApplicationContext());

                    webview.clearHistory();
                    webview.clearCache(true);
                }
            }


            if(url.startsWith("mailto:") || url.startsWith("tel:")){
                Intent intent = new Intent(Intent.ACTION_VIEW,
                        Uri.parse(url));
                startActivity(intent);

                return true;

            }else if(url.startsWith("notification://")){
                String urlScheme = url.substring(("notification://").length(), url.length());
                System.out.println("urlScheme ::: " + urlScheme);
                HashMap paramMap = CommonUtil.getParamMap(urlScheme, null);

                String noti_yn = (String)paramMap.get("USE_YN");
                if(noti_yn !=null && noti_yn.equals("Y")){
                    FirebaseMessaging.getInstance().subscribeToTopic("NOTICE");
                }else if(noti_yn !=null && noti_yn.equals("N")){
                    FirebaseMessaging.getInstance().unsubscribeFromTopic("NOTICE");
                }
                insertUpdatePushToken(FluxionContext.getInstance().getMid(getApplicationContext()), noti_yn);

                FluxionContext.getInstance().setPush_yn(getApplicationContext(), noti_yn);

                return true;
            }else if (url.contains("com.tc.closetshare")) {
                try {
                    Intent intent = new Intent(Intent.ACTION_VIEW);
                    intent.setData(Uri.parse("market://details?id=com.tc.closetshare"));
                    startActivity(intent);
                    return true;
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }else if(url.startsWith("closetshare://start")){
                FluxionContext.getInstance().setSplash_yn(getApplicationContext(),"Y");
                goHome(mainUrl);

                return true;
            }



            if (!url.startsWith("http://") && !url.startsWith("https://") && !url.startsWith("javascript:")) {
                //3rd-party앱에 대한 URL scheme 대응
                Intent intent = null;

                try {
                    intent = Intent.parseUri(url, Intent.URI_INTENT_SCHEME); //IntentURI처리
                    Uri uri = Uri.parse(intent.getDataString());

                    //MainActivity activity=(MainActivity) this.getClass().getCon;
                    MainActivity.this.startActivity(new Intent(Intent.ACTION_VIEW, uri));
                    return true;
                } catch (URISyntaxException ex) {
                    return false;
                } catch (ActivityNotFoundException e) {
                    if ( intent == null )	return false;

                    //설치되지 않은 앱에 대해 market이동 처리
                    if ( handleNotFoundPaymentScheme(intent.getScheme()) )	return true;

                    //handleNotFoundPaymentScheme()에서 처리되지 않은 것 중, url로부터 package정보를 추출할 수 있는 경우 market이동 처리
                    String packageName = intent.getPackage();
                    if (packageName != null) {
                        MainActivity.this.startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=" + packageName)));
                        return true;
                    }

                    return false;
                }
            }



            //return super.shouldOverrideUrlLoading(view, url);



            return false;
        }


        /**
         * @param scheme
         * @return 해당 scheme에 대해 처리를 직접 하는지 여부
         *
         * 결제를 위한 3rd-party 앱이 아직 설치되어있지 않아 ActivityNotFoundException이 발생하는 경우 처리합니다.
         * 여기서 handler되지않은 scheme에 대해서는 intent로부터 Package정보 추출이 가능하다면 다음에서 packageName으로 market이동합니다.
         *
         */
        protected boolean handleNotFoundPaymentScheme(String scheme) {
            //PG사에서 호출하는 url에 package정보가 없어 ActivityNotFoundException이 난 후 market 실행이 안되는 경우
            if ( PaymentScheme.ISP.equalsIgnoreCase(scheme) ) {
                MainActivity.this.startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=" + PaymentScheme.PACKAGE_ISP)));
                return true;
            } else if ( PaymentScheme.BANKPAY.equalsIgnoreCase(scheme) ) {
                MainActivity.this.startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=" + PaymentScheme.PACKAGE_BANKPAY)));
                return true;
            }else if(scheme.contains("loginwithfacebook")){

                loginwithfacebook();

                return true;
            }

            return false;
        }

        @Override
        public void onPageStarted(WebView view, String url,
                                  android.graphics.Bitmap favicon) {
            super.onPageStarted(view, url, favicon);

//            progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
//            progressDialog.setMessage("Loading..");
//            progressDialog.show();

//            if(url.indexOf("theclozet/main/index") < 0){
//                if(loadingDialog == null || !loadingDialog.isShowing()) {
//                    loadingDialog = new Dialog(MainActivity.this, R.style.LoadingDialog);
//                    loadingDialog.setCancelable(true);
//                    loadingDialog.addContentView(new ProgressBar(MainActivity.this), new ViewGroup.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));
//
//                    loadingDialog.getWindow().clearFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND);
//
//                    loadingDialog.show();
//
//
//                    new Handler().postDelayed(new Runnable()
//                    {
//                        @Override
//                        public void run()
//                        {
//                            Log.d("TAG", "3초후");
//                            if(loadingDialog != null && loadingDialog.isShowing()) {
//                                Log.d("TAG", "3초후 close");
//                                loadingDialog.dismiss();
//                                loadingDialog = null;
//                            }
//                        }
//                    }, 3000);// 초 정도 딜레이를 준 후 시작
//                }
//
//            }

        };

        @Override
        public void onPageFinished(WebView view, String url) {
            super.onPageFinished(view, url);

            System.out.println("onPageFinished :: " + url);

            //progressDialog.dismiss();

//            if(loadingDialog != null) {
//                loadingDialog.dismiss();
//                loadingDialog = null;
//            }

        };

    }




    public void insertUpdatePushToken(String mid, String useYnStr){

        String locale = getApplicationContext().getResources().getConfiguration().locale.getCountry();
        String notiSubject="";
        Log.d("LOCALE","Locale:"+locale );
        if(locale.toUpperCase().contains("KR")){
            notiSubject="NOTICE";
        }else{
            notiSubject="NOTICE_SG";
        }

        if(useYn !=null && useYn.equals("Y")){
            FirebaseMessaging.getInstance().subscribeToTopic(notiSubject);
        }else if(useYn !=null && useYn.equals("N")){
            FirebaseMessaging.getInstance().unsubscribeFromTopic(notiSubject);
        }


        Log.d("Main","mid:"+mid+" token:"+ FirebaseInstanceId.getInstance().getToken());
        this.useYn=useYnStr;
        apiService = APIClient.getClient().create(ApiService.class);
        FirebaseInstanceId.getInstance().getInstanceId().addOnSuccessListener( new OnSuccessListener<InstanceIdResult>() {
            @Override
            public void onSuccess(InstanceIdResult instanceIdResult) {
                String deviceToken = instanceIdResult.getToken();
                String myMid=FluxionContext.getInstance().getMid(getApplicationContext());

                Call<BaseResponse> call = apiService.insertUpdatePushToken(myMid, deviceToken, "ANDROID", useYn);

                call.enqueue(new retrofit2.Callback<BaseResponse>() {
                    @Override
                    public void onResponse(Call<BaseResponse> call, Response<BaseResponse> response) {
                        Log.d(AppConst.LOG_TAG, response.code()+"");

                        BaseResponse baseResponse = response.body();

                        Log.d(AppConst.LOG_TAG, baseResponse.code + " :: insertUpdatePushToken");
                    }

                    @Override
                    public void onFailure(Call<BaseResponse> call, Throwable t) {
                        call.cancel();
                    }
                });

            }
        });
    }




    //파일업로드
    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == RESULT_OK) {
            if (requestCode == FILECHOOSER_NORMAL_REQ_CODE) {
                if (filePathCallbackNormal == null) return ;
                Uri result = (data == null || resultCode != RESULT_OK) ? null : data.getData();
                filePathCallbackNormal.onReceiveValue(result);
                filePathCallbackNormal = null;
            } else if (requestCode == FILECHOOSER_LOLLIPOP_REQ_CODE) {
                if (filePathCallbackLollipop == null) return ;
                filePathCallbackLollipop.onReceiveValue(WebChromeClient.FileChooserParams.parseResult(resultCode, data));
                filePathCallbackLollipop = null;
            }
        } else {
            if (filePathCallbackLollipop != null) {
                filePathCallbackLollipop.onReceiveValue(null);
                filePathCallbackLollipop = null;
            }
        }

        mCallbackManager.onActivityResult(requestCode, resultCode, data);



    }

    public void loginwithfacebook(){
        LoginManager loginManager = LoginManager.getInstance();
        loginManager.logInWithReadPermissions(MainActivity.this,
                Arrays.asList("public_profile", "email"));
        //loginManager.registerCallback(mCallbackManager, mLoginCallback);
        mLoginCallback.setParentActivity(this);
        loginManager.registerCallback(mCallbackManager, mLoginCallback);
    }

    public void facebookLoginOk(String json){
        this.webview.loadUrl("javascript:loginWidthFacebookSDK('"+json+"')");
    }

    @Override
    protected void onDestroy() {
        mixpanel.flush();
        client.logEvent("APP Closed");
        client.setUserId(null);
        super.onDestroy();
    }

    public class WebAppInterface {
        Context mContext;

        WebAppInterface(Context c) {
            mContext = c;
        }

        /** 레퍼러 가져 오기  */
        @JavascriptInterface
        public String getReferrer() {

            return m_strReferrerUrl;
        }

        /** 핀크럭스  */
        @JavascriptInterface
        public void inappEvent(String event_type, String event_data) {

            /*
            AlertDialog.Builder dlg = new AlertDialog.Builder(MainActivity.this);
            dlg.setTitle("inapp_message 시작"); //제목
            dlg.setMessage(event_type + " " + event_data); // 메시지
            dlg.setPositiveButton("확인",new DialogInterface.OnClickListener(){
                public void onClick(DialogInterface dialog, int which) {
                  dialog.cancel();;
                }
            });
            dlg.show();
*/

            tracking.inappevent(mContext, event_type, event_data);
/*
            dlg.setTitle("inapp_message 종료"); //제목
            dlg.setMessage(event_type + " " + event_data); // 메시지
            dlg.setPositiveButton("확인",new DialogInterface.OnClickListener(){
                public void onClick(DialogInterface dialog, int which) {
                    dialog.cancel();;
                }
            });
            dlg.show();
 */
        }

        /** 앱스플라이어 이벤트  */
        @JavascriptInterface
        public void recordEvent(String name, String json){
            Map<String, Object> params = null;
            if(json!=null) {
                try {
                    JSONObject jsonObject = new JSONObject(json);
                    params = new HashMap<>();
                    Iterator keys = jsonObject.keys();
                    while (keys.hasNext()) {
                        String key = (String) keys.next();
                        Object value = jsonObject.opt(key);
                        params.put(key, value);
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
            AppsFlyerLib.getInstance().logEvent(this.mContext, name, params);
        }

        //amplitude
        @JavascriptInterface
        public void setAmplitude(String no)
        {
            if( bInitialAmpllitue.equals(true) ) return;

            bInitialAmpllitue = true;

//            AlertDialog.Builder dlg = new AlertDialog.Builder(MainActivity.this);
//            dlg.setTitle("setAmplitude 시작"); //제목
//            dlg.setMessage(no + " " + strLaunchedType); // 메시지
//
//            dlg.setPositiveButton("확인",new DialogInterface.OnClickListener(){
//                public void onClick(DialogInterface dialog, int which) {
//                    dialog.cancel();;
//                }
//            });
//            dlg.show();

            if ( no != null && no.equals( "no" ) == false){
                m_no = no;
                client.setUserId(m_no);
            }
//            String userId = client.getUserId();
//            if ( userId != null )
//            {
//                if ( userId.equals(no) == true )
//                    return;
//            }

            JSONObject obj = new JSONObject();
            try
            {
                obj.put("채널", strLaunchedType);
            } catch ( JSONException e)
            {
                e.printStackTrace();
                client.logEvent("APP Open");
                return ;
            }

            client.logEvent("APP Open", obj );
        }


        //amplitude client userId.
        @JavascriptInterface
        public void setUserID(String userID)
        {

          client.setUserId(userID);
          String id = client.getUserId();
//            AlertDialog.Builder dlg = new AlertDialog.Builder(MainActivity.this);
//            dlg.setTitle(""); //제목
//            dlg.setMessage(id); // 메시지
//            dlg.setPositiveButton("확인",new DialogInterface.OnClickListener(){
//                public void onClick(DialogInterface dialog, int which) {
//                    dialog.cancel();;
//                }
//            });
//            dlg.show();
        }


        @JavascriptInterface
        public void ampilitude_logEvent(String type, String json )
        {
//            AlertDialog.Builder dlg = new AlertDialog.Builder(MainActivity.this);
//            dlg.setTitle("ampilitude_logEvent "); //제목
//            dlg.setMessage(type + " " + json); // 메시지
//            dlg.setPositiveButton("확인",new DialogInterface.OnClickListener(){
//                public void onClick(DialogInterface dialog, int which) {
//                    dialog.cancel();;
//                }
//            });
//            dlg.show();

            if(json!=null) {
                try {
                    JSONObject jsonObject = new JSONObject(json);
                    client.logEvent(type, jsonObject );
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }

        //amplitude client null.
        @JavascriptInterface
        public void setAmplitudeNull()
        {
//            AlertDialog.Builder dlg = new AlertDialog.Builder(MainActivity.this);
//            dlg.setTitle("setAmplitudeNull 시작"); //제목
//            dlg.setMessage("null"); // 메시지
//            dlg.setPositiveButton("확인",new DialogInterface.OnClickListener(){
//                public void onClick(DialogInterface dialog, int which) {
//                    dialog.cancel();;
//                }
//            });
//            dlg.show();
            client.setUserId(null);
        }
    }



}
