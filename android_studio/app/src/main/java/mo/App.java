package mo;

import android.app.Application;

import com.eskyfun.sdk.EskyfunSDK;
import com.t.common.SdkUser;
import com.t.listener.AccountListener;
import com.t.listener.PaymentListener;

import layaair.game.browser.ConchJNI;

public class App extends Application {

    public static String platform;
    public static String userId;
    public static String token;
    public static SdkUser user;
    public static boolean isInited;
    @Override
    public void onCreate() {
        super.onCreate();
        System.out.print("start app");
        String clientId = "1083";
        App.platform = "eskyfun";

        EskyfunSDK.initSDK(clientId);
        EskyfunSDK.getInstance().setAccountListener(new AccountListener() {
            @Override
            public void didLoginSuccess(SdkUser user) {
                // 用户登录成功
                App.userId = user.getUserid();
                App.token = user.getToken();
                App.user = user;
                if(App.isInited){
//                    JSBridge.hideSplash();
                    ConchJNI.RunJS("app.SDK.onLoginSuc('"+user.toCacheJson()+"')");
                }

            }

            @Override
            public void didLogout() {
                App.userId = null;
                App.token = null;
                JSBridge.showSplash();
                if(App.isInited) {
                    ConchJNI.RunJS("app.SDK.onLogout()");
                }
            }
        });

        EskyfunSDK.getInstance().setPaymentListener(new PaymentListener() {
            @Override
            public void setupHelperFailed() {
                // GooglePlay InAppBilling初始化失败
                // 可能原因有：设备未安装GooglePlay框架、GooglePlay已经识别到当前为国内网络
                // 在国外的用户从GooglePlay中下载的App，不会有这种情况的发现
                // 国内用户需要安装GooglePlay以及使用VPN网络解决问题
                if(App.isInited) {
                    ConchJNI.RunJS("app.SDK.error('setupHelperFailed')");
                }
            }

            @Override
            public void paymentStart(String productId) {
                // 支付开始，可在此处添加代码显示loading
                if(App.isInited) {
                    ConchJNI.RunJS("app.SDK.onEvt('payStart','" + productId + "')");
                }
            }

            @Override
            public void paymentFailed(String result) {
                if(App.isInited) {
                    // GooglePlay支付失败
                    ConchJNI.RunJS("app.SDK.onEvt('payFailed')");
                }
            }

            @Override
            public void paymentSuccess(String productId) {
                if(App.isInited) {
                    // GooglePlay支付成功，但需要服务端进一步验证
                    ConchJNI.RunJS("app.SDK.onEvt('paySuc')");
                }
            }

            @Override
            public void otherPaymentFinish() {
                if (App.isInited) {
                    // 第三方支付结束（包括用户支付取消，失败，成功）
                    ConchJNI.RunJS("app.SDK.onEvt('payEnd3rd')");
                }
            }
        });
    }
}
