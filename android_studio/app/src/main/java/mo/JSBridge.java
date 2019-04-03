package mo;

import android.app.Activity;
import android.graphics.Color;
import android.os.Handler;
import android.os.Looper;

import com.eskyfun.sdk.EskyfunSDK;
import com.t.common.PaymentParam;
import com.t.listener.FbFriendCallback;
import com.t.listener.FbInviteCallback;
import com.t.listener.FbShareListener;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;

import layaair.game.browser.ConchJNI;


public class JSBridge {
    public static Handler m_Handler = new Handler(Looper.getMainLooper());
    public static Activity mMainActivity = null;

    public static void hideSplash() {
        m_Handler.post(
                new Runnable() {
                    public void run() {
                        MainActivity.mSplashDialog.dismissSplash();
                    }
                });
    }

    public static void setFontColor(final String color) {
        m_Handler.post(
                new Runnable() {
                    public void run() {
                        MainActivity.mSplashDialog.setFontColor(Color.parseColor(color));
                    }
                });
    }

    public static void setTips(final JSONArray tips) {
        m_Handler.post(
                new Runnable() {
                    public void run() {
                        try {
                            String[] tipsArray = new String[tips.length()];
                            for (int i = 0; i < tips.length(); i++) {
                                tipsArray[i] = tips.getString(i);
                            }
                            MainActivity.mSplashDialog.setTips(tipsArray);
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                });
    }

    public static void bgColor(final String color) {
        m_Handler.post(
                new Runnable() {
                    public void run() {
                        MainActivity.mSplashDialog.setBackgroundColor(Color.parseColor(color));
                    }
                });
    }

    public static void loading(final int percent) {
        m_Handler.post(
                new Runnable() {
                    public void run() {
                        MainActivity.mSplashDialog.setPercent(percent);
                    }
                });
    }

    public static void showTextInfo(final boolean show) {
        m_Handler.post(
                new Runnable() {
                    public void run() {
                        MainActivity.mSplashDialog.showTextInfo(show);
                    }
                });
    }


    public static void startSDK(){
        App.isInited = true;
        if(App.user!= null){
            ConchJNI.RunJS("app.SDK.onLoginSuc('"+App.user.toCacheJson()+"')");
        }else{
            sdkLogin();
        }

    }

    public static String getUserId(){
        return App.userId;
    }

    public static String getToken(){
        return App.token;
    }

    public static void sdkLogin() {
        EskyfunSDK.getInstance().popLoginView();
    }
    public static void sdkLogout() {
        EskyfunSDK.getInstance().logout();
    }


    public static void selectGameServer(final String serverID ,final String serverName) {
//        ConchJNI.RunJS("alert('"+serverID + " "+ serverName +"')");
        EskyfunSDK.getInstance().reportGameServer(serverID, serverName);
    }

    public static void createGameRole( String serverID,
                                       String serverName,
                                       String roleId,
                                       String roleName,
                                       String profession) {
//        roleId = Long.decode(roleId).toString();
//        ConchJNI.RunJS("alert('"+serverID + " "+ serverName + " " + roleID + " " + roleName + " " + profession + "')");
        EskyfunSDK.getInstance().createGameRole(serverID, serverName, roleId, roleName, profession);
    }

    public static void roleLevelUpgrade(        String serverID,
                                                String serverName,
                                                String roleID,
                                                String roleName,
                                                String level) {

        EskyfunSDK.getInstance().roleLevelUpgrade(serverID, serverName, roleID, roleName, level);
    }

    public static void roleReport(        String profession ,
                                          String serverId ,
                                          String serverName ,
                                          String roleId ,
                                          String roleName,
                                          int level ) {
//        roleId = Long.decode(roleId).toString();
//        ConchJNI.RunJS("alert('" + profession + " "+serverId + " "+ serverName + " " + roleId
//                + " " + roleName + " "   + level +  "')");
        EskyfunSDK.getInstance().roleReport(profession, serverId, serverName, roleId, roleName, level);
    }

    public static void facebookShare() {
        EskyfunSDK.getInstance().shareToFb(new FbShareListener() {
            @Override
            public void onShareSuccess(String postId) {
                // 分享成功
                ConchJNI.RunJS("app.SDK.fbShareSuc('"+postId+")'");
            }

            @Override
            public void onShareError(Exception e) {
                e.printStackTrace();
                ConchJNI.RunJS("app.SDK.fbShareError('"+e.getMessage()+"')");
            }

            @Override
            public void onShareCancel() {
                ConchJNI.RunJS("app.SDK.fbShareCancel()");
            }
        });
    }

    public static void facebookFriendsInGame() {
        EskyfunSDK.getInstance().getFacebookFriendsInGame(new FbFriendCallback() {
            @Override
            public void onGetFriends(JSONArray array) {
                if (array != null) {
                    for (int i = 0; i < array.length(); i++) {
                        try {
                            JSONObject friend = array.getJSONObject(i);
                            String fbUserId = friend.getString("fbid");
                            String roleId = friend.getString("role_id");
                            String serverId = friend.getString("server_id");
                            String sdkUserId = friend.getString("user_id");
                            ConchJNI.RunJS("app.SDK.fbFriendInGame('"+friend.toString()+"')");
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                }
            }
        });
    }

    //Facebook 可邀请好友列表
    public static void facebookInvitableFriends() {
        EskyfunSDK.getInstance().getFacebookFriendsInvitable(new FbFriendCallback() {
            @Override
            public void onGetFriends(JSONArray array) {
                if (array != null) {
                    ConchJNI.RunJS("app.SDK.fbFriendsInvitable('"+array.toString()+"')");
                }else{
                    JSONArray resp = new JSONArray();
                    ConchJNI.RunJS("app.SDK.fbFriendsInvitable('"+resp.toString()+"')");
                }

            }
        });
    }

    public static void sendFacebookInvite(List<String> idList) {
        EskyfunSDK.getInstance().sendInvite(idList, new FbInviteCallback() {
            @Override
            public void onInviteSuccess() {
                // 邀请发送成功
                ConchJNI.RunJS("app.SDK.fbInviteSuc()");
            }

            @Override
            public void onInviteCancel() {
                // 邀请发送失败
                ConchJNI.RunJS("app.SDK.fbInviteFail()");
            }
        });
    }

    public static void onGameResLoading() {
        String resName = "";//@"GameResourceName";    // 正在下载的游戏资源名
        String resVersion =  "";//@"1.1.1";            // 正在下载的游戏资源版本
        long totalSize = 1000000;                // 正在下载的资源文件大小，单位为字节
        long currentSize = 0;                    // 已经下载的文件大小，单位为字节
        float speed = 0;                         // 当前下载速度，单位为kb/s
        EskyfunSDK.getInstance().onGameResourceLoading(resName, resVersion, totalSize, currentSize, speed);
    }

    public static void paymentDefault(String serverId,String serverName,String roleId,String roleName ,
                                      String productId ,String description,float amount,String extra ) {
        String currency = "USD";
//        roleId = Long.decode(roleId).toString();
        PaymentParam paymentParam = new PaymentParam(serverId, serverName, roleId, roleName, productId,
                description, amount, currency, extra);

//        ConchJNI.RunJS("alert('" + serverId+" "+ serverName+" "+ roleId +" "+ roleName+" "+ productId+" "+
//                description+" "+ amount+" "+ currency+" "+ extra +" "+ "')");
        EskyfunSDK.getInstance().paymentDefault(paymentParam);
    }
}
