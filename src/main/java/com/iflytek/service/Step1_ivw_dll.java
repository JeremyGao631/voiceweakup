package com.iflytek.service;
import com.iflytek.util.Step2_ivw_ntf_handler;
import com.sun.jna.Library;
import com.sun.jna.Native;
import com.sun.jna.ptr.IntByReference;

public interface Step1_ivw_dll extends Library {
    /**
     * 重点：
     * 1.char *   对应  String
     * 2.int *    对应  IntByReference
     * 3.void *   对应  Pointer或byte[]
     * 4.int      对应  int
     * 5.无参     对应  无参
     * 6.回调函数  对应  根据文档自定义回调函数，实现接口Callback
     */
    //加载dll动态库并实例化,从而使用其内部的方法
    Step1_ivw_dll INSTANCE = (Step1_ivw_dll) Native.loadLibrary
            ("C:\\Users\\Think centre\\IdeaProjects\\voice\\res\\msc_x64.dll", Step1_ivw_dll.class);

    //定义登录方法    MSPLogin(const char *usr, const char *pwd, const char *params)
    public int MSPLogin(String usr, String pwd, String params);

    //定义开始方法    QIVWSessionbegin(const char *grammarList, const char *params, int *errorCode)
    public String QIVWSessionBegin(String grammarList, String params, IntByReference errorCode);

    //定义写音频方法  QIVWAudioWrite(const char *sessionID, const void *audioData, unsigned int audioLen, int audioStatus)
    public int QIVWAudioWrite(String sessionID, byte[] audioData, int audioLen, int audioStatus);

    //定义结束方法    QIVWSessionEnd(const char *sessionID, const char *hints)
    public int QIVWSessionEnd(String sessionID, String hints);

    //定义获取结果方法 QIVWRegisterNotify(const char *sessionID, ivw_ntf_handler msgProcCb, void *userData)
    public int QIVWRegisterNotify(String sessionID, Step2_ivw_ntf_handler msgProcCb, byte[] userData);

    //定义退出方法     MSPLogout()
    public int MSPLogout();
}