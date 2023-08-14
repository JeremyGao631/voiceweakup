package com.iflytek.util;
import javax.sound.sampled.AudioFormat;

public class Step4_audioFormat {
    //构造线程参数
    //16k采样率的16bit音频，一帧的大小为640B, 时长20ms
    /**
     sampleRate - 每秒样品数
     sampleSizeInBits - 每个样本中的位数
     channels - 通道数（1为mono，2为立体声等）
     signed - 表示数据是签名还是无符号
     bigEndian - 指示单个样本的数据是否以大字节顺序存储（ false表示小端）
     */
    public static AudioFormat getAudioFormat(AudioFormat audioFormat) {
        audioFormat=new AudioFormat(16000F, 16, 1,true,false);
        // true,false 指示是以 big-endian 顺序还是以 little-endian 顺序存储音频数据。
        return audioFormat;//构造具有线性 PCM 编码和给定参数的 AudioFormat。
    }
}


