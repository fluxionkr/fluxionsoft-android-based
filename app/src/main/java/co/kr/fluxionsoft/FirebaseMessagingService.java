package co.kr.fluxionsoft;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;

import com.appboy.AppboyFirebaseMessagingService;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.InstanceIdResult;
import com.google.firebase.messaging.RemoteMessage;
import co.kr.fluxionsoft.R;
import com.mixpanel.android.mpmetrics.MPConfig;
import com.mixpanel.android.mpmetrics.ResourceIds;

import static com.mixpanel.android.mpmetrics.MixpanelFCMMessagingService.addToken;


public class FirebaseMessagingService extends com.google.firebase.messaging.FirebaseMessagingService {
    private static final String TAG = "FirebaseMsgService";

    protected static class NotificationData {
        private NotificationData(int anIcon, int aLargeIcon, int aWhiteIcon, CharSequence aTitle, String aMessage, Intent anIntent, int aColor) {
            icon = anIcon;
            largeIcon = aLargeIcon;
            whiteIcon = aWhiteIcon;
            title = aTitle;
            message = aMessage;
            intent = anIntent;
            color = aColor;
        }

        public final int icon;
        public final int largeIcon;
        public final int whiteIcon;
        public final CharSequence title;
        public final String message;
        public final Intent intent;
        public final int color;

        public static final int NOT_SET = -1;
    }

    /* package */ static void init() {
        FirebaseInstanceId.getInstance().getInstanceId()
                .addOnCompleteListener(new OnCompleteListener<InstanceIdResult>() {
                    @Override
                    public void onComplete(Task<InstanceIdResult> task) {
                        if (task.isSuccessful()) {
                            String registrationId = task.getResult().getToken();
                            addToken(registrationId);
                        }
                    }
                });
    }

    @Override
    public void onNewToken(String token) {
        super.onNewToken(token);
        //MPLog.d(LOGTAG, "MP FCM on new push token: " + token);
        addToken(token);
    }

    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        System.out.println("===========");
        System.out.println("=========== data : >> " + remoteMessage.getData());
        System.out.println("=========== data : >> " + remoteMessage.getFrom());


        super.onMessageReceived(remoteMessage);
        if (AppboyFirebaseMessagingService.handleBrazeRemoteMessage(this, remoteMessage)) {
            // This Remote Message originated from Braze and a push notification was displayed.
            // No further action is needed.
        } else {
            // This Remote Message did not originate from Braze.
            // No action was taken and you can safely pass this Remote Message to other handlers.

            onMessageReceived(getApplicationContext(), remoteMessage.toIntent());
        }

        //onMessageReceived(getApplicationContext(), remoteMessage.toIntent());
    }

    protected void onMessageReceived(Context context, Intent intent) {
        showPushNotification(context, intent);
    }

    public static void showPushNotification(Context context, Intent messageIntent) {
        final MPConfig config = MPConfig.getInstance(context);
        String resourcePackage = config.getResourcePackageName();
        if (null == resourcePackage) {
            resourcePackage = context.getPackageName();
        }

        //final ResourceIds drawableIds = new ResourceReader.Drawables(resourcePackage, context);
        final Context applicationContext = context.getApplicationContext();
        final Notification notification = buildNotification(applicationContext, messageIntent, null);

        if (null != notification) {
            final NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
            notificationManager.notify(0, notification);
        }
    }



    private void sendNotification(String title, String body, String goodsno,String orderPath) {
        Intent intent = new Intent(this, IntroActivity.class);


        if(goodsno != null){
            intent.putExtra("goodsno", goodsno);
        }

        if(orderPath != null){
            intent.putExtra("orderPath", orderPath);
        }


    }



    private static Notification buildNotification(Context context, Intent inboundIntent, ResourceIds iconIds) {
        final PackageManager manager = context.getPackageManager();
        Intent defaultIntent =  manager.getLaunchIntentForPackage(context.getPackageName());

        final FirebaseMessagingService.NotificationData notificationData = readInboundIntent(context, inboundIntent, iconIds, defaultIntent);
        if (null == notificationData) {
            return null;
        }

        //MPLog.d(LOGTAG, "MP FCM notification received: " + notificationData.message);
        final PendingIntent contentIntent = PendingIntent.getActivity(
                context,
                0,
                notificationData.intent,
                PendingIntent.FLAG_UPDATE_CURRENT
        );

        final Notification notification;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            notification = makeNotificationSDK26OrHigher(context, contentIntent, notificationData);
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            notification = makeNotificationSDK21OrHigher(context, contentIntent, notificationData);
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            notification = makeNotificationSDK16OrHigher(context, contentIntent, notificationData);
        } else {
            notification = makeNotificationSDK11OrHigher(context, contentIntent, notificationData);
        }

        return notification;
    }

    /* package */ static FirebaseMessagingService.NotificationData readInboundIntent(Context context, Intent inboundIntent, ResourceIds iconIds, Intent defaultIntent) {
        final PackageManager manager = context.getPackageManager();

        final String message = inboundIntent.getStringExtra("mp_message");
        final String iconName = inboundIntent.getStringExtra("mp_icnm");
        final String largeIconName = inboundIntent.getStringExtra("mp_icnm_l");
        final String whiteIconName = inboundIntent.getStringExtra("mp_icnm_w");
        final String uriString = inboundIntent.getStringExtra("mp_cta");
        CharSequence notificationTitle = inboundIntent.getStringExtra("mp_title");
        final String colorName = inboundIntent.getStringExtra("mp_color");
        final String campaignId = inboundIntent.getStringExtra("mp_campaign_id");
        final String messageId = inboundIntent.getStringExtra("mp_message_id");
        final String extraLogData = inboundIntent.getStringExtra("mp");
        final String goodsno = inboundIntent.getStringExtra("goodsno");
        final String link = inboundIntent.getStringExtra("link");
        int color = FirebaseMessagingService.NotificationData.NOT_SET;

        //trackCampaignReceived(campaignId, messageId, extraLogData);
//                .setSmallIcon(R.drawable.small_icon)
//                .setLargeIcon(BitmapFactory.decodeResource(getResources(), R.drawable.large_icon))
//                .setColor(ContextCompat.getColor(getApplicationContext(), R.color.colorPrimary))


        if (colorName != null) {
            try {
                color = Color.parseColor(colorName);
            } catch (IllegalArgumentException e) {}
        }

        if (message == null) {
            return null;
        }

        int notificationIcon = -1;
        if (null != iconName) {
            if (iconIds.knownIdName(iconName)) {
                notificationIcon = iconIds.idFromName(iconName);
            }
        }

        int largeNotificationIcon = FirebaseMessagingService.NotificationData.NOT_SET;
        if (null != largeIconName) {
            if (iconIds.knownIdName(largeIconName)) {
                //largeNotificationIcon = iconIds.idFromName(largeIconName);
                largeNotificationIcon = R.drawable.large_icon;
            }
        }

        int whiteNotificationIcon = FirebaseMessagingService.NotificationData.NOT_SET;
        if (null != whiteIconName) {
            if (iconIds.knownIdName(whiteIconName)) {
                //whiteNotificationIcon = iconIds.idFromName(whiteIconName);
                whiteNotificationIcon = R.drawable.small_icon;
            }
        }

        ApplicationInfo appInfo;
        try {
            appInfo = manager.getApplicationInfo(context.getPackageName(), 0);
        } catch (final PackageManager.NameNotFoundException e) {
            appInfo = null;
        }

        if (notificationIcon == FirebaseMessagingService.NotificationData.NOT_SET && null != appInfo) {
            notificationIcon = appInfo.icon;
        }

        if (notificationIcon == FirebaseMessagingService.NotificationData.NOT_SET) {
            notificationIcon = android.R.drawable.sym_def_app_icon;
        }

        if (null == notificationTitle && null != appInfo) {
            notificationTitle = manager.getApplicationLabel(appInfo);
        }

        if (null == notificationTitle) {
            notificationTitle = "A message for you";
        }

        Uri uri = null;
        if (null != uriString) {
            uri = Uri.parse(uriString);
        }
        final Intent intent;
        if (null == uri) {
            intent = defaultIntent;
        } else {
            intent = new Intent(Intent.ACTION_VIEW, uri);
        }

        final Intent notificationIntent = buildNotificationIntent(intent, campaignId, messageId, extraLogData,goodsno,link);

        return new FirebaseMessagingService.NotificationData(notificationIcon, largeNotificationIcon, whiteNotificationIcon, notificationTitle, message, notificationIntent, color);
    }

    private static Intent buildNotificationIntent(Intent intent, String campaignId, String messageId, String extraLogData,String goodsno,String link) {
        if (campaignId != null) {
            intent.putExtra("mp_campaign_id", campaignId);
        }

        if (messageId != null) {
            intent.putExtra("mp_message_id", messageId);
        }

        if (extraLogData != null) {
            intent.putExtra("mp", extraLogData);
        }

        if (goodsno != null) {
            intent.putExtra("goodsno", goodsno);
        }

        if (link != null) {
            intent.putExtra("link", link);
        }

        intent.putExtra("notification", "Y");

        return intent;
    }

    /**
     * Mixpanel Notification Builder
     */
    @SuppressLint("NewApi")
    @TargetApi(26)
    protected static Notification makeNotificationSDK26OrHigher(Context context, PendingIntent intent, FirebaseMessagingService.NotificationData notificationData) {
        NotificationManager mNotificationManager =
                (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

        String channelId = MPConfig.getInstance(context).getNotificationChannelId();
        String channelName = MPConfig.getInstance(context).getNotificationChannelName();
        int importance = MPConfig.getInstance(context).getNotificationChannelImportance();

        NotificationChannel channel = new NotificationChannel(channelId, channelName, importance);
        int notificationDefaults = MPConfig.getInstance(context).getNotificationDefaults();
        if (notificationDefaults == Notification.DEFAULT_VIBRATE || notificationDefaults == Notification.DEFAULT_ALL) {
            channel.enableVibration(true);
        }
        if (notificationDefaults == Notification.DEFAULT_LIGHTS || notificationDefaults == Notification.DEFAULT_ALL) {
            channel.enableLights(true);
            channel.setLightColor(Color.WHITE);
        }
        mNotificationManager.createNotificationChannel(channel);

        final Notification.Builder builder = new Notification.Builder(context).
                setTicker(notificationData.message).
                setWhen(System.currentTimeMillis()).
                setContentTitle(notificationData.title).
                setContentText(notificationData.message).
                setContentIntent(intent).
                setStyle(new Notification.BigTextStyle().bigText(notificationData.message)).
                setChannelId(channelId);

        if (notificationData.whiteIcon != FirebaseMessagingService.NotificationData.NOT_SET) {
            builder.setSmallIcon(notificationData.whiteIcon);
        } else {
            builder.setSmallIcon(notificationData.icon);
        }

        if (notificationData.largeIcon != FirebaseMessagingService.NotificationData.NOT_SET) {
            builder.setLargeIcon(BitmapFactory.decodeResource(context.getResources(), notificationData.largeIcon));
        }

        if (notificationData.color != FirebaseMessagingService.NotificationData.NOT_SET) {
            builder.setColor(notificationData.color);
        }

        final Notification n = builder.build();
        n.flags |= Notification.FLAG_AUTO_CANCEL;
        return n;
    }

    @SuppressWarnings("deprecation")
    @TargetApi(11)
    protected static Notification makeNotificationSDK11OrHigher(Context context, PendingIntent intent, FirebaseMessagingService.NotificationData notificationData) {
        final Notification.Builder builder = new Notification.Builder(context).
                setSmallIcon(notificationData.icon).
                setTicker(notificationData.message).
                setWhen(System.currentTimeMillis()).
                setContentTitle(notificationData.title).
                setContentText(notificationData.message).
                setContentIntent(intent).
                setDefaults(MPConfig.getInstance(context).getNotificationDefaults());

        if (notificationData.largeIcon != FirebaseMessagingService.NotificationData.NOT_SET) {
            builder.setLargeIcon(BitmapFactory.decodeResource(context.getResources(), notificationData.largeIcon));
        }

        final Notification n = builder.getNotification();
        n.flags |= Notification.FLAG_AUTO_CANCEL;
        return n;
    }

    @SuppressLint("NewApi")
    @TargetApi(16)
    protected static Notification makeNotificationSDK16OrHigher(Context context, PendingIntent intent, FirebaseMessagingService.NotificationData notificationData) {
        final Notification.Builder builder = new Notification.Builder(context).
                setSmallIcon(notificationData.icon).
                setTicker(notificationData.message).
                setWhen(System.currentTimeMillis()).
                setContentTitle(notificationData.title).
                setContentText(notificationData.message).
                setContentIntent(intent).
                setStyle(new Notification.BigTextStyle().bigText(notificationData.message)).
                setDefaults(MPConfig.getInstance(context).getNotificationDefaults());

        if (notificationData.largeIcon != FirebaseMessagingService.NotificationData.NOT_SET) {
            builder.setLargeIcon(BitmapFactory.decodeResource(context.getResources(), notificationData.largeIcon));
        }

        final Notification n = builder.build();
        n.flags |= Notification.FLAG_AUTO_CANCEL;
        return n;
    }

    @SuppressLint("NewApi")
    @TargetApi(21)
    protected static Notification makeNotificationSDK21OrHigher(Context context, PendingIntent intent, FirebaseMessagingService.NotificationData notificationData) {
        final Notification.Builder builder = new Notification.Builder(context).
                setTicker(notificationData.message).
                setWhen(System.currentTimeMillis()).
                setContentTitle(notificationData.title).
                setContentText(notificationData.message).
                setContentIntent(intent).
                setStyle(new Notification.BigTextStyle().bigText(notificationData.message)).
                setDefaults(MPConfig.getInstance(context).getNotificationDefaults());

        if (notificationData.whiteIcon != FirebaseMessagingService.NotificationData.NOT_SET) {
            builder.setSmallIcon(notificationData.whiteIcon);
        } else {
            builder.setSmallIcon(notificationData.icon);
        }

        if (notificationData.largeIcon != FirebaseMessagingService.NotificationData.NOT_SET) {
            builder.setLargeIcon(BitmapFactory.decodeResource(context.getResources(), notificationData.largeIcon));
        }

        if (notificationData.color != FirebaseMessagingService.NotificationData.NOT_SET) {
            builder.setColor(notificationData.color);
        }

        final Notification n = builder.build();
        n.flags |= Notification.FLAG_AUTO_CANCEL;
        return n;
    }


}

