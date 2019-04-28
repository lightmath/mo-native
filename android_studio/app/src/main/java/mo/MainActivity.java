package mo;
import java.io.InputStream;

import layaair.autoupdateversion.AutoUpdateAPK;
import layaair.game.IMarket.IPlugin;
import layaair.game.IMarket.IPluginRuntimeProxy;
import layaair.game.Market.GameEngine;
import layaair.game.config.config;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.webkit.ValueCallback;

import com.ictitan.union.IctitanUnionSDK;
import com.ictitan.union.callback.IIctitanUnionListener;
import com.ictitan.union.callback.IctitanUnionPermissionCallback;
import com.ictitan.union.constant.RoleEventType;
import com.ictitan.union.constant.UnionSDKCallbackCode;
import com.ictitan.union.entity.IctitanUnionPaymentParam;
import com.ictitan.union.entity.IctitanUnionRoleInfoParam;
import com.ictitan.union.entity.UnionSdkUser;
import com.ictitan.union.util.FileData;
import com.ictitan.union.util.JJJson;


public class MainActivity extends Activity{
    public static final int AR_CHECK_UPDATE = 1;
    private IPlugin mPlugin = null;
    private IPluginRuntimeProxy mProxy = null;
    boolean isLoad=false;
    boolean isExit=false;
    public static UnionSdkUser user;
    public static boolean isInited;
    private Activity activity;
    public static SplashDialog mSplashDialog;
    public static String UID;
    public static String Token;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().requestFeature(Window.FEATURE_NO_TITLE);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        JSBridge.mMainActivity = this;
        if (Build.VERSION.SDK_INT >= 23) {
            int REQUEST_CODE_CONTACT = 101;
            String[] permissions = {
                    Manifest.permission.WRITE_EXTERNAL_STORAGE,
                    Manifest.permission.MOUNT_UNMOUNT_FILESYSTEMS
            };
            //验证是否许可权限
            for (String str : permissions) {
                if (this.checkSelfPermission(str) != PackageManager.PERMISSION_GRANTED) {
                    //申请权限
                    this.requestPermissions(permissions, REQUEST_CODE_CONTACT);
                }
            }
        }
        mSplashDialog = new SplashDialog(this);
        mSplashDialog.showSplash();

        /*
         * 如果不想使用更新流程，可以屏蔽checkApkUpdate函数，直接打开initEngine函数
         */
//        checkApkUpdate(this);
        activity = this;
        PlatformInit("");
        initEngine();
    }

    public void PlatformInit(String initextension) {
        // 回调
        IctitanUnionSDK.getInstance().setSDKListener(new IIctitanUnionListener() {
            // 初始化回调
            @Override
            public void IctitanUnionInitCallback(int code, String result) {
                switch (code) {
                    case UnionSDKCallbackCode.CODE_INIT_SUCCESS:
                        //初始化成功。初始化成功后才可调用登陆接口
                        Log.e("UnionInitCallback", "CODE_INIT_SUCCESS");
                        break;
                    default:
                        Log.e("UnionInitCallback", "code="+code);
                        break;
                }
            }

            // 登陆回调
            @Override
            public void IctitanUnionLoginCallback(int code, String result, UnionSdkUser unionSdkUser) {
                switch (code) {
                    case UnionSDKCallbackCode.CODE_LOGIN_SUCCESS:
                        MainActivity.UID = unionSdkUser.getUserId();
                        MainActivity.Token = unionSdkUser.getToken();
                        MainActivity.user = unionSdkUser;
                        String gameid = unionSdkUser.getAppId();
                        String Channelid = unionSdkUser.getChannelId();
                        Log.e("LOGIN_SUCCESS", " MainActivity.UID = " + MainActivity.UID + "\n" + " token = " + Token + "\n");
                        JSBridge.onLoginSuc();
                        break;
                    case UnionSDKCallbackCode.CODE_LOGIN_FAIL:
                        Log.e("UnionLoginCallback", "login fail:" + result);
                        break;
                    case UnionSDKCallbackCode.CODE_LOGIN_CANCEL:
                        Log.e("UnionLoginCallback", "login cancel:" + result);
                        break;
                    case UnionSDKCallbackCode.CODE_LOGIN_TIMEOUT:
                        Log.e("UnionLoginCallback", "login timeout:" + result);
                        break;
                    default:
                        break;
                }
            }

            // 支付回调
            @Override
            public void IctitanUnionPayCallback(int code, String result) {
                switch (code) {
                    case UnionSDKCallbackCode.CODE_PAY_SUCCESS:
                        Log.e("UnionPayCallback", "pay success:" + result);
                        break;
                    case UnionSDKCallbackCode.CODE_PAY_FAIL:
                        Log.e("UnionPayCallback", "pay fail:" + result);
                        break;
                    case UnionSDKCallbackCode.CODE_PAY_CANCEL:
                        Log.e("UnionPayCallback", "pay cancel:" + result);
                        break;
                    default:
                        break;
                }
            }

            @Override
            public void IctitanUnionLogoutCallback(int code, String result) {
                switch (code) {
                    case UnionSDKCallbackCode.CODE_LOGOUT_SUCCESS:
                        Log.e("UnionLogoutCallbac", "logout success:" + result);
                        break;
                    case UnionSDKCallbackCode.CODE_LOGOUT_FAIL:
                        Log.e("UnionLogoutCallbac", "logout fail:" + result);
                        break;
                    default:
                        break;
                }
            }

            @Override
            public void IctitanUnionExitGameCallback(int code, String result) {
                switch (code) {
                    case UnionSDKCallbackCode.CODE_EXIT_SUCCESS:
                        Log.e("UnionExitGameCallb", "exit success:" + result);
                        finish();
                        System.exit(0);
                        break;
                    case UnionSDKCallbackCode.CODE_EXIT_FAIL:
                        Log.e("UnionExitGameCallb", "exit fail:" + result);
                        break;
                    default:
                        break;
                }

            }

            @Override
            public void IctitanUnionShareToSocialNetworkCallback(int code, String result) {
                JSBridge.onFaceBookShareBack(code, result);
            }
        });
        // 初始化
        IctitanUnionSDK.getInstance().init(activity);
    }

    public void initEngine()
    {
        mProxy = new RuntimeProxy(this);
        mPlugin = new GameEngine(this);
        mPlugin.game_plugin_set_runtime_proxy(mProxy);
        mPlugin.game_plugin_set_option("localize","false");
//        mPlugin.game_plugin_set_option("gameUrl", "http://192.168.5.135:9876/index.html");
//        mPlugin.game_plugin_set_option("gameUrl", "http://test1.webgame.zhaouc.com/moli/client/native.html");
//        mPlugin.game_plugin_set_option("gameUrl", "http://61.160.219.98/testmoli/client/native_debug.html");
//        mPlugin.game_plugin_set_option("gameUrl", "http://61.160.219.98/testmoli/client/native.html");
//        mPlugin.game_plugin_set_option("gameUrl", "http://61.160.219.98/testmoli/update/tyclient/client/native.html");
        mPlugin.game_plugin_set_option("gameUrl", "http://61.160.219.98/testmoli/tg_client/native.html");
//        mPlugin.game_plugin_set_option("gameUrl", "http://61.160.219.98/testmoli/update/dalishen/client/native.html");

//        mPlugin.game_plugin_set_option("gameUrl", "http://192.168.1.137/native.html");
        mPlugin.game_plugin_init(5);
        View gameView = mPlugin.game_plugin_get_view();
        ViewGroup.LayoutParams params = new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);

        this.addContentView(gameView,params);
//        this.getWindow().setBackgroundDrawableResource(R.drawable.layabox);
        JSBridge.sdkLogin();
        isLoad=true;
    }

    public  boolean isOpenNetwork(Context context)
    {
        if (!config.GetInstance().m_bCheckNetwork)
            return true;
        ConnectivityManager connManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        return connManager.getActiveNetworkInfo() != null && (connManager.getActiveNetworkInfo().isAvailable() && connManager.getActiveNetworkInfo().isConnected());
    }

    public void settingNetwork(final Context context, final int p_nType)
    {
        AlertDialog.Builder pBuilder = new AlertDialog.Builder(context);
        pBuilder.setTitle("连接失败，请检查网络或与开发商联系").setMessage("是否对网络进行设置?");
        // 退出按钮
        pBuilder.setPositiveButton("是", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface p_pDialog, int arg1) {
                Intent intent;
                try {
                    String sdkVersion = android.os.Build.VERSION.SDK;
                    if (Integer.valueOf(sdkVersion) > 10) {
                        intent = new Intent(
                                android.provider.Settings.ACTION_WIRELESS_SETTINGS);
                    } else {
                        intent = new Intent();
                        ComponentName comp = new ComponentName(
                                "com.android.settings",
                                "com.android.settings.WirelessSettings");
                        intent.setComponent(comp);
                        intent.setAction("android.intent.action.VIEW");
                    }
                    ((Activity)context).startActivityForResult(intent, p_nType);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
        pBuilder.setNegativeButton("否", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
                ((Activity)context).finish();
            }
        });
        AlertDialog alertdlg = pBuilder.create();
        alertdlg.setCanceledOnTouchOutside(false);
        alertdlg.show();
    }
    public  void checkApkUpdate( Context context,final ValueCallback<Integer> callback)
    {
        if (isOpenNetwork(context)) {
            // 自动版本更新
            if ( "0".equals(config.GetInstance().getProperty("IsHandleUpdateAPK","0")) == false ) {
                Log.e("0", "==============Java流程 checkApkUpdate");
                new AutoUpdateAPK(context, new ValueCallback<Integer>() {
                    @Override
                    public void onReceiveValue(Integer integer) {
                        Log.e("",">>>>>>>>>>>>>>>>>>");
                        callback.onReceiveValue(integer);
                    }
                });
            } else {
                Log.e("0", "==============Java流程 checkApkUpdate 不许要自己管理update");
                callback.onReceiveValue(1);
            }
        } else {
            settingNetwork(context,AR_CHECK_UPDATE);
        }
    }
    public void checkApkUpdate(Context context) {
        InputStream inputStream = getClass().getResourceAsStream("/assets/config.ini");
        config.GetInstance().init(inputStream);
        checkApkUpdate(context,new ValueCallback<Integer>() {
            @Override
            public void onReceiveValue(Integer integer) {
                if (integer.intValue() == 1) {
                    initEngine();
                } else {
                    finish();
                }
            }
        });
    }
    public void onActivityResult(int requestCode, int resultCode,Intent data) {
        if (requestCode == AR_CHECK_UPDATE) {
            checkApkUpdate(this);
        }
        IctitanUnionSDK.getInstance().onActivityResult(requestCode, resultCode, data);
        super.onActivityResult(requestCode, resultCode, data);
    }

    public void onPause() {
        IctitanUnionSDK.getInstance().onPause();
        super.onPause();
        if(isLoad)mPlugin.game_plugin_onPause();
    }

    protected void onStart() {
        IctitanUnionSDK.getInstance().onStart();
        super.onStart();
    }

    protected void onResume()
    {
        IctitanUnionSDK.getInstance().onResume();
        super.onResume();
        if(isLoad)mPlugin.game_plugin_onResume();

    }

    public void onNewIntent(Intent newIntent) {
        IctitanUnionSDK.getInstance().onNewIntent(newIntent);
        super.onNewIntent(newIntent);
    }

    public void onStop() {
        IctitanUnionSDK.getInstance().onStop();
        super.onStop();
    }

    protected void onDestroy()
    {
        IctitanUnionSDK.getInstance().onDestroy();
        super.onDestroy();
        if(isLoad)mPlugin.game_plugin_onDestory();
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event)
    {
        return super.onKeyDown(keyCode, event);
    }

    public void onRestart() {
        IctitanUnionSDK.getInstance().onRestart();
        super.onRestart();
    }

    public void onBackPressed() {
        IctitanUnionSDK.getInstance().onBackPressed();
        super.onBackPressed();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }
}