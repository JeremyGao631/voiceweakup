package com.iflytek.util;
import com.iflytek.OfflineIvwMain;
import com.sun.jna.Callback;

public class Step2_ivw_ntf_handler implements Callback {
    //根据文档写回调方法
    public String cb_ivw_msg_proc(String sessionID, int msg, int param1, int param2,
                                  String info, String userData) {
        System.out.println("..............................................");
        System.out.println("回调函数返回的唤醒结果:"+info);
        String weakupResult = info;
        OfflineIvwMain.callbackResult = weakupResult;  // 将weakupResult的值存储到OfflineIvwMain类中的callbackResult变量中
        return weakupResult;
    }
}
