package com.vision.channel_msg;

import android.annotation.TargetApi;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.BitmapFactory;
import android.os.Build;
import android.provider.Settings;
import android.util.Log;
import android.widget.Toast;

import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import com.facebook.react.bridge.Callback;
import com.facebook.react.bridge.Arguments;
import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.ReactContext;
import com.facebook.react.bridge.ReactContextBaseJavaModule;
import com.facebook.react.bridge.ReactMethod;
import com.facebook.react.bridge.ReadableMap;
import com.facebook.react.bridge.WritableMap;
import com.facebook.react.modules.core.DeviceEventManagerModule;

import javax.annotation.Nullable;


/**
 * MyDemo   com.vision.channel_msg
 * Created by vision on 2019/9/2 17:10   永无bug
 */
public class ChannelMsgPlugin extends ReactContextBaseJavaModule {
    private String smallIcon = null;
    private String largeIcon = null;
    private Intent broadcastIntent = new Intent();
    private NotificationCompat.Builder builder;
    private IntentFilter intentFilter;
    private static String BROADCASTNAME = "com.vision.broadcast";

    public ChannelMsgPlugin(ReactApplicationContext reactContext) {
        super(reactContext);
    }

    @Override
    public String getName() {
        return "ChannelPlugin";
    }

    @ReactMethod
    public void createNotificationChannel(String channelId,String chanelName,int importance) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            Log.e("ChannelMsg","通知渠道设置成功！");
            NotificationChannel channel = new NotificationChannel(channelId,chanelName,importance);
            NotificationManager notificationManager = (NotificationManager) getReactApplicationContext().getSystemService(Context.NOTIFICATION_SERVICE);
            notificationManager.createNotificationChannel(channel);
        } else {
            Log.e("ChannelMsgErr","该系统版本不能设置通知渠道！");
        }
    }

    /**
     * @param smallIcon 通知左侧图标（必传）
     * @param largeIcon 通知右侧图标,内容图标（必传）
     */
    @ReactMethod
    public void setIcon(String smallIcon,String largeIcon) {
        if (smallIcon == null || smallIcon == "") {
            Log.e("ChannelMsgErr","setIcon参数不能为空");
            return;
        }
        this.smallIcon = smallIcon;
        this.largeIcon = largeIcon;
    }

    /**
     * 发送消息
     * @param map  传入的对象。需包含以下
     * @param contentTitle  内容标题
     * @param contentText   内容正文
     * @param smallIcon     消息左侧图标（调用了setIcon方法后可不传）
     * @param msgId  消息Id（可选，Int类型，区分消息类型,不区分会导致不同的消息会被合并）
     * @param channelId     渠道ID（可选，渠道消息用）
     * @param nitificationInfo     消息信息（可选，点击消息的时候回传此标识）
     */
    @ReactMethod
    public void sendNotification(ReadableMap map){
        try{
            getReactApplicationContext().unregisterReceiver(MyReceiver);
        }catch (IllegalArgumentException e){
        }
        intentFilter = new IntentFilter(BROADCASTNAME);
        getReactApplicationContext().registerReceiver(MyReceiver,intentFilter);
        broadcastIntent.setAction(BROADCASTNAME);
        if ((smallIcon == null || smallIcon == "") && !map.hasKey("smallIcon")) {
            Log.e("ChannelMsgErr","集合中必须传入smallIcon或者调用setIcon来设置通知栏图标");
            return;
        }
        if (map.hasKey("smallIcon")){
            this.smallIcon = map.getString("smallIcon");
            Log.e("ChannelMsg","图标获取成功！");
        }
        if (map.hasKey("nitificationInfo")){
            broadcastIntent.putExtra("notificationMsg", map.getString("nitificationInfo"));
            Log.e("ChannelMsg","nitificationInfo设置成功！");
        }
        String contentTitle = map.getString("contentTitle");
        String contentText = map.getString("contentText");
        PendingIntent pendingIntent = PendingIntent.getBroadcast(getReactApplicationContext(),0,broadcastIntent,PendingIntent.FLAG_UPDATE_CURRENT);
        NotificationManager manager = (NotificationManager) getReactApplicationContext().getSystemService(Context.NOTIFICATION_SERVICE);
        if (map.hasKey("channelId")){
            builder = new NotificationCompat.Builder(getReactApplicationContext(),map.getString("channelId"));
        }else {
            builder = new NotificationCompat.Builder(getReactApplicationContext());
        }
        builder.setContentTitle(contentTitle);
        builder.setContentText(contentText);
        builder.setWhen(System.currentTimeMillis());
        builder.setContentIntent(pendingIntent);
        builder.setSmallIcon(getResource(this.smallIcon));
        builder.setAutoCancel(true);
        if (map.hasKey("largeIcon")) {
            builder.setLargeIcon(BitmapFactory.decodeResource(getReactApplicationContext().getResources(),getResource(map.getString("largeIcon"))));
            Log.e("ChannelMsg","largeIcon设置成功！");
        }
        if (map.hasKey("msgId")){
            manager.notify(map.getInt("msgId"),builder.build());
        }else {
            manager.notify(1,builder.build());
        }
        Log.e("ChannelMsg","发送通知！");
    }

    /**
     * 检查渠道通知是否被关闭
     * @param channel   渠道消息ID
     */
    @ReactMethod
    public void censorNotification(String channelType,Callback callback) {
        //判断消息总开关是否关闭
        if (channelType == null) {
            if (!NotificationManagerCompat.from(getReactApplicationContext()).areNotificationsEnabled()){
                callback.invoke("AllClose");
            }else {
                callback.invoke("Normal");
            }
            return;
        }
        NotificationManager manager = (NotificationManager) getReactApplicationContext().getSystemService(Context.NOTIFICATION_SERVICE);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = manager.getNotificationChannel(channelType);
            if (channel.getImportance() == NotificationManager.IMPORTANCE_NONE) {
                // Intent intent = new Intent(Settings.ACTION_CHANNEL_NOTIFICATION_SETTINGS);
                // intent.putExtra(Settings.EXTRA_APP_PACKAGE, getReactApplicationContext().getPackageName());
                // intent.putExtra(Settings.EXTRA_CHANNEL_ID, channel.getId());
                // getReactApplicationContext().startActivity(intent);
                // Toast.makeText(getReactApplicationContext(),"请手动开启消息通知！",Toast.LENGTH_SHORT).show();
                callback.invoke("Close");
            }else{
                callback.invoke("Normal");
            }
        }
    }

    /**
     * 去往系统通知设置界面
     * @param channelId 去往该渠道通知的设置，传null则去到应用通知设置
     * @return
     */
    @ReactMethod
    public void inSettings(String channelType) {
        if (channelType == null) {
            Intent intent = new Intent(Settings.ACTION_APP_NOTIFICATION_SETTINGS);
            intent.putExtra(Settings.EXTRA_APP_PACKAGE,getReactApplicationContext().getPackageName());
            getReactApplicationContext().startActivity(intent);
            return;
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationManager manager = (NotificationManager) getReactApplicationContext().getSystemService(Context.NOTIFICATION_SERVICE);
            NotificationChannel channel = manager.getNotificationChannel(channelType);
            Intent intent = new Intent(Settings.ACTION_CHANNEL_NOTIFICATION_SETTINGS);
            intent.putExtra(Settings.EXTRA_APP_PACKAGE, getReactApplicationContext().getPackageName());
            intent.putExtra(Settings.EXTRA_CHANNEL_ID, channel.getId());
            getReactApplicationContext().startActivity(intent);
        }
    }

    public int getResource(String imgName) {
        Context context = getReactApplicationContext();
        int resourceId = context.getResources().getIdentifier(imgName, "drawable",context.getPackageName());
        return resourceId;
    }

    public BroadcastReceiver MyReceiver = new BroadcastReceiver() {
        private String msgKey = null;
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equals(BROADCASTNAME)){
                msgKey = intent.getStringExtra("notificationMsg");
                WritableMap params= Arguments.createMap();
                Log.e("广播消息",msgKey);
                if (msgKey!=null){
                    params.putString("msgKey", msgKey);
                }else {
                    params.putString("msgKey", "消息被点击！");
                }
                sendEvent(getReactApplicationContext(),"notificationMsg", params);
            }
        }

        private void sendEvent(ReactContext reactContext, String eventName, @Nullable WritableMap params){
            reactContext.getJSModule(DeviceEventManagerModule.RCTDeviceEventEmitter.class)
                    .emit(eventName,params);
        }
    };

}
