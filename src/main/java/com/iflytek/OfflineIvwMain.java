package com.iflytek;
import com.iflytek.util.Step3_ivw_thread;
import com.iflytek.util.Step4_audioFormat;
import com.iflytek.util.Step2_ivw_ntf_handler;

import javax.sound.sampled.*;

import com.iflytek.util.Sample;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;



public class OfflineIvwMain extends JFrame{
    //录音相关参数
    public static AudioFormat audioFormat;
    public static TargetDataLine targetDataLine;
    public static String callbackResult;
    private Sample sample;

    private JLabel resultLabel; // 识别结果的标签
    private JLabel processingLabel; // “正在识别中...”标签
    private JLabel imageLabel; // 显示动态图片的标签
    private JPanel panel; // 面板


    public OfflineIvwMain() throws Exception {
        sample = new Sample();

        // 设置窗口大小
        setSize(500, 500);
        // 设置窗口标题
        setTitle("My GUI Application");
        // 设置窗口关闭时的默认操作
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        // 创建一个面板
        panel = new JPanel();
        // 设置面板布局为BoxLayout，并设置竖直方向
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));


        // 设定定时器，持续监听callbackResult是否有值
        Timer timer = new Timer(1000, new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                checkCallbackResult();

            }

        });
        timer.start();


        // 将面板添加到窗口
        add(panel, BorderLayout.CENTER);

        // 显示窗口
        setVisible(true);
    }

    private void checkCallbackResult() {
        if (callbackResult != null && !callbackResult.isEmpty()) {
            callbackResult = null;
            targetDataLine.stop();
            targetDataLine.close();

            // 创建“正在识别中...”标签并添加到面板中
            processingLabel = new JLabel("正在识别中...");
            processingLabel.setFont(new Font("宋体", Font.BOLD, 18));
            processingLabel.setHorizontalAlignment(JLabel.LEFT);
            panel.add(processingLabel);

            // 创建显示动态图片的标签
            ImageIcon imageIcon = new ImageIcon("C:/Users/Think centre/Desktop/火影.gif"); // 读取动态图片的文件
            imageLabel = new JLabel(imageIcon);
            // 将标签添加到面板中
            panel.add(imageLabel);

            panel.revalidate();
            panel.repaint();

            // 设置延时等待识别结果的返回
            new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        // 调用Sample类获取识别结果result
                        String result = sample.process();
                        System.out.println(result+"?????????????????????????????");
                        // 移除动态图片标签
                        panel.remove(imageLabel);
                        // 创建识别结果的标签，设置左对齐
                        resultLabel = new JLabel("识别结果：" + result);
                        resultLabel.setFont(new Font("宋体", Font.BOLD, 18));
                        resultLabel.setHorizontalAlignment(JLabel.LEFT);


                        panel.add(resultLabel);
                        panel.revalidate();
                        panel.repaint();

                    } catch (Exception ex) {
                        ex.printStackTrace();
                    }
                }
            }).start();


        }
    }

    public static void main(String[] args) throws Exception {
        // 创建GUI界面实例
        OfflineIvwMain OfflineIvwMain = new OfflineIvwMain();

        audioFormat = Step4_audioFormat.getAudioFormat(audioFormat);//构造具有线性 PCM 编码和给定参数的 AudioFormat。
        DataLine.Info dataLineInfo = new DataLine.Info(TargetDataLine.class, audioFormat);
        targetDataLine = (TargetDataLine) AudioSystem.getLine(dataLineInfo);
        Step3_ivw_thread myThread=new Step3_ivw_thread();
        myThread.start();


    }
}