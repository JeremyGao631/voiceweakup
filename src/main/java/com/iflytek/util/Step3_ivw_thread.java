package com.iflytek.util;
import com.iflytek.OfflineIvwMain;
import com.iflytek.service.Step1_ivw_dll;
import com.sun.jna.ptr.IntByReference;
import javax.sound.sampled.AudioInputStream;

public class Step3_ivw_thread extends Thread{
    public void run() {
        //登录参数
        String lgi_param = "appid = 7fbe4e99, work_dir = ./res";
        String ssb_param = "ivw_threshold=0:1450,sst=wakeup,ivw_shot_word=1,ivw_res_path =fo|res/ivw/wakeupresource.jet";
        int ret = Step1_ivw_dll.INSTANCE.MSPLogin(null, null, lgi_param);
        if (ret != 0) {//登录成功标志ret为0
            System.out.println("登录失败...请检查");
            System.exit(1);
        } else {
            System.out.println("请注意，唤醒语音需要根据控制台定义的唤醒词来进行唤醒...");
        }

        //开启会话
        IntByReference intByReference = new IntByReference(-100);
        String sessionId = Step1_ivw_dll.INSTANCE.QIVWSessionBegin(null, ssb_param, intByReference);
        if (intByReference.getValue() == 0) {//只有返回0为函数调用成功
            System.out.println("本次会话开启成功...,会话id为:" + sessionId);
        }

        int frameSize = 6400; //一帧的大小为6400B,其他的可能导致无法长时间唤醒
        int audioStatus = 1;//用来告知MSC音频发送是否完成

        //注册回调
        Step2_ivw_ntf_handler msgProcCb = new Step2_ivw_ntf_handler();//回调函数实例
        ret = Step1_ivw_dll.INSTANCE.QIVWRegisterNotify(sessionId, msgProcCb, null);
        if (ret == 0) {//返回为0则代表成功
            System.out.println("注册回调函数成功...");
        } else {
            System.out.println("注册函数返回的错误码" + ret);
        }

        //反复调用QIVWAudioWrite写音频方法，直到音频写完为止
        //long startTime=System.currentTimeMillis();
        try {
            while (true) {
                byte[] audioDataByteArray = new byte[frameSize];
                OfflineIvwMain.targetDataLine.open(OfflineIvwMain.audioFormat);
                OfflineIvwMain.targetDataLine.start();
                int len = new AudioInputStream(OfflineIvwMain.targetDataLine).read(audioDataByteArray);
                //long endTime=System.currentTimeMillis();
                if (len == -1) {//调用麦克风时候,这段将不会被执行...
                    //||(endTime-startTime>60*1000)
                    audioStatus = 4;//最后帧
                    ret = Step1_ivw_dll.INSTANCE.QIVWAudioWrite(sessionId, "".getBytes(), 0, audioStatus);
                    System.out.println("最后一帧返回的错误码:" + ret + ",即将执行退出...");
                    break;  //文件读完，跳出循环
                } else {
                    //反复调用
                    ret = Step1_ivw_dll.INSTANCE.QIVWAudioWrite(sessionId, audioDataByteArray, len, audioStatus);
                }
                audioStatus = 2;//中间帧
                if (ret != 0) {
                    System.err.println("出错了:" + ret);
                }
                Thread.sleep(200); //模拟人说话时间间隙
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        //终止会话
        ret = Step1_ivw_dll.INSTANCE.QIVWSessionEnd(sessionId, "正常终止");
        if (ret == 0) {
            System.out.println("本次会话正常终止...");
        }

        //执行退出
        ret = Step1_ivw_dll.INSTANCE.MSPLogout();
        if (ret == 0) {
            System.out.println("正常退出...");
        }else{
            System.out.println("异常退出...");
        }
    }
}