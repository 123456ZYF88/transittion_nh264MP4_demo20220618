package org.example.transittion;

import org.bytedeco.ffmpeg.avcodec.AVCodecParameters;
import org.bytedeco.ffmpeg.avformat.AVFormatContext;
import org.bytedeco.ffmpeg.avformat.AVStream;
import org.bytedeco.ffmpeg.global.avcodec;
import org.bytedeco.javacv.FFmpegFrameGrabber;
import org.bytedeco.javacv.FFmpegFrameRecorder;
import org.bytedeco.javacv.FFmpegLogCallback;
import org.bytedeco.javacv.Frame;
import org.bytedeco.javacv.FrameGrabber;
import org.junit.Test;


/**
 * @author zhangyifan
 * @version 8.0
 * @description: 转码器
 * @date 2022/6/18 8:45
 */
public class VideoUtil {
    /**
     * 视频转码函数(仅转码)
     *
     * @param inputfile  原始视频文件完整路径
     * @param outputfile 目标视频文件完整保存路径（必须完整文件名，即包含格式后缀，推荐格式后缀为.mp4）
     * @throws Exception 异常
     */
    public static void videoConvert(String inputfile, String outputfile) throws Exception
    {
        if (outputfile.lastIndexOf('.') < 0)
        {
            throw new Exception("Error! Output file format undetected!");
        }
        String format = outputfile.substring(outputfile.lastIndexOf('.'));

        FFmpegLogCallback.set();
        Frame frame;
        FFmpegFrameGrabber grabber = new FFmpegFrameGrabber(inputfile);
        FFmpegFrameRecorder recorder = null;

        try
        {
            System.out.println("开始初始化帧抓取器");

            // 初始化帧抓取器，例如数据结构（时间戳、编码器上下文、帧对象等），
            // 如果入参等于true，还会调用avformat_find_stream_info方法获取流的信息，放入AVFormatContext类型的成员变量oc中
            grabber.start(true);

            System.out.println("帧抓取器初始化完成");

            // grabber.start方法中，初始化的解码器信息存在放在grabber的成员变量oc中
            AVFormatContext avformatcontext = grabber.getFormatContext();

            // 文件内有几个媒体流（一般是视频流+音频流）
            int streamNum = avformatcontext.nb_streams();

            // 没有媒体流就不用继续了
            if (streamNum < 1)
            {
                System.out.println("文件内不存在媒体流");
                throw new Exception("Error! There is no media stream in the file!");
            }

            // 取得视频的帧率
            int framerate = (int) grabber.getVideoFrameRate();

            System.out.printf("视频帧率[%d]，视频时长[%d]秒，媒体流数量[%d]\r\n", framerate, avformatcontext.duration() / 1000000,
                    avformatcontext.nb_streams());

            // 遍历每一个流，检查其类型
            for (int i = 0; i < streamNum; i++)
            {
                AVStream avstream = avformatcontext.streams(i);
                AVCodecParameters avcodecparameters = avstream.codecpar();
                System.out.printf("流的索引[%d]，编码器类型[%d]，编码器ID[%d]\r\n", i, avcodecparameters.codec_type(),
                        avcodecparameters.codec_id());
            }

            // 视频宽度
            int frameWidth = grabber.getImageWidth();
            // 视频高度
            int frameHeight = grabber.getImageHeight();
            // 音频通道数量
            int audiochannels = grabber.getAudioChannels();

            System.out.printf("视频宽度[%d]，视频高度[%d]，音频通道数[%d]\r\n", frameWidth, frameHeight, audiochannels);

            recorder = new FFmpegFrameRecorder(outputfile, frameWidth, frameHeight, audiochannels);
            recorder.setVideoCodec(avcodec.AV_CODEC_ID_H264);

            recorder.setFormat(format);
            // 使用原始视频的码率，若需要则自行修改码率
            recorder.setVideoBitrate(grabber.getVideoBitrate());

            // 一秒内的帧数，帧率
            recorder.setFrameRate(framerate);

            // 两个关键帧之间的帧数
            recorder.setGopSize(framerate);

            // 设置音频通道数，与视频源的通道数相等
            recorder.setAudioChannels(grabber.getAudioChannels());

            recorder.start();

            int videoframenum = 0;
            int audioframenum = 0;
            int dataframenum = 0;

            // 持续从视频源取帧
            while (null != (frame = grabber.grab()))
            {
                // 有图像，就把视频帧加一
                if (null != frame.image)
                {
                    videoframenum++;
                    // 取出的每一帧，都保存到视频
                    recorder.record(frame);
                }

                // 有声音，就把音频帧加一
                if (null != frame.samples)
                {
                    audioframenum++;
                    // 取出的每一帧，都保存到视频
                    recorder.record(frame);
                }

                // 有数据，就把数据帧加一
                if (null != frame.data)
                {
                    dataframenum++;
                }
            }

            System.out.printf("转码完成，视频帧[%d]，音频帧[%d]，数据帧[%d]\r\n", videoframenum, audioframenum, dataframenum);

        } catch (Exception e)
        {
            // e.printStackTrace();
            throw e;
        } finally
        {
            if (recorder != null)
            {
                try
                {
                    recorder.close();
                } catch (Exception e)
                {
                    // System.out.println("recorder.close异常" + e);
                    throw e;
                }
            }

            try
            {
                grabber.close();
            } catch (FrameGrabber.Exception e)
            {
                // System.out.println("frameGrabber.close异常" + e);
                throw e;
            }
        }
    }
    @Test
    public void VideoUti(){
        try
        {
            // videoConvert函数，根据outputfile的格式后缀设置转码后的视频格式，推荐使用mp4格式后缀
            VideoUtil.videoConvert("C:\\Users\\15836\\Desktop\\test类型.MP4", "C:\\Users\\15836\\Desktop\\test1.MP4");
        } catch (java.lang.Exception e)
        {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        System.out.println("执行完毕！");
    }

}
