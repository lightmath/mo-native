package mo;

import android.app.Activity;
import android.graphics.Color;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import layaair.game.browser.ConchJNI;
import com.ictitan.union.IctitanUnionSDK;
import com.ictitan.union.constant.UnionSDKCallbackCode;
import com.ictitan.union.entity.UnionSdkUser;
import com.ictitan.union.entity.IctitanUnionPaymentParam;
import com.ictitan.union.entity.IctitanUnionRoleInfoParam;
import com.ictitan.union.constant.RoleEventType;


public class JSBridge {
    public static Handler m_Handler = new Handler(Looper.getMainLooper());
    public static Activity mMainActivity = null;

    public static void showSplash() {
        m_Handler.post(
                new Runnable() {
                    public void run() {
                        MainActivity.mSplashDialog.show();
                    }
                });
    }

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
        MainActivity.isInited = true;
        if(MainActivity.user!= null){
            onLoginSuc();
        }else{
            sdkLogin();
        }
    }

    public static void onLoginSuc() {
        String userstr = userToCacheJson(MainActivity.user);
        Log.e("JSBridge", "onLoginSuc userstr="+userstr);
        ConchJNI.RunJS("app.SDK.onLoginSuc('"+userstr+"')");
    }

    public static String getUserId(){
        return MainActivity.UID;
    }

    public static String getToken(){
        return MainActivity.Token;
    }

    public static void sdkLogin() {
        IctitanUnionSDK.getInstance().login();
    }
    public static void sdkLogout() {
        Log.e("JSBridge","sdkLogout");
        MainActivity.clearToken();

        IctitanUnionSDK.getInstance().logout();
    }

    public static void onSdkLogoutBack() {
        ConchJNI.RunJS("app.SDK.onLogoutSuc()");
    }

    public static void selectGameServer(final String serverID ,final String serverName) {
        Log.e("JSBridge","serverID="+serverID);
//        IctitanUnionSDK.getInstance().reportGameServer(serverID, serverName);
    }

    public static void createGameRole( String serverId,
                                       String serverName,
                                       String roleId,
                                       String roleName,
                                       String profession) {

        roleId = Long.decode(roleId).toString();
        String level = "0";
//        ConchJNI.RunJS("alert('Create" + profession + " "+serverId + " "+ serverName + " " + roleId + " " + roleName +  "')");
        Log.e("JSBridge","Create="+profession + " "+serverId + " "+ serverName + " " + roleId + " " + roleName);
        IctitanUnionSDK.getInstance().reportRoleInfo(new IctitanUnionRoleInfoParam(RoleEventType.Create, serverId, serverName, roleId, roleName, profession, level));
    }

    public static void roleLevelUpgrade(        String profession ,
                                                String serverId,
                                                String serverName,
                                                String roleId,
                                                String roleName,
                                                String level) {

        roleId = Long.decode(roleId).toString();
        IctitanUnionSDK.getInstance().reportRoleInfo(new IctitanUnionRoleInfoParam(RoleEventType.LevelUpgrade, serverId, serverName, roleId, roleName, profession, level));
    }

    public static void roleReport(        String profession ,
                                          String serverId ,
                                          String serverName ,
                                          String roleId ,
                                          String roleName,
                                          String level ) {
        roleId = Long.decode(roleId).toString();
//        ConchJNI.RunJS("alert('EnterGame" + profession + " "+serverId + " "+ serverName + " " + roleId
//                + " " + roleName + " "   + level +  "')");
        Log.e("JSBridge","EnterGame " + profession + " "+serverId + " "+ serverName + " " + roleId
                + " " + roleName + " "   + level );
        IctitanUnionSDK.getInstance().reportRoleInfo(new IctitanUnionRoleInfoParam(RoleEventType.EnterGame, serverId, serverName, roleId, roleName, profession, level));
    }

    public static void facebookShare() {
        String shareId = "1001";
        Map<String,Object> shareParams = new HashMap<String ,Object>();
        shareParams.put("displayName", "你好啊");
        Log.e("JSBridge","facebookShare");
        IctitanUnionSDK.getInstance().shareToSocialNetwork(shareId, shareParams);
    }

    public static void onFaceBookShareBack(int code, String result) {
        switch (code) {
            case UnionSDKCallbackCode.CODE_SHARE_SUCCESS:
                Log.e("UnionShareSuccess", "share success:" + result);
//                ConchJNI.RunJS("app.SDK.fbShareSuc('"+result+"')");
                break;
            case UnionSDKCallbackCode.CODE_SHARE_FAIL:
                Log.e("UnionShareFail", "share fail:" + result);
//                ConchJNI.RunJS("app.SDK.fbShareError('"+result+"')");
                break;
            case UnionSDKCallbackCode.CODE_SHARE_CANCEL:
                Log.e("UnionShareCancel", "share cancel:" + result);
//                ConchJNI.RunJS("app.SDK.fbShareCancel()");
                break;
            default:
                break;
        }
    }

    public static void facebookFriendsInGame() {
//        EskyfunSDK.getInstance().getFacebookFriendsInGame(new FbFriendCallback() {
//            @Override
//            public void onGetFriends(JSONArray array) {
//                if (array != null) {
//                    for (int i = 0; i < array.length(); i++) {
//                        try {
//                            JSONObject friend = array.getJSONObject(i);
//                            String fbUserId = friend.getString("fbid");
//                            String roleId = friend.getString("role_id");
//                            String serverId = friend.getString("server_id");
//                            String sdkUserId = friend.getString("user_id");
//                            ConchJNI.RunJS("app.SDK.fbFriendInGame('"+friend.toString()+"')");
//                        } catch (JSONException e) {
//                            e.printStackTrace();
//                        }
//                    }
//                }
//            }
//        });
    }

    //Facebook 可邀请好友列表
    public static void facebookInvitableFriends() {
//        EskyfunSDK.getInstance().getFacebookFriendsInvitable(new FbFriendCallback() {
//            @Override
//            public void onGetFriends(JSONArray array) {
//                if (array != null) {
//                    ConchJNI.RunJS("app.SDK.fbFriendsInvitable('"+array.toString()+"')");
//                }else{
//                    JSONArray resp = new JSONArray();
//                    ConchJNI.RunJS("app.SDK.fbFriendsInvitable('"+resp.toString()+"')");
//                }
//
//            }
//        });
    }

    public static void sendFacebookInvite(List<String> idList) {
//        EskyfunSDK.getInstance().sendInvite(idList, new FbInviteCallback() {
//            @Override
//            public void onInviteSuccess() {
//                // 邀请发送成功
//                ConchJNI.RunJS("app.SDK.fbInviteSuc()");
//            }
//
//            @Override
//            public void onInviteCancel() {
//                // 邀请发送失败
//                ConchJNI.RunJS("app.SDK.fbInviteFail()");
//            }
//        });
    }

    public static void onGameResLoading() {
//        String resName = "";//@"GameResourceName";    // 正在下载的游戏资源名
//        String resVersion =  "";//@"1.1.1";            // 正在下载的游戏资源版本
//        long totalSize = 1000000;                // 正在下载的资源文件大小，单位为字节
//        long currentSize = 0;                    // 已经下载的文件大小，单位为字节
//        float speed = 0;                         // 当前下载速度，单位为kb/s
//        EskyfunSDK.getInstance().onGameResourceLoading(resName, resVersion, totalSize, currentSize, speed);
    }

    public static void paymentDefault(String serverId,String serverName,String roleId,String roleName ,String roleLevel,String roleProfession,
                                      String productId ,String description,String amount,String extra ) {
//        roleId = Long.decode(roleId).toString();
        String currency = "USD";
        float amountf = Float.parseFloat(amount);
// 进行支付
        Log.e("JSBridge","paymentDefault "+serverId+" "+ serverName+" "+ roleId +" "+ roleName+" "+ productId+" "+
                description+" amount="+ amountf+" "+ currency+" "+ extra);

        IctitanUnionSDK.getInstance().pay(new IctitanUnionPaymentParam(serverId, serverName, roleId, roleName, roleLevel, roleProfession,
                productId, description, amountf, currency, extra));
    }

//    public static void paymentDefault(String serverId,String serverName,String roleId,String roleName ,String roleLevel,String roleProfession,
//                                      String productId ,String description,int amount,String extra ) {
////        roleId = Long.decode(roleId).toString();
//        String currency = "USD";
//// 进行支付
//        Log.e("JSBridge","paymentDefault "+serverId+" "+ serverName+" "+ roleId +" "+ roleName+" "+ productId+" "+
//                description+" amount="+ amount+" "+ currency+" "+ extra);
//
////        IctitanUnionSDK.getInstance().pay(new IctitanUnionPaymentParam(serverId, serverName, roleId, roleName, roleLevel, roleProfession,
////                productId, description, amount, currency, extra));
////        ConchJNI.RunJS("alert('" + serverId+" "+ serverName+" "+ roleId +" "+ roleName+" "+ productId+" "+
////                description+" "+ amount+" "+ currency+" "+ extra +" "+ "')");
//    }

    /**
     * 把一个json格式转换成UnionSdkUser对象
     * @param var1
     * @return
     */
    public static UnionSdkUser stringToCacheUser(String var1) {
        try {
            UnionSdkUser user = new UnionSdkUser();
            JSONObject var2 = new JSONObject(var1);
            user.setAppId(var2.optString("appId"));
            user.setUserId(var2.optString("userId"));
            user.setToken(var2.optString("token"));
            return user;
        } catch (Exception var3) {
            return null;
        }
    }

    /**
     * 把UnionSdkUser对象转换成一个json字符串
     * @param user
     * @return
     */
    public static String userToCacheJson(UnionSdkUser user) {
        JSONObject var1 = new JSONObject();
        try {
            var1.put("userId", user.getUserId());
            var1.put("appId", user.getAppId());
            var1.put("token", user.getToken());
        } catch (JSONException var3) {
            var3.printStackTrace();
        }

        return var1.toString();
    }


}
