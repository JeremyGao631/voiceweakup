package com.iflytek.util;

import com.alibaba.nls.client.AccessToken;
import com.alibaba.nls.client.protocol.InputFormatEnum;
import com.alibaba.nls.client.protocol.NlsClient;
import com.alibaba.nls.client.protocol.SampleRateEnum;
import com.alibaba.nls.client.protocol.asr.SpeechTranscriber;
import com.alibaba.nls.client.protocol.asr.SpeechTranscriberListener;
import com.alibaba.nls.client.protocol.asr.SpeechTranscriberResponse;
import com.example.HttpUtil;
import com.iflytek.OfflineIvwMain;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.Channel;

import org.json.JSONObject;

import javax.sound.sampled.*;

import java.util.concurrent.LinkedBlockingQueue;

public class Sample {
    //设置请求rabbitmq队列名字
    private static final String QUEUE_NAME = "gwj_queue";

    private String res;

    private boolean stopListening; // 标志变量，控制是否停止监听识别结果的返回


    public String process() throws Exception {
        String appKey = "GE0Bz6tXVtoqmrk7";
        String id = "LTAI5tJpDtdivMesh8v6zSys";
        String secret = "sdkmkMVT4nZfptsoaZjq5U9z3J9wr8";
        String url = "wss://nls-gateway.cn-shanghai.aliyuncs.com/ws/v1";

        AccessToken accessToken = new AccessToken(id, secret);
        try {
            accessToken.apply();
            System.out.println("get token: " + ", expire time: " + accessToken.getExpireTime());
            NlsClient client;
            if (url.isEmpty()) {
                client = new NlsClient(accessToken.getToken());
            } else {
                client = new NlsClient(url, accessToken.getToken());
            }

            // 创建SpeechTranscriber
            SpeechTranscriber transcriber = new SpeechTranscriber(client, getTranscriberListener());
            transcriber.setAppKey(appKey);
            transcriber.setFormat(InputFormatEnum.PCM);
            transcriber.setSampleRate(SampleRateEnum.SAMPLE_RATE_16K);
            transcriber.setEnableIntermediateResult(false);
            transcriber.setEnablePunctuation(true);
            transcriber.setEnableITN(false);
            transcriber.start();

            // 获取实时语音流并发送
            LinkedBlockingQueue<byte[]> audioDataQueue = new LinkedBlockingQueue<>();
            Thread audioThread = new Thread(() -> {
                while (true) {
                    byte[] audioData = getRealtimeAudioDataFromMicrophone();
                    audioDataQueue.offer(audioData);
                }
            });
            audioThread.start();

            // 从audioDataQueue中获取语音流数据并发送
            byte[] audioData = null;
            do {
                if (!audioDataQueue.isEmpty()) {
                    audioData = audioDataQueue.take();
                    transcriber.send(audioData, audioData.length);
                } else {
                    Thread.sleep(100); // 休眠100毫秒
                }
            } while (audioData != null);

        } finally {

            if (res != null) {
                System.out.println("识别结果：" + res);
                // 调用接口
                Sample http = new Sample();

                // 设置请求参数
                String url1 = "http://39.99.229.73:10047/ci_intention/api/v3.2/syntactic_parsing";
                String token = "----";
                String userId = "admin";
                String question = res;
                String userGroupId = "zhongzi";
                String location = "北京";
                int recommendSwitch = 1;
                String postData = "{ \"token\": \"" + token + "\", \"user_id\": \"" + userId + "\", \"question\": \""
                        + question + "\", \"user_group_id\": \"" + userGroupId + "\", \"location\": \"" + location
                        + "\", \"recommend_switch\": " + recommendSwitch + " }";

                // 发起 POST 请求
                System.out.println(postData);
                String response = HttpUtil.postGeneralUrl(url1,"application/json",postData,"UTF-8");
                System.out.println("Response: " + response);

                // 创建连接工厂，并设置连接到RabbitMQ服务器
                ConnectionFactory factory = new ConnectionFactory();
                factory.setHost("localhost");
                factory.setUsername("guest");
                factory.setPassword("guest");

                // 创建连接和信道
                try (Connection connection = factory.newConnection();
                     Channel channel = connection.createChannel()) {
                    // 声明一个队列
                    channel.queueDeclare(QUEUE_NAME, false, false, false, null);
                    String message = new JSONObject(response).toString();;
                    // 发送消息到队列
                    channel.basicPublish("", QUEUE_NAME, null, message.getBytes("UTF-8"));
                    System.out.println(" [x] Sent '" + message + "'");
                }
            } else {
                System.out.println("识别失败");
            }


            return res;
        }
    }

    private SpeechTranscriberListener getTranscriberListener() {
        SpeechTranscriberListener listener = new SpeechTranscriberListener() {
            @Override
            public void onTranscriptionResultChange(SpeechTranscriberResponse response) {
                System.out.println("task_id: " + response.getTaskId() +
                        ", name: " + response.getName() +
                        ", status: " + response.getStatus() +
                        ", index: " + response.getTransSentenceIndex() +
                        ", result: " + response.getTransSentenceText() +
                        ", time: " + response.getTransSentenceTime());
            }

            @Override
            public void onTranscriberStart(SpeechTranscriberResponse response) {
                System.out.println("task_id: " + response.getTaskId() + ", name: " + response.getName() + ", status: " + response.getStatus());
            }

            @Override
            public void onSentenceBegin(SpeechTranscriberResponse response) {
                System.out.println("task_id: " + response.getTaskId() + ", name: " + response.getName() + ", status: " + response.getStatus());
            }

            @Override
            public void onSentenceEnd(SpeechTranscriberResponse response) {
                System.out.println("task_id: " + response.getTaskId() +
                        ", name: " + response.getName() +
                        ", status: " + response.getStatus() +
                        ", index: " + response.getTransSentenceIndex() +
                        ", result: " + response.getTransSentenceText() +
                        ", confidence: " + response.getConfidence() +
                        ", begin_time: " + response.getSentenceBeginTime() +
                        ", time: " + response.getTransSentenceTime());

                res = response.getTransSentenceText();
            }

            @Override
            public void onTranscriptionComplete(SpeechTranscriberResponse response) {
                System.out.println("task_id: " + response.getTaskId() + ", name: " + response.getName() + ", status: " + response.getStatus());
            }

            @Override
            public void onFail(SpeechTranscriberResponse response) {
                System.out.println("task_id: " + response.getTaskId() + ", status: " + response.getStatus() + ", status_text: " + response.getStatusText());
            }
        };

        return listener;
    }

    // 其他方法...
    private byte[] getRealtimeAudioDataFromMicrophone() {
        try {
            OfflineIvwMain.targetDataLine.start();
            // 设置音频参数
            AudioFormat format = OfflineIvwMain.audioFormat; // 使用已经定义好的音频格式

            // 打开音频输入设备
            if (!AudioSystem.isLineSupported(OfflineIvwMain.targetDataLine.getLineInfo())) {
                System.out.println("Line not supported!");
                return null;
            }
            OfflineIvwMain.targetDataLine.open(format);
            OfflineIvwMain.targetDataLine.start();

            // 读取音频数据
            int bufferSize = 3200;
            byte[] buffer = new byte[bufferSize];
            OfflineIvwMain.targetDataLine.read(buffer, 0, bufferSize);

            // 关闭音频输入流
            OfflineIvwMain.targetDataLine.stop();
            OfflineIvwMain.targetDataLine.close();

            return buffer;
        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }

    public static void main(String[] args) throws Exception {
        Sample sample = new Sample();
        sample.process();
    }

}
